package com.novakey.keyboard.ui.settings

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.novakey.keyboard.ai.OnDeviceAiProvider
import com.novakey.keyboard.ai.model.OnDeviceStatus
import kotlinx.coroutines.launch

/**
 * Onboarding and Setup Activity for NovaKey
 * Displays setup steps, on-device Gemini Nano availability status, and test typing area.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var enableButton: Button
    private lateinit var switchButton: Button
    private val onDeviceProvider by lazy { OnDeviceAiProvider(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0F172A")) // Dark background
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(24), dpToPx(36), dpToPx(24), dpToPx(36))
        }

        // Header Title
        val title = TextView(this).apply {
            text = "NovaKey AI Keyboard"
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }
        container.addView(title)

        // Subtitle
        val subtitle = TextView(this).apply {
            text = "Native Android Input Method with Gemini Nano & Local AI"
            textSize = 14f
            setTextColor(Color.parseColor("#94A3B8"))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(6)
                bottomMargin = dpToPx(28)
            }
            layoutParams = params
        }
        container.addView(subtitle)

        // Status Card
        statusText = TextView(this).apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.parseColor("#1E293B"))
                setStroke(dpToPx(1), Color.parseColor("#334155"))
            }
            background = bg
            setTextColor(Color.parseColor("#38BDF8"))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(24)
            }
            layoutParams = params
        }
        container.addView(statusText)

        // Step 1: Enable Button
        enableButton = Button(this).apply {
            text = "1. Enable NovaKey in Settings"
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.parseColor("#4F46E5"))
            }
            background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(50)
            ).apply {
                bottomMargin = dpToPx(14)
            }
            layoutParams = params
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }
        container.addView(enableButton)

        // Step 2: Switch Button
        switchButton = Button(this).apply {
            text = "2. Select NovaKey as Active Keyboard"
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.parseColor("#2563EB"))
            }
            background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(50)
            ).apply {
                bottomMargin = dpToPx(28)
            }
            layoutParams = params
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
        container.addView(switchButton)

        // Test Input Box Section
        val testLabel = TextView(this).apply {
            text = "Test Typing Area:"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#CBD5E1"))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(8)
            }
            layoutParams = params
        }
        container.addView(testLabel)

        val testEditText = EditText(this).apply {
            hint = "Tap here to test NovaKey typing..."
            setHintTextColor(Color.parseColor("#64748B"))
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(10).toFloat()
                setColor(Color.parseColor("#1E293B"))
                setStroke(dpToPx(1), Color.parseColor("#475569"))
            }
            background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = params
        }
        container.addView(testEditText)

        // AI Availability Status Card (Phase 4.1 Specification)
        val availabilityCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.parseColor("#1E293B"))
                setStroke(dpToPx(1), Color.parseColor("#334155"))
            }
            background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(20)
            }
            layoutParams = params
        }

        val availTitle = TextView(this).apply {
            text = "🤖 AI Engine Availability"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#38BDF8"))
        }
        availabilityCard.addView(availTitle)

        val localAiText = TextView(this).apply {
            text = "Local AI\n✓ Available offline"
            textSize = 13f
            setTextColor(Color.WHITE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(10)
            }
            layoutParams = params
        }
        availabilityCard.addView(localAiText)

        val onDeviceStatusView = TextView(this).apply {
            text = "On-device AI (Gemini Nano)\n⏳ Checking availability..."
            textSize = 13f
            setTextColor(Color.WHITE)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(10)
            }
            layoutParams = params
        }
        availabilityCard.addView(onDeviceStatusView)

        val downloadBtn = Button(this).apply {
            text = "⬇ Initiate Gemini Nano Download via AICore"
            textSize = 12f
            isAllCaps = false
            setTextColor(Color.WHITE)
            visibility = View.GONE
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#4F46E5"))
            }
            background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(40)
            ).apply {
                topMargin = dpToPx(8)
            }
            layoutParams = params
        }
        availabilityCard.addView(downloadBtn)

        val cloudAiText = TextView(this).apply {
            text = "Cloud AI\n• Optional (Available via server proxy)"
            textSize = 13f
            setTextColor(Color.parseColor("#94A3B8"))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(10)
            }
            layoutParams = params
        }
        availabilityCard.addView(cloudAiText)

        container.addView(availabilityCard)

        // Check On-Device Status asynchronously
        lifecycleScope.launch {
            val status = onDeviceProvider.checkAvailability()
            when (status) {
                OnDeviceStatus.AVAILABLE -> {
                    onDeviceStatusView.text = "On-device AI (Gemini Nano)\n• Available on this device (0ms, Private)"
                    onDeviceStatusView.setTextColor(Color.parseColor("#4ADE80")) // Green
                    downloadBtn.visibility = View.GONE
                }
                OnDeviceStatus.DOWNLOADABLE -> {
                    onDeviceStatusView.text = "On-device AI (Gemini Nano)\n• Downloadable via Android AICore"
                    onDeviceStatusView.setTextColor(Color.parseColor("#FBBF24")) // Yellow
                    downloadBtn.visibility = View.VISIBLE
                    downloadBtn.setOnClickListener {
                        downloadBtn.isEnabled = false
                        downloadBtn.text = "⏳ Requesting download via AICore..."
                        lifecycleScope.launch {
                            val success = onDeviceProvider.requestModelDownload()
                            if (success) {
                                onDeviceStatusView.text = "On-device AI (Gemini Nano)\n• Downloading via Play Services / AICore"
                                downloadBtn.text = "✓ Download Initiated by System"
                            } else {
                                downloadBtn.text = "Managed by Android System Settings"
                            }
                        }
                    }
                }
                OnDeviceStatus.DOWNLOADING -> {
                    onDeviceStatusView.text = "On-device AI (Gemini Nano)\n• Downloading via Play Services / AICore"
                    onDeviceStatusView.setTextColor(Color.parseColor("#38BDF8"))
                    downloadBtn.visibility = View.GONE
                }
                OnDeviceStatus.UNAVAILABLE -> {
                    onDeviceStatusView.text = "On-device AI (Gemini Nano)\n• Unsupported/Unavailable on this hardware"
                    onDeviceStatusView.setTextColor(Color.parseColor("#94A3B8"))
                    downloadBtn.visibility = View.GONE
                }
            }
        }

        // Feature Testing Guide Card
        val guideCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.parseColor("#1E293B"))
                setStroke(dpToPx(1), Color.parseColor("#334155"))
            }
            background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(20)
            }
            layoutParams = params
        }

        val guideTitle = TextView(this).apply {
            text = "✨ Phase 4.1 Hybrid AI & Gemini Nano Features"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#38BDF8"))
        }
        guideCard.addView(guideTitle)

        val guideBody = TextView(this).apply {
            text = "• Routing Policy:\n" +
                    "   - Grammar: LOCAL → ON_DEVICE → CLOUD\n" +
                    "   - Rewrite: LOCAL → ON_DEVICE → CLOUD\n" +
                    "   - Tone: LOCAL → ON_DEVICE → CLOUD\n" +
                    "   - Translation: LOCAL/basic → ON_DEVICE → CLOUD\n" +
                    "   - Context Action: LOCAL → ON_DEVICE → CLOUD\n" +
                    "   - Custom Prompt: ON_DEVICE → CLOUD → LOCAL\n" +
                    "• Real ML Kit GenAI Prompt API: Uses official GenerativeModel checkStatus() & generateContent().\n" +
                    "• No Model Downloads While Typing: Download can only be explicitly initiated in Settings.\n" +
                    "• Zero API Keys in APK: Safe and privacy-hardened; sensitive fields completely bypass AI."
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
            setLineSpacing(dpToPx(4).toFloat(), 1.0f)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            layoutParams = params
        }
        guideCard.addView(guideBody)
        container.addView(guideCard)

        root.addView(container)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        updateSetupState()
    }

    private fun updateSetupState() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = imm.enabledInputMethodList
        val isNovaKeyEnabled = enabledMethods.any { it.packageName == packageName }

        val currentIme = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val isNovaKeySelected = currentIme?.contains(packageName) == true

        when {
            isNovaKeySelected -> {
                statusText.text = "✓ NovaKey is active! You can type in any app."
                statusText.setTextColor(Color.parseColor("#4ADE80")) // Green
            }
            isNovaKeyEnabled -> {
                statusText.text = "Step 1 complete! Now select NovaKey as your active keyboard."
                statusText.setTextColor(Color.parseColor("#FBBF24")) // Yellow
            }
            else -> {
                statusText.text = "NovaKey is not enabled yet. Complete Step 1 above."
                statusText.setTextColor(Color.parseColor("#F87171")) // Red
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
