package com.onie.assistant.voice

import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

class ONIESessionService : VoiceInteractionSessionService() {

    override fun onNewSession(args: android.os.Bundle?): VoiceInteractionSession {
        return ONIESession(this)
    }
}
