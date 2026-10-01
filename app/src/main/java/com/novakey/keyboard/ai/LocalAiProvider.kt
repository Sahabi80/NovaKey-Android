package com.novakey.keyboard.ai

import com.novakey.keyboard.ai.model.AiContextResult
import com.novakey.keyboard.ai.model.AiGrammarResult
import com.novakey.keyboard.ai.model.AiResult
import com.novakey.keyboard.ai.model.AiRewriteResult
import com.novakey.keyboard.ai.model.AiToneResult
import com.novakey.keyboard.ai.model.ProviderSource
import com.novakey.keyboard.ai.model.RewriteMode
import com.novakey.keyboard.engine.banglish.BanglishConverter
import com.novakey.keyboard.engine.grammar.HeuristicGrammar
import com.novakey.keyboard.engine.tone.LocalToneEngine
import com.novakey.keyboard.engine.tone.ToneCategory

/**
 * Tier 1: 100% Offline Local AI Engine
 * 0ms latency, zero API keys, zero network, zero cost.
 */
class LocalAiProvider : AiEngineProvider {
    override val providerName: String = "NovaKey Local Engine"
    override val isLocal: Boolean = true

    override suspend fun checkGrammar(text: String, context: AiContext): AiResult<AiGrammarResult> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return AiResult.Error("No text found to check.")
        }

        val result = HeuristicGrammar.correctSentence(trimmed)
        val explanation = if (result.issues.isNotEmpty()) {
            result.issues.joinToString("; ") { "${it.ruleTitle}: ${it.original} → ${it.replacement}" }
        } else {
            "Sentence is grammatically sound."
        }

        return AiResult.Success(
            AiGrammarResult(
                originalText = trimmed,
                correctedText = result.correctedText,
                explanation = explanation,
                providerSource = ProviderSource.LOCAL
            )
        )
    }

    override suspend fun rewrite(text: String, mode: RewriteMode, context: AiContext): AiResult<AiRewriteResult> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return AiResult.Error("No text provided to rewrite.")
        }

        var rewritten = trimmed

        when (mode) {
            RewriteMode.PROFESSIONAL -> {
                val toneRes = LocalToneEngine.transform(trimmed, ToneCategory.PROFESSIONAL)
                rewritten = toneRes.transformedText
            }
            RewriteMode.SHORTER -> {
                val fillers = listOf(
                    Regex("\\bactually\\b,?\\s*", RegexOption.IGNORE_CASE),
                    Regex("\\bbasically\\b,?\\s*", RegexOption.IGNORE_CASE),
                    Regex("\\bliterally\\b,?\\s*", RegexOption.IGNORE_CASE),
                    Regex("\\bvery\\b\\s*", RegexOption.IGNORE_CASE),
                    Regex("\\breally\\b\\s*", RegexOption.IGNORE_CASE),
                    Regex("\\bin order to\\b", RegexOption.IGNORE_CASE),
                    Regex("\\bdue to the fact that\\b", RegexOption.IGNORE_CASE)
                )
                for (f in fillers) {
                    rewritten = f.replace(rewritten, "")
                }
                rewritten = rewritten.replace(Regex("\\s{2,}"), " ").trim()
            }
            RewriteMode.CLEARER -> {
                val grammarRes = HeuristicGrammar.correctSentence(trimmed)
                rewritten = grammarRes.correctedText
            }
            RewriteMode.NATURAL -> {
                val toneRes = LocalToneEngine.transform(trimmed, ToneCategory.CASUAL)
                rewritten = toneRes.transformedText
            }
            RewriteMode.IMPROVE -> {
                val grammarRes = HeuristicGrammar.correctSentence(trimmed)
                val toneRes = LocalToneEngine.transform(grammarRes.correctedText, ToneCategory.POLITE)
                rewritten = toneRes.transformedText
            }
            RewriteMode.CONTINUE -> {
                rewritten = when {
                    trimmed.endsWith("?") -> "$trimmed I would appreciate your thoughts on this."
                    trimmed.contains("report", ignoreCase = true) -> "$trimmed I will share the final document shortly."
                    trimmed.contains("meeting", ignoreCase = true) || trimmed.contains("call", ignoreCase = true) -> "$trimmed Please let me know if this schedule works for you."
                    else -> "$trimmed Looking forward to hearing from you."
                }
            }
        }

        return AiResult.Success(
            AiRewriteResult(
                originalText = trimmed,
                rewrittenText = rewritten,
                mode = mode,
                providerSource = ProviderSource.LOCAL
            )
        )
    }

    override suspend fun changeTone(text: String, tone: ToneCategory, context: AiContext): AiResult<AiToneResult> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return AiResult.Error("No text provided for tone change.")
        }

        val hasBengaliChar = trimmed.any { it in '\u0980'..'\u09FF' }
        val isBanglish = !hasBengaliChar && isLikelyBanglish(trimmed)

        val transformed = when {
            hasBengaliChar -> {
                // Preserve Bangla script
                when (tone) {
                    ToneCategory.FRIENDLY -> {
                        if (trimmed.contains("রিপ্লাই দাওনি কেন") || trimmed.contains("উত্তর দাওনি কেন")) {
                            "Hey, আমার মেসেজটা দেখেছিস? 😄 সময় পেলে একটা reply দিস।"
                        } else {
                            "Hey! $trimmed 😊"
                        }
                    }
                    ToneCategory.RESPECTFUL -> {
                        "অনুগ্রহপূর্বক বিষয়টি বিবেচনা করবেন: $trimmed। আপনার সহযোগিতার জন্য ধন্যবাদ।"
                    }
                    ToneCategory.PROFESSIONAL -> {
                        "বিনীত নিবেদন এই যে, $trimmed। আপনার মতামত প্রত্যাশা করছি।"
                    }
                    ToneCategory.POLITE -> {
                        "দয়া করে $trimmed"
                    }
                    else -> trimmed
                }
            }
            isBanglish -> {
                // Strictly preserve Banglish (never convert to Bangla script or English)
                when (tone) {
                    ToneCategory.FRIENDLY -> {
                        if (trimmed.lowercase().contains("reply dao nai") || trimmed.lowercase().contains("reply daw nai")) {
                            "Hey, amar message ta dekhechis? 😄 Shomoy pele ekta reply dis."
                        } else {
                            "Hey, $trimmed! 😄"
                        }
                    }
                    ToneCategory.RESPECTFUL -> {
                        "Apnar subidhamoto shomoy ektu janaben: $trimmed. Dhonnobad."
                    }
                    ToneCategory.PROFESSIONAL -> {
                        "Kindly janaben regarding: $trimmed. Thank you."
                    }
                    ToneCategory.POLITE -> {
                        "Please $trimmed"
                    }
                    else -> "$trimmed 👍"
                }
            }
            else -> {
                // English input
                LocalToneEngine.transform(trimmed, tone).transformedText
            }
        }

        return AiResult.Success(
            AiToneResult(
                originalText = trimmed,
                transformedText = transformed,
                tone = tone,
                explanation = "Tone adapted to ${tone.displayName} (Language preserved)",
                providerSource = ProviderSource.LOCAL
            )
        )
    }

    private fun isLikelyBanglish(text: String): Boolean {
        val banglishTokens = setOf(
            "ami", "tumi", "tui", "apni", "amar", "tomar", "apnar", "amra", "tomra",
            "apnara", "she", "tini", "ajke", "kalke", "ekhon", "kokhon", "kemon", "acho",
            "achi", "achen", "chilo", "hobe", "hocche", "korchi", "korbo", "koro", "koren",
            "jabo", "ashbo", "ashbe", "bhalo", "valo", "kharap", "keno", "ki", "kintu",
            "onek", "shob", "kothay", "kivabe", "dorkar", "lagbe", "dekhecho", "dekhechis",
            "reply", "dis", "dao", "nai", "gechi", "jani", "shomoy"
        )
        val words = text.lowercase().split(Regex("[^a-zA-Z]+")).filter { it.isNotBlank() }
        return words.any { it in banglishTokens }
    }

    override suspend fun processContextAction(text: String, actionType: String, context: AiContext): AiResult<AiContextResult> {
        val trimmed = text.trim()
        val lower = trimmed.lowercase()

        val response = when {
            // 1. Reply Action
            actionType.equals("reply", ignoreCase = true) -> {
                when {
                    lower.contains("send me the report") || lower.contains("send the file") ->
                        "Sure, I'll send you the report tomorrow."
                    lower.contains("coming to class") ->
                        "Yes, I will be attending class tomorrow."
                    lower.contains("are you free") || lower.contains("available") ->
                        "Yes, that time works for me."
                    lower.endsWith("?") ->
                        "Yes, sounds good. I will follow up with you on this."
                    else ->
                        "Thank you for the message. I have received this and will get back to you shortly."
                }
            }

            // 2. Write / Draft Action
            actionType.equals("write", ignoreCase = true) -> {
                when {
                    lower.contains("deadline") || lower.contains("extension") ->
                        "Dear Teacher,\n\nI am writing to respectfully request an extension on the assignment deadline due to unforeseen circumstances. Thank you for your consideration.\n\nSincerely,\n[Your Name]"
                    lower.contains("sick") || lower.contains("leave") ->
                        "Dear Sir/Madam,\n\nI am unable to attend today due to illness. I will make sure to catch up on all pending tasks. Thank you for understanding.\n\nBest regards,"
                    lower.contains("email") ->
                        "Hi,\n\nI hope you are doing well. I wanted to follow up regarding our previous discussion. Please let me know your thoughts.\n\nBest,"
                    else ->
                        "Dear Sir/Madam,\n\nI am writing to follow up on this matter: $trimmed. Looking forward to your guidance.\n\nSincerely,"
                }
            }

            // 3. Translation Action (English <-> Bangla / Banglish)
            actionType.equals("translate_bn", ignoreCase = true) || actionType.equals("translate", ignoreCase = true) -> {
                when {
                    lower.contains("are you coming tomorrow") || lower.contains("coming tomorrow") ->
                        "তুমি কি কাল আসবে?"
                    lower.contains("why didn't you reply to my message") || lower.contains("why didn't you reply") ->
                        "তুমি আমার মেসেজের উত্তর দাওনি কেন?"
                    lower.contains("how are you") ->
                        "আপনি কেমন আছেন?"
                    lower.contains("thank you") ->
                        "আপনাকে অনেক ধন্যবাদ।"
                    lower.contains("learning computer science") ->
                        "আমি কম্পিউটার সায়েন্স শিখছি।"
                    lower.contains("go to university today") || lower.contains("go to university tomorrow") ->
                        "আমি আজ বিশ্ববিদ্যালয়ে যাব।"
                    lower.contains("good morning") ->
                        "শুভ সকাল।"
                    lower.contains("good night") ->
                        "শুভ রাত্রি।"
                    else ->
                        "অফলাইন অনুবাদক সাধারণ বাক্যাংশে সীমিত। সম্পূর্ণ এআই অনুবাদের জন্য ইন্টারনেটে সংযুক্ত হোন।"
                }
            }

            actionType.equals("translate_en", ignoreCase = true) -> {
                when {
                    trimmed.contains("কেমন আছেন") || trimmed.contains("কেমন আছো") || lower.contains("kemon acho") || lower.contains("kemon achen") ->
                        "How are you?"
                    trimmed.contains("ধন্যবাদ") || lower.contains("dhonnobad") ->
                        "Thank you very much."
                    lower.contains("office e jabo") || lower.contains("office jabo") || trimmed.contains("অফিসে যাব") ->
                        "I will go to the office today."
                    lower.contains("reply dao nai keno") || lower.contains("reply daw nai keno") || trimmed.contains("মেসেজের রিপ্লাই দাওনি কেন") ->
                        "Why didn't you reply to my message?"
                    lower.contains("kal ashbe") || lower.contains("kalke ashba") || trimmed.contains("কাল আসবে") ->
                        "Are you coming tomorrow?"
                    trimmed.contains("বিশ্ববিদ্যালয়") || lower.contains("university") ->
                        "I am going to university today."
                    trimmed.contains("শুভ সকাল") || lower.contains("shuvo shokal") ->
                        "Good morning."
                    trimmed.contains("শুভ রাত্রি") || lower.contains("shuvo ratri") ->
                        "Good night."
                    else ->
                        "Offline translator has limited phrase coverage. Connect to network for full AI translation."
                }
            }

            // 4. Explain Action
            actionType.equals("explain", ignoreCase = true) -> {
                val grammar = HeuristicGrammar.correctSentence(trimmed)
                if (grammar.issues.isNotEmpty()) {
                    "Analysis: ${grammar.issues.joinToString("\n• ") { "${it.ruleTitle}: ${it.explanation}" }}"
                } else if (lower.contains("polymorphism")) {
                    "Polymorphism is an object-oriented programming concept where a single interface can represent different underlying forms (data types or classes)."
                } else {
                    "The sentence is clear and grammatically sound with standard subject-verb structure."
                }
            }

            // 5. Ask AI / Answer
            actionType.equals("ask", ignoreCase = true) || actionType.equals("answer", ignoreCase = true) || trimmed.endsWith("?") -> {
                when {
                    lower.contains("polymorphism") ->
                        "Polymorphism allows objects to take multiple forms—commonly through method overriding (runtime) and method overloading (compile-time)."
                    lower.contains("java") ->
                        "Java is a high-level, class-based, object-oriented programming language designed for portability ('Write Once, Run Anywhere')."
                    else ->
                        "Regarding '$trimmed': Key principles emphasize clarity, concise phrasing, and practical application."
                }
            }

            else -> "Processed locally: $trimmed"
        }

        return AiResult.Success(
            AiContextResult(
                query = trimmed,
                responseText = response,
                actionType = actionType,
                explanation = "Generated offline by Local Engine (0ms, Private)",
                providerSource = ProviderSource.LOCAL
            )
        )
    }

    override suspend fun customPrompt(text: String, userPrompt: String, context: AiContext): AiResult<AiContextResult> {
        val promptLower = userPrompt.lowercase()
        val textTrimmed = text.trim()

        val transformed = when {
            promptLower.contains("shorter") || promptLower.contains("brief") -> {
                rewrite(textTrimmed, RewriteMode.SHORTER, context).let {
                    if (it is AiResult.Success) it.data.rewrittenText else textTrimmed
                }
            }
            promptLower.contains("professional") || promptLower.contains("formal") -> {
                changeTone(textTrimmed, ToneCategory.PROFESSIONAL, context).let {
                    if (it is AiResult.Success) it.data.transformedText else textTrimmed
                }
            }
            promptLower.contains("polite") || promptLower.contains("respectful") -> {
                changeTone(textTrimmed, ToneCategory.RESPECTFUL, context).let {
                    if (it is AiResult.Success) it.data.transformedText else textTrimmed
                }
            }
            promptLower.contains("friendly") -> {
                changeTone(textTrimmed, ToneCategory.FRIENDLY, context).let {
                    if (it is AiResult.Success) it.data.transformedText else textTrimmed
                }
            }
            promptLower.contains("beginner") || promptLower.contains("easier") || promptLower.contains("student") -> {
                "In simple terms: $textTrimmed. It helps us understand things step by step."
            }
            else -> {
                val fixed = HeuristicGrammar.correctSentence(textTrimmed).correctedText
                "$fixed"
            }
        }

        return AiResult.Success(
            AiContextResult(
                query = userPrompt,
                responseText = transformed,
                actionType = "custom",
                explanation = "Executed by Local Engine for instruction: '$userPrompt'",
                providerSource = ProviderSource.LOCAL
            )
        )
    }
}
