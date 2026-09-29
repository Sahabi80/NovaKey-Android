package com.novakey.keyboard.ui.toolbar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Compact AI menu drawer opened when ✨ AI is tapped on the toolbar.
 * Contains: Ask AI, Reply, Write, Explain, Translate, and Custom Prompt.
 */
class AiMenuOverlayView(
    context: Context,
    private val onActionSelected: (String) -> Unit,
    private val onClose: () -> Unit
) : HorizontalScrollView(context) {

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#1E293B")) // Slate 800

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6))
        }

        // Close button
        val closeBtn = TextView(context).apply {
            text = "✕"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#94A3B8"))
            gravity = Gravity.CENTER
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            setOnClickListener { onClose() }
        }
        container.addView(closeBtn)

        val actions = listOf(
            "❓ Ask AI" to "ask",
            "↩️ Reply" to "reply",
            "✍️ Write" to "write",
            "💡 Explain" to "explain",
            "🌐 Translate" to "translate",
            "✏️ Custom Prompt" to "custom_prompt"
        )

        for ((label, actionKey) in actions) {
            val chip = TextView(context).apply {
                text = label
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))

                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(14).toFloat()
                    setColor(Color.parseColor("#334155"))
                }
                background = bg

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = dpToPx(6)
                }
                layoutParams = params

                setOnClickListener {
                    onActionSelected(actionKey)
                }
            }
            container.addView(chip)
        }

        addView(container)
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
