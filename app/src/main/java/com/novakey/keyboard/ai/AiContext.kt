package com.novakey.keyboard.ai

data class AiContext(
    val textBeforeCursor: String,
    val selectedText: String,
    val activeWord: String,
    val isSensitiveField: Boolean,
    val packageName: String? = null
) {
    /**
     * Obtains the target text to process: prefers active selection,
     * falls back to the last typed sentence or paragraph.
     */
    fun getTargetText(): String {
        if (selectedText.isNotBlank()) {
            return selectedText.trim()
        }
        val before = textBeforeCursor.trimEnd()
        if (before.isEmpty()) return ""

        // Extract last sentence if long
        val match = Regex("(?<=[.!?\\n])\\s*([^.!?\\n]+)$").find(before)
        return match?.groupValues?.get(1)?.trim() ?: before.takeLast(200).trim()
    }
}
