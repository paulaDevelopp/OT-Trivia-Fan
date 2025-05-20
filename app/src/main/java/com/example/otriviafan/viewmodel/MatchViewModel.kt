package com.example.otriviafan.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.Match
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

    fun createMatch(userId: String, context: Context, nivelId: Int) {
        viewModelScope.launch {
            val questions = repository.getQuestionsForMultiplayerLevel(nivelId)
            val matchId = repository.createMatchWithQuestions(userId, nivelId, questions)

            repository.observeMatch(matchId) { updated ->
                handleMatchUpdate(updated)
            }
        }
    }

    fun joinMatch(userId: String, context: Context, nivelId: Int) {
        viewModelScope.launch {
            val matchId = repository.joinOrCreateMatch(userId, context, nivelId)

            repository.observeMatch(matchId) { updated ->
                handleMatchUpdate(updated)
            }
        }
    }

    private fun handleMatchUpdate(updated: Match) {
        // Solo actualiza si cambia la pregunta, para evitar múltiples reinicios de UI
        if (updated.currentQuestionIndex != lastHandledQuestionIndex) {
            lastHandledQuestionIndex = updated.currentQuestionIndex
            setMatch(updated)
        } else {
            // Aun si no cambió, refrescamos el valor para mantenerlo sincronizado
            _match.value = updated
        }
    }
}
