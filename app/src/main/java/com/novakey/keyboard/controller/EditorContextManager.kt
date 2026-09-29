package com.novakey.keyboard.controller

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.novakey.keyboard.ai.AiContext

/**
 * Snapshot of the editor context extracted before an AI operation.
 * Used for safe target extraction and concurrency verification before text commits.
 */
data class SafeEditorContext(
    val selectedText: String,
    val textBeforeCursor: String,
    val textAfterCursor: String,
    val currentSentence: String,
    val targetText: String,
    val targetLength: Int,
    val isSelection: Boolean,
    val packageName: String?,
    val isSensitive: Boolean,
    val contextToken: String
)

/**
 * Reusable manager for extracting context from InputConnection with strict
 * privacy guards (password/PIN/OTP fields) and input safety verification.
 */
object EditorContextManager {

    // Maximum characters allowed for context extraction to strictly guarantee
    // staying under the Prompt API limit (< 4000 tokens)
    private const val MAX_CONTEXT_CHARS = 1000

    /**
     * Determines whether the active editor field contains sensitive content
     * such as passwords, visible passwords, PINs, or OTP/two-factor codes.
     */
    fun isSensitiveField(info: EditorInfo?): Boolean {
        if (info == null) return false

        val inputType = info.inputType
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val inputClass = inputType and InputType.TYPE_MASK_CLASS

        // 1. Password variations
        val isPassword = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                (inputClass == InputType.TYPE_CLASS_NUMBER && variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)

        if (isPassword) return true

        // 2. OTP / Two-Factor Code detection via autofillHints or field names
        info.hintText?.let { hint ->
            val h = hint.toString().lowercase()
            if (h.contains("otp") || h.contains("one-time") || h.contains("verification code") || h.contains("security code")) {
                return true
            }
        }

        info.fieldName?.let { name ->
            val n = name.lowercase()
            if (n.contains("otp") || n.contains("password") || n.contains("pin") || n.contains("cvv")) {
                return true
            }
        }

        return false
    }

    /**
     * Safely retrieves text context from InputConnection.
     * Enforces size limits (< 1000 chars) and returns empty/blocked context for sensitive fields.
     */
    fun extractContext(ic: InputConnection?, info: EditorInfo?): SafeEditorContext? {
        if (ic == null) return null

        val isSensitive = isSensitiveField(info)
        val packageName = info?.packageName

        if (isSensitive) {
            return SafeEditorContext(
                selectedText = "",
                textBeforeCursor = "",
                textAfterCursor = "",
                currentSentence = "",
                targetText = "",
                targetLength = 0,
                isSelection = false,
                packageName = packageName,
                isSensitive = true,
                contextToken = "SENSITIVE_BLOCKED"
            )
        }

        // Priority 1: Use selected text (capped to MAX_CONTEXT_CHARS)
        val rawSelected = ic.getSelectedText(0)?.toString() ?: ""
        val selected = rawSelected.take(MAX_CONTEXT_CHARS)

        // Priority 2: Safely read small window around cursor
        val before = (ic.getTextBeforeCursor(250, 0)?.toString() ?: "").takeLast(250)
        val after = (ic.getTextAfterCursor(100, 0)?.toString() ?: "").take(100)

        // Extract current sentence around cursor
        val sentence = extractSentenceAroundCursor(before, after)

        val targetText: String
        val isSelection: Boolean
        if (selected.isNotBlank()) {
            targetText = selected.trim()
            isSelection = true
        } else if (sentence.isNotBlank()) {
            targetText = sentence.trim()
            isSelection = false
        } else {
            targetText = before.takeLast(150).trim()
            isSelection = false
        }

        val token = "${packageName}_${before.takeLast(20)}_${after.take(20)}_${targetText.hashCode()}"

        return SafeEditorContext(
            selectedText = selected,
            textBeforeCursor = before,
            textAfterCursor = after,
            currentSentence = sentence,
            targetText = targetText,
            targetLength = targetText.length,
            isSelection = isSelection,
            packageName = packageName,
            isSensitive = false,
            contextToken = token
        )
    }

    /**
     * Converts a SafeEditorContext into an AiContext for the AI repository.
     */
    fun toAiContext(safeContext: SafeEditorContext, activeWord: String): AiContext {
        return AiContext(
            textBeforeCursor = safeContext.textBeforeCursor,
            selectedText = safeContext.selectedText,
            activeWord = activeWord,
            isSensitiveField = safeContext.isSensitive,
            packageName = safeContext.packageName
        )
    }

    /**
     * Verifies that the editor context is still valid and has not changed
     * significantly during asynchronous AI inference.
     * Prevents overwriting unrelated text if selection, cursor, or package changed.
     */
    fun canSafelyApply(original: SafeEditorContext?, ic: InputConnection?, currentInfo: EditorInfo?): Boolean {
        if (original == null || ic == null) return false
        if (original.isSensitive) return false

        // Check package name consistency
        if (original.packageName != null && currentInfo?.packageName != null) {
            if (original.packageName != currentInfo.packageName) {
                return false // Switched apps or editor views
            }
        }

        // Check selection consistency if it was a selection replacement
        if (original.isSelection) {
            val currentSelected = ic.getSelectedText(0)?.toString() ?: ""
            return currentSelected.trim() == original.selectedText.trim()
        }

        // Check text immediately before cursor for sentence/phrase replacements
        val currentBefore = ic.getTextBeforeCursor(original.targetLength + 10, 0)?.toString() ?: ""
        return currentBefore.trimEnd().endsWith(original.targetText) || currentBefore.contains(original.targetText.take(20))
    }

    private fun extractSentenceAroundCursor(before: String, after: String): String {
        val lastSentenceBreak = before.lastIndexOfAny(charArrayOf('.', '!', '?', '\n'))
        val sentenceStart = if (lastSentenceBreak >= 0) lastSentenceBreak + 1 else 0
        val beforePart = before.substring(sentenceStart)

        val nextSentenceBreak = after.indexOfAny(charArrayOf('.', '!', '?', '\n'))
        val afterPart = if (nextSentenceBreak >= 0) after.substring(0, nextSentenceBreak) else after

        return (beforePart + afterPart).trim()
    }
}
