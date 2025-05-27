package com.example.otriviafan.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.theme.LuckiestGuyFont
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference

    LaunchedEffect(uid) {
        if (uid != null) {
            val isNewUserSnapshot = database.child("users").child(uid).child("isNewUser").get().await()
            val isNewUser = isNewUserSnapshot.getValue(Boolean::class.java) == true
            if (isNewUser) {
                Toast.makeText(context, "¡Bienvenido! Te regalamos 5 puntos de bienvenida🎉", Toast.LENGTH_LONG).show()
                database.child("users").child(uid).child("isNewUser").setValue(false)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo__home),
            contentDescription = "Fondo home",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logobrillante),
                contentDescription = "Título OTRIVIA FAN",
                modifier = Modifier
                    .fillMaxWidth(0.99f)
                    .size(380.dp)
            )

            OTStyledButton(
                label = "🎯 J U G A R",
                height = 100.dp,
                fontSize = 40.sp
            ) { navController.navigate(Screen.LevelMap.route) }

            Spacer(modifier = Modifier.height(10.dp))
            OTStyledButton(
                label = "🛍️ T I E N D A"
            ) { navController.navigate(Screen.Store.route) }

            OTStyledButton(
                label = "👤 M I   P E R F I L"
            ) { navController.navigate(Screen.Profile.route) }
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun OTStyledButton(
    label: String,
    height: Dp = 70.dp,
    fontSize: TextUnit = 22.sp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 1.02f else 1f, label = "scale")

    val shape = RoundedCornerShape(50)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(scale)
            .shadow(14.dp, shape)
            .background(Color(0xFF00BFFF), shape)
            .padding(4.dp)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF4FC3F7), Color(0xFF0288D1))
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .fillMaxWidth(0.85f)
            .height(height)
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color.LightGray,
            fontFamily = LuckiestGuyFont
        )
    }
}
