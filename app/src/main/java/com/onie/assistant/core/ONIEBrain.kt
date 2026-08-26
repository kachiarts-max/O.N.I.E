package com.onie.assistant.core

import android.content.Context
import com.onie.assistant.network.ONIEConnectivity

/**
 * ONIEBrain controls reasoning and decides whether
 * ONIE should use local or online intelligence.
 */
class ONIEBrain(context: Context) {

    private val connectivity = ONIEConnectivity(context)

    fun isOnline(): Boolean {
        return connectivity.isOnline()
    }

    fun respond(input: String): String {
        val normalized = input.trim().lowercase()

        return when {
            normalized.contains("25") && normalized.contains("16") ->
                "25 times 16 is 400."

            normalized.contains("hello") || normalized == "hi" ->
                if (isOnline()) {
                    "Hello. I'm ONIE. I'm online and ready."
                } else {
                    "Hello. I'm ONIE. I'm currently offline."
                }

            normalized.contains("who are you") ->
                "I'm ONIE, your Operational Networked Intelligent Engine."

            else ->
                if (isOnline()) {
                    "I'm online, but my network intelligence engine is not connected yet."
                } else {
                    "I heard you say: $input. I'm currently offline."
                }
        }
    }
}
