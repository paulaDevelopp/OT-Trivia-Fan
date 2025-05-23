package com.example.otriviafan.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*

/*
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LevelGameScreen(navController: NavController, selectedLevel: Int) {
    val factory = remember { LevelGameViewModelFactory(selectedLevel) }
    val viewModel: LevelGameViewModel = viewModel(factory = factory)

    val questions = viewModel.questions.collectAsState().value
    val currentIndex = viewModel.currentQuestionIndex.collectAsState().value
    val score = viewModel.score.collectAsState().value
    val lives = viewModel.lives.collectAsState().value
    val levelCompleted = viewModel.levelCompleted.collectAsState().value
    val outOfLives = viewModel.outOfLives.collectAsState().value
    val perfectStreak = viewModel.perfectStreak.collectAsState().value
    val nivelSubido = viewModel.nivelSubido.collectAsState().value
    val mostrarNivel1Superado = viewModel.mostrarSubidaDeNivelDesdeNivel1.collectAsState().value

    val scope = rememberCoroutineScope()
    var selectedAnswerId by remember { mutableStateOf<Int?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var canRetry by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
    }

    LaunchedEffect(outOfLives) {
        if (outOfLives) {
            canRetry = viewModel.canRetryWithPoints()
        }
    }

    if (nivelSubido) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("\uD83C\uDF89 ¡Subiste de Nivel!") },
            text = { Text("Has respondido 20 preguntas seguidas correctamente.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFeedbackFlags() }) {
                    Text("Continuar")
                }
            }
        )
    }

    if (mostrarNivel1Superado) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Nivel 1 superado!") },
            text = { Text("Ya puedes acceder al Nivel 2.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearFeedbackFlags()
                    navController.popBackStack()
                }) {
                    Text("Ir al selector de niveles")
                }
            }
        )
    }

    /*if (levelCompleted) {
        LevelCompletedDialog(
            onContinue = { navController.popBackStack() },
            score = score,
            level = selectedLevel
        )
    }*/

    if (outOfLives) {
        OutOfLivesDialog(
            onRetry = {
                scope.launch {
                    val retried = viewModel.retryUsingPoints()
                    if (!retried) {
                        navController.popBackStack()
                    }
                }
            },
            onExit = { navController.popBackStack() },
            canRetry = canRetry
        )
    }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text("Nivel $selectedLevel", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Puntos: $score", color = Color.White)
                Text("Vidas: $lives", color = Color.White)
                Text("Racha perfecta: $perfectStreak / 20", color = Color.White)

                Spacer(Modifier.height(24.dp))

                if (questions.isNotEmpty() && currentIndex < questions.size) {
                    val question = questions[currentIndex]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8E24AA)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = question.questionText,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    question.answers.forEach { answer ->
                        Button(
                            onClick = {
                                selectedAnswerId = answer.id
                                isCorrect = answer.id == question.correctAnswerId
                                showResult = true
                                scope.launch {
                                    delay(1000)
                                    viewModel.submitAnswer(isCorrect)
                                    selectedAnswerId = null
                                    showResult = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAnswerId == answer.id) Color(0xFF3949AB) else Color(0xFF673AB7)
                            ),
                            enabled = selectedAnswerId == null
                        ) {
                            Text(text = answer.answerText, color = Color.White)
                        }
                    }

                    if (showResult) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (isCorrect) "✅ ¡Correcto!" else "❌ Incorrecto",
                            color = if (isCorrect) Color.Green else Color.Red,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelCompletedDialog(
    onContinue: () -> Unit,
    score: Int,
    level: Int
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("¡Nivel $level completado!") },
        text = { Text("Has ganado $score puntos.") },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Continuar")
            }
        }
    )
}
*/
@Composable
fun OutOfLivesDialog(
    onRetry: () -> Unit,
    onExit: () -> Unit,
    canRetry: Boolean
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("¡Te has quedado sin vidas!") },
        text = {
            if (canRetry) {
                Text("¿Quieres usar 5 puntos para continuar?")
            } else {
                Text("No tienes suficientes puntos para continuar.")
            }
        },
        confirmButton = {
            if (canRetry) {
                TextButton(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text("Salir")
            }
        }
    )
}
