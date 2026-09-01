package com.onie.assistant.core

import android.content.Context
import android.util.Log
import com.onie.assistant.network.ONIEConnectivity
import com.onie.assistant.network.ONIEOnlineBrain
import com.onie.assistant.network.ONIEWebEngine

class ONIEBrain(
    context: Context
) {

    private val connectivity =
        ONIEConnectivity(context)

    private val webEngine =
        ONIEWebEngine()

    private val onlineBrain =
        ONIEOnlineBrain()

    suspend fun respond(
        input: String
    ): String {

        val text =
            input.trim()

        if (text.isBlank()) {
            return "I didn't hear a request."
        }

        Log.e(
            "ONIE_TRACE",
            "BRAIN received: $text"
        )

        if (!connectivity.isOnline()) {

            Log.e(
                "ONIE_TRACE",
                "BRAIN: No Internet connection"
            )

            return "I need an Internet connection to operate."
        }

        return try {

            /*
             * First: deterministic local calculation.
             */
            val calculation =
                webEngine.tryCalculate(text)

            if (calculation != null) {

                Log.e(
                    "ONIE_TRACE",
                    "BRAIN: Local calculation = $calculation"
                )

                return calculation
            }

            /*
             * Everything else goes to the
             * actual AI reasoning engine.
             */
            Log.e(
                "ONIE_TRACE",
                "BRAIN: Sending request to ONIE Reasoner"
            )

            val result =
                onlineBrain.respond(text)

            Log.e(
                "ONIE_TRACE",
                "BRAIN: Reasoner returned successfully"
            )

            result

        } catch (e: Throwable) {

            Log.e(
                "ONIE_TRACE",
                "BRAIN ERROR",
                e
            )

            "I couldn't complete that request. " +
                    (e.message
                        ?: e.javaClass.simpleName)
        }
    }
}
