package com.novakey.keyboard.engine.grammar

/**
 * Local rule-based heuristic grammar, typo, and punctuation correction engine.
 * Runs instantly in 0ms on-device without cloud or network requirements.
 */
object HeuristicGrammar {

    private val COMMON_TYPOS = mapOf(
        "teh" to "the",
        "dont" to "don't",
        "cant" to "can't",
        "wont" to "won't",
        "im" to "I'm",
        "ive" to "I've",
        "id" to "I'd",
        "youre" to "youre",
        "theyre" to "they're",
        "weve" to "we've",
        "isnt" to "isn't",
        "arent" to "aren't",
        "wasnt" to "wasn't",
        "werent" to "weren't",
        "hasnt" to "hasn't",
        "havent" to "haven't",
        "definately" to "definitely",
        "recieve" to "receive",
        "seperate" to "separate",
        "occured" to "occurred",
        "untill" to "until",
        "alot" to "a lot",
        "wich" to "which",
        "thier" to "their",
        "waht" to "what",
        "taht" to "that",
        "thsi" to "this",
        "nad" to "and",
        "adn" to "and",
        "wiht" to "with",
        "becuase" to "because",
        "beacuse" to "because"
    )

    /**
     * Checks if a typed word has an instantaneous auto-correction candidate.
     */
    fun getSpellingCorrection(word: String): String? {
        if (word.isBlank()) return null
        val lower = word.trim().lowercase()
        val corrected = COMMON_TYPOS[lower] ?: return null

        // Preserve initial casing if typed with a capital letter
        return if (word.first().isUpperCase()) {
            corrected.replaceFirstChar { it.uppercase() }
        } else {
            corrected
        }
    }

    /**
     * Determines whether the next character typed should be auto-capitalized.
     */
    fun shouldAutoCapitalize(textBeforeCursor: String): Boolean {
        if (textBeforeCursor.isBlank()) return true
        if (Regex("(\\r?\\n)\\s*$").containsMatchIn(textBeforeCursor)) return true
        if (Regex("[.!?]\\s+$").containsMatchIn(textBeforeCursor)) return true
        return false
    }

    /**
     * Performs comprehensive local rule-based grammar and stylistic correction.
     */
    fun correctSentence(text: String): GrammarResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return GrammarResult(text, text, emptyList())
        }

        var working = trimmed
        val issues = mutableListOf<GrammarIssue>()

        // 1. Subject-Verb Agreement: (he|she|it) don't -> doesn't
        val srvRegex = Regex("\\b(she|he|it)\\s+(?:dont|don't)\\b", RegexOption.IGNORE_CASE)
        working = srvRegex.replace(working) { m ->
            val orig = m.value
            val subj = m.groupValues[1]
            val repl = "$subj doesn't"
            issues.add(GrammarIssue(orig, repl, "Subject-Verb Agreement", "Third-person singular subjects take 'doesn't'."))
            repl
        }

        // 2. Plural Subject-Verb Agreement: (they|we) is -> are
        val pluralRegex = Regex("\\b(they|we)\\s+is\\b", RegexOption.IGNORE_CASE)
        working = pluralRegex.replace(working) { m ->
            val orig = m.value
            val subj = m.groupValues[1]
            val repl = "$subj are"
            issues.add(GrammarIssue(orig, repl, "Plural Agreement", "Plural pronouns require 'are', not 'is'."))
            repl
        }

        // 3. You was -> You were
        val youWasRegex = Regex("\\byou\\s+was\\b", RegexOption.IGNORE_CASE)
        working = youWasRegex.replace(working) { m ->
            issues.add(GrammarIssue(m.value, "you were", "Second Person Agreement", "'You' takes 'were' in past tense."))
            "you were"
        }

        // 4. Double Negative: don't know nothing -> don't know anything
        val doubleNeg = Regex("\\b(don't|doesn't|didn't)\\s+(know|have|see|want)\\s+nothing\\b", RegexOption.IGNORE_CASE)
        working = doubleNeg.replace(working) { m ->
            val repl = "${m.groupValues[1]} ${m.groupValues[2]} anything"
            issues.add(GrammarIssue(m.value, repl, "Double Negative", "Use 'anything' instead of 'nothing' with negative verbs."))
            repl
        }

        // 5. Common Typos
        for ((wrong, right) in COMMON_TYPOS) {
            val typoRegex = Regex("\\b$wrong\\b", RegexOption.IGNORE_CASE)
            if (typoRegex.containsMatchIn(working)) {
                working = typoRegex.replace(working) { m ->
                    val replacement = if (m.value.first().isUpperCase()) right.replaceFirstChar { it.uppercase() } else right
                    issues.add(GrammarIssue(m.value, replacement, "Spelling Correction", "Corrected spelling of '$wrong'."))
                    replacement
                }
            }
        }

        // 6. Capitalize first letter of sentence
        if (working.isNotEmpty() && working.first().isLowerCase()) {
            val capitalized = working.replaceFirstChar { it.uppercase() }
            issues.add(GrammarIssue(working.substring(0, 1), capitalized.substring(0, 1), "Sentence Capitalization", "Sentences should begin with a capital letter."))
            working = capitalized
        }

        // 7. Ensure end-of-sentence punctuation if length > 8
        if (working.length > 8 && !working.endsWith(".") && !working.endsWith("!") && !working.endsWith("?") && !working.endsWith("।")) {
            working += "."
            issues.add(GrammarIssue("", ".", "End Punctuation", "Added period at end of sentence."))
        }

        return GrammarResult(trimmed, working, issues)
    }
}
