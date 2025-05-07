package com.example.otriviafan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.otriviafan.R
import com.example.otriviafan.data.entities.StoreItem
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.viewmodel.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(navController: NavHostController) {
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    val uid = auth.currentUser?.uid
    val viewModel: UserViewModel = viewModel()

    LaunchedEffect(Unit) {
        viewModel.refreshUserData()
    }

    val points by viewModel.points.collectAsState()
    val highestLevelUnlocked by viewModel.highestLevelUnlocked.collectAsState()

    var email by remember { mutableStateOf("") }
    var backgrounds by remember { mutableStateOf<List<StoreItem>>(emptyList()) }

    LaunchedEffect(uid) {
        uid?.let {
            val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
            val userRef = db.child("users").child(it)
            val storeRef = db.child("store_items")

            try {
                val emailSnap = userRef.child("email").get().await()
                email = emailSnap.getValue(String::class.java) ?: ""

                val purchasesSnap = userRef.child("purchases").get().await()
                val purchasedIds = purchasesSnap.children.mapNotNull { it.key }

                val storeSnap = storeRef.get().await()
                val allItems = storeSnap.children.mapNotNull { it.getValue(StoreItem::class.java) }

                // Ya no filtramos por "type"
                backgrounds = allItems.filter { purchasedIds.contains(it.id) }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFE1BEE7)) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Home.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = true,
                    onClick = {}
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ot_sinlogo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Perfil", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                Spacer(Modifier.height(16.dp))

                Text("Correo: $email", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text("Puntos: $points", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text("Nivel máximo desbloqueado: $highestLevelUnlocked", style = MaterialTheme.typography.bodyLarge, color = Color.White)

                Spacer(Modifier.height(24.dp))
                Divider(color = Color.White)
                Spacer(Modifier.height(16.dp))

                Text("🖼️ Fondos Canjeados", style = MaterialTheme.typography.titleMedium, color = Color.White)
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(150.dp)) {
                    items(backgrounds) { bg ->
                        Image(
                            painter = rememberAsyncImagePainter(bg.imageUrl),
                            contentDescription = bg.name,
                            modifier = Modifier
                                .size(140.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
