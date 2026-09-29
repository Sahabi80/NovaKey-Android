package com.novakey.keyboard.ai.model

import com.novakey.keyboard.engine.tone.ToneCategory

enum class ProviderSource(val displayName: String, val badge: String) {
    LOCAL("Local Engine", "LOCAL"),
    ON_DEVICE("Gemini Nano", "ON_DEVICE"),
    CLOUD("Cloud AI", "CLOUD")
}

enum class RewriteMode(val displayName: String, val icon: String) {
    IMPROVE("Improve", "✨"),
    NATURAL("Natural", "🌿"),
    SHORTER("Shorter", "✂️"),
    CLEARER("Clearer", "🔍"),
    PROFESSIONAL("Professional", "💼"),
    CONTINUE("Continue", "⏩")
}

data class AiGrammarResult(
    val originalText: String,
    val correctedText: String,
    val explanation: String? = null,
    val providerSource: ProviderSource = ProviderSource.LOCAL,
    val isFromCloud: Boolean = (providerSource == ProviderSource.CLOUD)
)

data class AiRewriteResult(
    val originalText: String,
    val rewrittenText: String,
    val mode: RewriteMode,
    val providerSource: ProviderSource = ProviderSource.LOCAL,
    val isFromCloud: Boolean = (providerSource == ProviderSource.CLOUD)
)

data class AiToneResult(
    val originalText: String,
    val transformedText: String,
    val tone: ToneCategory,
    val explanation: String? = null,
    val providerSource: ProviderSource = ProviderSource.LOCAL,
    val isFromCloud: Boolean = (providerSource == ProviderSource.CLOUD)
)

data class AiContextResult(
    val query: String,
    val responseText: String,
    val actionType: String,
    val explanation: String? = null,
    val providerSource: ProviderSource = ProviderSource.LOCAL,
    val isFromCloud: Boolean = (providerSource == ProviderSource.CLOUD)
)

sealed class AiResult<out T> {
    data class Success<out T>(val data: T) : AiResult<T>()
    data class Error<out T>(val message: String, val fallbackData: T? = null) : AiResult<T>()
    object Loading : AiResult<Nothing>()
}
