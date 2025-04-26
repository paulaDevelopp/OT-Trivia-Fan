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

    val usedQuestionIds = mutableSetOf<Int>()

    var hasUpdatedScore = false

    // 🚀 NUEVOS CAMPOS PARA NIVELES Y LOGROS
    private val _nivel = MutableStateFlow(1)
    val nivel: StateFlow<Int> = _nivel

    private val _logros = MutableStateFlow<Set<String>>(emptySet())
    val logros: StateFlow<Set<String>> = _logros

    fun loadQuestions(excludeIds: Set<Int> = emptySet()) {
        viewModelScope.launch {
            try {
                val firebaseQuestions = repository.getQuestionsFromFirebase()
                    .filterNot { it.id in excludeIds }
                    .shuffled()
                    .take(10)
                _questions.value = firebaseQuestions
                usedQuestionIds.addAll(firebaseQuestions.map { it.id })
                if (firebaseQuestions.isNotEmpty()) {
                    _answers.value = firebaseQuestions[0].answers.shuffled()
                }
            } catch (e: Exception) {
                _questions.value = emptyList()
            }
        }
    }

    fun answerQuestion(selectedAnswer: AnswerEntity, userId: String) {
        val currentQuestion = _questions.value[_currentQuestionIndex.value]
        val isCorrect = selectedAnswer.id == currentQuestion.correctAnswerId
        if (isCorrect) _score.value += 1

        _nivel.value = calcularNivel(_score.value)
        comprobarLogros()

        viewModelScope.launch {
            repository.saveProgress(
                userId = userId,
                questionId = currentQuestion.id,
                correct = isCorrect
            )
        }
    }

    fun nextQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _questions.value.size) {
            _currentQuestionIndex.value = nextIndex
            _answers.value = _questions.value[nextIndex].answers.shuffled()
        }
    }

    fun resetGame() {
        _score.value = 0
        _currentQuestionIndex.value = 0
        hasUpdatedScore = false
        loadQuestions(excludeIds = usedQuestionIds)
    }

    // 🚀 FUNCIONES DE NIVELES Y LOGROS
    private fun calcularNivel(aciertos: Int): Int {
        return when {
            aciertos <= 10 -> 1
            aciertos <= 25 -> 2
            aciertos <= 50 -> 3
            aciertos <= 80 -> 4
            aciertos <= 120 -> 5
            else -> 6
        }
    }

    private fun comprobarLogros() {
        val nuevosLogros = mutableSetOf<String>()

        if (score.value >= 1) nuevosLogros.add("primer_acierto")
        if (score.value >= 10) nuevosLogros.add("racha_10")
        if (_nivel.value >= 5) nuevosLogros.add("maestro_musical")

        _logros.value = nuevosLogros
    }
}
