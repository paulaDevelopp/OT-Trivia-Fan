package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
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

    /*Procesa la respuesta del usuario.
    Suma puntos si es correcta, resta vidas si no.
    Marca el nivel como completado si se responden todas las preguntas o se acaban las vidas.*/
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
                _levelCompleted.value = true
            } else {
                _currentQuestionIndex.value += 1
            }
        }
    }

    //Si el nivel no es multijugador, lo marca como desbloqueado y guarda el progreso.
    fun finishLevel() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            val yaCompletado = repository.verificarNivelCompletado(userId, levelName)
            if (yaCompletado) {
                _levelCompleted.value = true
                _userDataShouldRefresh.value = true
                return@launch
            }

            val hizoPerfecta = _score.value == (numeroPreguntas * puntosPorPregunta) && !huboError
            val perfectoConReintento = _score.value == (numeroPreguntas * puntosPorPregunta) && huboError && usadoReintento

            val esMultijugador = repository.esNivelMultijugador(levelName)

            if (hizoPerfecta || perfectoConReintento) {
                repository.addPoints(_score.value)

                if (hizoPerfecta) _partidaPerfecta.value = true

                if (!esMultijugador) {
                    val currentLevelName = repository.getUserLevel(userId)
                    val levels = repository.getAllLevelNamesOrdered()

                    val currentIndex = levels.indexOfFirst { it.trim().equals(currentLevelName.trim(), ignoreCase = true) }
                    val playingIndex = levels.indexOfFirst { it.trim().equals(levelName.trim(), ignoreCase = true) }

                    if (currentIndex != -1 && playingIndex >= currentIndex) {
                        repository.saveUserLevel(userId, levelName)
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

            repository.marcarNivelCompletado(userId, levelName)

            _userDataShouldRefresh.value = true
            _levelCompleted.value = true
            usedQuestionIds.clear()
        }
    }

    /*Intenta gastar 5 puntos del usuario para reintentar si ya perdió.
    Solo permite un reintento por partida.*/
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

    //Verifica si el jugador tiene al menos 5 puntos y no ha usado reintento.
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

    /*Escucha los cambios en las preguntas del nivel en tiempo real desde Firestore.

    Filtra preguntas no repetidas, selecciona aleatoriamente 5 y reinicia el estado de la partida.*/
    fun observeQuestionsRealtime() {
        repository.observeQuestionsForLevel(levelName) { allQuestions ->
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

}