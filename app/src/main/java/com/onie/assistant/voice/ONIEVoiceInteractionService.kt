package com.onie.assistant.voice

import android.content.Intent
import android.os.IBinder
import android.service.voice.VoiceInteractionService

class ONIEVoiceInteractionService : VoiceInteractionService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onReady() {
        super.onReady()
    }

    override fun onShutdown() {
        super.onShutdown()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }
}
