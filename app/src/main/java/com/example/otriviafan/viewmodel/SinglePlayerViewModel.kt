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
class SinglePlayerViewModel(
    private val repository: Repository,
    private val levelName: String
) : ViewModel() {

    private val numeroPreguntas = 5
    private val puntosPorPregunta = 2

    private val _questions = MutableStateFlow<List<QuestionWithAnswers>>(emptyList())
    val questions: StateFlow<List<QuestionWithAnswers>> = _questions

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _lives = MutableStateFlow(1)
    val lives: StateFlow<Int> = _lives

    private val _levelCompleted = MutableStateFlow(false)
    val levelCompleted: StateFlow<Boolean> = _levelCompleted

    private val _outOfLives = MutableStateFlow(false)
    val outOfLives: StateFlow<Boolean> = _outOfLives

    private val _visibleLives = MutableStateFlow(1)
    val visibleLives: StateFlow<Int> = _visibleLives

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

    private var usadoReintento = false
    private var huboError = false
    private val usedQuestionIds = mutableSetOf<Int>()

    fun loadQuestions() {
        viewModelScope.launch {
            val allQuestions = repository.getQuestionsForLevel(levelName)

            if (allQuestions.isEmpty()) {
                _questions.value = emptyList()
                _outOfLives.value = true
                return@launch
            }

            val preguntasNoRepetidas = allQuestions.filterNot { usedQuestionIds.contains(it.id) }

            val seleccionadas = preguntasNoRepetidas.shuffled().take(numeroPreguntas)
            _questions.value = seleccionadas
            usedQuestionIds.addAll(seleccionadas.map { it.id })

            _currentQuestionIndex.value = 0
            _score.value = 0
            _lives.value = 1
            _outOfLives.value = false
            _levelCompleted.value = false
            usadoReintento = false
            huboError = false
        }
    }

    fun submitAnswer(isCorrect: Boolean) {
        viewModelScope.launch {
            if (isCorrect) {
                _score.value += puntosPorPregunta
            } else {
                _lives.value -= 1
                huboError = true
            }

            if (_lives.value <= 0) {
                _outOfLives.value = true
            } else if (_currentQuestionIndex.value + 1 >= numeroPreguntas) {
                finishLevel()
            } else {
                _currentQuestionIndex.value += 1
            }
        }
    }

    fun finishLevel() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val hizoPerfecta = _score.value == (numeroPreguntas * puntosPorPregunta) && !huboError
            val perfectoConReintento = _score.value == (numeroPreguntas * puntosPorPregunta) && huboError && usadoReintento

            val esMultijugador = repository.esNivelMultijugador(levelName)

            if (hizoPerfecta || perfectoConReintento) {
                repository.addPoints(_score.value)

                if (hizoPerfecta) _partidaPerfecta.value = true

                if (!esMultijugador) {
                    val currentLevelName = repository.getUserLevel(userId)
                    val levels = repository.getAllLevelNamesOrdered()

                    if (currentLevelName == levelName) {
                        repository.incrementUserLevel(userId)
                        _nivelSubido.value = true
                        if (levelName.contains("level1")) {
                            _mostrarSubidaDeNivelDesdeNivel1.value = true
                        }
                    }

                    repository.unlockWallpaperForLevel(userId, levelName)
                }

            } else if (!esMultijugador) {
                repository.saveUserLevel(userId, levelName)
            }

            _userDataShouldRefresh.value = true
            _levelCompleted.value = true
            usedQuestionIds.clear()
        }
    }

    suspend fun retryUsingPoints(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        if (usadoReintento) return false
        val points = repository.getUserPoints(userId)

        return if (points >= 5) {
            repository.spendPoints(5)
            usadoReintento = true
            _lives.value = 1
            _visibleLives.value = 0
            _outOfLives.value = false
            true
        } else false
    }

    suspend fun canRetryWithPoints(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val points = repository.getUserPoints(userId)
        return !usadoReintento && points >= 5
    }

    fun clearFeedbackFlags() {
        _partidaPerfecta.value = false
        _nivelSubido.value = false
        _mostrarSubidaDeNivelDesdeNivel1.value = false
    }

    fun setRefreshHandled() {
        _userDataShouldRefresh.value = false
    }

    fun setLives(value: Int) {
        _lives.value = value
    }

    companion object {
        fun Factory(levelName: String) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SinglePlayerViewModel(Repository(), levelName) as T
            }
        }
    }
}
