package com.onie.assistant.voice

import android.service.voice.VoiceInteractionSession

class ONIESession(
    service: ONIESessionService
) : VoiceInteractionSession(service) {

    override fun onShow(args: android.os.Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
    }

    override fun onHide() {
        super.onHide()
    }
}
