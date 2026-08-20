package com.onie.assistant.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.onie.assistant.core.ONIEBrain
import com.onie.assistant.core.ONIEState
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val brain: ONIEBrain,
    private val onStateChanged: (ONIEState) -> Unit
) : TextToSpeech.OnInitListener {

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context, this).apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                override fun onStart(utteranceId: String?) = Unit

                override fun onDone(utteranceId: String?) {
                    onStateChanged(ONIEState.IDLE)
                }

                override fun onError(utteranceId: String?) {
                    onStateChanged(ONIEState.ERROR)
                }
            })
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {

                override fun onReadyForSpeech(params: android.os.Bundle?) {
                    onStateChanged(ONIEState.LISTENING)
                }

                override fun onBeginningOfSpeech() {
                    onStateChanged(ONIEState.LISTENING)
                }

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    onStateChanged(ONIEState.THINKING)
                }

                override fun onError(error: Int) {
                    onStateChanged(ONIEState.ERROR)
                }

                override fun onResults(results: android.os.Bundle?) {
                    val text = results
                        ?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        ?.firstOrNull()

                    if (text.isNullOrBlank()) {
                        onStateChanged(ONIEState.ERROR)
                        return
                    }

                    onStateChanged(ONIEState.THINKING)

                    val response = brain.respond(text)
                    speak(response)
                }

                override fun onPartialResults(
                    partialResults: android.os.Bundle?
                ) = Unit

                override fun onEvent(
                    eventType: Int,
                    params: android.os.Bundle?
                ) = Unit
            })
        }
    }

    fun startListening() {
        onStateChanged(ONIEState.LISTENING)

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
            )
        }

        recognizer?.startListening(intent)
    }

    private fun speak(text: String) {
        onStateChanged(ONIEState.SPEAKING)

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "ONIE_RESPONSE"
        )
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(0.95f)
        }
    }

    fun destroy() {
        recognizer?.destroy()
        recognizer = null

        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
