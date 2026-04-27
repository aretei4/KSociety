package com.khaga.ksociety.model

data class MonthlyReport(
    val id: Long = 0,
    var month: String = "",
    var collected: Long = 0,
    var interestIn: Long = 0,
    var penalties: Long = 0,
    var fees: Long = 0,
    var totalFund: Long = 0,
    var defaults: Int = 0,
    var fundId: Long = 0
) {
    val totalRevenue: Long get() = collected + interestIn + fees + penalties
}
