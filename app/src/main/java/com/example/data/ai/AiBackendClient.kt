package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiBackendClient(
    private var customBackendUrlProvider: () -> String = { "" },
    private var devApiKeyProvider: () -> String = { "" }
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Resolves active API key safely without hardcoding
    val effectiveApiKey: String
        get() {
            val devKey = devApiKeyProvider().trim()
            if (devKey.isNotBlank()) return devKey
            return try {
                val configKey = BuildConfig.GEMINI_API_KEY
                if (configKey.isNullOrBlank() || configKey == "MY_GEMINI_API_KEY") "" else configKey
            } catch (e: Throwable) {
                ""
            }
        }

    val customBackendUrl: String
        get() = customBackendUrlProvider().trim()

    val isConfigured: Boolean
        get() = customBackendUrl.isNotBlank() || effectiveApiKey.isNotBlank()

    suspend fun executeGenerateContent(
        prompt: String,
        systemInstruction: String? = null,
        forceJson: Boolean = false,
        maxRetries: Int = 1
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("AI Service unconfigured: Set Gemini API key or Backend URL."))
        }

        var attempt = 0
        var lastException: Exception? = null

        while (attempt <= maxRetries) {
            try {
                val responseText = if (customBackendUrl.isNotBlank()) {
                    callCustomBackend(prompt, systemInstruction, forceJson)
                } else {
                    callDirectGemini(prompt, systemInstruction, forceJson)
                }
                if (responseText.isNotBlank()) {
                    return@withContext Result.success(responseText)
                } else {
                    throw IOException("Empty response from AI engine")
                }
            } catch (e: Exception) {
                lastException = e
                attempt++
                if (attempt <= maxRetries) {
                    delay(1000L * attempt)
                }
            }
        }

        Result.failure(lastException ?: IOException("Failed to generate content after retries"))
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val result = executeGenerateContent(
                prompt = "Respond with 'PONG' only.",
                maxRetries = 0
            )
            result.isSuccess && result.getOrNull()?.isNotBlank() == true
        } catch (e: Throwable) {
            false
        }
    }

    private fun callDirectGemini(
        prompt: String,
        systemInstruction: String?,
        forceJson: Boolean
    ): String {
        val apiKey = effectiveApiKey
        if (apiKey.isBlank()) throw IllegalStateException("Gemini API key is missing.")

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)

            if (!systemInstruction.isNullOrBlank()) {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    } )
                })
            }

            if (forceJson) {
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                })
            }
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errCode = response.code
                val errBody = response.body?.string() ?: ""
                throw IOException("Gemini API error ($errCode): $errBody")
            }
            val bodyString = response.body?.string() ?: throw IOException("Null response body")
            return extractTextFromGeminiResponse(bodyString)
        }
    }

    private fun callCustomBackend(
        prompt: String,
        systemInstruction: String?,
        forceJson: Boolean
    ): String {
        val endpoint = customBackendUrl
        val payload = JSONObject().apply {
            put("prompt", prompt)
            if (!systemInstruction.isNullOrBlank()) {
                put("systemInstruction", systemInstruction)
            }
            put("forceJson", forceJson)
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Custom backend error (${response.code})")
            }
            val bodyString = response.body?.string() ?: throw IOException("Null response body")
            // Backend can return either standard Gemini payload or direct { "text": "..." }
            return if (bodyString.trim().startsWith("{")) {
                val json = JSONObject(bodyString)
                if (json.has("text")) {
                    json.getString("text")
                } else if (json.has("candidates")) {
                    extractTextFromGeminiResponse(bodyString)
                } else {
                    bodyString
                }
            } else {
                bodyString
            }
        }
    }

    private fun extractTextFromGeminiResponse(rawJson: String): String {
        val json = JSONObject(rawJson)
        val candidates = json.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        if (parts.length() == 0) return ""
        return parts.getJSONObject(0).optString("text", "")
    }
}
