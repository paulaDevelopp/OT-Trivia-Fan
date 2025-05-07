package com.example.otriviafan.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object SinglePlayer : Screen("single_player")
    object Profile : Screen("profile")
    object MultiPlayerEntry : Screen("multiplayer_entry")
    object MultiPlayerWaiting : Screen("multiplayer_waiting")
    object MultiPlayerJoin : Screen("multiplayer_join")
    object MultiPlayerGame : Screen("multiplayer_game")
    object MultiPlayerResult : Screen("multiplayer_result")
    object Store : Screen("store")

    // 🚀 FALTABA ESTO:
    object LevelSelect : Screen("level_select")

    // Nivel jugable con parámetro
    object LevelGame : Screen("level_game/{levelId}") {
        fun createRoute(levelId: Int) = "level_game/$levelId"
    }
}
