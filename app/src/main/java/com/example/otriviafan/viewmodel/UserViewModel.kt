package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.WallpaperItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val repository = Repository()
    private val auth = FirebaseAuth.getInstance()

    private val _highestLevelUnlocked = MutableStateFlow(1)
    val highestLevelUnlocked: StateFlow<Int> = _highestLevelUnlocked

    private val _points = MutableStateFlow(0)
    val points: StateFlow<Int> = _points

    private val _availableWallpapers = MutableStateFlow<List<WallpaperItem>>(emptyList())
    val availableWallpapers: StateFlow<List<WallpaperItem>> = _availableWallpapers

    private val _purchasedWallpapers = MutableStateFlow<List<String>>(emptyList())
    val purchasedWallpapers: StateFlow<List<String>> = _purchasedWallpapers

    private val _unlockedWallpapers = MutableStateFlow<List<String>>(emptyList())
    val unlockedWallpapers: StateFlow<List<String>> = _unlockedWallpapers

    init {
        loadDataIfLoggedIn()
    }

    private fun loadDataIfLoggedIn() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _highestLevelUnlocked.value = repository.getUserLevel(userId)
            _points.value = repository.getUserPoints(userId)
            _purchasedWallpapers.value = repository.getUserWallpaperPurchases(userId)
            _unlockedWallpapers.value = repository.getUnlockedWallpapers(userId)
            _availableWallpapers.value = loadAndSortWallpapers()
        }
    }

    fun refreshUserData() = loadDataIfLoggedIn()

    fun refreshLevel() = loadDataIfLoggedIn()

    fun reloadWallpapers() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _purchasedWallpapers.value = repository.getUserWallpaperPurchases(userId)
            _unlockedWallpapers.value = repository.getUnlockedWallpapers(userId)
            _availableWallpapers.value = loadAndSortWallpapers()
        }
    }

    fun buyWallpaper(wallpaper: WallpaperItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.buyWallpaper(userId, wallpaper)
                _points.value = repository.getUserPoints(userId)
                _purchasedWallpapers.value = repository.getUserWallpaperPurchases(userId)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    private suspend fun loadAndSortWallpapers(): List<WallpaperItem> {
        val allWallpapers = repository.getAllWallpapers()
        return allWallpapers.sortedWith(
            compareBy(
                { when (it.difficulty) {
                    "easy" -> 0
                    "medium" -> 1
                    "difficult" -> 2
                    else -> 3
                }},
                { extractLevelNumber(it.filename) }
            )
        )
    }

    private fun extractLevelNumber(filename: String): Int {
        return Regex("""level(\d+)""")
            .find(filename)
            ?.groupValues?.get(1)
            ?.toIntOrNull() ?: Int.MAX_VALUE
    }
}
