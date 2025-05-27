package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen

@Composable
fun MultiPlayerEntryScreen(navController: NavController, levelName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👥 Modo multijugador",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                navController.navigate("multiplayer_waiting/$levelName")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("🎮 Crear partida", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("multiplayer_join/$levelName")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("🔗 Unirse a partida", fontSize = 18.sp)
        }
    }
}
