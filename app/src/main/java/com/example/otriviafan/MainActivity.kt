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
import com.example.otriviafan.ui.screens.multiplayer.MultiPlayerEntryScreen
import com.example.otriviafan.ui.screens.multiplayer.MultiPlayerGameScreen
import com.example.otriviafan.ui.screens.multiplayer.MultiPlayerJoinScreen
import com.example.otriviafan.ui.screens.multiplayer.MultiPlayerWaitingScreen
import com.example.otriviafan.viewmodel.MatchViewModel
import com.example.otriviafan.viewmodel.StoreViewModel
import com.example.otriviafan.viewmodel.UserViewModel
import com.example.otriviafan.viewmodel.factory.MatchViewModelFactory
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
    val matchViewModel: MatchViewModel = viewModel(factory = MatchViewModelFactory(repository))
    val storeViewModel: StoreViewModel = viewModel(factory = StoreViewModelFactory(repository))
    val userViewModel: UserViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        // Pantallas iniciales
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController, userViewModel) }
        composable(Screen.Register.route) { RegisterScreen(navController, userViewModel) }
        composable(Screen.Home.route) { HomeScreen(navController) }

        // Perfil del usuario
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }

        // Pantalla principal: mapa de niveles
        composable(Screen.LevelMap.route) {
            LevelMapScreen(navController = navController, userViewModel = userViewModel)
        }

        // Juego individual
        composable("${Screen.SinglePlayer.route}/{nivel}") { backStackEntry ->
            val nivel = backStackEntry.arguments?.getString("nivel")?.toIntOrNull() ?: 1
            SinglePlayerScreen(navController = navController, nivelSeleccionado = nivel)
        }

        composable("multiplayer_entry/{nivelId}") { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getString("nivelId")?.toIntOrNull() ?: 1
            MultiPlayerEntryScreen(navController, nivelId)
        }

        composable("multiplayer_waiting/{nivelId}") { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getString("nivelId")?.toIntOrNull() ?: 1
            MultiPlayerWaitingScreen(navController, matchViewModel, nivelId)
        }

        composable("multiplayer_join/{nivelId}") { backStackEntry ->
            val nivelId = backStackEntry.arguments?.getString("nivelId")?.toIntOrNull() ?: 1
            MultiPlayerJoinScreen(navController, matchViewModel, nivelId)
        }

        composable(Screen.MultiPlayerGame.route) {
            MultiPlayerGameScreen(navController, matchViewModel)
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