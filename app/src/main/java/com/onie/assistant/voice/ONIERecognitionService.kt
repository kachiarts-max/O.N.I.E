package com.onie.assistant.voice

import android.content.Intent
import android.speech.RecognitionService

class ONIERecognitionService : RecognitionService() {

    override fun onStartListening(
        recognizerIntent: Intent?,
        listener: Callback?
    ) {
        // Recognition is currently handled by VoiceManager.
        // This service exists so Android can register ONIE
        // correctly as a VoiceInteractionService.
    }

    override fun onCancel(listener: Callback?) {
        // Nothing to cancel yet.
    }

    override fun onStopListening(listener: Callback?) {
        // Nothing to stop yet.
    }
}
