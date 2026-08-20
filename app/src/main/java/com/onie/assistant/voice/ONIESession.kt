package com.onie.assistant.voice

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.onie.assistant.core.ONIEBrain
import java.util.Locale

class ONIESession(
    service: ONIESessionService
) : VoiceInteractionSession(service), TextToSpeech.OnInitListener {

    private val brain = ONIEBrain()
    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()

        tts = TextToSpeech(context, this).apply {
            setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {

                    override fun onStart(utteranceId: String?) = Unit

                    override fun onDone(utteranceId: String?) {
                        // Speech finished.
                    }

                    override fun onError(utteranceId: String?) {
                        // Speech failed.
                    }
                }
            )
        }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
    }

    override fun onHide() {
        super.onHide()
    }

    override fun onRequestCommand(
        request: CommandRequest
    ) {
        val command = request.command

        if (command.isNullOrBlank()) {
            request.cancel()
            return
        }

        val response = brain.respond(command)

        request.sendResult(
            Bundle().apply {
                putString("response", response)
            }
        )

        speak(response)
    }

    private fun speak(text: String) {
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "ONIE_SESSION_RESPONSE"
        )
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setSpeechRate(0.95f)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}
