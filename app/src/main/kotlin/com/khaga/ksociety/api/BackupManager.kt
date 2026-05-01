package com.khaga.ksociety.api

import android.content.Context
import android.provider.Settings
import com.khaga.ksociety.database.AppDatabase
import com.khaga.ksociety.database.FundDao
import com.khaga.ksociety.database.MemberDao
import com.khaga.ksociety.database.PaymentDao
import com.khaga.ksociety.database.ReportDao
import com.khaga.ksociety.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BackupManager {

    private const val PREFS       = "ksociety_backup"
    private const val KEY_URL     = "base_url"
    private const val KEY_DEVICE  = "device_id"
    private const val DEFAULT_URL = "http://10.0.2.2:8080/"

    // ── Device ID (IMEI or phone number entered by user) ──────────────────────
    fun getDeviceId(ctx: Context): String {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // Prefer user-provided ID (IMEI / phone number)
        val saved = prefs.getString(KEY_DEVICE, null)
        if (!saved.isNullOrBlank()) return saved
        // Fallback to Android ID
        return Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown"
    }

    fun setDeviceId(ctx: Context, deviceId: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_DEVICE, deviceId.trim()).apply()
    }

    // ── Base URL ─────────────────────────────────────────────────────────────
    fun getBaseUrl(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun setBaseUrl(ctx: Context, url: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_URL, url.trim()).apply()
        RetrofitClient.setBaseUrl(url.trim())
    }

    // ── BACKUP — upload all local data ────────────────────────────────────────
    suspend fun performBackup(ctx: Context): Result<BackupResponse> = withContext(Dispatchers.IO) {
        try {
            val db        = AppDatabase.getInstance(ctx)
            val deviceId  = getDeviceId(ctx)
            RetrofitClient.setBaseUrl(getBaseUrl(ctx))

            val payload = BackupPayload(
                deviceId    = deviceId,
                deviceLabel = android.os.Build.MODEL,
                appVersion  = "1.0.0",
                funds       = FundDao(db).getAll(),
                members     = MemberDao(db).getAll(),
                payments    = PaymentDao(db).getAll(),
                reports     = ReportDao(db).getAll()
            )

            val response = RetrofitClient.api.uploadBackup(payload)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Server error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── RESTORE — download and write back to SQLite ───────────────────────────
    suspend fun performRestore(ctx: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db       = AppDatabase.getInstance(ctx)
            val deviceId = getDeviceId(ctx)
            RetrofitClient.setBaseUrl(getBaseUrl(ctx))

            val response = RetrofitClient.api.downloadBackup(deviceId)
            if (!response.isSuccessful || response.body() == null) {
                return@withContext Result.failure(
                    Exception("No backup found for device: $deviceId (${response.code()})")
                )
            }

            val payload   = response.body()!!
            val fundDao   = FundDao(db)
            val memberDao = MemberDao(db)
            val payDao    = PaymentDao(db)
            val repDao    = ReportDao(db)

            // Clear existing data and restore from backup
            db.clearAllData()

            payload.funds.forEach    { fundDao.insert(it) }
            payload.members.forEach  { memberDao.insert(it) }
            payload.payments.forEach { payDao.insert(it) }
            payload.reports.forEach  { repDao.insert(it) }

            val summary = "Restored: ${payload.funds.size} funds, " +
                    "${payload.members.size} members, " +
                    "${payload.payments.size} payments"
            Result.success(summary)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Local DB stats ────────────────────────────────────────────────────────
    suspend fun getDbStats(ctx: Context): DbStats = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(ctx)
        DbStats(
            funds    = FundDao(db).getAll().size,
            members  = MemberDao(db).getTotalCount(),
            payments = PaymentDao(db).getAll().size
        )
    }
}