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

    private val _vidas = MutableStateFlow(1) // Solo una vida permitida
    val vidas: StateFlow<Int> = _vidas

    private val _vidasAgotadas = MutableStateFlow(false)
    val vidasAgotadas: StateFlow<Boolean> = _vidasAgotadas

    private val _nivelSuperado = MutableStateFlow(false)
    val nivelSuperado: StateFlow<Boolean> = _nivelSuperado

    private val numeroPreguntas = 5
    private var vidaUsada = false
    private var falloYaCometido = false
    private val usedQuestionIds = mutableSetOf<Int>()

    fun iniciarNivel(nivel: Int) {
        resetEstado()

        viewModelScope.launch {
            val firebaseQuestions = repository.getQuestionsByLevelIndex(nivel)
                .filterNot { it.id in usedQuestionIds }
                .shuffled()
                .take(numeroPreguntas)

            _questions.value = firebaseQuestions
            usedQuestionIds.addAll(firebaseQuestions.map { it.id })

            if (firebaseQuestions.isNotEmpty()) {
                _answers.value = firebaseQuestions.first().answers.shuffled()
            }
        }
    }

    private fun resetEstado() {
        _vidas.value = 1
        _score.value = 0
        _currentQuestionIndex.value = 0
        _nivelSuperado.value = false
        _vidasAgotadas.value = false
        vidaUsada = false
        falloYaCometido = false
        usedQuestionIds.clear()
    }

    fun responder(answer: AnswerEntity) {
        val preguntaActual = questions.value.getOrNull(currentQuestionIndex.value) ?: return
        val esCorrecta = preguntaActual.correctAnswerId == answer.id

        if (esCorrecta) {
            _score.value += 4
        } else {
            manejarFallo()
            if (_vidasAgotadas.value) return
        }

        avanzarPregunta()
    }

    private fun manejarFallo() {
        if (!falloYaCometido) {
            if (_score.value >= 20 && !vidaUsada) {
                _score.value -= 20
                vidaUsada = true
            } else {
                falloYaCometido = true
                _vidas.value = 0
                _vidasAgotadas.value = true
            }
        } else {
            _vidas.value = 0
            _vidasAgotadas.value = true
        }
    }

    private fun avanzarPregunta() {
        val siguienteIndex = _currentQuestionIndex.value + 1
        if (siguienteIndex < questions.value.size) {
            _currentQuestionIndex.value = siguienteIndex
            _answers.value = questions.value[siguienteIndex].answers.shuffled()
        } else {
            if (!falloYaCometido || vidaUsada) {
                _nivelSuperado.value = true
            }
        }
    }
}
