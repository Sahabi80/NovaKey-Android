package com.novakey.keyboard.ai

import com.novakey.keyboard.ai.model.AiContextResult
import com.novakey.keyboard.ai.model.AiGrammarResult
import com.novakey.keyboard.ai.model.AiResult
import com.novakey.keyboard.ai.model.AiRewriteResult
import com.novakey.keyboard.ai.model.AiToneResult
import com.novakey.keyboard.ai.model.ProviderSource
import com.novakey.keyboard.ai.model.RewriteMode
import com.novakey.keyboard.engine.tone.ToneCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Tier 2: Cloud AI Provider
 * Connects to the server proxy endpoints without embedding private API keys or secrets in the APK.
 * Falls back gracefully if network fails, timeout occurs, or quota is exhausted.
 */
class CloudAiProvider(
    private val baseEndpointUrl: String = "https://ais-dev-sv4cpezqtitx4em75gloat-744117460115.asia-southeast1.run.app/api/ai"
) : AiEngineProvider {

    override val providerName: String = "NovaKey Cloud Assistant"
    override val isLocal: Boolean = false

    override suspend fun checkGrammar(text: String, context: AiContext): AiResult<AiGrammarResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("text", text)
        }
        val response = makeRequest("$baseEndpointUrl/grammar", payload)
            ?: return@withContext AiResult.Error("Network request failed; local fallback active.")

        try {
            val json = JSONObject(response)
            if (json.has("error")) {
                return@withContext AiResult.Error(json.optString("error", "AI service error."))
            }
            val corrected = json.optString("correctedText", text)
            val explanation = json.optString("summary", "Proofread by Cloud AI.")
            AiResult.Success(
                AiGrammarResult(
                    originalText = text,
                    correctedText = corrected,
                    explanation = explanation,
                    providerSource = ProviderSource.CLOUD
                )
            )
        } catch (e: Exception) {
            AiResult.Error("Parsing error: ${e.localizedMessage}")
        }
    }

    override suspend fun rewrite(text: String, mode: RewriteMode, context: AiContext): AiResult<AiRewriteResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("text", text)
            put("mode", mode.name.lowercase())
        }
        val response = makeRequest("$baseEndpointUrl/rewrite", payload)
            ?: return@withContext AiResult.Error("Network error.")

        try {
            val json = JSONObject(response)
            if (json.has("error")) {
                return@withContext AiResult.Error(json.optString("error", "AI service busy."))
            }
            val rewritten = json.optString("rewrittenText", text)
            AiResult.Success(
                AiRewriteResult(
                    originalText = text,
                    rewrittenText = rewritten,
                    mode = mode,
                    providerSource = ProviderSource.CLOUD
                )
            )
        } catch (e: Exception) {
            AiResult.Error("Failed to parse response: ${e.localizedMessage}")
        }
    }

    override suspend fun changeTone(text: String, tone: ToneCategory, context: AiContext): AiResult<AiToneResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("text", text)
            put("tone", tone.displayName)
        }
        val response = makeRequest("$baseEndpointUrl/tone", payload)
            ?: return@withContext AiResult.Error("Network error.")

        try {
            val json = JSONObject(response)
            if (json.has("error")) {
                return@withContext AiResult.Error(json.optString("error", "Cloud tone unavailable."))
            }
            val transformed = json.optString("transformedText", text)
            val explanation = json.optString("toneExplanation", "Changed tone to ${tone.displayName}")
            AiResult.Success(
                AiToneResult(
                    originalText = text,
                    transformedText = transformed,
                    tone = tone,
                    explanation = explanation,
                    providerSource = ProviderSource.CLOUD
                )
            )
        } catch (e: Exception) {
            AiResult.Error("Parsing error: ${e.localizedMessage}")
        }
    }

    override suspend fun processContextAction(text: String, actionType: String, context: AiContext): AiResult<AiContextResult> = withContext(Dispatchers.IO) {
        val isTranslation = actionType.startsWith("translate", ignoreCase = true)
        val url = if (isTranslation) "$baseEndpointUrl/custom" else "$baseEndpointUrl/context"

        val payload = if (isTranslation) {
            val instruction = if (actionType.equals("translate_bn", ignoreCase = true) || actionType.equals("translate", ignoreCase = true)) {
                "Translate this text into standard Bengali (Bangla script). Return only the translation."
            } else {
                "Translate this Bengali text into natural English. Return only the translation."
            }
            JSONObject().apply {
                put("text", text)
                put("instruction", instruction)
            }
        } else {
            JSONObject().apply {
                put("text", text)
                put("appContext", actionType)
            }
        }

        val response = makeRequest(url, payload)
            ?: return@withContext AiResult.Error("Network error.")

        try {
            val json = JSONObject(response)
            if (json.has("error")) {
                return@withContext AiResult.Error(json.optString("error", "AI could not process instruction."))
            }
            val resultText = if (isTranslation) {
                json.optString("resultText", text)
            } else {
                json.optString("suggestedText", text)
            }
            val explanation = if (isTranslation) {
                "Translated by Cloud AI"
            } else {
                json.optString("explanation", "Cloud Context AI")
            }
            AiResult.Success(
                AiContextResult(
                    query = text,
                    responseText = resultText,
                    actionType = actionType,
                    explanation = explanation,
                    providerSource = ProviderSource.CLOUD
                )
            )
        } catch (e: Exception) {
            AiResult.Error("Parsing error: ${e.localizedMessage}")
        }
    }

    override suspend fun customPrompt(text: String, userPrompt: String, context: AiContext): AiResult<AiContextResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("text", text)
            put("instruction", userPrompt)
        }
        val response = makeRequest("$baseEndpointUrl/custom", payload)
            ?: return@withContext AiResult.Error("Network error.")

        try {
            val json = JSONObject(response)
            if (json.has("error")) {
                return@withContext AiResult.Error(json.optString("error", "Custom instruction failed."))
            }
            val resultText = json.optString("resultText", text)
            val summary = json.optString("summary", "Followed instruction: $userPrompt")
            AiResult.Success(
                AiContextResult(
                    query = userPrompt,
                    responseText = resultText,
                    actionType = "custom",
                    explanation = summary,
                    providerSource = ProviderSource.CLOUD
                )
            )
        } catch (e: Exception) {
            AiResult.Error("Parsing error: ${e.localizedMessage}")
        }
    }

    private fun makeRequest(urlStr: String, jsonPayload: JSONObject): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 4000
                readTimeout = 6000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonPayload.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            if (statusCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
