package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.QuestionWithAnswers
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LevelGameViewModel(
    private val repository: Repository,
    private val selectedLevel: Int
) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuestionWithAnswers>>(emptyList())
    val questions: StateFlow<List<QuestionWithAnswers>> = _questions

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives

    private val _levelCompleted = MutableStateFlow(false)
    val levelCompleted: StateFlow<Boolean> = _levelCompleted

    private val _outOfLives = MutableStateFlow(false)
    val outOfLives: StateFlow<Boolean> = _outOfLives

    private val _perfectStreak = MutableStateFlow(0)
    val perfectStreak: StateFlow<Int> = _perfectStreak

    private val _partidaPerfecta = MutableStateFlow(false)
    val partidaPerfecta: StateFlow<Boolean> = _partidaPerfecta

    private val _nivelSubido = MutableStateFlow(false)
    val nivelSubido: StateFlow<Boolean> = _nivelSubido

    private val _mostrarSubidaDeNivelDesdeNivel1 = MutableStateFlow(false)
    val mostrarSubidaDeNivelDesdeNivel1: StateFlow<Boolean> = _mostrarSubidaDeNivelDesdeNivel1

    private val _userDataShouldRefresh = MutableStateFlow(false)
    val userDataShouldRefresh: StateFlow<Boolean> = _userDataShouldRefresh

    private var partidaInvalida = false

    fun loadQuestions() {
        viewModelScope.launch {
            val allQuestions = repository.getQuestionsByLevelIndex(selectedLevel)
            _questions.value = allQuestions.shuffled().take(15)
            _currentQuestionIndex.value = 0
            _score.value = 0
            _lives.value = 3
            _outOfLives.value = false
            _levelCompleted.value = false
            partidaInvalida = false
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                _perfectStreak.value = repository.getPerfectStreakCount(userId)
            }
        }
    }


    fun submitAnswer(isCorrect: Boolean) {
        viewModelScope.launch {
            if (isCorrect) {
                _score.value += 1
            } else {
                _lives.value -= 1
                partidaInvalida = true
            }

            if (_lives.value <= 0) {
                _outOfLives.value = true
            } else if (_currentQuestionIndex.value + 1 >= 15) {
                finishLevel()
            } else {
                _currentQuestionIndex.value += 1
            }
        }
    }

    fun finishLevel() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val subeNivel = _score.value == 15 && _lives.value > 0

            val highestUnlocked = repository.getUserLevel(userId)
            if (subeNivel && selectedLevel == highestUnlocked) {
                repository.incrementUserLevel(userId)
                _nivelSubido.value = true
                if (selectedLevel == 1) {
                    _mostrarSubidaDeNivelDesdeNivel1.value = true
                }
            }

            if (_lives.value > 0) {
                repository.addPoints(_score.value)
            }

            if (!subeNivel) {
                repository.saveUserLevel(userId, selectedLevel)
            }

            _userDataShouldRefresh.value = true
            _levelCompleted.value = true
        }
    }

    suspend fun retryUsingPoints(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        if (repository.hasUsedRetryForLevel(userId, selectedLevel)) return false
        val points = repository.getUserPoints(userId)

        return if (points >= 20) {
            repository.spendPoints(20)
            repository.markRetryUsed(userId, selectedLevel)
            _lives.value = 1
            _outOfLives.value = false
            true
        } else {
            false
        }
    }

    suspend fun canRetryWithPoints(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val hasUsed = repository.hasUsedRetryForLevel(userId, selectedLevel)
        val points = repository.getUserPoints(userId)
        return !hasUsed && points >= 20
    }

    fun clearFeedbackFlags() {
        _partidaPerfecta.value = false
        _nivelSubido.value = false
        _mostrarSubidaDeNivelDesdeNivel1.value = false
    }

    fun setRefreshHandled() {
        _userDataShouldRefresh.value = false
    }

    companion object {
        fun Factory(selectedLevel: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LevelGameViewModel(Repository(), selectedLevel) as T
            }
        }
    }


}