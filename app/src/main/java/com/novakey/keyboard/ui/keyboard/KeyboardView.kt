package com.novakey.keyboard.ui.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Handler
import android.os.Looper
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
        setBackgroundColor(Color.parseColor("#0F172A")) // Dark slate background
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
        currentLayer = layer
        renderKeyboard()
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
            KeyboardLayer.SYMBOLS, KeyboardLayer.NUMBERS -> {
                addRow(KeyboardLayouts.SYMBOLS_ROW_1)
                addRow(KeyboardLayouts.SYMBOLS_ROW_2)
                addRow(KeyboardLayouts.SYMBOLS_ROW_3)
                addRow(KeyboardLayouts.BOTTOM_ROW)
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

            val normalBg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                when (key.type) {
                    KeyType.ENTER -> setColor(Color.parseColor("#3B82F6")) // Blue 500
                    KeyType.SHIFT, KeyType.BACKSPACE, KeyType.SWITCH_LAYER, KeyType.SWITCH_LANGUAGE -> {
                        if (key.type == KeyType.SHIFT && isShifted) {
                            setColor(Color.parseColor("#6366F1")) // Active Indigo
                        } else {
                            setColor(Color.parseColor("#334155")) // Slate 700
                        }
                    }
                    KeyType.SPACE -> setColor(Color.parseColor("#1E293B"))
                    KeyType.EMOJI -> setColor(Color.parseColor("#1E293B"))
                    else -> setColor(Color.parseColor("#1E293B")) // Slate 800
                }
            }

            val rippleColor = ColorStateList.valueOf(Color.parseColor("#475569"))
            background = RippleDrawable(rippleColor, normalBg, null)

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
                setTextColor(Color.parseColor("#F8FAFC"))
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
                    setTextColor(Color.parseColor("#64748B"))
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
            } else {
                setOnClickListener {
                    handleKeyClick(key)
                }

                // Long press for secondary character
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
                if (currentLayer == KeyboardLayer.QWERTY) {
                    setLayer(KeyboardLayer.SYMBOLS)
                } else {
                    setLayer(KeyboardLayer.QWERTY)
                }
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
