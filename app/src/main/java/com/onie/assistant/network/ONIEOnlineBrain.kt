package com.onie.assistant.network

import android.util.Log
import com.onie.assistant.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ONIEOnlineBrain {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun respond(input: String): String =
        withContext(Dispatchers.IO) {

            val apiKey = BuildConfig.GEMINI_API_KEY

            if (apiKey.isBlank()) {
                throw Exception(
                    "ONIE AI API key is not configured."
                )
            }

            Log.e(
                "ONIE_TRACE",
                "REASONER: Sending request to Gemini 3.6 Flash"
            )

            val requestJson = JSONObject().apply {

                put(
                    "systemInstruction",
                    JSONObject().apply {
                        put(
                            "parts",
                            org.json.JSONArray().apply {
                                put(
                                    JSONObject().apply {
                                        put(
                                            "text",
                                            """
                                            You are ONIE, an intelligent Android personal assistant.

                                            Your job is to understand the user's actual intention,
                                            reason through problems, and provide useful answers.

                                            You are not merely a search engine.

                                            When answering:
                                            - Understand context.
                                            - Reason through multi-step problems.
                                            - Ask for clarification when essential information is missing.
                                            - Do not invent facts.
                                            - Be concise when the request is simple.
                                            - Give deeper explanations when the problem requires them.
                                            - Distinguish facts from assumptions.
                                            - Never claim that you performed an action unless it actually happened.

                                            ONIE is eventually capable of using external tools.
                                            When tools become available, determine which tool is
                                            appropriate before attempting an action.

                                            For consequential actions such as sending messages,
                                            making purchases, transferring money, deleting data,
                                            or publishing content, require explicit confirmation
                                            before execution.
                                            """.trimIndent()
                                        )
                                    }
                                )
                            }
                        )
                    }
                )

                put(
                    "contents",
                    org.json.JSONArray().apply {
                        put(
                            JSONObject().apply {

                                put(
                                    "role",
                                    "user"
                                )

                                put(
                                    "parts",
                                    org.json.JSONArray().apply {
                                        put(
                                            JSONObject().apply {
                                                put(
                                                    "text",
                                                    input
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )

                put(
                    "generationConfig",
                    JSONObject().apply {

                        put(
                            "thinkingConfig",
                            JSONObject().apply {
                                put(
                                    "thinkingLevel",
                                    "HIGH"
                                )
                            }
                        )
                    }
                )
            }

            val request =
                Request.Builder()
                    .url(
                        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"
                    )
                    .addHeader(
                        "x-goog-api-key",
                        apiKey
                    )
                    .addHeader(
                        "Content-Type",
                        "application/json"
                    )
                    .post(
                        requestJson
                            .toString()
                            .toRequestBody(
                                "application/json".toMediaType()
                            )
                    )
                    .build()

            try {

                client.newCall(request).execute().use { response ->

                    val body =
                        response.body?.string().orEmpty()

                    Log.e(
                        "ONIE_TRACE",
                        "REASONER: HTTP ${response.code}"
                    )

                    Log.e(
                        "ONIE_TRACE",
                        "REASONER: Response length=${body.length}"
                    )

                    if (!response.isSuccessful) {

                        Log.e(
                            "ONIE_TRACE",
                            "REASONER ERROR BODY: $body"
                        )

                        throw Exception(
                            "Gemini HTTP ${response.code}: $body"
                        )
                    }

                    val json =
                        JSONObject(body)

                    extractOutputText(json)
                }

            } catch (e: Throwable) {

                Log.e(
                    "ONIE_TRACE",
                    "REASONER REQUEST FAILED: " +
                            "${e.javaClass.simpleName}: ${e.message}",
                    e
                )

                throw e
            }
        }

    private fun extractOutputText(
        json: JSONObject
    ): String {

        val candidates =
            json.optJSONArray("candidates")
                ?: throw Exception(
                    "ONIE received no candidates from Gemini."
                )

        for (i in 0 until candidates.length()) {

            val candidate =
                candidates.optJSONObject(i)
                    ?: continue

            val content =
                candidate.optJSONObject("content")
                    ?: continue

            val parts =
                content.optJSONArray("parts")
                    ?: continue

            for (j in 0 until parts.length()) {

                val part =
                    parts.optJSONObject(j)
                        ?: continue

                val text =
                    part.optString("text")

                if (text.isNotBlank()) {

                    Log.e(
                        "ONIE_TRACE",
                        "REASONER: Text response extracted"
                    )

                    return text
                }
            }
        }

        throw Exception(
            "ONIE received an empty AI response."
        )
    }
}
