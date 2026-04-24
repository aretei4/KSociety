package com.bccommittee.api;

import com.bccommittee.model.BackupData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Swagger-style REST API interface for BC Committee backup/restore.
 * Maps to Swagger Petstore-style endpoint structure.
 *
 * Base URL: https://petstore.swagger.io/v2/  (demo)
 * Or your own backend: https://your-api.com/api/v1/
 *
 * Swagger UI: https://petstore.swagger.io
 */
public interface BCCommitteeApiService {

    /**
     * POST /backup
     * Upload full backup data to server.
     * Swagger tag: backup
     */
    @POST("backup")
    Call<ApiResponse<BackupData>> uploadBackup(@Body BackupData backupData);

    /**
     * GET /backup/{deviceId}
     * Retrieve the latest backup for this device.
     * Swagger tag: backup
     */
    @GET("backup/{deviceId}")
    Call<ApiResponse<BackupData>> downloadBackup(@Path("deviceId") String deviceId);

    /**
     * GET /backup/list
     * List all available backup snapshots.
     * Swagger tag: backup
     */
    @GET("backup/list")
    Call<ApiResponse<java.util.List<BackupSummary>>> listBackups(
            @Query("deviceId") String deviceId,
            @Query("limit") int limit);

    /**
     * POST /backup/validate
     * Validate backup data integrity.
     */
    @POST("backup/validate")
    Call<ApiResponse<Boolean>> validateBackup(@Body BackupData backupData);
}
