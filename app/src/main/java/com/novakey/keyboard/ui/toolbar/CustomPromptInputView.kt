package com.novakey.keyboard.ui.toolbar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Compact inline custom prompt input bar rendered above the keyboard.
 */
class CustomPromptInputView(
    context: Context,
    private val onSubmitPrompt: (String) -> Unit,
    private val onClose: () -> Unit
) : LinearLayout(context) {

    private val inputField: EditText

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundColor(Color.parseColor("#1E293B"))
        setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6))

        // Close button
        val closeBtn = TextView(context).apply {
            text = "✕"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(dpToPx(6), dpToPx(6), dpToPx(8), dpToPx(6))
            setOnClickListener { onClose() }
        }
        addView(closeBtn)

        // Custom Prompt Edit Box
        inputField = EditText(context).apply {
            hint = "e.g. Make this sound polite & short..."
            setHintTextColor(Color.parseColor("#64748B"))
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#0F172A"))
                setStroke(dpToPx(1), Color.parseColor("#334155"))
            }
            background = bg
            val params = LayoutParams(0, dpToPx(40), 1.0f)
            layoutParams = params
        }
        addView(inputField)

        // Submit Button
        val submitBtn = Button(context).apply {
            text = "Apply"
            textSize = 12f
            isAllCaps = false
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#4F46E5"))
            }
            background = bg
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(40)).apply {
                marginStart = dpToPx(8)
            }
            layoutParams = params
            setOnClickListener {
                val prompt = inputField.text.toString().trim()
                if (prompt.isNotEmpty()) {
                    onSubmitPrompt(prompt)
                }
            }
        }
        addView(submitBtn)
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
