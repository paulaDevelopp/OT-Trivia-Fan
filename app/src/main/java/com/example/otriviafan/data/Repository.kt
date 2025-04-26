package com.example.otriviafan.data

import com.example.otriviafan.data.entities.StoreItem
import com.example.otriviafan.data.model.Match
import com.example.otriviafan.data.model.QuestionWithAnswers
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class Repository {

    private val auth = FirebaseAuth.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference

    // ---------------------------
    // PREGUNTAS
    // ---------------------------
    suspend fun getQuestionsFromFirebase(): List<QuestionWithAnswers> {
        val snapshot = realtimeDb.child("questions").get().await()
        val list = mutableListOf<QuestionWithAnswers>()

        snapshot.children.forEach { qSnap ->
            val questionId = qSnap.key?.toIntOrNull() ?: return@forEach
            val questionText = qSnap.child("questionText").getValue(String::class.java) ?: return@forEach
            val correctAnswerId = qSnap.child("correctAnswerId").getValue(Int::class.java) ?: return@forEach

            val answers = qSnap.child("answers").children.mapNotNull {
                val answerId = it.child("id").getValue(Int::class.java)
                val answerText = it.child("answerText").getValue(String::class.java)
                if (answerId != null && answerText != null) {
                    com.example.otriviafan.data.entities.AnswerEntity(id = answerId, questionId = questionId, answerText = answerText)
                } else null
            }

            list.add(
                QuestionWithAnswers(
                    id = questionId,
                    questionText = questionText,
                    correctAnswerId = correctAnswerId,
                    answers = answers
                )
            )
        }

        return list
    }

    // ---------------------------
    // PROGRESO
    // ---------------------------
    suspend fun saveProgress(userId: String, questionId: Int, correct: Boolean) {
        val progressRef = realtimeDb.child("progress").child(userId).push()
        val progress = mapOf(
            "questionId" to questionId,
            "correct" to correct,
            "timestamp" to System.currentTimeMillis()
        )
        progressRef.setValue(progress).await()
    }

    // ---------------------------
    // MULTIJUGADOR
    // ---------------------------
    suspend fun createMatch(playerId: String): String {
        val matchId = realtimeDb.child("matches").push().key ?: return ""
        val questions = getQuestionsFromFirebase().shuffled().take(10)

        val match = Match(
            matchId = matchId,
            player1Id = playerId,
            questions = questions,
            answered = mapOf(playerId to false),
            status = "waiting"
        )

        realtimeDb.child("matches").child(matchId).setValue(match).await()
        return matchId
    }

    suspend fun joinMatch(playerId: String): String? {
        val matchesSnapshot = realtimeDb.child("matches").get().await()

        for (matchSnap in matchesSnapshot.children) {
            val match = matchSnap.getValue(Match::class.java)
            if (match != null && match.status == "waiting" && match.player1Id != playerId) {
                val matchId = match.matchId

                val updatedMatch = match.copy(
                    player2Id = playerId,
                    answered = match.answered + (playerId to false),
                    status = "active"
                )

                realtimeDb.child("matches").child(matchId).setValue(updatedMatch).await()
                return matchId
            }
        }

        return null
    }

    fun observeMatch(matchId: String, onUpdate: (Match) -> Unit) {
        val matchRef = realtimeDb.child("matches").child(matchId)

        matchRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val match = snapshot.getValue(Match::class.java)
                match?.let { onUpdate(it) }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                // No hacemos nada en caso de error por ahora
            }
        })
    }

    // ---------------------------
    // TIENDA
    // ---------------------------

    // 📦 1. Cargar stickers y fondos de pantalla
    suspend fun loadStoreItems(): List<StoreItem> {
        val snapshot = realtimeDb.child("store_items").get().await()
        val list = mutableListOf<StoreItem>()

        snapshot.children.forEach { itemSnap ->
            val item = itemSnap.getValue(StoreItem::class.java)
            item?.let { list.add(it.copy(id = itemSnap.key ?: "")) }
        }

        return list
    }

    // 🛒 2. Comprar un ítem
    suspend fun assignInitialItemsIfNeeded(userId: String) {
        val userRef = realtimeDb.child("users").child(userId)

        // 1. Comprobar si ya tiene compras
        val snapshot = userRef.child("purchases").get().await()

        if (snapshot.exists() && snapshot.childrenCount > 0) {
            // Ya tiene compras, no hacemos nada
            return
        }

        // 2. Si no tiene compras, asignar los 6 stickers y 6 fondos
        val storeSnapshot = realtimeDb.child("store_items").get().await()

        val stickers = storeSnapshot.children.filter {
            it.child("type").getValue(String::class.java) == "sticker"
        }.take(6)

        val backgrounds = storeSnapshot.children.filter {
            it.child("type").getValue(String::class.java) == "background"
        }.take(6)

        val initialPurchases = (stickers + backgrounds).associate { it.key!! to true }

        // 3. Actualizar en el usuario: sus compras + puntos iniciales
        userRef.child("purchases").updateChildren(initialPurchases).await()
        userRef.child("points").setValue(500).await()
    }

    suspend fun buyItem(userId: String, itemId: String, price: Int) {
        val userRef = realtimeDb.child("users").child(userId)

        val snapshot = userRef.get().await()
        val currentPoints = snapshot.child("points").getValue(Int::class.java) ?: 0

        if (currentPoints >= price) {
            userRef.child("points").setValue(currentPoints - price).await()
            userRef.child("purchases").child(itemId).setValue(true).await()
        } else {
            throw Exception("No tienes suficientes puntos para comprar este ítem")
        }
    }

    // ✅ 3. Cargar compras del usuario
    suspend fun loadUserPurchases(userId: String): List<String> {
        val snapshot = realtimeDb.child("users").child(userId).child("purchases").get().await()
        return snapshot.children.mapNotNull { it.key }
    }
}
