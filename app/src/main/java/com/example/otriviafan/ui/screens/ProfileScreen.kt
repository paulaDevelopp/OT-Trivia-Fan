package com.example.otriviafan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.otriviafan.R
import com.example.otriviafan.viewmodel.UserViewModel
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewmodel.compose.viewModel

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

    val purchasedWallpapers by viewModel.purchasedWallpapers.collectAsState()
    val availableWallpapers by viewModel.availableWallpapers.collectAsState()
    val unlockedWallpapers by viewModel.unlockedWallpapers.collectAsState()

    val savedWallpapers by viewModel.savedWallpapers.collectAsState()

    val savedImages = availableWallpapers.filter {
        savedWallpapers.contains(it.filename)
    }

    LaunchedEffect(uid) {
        uid?.let {
            val db = com.google.firebase.database.FirebaseDatabase.getInstance().reference
            val userRef = db.child("users").child(it)
            try {
                val emailSnap = userRef.child("email").get().await()
                email = emailSnap.getValue(String::class.java) ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF80D8FF)) {
                NavigationBarItem(
                    icon = { Text("🏠", fontSize = 20.sp) },
                    label = { Text("Inicio", fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Text("🎮", fontSize = 20.sp) },
                    label = { Text("Jugar", fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("level_map") }
                )
                NavigationBarItem(
                    icon = { Text("🛍️", fontSize = 20.sp) },
                    label = { Text("Tienda", fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("store") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Fondo
            Image(
                painter = painterResource(id = R.drawable.ot_sinlogo),
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
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC80D8FF)),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Icon",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Perfil", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("📧 $email", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        Text("🏆 Nivel: $highestLevelUnlocked", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        Text(
                            text = "⭐ Puntos: $points",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFEB3B),
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Fondos guardados en galería", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(savedImages) { bg ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(bg.url),
                                    contentDescription = bg.filename,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Text(
                                    text = "📥 Guardado",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
