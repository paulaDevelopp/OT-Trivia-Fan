package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
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

    init {
        loadDataIfLoggedIn()
    }

    private fun loadDataIfLoggedIn() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _highestLevelUnlocked.value = repository.getUserLevel(userId)
            _points.value = repository.getUserPoints(userId)
        }
    }

    fun refreshUserData() = loadDataIfLoggedIn()

    // Si alguna vista sigue llamando esto
    fun refreshLevel() = loadDataIfLoggedIn()
}
