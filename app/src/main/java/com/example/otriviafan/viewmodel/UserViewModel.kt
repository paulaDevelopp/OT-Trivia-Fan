package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.WallpaperItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
        auth.currentUser?.uid?.let { loadUserDataFor(it) }
    }

    fun loadUserDataFor(uid: String) {
        viewModelScope.launch {
            _highestLevelUnlocked.value = repository.getUserLevel(uid)
            _points.value = repository.getUserPoints(uid)
            _purchasedWallpapers.value = repository.getUserWallpaperPurchases(uid)
            _unlockedWallpapers.value = repository.getUnlockedWallpapers(uid)
            _availableWallpapers.value = loadAndSortWallpapersFor(uid)
        }
    }

    private suspend fun loadAndSortWallpapersFor(uid: String): List<WallpaperItem> {
        val userLevel = repository.getUserLevel(uid)
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

    fun refreshUserData() {
        auth.currentUser?.uid?.let { loadUserDataFor(it) }
    }

    fun reloadWallpapers() {
        auth.currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                _purchasedWallpapers.value = repository.getUserWallpaperPurchases(uid)
                _unlockedWallpapers.value = repository.getUnlockedWallpapers(uid)
                _availableWallpapers.value = loadAndSortWallpapersFor(uid)
            }
        }
    }

    fun buyWallpaper(wallpaper: WallpaperItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.buyWallpaper(uid, wallpaper)
                _points.value = repository.getUserPoints(uid)
                _purchasedWallpapers.value = repository.getUserWallpaperPurchases(uid)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    suspend fun getNivelProgreso(userId: String): Map<Int, NivelProgreso> {
        return repository.getNivelProgreso(userId)
    }

    fun gastarPuntos(cantidad: Int, onComplete: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.spendPoints(cantidad)
                _points.value = repository.getUserPoints(uid)
                onComplete()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun marcarNivelComoCompletado(nivelId: Int, tipo: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.marcarNivelCompletado(uid, nivelId, tipo)
            _unlockedWallpapers.value = repository.getUnlockedWallpapers(uid)
            _availableWallpapers.value = loadAndSortWallpapersFor(uid)
        }
    }

    fun getUserId(): String = auth.currentUser?.uid.orEmpty()

    data class NivelProgreso(
        val completado: Boolean = false,
        val tipo: String = "individual"
    )
}
