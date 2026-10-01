package com.novakey.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.novakey.keyboard.ai.AiRepository
import com.novakey.keyboard.ai.model.AiResult
import com.novakey.keyboard.ai.model.OnDeviceStatus
import com.novakey.keyboard.ai.model.ProviderSource
import com.novakey.keyboard.ai.model.RewriteMode
import com.novakey.keyboard.controller.AiAction
import com.novakey.keyboard.controller.EditorContextManager
import com.novakey.keyboard.controller.InputConnectionManager
import com.novakey.keyboard.controller.InputLanguage
import com.novakey.keyboard.controller.KeyboardActionHandler
import com.novakey.keyboard.controller.KeyboardLayer
import com.novakey.keyboard.controller.SafeEditorContext
import com.novakey.keyboard.engine.banglish.BanglishConverter
import com.novakey.keyboard.engine.grammar.HeuristicGrammar
import com.novakey.keyboard.engine.tone.ToneCategory
import com.novakey.keyboard.ui.keyboard.EmojiKeyboardView
import com.novakey.keyboard.ui.keyboard.KeyboardView
import com.novakey.keyboard.ui.toolbar.AiMenuOverlayView
import com.novakey.keyboard.ui.toolbar.AiResultPreviewView
import com.novakey.keyboard.ui.toolbar.AiToolbarView
import com.novakey.keyboard.ui.toolbar.CandidateBarView
import com.novakey.keyboard.ui.toolbar.CustomPromptInputView
import com.novakey.keyboard.ui.toolbar.RewriteMenuView
import com.novakey.keyboard.ui.toolbar.ToneMenuView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * NovaKey Android Input Method Service (IME)
 * Phase 5: Polished AI Writing Experience
 * Features: Non-destructive review, [Replace], [Insert], [Copy], [Cancel],
 * Input Safety verification, Provider diagnostics, and 100% offline normal typing.
 */
class NovaKeyInputMethodService : InputMethodService(), KeyboardActionHandler {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var activeAiJob: Job? = null

    private val inputConnectionManager = InputConnectionManager()
    private val aiRepository by lazy { AiRepository(context = this) }
    private var currentLanguage = InputLanguage.ENGLISH

    // View Components
    private var rootLayout: LinearLayout? = null
    private var aiToolbarView: AiToolbarView? = null
    private var candidateBarView: CandidateBarView? = null
    private var keyboardContainer: FrameLayout? = null
    private var keyboardView: KeyboardView? = null
    private var emojiKeyboardView: EmojiKeyboardView? = null

    // Overlays & Secondary Panels
    private var toneMenuView: ToneMenuView? = null
    private var rewriteMenuView: RewriteMenuView? = null
    private var aiMenuOverlayView: AiMenuOverlayView? = null
    private var customPromptInputView: CustomPromptInputView? = null
    private var aiResultPreviewView: AiResultPreviewView? = null

    private var isShowingEmoji = false
    private var isSensitiveField = false
    private var isBackspacing = false
    private var vibrator: Vibrator? = null

    // Active AI operation context snapshot for input safety verification
    private var pendingEditorContext: SafeEditorContext? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardView?.stopBackspaceRepeat()
        serviceScope.cancel()
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        rootLayout = root

        // 1. AI Toolbar Strip: [ ✨ AI ] [ ✓ Grammar ] [ Rewrite ] [ Tone ]
        aiToolbarView = AiToolbarView(this, this)
        root.addView(aiToolbarView)

        // 2. Candidate / Suggestion Bar
        candidateBarView = CandidateBarView(this) { candidate ->
            onCandidateSelected(candidate)
        }
        root.addView(candidateBarView)

        // 3. Dynamic Keyboard Container
        val container = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        keyboardContainer = container

        keyboardView = KeyboardView(this, this)
        container.addView(keyboardView)

        emojiKeyboardView = EmojiKeyboardView(
            context = this,
            onEmojiSelected = { emoji -> onEmojiInserted(emoji) },
            onBackspace = { onBackspace() },
            onReturnToLetters = { onEmojiToggle() }
        ).apply {
            visibility = View.GONE
        }
        container.addView(emojiKeyboardView)

        root.addView(container)
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        inputConnectionManager.updateInputConnection(currentInputConnection, info)

        isSensitiveField = EditorContextManager.isSensitiveField(info)
        if (isSensitiveField) {
            // Strictly hide AI toolbar and predictions in sensitive / password / PIN / OTP fields
            aiToolbarView?.visibility = View.GONE
            candidateBarView?.visibility = View.GONE
            dismissAllOverlays()
        } else {
            aiToolbarView?.visibility = View.VISIBLE
            candidateBarView?.visibility = View.VISIBLE
        }

        if (isShowingEmoji) {
            showKeyboardLayout()
        }

        refreshCandidates()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        keyboardView?.stopBackspaceRepeat()
        isBackspacing = false
        activeAiJob?.cancel()
        inputConnectionManager.updateInputConnection(null, null)
        dismissAllOverlays()
    }

    // --- Candidate Updates (100% Offline / Zero Network) ---

    private fun refreshCandidates() {
        if (isSensitiveField) return

        val (currentWord, _) = inputConnectionManager.getCurrentActiveWord()
        if (currentWord.isBlank()) {
            candidateBarView?.setCandidates(emptyList())
            return
        }

        val candidates = mutableListOf<String>()

        when (currentLanguage) {
            InputLanguage.BANGLISH -> {
                candidates.addAll(BanglishConverter.getCandidates(currentWord))
            }
            InputLanguage.ENGLISH -> {
                HeuristicGrammar.getSpellingCorrection(currentWord)?.let { corrected ->
                    candidates.add(corrected)
                }
                candidates.add(currentWord)
                if (currentWord.first().isLowerCase()) {
                    candidates.add(currentWord.replaceFirstChar { it.uppercase() })
                }
            }
            InputLanguage.BANGLA -> {
                candidates.add(currentWord)
            }
        }

        candidateBarView?.setCandidates(candidates.distinct())
    }

    // --- Safe Context Extraction ---

    private fun captureEditorContext(): SafeEditorContext? {
        val safeContext = EditorContextManager.extractContext(currentInputConnection, currentInputEditorInfo)
        pendingEditorContext = safeContext
        return safeContext
    }

    // --- KeyboardActionHandler Implementation ---

    override fun onTextKey(text: String) {
        triggerHaptic()
        inputConnectionManager.commitText(text)
        refreshCandidates()
    }

    override fun onBackspace(isRepeat: Boolean) {
        if (isSensitiveField) {
            inputConnectionManager.deleteBackward(isRepeat)
            return
        }

        triggerHaptic(isRepeat)
        inputConnectionManager.deleteBackward(isRepeat)

        if (isRepeat) {
            isBackspacing = true
            // Suppress candidate refresh during rapid backspace repeat to avoid blocking UI thread
        } else {
            isBackspacing = false
            // Single tap: perform immediate candidate refresh
            refreshCandidates()
        }
    }

    override fun onBackspaceUp() {
        if (isBackspacing) {
            isBackspacing = false
            // Perform one final candidate refresh after backspace repeat finishes
            refreshCandidates()
        }
    }

    override fun onEnter() {
        triggerHaptic()
        inputConnectionManager.performEnterAction()
        refreshCandidates()
    }

    override fun onSpace() {
        triggerHaptic()
        if (currentLanguage == InputLanguage.BANGLISH) {
            val (currentWord, length) = inputConnectionManager.getCurrentActiveWord()
            if (currentWord.isNotEmpty()) {
                val converted = BanglishConverter.convert(currentWord)
                if (converted.isNotEmpty() && converted != currentWord) {
                    inputConnectionManager.replaceCurrentOrSelected(converted, length)
                }
            }
        }
        inputConnectionManager.commitSpace()
        refreshCandidates()
    }

    override fun onShiftToggle() {
        triggerHaptic()
        keyboardView?.toggleShift()
    }

    override fun onLayerSwitch(targetLayer: KeyboardLayer) {
        triggerHaptic()
        keyboardView?.setLayer(targetLayer)
    }

    override fun onLanguageSwitch() {
        triggerHaptic()
        val next = when (currentLanguage) {
            InputLanguage.ENGLISH -> InputLanguage.BANGLISH
            InputLanguage.BANGLISH -> InputLanguage.BANGLA
            InputLanguage.BANGLA -> InputLanguage.ENGLISH
        }
        onLanguageSelected(next)
    }

    override fun onLanguageSelected(language: InputLanguage) {
        triggerHaptic()
        currentLanguage = language
        aiToolbarView?.setLanguage(language)
        keyboardView?.setLanguage(language)
        refreshCandidates()
    }

    override fun onCandidateSelected(candidate: String) {
        triggerHaptic()
        inputConnectionManager.replaceCurrentWord(candidate + " ")
        refreshCandidates()
    }

    override fun onEmojiToggle() {
        triggerHaptic()
        if (isShowingEmoji) {
            showKeyboardLayout()
        } else {
            showEmojiLayout()
        }
    }

    override fun onEmojiInserted(emoji: String) {
        triggerHaptic()
        inputConnectionManager.commitText(emoji)
    }

    override fun onToneSelected(tone: ToneCategory) {
        triggerHaptic()
        dismissAllOverlays()

        val safeContext = captureEditorContext()
        if (safeContext == null || safeContext.targetText.isBlank()) {
            candidateBarView?.setCandidates(listOf("Select some text first."))
            return
        }

        candidateBarView?.setCandidates(listOf("⏳ NovaKey AI is writing…"))

        val (word, _) = inputConnectionManager.getCurrentActiveWord()
        val aiContext = EditorContextManager.toAiContext(safeContext, word)

        activeAiJob?.cancel()
        activeAiJob = serviceScope.launch {
            when (val res = aiRepository.changeTone(safeContext.targetText, tone, aiContext)) {
                is AiResult.Success -> {
                    showAiPreview(
                        title = "🎭 ${tone.displayName} Tone",
                        output = res.data.transformedText,
                        explanation = res.data.explanation,
                        provider = res.data.providerSource
                    )
                }
                is AiResult.Error -> {
                    candidateBarView?.setCandidates(listOf("AI isn't available right now."))
                }
                is AiResult.Loading -> {}
            }
        }
    }

    override fun onRewriteModeSelected(mode: RewriteMode) {
        triggerHaptic()
        dismissAllOverlays()

        val safeContext = captureEditorContext()
        if (safeContext == null || safeContext.targetText.isBlank()) {
            candidateBarView?.setCandidates(listOf("Select some text first."))
            return
        }

        candidateBarView?.setCandidates(listOf("⏳ NovaKey AI is writing…"))

        val (word, _) = inputConnectionManager.getCurrentActiveWord()
        val aiContext = EditorContextManager.toAiContext(safeContext, word)

        activeAiJob?.cancel()
        activeAiJob = serviceScope.launch {
            when (val res = aiRepository.rewrite(safeContext.targetText, mode, aiContext)) {
                is AiResult.Success -> {
                    showAiPreview(
                        title = "✨ Rewrite (${mode.displayName})",
                        output = res.data.rewrittenText,
                        explanation = "Rewritten in ${mode.displayName} mode",
                        provider = res.data.providerSource
                    )
                }
                is AiResult.Error -> {
                    candidateBarView?.setCandidates(listOf("AI isn't available right now."))
                }
                is AiResult.Loading -> {}
            }
        }
    }

    override fun onCustomPromptSubmitted(prompt: String) {
        triggerHaptic()
        dismissAllOverlays()

        val safeContext = captureEditorContext()
        val targetText = safeContext?.targetText ?: ""

        candidateBarView?.setCandidates(listOf("⏳ NovaKey AI is writing…"))

        val (word, _) = inputConnectionManager.getCurrentActiveWord()
        val aiContext = if (safeContext != null) {
            EditorContextManager.toAiContext(safeContext, word)
        } else {
            com.novakey.keyboard.ai.AiContext(
                textBeforeCursor = inputConnectionManager.getTextBeforeCursor(250),
                selectedText = inputConnectionManager.getSelectedText(),
                activeWord = word,
                isSensitiveField = isSensitiveField,
                packageName = currentInputEditorInfo?.packageName
            )
        }

        activeAiJob?.cancel()
        activeAiJob = serviceScope.launch {
            when (val res = aiRepository.customPrompt(targetText, prompt, aiContext)) {
                is AiResult.Success -> {
                    showAiPreview(
                        title = "✏️ Instruction: '$prompt'",
                        output = res.data.responseText,
                        explanation = res.data.explanation,
                        provider = res.data.providerSource
                    )
                }
                is AiResult.Error -> {
                    candidateBarView?.setCandidates(listOf("AI isn't available right now."))
                }
                is AiResult.Loading -> {}
            }
        }
    }

    override fun onContextActionSelected(actionKey: String) {
        triggerHaptic()
        dismissAllOverlays()

        when (actionKey) {
            "custom_prompt" -> {
                showCustomPromptInput()
            }
            "translate" -> {
                // Determine direction: English -> Bangla or Bangla -> English
                val safeContext = captureEditorContext()
                val targetText = safeContext?.targetText ?: ""
                if (targetText.isBlank()) {
                    candidateBarView?.setCandidates(listOf("Select some text to translate."))
                    return
                }

                val hasBengaliChar = targetText.any { it in '\u0980'..'\u09FF' }
                val targetLang = if (hasBengaliChar) "English" else "Bangla"

                candidateBarView?.setCandidates(listOf("⏳ Translating to $targetLang…"))

                val (word, _) = inputConnectionManager.getCurrentActiveWord()
                val aiContext = EditorContextManager.toAiContext(safeContext, word)

                activeAiJob?.cancel()
                activeAiJob = serviceScope.launch {
                    val langCode = if (hasBengaliChar) "en" else "bn"
                    when (val res = aiRepository.translate(targetText, langCode, aiContext)) {
                        is AiResult.Success -> {
                            showAiPreview(
                                title = "🌐 Translation ($targetLang)",
                                output = res.data.responseText,
                                explanation = "Translated to $targetLang",
                                provider = res.data.providerSource
                            )
                        }
                        is AiResult.Error -> {
                            candidateBarView?.setCandidates(listOf("Translation unavailable."))
                        }
                        is AiResult.Loading -> {}
                    }
                }
            }
            "ask" -> {
                val safeContext = captureEditorContext()
                val targetText = safeContext?.targetText ?: ""
                if (targetText.isBlank()) {
                    // Open custom prompt to ask question
                    showCustomPromptInput()
                    return
                }
                executeContextAction(targetText, "ask", "❓ Answer")
            }
            "reply" -> {
                val safeContext = captureEditorContext()
                val targetText = safeContext?.targetText ?: ""
                if (targetText.isBlank()) {
                    candidateBarView?.setCandidates(listOf("No message context to reply to."))
                    return
                }
                executeContextAction(targetText, "reply", "↩️ Suggested Reply")
            }
            "write" -> {
                val safeContext = captureEditorContext()
                val targetText = safeContext?.targetText ?: ""
                if (targetText.isBlank()) {
                    showCustomPromptInput()
                    return
                }
                executeContextAction(targetText, "write", "✍️ Drafted Message")
            }
            "explain" -> {
                val safeContext = captureEditorContext()
                val targetText = safeContext?.targetText ?: ""
                if (targetText.isBlank()) {
                    candidateBarView?.setCandidates(listOf("Select text to explain."))
                    return
                }
                executeContextAction(targetText, "explain", "💡 Explanation")
            }
            else -> {
                val safeContext = captureEditorContext()
                val targetText = safeContext?.targetText ?: ""
                executeContextAction(targetText, actionKey, "✨ AI Result")
            }
        }
    }

    private fun executeContextAction(targetText: String, actionKey: String, displayTitle: String) {
        candidateBarView?.setCandidates(listOf("⏳ NovaKey AI is writing…"))

        val safeContext = pendingEditorContext
        val (word, _) = inputConnectionManager.getCurrentActiveWord()
        val aiContext = if (safeContext != null) {
            EditorContextManager.toAiContext(safeContext, word)
        } else {
            com.novakey.keyboard.ai.AiContext(
                textBeforeCursor = inputConnectionManager.getTextBeforeCursor(250),
                selectedText = inputConnectionManager.getSelectedText(),
                activeWord = word,
                isSensitiveField = isSensitiveField,
                packageName = currentInputEditorInfo?.packageName
            )
        }

        activeAiJob?.cancel()
        activeAiJob = serviceScope.launch {
            when (val res = aiRepository.processContextAction(targetText, actionKey, aiContext)) {
                is AiResult.Success -> {
                    showAiPreview(
                        title = displayTitle,
                        output = res.data.responseText,
                        explanation = res.data.explanation,
                        provider = res.data.providerSource
                    )
                }
                is AiResult.Error -> {
                    candidateBarView?.setCandidates(listOf("AI isn't available right now."))
                }
                is AiResult.Loading -> {}
            }
        }
    }

    override fun onAiActionRequested(action: AiAction) {
        if (isSensitiveField) return
        triggerHaptic()
        when (action) {
            AiAction.AI_MENU -> {
                toggleAiMenu()
            }

            AiAction.GRAMMAR -> {
                val safeContext = captureEditorContext()
                if (safeContext == null || safeContext.targetText.isBlank()) {
                    candidateBarView?.setCandidates(listOf("Select some text first."))
                    return
                }

                candidateBarView?.setCandidates(listOf("⏳ NovaKey AI is writing…"))

                val (word, _) = inputConnectionManager.getCurrentActiveWord()
                val aiContext = EditorContextManager.toAiContext(safeContext, word)

                activeAiJob?.cancel()
                activeAiJob = serviceScope.launch {
                    when (val res = aiRepository.checkGrammar(safeContext.targetText, aiContext)) {
                        is AiResult.Success -> {
                            showAiPreview(
                                title = "✓ Grammar Correction",
                                output = res.data.correctedText,
                                explanation = res.data.explanation,
                                provider = res.data.providerSource
                            )
                        }
                        is AiResult.Error -> {
                            candidateBarView?.setCandidates(listOf("AI isn't available right now."))
                        }
                        is AiResult.Loading -> {}
                    }
                }
            }

            AiAction.TONE -> {
                toggleToneMenu()
            }

            AiAction.REWRITE -> {
                toggleRewriteMenu()
            }

            AiAction.CUSTOM_PROMPT -> {
                showCustomPromptInput()
            }

            AiAction.BANGLISH -> {
                val text = inputConnectionManager.getTextBeforeCursor(150)
                if (text.isNotBlank()) {
                    val converted = BanglishConverter.convert(text)
                    inputConnectionManager.replaceCurrentOrSelected(converted, text.length)
                }
            }

            AiAction.EXPLAIN -> {
                onContextActionSelected("explain")
            }

            AiAction.SETTINGS -> {}
        }
    }

    // --- Overlay UI Management ---

    private fun showAiPreview(
        title: String,
        output: String,
        explanation: String?,
        provider: ProviderSource
    ) {
        dismissAllOverlays()

        val root = rootLayout ?: return
        val preview = AiResultPreviewView(
            context = this,
            onReplace = { textToCommit ->
                // Input Safety check before commit: verify editor context has not changed
                val isSafe = EditorContextManager.canSafelyApply(
                    pendingEditorContext,
                    currentInputConnection,
                    currentInputEditorInfo
                )
                if (isSafe && pendingEditorContext != null) {
                    val orig = pendingEditorContext!!
                    inputConnectionManager.replaceCurrentOrSelected(textToCommit, orig.targetLength)
                } else {
                    // Editor changed or cursor moved significantly: do NOT overwrite unrelated text!
                    candidateBarView?.setCandidates(listOf("Context changed; text not modified."))
                }
                dismissAllOverlays()
                refreshCandidates()
            },
            onInsert = { textToCommit ->
                // Insert at cursor position without deleting existing text
                inputConnectionManager.commitText(textToCommit)
                dismissAllOverlays()
                refreshCandidates()
            },
            onCancel = {
                dismissAllOverlays()
                refreshCandidates()
            }
        )
        preview.showResult(title, output, explanation, provider)
        aiResultPreviewView = preview

        // Insert directly beneath AI toolbar
        root.addView(preview, 1)
    }

    private fun toggleAiMenu() {
        if (aiMenuOverlayView != null) {
            dismissAllOverlays()
        } else {
            dismissAllOverlays()
            val root = rootLayout ?: return
            val menu = AiMenuOverlayView(
                context = this,
                onActionSelected = { actionKey -> onContextActionSelected(actionKey) },
                onClose = { dismissAllOverlays() }
            )
            aiMenuOverlayView = menu
            root.addView(menu, 1)
        }
    }

    private fun toggleToneMenu() {
        if (toneMenuView != null) {
            dismissAllOverlays()
        } else {
            dismissAllOverlays()
            val root = rootLayout ?: return
            val menu = ToneMenuView(
                context = this,
                onToneSelected = { tone -> onToneSelected(tone) },
                onClose = { dismissAllOverlays() }
            )
            toneMenuView = menu
            root.addView(menu, 1)
        }
    }

    private fun toggleRewriteMenu() {
        if (rewriteMenuView != null) {
            dismissAllOverlays()
        } else {
            dismissAllOverlays()
            val root = rootLayout ?: return
            val menu = RewriteMenuView(
                context = this,
                onModeSelected = { mode -> onRewriteModeSelected(mode) },
                onClose = { dismissAllOverlays() }
            )
            rewriteMenuView = menu
            root.addView(menu, 1)
        }
    }

    private fun showCustomPromptInput() {
        dismissAllOverlays()
        val root = rootLayout ?: return
        val input = CustomPromptInputView(
            context = this,
            onSubmitPrompt = { prompt -> onCustomPromptSubmitted(prompt) },
            onClose = { dismissAllOverlays() }
        )
        customPromptInputView = input
        root.addView(input, 1)
    }

    private fun dismissAllOverlays() {
        aiResultPreviewView?.let { rootLayout?.removeView(it) }
        aiResultPreviewView = null

        toneMenuView?.let { rootLayout?.removeView(it) }
        toneMenuView = null

        rewriteMenuView?.let { rootLayout?.removeView(it) }
        rewriteMenuView = null

        aiMenuOverlayView?.let { rootLayout?.removeView(it) }
        aiMenuOverlayView = null

        customPromptInputView?.let { rootLayout?.removeView(it) }
        customPromptInputView = null
    }

    private fun showEmojiLayout() {
        isShowingEmoji = true
        keyboardView?.visibility = View.GONE
        emojiKeyboardView?.visibility = View.VISIBLE
    }

    private fun showKeyboardLayout() {
        isShowingEmoji = false
        emojiKeyboardView?.visibility = View.GONE
        keyboardView?.visibility = View.VISIBLE
    }

    private fun triggerHaptic(isRepeat: Boolean = false) {
        if (isRepeat) return // Do not saturate vibrator queue during rapid backspace repeat
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (_: Exception) {
            // Ignore haptic failure fallback
        }
    }
}
