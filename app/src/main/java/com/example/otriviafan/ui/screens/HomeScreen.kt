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
import com.example.otriviafan.ui.rememberResponsiveSizes
import com.example.otriviafan.ui.theme.LuckiestGuyFont
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference
    val sizes = rememberResponsiveSizes()

    LaunchedEffect(uid) {
        if (uid != null) {
            val isNewUserSnapshot = database.child("users").child(uid).child("isNewUser").get().await()
            val isNewUser = isNewUserSnapshot.getValue(Boolean::class.java) == true
            if (isNewUser) {
                Toast.makeText(context, "Te regalamos 5 puntos de bienvenida🎉", Toast.LENGTH_LONG).show()
                database.child("users").child(uid).child("isNewUser").setValue(false)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                .padding(horizontal = sizes.screenWidth * 0.08f, vertical = sizes.screenHeight * 0.01f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(id = R.drawable.logobrillante),
                contentDescription = "Título OTRIVIA FAN",
                modifier = Modifier
                    .fillMaxWidth()
                    .size(sizes.screenHeight * 0.42f)
            )

            OTStyledButton(
                label = "🎯 J U G A R",
                height = sizes.screenHeight * 0.12f,
                fontSize = sizes.fontSizeLarge
            ) { navController.navigate(Screen.LevelMap.route) }

            Spacer(modifier = Modifier.height(sizes.screenHeight * 0.01f))

            OTStyledButton(
                label = "🛍️ T I E N D A",
                height = sizes.buttonHeight,
                fontSize = sizes.fontSizeMedium
            ) { navController.navigate(Screen.Store.route) }

            OTStyledButton(
                label = "👤 M I   P E R F I L",
                height = sizes.buttonHeight,
                fontSize = sizes.fontSizeMedium
            ) { navController.navigate(Screen.Profile.route) }

            Spacer(modifier = Modifier.height(sizes.screenHeight * 0.08f))
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
