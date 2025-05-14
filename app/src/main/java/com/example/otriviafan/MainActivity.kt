package com.example.otriviafan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.otriviafan.data.Repository
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.screens.*
import com.example.otriviafan.ui.screens.multiplayer.MultiPlayerGameScreen
import com.example.otriviafan.viewmodel.StoreViewModel
import com.example.otriviafan.viewmodel.UserViewModel
import com.example.otriviafan.viewmodel.factory.StoreViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            Surface(color = MaterialTheme.colorScheme.background) {
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val repository = remember { Repository() }
    val storeViewModel: StoreViewModel = viewModel(factory = StoreViewModelFactory(repository))
    val userViewModel: UserViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // Pantallas iniciales
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) } // ✅ Añadido

        // Pantalla principal: mapa de niveles
        composable(Screen.LevelMap.route) {
            LevelMapScreen(navController = navController, userViewModel = userViewModel)
        }

        // Juego individual
        composable("${Screen.SinglePlayer.route}/{nivel}") { backStackEntry ->
            val nivel = backStackEntry.arguments?.getString("nivel")?.toIntOrNull() ?: 1
            SinglePlayerScreen(navController = navController, nivelSeleccionado = nivel)
        }

        // Juego multijugador desde el mapa
        composable(
            route = "multiplayer_game_screen/{nivelId}",
            arguments = listOf(navArgument("nivelId") { type = NavType.IntType })
        ) { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getInt("nivelId") ?: 1
            MultiPlayerGameScreen(navController, nivelId)
        }

        // Tienda
        composable(Screen.Store.route) {
            StoreScreen(
                navController = navController,
                storeViewModel = storeViewModel,
                userViewModel = userViewModel
            )
        }
    }
}