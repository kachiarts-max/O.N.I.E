package com.onie.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.onie.assistant.core.ONIEBrain
import com.onie.assistant.core.ONIEState
import com.onie.assistant.ui.ONIEApp
import com.onie.assistant.ui.theme.ONIETheme
import com.onie.assistant.voice.VoiceManager

class MainActivity : ComponentActivity() {

    private lateinit var voiceManager: VoiceManager

    private var onieState by mutableStateOf(ONIEState.IDLE)

    private val microphonePermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                voiceManager.startListening()
            } else {
                onieState = ONIEState.ERROR
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val brain = ONIEBrain()

        voiceManager = VoiceManager(
            context = this,
            brain = brain,
            onStateChanged = { state ->
                runOnUiThread {
                    onieState = state
                }
            }
        )

        setContent {
            ONIETheme {
                ONIEApp(
                    state = onieState,
                    onMicrophonePressed = {
                        if (
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            voiceManager.startListening()
                        } else {
                            microphonePermission.launch(
                                Manifest.permission.RECORD_AUDIO
                            )
                        }
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        voiceManager.destroy()
        super.onDestroy()
    }
}
