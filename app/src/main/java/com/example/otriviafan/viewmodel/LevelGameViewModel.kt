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

    // Configuración básica
    private val numeroPreguntas = 5
    private val puntosPorPregunta = 2

    // Estado general del juego
    private val _questions = MutableStateFlow<List<QuestionWithAnswers>>(emptyList())
    val questions: StateFlow<List<QuestionWithAnswers>> = _questions

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives

    // Estados del progreso del nivel
    private val _levelCompleted = MutableStateFlow(false)
    val levelCompleted: StateFlow<Boolean> = _levelCompleted

    private val _outOfLives = MutableStateFlow(false)
    val outOfLives: StateFlow<Boolean> = _outOfLives

    // Datos de usuario (como racha perfecta)
    private val _perfectStreak = MutableStateFlow(0)
    val perfectStreak: StateFlow<Int> = _perfectStreak

    // Feedback visual al jugador
    private val _partidaPerfecta = MutableStateFlow(false)
    val partidaPerfecta: StateFlow<Boolean> = _partidaPerfecta

    private val _nivelSubido = MutableStateFlow(false)
    val nivelSubido: StateFlow<Boolean> = _nivelSubido

    private val _mostrarSubidaDeNivelDesdeNivel1 = MutableStateFlow(false)
    val mostrarSubidaDeNivelDesdeNivel1: StateFlow<Boolean> = _mostrarSubidaDeNivelDesdeNivel1

    private val _userDataShouldRefresh = MutableStateFlow(false)
    val userDataShouldRefresh: StateFlow<Boolean> = _userDataShouldRefresh

    // Control de lógica de partida
    private var usadoReintento = false
    private var huboError = false
    private val usedQuestionIds = mutableSetOf<Int>() // IDs ya usados en esta partida

    // Cargar las preguntas de Firebase (filtrando las ya usadas)
    fun loadQuestions() {
        viewModelScope.launch {
            val allQuestions = repository.getQuestionsByLevelIndex(selectedLevel)

            if (allQuestions.isEmpty()) {
                // Si no hay preguntas en Firestore para este nivel
                _questions.value = emptyList()
                _outOfLives.value = true // Esto activará un mensaje en la UI (por ejemplo: "nivel no disponible")
                return@launch
            }

            val preguntasNoRepetidas = allQuestions.filterNot { usedQuestionIds.contains(it.id) }

            val seleccionadas = preguntasNoRepetidas.shuffled().take(numeroPreguntas)
            _questions.value = seleccionadas
            usedQuestionIds.addAll(seleccionadas.map { it.id })

            // Reinicio del estado del juego
            _currentQuestionIndex.value = 0
            _score.value = 0
            _lives.value = 1
            _outOfLives.value = false
            _levelCompleted.value = false
            usadoReintento = false
            huboError = false
        }
    }


    // Enviar respuesta y actualizar estado del juego
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

    // Lógica de finalización del nivel
    fun finishLevel() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val hizoPartidaPerfecta = _score.value == (numeroPreguntas * puntosPorPregunta) && !huboError && _lives.value > 0
            val falloPeroUsoReintentoYSeRecupero = _score.value == (numeroPreguntas * puntosPorPregunta) && huboError && usadoReintento && _lives.value > 0

            if (hizoPartidaPerfecta || falloPeroUsoReintentoYSeRecupero) {
                repository.addPoints(_score.value)
                if (hizoPartidaPerfecta) {
                    _partidaPerfecta.value = true
                }

                val highestUnlocked = repository.getUserLevel(userId)
                if (selectedLevel == highestUnlocked) {
                    repository.incrementUserLevel(userId)
                    _nivelSubido.value = true
                    if (selectedLevel == 1) {
                        _mostrarSubidaDeNivelDesdeNivel1.value = true
                    }

                    // Desbloquear wallpaper asociado
                    val orderedDocs = repository.getOrderedLevelNames()
                    val levelName = orderedDocs.getOrNull(selectedLevel - 1)?.first
                    if (levelName != null) {
                        repository.unlockWallpaperForLevel(userId, levelName)
                    }
                }

            } else {
                // Guarda el nivel actual aunque no haya sido perfecto
                repository.saveUserLevel(userId, selectedLevel)
            }

            // Refrescar datos de usuario y cerrar la partida
            _userDataShouldRefresh.value = true
            _levelCompleted.value = true
            usedQuestionIds.clear() // Reinicio para permitir nueva partida limpia
        }
    }

    // Reintento usando puntos del usuario (solo una vez)
    suspend fun retryUsingPoints(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        if (usadoReintento) return false
        val points = repository.getUserPoints(userId)

        return if (points >= 20) {
            repository.spendPoints(20)
            _lives.value = 1
            _outOfLives.value = false
            usadoReintento = true
            true
        } else {
            false
        }
    }

    // Verifica si se puede reintentar (por puntos y si ya se usó)
    suspend fun canRetryWithPoints(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val points = repository.getUserPoints(userId)
        return !usadoReintento && points >= 20
    }

    // Limpia indicadores de feedback tras confirmación del usuario
    fun clearFeedbackFlags() {
        _partidaPerfecta.value = false
        _nivelSubido.value = false
        _mostrarSubidaDeNivelDesdeNivel1.value = false
    }

    // Marca los datos como actualizados para evitar recarga innecesaria
    fun setRefreshHandled() {
        _userDataShouldRefresh.value = false
    }

    // Fábrica para inyectar el nivel seleccionado
    companion object {
        fun Factory(selectedLevel: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LevelGameViewModel(Repository(), selectedLevel) as T
            }
        }
    }
}
