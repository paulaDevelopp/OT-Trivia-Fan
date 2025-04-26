package com.example.otriviafan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.otriviafan.navigation.Screen
import com.example.otriviafan.ui.screens.HomeScreen
import com.example.otriviafan.ui.screens.LoginScreen
import com.example.otriviafan.ui.screens.ProfileScreen
import com.example.otriviafan.ui.screens.RegisterScreen
import com.example.otriviafan.ui.screens.SinglePlayerScreen
import com.example.otriviafan.ui.screens.SplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otriviafan.viewmodel.MatchViewModel
import com.example.otriviafan.viewmodel.MatchViewModelFactory
import com.example.otriviafan.data.Repository
import com.example.otriviafan.ui.screens.*
import com.example.otriviafan.viewmodel.StoreViewModel
import com.example.otriviafan.viewmodel.StoreViewModelFactory

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
    val matchViewModel: MatchViewModel = viewModel(factory = MatchViewModelFactory(Repository()))
    val storeViewModel: StoreViewModel = viewModel(factory = StoreViewModelFactory(Repository())) // ✅

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.SinglePlayer.route) { SinglePlayerScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }

        // MULTIJUGADOR
        composable(Screen.MultiPlayerEntry.route) { MultiPlayerEntryScreen(navController) }
        composable(Screen.MultiPlayerWaiting.route) { MultiPlayerWaitingScreen(navController, matchViewModel) }
        composable(Screen.MultiPlayerJoin.route) { MultiPlayerJoinScreen(navController, matchViewModel) }
        composable(Screen.MultiPlayerGame.route) { MultiPlayerGameScreen(navController, matchViewModel) }
        composable(Screen.MultiPlayerResult.route) { MultiPlayerResultScreen(navController, matchViewModel) }

        // TIENDA
        composable(Screen.Store.route) { StoreScreen(navController, storeViewModel) }
    }
}
