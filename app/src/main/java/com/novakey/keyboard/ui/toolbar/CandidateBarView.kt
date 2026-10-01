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

/**
 * Candidate and suggestion bar displayed above the keyboard.
 * Optimized for high-frequency typing: reuses child views, eliminates allocation churn,
 * caches drawables and click listeners to prevent GC pauses on rapid keystrokes.
 */
class CandidateBarView(
    context: Context,
    private val onCandidateClicked: (String) -> Unit
) : HorizontalScrollView(context) {

    private val container: LinearLayout
    private val placeholderView: TextView
    private val candidateViews = mutableListOf<TextView>()
    private var lastCandidates: List<String> = emptyList()

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#1E293B")) // Slate 800 background

        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(42))
        }

        placeholderView = TextView(context).apply {
            text = "✨ NovaKey Local Intelligence"
            textSize = 12f
            setTextColor(Color.parseColor("#64748B"))
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            visibility = View.VISIBLE
        }
        container.addView(placeholderView)

        addView(container)
    }

    private fun createCandidateTextView(): TextView {
        return TextView(context).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(dpToPx(14), dpToPx(6), dpToPx(14), dpToPx(6))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(8)
            }
            layoutParams = params
            // Static click listener bound once per view to eliminate per-keystroke closure allocations
            setOnClickListener {
                val text = this.text?.toString()
                if (!text.isNullOrEmpty()) {
                    onCandidateClicked(text)
                }
            }
        }
    }

    private fun getOrCreateCandidateView(index: Int): TextView {
        if (index < candidateViews.size) {
            return candidateViews[index]
        }
        val newView = createCandidateTextView()
        candidateViews.add(newView)
        container.addView(newView)
        return newView
    }

    /**
     * Updates the displayed candidate chips dynamically using view recycling.
     */
    fun setCandidates(candidates: List<String>) {
        if (candidates == lastCandidates) {
            return
        }
        lastCandidates = candidates.toList()

        if (candidates.isEmpty()) {
            if (placeholderView.visibility != View.VISIBLE) {
                placeholderView.visibility = View.VISIBLE
            }
            for (view in candidateViews) {
                if (view.visibility != View.GONE) {
                    view.visibility = View.GONE
                }
            }
            return
        }

        if (placeholderView.visibility != View.GONE) {
            placeholderView.visibility = View.GONE
        }

        for ((index, candidate) in candidates.withIndex()) {
            val isFirst = index == 0
            val view = getOrCreateCandidateView(index)

            if (view.text != candidate) {
                view.text = candidate
            }

            // Style caching: only recreate background and style when role changes (first vs secondary)
            val targetStyle = if (isFirst) 1 else 0
            if (view.tag != targetStyle) {
                view.tag = targetStyle
                view.typeface = if (isFirst) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                view.setTextColor(if (isFirst) Color.parseColor("#38BDF8") else Color.parseColor("#F1F5F9"))

                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(8).toFloat()
                    if (isFirst) {
                        setColor(Color.parseColor("#0F172A"))
                        setStroke(dpToPx(1), Color.parseColor("#38BDF8"))
                    } else {
                        setColor(Color.parseColor("#334155"))
                    }
                }
                view.background = bg
            }

            if (view.visibility != View.VISIBLE) {
                view.visibility = View.VISIBLE
            }
        }

        // Hide any surplus views without removing them from hierarchy
        for (i in candidates.size until candidateViews.size) {
            if (candidateViews[i].visibility != View.GONE) {
                candidateViews[i].visibility = View.GONE
            }
        }

        // Reset scroll position to beginning on new candidates
        scrollTo(0, 0)
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
