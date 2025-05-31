package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.ui.theme.LuckiestGuyFont
import com.example.otriviafan.ui.rememberResponsiveSizes

@Composable
fun MultiPlayerEntryScreen(navController: NavController, levelName: String) {
    val sizes = rememberResponsiveSizes()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo__home),
            contentDescription = "Fondo multijugador",
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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "👥 MULTIJUGADOR",
                fontSize = sizes.fontSizeLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = LuckiestGuyFont,
                modifier = Modifier.padding(bottom = sizes.screenHeight * 0.08f)
            )

            OTStyledButton(
                label = "C R E A R   P A R T I D A",
                height = sizes.buttonHeight,
                fontSize = sizes.fontSizeMedium
            ) {
                navController.navigate("multiplayer_waiting/$levelName")
            }

            Spacer(modifier = Modifier.height(sizes.screenHeight * 0.025f))

            OTStyledButton(
                label = "U N I R S E",
                height = sizes.buttonHeight,
                fontSize = sizes.fontSizeMedium
            ) {
                navController.navigate("multiplayer_join/$levelName")
            }
        }
    }
}

@Composable
fun OTStyledButton(
    label: String,
    height: Dp,
    fontSize: TextUnit,
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
