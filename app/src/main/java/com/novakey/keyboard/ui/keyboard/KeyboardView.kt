package com.novakey.keyboard.ui.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Handler
import android.os.Looper
import android.util.StateSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.novakey.keyboard.controller.InputLanguage
import com.novakey.keyboard.controller.KeyboardActionHandler
import com.novakey.keyboard.controller.KeyboardLayer
import kotlin.math.abs

class KeyboardView(
    context: Context,
    private val actionHandler: KeyboardActionHandler?
) : LinearLayout(context) {

    private var isShifted: Boolean = false
    private var currentLayer: KeyboardLayer = KeyboardLayer.QWERTY
    private var currentLanguage: InputLanguage = InputLanguage.ENGLISH

    // Backspace continuous repeat mechanism (Handler-based, lifecycle safe)
    private val repeatHandler = Handler(Looper.getMainLooper())
    private val backspaceRunnable = object : Runnable {
        override fun run() {
            actionHandler?.onBackspace()
            repeatHandler.postDelayed(this, REPEAT_INTERVAL_MS)
        }
    }

    companion object {
        private const val INITIAL_REPEAT_DELAY_MS = 300L
        private const val REPEAT_INTERVAL_MS = 60L
    }

    init {
        orientation = VERTICAL
        // Futuristic near-black keyboard background with subtle electric cyan boundary
        val rootBg = GradientDrawable().apply {
            setColor(Color.parseColor("#030712")) // Ultra-dark / near-black obsidian
            setStroke(dpToPx(1), Color.parseColor("#155E75")) // Subtle electric cyan outer boundary outline
        }
        background = rootBg
        setPadding(dpToPx(4), dpToPx(5), dpToPx(4), dpToPx(8))
        renderKeyboard()
    }

    private fun stopBackspaceRepeat() {
        repeatHandler.removeCallbacks(backspaceRunnable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopBackspaceRepeat()
    }

    fun toggleShift() {
        isShifted = !isShifted
        renderKeyboard()
    }

    fun setShift(shifted: Boolean) {
        if (isShifted != shifted) {
            isShifted = shifted
            renderKeyboard()
        }
    }

    fun setLayer(layer: KeyboardLayer) {
        if (currentLayer != layer) {
            currentLayer = layer
            renderKeyboard()
        }
    }

    fun setLanguage(language: InputLanguage) {
        currentLanguage = language
        renderKeyboard()
    }

    private fun renderKeyboard() {
        // Stop any running repeat before rebuilding layout
        stopBackspaceRepeat()
        removeAllViews()

        when (currentLayer) {
            KeyboardLayer.QWERTY -> {
                if (currentLanguage == InputLanguage.BANGLA) {
                    addRow(KeyboardLayouts.BANGLA_ROW_1)
                    addRow(KeyboardLayouts.BANGLA_ROW_2)
                    addRow(KeyboardLayouts.BANGLA_ROW_3)
                } else {
                    addRow(KeyboardLayouts.QWERTY_ROW_1)
                    addRow(KeyboardLayouts.QWERTY_ROW_2)
                    addRow(KeyboardLayouts.QWERTY_ROW_3)
                }
                addRow(KeyboardLayouts.BOTTOM_ROW)
            }
            KeyboardLayer.SYMBOLS -> {
                addRow(KeyboardLayouts.SYMBOLS_ROW_1)
                addRow(KeyboardLayouts.SYMBOLS_ROW_2)
                addRow(KeyboardLayouts.SYMBOLS_ROW_3)
                addRow(KeyboardLayouts.SYMBOLS_BOTTOM_ROW)
            }
            KeyboardLayer.MORE_SYMBOLS -> {
                addRow(KeyboardLayouts.MORE_SYMBOLS_ROW_1)
                addRow(KeyboardLayouts.MORE_SYMBOLS_ROW_2)
                addRow(KeyboardLayouts.MORE_SYMBOLS_ROW_3)
                addRow(KeyboardLayouts.SYMBOLS_BOTTOM_ROW)
            }
            KeyboardLayer.NUMBERS -> {
                addRow(KeyboardLayouts.MORE_SYMBOLS_ROW_1)
                addRow(KeyboardLayouts.MORE_SYMBOLS_ROW_2)
                addRow(KeyboardLayouts.MORE_SYMBOLS_ROW_3)
                addRow(KeyboardLayouts.SYMBOLS_BOTTOM_ROW)
            }
        }
    }

    private fun addRow(keys: List<KeyModel>) {
        val rowLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            weightSum = keys.sumOf { it.weight.toDouble() }.toFloat()
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(52)).apply {
                topMargin = dpToPx(3)
                bottomMargin = dpToPx(3)
            }
        }

        for (key in keys) {
            val keyView = createKeyContainer(key)
            rowLayout.addView(keyView)
        }

        addView(rowLayout)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createKeyContainer(key: KeyModel): View {
        val frame = FrameLayout(context).apply {
            val params = LayoutParams(0, LayoutParams.MATCH_PARENT, key.weight).apply {
                leftMargin = dpToPx(3)
                rightMargin = dpToPx(3)
            }
            layoutParams = params

            val cornerRadiusPx = dpToPx(8).toFloat()

            // 1. Normal Key Background (Nearly black interior + Thin Electric Cyan outline + Soft outer glow)
            val normalOuterHalo = GradientDrawable().apply {
                cornerRadius = cornerRadiusPx + dpToPx(1)
                setColor(Color.TRANSPARENT)
                when (key.type) {
                    KeyType.ENTER -> setStroke(dpToPx(2), Color.parseColor("#4D00F0FF"))
                    KeyType.SHIFT -> if (isShifted) setStroke(dpToPx(2), Color.parseColor("#6600F0FF")) else setStroke(dpToPx(2), Color.parseColor("#2600F0FF"))
                    else -> setStroke(dpToPx(2), Color.parseColor("#2600F0FF")) // Soft cyan outer glow halo
                }
            }

            val normalInnerCore = GradientDrawable().apply {
                cornerRadius = cornerRadiusPx
                // Nearly black transparent-looking interior across all keys (NEVER solid blue)
                when (key.type) {
                    KeyType.ENTER -> {
                        setColor(Color.parseColor("#041624"))
                        setStroke(dpToPx(1), Color.parseColor("#00F0FF")) // Electric cyan outline
                    }
                    KeyType.SHIFT -> {
                        if (isShifted) {
                            setColor(Color.parseColor("#041624"))
                            setStroke(dpToPx(1), Color.parseColor("#00F0FF"))
                        } else {
                            setColor(Color.parseColor("#050A14"))
                            setStroke(dpToPx(1), Color.parseColor("#0891B2"))
                        }
                    }
                    KeyType.BACKSPACE, KeyType.SWITCH_LAYER, KeyType.SWITCH_LANGUAGE -> {
                        setColor(Color.parseColor("#050A14"))
                        setStroke(dpToPx(1), Color.parseColor("#0891B2"))
                    }
                    else -> {
                        setColor(Color.parseColor("#050A14")) // Almost black interior
                        setStroke(dpToPx(1), Color.parseColor("#06B6D4")) // Thin electric cyan outline
                    }
                }
            }

            val normalBg = LayerDrawable(arrayOf(normalOuterHalo, normalInnerCore)).apply {
                setLayerInset(1, dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1))
            }

            // 2. Pressed Key Glow (Interior remains dark; cyan border & outer glow become intensely bright)
            val pressedOuterHalo = GradientDrawable().apply {
                cornerRadius = cornerRadiusPx + dpToPx(1)
                setColor(Color.TRANSPARENT)
                setStroke(dpToPx(3), Color.parseColor("#8000F0FF")) // Intense electric cyan outer glow
            }

            val pressedInnerCore = GradientDrawable().apply {
                cornerRadius = cornerRadiusPx
                // Dark interior with faint electric shadow tint (NOT a solid blue button)
                setColor(Color.parseColor("#071526"))
                setStroke(dpToPx(2), Color.parseColor("#00F0FF")) // Intense bright electric cyan border
            }

            val pressedBg = LayerDrawable(arrayOf(pressedOuterHalo, pressedInnerCore)).apply {
                setLayerInset(1, dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1))
            }

            // 3. Hardware-accelerated StateListDrawable with 120ms exit fade
            val keyBackground = StateListDrawable().apply {
                setEnterFadeDuration(0)
                setExitFadeDuration(120)
                addState(intArrayOf(android.R.attr.state_pressed), pressedBg)
                addState(StateSet.WILD_CARD, normalBg)
            }

            background = keyBackground

            // Primary Label (Bright cyan / neon blue)
            val primaryText = TextView(context).apply {
                val displayLabel = when {
                    key.type == KeyType.CHARACTER && isShifted -> key.shiftLabel
                    key.type == KeyType.SPACE -> when (currentLanguage) {
                        InputLanguage.ENGLISH -> "EN"
                        InputLanguage.BANGLISH -> "বং"
                        InputLanguage.BANGLA -> "বাংলা"
                    }
                    else -> key.primaryLabel
                }
                text = displayLabel
                textSize = when (key.type) {
                    KeyType.ENTER, KeyType.SHIFT, KeyType.BACKSPACE -> 18f
                    KeyType.SPACE -> 15f
                    KeyType.SWITCH_LAYER, KeyType.SWITCH_LANGUAGE -> 13f
                    KeyType.EMOJI -> 17f
                    else -> 19f
                }
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#00F0FF")) // Bright electric cyan text
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            addView(primaryText)

            // Secondary label preview (Dimmer cyan hint in corner)
            if (key.secondaryLabel != null && currentLayer == KeyboardLayer.QWERTY && !isShifted) {
                val secText = TextView(context).apply {
                    text = key.secondaryLabel
                    textSize = 10f
                    setTextColor(Color.parseColor("#0891B2")) // Dimmer cyan hint
                    val secParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.END
                        topMargin = dpToPx(2)
                        marginEnd = dpToPx(4)
                    }
                    layoutParams = secParams
                }
                addView(secText)
            }

            // Dedicated Touch Handling for Backspace (single authoritative input path with repeat)
            if (key.type == KeyType.BACKSPACE) {
                setOnTouchListener { view, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            view.isPressed = true
                            actionHandler?.onBackspace()
                            repeatHandler.removeCallbacks(backspaceRunnable)
                            repeatHandler.postDelayed(backspaceRunnable, INITIAL_REPEAT_DELAY_MS)
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val x = event.x
                            val y = event.y
                            if (x < 0 || x > view.width || y < 0 || y > view.height) {
                                view.isPressed = false
                                stopBackspaceRepeat()
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            view.isPressed = false
                            stopBackspaceRepeat()
                            true
                        }
                        else -> false
                    }
                }
            } else if (key.type == KeyType.CHARACTER) {
                // Immediate low-latency touch dispatch for CHARACTER keys to prevent missed taps during fast typing
                setOnTouchListener { view, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            view.isPressed = true
                            handleKeyClick(key)
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val x = event.x
                            val y = event.y
                            if (x < 0 || x > view.width || y < 0 || y > view.height) {
                                view.isPressed = false
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            view.isPressed = false
                            true
                        }
                        else -> false
                    }
                }
            } else if (key.type == KeyType.SPACE) {
                var startX = 0f
                var startY = 0f
                var isSwipeHandled = false
                val swipeThresholdPx = dpToPx(48)

                setOnTouchListener { view, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            view.isPressed = true
                            startX = event.x
                            startY = event.y
                            isSwipeHandled = false
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.x - startX
                            val dy = event.y - startY
                            if (!isSwipeHandled && abs(dx) > swipeThresholdPx && abs(dx) > abs(dy) * 1.5f) {
                                isSwipeHandled = true
                                view.isPressed = false
                                if (dx > 0) {
                                    // Swipe RIGHT -> Next language (ENGLISH -> BANGLISH -> BANGLA -> ENGLISH)
                                    val next = when (currentLanguage) {
                                        InputLanguage.ENGLISH -> InputLanguage.BANGLISH
                                        InputLanguage.BANGLISH -> InputLanguage.BANGLA
                                        InputLanguage.BANGLA -> InputLanguage.ENGLISH
                                    }
                                    actionHandler?.onLanguageSelected(next)
                                } else {
                                    // Swipe LEFT -> Previous language (ENGLISH -> BANGLA -> BANGLISH -> ENGLISH)
                                    val prev = when (currentLanguage) {
                                        InputLanguage.ENGLISH -> InputLanguage.BANGLA
                                        InputLanguage.BANGLA -> InputLanguage.BANGLISH
                                        InputLanguage.BANGLISH -> InputLanguage.ENGLISH
                                    }
                                    actionHandler?.onLanguageSelected(prev)
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            view.isPressed = false
                            if (!isSwipeHandled) {
                                handleKeyClick(key)
                            }
                            true
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            view.isPressed = false
                            isSwipeHandled = false
                            true
                        }
                        else -> false
                    }
                }
            } else {
                setOnClickListener {
                    handleKeyClick(key)
                }

                // Long press for secondary character on special/action keys if configured
                if (key.secondaryLabel != null) {
                    setOnLongClickListener {
                        actionHandler?.onTextKey(key.secondaryLabel)
                        true
                    }
                }
            }
        }

        return frame
    }

    private fun handleKeyClick(key: KeyModel) {
        when (key.type) {
            KeyType.CHARACTER -> {
                val charToSend = if (isShifted) key.shiftLabel else key.primaryLabel
                actionHandler?.onTextKey(charToSend)
                if (isShifted) {
                    isShifted = false
                    renderKeyboard()
                }
            }
            KeyType.SHIFT -> {
                toggleShift()
            }
            KeyType.BACKSPACE -> {
                // Fallback only; primary path is via OnTouchListener
                actionHandler?.onBackspace()
            }
            KeyType.ENTER -> {
                actionHandler?.onEnter()
            }
            KeyType.SPACE -> {
                actionHandler?.onSpace()
            }
            KeyType.SWITCH_LAYER -> {
                val target = when (key.targetLayer) {
                    "QWERTY" -> KeyboardLayer.QWERTY
                    "SYMBOLS" -> KeyboardLayer.SYMBOLS
                    "MORE_SYMBOLS" -> KeyboardLayer.MORE_SYMBOLS
                    else -> {
                        if (currentLayer == KeyboardLayer.QWERTY) {
                            KeyboardLayer.SYMBOLS
                        } else {
                            KeyboardLayer.QWERTY
                        }
                    }
                }
                setLayer(target)
                actionHandler?.onLayerSwitch(target)
            }
            KeyType.SWITCH_LANGUAGE -> {
                actionHandler?.onLanguageSwitch()
            }
            KeyType.EMOJI -> {
                actionHandler?.onEmojiToggle()
            }
            KeyType.SETTINGS -> {}
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
