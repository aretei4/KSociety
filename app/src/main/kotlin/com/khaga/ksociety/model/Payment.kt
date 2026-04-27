package com.khaga.ksociety.model

data class Payment(
    val id: Long = 0,
    var memberId: Long = 0,
    var memberName: String = "",
    var memberAvatar: String = "",
    var type: String = "",
    var amount: Long = 0,
    var principal: Long = 0,
    var interest: Long = 0,
    var penalty: Long = 0,
    var memberFee: Long = 0,
    var date: String = "",
    var status: String = "paid",
    var fundId: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    var note: String = ""
) {
    val isPaid    get() = status == "paid"
    val isPending get() = status == "pending"
}
