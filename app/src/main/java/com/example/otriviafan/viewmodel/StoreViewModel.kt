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
    init {
        observeAvailableWallpapersRealtime()
        observeUserPurchasesRealtime()
    }

    private val _userPurchases = MutableStateFlow<List<String>>(emptyList())
    val userPurchases: StateFlow<List<String>> = _userPurchases

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage
    private val _availableWallpapers = MutableStateFlow<List<WallpaperItem>>(emptyList())
    val availableWallpapers: StateFlow<List<WallpaperItem>> = _availableWallpapers

    fun loadUserPurchases(userId: String) {
        viewModelScope.launch {
            try {
                _userPurchases.value = repository.getUserWallpaperPurchases(userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }


    fun observeAvailableWallpapersRealtime() {
        repository.observeWallpapers { wallpapers ->
            _availableWallpapers.value = wallpapers
        }
    }

    fun observeUserPurchasesRealtime() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        repository.observeUserPurchases(userId) { purchases ->
            _userPurchases.value = purchases
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

}
