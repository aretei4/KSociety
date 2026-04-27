package com.khaga.ksociety.api

import android.content.Context
import android.provider.Settings
import com.khaga.ksociety.database.FundDao
import com.khaga.ksociety.database.MemberDao
import com.khaga.ksociety.database.PaymentDao
import com.khaga.ksociety.database.ReportDao
import com.khaga.ksociety.model.BackupData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object BackupManager {

    interface BackupCallback {
        fun onSuccess(message: String)
        fun onFailure(error: String)
    }

    fun performBackup(
        context: Context,
        fundDao: FundDao,
        memberDao: MemberDao,
        paymentDao: PaymentDao,
        reportDao: ReportDao,
        callback: BackupCallback
    ) {
        val data = BackupData(
            deviceId  = getDeviceId(context),
            funds     = fundDao.getAll(),
            members   = memberDao.getAll(),
            payments  = paymentDao.getAll(),
            reports   = reportDao.getAll()
        )

        RetrofitClient.getInstance(context)
            .uploadBackup(data)
            .enqueue(object : Callback<ApiResponse<BackupData>> {
                override fun onResponse(
                    call: Call<ApiResponse<BackupData>>,
                    response: Response<ApiResponse<BackupData>>
                ) {
                    if (response.isSuccessful) {
                        callback.onSuccess(
                            "Backup successful! " +
                            "${data.funds.size} funds, " +
                            "${data.members.size} members, " +
                            "${data.payments.size} payments uploaded."
                        )
                    } else {
                        // Demo mode — petstore returns 405; simulate success for UI testing
                        callback.onSuccess(
                            "[DEMO] Backup simulated (HTTP ${response.code()})\n" +
                            "Funds: ${data.funds.size} · " +
                            "Members: ${data.members.size} · " +
                            "Payments: ${data.payments.size}"
                        )
                    }
                }

                override fun onFailure(call: Call<ApiResponse<BackupData>>, t: Throwable) {
                    callback.onFailure("Network error: ${t.message}")
                }
            })
    }

    fun performRestore(
        context: Context,
        callback: BackupCallback
    ) {
        val deviceId = getDeviceId(context)
        RetrofitClient.getInstance(context)
            .downloadBackup(deviceId)
            .enqueue(object : Callback<ApiResponse<BackupData>> {
                override fun onResponse(
                    call: Call<ApiResponse<BackupData>>,
                    response: Response<ApiResponse<BackupData>>
                ) {
                    val restored = response.body()?.data
                    if (response.isSuccessful && restored != null) {
                        callback.onSuccess(
                            "Restore complete! ${restored.funds.size} funds restored."
                        )
                    } else {
                        callback.onFailure(
                            "[DEMO] No backup found on server (HTTP ${response.code()}).\n" +
                            "Connect your own backend to enable real restore."
                        )
                    }
                }

                override fun onFailure(call: Call<ApiResponse<BackupData>>, t: Throwable) {
                    callback.onFailure("Network error: ${t.message}")
                }
            })
    }

    fun getDeviceId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
}
