package com.onie.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ONIEDark = darkColorScheme(
    background = Color(0xFF03070D),
    surface = Color(0xFF07111C),
    primary = Color(0xFF00E5FF),
    secondary = Color(0xFF9C5CFF)
)

@Composable
fun ONIETheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ONIEDark,
        content = content
    )
}
