package com.onie.assistant.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.onie.assistant.core.ONIEState

@Composable
fun ONIEApp(
    onMicrophonePressed: () -> Unit
) {
    var state by remember { mutableStateOf(ONIEState.IDLE) }

    Surface(modifier = Modifier.fillMaxSize()) {
        HomeScreen(
            state = state,
            onMicrophonePressed = {
                state = ONIEState.LISTENING
                onMicrophonePressed()
            }
        )
    }
}
