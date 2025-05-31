package com.example.otriviafan.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.Match
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchViewModel(private val repository: Repository) : ViewModel() {

    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match

    private var lastHandledQuestionIndex = -1

    private fun setMatch(updated: Match) {
        _match.value = updated.copy()
    }

    fun createMatch(userId: String, context: Context, levelName: String) {
        viewModelScope.launch {
            val questions = repository.getQuestionsForMultiplayerLevel(levelName)
            val matchId = repository.createMatchWithQuestions(userId, levelName, questions)

            // Espera pequeña antes de empezar a escuchar (para asegurar persistencia en Firebase)
            delay(500)

            repository.observeMatch(matchId) { updated ->
                handleMatchUpdate(updated)
            }
        }
    }


    fun joinMatch(userId: String, context: Context, levelName: String) {
        viewModelScope.launch {
            val matchId = repository.joinOrCreateMatch(userId, context, levelName)

            repository.observeMatch(matchId) { updated ->
                handleMatchUpdate(updated)
            }
        }
    }

    private fun handleMatchUpdate(updated: Match) {
        val current = _match.value
        if (updated.currentQuestionIndex != lastHandledQuestionIndex ||
            updated.answered != current?.answered ||
            updated.status != current?.status) {
            lastHandledQuestionIndex = updated.currentQuestionIndex
            _match.value = updated.copy()
        }
    }
}
