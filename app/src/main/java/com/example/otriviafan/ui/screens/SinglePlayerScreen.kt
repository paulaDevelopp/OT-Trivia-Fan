package com.example.otriviafan.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.api.RetrofitClient
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.components.ConfettiAnimation
import com.example.otriviafan.viewmodel.SinglePlayerViewModel
import com.example.otriviafan.viewmodel.UserViewModel
import com.example.otriviafan.viewmodel.factory.SinglePlayerViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun SinglePlayerScreen(navController: NavController, levelName: String) {
    val viewModel: SinglePlayerViewModel = viewModel(
        factory = SinglePlayerViewModelFactory(levelName)
    )
    val userViewModel: UserViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val lives by viewModel.visibleLives.collectAsState()
    val levelCompleted by viewModel.levelCompleted.collectAsState()
    val outOfLives by viewModel.outOfLives.collectAsState()
    val partidaPerfecta by viewModel.partidaPerfecta.collectAsState()
    val nivelSubido by viewModel.nivelSubido.collectAsState()
    val shouldRefresh by viewModel.userDataShouldRefresh.collectAsState()

    var selectedAnswerId by remember { mutableStateOf<Int?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    var canRetry by remember { mutableStateOf(false) }
    var nivelYaCompletado by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }

    val repository = Repository()

    LaunchedEffect(levelName) {
        val userId = userViewModel.getUserId()
        val yaCompletado = repository.verificarNivelCompletado(userId, levelName)
        if (yaCompletado) {
            nivelYaCompletado = true
        } else {
            viewModel.loadQuestions()
        }
    }

    LaunchedEffect(outOfLives) {
        if (outOfLives) {
            canRetry = viewModel.canRetryWithPoints()
        }
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            userViewModel.refreshUserData()
            viewModel.setRefreshHandled()
        }
    }
    LaunchedEffect(levelCompleted) {
        if (levelCompleted && !nivelSubido && !outOfLives) {
            viewModel.finishLevel()
        }
    }

    if (outOfLives) {
        OutOfLivesDialog(
            onRetry = {
                scope.launch {
                    viewModel.setLives(0)
                    delay(500)
                    val retried = viewModel.retryUsingPoints()
                    if (!retried) {
                        delay(300)
                        navController.popBackStack()
                    }
                }
            },
            onExit = {
                scope.launch {
                    delay(300)
                    navController.popBackStack()
                }
            },
            canRetry = canRetry
        )
    }

    if (nivelSubido) {
        if (partidaPerfecta) {
            showConfetti = true
        }

        val dificultad = when {
            levelName.startsWith("easy") -> "easy"
            levelName.startsWith("medium") -> "medium"
            levelName.startsWith("difficult") -> "difficult"
            else -> "unknown"
        }

        scope.launch {
            try {
                val response = RetrofitClient.instance.uploadWallpapers(dificultad)
                if (response.isSuccessful) {
                    println("Fondos subidos: ${response.body()?.difficulty}")
                } else {
                    println("Error de servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Error en Retrofit: ${e.message}")
            }
        }

        scope.launch {
            userViewModel.marcarNivelComoCompletado(levelName)
        }

        val mensaje = if (partidaPerfecta)
            "¡Has hecho una partida perfecta! Respondiste correctamente todas las preguntas."
        else
            "Has completado el nivel. ¡Buen trabajo!"

        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Subiste de Nivel!") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearFeedbackFlags()
                    navController.navigate(Screen.LevelMap.route) {
                        popUpTo(Screen.LevelMap.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Vamos allá")
                }
            }
        )
    }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            title = { Text("¿Pausar partida?") },
            text = { Text("Si sales ahora, no se guardará el progreso de este nivel.") },
            confirmButton = {
                TextButton(onClick = {
                    showPauseDialog = false
                    navController.navigate(Screen.LevelMap.route) {
                        popUpTo(Screen.LevelMap.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Salir al mapa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPauseDialog = false }) {
                    Text("Continuar")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ot_sinlogo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        if (showConfetti) {
            ConfettiAnimation(trigger = true) {
                showConfetti = false
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text("Puntos", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Text("$score", color = Color(0xFFBB86FC), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.vida_extra),
                            contentDescription = "Vida extra",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("x$lives", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Nivel: ${levelName.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Pregunta ${currentQuestionIndex + 1} / ${questions.size}", color = Color.White.copy(alpha = 0.8f))
                }

                IconButton(onClick = { showPauseDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.pausa),
                        contentDescription = "Pausa",
                        modifier = Modifier.size(52.dp), // Establece un tamaño razonable
                        tint = Color.Unspecified // No intentes aplicar un tinte si es una imagen a color
                    )

                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (questions.isNotEmpty() && currentQuestionIndex < questions.size) {
                val question = questions[currentQuestionIndex]

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
                        Text(question.questionText, color = Color.Black, style = MaterialTheme.typography.headlineSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                question.answers.forEach { answer ->
                    val isSelected = selectedAnswerId == answer.id
                    val isCorrect = isSelected && answer.id == question.correctAnswerId

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable(enabled = selectedAnswerId == null) {
                                selectedAnswerId = answer.id
                                val correct = answer.id == question.correctAnswerId
                                if (correct) {
                                    showConfetti = true
                                    viewModel.submitAnswer(true)
                                } else {
                                    viewModel.submitAnswer(false)
                                }
                                scope.launch {
                                    delay(1000)
                                    selectedAnswerId = null
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(
                                    color = if (isCorrect) Color(0xFFBA68C8).copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.estrella),
                                contentDescription = answer.answerText,
                                modifier = Modifier.size(70.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = answer.answerText,
                            color = Color.White,
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}