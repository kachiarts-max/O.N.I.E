package com.onie.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.onie.assistant.ui.ONIEApp
import com.onie.assistant.ui.theme.ONIETheme
import com.onie.assistant.voice.VoiceManager

class MainActivity : ComponentActivity() {

    private lateinit var voiceManager: VoiceManager

    private val microphonePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                voiceManager.startListening()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager = VoiceManager(
            context = this,
            onStateChanged = { state ->
                // UI state is owned by ONIEApp in v0.1.
                // This callback is intentionally kept as the bridge for the next iteration.
            }
        )

        setContent {
            ONIETheme {
                ONIEApp(
                    onMicrophonePressed = {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            voiceManager.startListening()
                        } else {
                            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
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
