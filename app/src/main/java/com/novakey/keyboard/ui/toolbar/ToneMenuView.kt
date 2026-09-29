package com.novakey.keyboard.ui.toolbar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.novakey.keyboard.engine.tone.ToneCategory

class ToneMenuView(
    context: Context,
    private val onToneSelected: (ToneCategory) -> Unit,
    private val onClose: () -> Unit
) : HorizontalScrollView(context) {

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#1E293B"))

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
            setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))
            setOnClickListener { onClose() }
        }
        container.addView(closeBtn)

        val label = TextView(context).apply {
            text = "Choose Tone:"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#38BDF8"))
            setPadding(dpToPx(4), dpToPx(4), dpToPx(8), dpToPx(4))
        }
        container.addView(label)

        for (tone in ToneCategory.values()) {
            val chip = TextView(context).apply {
                text = "${tone.icon} ${tone.displayName}"
                textSize = 12f
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
                    onToneSelected(tone)
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
