package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun MultiPlayerJoinScreen(
    navController: NavController,
    matchViewModel: MatchViewModel,
    levelName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var joining by remember { mutableStateOf(false) }
    val match = matchViewModel.match.collectAsState().value

    LaunchedEffect(joining) {
        if (joining) {
            matchViewModel.joinMatch(uid, context, levelName)
        }
    }

    LaunchedEffect(match?.status, match?.questions) {
        if (joining && match?.status == "active" && match.questions.isNotEmpty()) {
            navController.navigate(Screen.MultiPlayerGame.route) {
                popUpTo(Screen.MultiPlayerJoin.route) { inclusive = true }
            }
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🎮 Modo multijugador",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!joining) {
            Text("Pulsa para buscar una partida disponible", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { joining = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unirse a una partida")
            }
        } else {
            Text("🔍 Buscando partida...", fontSize = 18.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (joining && match?.matchId != null) {
            Text("Código de partida:", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(match.matchId, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}
