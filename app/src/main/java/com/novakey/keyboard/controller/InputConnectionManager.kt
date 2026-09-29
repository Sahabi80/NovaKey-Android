package com.novakey.keyboard.controller

import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * Manages all low-level text interactions with the host application's InputConnection.
 */
class InputConnectionManager {

    private var inputConnection: InputConnection? = null
    private var currentEditorInfo: EditorInfo? = null

    fun updateInputConnection(ic: InputConnection?, info: EditorInfo?) {
        this.inputConnection = ic
        this.currentEditorInfo = info
    }

    /**
     * Commits a sequence of characters at the current cursor position.
     */
    fun commitText(text: String) {
        inputConnection?.commitText(text, 1)
    }

    /**
     * Deletes one character before the current cursor position.
     */
    fun deleteBackward() {
        val ic = inputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    /**
     * Commits a standard space.
     */
    fun commitSpace() {
        commitText(" ")
    }

    /**
     * Executes the appropriate Enter / Action key based on the EditorInfo imeOptions.
     */
    fun performEnterAction() {
        val ic = inputConnection ?: return
        val info = currentEditorInfo

        if (info != null && (info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0) {
            val action = info.imeOptions and EditorInfo.IME_MASK_ACTION
            if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                ic.performEditorAction(action)
                return
            }
        }

        // Fallback to inserting newline
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    /**
     * Retrieves up to [maxChars] characters before the current cursor position.
     */
    fun getTextBeforeCursor(maxChars: Int = 100): String {
        return inputConnection?.getTextBeforeCursor(maxChars, 0)?.toString() ?: ""
    }

    /**
     * Retrieves currently selected text, if any.
     */
    fun getSelectedText(): String {
        return inputConnection?.getSelectedText(0)?.toString() ?: ""
    }

    /**
     * Extracts the active word currently being typed directly before the cursor.
     * Returns Pair(word, length).
     */
    fun getCurrentActiveWord(): Pair<String, Int> {
        val before = getTextBeforeCursor(50)
        if (before.isEmpty()) return Pair("", 0)

        // Find boundary backwards
        var i = before.length - 1
        while (i >= 0 && !before[i].isWhitespace() && before[i] !in ".,!?;:\"'()[]{}") {
            i--
        }
        val word = before.substring(i + 1)
        return Pair(word, word.length)
    }

    /**
     * Replaces the word currently before the cursor with a replacement candidate.
     */
    fun replaceCurrentWord(replacement: String) {
        val ic = inputConnection ?: return
        val (_, length) = getCurrentActiveWord()
        if (length > 0) {
            ic.deleteSurroundingText(length, 0)
        }
        ic.commitText(replacement, 1)
    }

    /**
     * Atomically replaces the current text selection or preceding word with the replacement string.
     */
    fun replaceCurrentOrSelected(replacement: String, charactersToDeleteBefore: Int = 0) {
        val ic = inputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText(replacement, 1)
        } else if (charactersToDeleteBefore > 0) {
            ic.deleteSurroundingText(charactersToDeleteBefore, 0)
            ic.commitText(replacement, 1)
        } else {
            ic.commitText(replacement, 1)
        }
    }

    /**
     * Inspects EditorInfo to determine if the target field is sensitive (passwords, PINs, etc.)
     */
    fun isSensitiveField(): Boolean {
        val info = currentEditorInfo ?: return false
        val inputType = info.inputType
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        return variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER && (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    }

    /**
     * Detects numeric or phone-only fields.
     */
    fun isNumericOrPhone(): Boolean {
        val info = currentEditorInfo ?: return false
        val inputClass = info.inputType and InputType.TYPE_MASK_CLASS
        return inputClass == InputType.TYPE_CLASS_NUMBER || inputClass == InputType.TYPE_CLASS_PHONE
    }
}
