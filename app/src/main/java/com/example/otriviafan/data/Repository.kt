package com.example.otriviafan.data

import android.content.Context
import com.example.otriviafan.data.entities.AnswerEntity
import com.example.otriviafan.data.model.Match
import com.example.otriviafan.data.model.PuntosUsuario
import com.example.otriviafan.data.model.QuestionWithAnswers
import com.example.otriviafan.data.model.WallpaperItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class Repository {

    private val auth = FirebaseAuth.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference


    // USUARIO y PUNTOS

    //crear nuevo user
    suspend fun initializeNewUser(userId: String, email: String) {
        val userRef = realtimeDb.child("users").child(userId)
        val exists = userRef.get().await().exists()
        if (!exists) {
            val initialData = mapOf(
                "email" to email,
                "usedRetryPerLevel" to mapOf<String, Boolean>(),
                "isNewUser" to true
            )

            userRef.setValue(initialData).await()

            // Agrega los puntos iniciales de regalo
            val initialPoints = PuntosUsuario(total = 5, ultimaActualizacion = System.currentTimeMillis())
            userRef.child("puntos").setValue(initialPoints).await()

        }
    }
    //Devuelve el total de puntos que tiene el usuario actualmente.
    suspend fun getUserPoints(userId: String): Int {
        val snapshot = realtimeDb.child("users").child(userId).child("puntos").get().await()
        val puntos = snapshot.getValue(PuntosUsuario::class.java)
        return puntos?.total ?: 0
    }
    //Suma una cantidad de puntos al usuario autenticado y actualiza la fecha de modificación.
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
    //Resta puntos del usuario autenticado, si tiene suficientes. Lanza excepción si no los tiene.
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

    // PREGUNTAS
    suspend fun getQuestionsForLevel(levelName: String): List<QuestionWithAnswers> {
        val doc = FirebaseFirestore.getInstance()
            .collection("questions_by_level")
            .document(levelName)
            .get()
            .await()

        val questionsList = doc.get("questions") as? List<Map<String, Any>> ?: return emptyList()

        return questionsList.map { questionMap ->
            val questionText = questionMap["questionText"] as? String ?: ""
            val correctAnswerId = (questionMap["correctAnswerId"] as? Number)?.toInt() ?: 0
            val questionId = (questionMap["questionId"] as? Number)?.toInt() ?: 0

            val answers = (questionMap["answers"] as? List<Map<String, Any>>)?.map { ans ->
                AnswerEntity(
                    id = (ans["id"] as? Number)?.toInt() ?: 0,
                    questionId = (ans["questionId"] as? Number)?.toInt() ?: 0,
                    answerText = ans["answerText"] as? String ?: ""
                )
            } ?: emptyList()

            QuestionWithAnswers(
                id = questionId,
                questionText = questionText,
                correctAnswerId = correctAnswerId,
                answers = answers
            )
        }
    }
    suspend fun getQuestionsForMultiplayerLevel(levelName: String): List<QuestionWithAnswers> {
        val doc = FirebaseFirestore.getInstance()
            .collection("questions_by_level")
            .document(levelName)
            .get()
            .await()

        val questionsList = doc.get("questions") as? List<Map<String, Any>> ?: return emptyList()

        return questionsList.map { questionMap ->
            val questionText = questionMap["questionText"] as? String ?: ""
            val correctAnswerId = (questionMap["correctAnswerId"] as? Number)?.toInt() ?: 0
            val questionId = (questionMap["questionId"] as? Number)?.toInt() ?: 0
            val imageUrl = questionMap["imageUrl"] as? String ?: ""

            val answers = (questionMap["answers"] as? List<Map<String, Any>>)?.map { ans ->
                AnswerEntity(
                    id = (ans["id"] as? Number)?.toInt() ?: 0,
                    questionId = (ans["questionId"] as? Number)?.toInt() ?: 0,
                    answerText = ans["answerText"] as? String ?: ""
                )
            } ?: emptyList()

            QuestionWithAnswers(
                id = questionId,
                questionText = questionText,
                correctAnswerId = correctAnswerId,
                imageUrl = imageUrl,
                answers = answers
            )
        }.shuffled()
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


    // WALLPAPERS

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


    // NIVEL
    suspend fun getAllLevelNamesOrdered(): List<String> {
        val snapshot = FirebaseFirestore.getInstance().collection("questions_by_level").get().await()

        val individualLevels = mutableListOf<Triple<String, String, Int>>()
        val multiplayerLevels = mutableListOf<String>()

        for (doc in snapshot.documents) {
            val id = doc.id
            when {
                id.startsWith("multiplayer") -> multiplayerLevels.add(id)

                Regex("""(easy|medium|difficult)_level(\d+)""").matches(id) -> {
                    val match = Regex("""(easy|medium|difficult)_level(\d+)""").find(id)
                    val difficulty = match?.groupValues?.get(1)
                    val number = match?.groupValues?.get(2)?.toIntOrNull()
                    if (difficulty != null && number != null) {
                        individualLevels.add(Triple(id, difficulty, number))
                    }
                }
            }
        }

        // Orden por dificultad y número interno
        val orderedIndividuals = individualLevels.sortedWith(compareBy(
            { when (it.second) {
                "easy" -> 0
                "medium" -> 1
                "difficult" -> 2
                else -> 3
            }},
            { it.third }
        )).map { it.first }

        // Intercalar multijugadores
        val finalList = mutableListOf<String>()
        var multiplayerIndex = 0

        for ((i, id) in orderedIndividuals.withIndex()) {
            finalList.add(id)
            if ((i + 1) % 3 == 0 && multiplayerIndex < multiplayerLevels.size) {
                finalList.add(multiplayerLevels[multiplayerIndex++])
            }
        }

        // Si sobran multijugadores los agregamos al final
        while (multiplayerIndex < multiplayerLevels.size) {
            finalList.add(multiplayerLevels[multiplayerIndex++])
        }

        return finalList
    }

    suspend fun getUserLevel(userId: String): String {
        val snapshot = realtimeDb.child("users").child(userId).child("highestLevelUnlockedName").get().await()
        return snapshot.getValue(String::class.java) ?: getAllLevelNamesOrdered().firstOrNull() ?: ""
    }

    suspend fun saveUserLevel(userId: String, levelName: String) {
        realtimeDb.child("users").child(userId).child("highestLevelUnlockedName").setValue(levelName).await()
    }

    fun esNivelMultijugador(levelName: String): Boolean {
        return levelName.startsWith("multiplayer")
    }

    suspend fun incrementUserLevel(userId: String) {
        val allLevels = getAllLevelNamesOrdered()
        val currentLevel = getUserLevel(userId)
        val currentIndex = allLevels.indexOf(currentLevel)

        if (currentIndex != -1 && currentIndex + 1 < allLevels.size) {
            saveUserLevel(userId, allLevels[currentIndex + 1])
        }
    }

    suspend fun marcarNivelCompletado(userId: String, levelName: String) {
        val tipo = if (levelName.startsWith("multiplayer")) "multiplayer" else "individual"

        val ref = realtimeDb
            .child("users")
            .child(userId)
            .child("nivel_progreso")
            .child(levelName)

        val progreso = mapOf("completado" to true, "tipo" to tipo)

        ref.setValue(progreso).await()

        if (tipo == "individual") {
            val wallpapers = getAllWallpapers()
            wallpapers.find { it.filename == levelName }?.let {
                unlockWallpaperForLevel(userId, it.filename)
            }
        }
    }

    suspend fun verificarNivelCompletado(userId: String, levelName: String): Boolean {
        val snapshot = realtimeDb
            .child("users")
            .child(userId)
            .child("nivel_progreso")
            .child(levelName)
            .get()
            .await()

        val completado = snapshot.child("completado").getValue(Boolean::class.java) ?: false
        val tipo = snapshot.child("tipo").getValue(String::class.java) ?: "individual"

        return completado && tipo == if (levelName.startsWith("multiplayer")) "multiplayer" else "individual"
    }


    suspend fun getNivelProgreso(userId: String): Map<String, com.example.otriviafan.viewmodel.UserViewModel.NivelProgreso> {
        val snapshot = realtimeDb
            .child("users")
            .child(userId)
            .child("nivel_progreso")
            .get()
            .await()

        val result = mutableMapOf<String, com.example.otriviafan.viewmodel.UserViewModel.NivelProgreso>()
        for (nivel in snapshot.children) {
            val id = nivel.key ?: continue
            val completado = nivel.child("completado").getValue(Boolean::class.java) ?: false
            val tipo = nivel.child("tipo").getValue(String::class.java) ?: "individual"
            result[id] = com.example.otriviafan.viewmodel.UserViewModel.NivelProgreso(completado, tipo)
        }
        return result
    }

    // MULTIJUGADOR

    //Crea un nodo en Firebase
    suspend fun createMatchWithQuestions(playerId: String, levelName: String, questions: List<QuestionWithAnswers>): String {
        val matchId = realtimeDb.child("matches").push().key ?: return ""
        val now = System.currentTimeMillis()
        val match = Match(
            matchId = matchId,
            player1Id = playerId,
            questions = questions,
            answered = mapOf(playerId to false),
            status = "waiting",
            difficulty = "",
            levelName = levelName,
            lastActive = mapOf(playerId to now)
        )

        realtimeDb.child("matches").child(matchId).setValue(match).await()
        return matchId
    }


    suspend fun joinOrCreateMatch(playerId: String, context: Context, levelName: String): String {
        val query = realtimeDb.child("matches")
            .orderByChild("status")
            .equalTo("waiting")
        val matchesSnapshot = query.get().await()

        delay(300) // Para mitigar condiciones de carrera simples

        for (matchSnap in matchesSnapshot.children) {
            val match = matchSnap.getValue(Match::class.java)

            if (match != null &&
                match.player1Id != playerId &&
                match.player2Id.isNullOrEmpty() &&
                match.levelName.trim() == levelName.trim()
            ) {
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

        // Si no se encontró una partida válida, crea una nueva
        val questions = getQuestionsForMultiplayerLevel(levelName)
        return createMatchWithQuestions(playerId, levelName, questions)
    }

    fun observeMatch(matchId: String, onUpdate: (Match) -> Unit) {
        val matchRef = realtimeDb.child("matches").child(matchId)
        matchRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val match = snapshot.getValue(Match::class.java) ?: return
                onUpdate(match)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    fun updateLastActive(matchId: String, userId: String) {
        val now = System.currentTimeMillis()
        realtimeDb.child("matches").child(matchId).child("lastActive").child(userId).setValue(now)
    }

    /*Marca al jugador como que respondió.
    Si la respuesta es correcta, suma un punto.*/
    fun setPlayerAnswered(matchId: String, userId: String, answerId: Int) {
        val matchRef = realtimeDb.child("matches").child(matchId)

        matchRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                println("📨 Usuario $userId respondió en partida $matchId con respuesta $answerId")

                val match = currentData.getValue(Match::class.java) ?: return Transaction.success(currentData)

                if (match.answered[userId] == true) {
                    return Transaction.success(currentData)
                }


                val currentQuestion = match.questions.getOrNull(match.currentQuestionIndex)
                    ?: return Transaction.success(currentData)

                val isCorrect = currentQuestion.correctAnswerId == answerId
                val updatedAnswered = match.answered.toMutableMap().apply {
                    this[userId] = true
                }

                var newWinner = match.currentWinner
                var player1Score = match.player1Score
                var player2Score = match.player2Score

                if (isCorrect && newWinner == null) {
                    newWinner = userId
                    if (userId == match.player1Id) player1Score++ else if (userId == match.player2Id) player2Score++
                }

                val updatedMatch = match.copy(
                    answered = updatedAnswered,
                    player1Score = player1Score,
                    player2Score = player2Score,
                    currentWinner = newWinner
                )

                currentData.value = updatedMatch
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {}
        })
    }

    suspend fun abandonMatch(matchId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val matchRef = FirebaseDatabase.getInstance().reference.child("matches").child(matchId)
        val update = mapOf(
            "player1Score" to 0,
            "player2Score" to 0,
            "status" to "finished",
            "abandonedBy" to userId
        )
        matchRef.updateChildren(update).await()
    }


    fun nextQuestion(matchId: String) {
        val matchRef = realtimeDb.child("matches").child(matchId)

        matchRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val match = currentData.getValue(Match::class.java) ?: return Transaction.success(currentData)

                println(" Intentando avanzar a siguiente pregunta - índice actual: ${match.currentQuestionIndex}")

                val currentIndex = match.currentQuestionIndex
                val totalQuestions = match.questions.size

                return if (currentIndex + 1 >= totalQuestions) {
                    println(" Partida finalizada")
                    val updatedMatch = match.copy(status = "finished")
                    currentData.value = updatedMatch
                    Transaction.success(currentData)
                } else {
                    val newIndex = currentIndex + 1
                    val resetAnswered = match.answered.mapValues { false }

                    println(" Siguiente pregunta: $newIndex")
                    val updatedMatch = match.copy(
                        currentQuestionIndex = newIndex,
                        answered = resetAnswered,
                        currentWinner = null
                    )
                    currentData.value = updatedMatch
                    Transaction.success(currentData)
                }
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    println(" Error al avanzar de pregunta: ${error.message}")
                }
            }
        })
    }


    // ACTUALIZAR CONTENIDO
    fun observeWallpapers(onUpdate: (List<WallpaperItem>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("wallpapers")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val wallpapers = snapshot.documents.mapNotNull { doc ->
                    val filename = doc.getString("filename") ?: return@mapNotNull null
                    val url = doc.getString("url") ?: return@mapNotNull null
                    val price = doc.getLong("price")?.toInt() ?: 50
                    val difficulty = doc.getString("difficulty") ?: return@mapNotNull null
                    val level = doc.getLong("level")?.toInt() ?: 1

                    WallpaperItem(filename, url, difficulty, price, level)
                }

                onUpdate(wallpapers)
            }
    }
    fun observeUserPurchases(userId: String, onUpdate: (List<String>) -> Unit) {
        val ref = realtimeDb.child("users").child(userId).child("purchases")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val purchases = snapshot.children.mapNotNull { it.key }
                onUpdate(purchases)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    fun observeQuestionsForLevel(levelName: String, onUpdate: (List<QuestionWithAnswers>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("questions_by_level")
            .document(levelName)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val questionsList = snapshot.get("questions") as? List<Map<String, Any>> ?: return@addSnapshotListener

                val questions = questionsList.map { questionMap ->
                    val questionText = questionMap["questionText"] as? String ?: ""
                    val correctAnswerId = (questionMap["correctAnswerId"] as? Number)?.toInt() ?: 0
                    val questionId = (questionMap["questionId"] as? Number)?.toInt() ?: 0
                    val imageUrl = questionMap["imageUrl"] as? String ?: ""

                    val answers = (questionMap["answers"] as? List<Map<String, Any>>)?.map { ans ->
                        AnswerEntity(
                            id = (ans["id"] as? Number)?.toInt() ?: 0,
                            questionId = (ans["questionId"] as? Number)?.toInt() ?: 0,
                            answerText = ans["answerText"] as? String ?: ""
                        )
                    } ?: emptyList()

                    QuestionWithAnswers(
                        id = questionId,
                        questionText = questionText,
                        correctAnswerId = correctAnswerId,
                        imageUrl = imageUrl,
                        answers = answers
                    )
                }

                onUpdate(questions)
            }
    }


}
