package com.example.otriviafan.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.data.api.RetrofitClient
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.components.ConfettiAnimation
import com.example.otriviafan.util.nivelYaExisteEnFirebase
import com.example.otriviafan.util.obtenerNombreArchivoPorNivel
import com.example.otriviafan.util.subirPreguntasNivelDesdeAssets
import com.example.otriviafan.viewmodel.LevelGameViewModel
import com.example.otriviafan.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun SinglePlayerScreen(navController: NavController, nivelSeleccionado: Int) {
    val viewModel: LevelGameViewModel = viewModel(factory = LevelGameViewModel.Factory(nivelSeleccionado))
    val userViewModel: UserViewModel = viewModel()

    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val lives by viewModel.lives.collectAsState()
    val levelCompleted by viewModel.levelCompleted.collectAsState()
    val outOfLives by viewModel.outOfLives.collectAsState()
    val perfectStreak by viewModel.perfectStreak.collectAsState()
    val partidaPerfecta by viewModel.partidaPerfecta.collectAsState()
    val nivelSubido by viewModel.nivelSubido.collectAsState()
    val shouldRefresh by viewModel.userDataShouldRefresh.collectAsState()

    val scope = rememberCoroutineScope()

    var selectedAnswerId by remember { mutableStateOf<Int?>(null) }
    var showConfetti by remember { mutableStateOf(false) }
    var canRetry by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
    }
    val context = LocalContext.current

    LaunchedEffect(nivelSeleccionado) {
        val siguienteNivel = nivelSeleccionado + 1
        if (siguienteNivel <= 10) {
            val yaExiste = nivelYaExisteEnFirebase(siguienteNivel)
            if (!yaExiste) {
                val archivoUsado = subirPreguntasNivelDesdeAssets(context, siguienteNivel)
                println("Nivel $siguienteNivel subido a Firebase desde $archivoUsado")

                val dificultad = when {
                    archivoUsado?.startsWith("easy") == true -> "easy"
                    archivoUsado?.startsWith("medium") == true -> "medium"
                    archivoUsado?.startsWith("difficult") == true -> "difficult"
                    else -> "unknown"
                }

                // Aquí puedes hacer lo que necesites con la dificultad:
                println("Dificultad detectada: $dificultad")

                // Ejemplo: subir wallpapers, llamar a API, etc.
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val url = URL("https://tu-api.com/upload_wallpapers?difficulty=$dificultad")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connect()
                        println("Respuesta wallpapers: ${conn.responseCode}")
                    }
                }
            } else {
                println("ℹNivel $siguienteNivel ya existe.")
            }
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

    if (partidaPerfecta) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Partida Perfecta!") },
            text = { Text("Has respondido correctamente todas las preguntas.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFeedbackFlags() }) {
                    Text("Genial")
                }
            }
        )
    }

    if (nivelSubido) {
        showConfetti = true

        val siguienteNivel = nivelSeleccionado + 1
        val archivoUsado = obtenerNombreArchivoPorNivel(context, siguienteNivel)
        val dificultad = when {
            archivoUsado?.startsWith("easy") == true -> "easy"
            archivoUsado?.startsWith("medium") == true -> "medium"
            archivoUsado?.startsWith("difficult") == true -> "difficult"
            else -> "unknown"
        }

        // Subida de wallpapers desde Android al superar un nivel
        scope.launch {
            try {
                val response = RetrofitClient.instance.uploadWallpapers(dificultad)
                if (response.isSuccessful) {
                    println(" Fondos subidos: ${response.body()?.difficulty}")
                } else {
                    println(" Error de servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                println(" Error en la petición Retrofit: ${e.message}")
            }
        }


        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Subiste de Nivel!") },
            text = { Text("Has respondido correctamente todas las preguntas. ¡Buen trabajo!") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearFeedbackFlags()
                    navController.navigate(Screen.LevelSelect.route) {
                        popUpTo(Screen.LevelSelect.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Vamos allá")
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
                Column {
                    Text("Puntos: $score", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Vidas: $lives", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Nivel: $nivelSeleccionado", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Pregunta ${currentQuestionIndex + 1} / 15", color = Color.White, style = MaterialTheme.typography.bodyMedium)
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
                    Button(
                        onClick = {
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
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF3949AB) else Color(0xFF8E24AA),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedAnswerId == null
                    ) {
                        Text(answer.answerText)
                    }
                }
            }
        }
    }
}
