package com.novakey.keyboard.ui.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class EmojiKeyboardView(
    context: Context,
    private val onEmojiSelected: (String) -> Unit,
    private val onBackspace: () -> Unit,
    private val onReturnToLetters: () -> Unit
) : LinearLayout(context) {

    private val categories = listOf(
        EmojiCategory("Smileys", "😊", listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥹", "😊",
            "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙",
            "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎",
            "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁",
            "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😮‍💨", "😤"
        )),
        EmojiCategory("Gestures", "👍", listOf(
            "👍", "👎", "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟", "🤘",
            "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "🫵", "👋", "🤚",
            "🖐️", "✋", "🖖", "🫱", "🫲", "👏", "🙌", "👐", "🤲", "🤝",
            "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂"
        )),
        EmojiCategory("Hearts", "❤️", listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
            "✨", "⭐", "🌟", "💫", "💥", "🔥", "🎉", "🎊", "💯", "🎯"
        )),
        EmojiCategory("Objects", "💡", listOf(
            "💡", "📱", "💻", "⌨️", "📝", "✉️", "📦", "☕", "🍕", "🍔",
            "🚀", "🚗", "✈️", "🌍", "🇧🇩", "🇺🇸", "🇬🇧", "🇯🇵", "⏰", "🔒"
        ))
    )

    private var currentCategoryIndex = 0
    private val gridContainer: GridLayout
    private val tabsContainer: LinearLayout

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#0F172A"))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(270))

        // 1. Top Control & Category Bar
        val topBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
            setBackgroundColor(Color.parseColor("#1E293B"))
        }

        val scrollTabs = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
        }

        tabsContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
        }
        scrollTabs.addView(tabsContainer)
        topBar.addView(scrollTabs)

        // Action Keys: Backspace & ABC
        val backspaceBtn = TextView(context).apply {
            text = "⌫"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#334155"))
            }
            background = bg
            setOnClickListener { onBackspace() }
        }
        topBar.addView(backspaceBtn)

        val abcBtn = TextView(context).apply {
            text = "ABC"
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dpToPx(14), dpToPx(6), dpToPx(14), dpToPx(6))
            val bg = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(Color.parseColor("#4F46E5"))
            }
            background = bg
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dpToPx(6)
            }
            layoutParams = params
            setOnClickListener { onReturnToLetters() }
        }
        topBar.addView(abcBtn)
        addView(topBar)

        // 2. Scrollable Emoji Grid
        val scrollView = ScrollView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(220))
        }

        gridContainer = GridLayout(context).apply {
            columnCount = 7
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6))
        }
        scrollView.addView(gridContainer)
        addView(scrollView)

        renderTabs()
        renderEmojis()
    }

    private fun renderTabs() {
        tabsContainer.removeAllViews()
        for ((index, cat) in categories.withIndex()) {
            val isSelected = index == currentCategoryIndex
            val tab = TextView(context).apply {
                text = "${cat.icon} ${cat.name}"
                textSize = 12f
                typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#94A3B8"))
                setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6))

                val bg = GradientDrawable().apply {
                    cornerRadius = dpToPx(8).toFloat()
                    if (isSelected) {
                        setColor(Color.parseColor("#4F46E5"))
                    } else {
                        setColor(Color.TRANSPARENT)
                    }
                }
                background = bg

                setOnClickListener {
                    currentCategoryIndex = index
                    renderTabs()
                    renderEmojis()
                }
            }
            tabsContainer.addView(tab)
        }
    }

    private fun renderEmojis() {
        gridContainer.removeAllViews()
        val currentEmojis = categories[currentCategoryIndex].emojis

        for (emoji in currentEmojis) {
            val emojiView = TextView(context).apply {
                text = emoji
                textSize = 24f
                gravity = Gravity.CENTER
                setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6))

                val params = GridLayout.LayoutParams().apply {
                    width = dpToPx(46)
                    height = dpToPx(46)
                }
                layoutParams = params

                setOnClickListener {
                    onEmojiSelected(emoji)
                }
            }
            gridContainer.addView(emojiView)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    data class EmojiCategory(val name: String, val icon: String, val emojis: List<String>)
}
