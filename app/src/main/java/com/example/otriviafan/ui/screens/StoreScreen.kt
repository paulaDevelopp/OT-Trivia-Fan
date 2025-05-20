package com.example.otriviafan.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.otriviafan.viewmodel.StoreViewModel
import com.example.otriviafan.viewmodel.UserViewModel
import com.example.otriviafan.util.saveImageToGallery
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(navController: NavController, storeViewModel: StoreViewModel, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val availableWallpapers by userViewModel.availableWallpapers.collectAsState()
    val unlockedWallpapers by userViewModel.unlockedWallpapers.collectAsState()
    val purchasedWallpapers by userViewModel.purchasedWallpapers.collectAsState()
    val userPoints by userViewModel.points.collectAsState()
    val errorMessage by storeViewModel.error.collectAsState()
    val successMessage by storeViewModel.successMessage.collectAsState()
    val savedWallpapers = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(Unit) {
        userViewModel.reloadWallpapers()
        storeViewModel.refreshUserPurchases()
    }
    Scaffold(
        containerColor = Color(0xFFFAF7FF), // Fondo muy suave lila
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF512DA8),
                contentColor = Color.White
            ) {
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
                    icon = { Text("👤", fontSize = 20.sp) },
                    label = { Text("Perfil", fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("profile") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🪩 Tienda de Fondos OT",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF4A148C),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(40),
                color = Color(0xFFE1BEE7).copy(alpha = 0.3f),
                shadowElevation = 3.dp
            ) {
                Text(
                    text = "Mis puntos: $userPoints",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6A1B9A),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(availableWallpapers) { item ->
                    val isUnlocked = unlockedWallpapers.contains(item.filename)
                    val isPurchased = purchasedWallpapers.contains(item.filename)

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        elevation = CardDefaults.cardElevation(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(item.url),
                                contentDescription = item.filename,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .alpha(if (isUnlocked) 1f else 0.3f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isPurchased) {
                                val isSaved = savedWallpapers[item.filename] == true

                                Button(
                                    onClick = {
                                        scope.launch {
                                            val success = saveImageToGallery(context, item.url, item.filename)
                                            if (success) {
                                                savedWallpapers[item.filename] = true
                                                Toast.makeText(context, "Guardado en galería", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                                            }

                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                                    enabled = !isSaved
                                ) {
                                    Text(if (savedWallpapers[item.filename] == true) "Guardado" else "Guardar", fontSize = 14.sp)
                                }
                            }else {
                                Button(
                                    onClick = {
                                        userViewModel.buyWallpaper(
                                            item,
                                            onSuccess = { storeViewModel.refreshUserPurchases() },
                                            onFailure = { e ->
                                                storeViewModel.setError(
                                                    e.message ?: "Error"
                                                )
                                            }
                                        )
                                    },
                                    enabled = isUnlocked && userPoints >= item.price,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                                ) {
                                    Text("Comprar: ${item.price} pts", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = {
                        TextButton(onClick = { storeViewModel.clearMessages() }) {
                            Text("OK")
                        }
                    }
                ) { Text(error) }
            }

            successMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = {
                        TextButton(onClick = { storeViewModel.clearMessages() }) {
                            Text("OK")
                        }
                    }
                ) { Text(message) }
            }
        }
    }

}
