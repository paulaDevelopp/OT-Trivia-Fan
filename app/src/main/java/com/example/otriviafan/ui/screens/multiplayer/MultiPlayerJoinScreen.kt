package com.example.otriviafan.ui.screens.multiplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.otriviafan.R
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.rememberResponsiveSizes
import com.example.otriviafan.ui.theme.LuckiestGuyFont
import com.example.otriviafan.viewmodel.MatchViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MultiPlayerJoinScreen(
    navController: NavController,
    matchViewModel: MatchViewModel,
    levelName: String
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val responsive = rememberResponsiveSizes()

    var joining by remember { mutableStateOf(false) }
    val match by matchViewModel.match.collectAsState()

    LaunchedEffect(joining) {
        if (joining) {
            matchViewModel.joinMatch(uid, context, levelName)
        }
    }

    LaunchedEffect(match?.status, match?.questions) {
        if (joining && match?.status == "active" && match?.questions?.isNotEmpty() == true) {
            navController.navigate(Screen.MultiPlayerGame.route) {
                popUpTo(Screen.MultiPlayerJoin.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo__home),
            contentDescription = null,
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
                .padding(horizontal = responsive.screenWidth * 0.08f),
            verticalArrangement = Arrangement.spacedBy(responsive.screenHeight * 0.03f, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎮 MULTIJUGADOR",
                fontSize = responsive.fontSizeLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = LuckiestGuyFont,
                color = Color.White
            )

            if (!joining) {
                Text(
                    "Pulsa para buscar una partida disponible",
                    fontSize = responsive.fontSizeSmall,
                    color = Color.White
                )

                OTStyledButton(label = "🔗 U N I R S E", height = responsive.buttonHeight, fontSize = responsive.fontSizeMedium) {
                    joining = true
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🔍 Buscando partida...",
                        fontSize = responsive.fontSizeMedium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(responsive.screenHeight * 0.02f))
                    CircularProgressIndicator(color = Color.Cyan)
                    Spacer(modifier = Modifier.height(responsive.screenHeight * 0.03f))

                    OTStyledButton(label = "❌ C A N C E L A R", height = responsive.buttonHeight * 0.75f, fontSize = responsive.fontSizeSmall) {
                        joining = false
                    }

                   /* if (match?.matchId != null) {
                        Spacer(modifier = Modifier.height(responsive.screenHeight * 0.03f))
                        Text("Código de partida:", fontSize = responsive.fontSizeSmall, color = Color.White)
                        Text(
                            match!!.matchId,
                            fontSize = responsive.fontSizeMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Cyan,
                            fontFamily = LuckiestGuyFont
                        )
                    }*/
                }
            }
        }
    }
}
