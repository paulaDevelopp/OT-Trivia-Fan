package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.WallpaperItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class StoreViewModel(private val repository: Repository) : ViewModel() {

  /*  private val _storeItems = MutableStateFlow<List<StoreItem>>(emptyList())
    val storeItems: StateFlow<List<StoreItem>> = _storeItems
*/
    private val _userPurchases = MutableStateFlow<List<String>>(emptyList())
    val userPurchases: StateFlow<List<String>> = _userPurchases

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage
    private val _availableWallpapers = MutableStateFlow<List<WallpaperItem>>(emptyList())
    val availableWallpapers: StateFlow<List<WallpaperItem>> = _availableWallpapers

    private var highestLevelUnlocked: Int = 1

 /*   fun loadStoreItemsFiltered(userId: String) {
        viewModelScope.launch {
            try {
                highestLevelUnlocked = repository.getUserLevel(userId)
                val wallpapers = repository.getAvailableWallpapersForUserLevel(highestLevelUnlocked)
                _storeItems.value = wallpapers.map {
                    StoreItem(
                        id = it.filename,
                        imageUrl = it.url,
                        price = it.price,
                        difficulty = it.difficulty,
                        level = 1 // O ajusta si decides guardar nivel por wallpaper
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
*/
    fun loadUserPurchases(userId: String) {
        viewModelScope.launch {
            try {
                _userPurchases.value = repository.getUserWallpaperPurchases(userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

 /*   fun buyItem(userId: String, item: StoreItem) {
        viewModelScope.launch {
            try {
                repository.buyWallpaper(userId, item.toWallpaperItem())
                _successMessage.value = "¡Compra exitosa!"
                loadUserPurchases(userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }*/

    fun loadAvailableWallpapers() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val wallpapers = repository.getAllWallpapers()
                _availableWallpapers.value = wallpapers
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun refreshUserPurchases() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        loadUserPurchases(userId)
    }

    fun setError(message: String) {
        _error.value = message
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }

   /* private fun StoreItem.toWallpaperItem() = com.example.otriviafan.data.model.WallpaperItem(
        filename = this.id,
        url = this.imageUrl,
        difficulty = this.difficulty,
        price = this.price
    )*/
}
