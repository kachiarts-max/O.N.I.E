package com.onie.assistant.core

/**
 * ONIEBrain is intentionally provider-agnostic.
 * v0.1 uses local demo reasoning so the UI/voice architecture
 * can be tested before connecting an external AI provider.
 */
class ONIEBrain {

    fun respond(input: String): String {
        val normalized = input.trim().lowercase()

        return when {
            normalized.contains("25") && normalized.contains("16") ->
                "25 times 16 is 400."

            normalized.contains("hello") || normalized == "hi" ->
                "Hello. I'm ONIE."

            normalized.contains("who are you") ->
                "I'm ONIE, your Operational Networked Intelligent Engine."

            else ->
                "I heard you say: $input. My full intelligence engine is coming online."
        }
    }
}
