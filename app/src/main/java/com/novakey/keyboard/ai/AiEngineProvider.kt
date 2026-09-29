package com.novakey.keyboard.ai

import com.novakey.keyboard.ai.model.AiContextResult
import com.novakey.keyboard.ai.model.AiGrammarResult
import com.novakey.keyboard.ai.model.AiResult
import com.novakey.keyboard.ai.model.AiRewriteResult
import com.novakey.keyboard.ai.model.AiToneResult
import com.novakey.keyboard.ai.model.RewriteMode
import com.novakey.keyboard.engine.tone.ToneCategory

/**
 * Provider-independent interface for AI writing assistance operations.
 * Isolates keyboard UI from model providers (Gemini, local on-device, or custom endpoints).
 */
interface AiEngineProvider {
    val providerName: String
    val isLocal: Boolean

    suspend fun checkGrammar(text: String, context: AiContext): AiResult<AiGrammarResult>
    suspend fun rewrite(text: String, mode: RewriteMode, context: AiContext): AiResult<AiRewriteResult>
    suspend fun changeTone(text: String, tone: ToneCategory, context: AiContext): AiResult<AiToneResult>
    suspend fun processContextAction(text: String, actionType: String, context: AiContext): AiResult<AiContextResult>
    suspend fun customPrompt(text: String, userPrompt: String, context: AiContext): AiResult<AiContextResult>
}
