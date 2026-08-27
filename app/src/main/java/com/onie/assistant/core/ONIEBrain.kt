package com.onie.assistant.core

import android.content.Context
import com.onie.assistant.BuildConfig
import com.onie.assistant.network.ONIEConnectivity
import com.onie.assistant.network.ONIEOnlineBrain

class ONIEBrain(
    context: Context,
    apiKey: String = BuildConfig.GEMINI_API_KEY
) {

    private val connectivity = ONIEConnectivity(context)
    private val onlineBrain = ONIEOnlineBrain(apiKey)

    fun isOnline(): Boolean {
        return connectivity.isOnline()
    }

    suspend fun respond(input: String): String {

        val normalized = input.trim().lowercase()

        // Always keep basic local commands available.
        when {
            normalized.contains("25") && normalized.contains("16") ->
                return "25 times 16 is 400."

            normalized.contains("who are you") ->
                return "I'm ONIE, your Operational Networked Intelligent Engine."
        }

        // If there is no internet, use the local fallback.
        if (!isOnline()) {
            return when {
                normalized.contains("hello") || normalized == "hi" ->
                    "Hello. I'm ONIE. I'm currently offline."

                else ->
                    "I heard you say: $input. I'm currently offline."
            }
        }

        // Internet is available — use ONIE's online intelligence.
        return try {
            onlineBrain.respond(input)
        } catch (e: Exception) {
            "I'm having trouble reaching my online intelligence right now."
        }
    }
}
