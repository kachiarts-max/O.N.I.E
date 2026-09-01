package com.onie.assistant.core

import android.content.Context
import android.util.Log
import com.onie.assistant.network.ONIEConnectivity
import com.onie.assistant.network.ONIEWebEngine

class ONIEBrain(
    context: Context
) {

    private val connectivity = ONIEConnectivity(context)
    private val webEngine = ONIEWebEngine()

    suspend fun respond(input: String): String {

        val text = input.trim()

        if (text.isBlank()) {
            return "I didn't hear a request."
        }

        Log.e("ONIE_TRACE", "BRAIN received: $text")

        /*
         * ONIE is Internet-dependent by design.
         * No Internet = no operation.
         */
        if (!connectivity.isOnline()) {
            Log.e("ONIE_TRACE", "BRAIN: No Internet connection")
            return "I need an Internet connection to operate."
        }

        return try {

            Log.e("ONIE_TRACE", "BRAIN: Internet available")

            /*
             * Calculations are handled by ONIE itself.
             * Gemini is NOT required.
             */
            val calculation = webEngine.tryCalculate(text)

            if (calculation != null) {

                Log.e(
                    "ONIE_TRACE",
                    "BRAIN: Calculation result = $calculation"
                )

                calculation

            } else {

                /*
                 * Everything else goes to ONIE's
                 * Internet intelligence engine.
                 */
                Log.e(
                    "ONIE_TRACE",
                    "BRAIN: Sending request to Web Engine"
                )

                val result = webEngine.process(text)

                Log.e(
                    "ONIE_TRACE",
                    "BRAIN: Web Engine returned: $result"
                )

                result
            }

        } catch (e: Throwable) {

            Log.e(
                "ONIE_TRACE",
                "BRAIN ERROR",
                e
            )

            "I couldn't complete that request. ${e.message ?: e.javaClass.simpleName}"
        }
    }
}
