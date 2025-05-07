package com.example.otriviafan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.entities.AnswerEntity
import com.example.otriviafan.data.model.QuestionWithAnswers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val repository: Repository) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuestionWithAnswers>>(emptyList())
    val questions: StateFlow<List<QuestionWithAnswers>> = _questions

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _answers = MutableStateFlow<List<AnswerEntity>>(emptyList())
    val answers: StateFlow<List<AnswerEntity>> = _answers

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _vidas = MutableStateFlow(3)
    val vidas: StateFlow<Int> = _vidas

    private val _nivelActual = MutableStateFlow(1)
    val nivelActual: StateFlow<Int> = _nivelActual

    private val _aciertosEnEsteNivel = MutableStateFlow(0)
    val aciertosEnEsteNivel: StateFlow<Int> = _aciertosEnEsteNivel

    private val _fallosConsecutivos = MutableStateFlow(0)
    val fallosConsecutivos: StateFlow<Int> = _fallosConsecutivos

    private val _nivelSuperado = MutableStateFlow(false)
    val nivelSuperado: StateFlow<Boolean> = _nivelSuperado

    private val _vidasAgotadas = MutableStateFlow(false)
    val vidasAgotadas: StateFlow<Boolean> = _vidasAgotadas

    private var usadoReintentar = false
    var puntosJugador = 0

    private val usedQuestionIds = mutableSetOf<Int>()

    val numeroPreguntas = 5;

    fun iniciarNivel(nivel: Int) {
        _nivelActual.value = nivel
        _vidas.value = 3
        _score.value = 0
        _aciertosEnEsteNivel.value = 0
        _fallosConsecutivos.value = 0
        _currentQuestionIndex.value = 0
        _nivelSuperado.value = false
        _vidasAgotadas.value = false
        usadoReintentar = false
        usedQuestionIds.clear()

        viewModelScope.launch {
            val firebaseQuestions = repository.getQuestionsByLevelIndex(nivel)

                .filterNot { it.id in usedQuestionIds }
                .shuffled()
                .take(numeroPreguntas) //preguntas por nivel

            _questions.value = firebaseQuestions
            if (firebaseQuestions.isNotEmpty()) {
                _answers.value = firebaseQuestions[0].answers.shuffled()
            }
        }
    }

    fun responder(answer: AnswerEntity) {
        val correcta = questions.value[currentQuestionIndex.value].correctAnswerId == answer.id

        if (correcta) {
            _score.value += 1
            _aciertosEnEsteNivel.value += 1
            _fallosConsecutivos.value = 0
        } else {
            _fallosConsecutivos.value += 1
            if (_fallosConsecutivos.value == 2) {
                _vidas.value = (_vidas.value - 1).coerceAtLeast(0)
                _fallosConsecutivos.value = 0
                if (_vidas.value == 0) {
                    _vidasAgotadas.value = true
                }
            }
        }

        if (_aciertosEnEsteNivel.value >= numeroPreguntas) {
            _nivelSuperado.value = true
        }
    }

    fun siguientePregunta() {
        _currentQuestionIndex.value += 1
        if (_currentQuestionIndex.value < _questions.value.size) {
            _answers.value = _questions.value[_currentQuestionIndex.value].answers.shuffled()
        }
    }

    fun puedeReintentar(): Boolean {
        return puntosJugador >= 20 && !usadoReintentar
    }

    fun reintentarNivel() {
        if (puedeReintentar()) {
            puntosJugador -= 20
            _vidas.value = 3
            _vidasAgotadas.value = false
            usadoReintentar = true
        }
    }
}
