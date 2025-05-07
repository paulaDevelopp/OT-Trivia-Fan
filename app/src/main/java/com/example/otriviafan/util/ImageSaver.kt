// Ruta: com/example/otriviafan/util/ImageSaver.kt

package com.example.otriviafan.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.OutputStream

suspend fun saveImageToGallery(context: Context, imageUrl: String, filename: String): Boolean {
    return try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()

        val result = (loader.execute(request) as SuccessResult).drawable
        val bitmap = result.toBitmap()

        val filenameWithExtension = "$filename.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filenameWithExtension)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/OTTriviaFan")
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            val stream: OutputStream? = resolver.openOutputStream(it)
            stream?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        }

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
