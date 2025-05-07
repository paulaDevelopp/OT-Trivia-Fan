package com.example.otriviafan.util

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

suspend fun nivelYaExisteEnFirebase(nivel: Int): Boolean {
    val ref = FirebaseDatabase.getInstance().getReference("questions/level$nivel")
    val snapshot = ref.get().await()
    return snapshot.exists() && snapshot.childrenCount > 0
}

suspend fun subirPreguntasNivelDesdeAssets(context: Context, nivel: Int): String? {
    val fileName = obtenerNombreArchivoPorNivel(context, nivel) ?: return null
    val inputStream = context.assets.open("level_questions/$fileName")
    val jsonString = inputStream.bufferedReader().use { it.readText() }

    val preguntas = JSONObject(jsonString).getJSONArray("questions")
    val ref = FirebaseDatabase.getInstance().getReference("questions/level$nivel")

    for (i in 0 until preguntas.length()) {
        val pregunta = preguntas.getJSONObject(i)
        ref.push().setValue(pregunta.deepToMap())
    }

    return fileName // ← ahora devuelve el nombre del archivo usado
}

fun obtenerNombreArchivoPorNivel(context: Context, nivel: Int): String? {
    val assetManager = context.assets
    val archivos = assetManager.list("level_questions") ?: return null

    return archivos
        .filter { it.endsWith(".json") }
        .sortedWith(compareBy(
            { when {
                it.startsWith("easy") -> 0
                it.startsWith("medium") -> 1
                it.startsWith("difficult") -> 2
                else -> 3
            }},
            { it.substringAfter("_level").substringBefore(".json").toIntOrNull() ?: Int.MAX_VALUE }
        ))
        .getOrNull(nivel - 1) // nivel 1 es índice 0
}

// Extensión para convertir JSONObject a Map
fun JSONObject.deepToMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    keys().forEach { key ->
        val value = get(key)
        map[key] = when (value) {
            is JSONObject -> value.deepToMap()
            is JSONArray -> (0 until value.length()).map { i ->
                val item = value.get(i)
                if (item is JSONObject) item.deepToMap() else item
            }
            else -> value
        }
    }
    return map
}

