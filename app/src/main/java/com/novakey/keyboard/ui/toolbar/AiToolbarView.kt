package com.novakey.keyboard.ui.toolbar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.novakey.keyboard.controller.AiAction
import com.novakey.keyboard.controller.InputLanguage
import com.novakey.keyboard.controller.KeyboardActionHandler

class AiToolbarView(
    context: Context,
    private val actionHandler: KeyboardActionHandler?
) : HorizontalScrollView(context) {

    private val container: LinearLayout
    private var langChip: TextView? = null
    private var currentLanguage: InputLanguage = InputLanguage.ENGLISH

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#0F172A")) // Dark background

        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6))
        }

        renderToolbar()
        addView(container)
    }

    fun setLanguage(language: InputLanguage) {
        currentLanguage = language
        langChip?.text = "🌐 ${language.badge}"
    }

    private fun renderToolbar() {
        container.removeAllViews()

        // 1. [ ✨ AI ] (Opens compact AI menu)
        container.addView(createChip("✨ AI", isAccent = true) {
            actionHandler?.onAiActionRequested(AiAction.AI_MENU)
        })

        // 2. [ ✓ Grammar ] (Natural proofreading)
        container.addView(createChip("✓ Grammar") {
            actionHandler?.onAiActionRequested(AiAction.GRAMMAR)
        })

        // 3. [ Rewrite ] (Rewrite modes: Improve, Natural, Shorter, Clearer, Professional, Continue)
        container.addView(createChip("Rewrite") {
            actionHandler?.onAiActionRequested(AiAction.REWRITE)
        })

        // 4. [ Tone ] (10 Tones: Friendly, Professional, Respectful, Confident, etc.)
        container.addView(createChip("Tone") {
            actionHandler?.onAiActionRequested(AiAction.TONE)
        })

        // 5. Language Switcher (EN / বাং / বং-EN)
        langChip = createChip("🌐 ${currentLanguage.badge}") {
            val next = when (currentLanguage) {
                InputLanguage.ENGLISH -> InputLanguage.BANGLISH
                InputLanguage.BANGLISH -> InputLanguage.BANGLA
                InputLanguage.BANGLA -> InputLanguage.ENGLISH
            }
            actionHandler?.onLanguageSelected(next)
        }
        container.addView(langChip)
    }

    private fun createChip(label: String, isAccent: Boolean = false, onClick: () -> Unit): TextView {
        return TextView(context).apply {
            text = label
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))

            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(14).toFloat()
                if (isAccent) {
                    setColor(Color.parseColor("#4F46E5")) // Indigo 600
                } else {
                    setColor(Color.parseColor("#1E293B")) // Slate 800
                    setStroke(dpToPx(1), Color.parseColor("#334155")) // Slate 700
                }
            }
            background = bg
            setTextColor(if (isAccent) Color.WHITE else Color.parseColor("#E2E8F0"))

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(6)
            }
            this.layoutParams = layoutParams

            setOnClickListener { onClick() }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
