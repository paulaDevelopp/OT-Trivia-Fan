package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.WallpaperItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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

    private val _savedWallpapers = MutableStateFlow<Set<String>>(emptySet())
    val savedWallpapers: StateFlow<Set<String>> = _savedWallpapers

    fun markWallpaperAsSaved(filename: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val db = FirebaseDatabase.getInstance().reference
                val savedRef = db.child("users").child(uid).child("savedWallpapers")
                savedRef.child(filename).setValue(true).await()
                _savedWallpapers.value = _savedWallpapers.value + filename
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        auth.currentUser?.uid?.let { loadUserDataFor(it) }
    }

    //Carga toda la información del usuario
    fun loadUserDataFor(uid: String) {
        viewModelScope.launch {
            val userLevelName = repository.getUserLevel(uid)
            val allLevels = repository.getAllLevelNamesOrdered()
            val levelIndex = allLevels.indexOf(userLevelName) + 1
            _highestLevelUnlocked.value = levelIndex
            _points.value = repository.getUserPoints(uid)
            _purchasedWallpapers.value = repository.getUserWallpaperPurchases(uid)
            _unlockedWallpapers.value = repository.getUnlockedWallpapers(uid)
            _availableWallpapers.value = loadAndSortWallpapersFor(uid)

            // Cargar fondos guardados
            val db = FirebaseDatabase.getInstance().reference
            val savedSnapshot = db.child("users").child(uid).child("savedWallpapers").get().await()
            val savedList = savedSnapshot.children.mapNotNull { it.key }.toSet()
            _savedWallpapers.value = savedList
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

    //Recarga toda la información del usuario actual
    fun refreshUserData() {
        auth.currentUser?.uid?.let { loadUserDataFor(it) }
    }

    //Recarga solo la información relacionada con wallpapers
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
              //  _points.value = repository.getUserPoints(uid)
                _purchasedWallpapers.value = repository.getUserWallpaperPurchases(uid)
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    suspend fun getNivelProgreso(userId: String): Map<String, NivelProgreso> {
        return repository.getNivelProgreso(userId)
    }

    fun gastarPuntos(cantidad: Int, onComplete: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.spendPoints(cantidad)
               // _points.value = repository.getUserPoints(uid)
                onComplete()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun marcarNivelComoCompletado(levelName: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.marcarNivelCompletado(uid, levelName)
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
