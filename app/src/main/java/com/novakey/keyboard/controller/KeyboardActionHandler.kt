package com.novakey.keyboard.controller

import com.novakey.keyboard.ai.model.RewriteMode
import com.novakey.keyboard.engine.tone.ToneCategory

/**
 * Interface defining events triggered from the keyboard UI to the IME Service.
 */
interface KeyboardActionHandler {
    fun onTextKey(text: String)
    fun onBackspace()
    fun onEnter()
    fun onSpace()
    fun onShiftToggle()
    fun onLayerSwitch(targetLayer: KeyboardLayer)
    fun onLanguageSwitch()
    fun onLanguageSelected(language: InputLanguage)
    fun onCandidateSelected(candidate: String)
    fun onEmojiToggle()
    fun onEmojiInserted(emoji: String)
    fun onToneSelected(tone: ToneCategory)
    fun onRewriteModeSelected(mode: RewriteMode)
    fun onCustomPromptSubmitted(prompt: String)
    fun onContextActionSelected(actionKey: String)
    fun onAiActionRequested(action: AiAction)
}

enum class KeyboardLayer {
    QWERTY,
    SYMBOLS,
    NUMBERS
}

enum class InputLanguage(val label: String, val badge: String) {
    ENGLISH("English", "EN"),
    BANGLA("Bangla", "বাং"),
    BANGLISH("Banglish", "বং-EN")
}

enum class AiAction {
    AI_MENU,
    GRAMMAR,
    TONE,
    REWRITE,
    CUSTOM_PROMPT,
    BANGLISH,
    EXPLAIN,
    SETTINGS
}
