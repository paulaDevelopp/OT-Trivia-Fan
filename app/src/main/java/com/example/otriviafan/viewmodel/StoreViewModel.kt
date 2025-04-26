package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.entities.StoreItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StoreViewModel(private val repository: Repository) : ViewModel() {

    private val _storeItems = MutableStateFlow<List<StoreItem>>(emptyList())
    val storeItems: StateFlow<List<StoreItem>> = _storeItems

    private val _userPurchases = MutableStateFlow<List<String>>(emptyList())
    val userPurchases: StateFlow<List<String>> = _userPurchases

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    // 📦 Cargar los ítems disponibles en la tienda
    fun loadStoreItems() {
        viewModelScope.launch {
            try {
                _storeItems.value = repository.loadStoreItems()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ✅ Cargar los ítems que el usuario ya compró
    fun loadUserPurchases(userId: String) {
        viewModelScope.launch {
            try {
                _userPurchases.value = repository.loadUserPurchases(userId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // 🛒 Comprar un ítem
    fun buyItem(userId: String, item: StoreItem) {
        viewModelScope.launch {
            try {
                repository.buyItem(userId, item.id, item.price)
                _successMessage.value = "¡Compra exitosa!"
                loadUserPurchases(userId) // Recargar compras
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // 🧹 Limpiar mensajes
    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}
