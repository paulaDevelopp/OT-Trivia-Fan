package com.example.otriviafan.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.otriviafan.data.Repository
import com.example.otriviafan.viewmodel.SinglePlayerViewModel
class SinglePlayerViewModelFactory(
    private val levelName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SinglePlayerViewModel::class.java)) {
            return SinglePlayerViewModel(Repository(), levelName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
