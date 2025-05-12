package com.example.otriviafan.data

import com.example.otriviafan.data.entities.AnswerEntity
import com.example.otriviafan.data.entities.StoreItem
import com.example.otriviafan.data.model.Match
import com.example.otriviafan.data.model.PuntosUsuario
import com.example.otriviafan.data.model.QuestionWithAnswers
import com.example.otriviafan.data.model.WallpaperItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class Repository {

    private val auth = FirebaseAuth.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference

    // USUARIO
    suspend fun initializeNewUser(userId: String, email: String) {
        val userRef = realtimeDb.child("users").child(userId)
        val exists = userRef.get().await().exists()
        if (!exists) {
            val initialData = mapOf(
                "email" to email,
                "points" to 20,
                "highestLevelUnlocked" to 1,
                "usedRetryPerLevel" to mapOf<String, Boolean>()
            )
            userRef.setValue(initialData).await()
        }
    }

    // PREGUNTAS
    suspend fun getQuestionsByLevelIndex(index: Int): List<QuestionWithAnswers> {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("questions_by_level").get().await()

        val orderedDocs = snapshot.documents.mapNotNull { doc ->
            val name = doc.id
            val parts = name.split("_level")
            if (parts.size == 2) {
                val difficulty = parts[0]
                val levelNumber = parts[1].toIntOrNull()
                if (levelNumber != null) Triple(name, difficulty, levelNumber) else null
            } else null
        }.sortedWith(compareBy<Triple<String, String, Int>>(
            { when (it.second) {
                "easy" -> 0
                "medium" -> 1
                "difficult" -> 2
                else -> 3
            } },
            { it.third }
        ))

        val docName = orderedDocs.getOrNull(index - 1)?.first ?: return emptyList()
        val doc = db.collection("questions_by_level").document(docName).get().await()

        val questionsList = doc.get("questions") as? List<Map<String, Any>> ?: return emptyList()

        return questionsList.map { questionMap ->
            val questionText = questionMap["questionText"] as? String ?: ""
            val correctAnswerId = (questionMap["correctAnswerId"] as? Number)?.toInt() ?: 0
            val answers = (questionMap["answers"] as? List<Map<String, Any>>)?.map { ans ->
                AnswerEntity(
                    id = (ans["id"] as? Number)?.toInt() ?: 0,
                    questionId = (ans["questionId"] as? Number)?.toInt() ?: 0,
                    answerText = ans["answerText"] as? String ?: ""
                )
            } ?: emptyList()

            QuestionWithAnswers(
                questionText = questionText,
                answers = answers,
                correctAnswerId = correctAnswerId
            )
        }
    }

    // TIENDA
    suspend fun assignInitialItemsIfNeeded(userId: String) {
        val userRef = realtimeDb.child("users").child(userId)
        val snapshot = userRef.child("purchases").get().await()

        if (snapshot.exists() && snapshot.childrenCount > 0) return

        val storeSnapshot = realtimeDb.child("store_items").get().await()
        val initialItems = storeSnapshot.children.take(6)
        val initialPurchases = initialItems.associate { it.key!! to true }

        userRef.child("purchases").updateChildren(initialPurchases).await()
        userRef.child("points").setValue(0).await()
    }

    suspend fun loadUserPurchases(userId: String): List<String> {
        val snapshot = realtimeDb.child("users").child(userId).child("purchases").get().await()
        return snapshot.children.mapNotNull { it.key }
    }

    suspend fun loadStoreItems(): List<StoreItem> {
        val snapshot = realtimeDb.child("store_items").get().await()
        return snapshot.children.mapNotNull { it.getValue(StoreItem::class.java)?.copy(id = it.key ?: "") }
    }

    suspend fun buyItem(userId: String, itemId: String, price: Int) {
        val userRef = realtimeDb.child("users").child(userId)
        val snapshot = userRef.get().await()
        val currentPoints = snapshot.child("points").getValue(Int::class.java) ?: 0

        if (currentPoints >= price) {
            userRef.child("points").setValue(currentPoints - price).await()
            userRef.child("purchases").child(itemId).setValue(true).await()
        } else {
            throw Exception("No tienes suficientes puntos")
        }
    }

    // WALLPAPERS
    suspend fun getAvailableWallpapersForUserLevel(userLevel: Int): List<WallpaperItem> {
        val db = FirebaseFirestore.getInstance()

        val difficulties = when {
            userLevel <= 4 -> listOf("easy")
            userLevel <= 8 -> listOf("easy", "medium")
            else -> listOf("easy", "medium", "difficult")
        }

        val wallpapers = mutableListOf<WallpaperItem>()
        for (difficulty in difficulties) {
            val snapshot = db.collection("wallpapers")
                .whereEqualTo("difficulty", difficulty)
                .get().await()

            wallpapers.addAll(snapshot.documents.mapNotNull { doc ->
                val filename = doc.getString("filename") ?: return@mapNotNull null
                val url = doc.getString("url") ?: return@mapNotNull null
                val price = doc.getLong("price")?.toInt() ?: 50

                WallpaperItem(filename, url, difficulty, price)
            })
        }

        return wallpapers
    }

    suspend fun buyWallpaper(userId: String, wallpaper: WallpaperItem) {
        val userRef = realtimeDb.child("users").child(userId)
        val snapshot = userRef.child("puntos").get().await()
        val current = snapshot.getValue(PuntosUsuario::class.java) ?: PuntosUsuario()

        if (current.total >= wallpaper.price) {
            val updated = current.copy(
                total = current.total - wallpaper.price,
                ultimaActualizacion = System.currentTimeMillis()
            )
            userRef.child("puntos").setValue(updated).await()
            userRef.child("purchases").child(wallpaper.filename).setValue(true).await()
        } else {
            throw Exception("No tienes suficientes puntos")
        }
    }

    suspend fun getUserWallpaperPurchases(userId: String): List<String> {
        val snapshot = realtimeDb.child("users").child(userId).child("purchases").get().await()
        return snapshot.children.mapNotNull { it.key }
    }
    // Marca el wallpaper como desbloqueado al superar un nivel
    suspend fun unlockWallpaperForLevel(userId: String, levelName: String) {
        val userRef = realtimeDb.child("users").child(userId).child("unlocked_wallpapers")
        userRef.child(levelName).setValue(true).await()
    }

    suspend fun getAllWallpapers(): List<WallpaperItem> {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("wallpapers").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val filename = doc.getString("filename") ?: return@mapNotNull null
            val url = doc.getString("url") ?: return@mapNotNull null
            val price = doc.getLong("price")?.toInt() ?: 50
            val difficulty = doc.getString("difficulty") ?: return@mapNotNull null

            WallpaperItem(filename, url, difficulty, price)
        }
    }

    // Devuelve la lista de wallpapers desbloqueados para ese usuario
    suspend fun getUnlockedWallpapers(userId: String): List<String> {
        val snapshot = realtimeDb.child("users").child(userId).child("unlocked_wallpapers").get().await()
        return snapshot.children.mapNotNull { it.key }
    }


    // PUNTOS
    suspend fun addPoints(pointsToAdd: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = realtimeDb.child("users").child(userId).child("puntos")

        val snapshot = userRef.get().await()
        val current = snapshot.getValue(PuntosUsuario::class.java) ?: PuntosUsuario()

        val updated = PuntosUsuario(
            total = current.total + pointsToAdd,
            ultimaActualizacion = System.currentTimeMillis()
        )

        userRef.setValue(updated).await()
    }

    suspend fun spendPoints(pointsToSpend: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = realtimeDb.child("users").child(userId).child("puntos")

        val snapshot = userRef.get().await()
        val current = snapshot.getValue(PuntosUsuario::class.java) ?: PuntosUsuario()

        if (current.total >= pointsToSpend) {
            val updated = current.copy(
                total = current.total - pointsToSpend,
                ultimaActualizacion = System.currentTimeMillis()
            )
            userRef.setValue(updated).await()
        } else {
            throw Exception("No tienes suficientes puntos")
        }
    }

    suspend fun getUserPoints(userId: String): Int {
        val snapshot = realtimeDb.child("users").child(userId).child("puntos").get().await()
        val puntos = snapshot.getValue(PuntosUsuario::class.java)
        return puntos?.total ?: 0
    }

    // NIVEL
    suspend fun getUserLevel(userId: String): Int {
        val snapshot = realtimeDb.child("users").child(userId).child("highestLevelUnlocked").get().await()
        return snapshot.getValue(Int::class.java) ?: 1
    }

    suspend fun saveUserLevel(userId: String, level: Int) {
        realtimeDb.child("users").child(userId).child("highestLevelUnlocked").setValue(level).await()
    }

    suspend fun incrementUserLevel(userId: String) {
        val current = getUserLevel(userId)
        saveUserLevel(userId, current + 1)
    }
    suspend fun getOrderedLevelNames(): List<Triple<String, String, Int>> {
        val snapshot = FirebaseFirestore.getInstance().collection("questions_by_level").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val name = doc.id
            val parts = name.split("_level")
            if (parts.size == 2) {
                val difficulty = parts[0]
                val levelNumber = parts[1].toIntOrNull()
                if (levelNumber != null) Triple(name, difficulty, levelNumber) else null
            } else null
        }.sortedWith(compareBy(
            { when (it.second) {
                "easy" -> 0
                "medium" -> 1
                "difficult" -> 2
                else -> 3
            }},
            { it.third }
        ))
    }

    // MULTIJUGADOR
    suspend fun createMatch(playerId: String): String {
        val matchId = realtimeDb.child("matches").push().key ?: return ""

        val match = Match(
            matchId = matchId,
            player1Id = playerId,
            questions = emptyList(),
            answered = mapOf(playerId to false),
            status = "waiting",
            difficulty = "",
            level = 1
        )

        realtimeDb.child("matches").child(matchId).setValue(match).await()
        return matchId
    }

    fun observeMatch(matchId: String, onUpdate: (Match) -> Unit) {
        val matchRef = realtimeDb.child("matches").child(matchId)
        matchRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val match = snapshot.getValue(Match::class.java)
                match?.let { onUpdate(it) }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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
}
