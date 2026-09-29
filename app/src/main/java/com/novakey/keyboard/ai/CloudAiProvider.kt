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
 * Connects to a proxy endpoint without embedding private API keys or secrets in the APK.
 * Falls back gracefully if network fails or quota is exhausted.
 */
class CloudAiProvider(
    private val endpointUrl: String = "https://ais-dev-sv4cpezqtitx4em75gloat-744117460115.asia-southeast1.run.app/api/ai"
) : AiEngineProvider {

    override val providerName: String = "NovaKey Cloud Assistant"
    override val isLocal: Boolean = false

    override suspend fun checkGrammar(text: String, context: AiContext): AiResult<AiGrammarResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("action", "grammar")
            put("text", text)
        }
        val response = makeRequest(payload) ?: return@withContext AiResult.Error("Network request failed; local fallback active.")

        try {
            val json = JSONObject(response)
            if (json.optBoolean("success", false)) {
                val corrected = json.optString("result", text)
                val explanation = json.optString("explanation", "Corrected by Cloud AI.")
                AiResult.Success(AiGrammarResult(text, corrected, explanation, providerSource = ProviderSource.CLOUD))
            } else {
                AiResult.Error(json.optString("error", "AI service quota exhausted."))
            }
        } catch (e: Exception) {
            AiResult.Error("Parsing error: ${e.localizedMessage}")
        }
    }

    override suspend fun rewrite(text: String, mode: RewriteMode, context: AiContext): AiResult<AiRewriteResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("action", "rewrite")
            put("text", text)
            put("mode", mode.name.lowercase())
        }
        val response = makeRequest(payload) ?: return@withContext AiResult.Error("Network error.")

        try {
            val json = JSONObject(response)
            if (json.optBoolean("success", false)) {
                val rewritten = json.optString("result", text)
                AiResult.Success(AiRewriteResult(text, rewritten, mode, providerSource = ProviderSource.CLOUD))
            } else {
                AiResult.Error(json.optString("error", "AI service busy."))
            }
        } catch (e: Exception) {
            AiResult.Error("Failed to parse response: ${e.localizedMessage}")
        }
    }

    override suspend fun changeTone(text: String, tone: ToneCategory, context: AiContext): AiResult<AiToneResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("action", "tone")
            put("text", text)
            put("tone", tone.name.lowercase())
        }
        val response = makeRequest(payload) ?: return@withContext AiResult.Error("Network error.")

        try {
            val json = JSONObject(response)
            if (json.optBoolean("success", false)) {
                val transformed = json.optString("result", text)
                val explanation = json.optString("explanation", "Changed tone to ${tone.displayName}")
                AiResult.Success(AiToneResult(text, transformed, tone, explanation, providerSource = ProviderSource.CLOUD))
            } else {
                AiResult.Error(json.optString("error", "Cloud tone unavailable."))
            }
        } catch (e: Exception) {
            AiResult.Error("Parsing error: ${e.localizedMessage}")
        }
    }

    override suspend fun processContextAction(text: String, actionType: String, context: AiContext): AiResult<AiContextResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("action", "context")
            put("text", text)
            put("instruction", actionType)
        }
        val response = makeRequest(payload) ?: return@withContext AiResult.Error("Network error.")

        try {
            val json = JSONObject(response)
            if (json.optBoolean("success", false)) {
                val resultText = json.optString("result", text)
                AiResult.Success(AiContextResult(text, resultText, actionType, "Cloud Context AI", providerSource = ProviderSource.CLOUD))
            } else {
                AiResult.Error(json.optString("error", "AI could not process instruction."))
            }
        } catch (e: Exception) {
            AiResult.Error("Parsing error: ${e.localizedMessage}")
        }
    }

    override suspend fun customPrompt(text: String, userPrompt: String, context: AiContext): AiResult<AiContextResult> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("action", "custom_prompt")
            put("text", text)
            put("prompt", userPrompt)
        }
        val response = makeRequest(payload) ?: return@withContext AiResult.Error("Network error.")

        try {
            val json = JSONObject(response)
            if (json.optBoolean("success", false)) {
                val resultText = json.optString("result", text)
                AiResult.Success(AiContextResult(userPrompt, resultText, "custom", "Followed: $userPrompt", providerSource = ProviderSource.CLOUD))
            } else {
                AiResult.Error(json.optString("error", "Custom instruction failed."))
            }
        } catch (e: Exception) {
            AiResult.Error("Parsing error: ${e.localizedMessage}")
        }
    }

    private fun makeRequest(jsonPayload: JSONObject): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(endpointUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 3000
                readTimeout = 5000
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
