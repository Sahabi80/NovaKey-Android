package com.novakey.keyboard.ai

import android.content.Context
import android.os.Build
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.genai.prompt.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.novakey.keyboard.ai.model.AiContextResult
import com.novakey.keyboard.ai.model.AiGrammarResult
import com.novakey.keyboard.ai.model.AiResult
import com.novakey.keyboard.ai.model.AiRewriteResult
import com.novakey.keyboard.ai.model.AiToneResult
import com.novakey.keyboard.ai.model.OnDeviceStatus
import com.novakey.keyboard.ai.model.ProviderSource
import com.novakey.keyboard.ai.model.RewriteMode
import com.novakey.keyboard.engine.tone.ToneCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * On-Device GenAI Provider using the official Google ML Kit GenAI Prompt API.
 * Uses the runtime checkStatus() API as the source of truth for device compatibility.
 * Configured in standard safe mode without experimental ModelPreference.FAST prefix caching.
 */
class OnDeviceAiProvider(
    private val context: Context? = null
) : AiEngineProvider {

    override val providerName: String = "Google Gemini Nano (ML Kit GenAI / AICore)"
    override val isLocal: Boolean = true

    private var generativeModel: GenerativeModel? = null

    @Synchronized
    private fun getClient(): GenerativeModel? {
        if (generativeModel == null) {
            try {
                // Uses standard safe configuration
                generativeModel = Generation.getClient()
            } catch (_: Throwable) {
                generativeModel = null
            }
        }
        return generativeModel
    }

    /**
     * Checks the actual model status via the official ML Kit GenAI checkStatus() API.
     * Evaluates whether Gemini Nano is AVAILABLE, DOWNLOADABLE, DOWNLOADING, or UNAVAILABLE.
     */
    suspend fun checkAvailability(): OnDeviceStatus = withContext(Dispatchers.IO) {
        // ML Kit GenAI Prompt API library requires minimum API 26 (Android 8.0)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return@withContext OnDeviceStatus.UNAVAILABLE
        }

        val client = getClient() ?: return@withContext OnDeviceStatus.UNAVAILABLE

        return@withContext try {
            val statusTask = client.checkStatus()
            val featureStatus = Tasks.await(statusTask, 2500, TimeUnit.MILLISECONDS)

            when (featureStatus) {
                FeatureStatus.AVAILABLE -> OnDeviceStatus.AVAILABLE
                FeatureStatus.DOWNLOADABLE -> OnDeviceStatus.DOWNLOADABLE
                FeatureStatus.DOWNLOADING -> OnDeviceStatus.DOWNLOADING
                else -> OnDeviceStatus.UNAVAILABLE
            }
        } catch (_: Throwable) {
            OnDeviceStatus.UNAVAILABLE
        }
    }

    suspend fun requestModelDownload(): Boolean = withContext(Dispatchers.IO) {
        val client = getClient() ?: return@withContext false
        return@withContext try {
            val downloadTask = client.download()
            Tasks.await(downloadTask, 5000, TimeUnit.MILLISECONDS)
            true
        } catch (_: Throwable) {
            false
        }
    }

    override suspend fun checkGrammar(text: String, context: AiContext): AiResult<AiGrammarResult> = withContext(Dispatchers.IO) {
        val client = getClient()
        if (client == null || checkAvailability() != OnDeviceStatus.AVAILABLE) {
            return@withContext AiResult.Error("Gemini Nano not available on this device.")
        }

        val prompt = "Proofread and correct grammar, spelling, and punctuation. Return only the corrected sentence:\n$text"

        return@withContext try {
            val response = client.generateContent(prompt).await()
            val raw = response.text?.trim() ?: text
            val cleaned = cleanKeyboardOutput(raw, 250)
            AiResult.Success(
                AiGrammarResult(
                    originalText = text,
                    correctedText = cleaned,
                    explanation = "Proofread on-device by Gemini Nano (Private & Offline)",
                    providerSource = ProviderSource.ON_DEVICE
                )
            )
        } catch (e: Throwable) {
            AiResult.Error("Gemini Nano inference failed: ${e.localizedMessage}")
        }
    }

    override suspend fun rewrite(text: String, mode: RewriteMode, context: AiContext): AiResult<AiRewriteResult> = withContext(Dispatchers.IO) {
        val client = getClient()
        if (client == null || checkAvailability() != OnDeviceStatus.AVAILABLE) {
            return@withContext AiResult.Error("Gemini Nano not available on this device.")
        }

        val prompt = when (mode) {
            RewriteMode.IMPROVE -> "Improve clarity, flow, and vocabulary while keeping meaning intact:\n$text"
            RewriteMode.NATURAL -> "Rewrite in a natural, conversational, fluent tone:\n$text"
            RewriteMode.SHORTER -> "Condense and make more concise, removing filler words:\n$text"
            RewriteMode.CLEARER -> "Simplify and clarify this sentence:\n$text"
            RewriteMode.PROFESSIONAL -> "Rewrite with an executive, professional tone:\n$text"
            RewriteMode.CONTINUE -> "Continue this thought with one concise, relevant sentence:\n$text"
        }

        return@withContext try {
            val response = client.generateContent(prompt).await()
            val raw = response.text?.trim() ?: text
            val cleaned = cleanKeyboardOutput(raw, 250)
            AiResult.Success(
                AiRewriteResult(
                    originalText = text,
                    rewrittenText = cleaned,
                    mode = mode,
                    providerSource = ProviderSource.ON_DEVICE
                )
            )
        } catch (e: Throwable) {
            AiResult.Error("Gemini Nano rewrite failed: ${e.localizedMessage}")
        }
    }

    override suspend fun changeTone(text: String, tone: ToneCategory, context: AiContext): AiResult<AiToneResult> = withContext(Dispatchers.IO) {
        val client = getClient()
        if (client == null || checkAvailability() != OnDeviceStatus.AVAILABLE) {
            return@withContext AiResult.Error("Gemini Nano not available on this device.")
        }

        val prompt = "Rewrite this sentence with a ${tone.displayName} tone. Return only the revised sentence:\n$text"

        return@withContext try {
            val response = client.generateContent(prompt).await()
            val raw = response.text?.trim() ?: text
            val cleaned = cleanKeyboardOutput(raw, 250)
            AiResult.Success(
                AiToneResult(
                    originalText = text,
                    transformedText = cleaned,
                    tone = tone,
                    explanation = "Tone modified on-device with Gemini Nano",
                    providerSource = ProviderSource.ON_DEVICE
                )
            )
        } catch (e: Throwable) {
            AiResult.Error("Gemini Nano tone transformation failed: ${e.localizedMessage}")
        }
    }

    override suspend fun processContextAction(text: String, actionType: String, context: AiContext): AiResult<AiContextResult> = withContext(Dispatchers.IO) {
        val client = getClient()
        if (client == null || checkAvailability() != OnDeviceStatus.AVAILABLE) {
            return@withContext AiResult.Error("Gemini Nano not available on this device.")
        }

        val prompt = when {
            actionType.equals("reply", ignoreCase = true) ->
                "Generate a short, suitable, and polite reply to this message. Do not make up personal facts:\n$text"
            actionType.equals("write", ignoreCase = true) ->
                "Draft a short, concise message or note based on this request (under 3 sentences):\n$text"
            actionType.equals("translate_bn", ignoreCase = true) || actionType.equals("translate", ignoreCase = true) ->
                "Translate this text into standard Bengali (Bangla script). Return only the translation:\n$text"
            actionType.equals("translate_en", ignoreCase = true) ->
                "Translate this Bengali text into natural English. Return only the translation:\n$text"
            actionType.equals("explain", ignoreCase = true) ->
                "Explain the meaning and grammatical structure of this sentence concisely in 1-2 lines:\n$text"
            actionType.equals("ask", ignoreCase = true) || text.endsWith("?") ->
                "Provide a direct, concise answer in 1-2 sentences for:\n$text"
            else ->
                "Perform action '$actionType' on:\n$text"
        }

        return@withContext try {
            val response = client.generateContent(prompt).await()
            val raw = response.text?.trim() ?: text
            val cleaned = cleanKeyboardOutput(raw, 300)
            AiResult.Success(
                AiContextResult(
                    query = text,
                    responseText = cleaned,
                    actionType = actionType,
                    explanation = "Generated on-device via Gemini Nano",
                    providerSource = ProviderSource.ON_DEVICE
                )
            )
        } catch (e: Throwable) {
            AiResult.Error("Gemini Nano action failed: ${e.localizedMessage}")
        }
    }

    override suspend fun customPrompt(text: String, userPrompt: String, context: AiContext): AiResult<AiContextResult> = withContext(Dispatchers.IO) {
        val client = getClient()
        if (client == null || checkAvailability() != OnDeviceStatus.AVAILABLE) {
            return@withContext AiResult.Error("Gemini Nano not available on this device.")
        }

        val prompt = "Instruction: $userPrompt\nInput text:\n$text\nProvide only the result (concise):"

        return@withContext try {
            val response = client.generateContent(prompt).await()
            val raw = response.text?.trim() ?: text
            val cleaned = cleanKeyboardOutput(raw, 300)
            AiResult.Success(
                AiContextResult(
                    query = userPrompt,
                    responseText = cleaned,
                    actionType = "custom",
                    explanation = "Custom instruction executed on-device by Gemini Nano",
                    providerSource = ProviderSource.ON_DEVICE
                )
            )
        } catch (e: Throwable) {
            AiResult.Error("Gemini Nano custom prompt failed: ${e.localizedMessage}")
        }
    }

    /**
     * Sanitizes and enforces keyboard-appropriate output limits.
     * Prevents walls of text from cluttering the IME view.
     */
    private fun cleanKeyboardOutput(rawText: String, maxChars: Int = 300): String {
        val trimmed = rawText.trim().removeSurrounding("\"")
        return if (trimmed.length > maxChars) {
            val truncated = trimmed.take(maxChars)
            val lastSentence = truncated.lastIndexOfAny(charArrayOf('.', '!', '?', '।', '\n'))
            if (lastSentence > 80) truncated.substring(0, lastSentence + 1).trim()
            else truncated.trim()
        } else {
            trimmed
        }
    }
}
