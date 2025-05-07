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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.otriviafan.viewmodel.StoreViewModel
import com.example.otriviafan.util.saveImageToGallery
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun StoreScreen(navController: NavController, storeViewModel: StoreViewModel) {
    val context = LocalContext.current
    val storeItems by storeViewModel.storeItems.collectAsState()
    val userPurchases by storeViewModel.userPurchases.collectAsState()
    val errorMessage by storeViewModel.error.collectAsState()
    val successMessage by storeViewModel.successMessage.collectAsState()

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        storeViewModel.loadStoreItems()
        userId?.let { storeViewModel.loadUserPurchases(it) }
    }

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
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(storeItems) { item ->
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
                            painter = rememberAsyncImagePainter(item.imageUrl),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(4.dp))

                        if (userPurchases.contains(item.id)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val success = saveImageToGallery(
                                            context = context,
                                            imageUrl = item.imageUrl,
                                            filename = item.name.replace(" ", "_")
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
                                    userId?.let { uid -> storeViewModel.buyItem(uid, item) }
                                },
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
