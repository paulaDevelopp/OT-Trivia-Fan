package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MultiPlayerWaitingScreen(
    navController: NavController,
    matchViewModel: MatchViewModel,
    levelName: String
) {
    val match = matchViewModel.match.collectAsState().value
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Crear partida al iniciar
    LaunchedEffect(Unit) {
        matchViewModel.createMatch(uid, context, levelName)
    }

    // Navegar automáticamente cuando la partida esté lista
    LaunchedEffect(match?.status, match?.questions) {
        if (match?.status == "active" && match.questions.isNotEmpty()) {
            navController.navigate(Screen.MultiPlayerGame.route) {
                popUpTo(Screen.MultiPlayerWaiting.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_store), // Usa el fondo que desees
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "👥 Esperando a otro jugador...",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Código de partida:", color = Color.White, fontSize = 16.sp)
            Text(
                text = match?.matchId ?: "Cargando...",
                color = Color(0xFFBBDEFB),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(color = Color.White)
        }
    }
}
