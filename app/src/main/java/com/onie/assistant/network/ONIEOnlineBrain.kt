package com.onie.assistant.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ONIEOnlineBrain(
    private val apiKey: String
) {

    private val client = OkHttpClient()

    suspend fun respond(input: String): String = withContext(Dispatchers.IO) {

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

        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw Exception(
                    "ONIE online brain failed: HTTP ${response.code}"
                )
            }

            val body = response.body.string()
            val json = JSONObject(body)

            extractOutputText(json)
        }
    }

    private fun extractOutputText(json: JSONObject): String {

        // The API exposes output items in the interaction response.
        val outputs = json.optJSONArray("outputs")
            ?: throw Exception("ONIE received no output from the AI.")

        for (i in 0 until outputs.length()) {

            val output = outputs.getJSONObject(i)

            if (output.optString("type") == "text") {
                val text = output.optString("text")

                if (text.isNotBlank()) {
                    return text
                }
            }
        }

        throw Exception("ONIE received an empty AI response.")
    }
}
