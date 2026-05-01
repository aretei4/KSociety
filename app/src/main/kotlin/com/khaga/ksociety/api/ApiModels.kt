package com.khaga.ksociety.api

import com.google.gson.annotations.SerializedName
import com.khaga.ksociety.model.*

/** Root payload sent to POST /api/backup and received from GET /api/restore/{deviceId} */
data class BackupPayload(
    @SerializedName("deviceId")         val deviceId:    String,
    @SerializedName("deviceLabel")      val deviceLabel: String  = "",
    @SerializedName("appVersion")       val appVersion:  String  = "1.0.0",
    @SerializedName("backupTimestamp")  val backupTimestamp: Long = System.currentTimeMillis(),
    @SerializedName("funds")            val funds:    List<Fund>          = emptyList(),
    @SerializedName("members")          val members:  List<Member>        = emptyList(),
    @SerializedName("payments")         val payments: List<Payment>       = emptyList(),
    @SerializedName("reports")          val reports:  List<MonthlyReport> = emptyList()
)

/** Response from POST /api/backup */
data class BackupResponse(
    @SerializedName("success")       val success:       Boolean = false,
    @SerializedName("message")       val message:       String  = "",
    @SerializedName("deviceId")      val deviceId:      String  = "",
    @SerializedName("fileName")      val fileName:      String  = "",
    @SerializedName("timestamp")     val timestamp:     Long    = 0L,
    @SerializedName("fundsCount")    val fundsCount:    Int     = 0,
    @SerializedName("membersCount")  val membersCount:  Int     = 0,
    @SerializedName("paymentsCount") val paymentsCount: Int     = 0,
    @SerializedName("reportsCount")  val reportsCount:  Int     = 0
)

/** Stats about local DB shown on BackupFragment */
data class DbStats(
    val funds: Int, val members: Int, val payments: Int
)