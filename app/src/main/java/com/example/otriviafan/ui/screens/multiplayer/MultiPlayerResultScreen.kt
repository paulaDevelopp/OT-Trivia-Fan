package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.example.otriviafan.data.model.PuntosUsuario
import com.google.firebase.database.FirebaseDatabase

@Composable
fun MultiPlayerResultScreen(
    navController: NavController,
    matchViewModel: MatchViewModel
) {
    val match = matchViewModel.match.collectAsState().value ?: return
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val myScore = if (uid == match.player1Id) match.player1Score else match.player2Score
    val opponentScore = if (uid == match.player1Id) match.player2Score else match.player1Score

    val resultText = when {
        myScore > opponentScore -> "🎉 ¡Ganaste la partida!"
        myScore < opponentScore -> "😓 Perdiste... ¡la próxima será!"
        else -> "🤝 ¡Empate!"
    }

    val scope = rememberCoroutineScope()

    // ⚡ Guardar los puntos al entrar en esta pantalla
    LaunchedEffect(Unit) {
        val pointsToAdd = when {
            myScore > opponentScore -> 20
            myScore == opponentScore -> 10
            else -> 0
        }
        if (pointsToAdd > 0) {
            savePointsForUser(pointsToAdd)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Partida finalizada", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Tu puntuación: $myScore", style = MaterialTheme.typography.titleMedium)
        Text("Oponente: $opponentScore", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Text(resultText, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }) {
            Text("Volver al inicio")
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
