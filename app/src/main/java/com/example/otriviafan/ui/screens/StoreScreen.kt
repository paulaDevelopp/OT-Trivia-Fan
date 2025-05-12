package com.example.otriviafan.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Tienda de fondos de pantalla de OT",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tus puntos: $userPoints",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(availableWallpapers) { item ->
                val isUnlocked = unlockedWallpapers.contains(item.filename)
                val isPurchased = purchasedWallpapers.contains(item.filename)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(item.url),
                            contentDescription = item.filename,
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.LightGray)
                                .alpha(if (isUnlocked) 1f else 0.3f),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                     //   Text(item.filename, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(4.dp))

                        if (isPurchased) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = saveImageToGallery(
                                            context = context,
                                            imageUrl = item.url,
                                            filename = item.filename
                                        )
                                        val message = if (success) "Guardado en galería 📷" else "Error al guardar"
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("Guardar en galería")
                            }
                        } else {
                            Button(
                                onClick = {
                                    userViewModel.buyWallpaper(
                                        wallpaper = item,
                                        onSuccess = {
                                            storeViewModel.refreshUserPurchases()
                                        },
                                        onFailure = { e ->
                                            storeViewModel.setError(e.message ?: "Error al comprar")
                                        }
                                    )
                                },
                                enabled = isUnlocked && userPoints >= item.price,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                            ) {
                                Text("Comprar: ${item.price} pts")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        errorMessage?.let { error ->
            Snackbar(
                action = {
                    TextButton(onClick = { storeViewModel.clearMessages() }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) { Text(error) }
        }

        successMessage?.let { message ->
            Snackbar(
                action = {
                    TextButton(onClick = { storeViewModel.clearMessages() }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) { Text(message) }
        }
    }
}
