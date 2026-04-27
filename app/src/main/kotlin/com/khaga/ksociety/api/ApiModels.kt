package com.khaga.ksociety.api

import com.khaga.ksociety.model.BackupData
import retrofit2.Call
import retrofit2.http.*

data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null
)

data class BackupSummary(
    val backupId: String = "",
    val deviceId: String = "",
    val timestamp: Long = 0L,
    val fundCount: Int = 0,
    val memberCount: Int = 0,
    val paymentCount: Int = 0
)

interface KSocietyApiService {

    @POST("backup")
    fun uploadBackup(@Body data: BackupData): Call<ApiResponse<BackupData>>

    @GET("backup/{deviceId}")
    fun downloadBackup(@Path("deviceId") deviceId: String): Call<ApiResponse<BackupData>>

    @GET("backup/list")
    fun listBackups(
        @Query("deviceId") deviceId: String,
        @Query("limit") limit: Int = 10
    ): Call<ApiResponse<List<BackupSummary>>>
}
