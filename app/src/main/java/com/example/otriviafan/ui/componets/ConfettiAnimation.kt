package com.example.otriviafan.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

@Composable
fun ConfettiAnimation(
    trigger: Boolean,
    onFinish: () -> Unit
) {
    if (trigger) {
        Box(modifier = Modifier.fillMaxSize()) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(
                        speed = 0f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        spread = 360,
                        colors = listOf(
                            0xFF3949AB.toInt(), // Azul OT
                            0xFF8E24AA.toInt(), // Morado OT
                            0xFFE1BEE7.toInt()  // Lila claro
                        ),
                        position = Position.Relative(0.5, 0.0),
                        emitter = Emitter(duration = 1, TimeUnit.SECONDS).max(100),
                        shapes = listOf(Shape.Square, Shape.Circle) // ✅ así sí
                    )


                )
            )


        }

        LaunchedEffect(Unit) {
            delay(1500) // Espera que termine la animación
            onFinish()
        }
    }
}
