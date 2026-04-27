package com.khaga.ksociety.model

data class Member(
    val id: Long = 0,
    var name: String = "",
    var phone: String = "",
    var avatar: String = "",
    var contribution: Long = 0,
    var amtBorrowed: Long = 0,
    var fees: Long = 0,
    var joinDate: String = "",
    var fundId: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val monthlyInterest: Long
        get() = if (amtBorrowed <= 0) 0L else (amtBorrowed * 0.02).toLong()

    companion object {
        fun generateAvatar(name: String): String {
            if (name.isBlank()) return "?"
            return name.trim()
                .split("\\s+".toRegex())
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifEmpty { "?" }
        }
    }
}
