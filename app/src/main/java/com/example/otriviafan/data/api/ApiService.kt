package com.example.otriviafan.data.api
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/upload_wallpapers")
    suspend fun uploadWallpapers(
        @Query("difficulty") difficulty: String
    ): Response<UploadResponse>
}

