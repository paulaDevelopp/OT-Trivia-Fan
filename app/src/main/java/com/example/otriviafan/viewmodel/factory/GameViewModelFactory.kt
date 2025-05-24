package com.example.otriviafan.viewmodel.factory
/*
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.otriviafan.data.Repository
import com.example.otriviafan.viewmodel.GameViewModel

//fábrica personalizada que se encarga de crear tu GameViewModel con un parámetro (Repository), algo que no se puede hacer directamente con viewModel()
class GameViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = Repository() // ya no pasás db
        return GameViewModel(repository) as T
    }
}
*/