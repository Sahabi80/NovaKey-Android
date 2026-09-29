package com.novakey.keyboard.ai

import android.content.Context
import com.novakey.keyboard.ai.model.AiContextResult
import com.novakey.keyboard.ai.model.AiGrammarResult
import com.novakey.keyboard.ai.model.AiResult
import com.novakey.keyboard.ai.model.AiRewriteResult
import com.novakey.keyboard.ai.model.AiToneResult
import com.novakey.keyboard.ai.model.OnDeviceStatus
import com.novakey.keyboard.ai.model.ProviderSource
import com.novakey.keyboard.ai.model.RewriteMode
import com.novakey.keyboard.engine.banglish.BanglishConverter
import com.novakey.keyboard.engine.tone.ToneCategory

/**
 * NovaKey Intelligent Hybrid AI Router
 *
 * Routing Rules (Phase 5 Specification):
 * - Grammar:        LOCAL → ON_DEVICE → CLOUD
 * - Rewrite:        LOCAL → ON_DEVICE → CLOUD
 * - Tone:           LOCAL → ON_DEVICE → CLOUD
 * - Translation:    LOCAL/basic → ON_DEVICE → CLOUD
 * - Reply / Write / Explain: LOCAL → ON_DEVICE → CLOUD
 * - Custom Prompt:  ON_DEVICE → CLOUD → LOCAL
 */
class AiRepository(
    context: Context? = null,
    val localProvider: LocalAiProvider = LocalAiProvider(),
    val onDeviceProvider: OnDeviceAiProvider = OnDeviceAiProvider(context),
    val cloudProvider: CloudAiProvider = CloudAiProvider(),
    var isCloudEnabled: Boolean = false
) {

    suspend fun getOnDeviceStatus(): OnDeviceStatus {
        return onDeviceProvider.checkAvailability()
    }

    suspend fun requestOnDeviceDownload(): Boolean {
        return onDeviceProvider.requestModelDownload()
    }

    /**
     * Grammar Routing: LOCAL → ON_DEVICE → CLOUD
     */
    suspend fun checkGrammar(text: String, context: AiContext): AiResult<AiGrammarResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in password/PIN fields for privacy.")
        }

        // 1. Try Local heuristic rule engine first (0ms, 100% offline)
        val localRes = localProvider.checkGrammar(text, context)
        if (localRes is AiResult.Success && localRes.data.correctedText != text) {
            return localRes
        }

        // 2. Try On-Device Gemini Nano via ML Kit GenAI Prompt API
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.checkGrammar(text, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 3. Optional Cloud fallback if enabled
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.checkGrammar(text, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        return localRes
    }

    /**
     * Rewrite Routing: LOCAL → ON_DEVICE → CLOUD
     */
    suspend fun rewrite(text: String, mode: RewriteMode, context: AiContext): AiResult<AiRewriteResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        // 1. Try Local heuristic first
        val localRes = localProvider.rewrite(text, mode, context)

        // 2. Try On-Device Gemini Nano for richer generation
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.rewrite(text, mode, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 3. Optional Cloud fallback
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.rewrite(text, mode, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        return localRes
    }

    /**
     * Tone Routing: LOCAL → ON_DEVICE → CLOUD
     */
    suspend fun changeTone(text: String, tone: ToneCategory, context: AiContext): AiResult<AiToneResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        // 1. Try Local tone engine
        val localRes = localProvider.changeTone(text, tone, context)

        // 2. Try On-Device Gemini Nano
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.changeTone(text, tone, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 3. Optional Cloud fallback
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.changeTone(text, tone, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        return localRes
    }

    /**
     * Translation Routing: LOCAL/basic → ON_DEVICE → CLOUD
     */
    suspend fun translate(text: String, targetLang: String, context: AiContext): AiResult<AiContextResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        // 1. Local basic (Banglish to Bengali)
        if (targetLang.equals("bn", ignoreCase = true) || targetLang.contains("bangla", ignoreCase = true)) {
            val converted = BanglishConverter.convert(text)
            if (converted.isNotBlank() && converted != text) {
                return AiResult.Success(
                    AiContextResult(
                        query = text,
                        responseText = converted,
                        actionType = "translate",
                        explanation = "Local Banglish transliteration engine",
                        providerSource = ProviderSource.LOCAL
                    )
                )
            }
        }

        // 2. On-Device Gemini Nano
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val actionKey = if (targetLang.equals("en", ignoreCase = true)) "translate_en" else "translate_bn"
            val onDeviceRes = onDeviceProvider.processContextAction(text, actionKey, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 3. Optional Cloud fallback
        if (isCloudEnabled) {
            val actionKey = if (targetLang.equals("en", ignoreCase = true)) "translate_en" else "translate_bn"
            val cloudRes = cloudProvider.processContextAction(text, actionKey, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        val actionKey = if (targetLang.equals("en", ignoreCase = true)) "translate_en" else "translate_bn"
        return localProvider.processContextAction(text, actionKey, context)
    }

    /**
     * Context Action Routing: LOCAL → ON_DEVICE → CLOUD
     * Handles: reply, write, explain, ask
     */
    suspend fun processContextAction(text: String, actionType: String, context: AiContext): AiResult<AiContextResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        // 1. Try Local heuristics first
        val localRes = localProvider.processContextAction(text, actionType, context)
        if (localRes is AiResult.Success && !localRes.data.responseText.startsWith("Processed locally")) {
            return localRes
        }

        // 2. Try On-Device Gemini Nano
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.processContextAction(text, actionType, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 3. Optional Cloud fallback
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.processContextAction(text, actionType, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        return localRes
    }

    /**
     * Custom Prompt Routing: ON_DEVICE → CLOUD → LOCAL
     */
    suspend fun customPrompt(text: String, prompt: String, context: AiContext): AiResult<AiContextResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        // 1. Try On-Device Gemini Nano first
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.customPrompt(text, prompt, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 2. Try Cloud AI second
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.customPrompt(text, prompt, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        // 3. Fallback to Local heuristic instruction matcher
        return localProvider.customPrompt(text, prompt, context)
    }
}
