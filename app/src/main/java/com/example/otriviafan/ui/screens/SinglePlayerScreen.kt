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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.data.Repository
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.components.ConfettiAnimation
import com.example.otriviafan.ui.rememberResponsiveSizes
import com.example.otriviafan.viewmodel.SinglePlayerViewModel
import com.example.otriviafan.viewmodel.UserViewModel
import com.example.otriviafan.viewmodel.factory.SinglePlayerViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun SinglePlayerScreen(navController: NavController, levelName: String) {
    val viewModel: SinglePlayerViewModel = viewModel(factory = SinglePlayerViewModelFactory(levelName))
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
    val sizes = rememberResponsiveSizes()

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
        if (outOfLives) canRetry = viewModel.canRetryWithPoints()
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            userViewModel.refreshUserData()
            viewModel.setRefreshHandled()
        }
    }

    LaunchedEffect(levelCompleted) {
        if (levelCompleted && !nivelSubido && !outOfLives) viewModel.finishLevel()
    }

    if (outOfLives) {
        OutOfLivesDialog(
            onRetry = {
                scope.launch {
                    viewModel.setLives(0)
                    delay(500)
                    val retried = viewModel.retryUsingPoints()
                    if (!retried) navController.popBackStack()
                }
            },
            onExit = { scope.launch { navController.popBackStack() } },
            canRetry = canRetry
        )
    }

    if (nivelSubido) {
        if (partidaPerfecta) showConfetti = true

        scope.launch { userViewModel.marcarNivelComoCompletado(levelName) }

        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("¡Subiste de Nivel!", color = Color.White)
            },
            text = {
                Text(
                    if (partidaPerfecta) "¡Has hecho una partida perfecta!" else "Has completado el nivel. ¡Buen trabajo!",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearFeedbackFlags()
                    navController.navigate(Screen.LevelMap.route) {
                        popUpTo(Screen.LevelMap.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Vamos allá", color = Color.White)
                }
            },
            containerColor = Color(0xFF4CAF50)

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
            ConfettiAnimation(trigger = true) { showConfetti = false }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Puntos y vidas
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Puntos:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$score", color = Color(0xFFBB86FC), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.vida_extra),
                            contentDescription = "Vida extra",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("x$lives", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Nivel y número de pregunta
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Nivel: ${levelName.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Pregunta ${currentQuestionIndex + 1} / ${questions.size}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp
                    )
                }

                // Pausa
                IconButton(onClick = { showPauseDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.pausa),
                        contentDescription = "Pausa",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Unspecified
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (questions.isNotEmpty() && currentQuestionIndex < questions.size) {
                val question = questions[currentQuestionIndex]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor =Color(0xFF81D4FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = question.questionText,
                            color = Color.White,
                            fontSize = sizes.fontSizeLarge,
                            textAlign = TextAlign.Center,

                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                question.answers.forEach { answer ->
                    val isSelected = selectedAnswerId == answer.id
                    val isCorrect = isSelected && answer.id == question.correctAnswerId
                    val isIncorrect = isSelected && !isCorrect

                    val backgroundColor = when {
                        isCorrect -> Color(0xFF81C784) // Verde
                        isIncorrect -> Color(0xFFEF5350) // Rojo
                        else -> Color(0xFFBA68C8) // Lila morado (neutral)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = sizes.spacingSmall)
                            .clip(RoundedCornerShape(16.dp))
                            .background(backgroundColor)
                            .clickable(enabled = selectedAnswerId == null) {
                                scope.launch {
                                    selectedAnswerId = answer.id
                                    val correct = answer.id == question.correctAnswerId

                                    if (correct) showConfetti = true

                                    delay(1500) // Mostrar color verde/rojo
                                    viewModel.submitAnswer(correct)
                                    selectedAnswerId = null // Limpiar selección para que no se mantenga el color
                                }
                            }

                            .padding(vertical = sizes.spacingMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = answer.answerText,
                            color = Color.White,
                            fontSize = sizes.fontSizeMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
            }
        }
    }

/*package com.example.otriviafan.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.data.Repository
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
    val viewModel: SinglePlayerViewModel = viewModel(factory = SinglePlayerViewModelFactory(levelName))
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
    var showOutOfLivesDialog by remember { mutableStateOf(false) }

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
            showOutOfLivesDialog = true // Aquí lo activas
        }
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            userViewModel.refreshUserData()
            viewModel.setRefreshHandled()
        }
    }

    LaunchedEffect(levelCompleted) {
        if (levelCompleted && !outOfLives) {
            viewModel.finishLevel()
        }
    }

// Limpiar selección al cambiar de pregunta
    LaunchedEffect(currentQuestionIndex) {
        selectedAnswerId = null
    }

    if (outOfLives && showOutOfLivesDialog) {
        OutOfLivesDialog(
            onRetry = {
                scope.launch {
                    val retried = viewModel.retryUsingPoints()
                    if (retried) {
                        viewModel.setLives(1)
                        selectedAnswerId = null
                        showOutOfLivesDialog = false
                        viewModel.goToNextQuestion()
                    } else {
                        navController.popBackStack()
                    }
                }
            }
            ,
            onExit = {
                scope.launch {
                    showOutOfLivesDialog = false
                    navController.popBackStack()
                }
            },
            canRetry = canRetry
        )
    }


    if (nivelSubido) {
        if (partidaPerfecta) showConfetti = true

        scope.launch { userViewModel.marcarNivelComoCompletado(levelName) }

        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("¡Subiste de Nivel!", color = Color.Blue)
            },
            text = {
                Text(
                    if (partidaPerfecta)
                        "Has completado el nivel. ¡Buen trabajo!" else "Has completado el nivel. ¡Buen trabajo!",
                    color = Color.Black
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearFeedbackFlags()
                    navController.navigate(Screen.LevelMap.route) {
                        popUpTo(Screen.LevelMap.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }) {
                    Text("Vamos allá", color = Color.Green)
                }
            }
        )

    }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            title = {
                Text("¿Pausar partida?")
            },
            text = {
                Text("Si sales ahora, no se guardará el progreso de este nivel.")
            },
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
            ConfettiAnimation(trigger = true) { showConfetti = false }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Puntos y vidas
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Puntos:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("$score", color = Color(0xFFBB86FC), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.vida_extra),
                            contentDescription = "Vida extra",
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("x$lives", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Nivel y número de pregunta
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Nivel: ${levelName.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Pregunta ${currentQuestionIndex + 1} / ${questions.size}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp
                    )
                }

                // Pausa
                IconButton(onClick = { showPauseDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.pausa),
                        contentDescription = "Pausa",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Unspecified
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (questions.isNotEmpty() && currentQuestionIndex < questions.size) {
                val question = questions[currentQuestionIndex]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9)), // Lila suave
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = question.questionText,
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                    }
                }


                Spacer(modifier = Modifier.height(24.dp))

                question.answers.forEach { answer ->
                    val isSelected = selectedAnswerId == answer.id
                    val isCorrect = isSelected && answer.id == question.correctAnswerId
                    val isIncorrect = isSelected && answer.id != question.correctAnswerId

                    val shape = RoundedCornerShape(40)

                    val backgroundBrush = when {
                        isCorrect -> Brush.verticalGradient(listOf(Color(0xFF81C784), Color(0xFF388E3C))) // Verde
                        isIncorrect -> Brush.verticalGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828))) // Rojo
                        else -> Brush.verticalGradient(listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))) // Azul
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .shadow(10.dp, shape)
                            .clip(shape)
                            .background(brush = backgroundBrush)
                            .clickable(enabled = selectedAnswerId == null) {
                                selectedAnswerId = answer.id
                                val correct = answer.id == question.correctAnswerId
                                if (correct) showConfetti = true

                                scope.launch {
                                    delay(1500) // Tiempo para mostrar color
                                    viewModel.submitAnswer(correct)
                                }
                            }
                            .fillMaxWidth()
                            .height(65.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ){
                        Text(
                            text = answer.answerText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
    if (levelCompleted && !nivelSubido && !outOfLives) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

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
        title = {
            Text(
                "¡Te has quedado sin vidas!"
            )
        },
        text = {
            Text(
                if (canRetry) "¿Quieres usar 5 puntos para continuar?"
                else "No tienes suficientes puntos para continuar.",
                color = Color.Black
            )
        },
        confirmButton = {
            if (canRetry) {
                TextButton(onClick = onRetry) {
                    Text("Reintentar", color = Color.Blue)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text("Salir", color = Color.Gray)
            }
        }
    )

}
