package com.onie.assistant.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ONIEOnlineBrain(
    private val apiKey: String
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun respond(input: String): String = withContext(Dispatchers.IO) {

        Log.e("ONIE_TRACE", "ONLINE: Sending request")

        val requestJson = JSONObject().apply {
            put("model", "gemini-3.7-flash")
            put("input", input)
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/interactions")
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(
                requestJson.toString()
                    .toRequestBody("application/json".toMediaType())
            )
            .build()

        try {
            client.newCall(request).execute().use { response ->

                Log.e(
                    "ONIE_TRACE",
                    "ONLINE: HTTP ${response.code}"
                )

                val body = response.body?.string().orEmpty()

                Log.e(
                    "ONIE_TRACE",
                    "ONLINE: Response length=${body.length}"
                )

                if (!response.isSuccessful) {
                    throw Exception(
                        "Gemini HTTP ${response.code}: $body"
                    )
                }

                val json = JSONObject(body)

                extractOutputText(json)
            }
        } catch (e: Throwable) {
            Log.e(
                "ONIE_TRACE",
                "ONLINE REQUEST FAILED: ${e.javaClass.simpleName}: ${e.message}",
                e
            )

            throw e
        }
    }

    private fun extractOutputText(json: JSONObject): String {

        val outputs = json.optJSONArray("outputs")
            ?: throw Exception(
                "ONIE received no output from the AI."
            )

        for (i in 0 until outputs.length()) {

            val output = outputs.getJSONObject(i)

            if (output.optString("type") == "text") {

                val text = output.optString("text")

                if (text.isNotBlank()) {
                    return text
                }
            }
        }

        throw Exception(
            "ONIE received an empty AI response."
        )
    }
}
