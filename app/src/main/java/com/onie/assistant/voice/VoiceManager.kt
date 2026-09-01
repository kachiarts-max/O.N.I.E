package com.onie.assistant.voice

import android.content.Context
import android.util.Log
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.onie.assistant.core.ONIEBrain
import com.onie.assistant.core.ONIEState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val brain: ONIEBrain,
    private val onStateChanged: (ONIEState) -> Unit
) : TextToSpeech.OnInitListener {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

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
                    Log.e("ONIE_TRACE", "Speech recognition produced text: $text")

                    scope.launch {
                        try {
                            Log.e("ONIE_TRACE", "STEP 1: Sending text to ONIEBrain: $text")

                            val response = try {
                                Log.e("ONIE_TRACE", "STEP 2: Calling brain.respond()")
                                val result = brain.respond(text)
                                Log.e("ONIE_TRACE", "STEP 2 SUCCESS: ONIEBrain returned: $result")
                                result
                            } catch (e: Throwable) {
                                Log.e("ONIE_TRACE", "BRAIN FAILED", e)
                                val cause = e.cause
                                val detail = buildString {
                                    append(e.javaClass.simpleName)
                                    append(". ")
                                    append(e.message ?: "No message")
                                    if (cause != null) {
                                        append(". Cause: ")
                                        append(cause.javaClass.simpleName)
                                        append(". ")
                                        append(cause.message ?: "No cause message")
                                    }
                                }

                                speak("ONIE brain error. $detail")
                                return@launch
                            }

                            try {
                                Log.e("ONIE_TRACE", "STEP 3: Calling speak()")
                                speak(response)
                                Log.e("ONIE_TRACE", "STEP 3 SUCCESS: speak() called")
                            } catch (e: Throwable) {
                                Log.e("ONIE_TRACE", "TTS FAILED", e)
                                onStateChanged(ONIEState.ERROR)
                            }
                        } catch (e: Throwable) {
                            Log.e("ONIE_TRACE", "STEP ERROR: brain/respond/speak failed", e)

                            val errorMessage =
                                e.message ?: e.javaClass.simpleName

                            speak(
                                "ONIE debug error. " +
                                e.javaClass.simpleName +
                                ". " +
                                errorMessage
                            )
                        }
                    }
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

        scope.cancel()
    }
}
