package com.onie.assistant.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.onie.assistant.core.ONIEState

@Composable
fun ONIEApp(
    state: ONIEState,
    onMicrophonePressed: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        HomeScreen(
            state = state,
            onMicrophonePressed = onMicrophonePressed
        )
    }
}
