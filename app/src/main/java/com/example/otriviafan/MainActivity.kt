package com.example.otriviafan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
    val repository = Repository()
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

        // Mapa de niveles
        composable(Screen.LevelMap.route) {
            LevelMapScreen(navController = navController, userViewModel = userViewModel)
        }

        // Juego individual
        composable(
            "${Screen.SinglePlayer.route}/{levelName}",
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelName = backStackEntry.arguments?.getString("levelName") ?: "easy_level1"
            SinglePlayerScreen(navController = navController, levelName = levelName)
        }

        // Multijugador con levelName como argumento
        composable(
            "multiplayer_entry/{levelName}",
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelName = backStackEntry.arguments?.getString("levelName") ?: "easy_level1"
            MultiPlayerEntryScreen(navController, levelName)
        }

        composable("multiplayer_waiting/{levelName}",
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelName = backStackEntry.arguments?.getString("levelName") ?: "easy_level1"
            MultiPlayerWaitingScreen(navController, matchViewModel, levelName)
        }

        composable(
            "multiplayer_join/{levelName}",
            arguments = listOf(navArgument("levelName") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelName = backStackEntry.arguments?.getString("levelName") ?: "easy_level1"
            MultiPlayerJoinScreen(navController, matchViewModel, levelName)
        }

        composable(Screen.MultiPlayerGame.route) {
            MultiPlayerGameScreen(navController, matchViewModel)
        }

        composable(Screen.MultiPlayerResult.route) {
            MultiPlayerResultScreen(navController = navController, matchViewModel = matchViewModel)
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
