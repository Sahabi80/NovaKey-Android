package com.novakey.keyboard.ui.toolbar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

class CandidateBarView(
    context: Context,
    private val onCandidateClicked: (String) -> Unit
) : HorizontalScrollView(context) {

    private val container: LinearLayout

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#1E293B")) // Slate 800 background

        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(42))
        }
        addView(container)
    }

    /**
     * Updates the displayed candidate chips dynamically.
     */
    fun setCandidates(candidates: List<String>) {
        container.removeAllViews()

        if (candidates.isEmpty()) {
            // Show subtle prompt when no active word
            val placeholder = TextView(context).apply {
                text = "✨ NovaKey Local Intelligence"
                textSize = 12f
                setTextColor(Color.parseColor("#64748B"))
                setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            }
            container.addView(placeholder)
            return
        }

        for ((index, candidate) in candidates.withIndex()) {
            val isFirst = index == 0
            val candidateView = TextView(context).apply {
                text = candidate
                textSize = 15f
                typeface = if (isFirst) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (isFirst) Color.parseColor("#38BDF8") else Color.parseColor("#F1F5F9"))
                gravity = Gravity.CENTER
                setPadding(dpToPx(14), dpToPx(6), dpToPx(14), dpToPx(6))

                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(8).toFloat()
                    if (isFirst) {
                        setColor(Color.parseColor("#0F172A"))
                        setStroke(dpToPx(1), Color.parseColor("#38BDF8"))
                    } else {
                        setColor(Color.parseColor("#334155"))
                    }
                }
                background = bg

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = dpToPx(8)
                }
                layoutParams = params

                setOnClickListener {
                    onCandidateClicked(candidate)
                }
            }
            container.addView(candidateView)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
