package com.khaga.ksociety.model

data class Fund(
    val id: Long = 0,
    var name: String = "",
    var totalMembers: Int = 0,
    var monthlyAmount: Long = 0,
    var memberFee: Long = 0,
    var interestRate: Float = 2.0f,
    var status: String = "allpaid",
    var overdueCount: Int = 0,
    var closedDate: String? = null,
    var dotColor: String = "#1F5C3A",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isClosed  get() = status == "closed"
    val isOverdue get() = status == "overdue"
    val isAllPaid get() = status == "allpaid"
}
