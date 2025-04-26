package com.example.otriviafan.ui.screens

import android.graphics.Path
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.ui.components.ConfettiAnimation
import com.example.otriviafan.viewmodel.GameViewModel
import com.example.otriviafan.viewmodel.GameViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Confetti

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePlayerScreen(navController: NavController) {
    val context = LocalContext.current
    val factory = remember { GameViewModelFactory(context.applicationContext) }
    val viewModel: GameViewModel = viewModel(factory = factory)

    val questions = viewModel.questions.collectAsState().value
    val answers = viewModel.answers.collectAsState().value
    val index = viewModel.currentQuestionIndex.collectAsState().value
    val score = viewModel.score.collectAsState().value
    val nivel = viewModel.nivel.collectAsState().value
    val logros = viewModel.logros.collectAsState().value

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anon"
    val scope = rememberCoroutineScope() // <-- Añade esto arriba, al inicio de tu Composable

    var selectedAnswerId by remember { mutableStateOf<Int?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var shouldGoNext by remember { mutableStateOf(false) }
    var showScoreAnimation by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(3) }
    var gameStarted by remember { mutableStateOf(false) }
    var showResultsDialog by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }
    var showConfetti by remember { mutableStateOf(false) }

    // Cuenta atrás
    LaunchedEffect(Unit) {
        viewModel.loadQuestions(excludeIds = viewModel.usedQuestionIds)
        for (i in 3 downTo 1) {
            countdown = i
            delay(1000)
        }
        gameStarted = true
    }

    // Avanzar a siguiente pregunta
    LaunchedEffect(shouldGoNext) {
        if (shouldGoNext) {
            delay(1500)
            selectedAnswerId = null
            isAnswerCorrect = null
            if (index + 1 >= questions.size) {
                showResultsDialog = true
            } else {
                viewModel.nextQuestion()
            }
            shouldGoNext = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🎨 Fondo OT con oscurecido
        Image(
            painter = painterResource(id = R.drawable.ot_sinlogo),
            contentDescription = "Fondo OT",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        if (showConfetti) {
            ConfettiAnimation(
                trigger = showConfetti,
                onFinish = { showConfetti = false }
            )
        }

        if (!gameStarted) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = countdown.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
            }
        } else if (questions.isNotEmpty() && index < questions.size) {
            val question = questions[index]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Pregunta ${index + 1}/${questions.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF3949AB)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = question.questionText,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


                answers.forEach { answer ->
                    val isSelected = selectedAnswerId == answer.id
                    Button(
                        onClick = {
                            selectedAnswerId = answer.id
                            val correct = answer.id == question.correctAnswerId
                            isAnswerCorrect = correct
                            if (correct) {
                                showScoreAnimation = true
                                showConfetti = true
                                correctCount++

                                scope.launch {
                                    delay(1000) // ✅ Esperamos 1 segundo sin bloquear la UI
                                    showConfetti = false
                                }
                            } else {
                                incorrectCount++
                            }
                            shouldGoNext = true
                            viewModel.answerQuestion(answer, userId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF3949AB) else Color(0xFF8E24AA),
                            contentColor = Color.White
                        ),
                        enabled = selectedAnswerId == null
                    ) {
                        Text(
                            text = answer.answerText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                if (selectedAnswerId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val resultText = if (isAnswerCorrect == true) "✅ ¡Correcto!" else "❌ Incorrecto"
                    Text(
                        resultText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
            }
        }

        if (showResultsDialog) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetGame()
                        showResultsDialog = false
                        correctCount = 0
                        incorrectCount = 0
                    }) {
                        Text("Otra ronda")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Text("Salir")
                    }
                },
                title = { Text("🎉 ¡Ronda terminada!") },
                text = {
                    Text(
                        "Correctas: $correctCount\nIncorrectas: $incorrectCount\nPuntos obtenidos: $score",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
}

