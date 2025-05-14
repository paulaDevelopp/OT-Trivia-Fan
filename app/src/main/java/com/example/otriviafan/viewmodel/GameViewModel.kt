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

    private val _vidasAgotadas = MutableStateFlow(false)
    val vidasAgotadas: StateFlow<Boolean> = _vidasAgotadas

    private val _nivelSuperado = MutableStateFlow(false)
    val nivelSuperado: StateFlow<Boolean> = _nivelSuperado

    private val _fallosConsecutivos = MutableStateFlow(0)

    private val numeroPreguntas = 5
    private var usadoReintentar = false
    private val usedQuestionIds = mutableSetOf<Int>()

    fun iniciarNivel(nivel: Int) {
        _vidas.value = 3
        _score.value = 0
        _currentQuestionIndex.value = 0
        _fallosConsecutivos.value = 0
        _nivelSuperado.value = false
        _vidasAgotadas.value = false
        usadoReintentar = false
        usedQuestionIds.clear()

        viewModelScope.launch {
            val firebaseQuestions = repository.getQuestionsByLevelIndex(nivel)
                .filterNot { it.id in usedQuestionIds }
                .shuffled()
                .take(numeroPreguntas)

            _questions.value = firebaseQuestions
            usedQuestionIds.addAll(firebaseQuestions.map { it.id }) // ✅ Evitar repeticiones
            if (firebaseQuestions.isNotEmpty()) {
                _answers.value = firebaseQuestions[0].answers.shuffled()
            }
        }
    }

    fun responder(answer: AnswerEntity) {
        val correcta = questions.value[currentQuestionIndex.value].correctAnswerId == answer.id

        if (correcta) {
            _score.value += 1
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

        if (_score.value >= numeroPreguntas) {
            _nivelSuperado.value = true
        }
    }

    fun siguientePregunta() {
        _currentQuestionIndex.value += 1
        if (_currentQuestionIndex.value < _questions.value.size) {
            _answers.value = _questions.value[_currentQuestionIndex.value].answers.shuffled()
        }
    }

    fun puedeReintentar(puntosUsuario: Int): Boolean {
        return puntosUsuario >= 20 && !usadoReintentar
    }

    fun reintentarNivel(puntosUsuario: Int): Int {
        return if (puedeReintentar(puntosUsuario)) {
            _vidas.value = 3
            _vidasAgotadas.value = false
            usadoReintentar = true
            puntosUsuario - 20 // devuelve los puntos actualizados
        } else {
            puntosUsuario
        }
    }
}
