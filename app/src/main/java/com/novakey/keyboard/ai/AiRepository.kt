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
 * Routing Rules:
 * - Deterministic tasks (e.g. grammar typos): LOCAL heuristics first (0ms) → ON_DEVICE → CLOUD → LOCAL
 * - Generative tasks (Rewrite, Tone, Context/Reply/Ask, Translate, Custom):
 *   ON_DEVICE (Gemini Nano) → CLOUD (Gemini via Server Proxy) → LOCAL (Offline Fallback)
 */
class AiRepository(
    context: Context? = null,
    val localProvider: LocalAiProvider = LocalAiProvider(),
    val onDeviceProvider: OnDeviceAiProvider = OnDeviceAiProvider(context),
    val cloudProvider: CloudAiProvider = CloudAiProvider(),
    var isCloudEnabled: Boolean = true
) {

    suspend fun getOnDeviceStatus(): OnDeviceStatus {
        return onDeviceProvider.checkAvailability()
    }

    suspend fun requestOnDeviceDownload(): Boolean {
        return onDeviceProvider.requestModelDownload()
    }

    /**
     * Grammar Routing: LOCAL heuristics (0ms) → ON_DEVICE → CLOUD → LOCAL
     */
    suspend fun checkGrammar(text: String, context: AiContext): AiResult<AiGrammarResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in password/PIN fields for privacy.")
        }

        // 1. Try Local heuristic rule engine first (0ms, 100% offline) for clear typos/grammar issues
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
     * Rewrite Routing: ON_DEVICE → CLOUD → LOCAL
     */
    suspend fun rewrite(text: String, mode: RewriteMode, context: AiContext): AiResult<AiRewriteResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        // 1. Try On-Device Gemini Nano for generative rewriting
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.rewrite(text, mode, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 2. Try Cloud fallback via Server Proxy
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.rewrite(text, mode, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        // 3. Fallback to Local heuristic engine
        return localProvider.rewrite(text, mode, context)
    }

    /**
     * Tone Routing: ON_DEVICE → CLOUD → LOCAL
     */
    suspend fun changeTone(text: String, tone: ToneCategory, context: AiContext): AiResult<AiToneResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        // 1. Try On-Device Gemini Nano
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.changeTone(text, tone, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 2. Try Cloud fallback via Server Proxy
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.changeTone(text, tone, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        // 3. Fallback to Local tone engine
        return localProvider.changeTone(text, tone, context)
    }

    /**
     * Translation Routing: ON_DEVICE → CLOUD → LOCAL
     */
    suspend fun translate(text: String, targetLang: String, context: AiContext): AiResult<AiContextResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        val actionKey = if (targetLang.equals("en", ignoreCase = true)) "translate_en" else "translate_bn"

        // 1. Try On-Device Gemini Nano
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.processContextAction(text, actionKey, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 2. Try Cloud fallback via Server Proxy
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.processContextAction(text, actionKey, context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        // 3. Fallback to Local basic/transliteration
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

        return localProvider.processContextAction(text, actionKey, context)
    }

    /**
     * Context Action Routing: ON_DEVICE → CLOUD → LOCAL
     * Handles: reply, write, explain, ask
     */
    suspend fun processContextAction(text: String, actionType: String, context: AiContext): AiResult<AiContextResult> {
        if (context.isSensitiveField) {
            return AiResult.Error("AI disabled in sensitive fields.")
        }

        // 1. Try On-Device Gemini Nano
        if (onDeviceProvider.checkAvailability() == OnDeviceStatus.AVAILABLE) {
            val onDeviceRes = onDeviceProvider.processContextAction(text, actionType, context)
            if (onDeviceRes is AiResult.Success) return onDeviceRes
        }

        // 2. Try Cloud fallback via Server Proxy
        if (isCloudEnabled) {
            val cloudRes = cloudProvider.processContextAction(text, actionKey = actionType, context = context)
            if (cloudRes is AiResult.Success) return cloudRes
        }

        // 3. Fallback to Local heuristics
        return localProvider.processContextAction(text, actionType, context)
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
