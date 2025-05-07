package com.example.otriviafan.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.otriviafan.data.Repository
import com.example.otriviafan.viewmodel.LevelGameViewModel

class LevelGameViewModelFactory(private val selectedLevel: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LevelGameViewModel(Repository(), selectedLevel) as T
    }
}
