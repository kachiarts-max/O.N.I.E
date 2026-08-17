package com.onie.assistant.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.onie.assistant.core.ONIEState
import kotlin.math.sin

private val Background = Color(0xFF03070D)
private val Cyan = Color(0xFF00E5FF)
private val CyanSoft = Color(0xFF43BFEA)
private val Violet = Color(0xFF9C5CFF)
private val Green = Color(0xFF39F5B2)

@Composable
fun HomeScreen(
    state: ONIEState,
    onMicrophonePressed: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "onie-core")
    val pulse by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val ringColor = when (state) {
        ONIEState.THINKING -> Violet
        ONIEState.EXECUTING -> Green
        else -> Cyan
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF071521), Background)
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.Menu, "Menu", tint = Color.White.copy(alpha = .8f))
            Icon(Icons.Default.Settings, "Settings", tint = Color.White.copy(alpha = .8f))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(66.dp))

            Text(
                text = "O N I E",
                color = Color.White,
                fontSize = 25.sp,
                letterSpacing = 8.sp
            )

            Text(
                text = "OPERATIONAL NETWORKED\nINTELLIGENT ENGINE",
                color = Color.White.copy(alpha = .48f),
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                lineHeight = 14.sp
            )

            Spacer(Modifier.weight(1f))

            ONIECore(
                state = state,
                color = ringColor,
                scale = pulse
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = stateLabel(state),
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(14.dp))

            VoiceWaveform(
                active = state == ONIEState.LISTENING || state == ONIEState.SPEAKING,
                color = ringColor
            )

            Spacer(Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .size(78.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Cyan.copy(alpha = .35f), Color.Transparent)
                        ),
                        CircleShape
                    )
                    .clickable { onMicrophonePressed() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .background(Color(0xFF081522), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Speak to ONIE",
                        tint = Cyan,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavItem("Home", true)
                NavItem("Memory", false)
                NavItem("Automations", false)
                NavItem("Tools", false)
            }
        }
    }
}

@Composable
private fun ONIECore(
    state: ONIEState,
    color: Color,
    scale: Float
) {
    Canvas(
        modifier = Modifier
            .size(230.dp)
            .scale(scale)
    ) {
        val center = this.center
        val maxRadius = size.minDimension / 2f

        for (i in 1..5) {
            val radius = maxRadius * (0.34f + i * 0.095f)
            drawCircle(
                color = color.copy(alpha = 0.035f + (0.01f * i)),
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        drawCircle(
            color = color.copy(alpha = .12f),
            radius = maxRadius * .38f,
            center = center
        )

        drawCircle(
            color = color.copy(alpha = .8f),
            radius = maxRadius * .31f,
            center = center,
            style = Stroke(width = 4.dp.toPx())
        )

        drawCircle(
            color = color.copy(alpha = .25f),
            radius = maxRadius * .24f,
            center = center
        )
    }
}

@Composable
private fun VoiceWaveform(
    active: Boolean,
    color: Color
) {
    val infinite = rememberInfiniteTransition(label = "wave")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing)
        ),
        label = "wave-phase"
    )

    Canvas(
        modifier = Modifier
            .width(190.dp)
            .height(28.dp)
            .alpha(if (active) 1f else .45f)
    ) {
        val bars = 25
        val spacing = size.width / bars

        for (i in 0 until bars) {
            val x = spacing * i + spacing / 2f
            val wave = if (active) {
                (sin(phase + i * .55f) * .5f + .5f)
            } else {
                .25f
            }
            val height = size.height * (.18f + .72f * wave)

            drawLine(
                color = color.copy(alpha = .8f),
                start = androidx.compose.ui.geometry.Offset(x, size.height / 2f - height / 2f),
                end = androidx.compose.ui.geometry.Offset(x, size.height / 2f + height / 2f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
private fun NavItem(label: String, selected: Boolean) {
    Text(
        text = label,
        color = if (selected) Cyan else Color.White.copy(alpha = .45f),
        fontSize = 11.sp
    )
}

private fun stateLabel(state: ONIEState): String = when (state) {
    ONIEState.IDLE -> "How can I help?"
    ONIEState.WAKE -> "Yes?"
    ONIEState.LISTENING -> "I'm listening..."
    ONIEState.THINKING -> "Thinking..."
    ONIEState.SPEAKING -> "Speaking..."
    ONIEState.EXECUTING -> "Executing..."
    ONIEState.CONFIRMING -> "Confirmation required"
    ONIEState.ERROR -> "Something went wrong"
}
