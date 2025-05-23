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
import kotlinx.coroutines.tasks.await

class Repository {

    private val auth = FirebaseAuth.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference

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


 /*   private fun getMultiplayerDocName(nivelId: Int): String {
        return when (nivelId) {
            4 -> "multiplayer_level1"
            7 -> "multiplayer_level2"
            else -> "multiplayer_level1" // Fallback por si algo sale mal
        }
    }
*/
    // USUARIO
    suspend fun initializeNewUser(userId: String, email: String) {
        val userRef = realtimeDb.child("users").child(userId)
        val exists = userRef.get().await().exists()
        if (!exists) {
            val initialData = mapOf(
                "email" to email,
                "highestLevelUnlocked" to 1,
                "usedRetryPerLevel" to mapOf<String, Boolean>()
            )
            userRef.setValue(initialData).await()

            // Agrega los puntos iniciales de regalo correctamente en el nodo "puntos"
            val initialPoints = PuntosUsuario(total = 5, ultimaActualizacion = System.currentTimeMillis())
            userRef.child("puntos").setValue(initialPoints).await()

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

  /*  suspend fun loadUserPurchases(userId: String): List<String> {
        val snapshot = realtimeDb.child("users").child(userId).child("purchases").get().await()
        return snapshot.children.mapNotNull { it.key }
    }

    suspend fun loadStoreItems(): List<StoreItem> {
        val snapshot = realtimeDb.child("store_items").get().await()
        return snapshot.children.mapNotNull { it.getValue(StoreItem::class.java)?.copy(id = it.key ?: "") }
    }
*/
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
    suspend fun getAvailableWallpapersForUser(userId: String): List<WallpaperItem> {
        val progreso = getNivelProgreso(userId)

        val nivelesCompletados = progreso.filter { (nivel, datos) ->
            datos.completado && !nivel.startsWith("multiplayer")
        }

        val snapshot = FirebaseFirestore.getInstance().collection("wallpapers").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val filename = doc.getString("filename") ?: return@mapNotNull null
            val url = doc.getString("url") ?: return@mapNotNull null
            val price = doc.getLong("price")?.toInt() ?: 50
            val difficulty = doc.getString("difficulty") ?: return@mapNotNull null
            val minLevel = doc.getLong("level")?.toInt() ?: 1

            WallpaperItem(filename, url, difficulty, price, minLevel)
        }.filter { it.minLevel <= nivelesCompletados.size }
    }


    // Mueve esta función fuera (puede estar en el Repository o como extensión)
 /*   suspend fun esNivelMultijugador(nivelId: Int): Boolean {
        val levelNames = getAllLevelNamesOrdered()
        val docName = levelNames.find { it.first == nivelId }?.second ?: return false
        return docName.startsWith("multiplayer")
    }
*/
    fun esNivelMultijugador(levelName: String): Boolean {
        return levelName.startsWith("multiplayer")
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

    suspend fun incrementUserLevel(userId: String) {
        val allLevels = getAllLevelNamesOrdered()
        val currentLevel = getUserLevel(userId)
        val currentIndex = allLevels.indexOf(currentLevel)

        if (currentIndex != -1 && currentIndex + 1 < allLevels.size) {
            saveUserLevel(userId, allLevels[currentIndex + 1])
        }
    }

    suspend fun getIndividualLevelNamesOrdered(): List<Pair<Int, String>> {
        val snapshot = FirebaseFirestore.getInstance().collection("questions_by_level").get().await()

        val ordered = snapshot.documents.mapNotNull { doc ->
            val id = doc.id
            if (id.startsWith("multiplayer")) return@mapNotNull null // ❌ Salta multijugador

            val match = Regex("""(easy|medium|difficult)_level(\d+)""").find(id)
            val difficulty = match?.groupValues?.get(1)
            val level = match?.groupValues?.get(2)?.toIntOrNull()

            if (difficulty != null && level != null) {
                Triple(id, difficulty, level)
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

        return ordered.mapIndexed { index, triple -> (index + 1) to triple.first }
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
    suspend fun getQuestionsByLevelIndex(index: Int): List<QuestionWithAnswers> {
        val allLevels = getAllLevelNamesOrdered()
        val levelName = allLevels.getOrNull(index - 1) ?: return emptyList()
        return getQuestionsForLevel(levelName)
    }

//    suspend fun isMultiplayerRequiredForLevel(docName: String): Boolean {
//        val levelNumber = docName
//            .split("_level")
//            .getOrNull(1)
//            ?.toIntOrNull() ?: return false
//
//        return requiereMultijugador(levelNumber)
//    }

    // MULTIJUGADOR: Leer preguntas desde assets
  /*  suspend fun getRandomQuestionsFromFirestore(): List<QuestionWithAnswers> {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("questions_by_level").get().await()

        val documents = snapshot.documents
        if (documents.isEmpty()) return emptyList()

        val randomDoc = documents.random()
        val questionsList = randomDoc.get("questions") as? List<Map<String, Any>> ?: return emptyList()

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
    }*/

    // MULTIJUGADOR

    //Crea un nodo en Firebase
    suspend fun createMatchWithQuestions(playerId: String, levelName: String, questions: List<QuestionWithAnswers>): String {
        val matchId = realtimeDb.child("matches").push().key ?: return ""

        val match = Match(
            matchId = matchId,
            player1Id = playerId,
            questions = questions,
            answered = mapOf(playerId to false),
            status = "waiting",
            difficulty = "", // puedes asignar dificultad desde el nombre si lo necesitas
            levelName = levelName // actualiza el modelo Match para usar String
        )

        realtimeDb.child("matches").child(matchId).setValue(match).await()
        return matchId
    }


    suspend fun joinOrCreateMatch(playerId: String, context: Context, levelName: String): String {
        val matchesSnapshot = realtimeDb.child("matches").get().await()

        for (matchSnap in matchesSnapshot.children) {
            val match = matchSnap.getValue(Match::class.java)

            if (
                match != null &&
                match.status == "waiting" &&
                match.player1Id != playerId &&
                match.player2Id.isNullOrEmpty() &&
                match.levelName == levelName
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

  /*  suspend fun updatePlayerScore(matchId: String, userId: String, newScore: Int) {
        val matchSnapshot = realtimeDb.child("matches").child(matchId).get().await()
        val match = matchSnapshot.getValue(Match::class.java) ?: return

        val scoreField = when (userId) {
            match.player1Id -> "player1Score"
            match.player2Id -> "player2Score"
            else -> return
        }
        realtimeDb.child("matches").child(matchId).child(scoreField).setValue(newScore).await()


        realtimeDb.child("matches").child(matchId).child(scoreField).setValue(newScore).await()
    }*/
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


    suspend fun getMultiplayerRequirementsMap(): Map<String, Boolean> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("questions_by_level")
            .get()
            .await()

        return snapshot.documents.associate { doc ->
            val docId = doc.id
            val requiresMultiplayer = doc.getBoolean("requiresMultiplayerWin") ?: false
            docId to requiresMultiplayer
        }
    }

}
