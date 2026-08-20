package com.onie.assistant.voice

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import com.onie.assistant.core.ONIEBrain

class ONIESession(
    service: ONIESessionService
) : VoiceInteractionSession(service) {

    private val brain = ONIEBrain()

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
    }
}
