package com.novakey.keyboard.ui.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
        setBackgroundColor(Color.parseColor("#080D1A")) // Deep dark navy obsidian
        setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(8))
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

            // 1. Normal Key Background (Deep Navy with subtle Neon Blue/Cyan accent border)
            val normalBg = GradientDrawable().apply {
                cornerRadius = cornerRadiusPx
                when (key.type) {
                    KeyType.ENTER -> {
                        setColor(Color.parseColor("#0284C7")) // Electric Sky Blue
                        setStroke(dpToPx(1), Color.parseColor("#38BDF8"))
                    }
                    KeyType.SHIFT -> {
                        if (isShifted) {
                            setColor(Color.parseColor("#00B4D8")) // Active Luminescent Cyan
                            setStroke(dpToPx(1), Color.parseColor("#00F0FF"))
                        } else {
                            setColor(Color.parseColor("#152238")) // Function Slate Navy
                            setStroke(dpToPx(1), Color.parseColor("#1E3A5F"))
                        }
                    }
                    KeyType.BACKSPACE, KeyType.SWITCH_LAYER, KeyType.SWITCH_LANGUAGE -> {
                        setColor(Color.parseColor("#152238")) // Function Slate Navy
                        setStroke(dpToPx(1), Color.parseColor("#1E3A5F"))
                    }
                    KeyType.SPACE, KeyType.EMOJI -> {
                        setColor(Color.parseColor("#0F1A2E")) // Dark Navy
                        setStroke(dpToPx(1), Color.parseColor("#1E3250"))
                    }
                    else -> {
                        setColor(Color.parseColor("#0F1A2E")) // Normal Character Key Dark Navy
                        setStroke(dpToPx(1), Color.parseColor("#1E3250"))
                    }
                }
            }

            // 2. Pressed Key Glow (Noticeably brighter Neon Blue/Cyan wash + Glowing border)
            val pressedBg = GradientDrawable().apply {
                cornerRadius = cornerRadiusPx
                when (key.type) {
                    KeyType.ENTER -> {
                        setColor(Color.parseColor("#00F0FF")) // High-voltage Cyan Glow
                        setStroke(dpToPx(2), Color.parseColor("#E0F2FE"))
                    }
                    KeyType.SHIFT -> {
                        if (isShifted) {
                            setColor(Color.parseColor("#38BDF8"))
                            setStroke(dpToPx(2), Color.parseColor("#FFFFFF"))
                        } else {
                            setColor(Color.parseColor("#0284C7"))
                            setStroke(dpToPx(2), Color.parseColor("#00F0FF"))
                        }
                    }
                    else -> {
                        setColor(Color.parseColor("#0369A1")) // Neon Blue / Cyan Wash
                        setStroke(dpToPx(2), Color.parseColor("#00F0FF")) // Brilliant Neon Cyan Glowing Border
                    }
                }
            }

            // 3. Hardware-accelerated StateListDrawable with 120ms exit fade
            val keyBackground = StateListDrawable().apply {
                setEnterFadeDuration(0)
                setExitFadeDuration(120)
                addState(intArrayOf(android.R.attr.state_pressed), pressedBg)
                addState(StateSet.WILD_CARD, normalBg)
            }

            background = keyBackground

            // Primary Label
            val primaryText = TextView(context).apply {
                val displayLabel = when {
                    key.type == KeyType.CHARACTER && isShifted -> key.shiftLabel
                    else -> key.primaryLabel
                }
                text = displayLabel
                textSize = when (key.type) {
                    KeyType.ENTER, KeyType.SHIFT, KeyType.BACKSPACE -> 18f
                    KeyType.SWITCH_LAYER, KeyType.SWITCH_LANGUAGE -> 13f
                    KeyType.EMOJI -> 17f
                    else -> 19f
                }
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#F0F9FF"))
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            addView(primaryText)

            // Secondary label preview (hint in corner)
            if (key.secondaryLabel != null && currentLayer == KeyboardLayer.QWERTY && !isShifted) {
                val secText = TextView(context).apply {
                    text = key.secondaryLabel
                    textSize = 10f
                    setTextColor(Color.parseColor("#38BDF8"))
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
