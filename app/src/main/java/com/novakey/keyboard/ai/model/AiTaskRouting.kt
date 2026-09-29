package com.novakey.keyboard.ai.model

enum class OnDeviceStatus(val label: String) {
    AVAILABLE("Available"),
    DOWNLOADABLE("Downloadable"),
    DOWNLOADING("Downloading"),
    UNAVAILABLE("Unsupported/Unavailable")
}

enum class AiTaskType(val displayName: String) {
    GRAMMAR("Grammar Check"),
    REWRITE("Text Rewrite"),
    TONE("Tone Modulation"),
    TRANSLATION("Translation"),
    EXPLANATION("Explanation"),
    QUESTION_ANSWER("Question Answering"),
    CONTINUE_WRITING("Continue Writing"),
    CUSTOM_PROMPT("Custom Instruction")
}

enum class RoutingPreference {
    PREFER_LOCAL,       // Fast 0ms rule-based first
    PREFER_ON_DEVICE,   // Gemini Nano / AICore first
    PREFER_CLOUD        // Cloud LLM first (with fallback)
}
