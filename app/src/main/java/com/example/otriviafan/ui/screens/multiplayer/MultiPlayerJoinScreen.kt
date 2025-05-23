package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Buscando partida...", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { joining = true },
            enabled = !joining
        ) {
            Text("Unirse a una partida disponible")
        }

        Spacer(modifier = Modifier.height(32.dp))

        LaunchedEffect(match?.status, match?.questions) {
            if (joining && match?.status == "active" && match.questions.isNotEmpty()) {
                navController.navigate(Screen.MultiPlayerGame.route) {
                    popUpTo(Screen.MultiPlayerJoin.route) { inclusive = true }
                }
            }
        }

        if (joining) {
            CircularProgressIndicator()
        }
    }
}
