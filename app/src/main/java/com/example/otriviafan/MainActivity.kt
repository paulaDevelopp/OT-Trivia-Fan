package com.example.otriviafan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.otriviafan.data.Repository
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.screens.*
import com.example.otriviafan.ui.screens.multiplayer.*
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

        // Pantallas principales
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }

        // Selección de niveles
        composable(Screen.LevelSelect.route) {
            val userViewModel: UserViewModel = viewModel()
            val nivelesDesbloqueados by userViewModel.highestLevelUnlocked.collectAsState()

            LaunchedEffect(Unit) {
                userViewModel.refreshUserData()
            }

            LevelSelectScreen(
                navController = navController,
                highestLevelUnlocked = nivelesDesbloqueados
            )
        }


        // Juego individual con nivel
        composable("${Screen.SinglePlayer.route}/{nivel}") { backStackEntry ->
            val nivel = backStackEntry.arguments?.getString("nivel")?.toIntOrNull() ?: 1
            SinglePlayerScreen(navController = navController, nivelSeleccionado = nivel)
        }

        // Modo multijugador
        composable(Screen.MultiPlayerEntry.route) { MultiPlayerEntryScreen(navController) }
        composable(Screen.MultiPlayerWaiting.route) { MultiPlayerWaitingScreen(navController, matchViewModel) }
        composable(Screen.MultiPlayerJoin.route) { MultiPlayerJoinScreen(navController, matchViewModel) }
        composable(Screen.MultiPlayerGame.route) { MultiPlayerGameScreen(navController, matchViewModel) }
        composable(Screen.MultiPlayerResult.route) { MultiPlayerResultScreen(navController, matchViewModel) }

        // Tienda
        composable(Screen.Store.route) {
            StoreScreen(navController = navController, storeViewModel = storeViewModel)
        }
    }
}
