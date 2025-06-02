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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.data.model.PuntosUsuario
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.rememberResponsiveSizes
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay

@Composable
fun MultiPlayerResultScreen(
    navController: NavController,
    matchViewModel: MatchViewModel
) {
    val match = matchViewModel.match.collectAsState().value ?: return
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val responsive = rememberResponsiveSizes()

    val myScore = if (uid == match.player1Id) match.player1Score else match.player2Score
    val opponentScore = if (uid == match.player1Id) match.player2Score else match.player1Score

    val resultText = when {
        match.player1Score == 0 && match.player2Score == 0 -> "⚠ Se abandonó la partida. " +
                "Nadie ganó puntos."
        myScore > opponentScore -> "🎉 ¡Ganaste la partida!"
        myScore < opponentScore -> "😓 Perdiste... ¡la próxima será!"
        else -> "🤝 ¡Empate! Pasas al siguiente nivel."
    }

    val pointsToAdd = when {
        match.player1Score == 0 && match.player2Score == 0 -> 0
        myScore > opponentScore -> 20
        myScore == opponentScore -> 10
        else -> 0
    }

    LaunchedEffect(Unit) {
        if (pointsToAdd > 0) {
            savePointsForUser(pointsToAdd)
        }
        delay(3000)
        navController.navigate(Screen.LevelMap.route) {
            popUpTo(Screen.LevelMap.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_store),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(responsive.screenWidth * 0.08f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🏁 Partida finalizada",
                fontSize = responsive.fontSizeLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(responsive.screenHeight * 0.04f))

            Text(
                "⭐ Tu puntuación: $myScore",
                color = Color(0xFFB3E5FC),
                fontSize = responsive.fontSizeMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                "👤 Oponente: $opponentScore",
                color = Color.White,
                fontSize = responsive.fontSizeSmall
            )

            Spacer(modifier = Modifier.height(responsive.screenHeight * 0.03f))

            Text(
                text = resultText,
                color = when {
                    myScore > opponentScore -> Color(0xFF81C784)
                    myScore < opponentScore -> Color(0xFFEF5350)
                    else -> Color(0xFFFFF176)
                },
                fontSize = responsive.fontSizeMedium,
                fontWeight = FontWeight.Bold
            )

            if (pointsToAdd > 0) {
                Spacer(modifier = Modifier.height(responsive.screenHeight * 0.015f))
                Text(
                    "🔓 Has ganado +$pointsToAdd puntos",
                    color = Color.White,
                    fontSize = responsive.fontSizeSmall
                )
            }

            Spacer(modifier = Modifier.height(responsive.screenHeight * 0.025f))

            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(responsive.screenHeight * 0.01f))
            Text("Volviendo al mapa...", color = Color.White, fontSize = responsive.fontSizeSmall)
        }
    }
}

fun savePointsForUser(pointsToAdd: Int) {
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return
    val userRef = FirebaseDatabase.getInstance().reference
        .child("users")
        .child(currentUser.uid)
        .child("puntos")

    userRef.get().addOnSuccessListener { snapshot ->
        val current = snapshot.getValue(PuntosUsuario::class.java) ?: PuntosUsuario()
        val updated = current.copy(
            total = current.total + pointsToAdd,
            ultimaActualizacion = System.currentTimeMillis()
        )
        userRef.setValue(updated)
    }.addOnFailureListener {
        it.printStackTrace()
    }
}
