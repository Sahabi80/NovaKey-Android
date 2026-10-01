package com.novakey.keyboard.ui.keyboard

enum class KeyType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    SPACE,
    SWITCH_LAYER,
    SWITCH_LANGUAGE,
    EMOJI,
    SETTINGS
}

data class KeyModel(
    val primaryLabel: String,
    val shiftLabel: String = primaryLabel.uppercase(),
    val secondaryLabel: String? = null,
    val type: KeyType = KeyType.CHARACTER,
    val weight: Float = 1.0f,
    val targetLayer: String? = null
)

object KeyboardLayouts {

    val QWERTY_ROW_1 = listOf(
        KeyModel("q", secondaryLabel = "1"), KeyModel("w", secondaryLabel = "2"),
        KeyModel("e", secondaryLabel = "3"), KeyModel("r", secondaryLabel = "4"),
        KeyModel("t", secondaryLabel = "5"), KeyModel("y", secondaryLabel = "6"),
        KeyModel("u", secondaryLabel = "7"), KeyModel("i", secondaryLabel = "8"),
        KeyModel("o", secondaryLabel = "9"), KeyModel("p", secondaryLabel = "0")
    )

    val QWERTY_ROW_2 = listOf(
        KeyModel("a", secondaryLabel = "@"), KeyModel("s", secondaryLabel = "#"),
        KeyModel("d", secondaryLabel = "$"), KeyModel("f", secondaryLabel = "%"),
        KeyModel("g", secondaryLabel = "&"), KeyModel("h", secondaryLabel = "-"),
        KeyModel("j", secondaryLabel = "+"), KeyModel("k", secondaryLabel = "("),
        KeyModel("l", secondaryLabel = ")")
    )

    val QWERTY_ROW_3 = listOf(
        KeyModel("⇧", type = KeyType.SHIFT, weight = 1.4f),
        KeyModel("z", secondaryLabel = "*"), KeyModel("x", secondaryLabel = "\""),
        KeyModel("c", secondaryLabel = "'"), KeyModel("v", secondaryLabel = ":"),
        KeyModel("b", secondaryLabel = ";"), KeyModel("n", secondaryLabel = "!"),
        KeyModel("m", secondaryLabel = "?"),
        KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.4f)
    )

    // Direct Bangla Script Layout (for Bangla input mode)
    val BANGLA_ROW_1 = listOf(
        KeyModel("ড", shiftLabel = "ঢ"), KeyModel("ফ", shiftLabel = "ঋ"),
        KeyModel("এ", shiftLabel = "ঐ"), KeyModel("র", shiftLabel = "ড়"),
        KeyModel("ট", shiftLabel = "ঠ"), KeyModel("চ", shiftLabel = "ছ"),
        KeyModel("উ", shiftLabel = "ঊ"), KeyModel("ই", shiftLabel = "ঈ"),
        KeyModel("ও", shiftLabel = "ঔ"), KeyModel("প", shiftLabel = "ফ")
    )

    val BANGLA_ROW_2 = listOf(
        KeyModel("া", shiftLabel = "অ"), KeyModel("স", shiftLabel = "ষ"),
        KeyModel("দ", shiftLabel = "ধ"), KeyModel("ব", shiftLabel = "ভ"),
        KeyModel("গ", shiftLabel = "ঘ"), KeyModel("হ", shiftLabel = "ঃ"),
        KeyModel("জ", shiftLabel = "ঝ"), KeyModel("ক", shiftLabel = "খ"),
        KeyModel("ল", shiftLabel = "ং")
    )

    val BANGLA_ROW_3 = listOf(
        KeyModel("⇧", type = KeyType.SHIFT, weight = 1.3f),
        KeyModel("য", shiftLabel = "য়"), KeyModel("ত", shiftLabel = "থ"),
        KeyModel("ম", shiftLabel = "ণ"), KeyModel("ন", shiftLabel = "ঁ"),
        KeyModel("ব", shiftLabel = "র"), KeyModel("ি", shiftLabel = "ী"),
        KeyModel("ু", shiftLabel = "ূ"),
        KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.3f)
    )

    // Default bottom row for text layers (QWERTY / Bangla)
    val BOTTOM_ROW = listOf(
        KeyModel("?123", type = KeyType.SWITCH_LAYER, weight = 1.2f, targetLayer = "SYMBOLS"),
        KeyModel("😊", type = KeyType.EMOJI, weight = 1.0f),
        KeyModel("space", type = KeyType.SPACE, weight = 4.0f),
        KeyModel(".", weight = 0.9f),
        KeyModel("↵", type = KeyType.ENTER, weight = 1.5f)
    )

    // ==========================================
    // MAIN SYMBOLS PAGE
    // ==========================================

    // Row 1: 1 2 3 4 5 6 7 8 9 0
    val SYMBOLS_ROW_1 = listOf(
        KeyModel("1"), KeyModel("2"), KeyModel("3"), KeyModel("4"), KeyModel("5"),
        KeyModel("6"), KeyModel("7"), KeyModel("8"), KeyModel("9"), KeyModel("0")
    )

    // Row 2: @ # $ % & - + ( ) /
    val SYMBOLS_ROW_2 = listOf(
        KeyModel("@"), KeyModel("#"), KeyModel("$"), KeyModel("%"), KeyModel("&"),
        KeyModel("-"), KeyModel("+"), KeyModel("("), KeyModel(")"), KeyModel("/")
    )

    // Row 3: =< (Layer Switch to More Symbols) * " ' : ; ! ? = _ Backspace
    val SYMBOLS_ROW_3 = listOf(
        KeyModel("=\\<", type = KeyType.SWITCH_LAYER, weight = 1.3f, targetLayer = "MORE_SYMBOLS"),
        KeyModel("*"), KeyModel("\""), KeyModel("'"), KeyModel(":"),
        KeyModel(";"), KeyModel("!"), KeyModel("?"), KeyModel("="), KeyModel("_"),
        KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.3f)
    )

    // Dedicated bottom row for symbols: ABC | , | 😊 | space | . | Enter
    val SYMBOLS_BOTTOM_ROW = listOf(
        KeyModel("ABC", type = KeyType.SWITCH_LAYER, weight = 1.2f, targetLayer = "QWERTY"),
        KeyModel(",", weight = 0.9f),
        KeyModel("😊", type = KeyType.EMOJI, weight = 1.0f),
        KeyModel("space", type = KeyType.SPACE, weight = 3.5f),
        KeyModel(".", weight = 0.9f),
        KeyModel("↵", type = KeyType.ENTER, weight = 1.5f)
    )

    // ==========================================
    // ADVANCED / MORE SYMBOLS PAGE
    // ==========================================

    // Row 1: Core brackets and programming symbols (~ \ | ^ < > [ ] { })
    val MORE_SYMBOLS_ROW_1 = listOf(
        KeyModel("~"), KeyModel("\\"), KeyModel("|"), KeyModel("^"),
        KeyModel("<"), KeyModel(">"), KeyModel("["), KeyModel("]"),
        KeyModel("{"), KeyModel("}")
    )

    // Row 2: Currency, math, and typography symbols (€ £ ¥ ` ° • × ÷ ± ≠)
    val MORE_SYMBOLS_ROW_2 = listOf(
        KeyModel("€"), KeyModel("£"), KeyModel("¥"), KeyModel("`"),
        KeyModel("°"), KeyModel("•"), KeyModel("×"), KeyModel("÷"),
        KeyModel("±"), KeyModel("≠")
    )

    // Row 3: ?123 (Layer Switch back to Main Symbols) © ® ™ ≤ ≥ « » → … Backspace
    val MORE_SYMBOLS_ROW_3 = listOf(
        KeyModel("?123", type = KeyType.SWITCH_LAYER, weight = 1.3f, targetLayer = "SYMBOLS"),
        KeyModel("©"), KeyModel("®"), KeyModel("™"), KeyModel("≤"),
        KeyModel("≥"), KeyModel("«"), KeyModel("»"), KeyModel("→"),
        KeyModel("…"),
        KeyModel("⌫", type = KeyType.BACKSPACE, weight = 1.3f)
    )
}
