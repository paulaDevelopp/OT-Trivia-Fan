package com.example.otriviafan.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.navigation.Screen
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_home),
            contentDescription = "Fondo home",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 64.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedButton(
                label = "🛍️ TIENDA",
                backgroundColor = Color(0xFFBA68C8),
                onClick = { navController.navigate(Screen.Store.route) },
                modifier = Modifier.fillMaxWidth(0.7f).height(70.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedButton(
                label = "🎯 J U G A R",
                backgroundColor = Color(0xFF64B5F6),
                fontSize = 24.sp,
                onClick = { navController.navigate(Screen.LevelMap.route) },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedButton(
                label = "👤 MI PERFIL",
                backgroundColor = Color(0xFF9575CD),
                onClick = { navController.navigate(Screen.Profile.route) },
                modifier = Modifier.fillMaxWidth(0.7f).height(70.dp)
            )
        }
    }
}
@Composable
fun AnimatedButton(
    label: String,
    backgroundColor: Color,
    fontSize: TextUnit = 18.sp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 1.05f else 1f, label = "scale")

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = Color.Cyan),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif
        )
    }
}
