package com.novakey.keyboard.engine.tone

enum class ToneCategory(val displayName: String, val icon: String) {
    FRIENDLY("Friendly", "👋"),
    PROFESSIONAL("Professional", "💼"),
    RESPECTFUL("Respectful", "🙏"),
    CONFIDENT("Confident", "💪"),
    HAPPY("Happy", "😊"),
    CASUAL("Casual", "😎"),
    FORMAL("Formal", "👔"),
    POLITE("Polite", "✨"),
    EMPATHETIC("Empathetic", "❤️"),
    DIRECT("Direct", "🎯")
}

data class ToneTransformationResult(
    val tone: ToneCategory,
    val originalText: String,
    val transformedText: String,
    val explanation: String
)

/**
 * Local offline rule-based Tone Engine.
 * Supports all 10 standard tones with 0ms latency and 100% on-device execution.
 */
object LocalToneEngine {

    private val INFORMAL_TO_FORMAL = listOf(
        Regex("\\bgonna\\b", RegexOption.IGNORE_CASE) to "going to",
        Regex("\\bwanna\\b", RegexOption.IGNORE_CASE) to "want to",
        Regex("\\bgotta\\b", RegexOption.IGNORE_CASE) to "must",
        Regex("\\bkinda\\b", RegexOption.IGNORE_CASE) to "somewhat",
        Regex("\\bain't\\b", RegexOption.IGNORE_CASE) to "is not",
        Regex("\\bu\\b", RegexOption.IGNORE_CASE) to "you",
        Regex("\\bur\\b", RegexOption.IGNORE_CASE) to "your",
        Regex("\\br\\b", RegexOption.IGNORE_CASE) to "are",
        Regex("\\bpls\\b|\\bplz\\b", RegexOption.IGNORE_CASE) to "please",
        Regex("\\bthx\\b|\\bthanks\\b", RegexOption.IGNORE_CASE) to "thank you",
        Regex("\\basap\\b", RegexOption.IGNORE_CASE) to "at your earliest convenience",
        Regex("\\bidk\\b", RegexOption.IGNORE_CASE) to "I am not certain",
        Regex("\\bcuz\\b|\\bcoz\\b|\\bbcz\\b", RegexOption.IGNORE_CASE) to "because",
        Regex("\\bbtw\\b", RegexOption.IGNORE_CASE) to "incidentally",
        Regex("\\bnope\\b", RegexOption.IGNORE_CASE) to "no",
        Regex("\\byep\\b|\\byeah\\b", RegexOption.IGNORE_CASE) to "yes",
        Regex("\\bok\\b|\\bk\\b", RegexOption.IGNORE_CASE) to "understood"
    )

    fun transform(text: String, tone: ToneCategory): ToneTransformationResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return ToneTransformationResult(tone, text, text, "Empty text provided.")
        }

        var working = trimmed

        when (tone) {
            ToneCategory.PROFESSIONAL -> {
                for ((pattern, replacement) in INFORMAL_TO_FORMAL) {
                    working = pattern.replace(working, replacement)
                }
                working = working
                    .replace(Regex("\\bI think\\b", RegexOption.IGNORE_CASE), "In my assessment,")
                    .replace(Regex("\\bI want\\b", RegexOption.IGNORE_CASE), "I would like to request")
                    .replace(Regex("\\bcan you\\b", RegexOption.IGNORE_CASE), "would you be able to")
                    .replace(Regex("\\bgive me\\b", RegexOption.IGNORE_CASE), "could you provide")
                    .replace(Regex("\\bsend me the file\\b", RegexOption.IGNORE_CASE), "Could you please send me the file?")
                working = formatSentence(working)
            }

            ToneCategory.FORMAL -> {
                for ((pattern, replacement) in INFORMAL_TO_FORMAL) {
                    working = pattern.replace(working, replacement)
                }
                working = formatSentence(working)
                if (!working.startsWith("Dear", ignoreCase = true) && !working.startsWith("Greetings", ignoreCase = true)) {
                    working = "Greetings, $working"
                }
            }

            ToneCategory.RESPECTFUL -> {
                for ((pattern, replacement) in INFORMAL_TO_FORMAL) {
                    working = pattern.replace(working, replacement)
                }
                working = formatSentence(working)
                if (!working.startsWith("Respected", ignoreCase = true) && !working.startsWith("Dear", ignoreCase = true)) {
                    working = "Respected Sir/Madam, ${working.replaceFirstChar { it.lowercase() }}"
                }
                if (!working.endsWith("Thank you.", ignoreCase = true)) {
                    working += " Thank you for your consideration."
                }
            }

            ToneCategory.POLITE -> {
                working = working
                    .replace(Regex("\\bsend me\\b", RegexOption.IGNORE_CASE), "would you mind sending me")
                    .replace(Regex("\\bgive me\\b", RegexOption.IGNORE_CASE), "could you kindly provide")
                if (!working.startsWith("Please", ignoreCase = true)) {
                    working = "Please ${working.replaceFirstChar { it.lowercase() }}"
                }
                working = formatSentence(working)
            }

            ToneCategory.FRIENDLY -> {
                working = working
                    .replace(Regex("\\b(Respected Sir/Madam,|Dear Sir,)\\s*", RegexOption.IGNORE_CASE), "Hi! ")
                    .replace(Regex("\\bsend me the file\\b", RegexOption.IGNORE_CASE), "Hey, could you send me the file?")
                working = formatSentence(working).trimEnd('.') + "! 😊"
            }

            ToneCategory.CASUAL -> {
                working = working.replace(Regex("\\b(Respected Sir/Madam,|Dear Sir,)\\s*", RegexOption.IGNORE_CASE), "")
                working = formatSentence(working).trimEnd('.') + " 👍"
            }

            ToneCategory.HAPPY -> {
                working = formatSentence(working).trimEnd('.') + "! 🎉✨"
            }

            ToneCategory.EMPATHETIC -> {
                working = formatSentence(working)
                working = "I completely understand. $working Let me know how I can support you. ❤️"
            }

            ToneCategory.CONFIDENT -> {
                working = working
                    .replace(Regex("\\bI think maybe\\b", RegexOption.IGNORE_CASE), "I am certain that")
                    .replace(Regex("\\bI guess\\b", RegexOption.IGNORE_CASE), "I am confident that")
                    .replace(Regex("\\bmaybe we could\\b", RegexOption.IGNORE_CASE), "we should definitely")
                    .replace(Regex("\\bsorry to bother you,?\\s*", RegexOption.IGNORE_CASE), "")
                working = formatSentence(working)
            }

            ToneCategory.DIRECT -> {
                // Remove filler introductory phrases
                working = working
                    .replace(Regex("\\b(I think that|In my opinion|Just wanted to ask if|Could you please possibly)\\s*", RegexOption.IGNORE_CASE), "")
                working = formatSentence(working)
            }
        }

        working = working.replace(Regex("\\s{2,}"), " ").trim()

        return ToneTransformationResult(
            tone = tone,
            originalText = trimmed,
            transformedText = working,
            explanation = "Transformed to ${tone.displayName} tone (Local Heuristic Engine)"
        )
    }

    private fun formatSentence(s: String): String {
        if (s.isEmpty()) return s
        var res = s.replaceFirstChar { it.uppercase() }
        if (!res.endsWith(".") && !res.endsWith("!") && !res.endsWith("?") && !res.endsWith("।")) {
            res += "."
        }
        return res
    }
}
