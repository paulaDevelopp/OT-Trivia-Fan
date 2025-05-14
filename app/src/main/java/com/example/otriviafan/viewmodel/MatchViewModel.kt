package com.example.otriviafan.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otriviafan.data.Repository
import com.example.otriviafan.data.model.Match
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchViewModel(private val repository: Repository) : ViewModel() {

    private val _match = MutableStateFlow<Match?>(null)
    val match: StateFlow<Match?> = _match

    // Crear partida con preguntas aleatorias
    fun createMatch(playerId: String, context: Context) {
        viewModelScope.launch {
            try {
                val questions = repository.getRandomQuestionsFromFirestore()
                val matchId = repository.createMatchWithQuestions(playerId, questions)
                repository.observeMatch(matchId) { updatedMatch ->
                    _match.value = updatedMatch
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Unirse a partida existente
    fun joinMatch(playerId: String, context: Context) {
        viewModelScope.launch {
            val matchId = repository.joinOrCreateMatch(playerId, context)
            repository.observeMatch(matchId) { updatedMatch ->
                _match.value = updatedMatch
            }
        }
    }


    // Enviar respuesta
    fun sendAnswer(answerId: Int) {
        val currentMatch = match.value ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val question = currentMatch.questions.getOrNull(currentMatch.currentQuestionIndex) ?: return
        val isCorrect = answerId == question.correctAnswerId

        val matchRef = FirebaseDatabase.getInstance().reference
            .child("matches")
            .child(currentMatch.matchId)

        matchRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val match = currentData.getValue(Match::class.java) ?: return Transaction.success(currentData)

                if (match.answered[uid] == true) return Transaction.success(currentData)

                var newWinner = match.currentWinner
                var p1Score = match.player1Score
                var p2Score = match.player2Score

                if (isCorrect && newWinner == null) {
                    newWinner = uid
                    if (uid == match.player1Id) p1Score++ else p2Score++
                }

                val newAnswered = match.answered.toMutableMap()
                newAnswered[uid] = true

                val updatedMatch = match.copy(
                    currentWinner = newWinner,
                    player1Score = p1Score,
                    player2Score = p2Score,
                    answered = newAnswered
                )

                currentData.value = updatedMatch
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {}
        })
    }

    // Pasar a la siguiente pregunta
    fun nextQuestion() {
        val currentMatch = match.value ?: return

        val matchRef = FirebaseDatabase.getInstance().reference
            .child("matches")
            .child(currentMatch.matchId)

        matchRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val match = currentData.getValue(Match::class.java) ?: return Transaction.success(currentData)

                if (match.currentQuestionIndex >= match.questions.size - 1) {
                    val finishedMatch = match.copy(status = "finished")
                    currentData.value = finishedMatch
                    return Transaction.success(currentData)
                }

                val nextIndex = match.currentQuestionIndex + 1
                val newAnswered = match.answered.mapValues { false }

                val updatedMatch = match.copy(
                    currentQuestionIndex = nextIndex,
                    answered = newAnswered,
                    currentWinner = null
                )

                currentData.value = updatedMatch
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {}
        })
    }
}
