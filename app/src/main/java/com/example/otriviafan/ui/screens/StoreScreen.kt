package com.example.otriviafan.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.otriviafan.R
import com.example.otriviafan.ui.rememberResponsiveSizes
import com.example.otriviafan.util.saveImageToGallery
import com.example.otriviafan.viewmodel.StoreViewModel
import com.example.otriviafan.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StoreScreen(navController: NavController, storeViewModel: StoreViewModel, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sizes = rememberResponsiveSizes()

    val availableWallpapers by userViewModel.availableWallpapers.collectAsState()
    val unlockedWallpapers by userViewModel.unlockedWallpapers.collectAsState()
    val purchasedWallpapers by userViewModel.purchasedWallpapers.collectAsState()
    val savedWallpapers by userViewModel.savedWallpapers.collectAsState()
    val userPoints by userViewModel.points.collectAsState()
    val errorMessage by storeViewModel.error.collectAsState()
    val successMessage by storeViewModel.successMessage.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.reloadWallpapers()
        storeViewModel.refreshUserPurchases()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF80D8FF),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Text("🏠", fontSize = sizes.fontSizeMedium) },
                    label = { Text("Inicio", fontSize = sizes.fontSizeSmall) },
                    selected = false,
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Text("🎮", fontSize = sizes.fontSizeMedium) },
                    label = { Text("Jugar", fontSize = sizes.fontSizeSmall) },
                    selected = false,
                    onClick = { navController.navigate("level_map") }
                )
                NavigationBarItem(
                    icon = { Text("👤", fontSize = sizes.fontSizeMedium) },
                    label = { Text("Perfil", fontSize = sizes.fontSizeSmall) },
                    selected = false,
                    onClick = { navController.navigate("profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo_store),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(sizes.spacingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tienda de Fondos OT",
                    fontSize = sizes.fontSizeLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAFAFA),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(sizes.spacingSmall))

                Surface(
                    shape = RoundedCornerShape(30),
                    color = Color(0xFFD1C4E9).copy(alpha = 0.6f),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "Mis puntos: $userPoints",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        fontSize = sizes.fontSizeMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A148C),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(sizes.spacingMedium))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(sizes.screenWidth * 0.4f),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(availableWallpapers) { item ->
                        val isUnlocked = unlockedWallpapers.contains(item.filename)
                        val isPurchased = purchasedWallpapers.contains(item.filename)
                        val isSaved = savedWallpapers.contains(item.filename)

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(12.dp).fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(sizes.screenWidth * 0.3f).clip(RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(item.url),
                                        contentDescription = item.filename,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().alpha(if (isUnlocked) 1f else 0.3f)
                                    )
                                    if (!isUnlocked) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "Bloqueado",
                                                color = Color.White,
                                                fontSize = sizes.fontSizeSmall,
                                                modifier = Modifier.padding(2.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(sizes.spacingSmall))

                                if (isPurchased) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val success = saveImageToGallery(context, item.url, item.filename)
                                                if (success) {
                                                    userViewModel.markWallpaperAsSaved(item.filename)
                                                }
                                                Toast.makeText(
                                                    context,
                                                    if (success) "Guardado en galería" else "Error al guardar",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                                        enabled = !isSaved
                                    ) {
                                        Text(if (isSaved) "Guardado" else "Guardar", fontSize = sizes.fontSizeSmall)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            userViewModel.buyWallpaper(
                                                item,
                                                onSuccess = { storeViewModel.refreshUserPurchases() },
                                                onFailure = { e -> storeViewModel.setError(e.message ?: "Error") }
                                            )
                                        },
                                        enabled = isUnlocked && userPoints >= item.price,
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                                    ) {
                                        Text("Comprar: ${item.price} pts", fontSize = 10.sp)

                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(sizes.spacingSmall))

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
}
