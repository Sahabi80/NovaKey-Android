package com.novakey.keyboard.engine.banglish

/**
 * Offline Banglish (Latin script) to Bengali script phonetic transliteration engine.
 * Fast, memory-efficient, and operates 100% locally with zero cloud dependencies.
 */
object BanglishConverter {

    private val PHONETIC_MAP = listOf(
        "kkh" to "ক্ষ",
        "ggh" to "জ্ঞ",
        "bho" to "ভো",
        "dho" to "ধো",
        "cho" to "চো",
        "bha" to "ভা",
        "dha" to "ধা",
        "cha" to "চা",
        "sha" to "শা",
        "sho" to "শো",
        "khi" to "খি",
        "khe" to "খে",
        "kho" to "খো",
        "kha" to "খা",
        "ghi" to "ঘি",
        "ghe" to "ঘে",
        "gho" to "ঘো",
        "gha" to "ঘা",
        "thi" to "থি",
        "the" to "থে",
        "tho" to "থো",
        "tha" to "থা",
        "phi" to "ফি",
        "phe" to "ফে",
        "pho" to "ফো",
        "pha" to "ফা",
        "kh" to "খ",
        "gh" to "ঘ",
        "ch" to "চ",
        "jh" to "ঝ",
        "th" to "থ",
        "dh" to "ধ",
        "ph" to "ফ",
        "bh" to "ভ",
        "sh" to "শ",
        "ng" to "ঙ",
        "k" to "ক",
        "g" to "গ",
        "c" to "চ",
        "j" to "জ",
        "t" to "ত",
        "d" to "দ",
        "n" to "ন",
        "p" to "প",
        "f" to "ফ",
        "b" to "ব",
        "m" to "ম",
        "r" to "র",
        "l" to "ল",
        "s" to "স",
        "h" to "হ",
        "y" to "য়",
        "w" to "ও",
        "aa" to "আ",
        "ee" to "ঈ",
        "oo" to "ঊ",
        "oi" to "ঐ",
        "ou" to "ঔ",
        "a" to "আ",
        "i" to "ই",
        "u" to "উ",
        "e" to "এ",
        "o" to "ও"
    )

    /**
     * Converts a single word or full sentence of Banglish into Bangla script.
     */
    fun convert(text: String): String {
        if (text.isBlank()) return ""

        val regex = Regex("(\\s+|[.,!?;:])")
        val parts = text.split(regex)

        val sb = StringBuilder()
        var lastIdx = 0

        for (match in regex.findAll(text)) {
            val token = text.substring(lastIdx, match.range.first)
            if (token.isNotEmpty()) {
                sb.append(convertToken(token))
            }
            sb.append(match.value)
            lastIdx = match.range.last + 1
        }
        if (lastIdx < text.length) {
            sb.append(convertToken(text.substring(lastIdx)))
        }

        return sb.toString()
    }

    private fun convertToken(token: String): String {
        if (token.isBlank()) return token
        val lower = token.lowercase()

        // 1. Check exact dictionary match
        BanglishDictionary.lookup(lower)?.let { return it }

        // 2. Fall back to phonetic table substitution
        var result = lower
        for ((pattern, bangla) in PHONETIC_MAP) {
            result = result.replace(pattern, bangla)
        }
        return result
    }

    /**
     * Generates top 3-4 suggestions for the active typed word in the candidate bar:
     * e.g. "ami" -> ["আমি", "ami", "Ami"]
     */
    fun getCandidates(rawWord: String): List<String> {
        if (rawWord.isBlank()) return emptyList()

        val clean = rawWord.trim().lowercase()
        val candidates = mutableListOf<String>()

        // 1. Converted Bengali representation
        val bangla = BanglishDictionary.lookup(clean) ?: convert(clean)
        if (bangla.isNotEmpty() && bangla != clean) {
            candidates.add(bangla)
        }

        // 2. Exact word as typed
        candidates.add(rawWord)

        // 3. Capitalized variant if not all uppercase
        if (rawWord.first().isLowerCase()) {
            candidates.add(rawWord.replaceFirstChar { it.uppercase() })
        }

        return candidates.distinct().take(4)
    }
}
