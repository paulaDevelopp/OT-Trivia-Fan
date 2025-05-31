package com.example.otriviafan.navigation

sealed class Screen(val route: String) {

    // Pantallas principales
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")

    // Juego individual desde el mapa
    object SinglePlayer : Screen("single_player")

    // Modo multijugador clásico (opcional)
   // object MultiPlayerEntry : Screen("multiplayer_entry")
    object MultiPlayerWaiting : Screen("multiplayer_waiting")
    object MultiPlayerJoin : Screen("multiplayer_join")
    object MultiPlayerGame : Screen("multiplayer_game")
    object MultiPlayerResult : Screen("multiplayer_result")

    // Tienda
    object Store : Screen("store")

    // Perfil
    object Profile : Screen("profile")

    // Mapa de niveles unificado
    object LevelMap : Screen("level_map")

    // Ruta para nivel multijugador nuevo con ID (ya está en MainActivity)
    /* object MultiPlayerGameWithId : Screen("multiplayer_game_screen") {
        fun createRoute(nivelId: Int) = "multiplayer_game_screen/$nivelId"
    }*/
}
