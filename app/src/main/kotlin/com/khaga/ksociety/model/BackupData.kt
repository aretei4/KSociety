package com.khaga.ksociety.model

data class BackupData(
    var appVersion: String = "1.0.0",
    var backupTimestamp: Long = System.currentTimeMillis(),
    var deviceId: String = "",
    var funds: List<Fund> = emptyList(),
    var members: List<Member> = emptyList(),
    var payments: List<Payment> = emptyList(),
    var reports: List<MonthlyReport> = emptyList()
)
