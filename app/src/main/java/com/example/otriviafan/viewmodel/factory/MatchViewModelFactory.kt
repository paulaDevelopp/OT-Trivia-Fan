package com.example.otriviafan.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.otriviafan.data.Repository
import com.example.otriviafan.viewmodel.MatchViewModel

class MatchViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MatchViewModel(repository) as T
    }
}
