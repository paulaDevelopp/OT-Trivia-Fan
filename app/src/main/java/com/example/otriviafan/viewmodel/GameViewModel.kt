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
        _vidas.value = 1
        _score.value = 0
        _currentQuestionIndex.value = 0
        _nivelSuperado.value = false
        _vidasAgotadas.value = false
        vidaUsada = false
        falloYaCometido = false
        usedQuestionIds.clear()

        viewModelScope.launch {
            val firebaseQuestions = repository.getQuestionsByLevelIndex(nivel)
                .filterNot { it.id in usedQuestionIds }
                .shuffled()
                .take(numeroPreguntas)

            _questions.value = firebaseQuestions
            usedQuestionIds.addAll(firebaseQuestions.map { it.id })

            if (firebaseQuestions.isNotEmpty()) {
                _answers.value = firebaseQuestions[0].answers.shuffled()
            }
        }
    }

    fun responder(answer: AnswerEntity) {
        val correcta = questions.value[currentQuestionIndex.value].correctAnswerId == answer.id

        if (correcta) {
            _score.value += 4
        } else {
            if (!falloYaCometido) {
                // Primera vez que falla
                if (_score.value >= 20 && !vidaUsada) {
                    _score.value -= 20
                    vidaUsada = true
                    // Se cancela el fallo, puede continuar
                } else {
                    falloYaCometido = true
                    _vidas.value = 0
                    _vidasAgotadas.value = true
                    return
                }
            } else {
                // Ya falló antes, se termina
                _vidas.value = 0
                _vidasAgotadas.value = true
                return
            }
        }

        // Avanzar
        if (_currentQuestionIndex.value + 1 < questions.value.size) {
            _currentQuestionIndex.value += 1
            _answers.value = questions.value[_currentQuestionIndex.value].answers.shuffled()
        } else {
            // Terminó el nivel
            if (!falloYaCometido || vidaUsada) {
                _nivelSuperado.value = true
            }
        }
    }


}
