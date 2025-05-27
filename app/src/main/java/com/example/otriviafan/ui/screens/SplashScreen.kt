package com.example.otriviafan.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(1900)
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splashscreen3d),
            contentDescription = "OT SplashScreen",
            contentScale = ContentScale.Crop, // Asegura que la imagen cubra toda la pantalla sin dejar bordes
            modifier = Modifier.fillMaxSize()
        )
    }
}