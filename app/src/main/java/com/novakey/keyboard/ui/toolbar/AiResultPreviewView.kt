package com.novakey.keyboard.ui.toolbar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.novakey.keyboard.ai.model.ProviderSource

/**
 * Compact preview card showing AI-generated result before modifying user text.
 * Provides explicit [Replace], [Insert], [Copy], and [Cancel] operations.
 * Displays a subtle provider indicator badge ([LOCAL], [ON_DEVICE], [CLOUD]).
 */
class AiResultPreviewView(
    context: Context,
    private val onReplace: (String) -> Unit,
    private val onInsert: (String) -> Unit,
    private val onCancel: () -> Unit
) : LinearLayout(context) {

    private val titleView: TextView
    private val providerBadge: TextView
    private val resultTextView: TextView
    private val explanationView: TextView
    private var currentOutput: String = ""

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#0F172A")) // Dark Slate 900
        setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))

        // 1. Header Row (Title + Provider Badge)
        val headerRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        titleView = TextView(context).apply {
            text = "✨ NovaKey AI"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#38BDF8"))
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
        }
        headerRow.addView(titleView)

        providerBadge = TextView(context).apply {
            text = "LOCAL"
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(4).toFloat()
                setColor(Color.parseColor("#1E293B"))
                setStroke(dpToPx(1), Color.parseColor("#334155"))
            }
            background = bg
        }
        headerRow.addView(providerBadge)
        addView(headerRow)

        // 2. Result text card
        resultTextView = TextView(context).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#1E293B"))
                setStroke(dpToPx(1), Color.parseColor("#475569"))
            }
            background = bg
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = dpToPx(4)
                bottomMargin = dpToPx(4)
            }
            layoutParams = params
        }
        addView(resultTextView)

        // 3. Explanation text
        explanationView = TextView(context).apply {
            textSize = 11f
            setTextColor(Color.parseColor("#94A3B8"))
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dpToPx(6)
            }
            layoutParams = params
        }
        addView(explanationView)

        // 4. Action Buttons Strip: [Cancel] [Copy] [Insert] [Replace]
        val scrollButtons = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        val buttonRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.END
            setPadding(0, 0, 0, dpToPx(2))
        }

        // Cancel Button
        val cancelBtn = Button(context).apply {
            text = "Cancel"
            textSize = 11f
            isAllCaps = false
            setTextColor(Color.parseColor("#94A3B8"))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(6).toFloat()
                setColor(Color.parseColor("#334155"))
            }
            background = bg
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(34)).apply {
                marginEnd = dpToPx(6)
            }
            layoutParams = params
            setOnClickListener { onCancel() }
        }
        buttonRow.addView(cancelBtn)

        // Copy Button
        val copyBtn = Button(context).apply {
            text = "📋 Copy"
            textSize = 11f
            isAllCaps = false
            setTextColor(Color.parseColor("#E2E8F0"))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(6).toFloat()
                setColor(Color.parseColor("#334155"))
            }
            background = bg
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(34)).apply {
                marginEnd = dpToPx(6)
            }
            layoutParams = params
            setOnClickListener {
                copyToClipboard(currentOutput)
                text = "✓ Copied"
                postDelayed({ text = "📋 Copy" }, 1500)
            }
        }
        buttonRow.addView(copyBtn)

        // Insert Button (for new drafts, replies, continuation)
        val insertBtn = Button(context).apply {
            text = "+ Insert"
            textSize = 11f
            isAllCaps = false
            setTextColor(Color.WHITE)
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(6).toFloat()
                setColor(Color.parseColor("#2563EB")) // Blue 600
            }
            background = bg
            val params = LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(34)).apply {
                marginEnd = dpToPx(6)
            }
            layoutParams = params
            setOnClickListener { onInsert(currentOutput) }
        }
        buttonRow.addView(insertBtn)

        // Replace Button (replaces target text)
        val replaceBtn = Button(context).apply {
            text = "✓ Replace"
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            setTextColor(Color.WHITE)
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(6).toFloat()
                setColor(Color.parseColor("#4F46E5")) // Indigo 600
            }
            background = bg
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, dpToPx(34))
            setOnClickListener { onReplace(currentOutput) }
        }
        buttonRow.addView(replaceBtn)

        scrollButtons.addView(buttonRow)
        addView(scrollButtons)
    }

    fun showResult(
        title: String,
        output: String,
        explanation: String? = null,
        provider: ProviderSource = ProviderSource.LOCAL
    ) {
        titleView.text = title
        currentOutput = output
        resultTextView.text = output

        // Provider Badge
        providerBadge.text = provider.badge
        when (provider) {
            ProviderSource.ON_DEVICE -> {
                providerBadge.setTextColor(Color.parseColor("#4ADE80")) // Green
            }
            ProviderSource.CLOUD -> {
                providerBadge.setTextColor(Color.parseColor("#38BDF8")) // Sky Blue
            }
            ProviderSource.LOCAL -> {
                providerBadge.setTextColor(Color.parseColor("#94A3B8")) // Slate
            }
        }

        if (!explanation.isNullOrBlank()) {
            explanationView.text = "💡 $explanation"
            explanationView.visibility = VISIBLE
        } else {
            explanationView.visibility = GONE
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("NovaKey AI Output", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
