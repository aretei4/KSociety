package com.khaga.ksociety.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface KSocietyApiService {

    /** Upload full backup */
    @POST("api/backup")
    suspend fun uploadBackup(@Body payload: BackupPayload): Response<BackupResponse>

    /** Download latest backup for device */
    @GET("api/restore/{deviceId}")
    suspend fun downloadBackup(@Path("deviceId") deviceId: String): Response<BackupPayload>

    /** List all backups for device */
    @GET("api/backups/{deviceId}")
    suspend fun listBackups(@Path("deviceId") deviceId: String): Response<Map<String, Any>>

    /** Health check */
    @GET("api/health")
    suspend fun health(): Response<Map<String, Any>>
}

object RetrofitClient {

    private var _baseUrl: String = "https://device4autism.in/popinion/"  // localhost for emulator
    private var _instance: KSocietyApiService? = null

    val api: KSocietyApiService
        get() = _instance ?: buildService(_baseUrl)

    fun setBaseUrl(url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        if (normalized != _baseUrl) {
            _baseUrl  = normalized
            _instance = null   // force rebuild
        }
    }

    fun getCurrentUrl() = _baseUrl

    private fun buildService(baseUrl: String): KSocietyApiService {
        _instance = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KSocietyApiService::class.java)
        return _instance!!
    }
}