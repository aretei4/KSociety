package com.khaga.ksociety.database

import android.content.ContentValues
import android.database.Cursor
import com.khaga.ksociety.database.DbContract.PaymentEntry
import com.khaga.ksociety.database.DbContract.TABLE_PAYMENTS
import com.khaga.ksociety.model.Payment

class PaymentDao(private val db: AppDatabase) {

    fun insert(payment: Payment): Long {
        return db.writableDatabase.insert(TABLE_PAYMENTS, null, payment.toContentValues())
    }

    fun delete(id: Long): Boolean {
        val rows = db.writableDatabase.delete(TABLE_PAYMENTS, "${PaymentEntry.ID}=?", arrayOf(id.toString()))
        return rows > 0
    }

    fun getByFund(fundId: Long): List<Payment> {
        val cursor = db.readableDatabase.query(
            TABLE_PAYMENTS, null,
            "${PaymentEntry.FUND_ID}=?", arrayOf(fundId.toString()),
            null, null, "${PaymentEntry.CREATED_AT} DESC"
        )
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toPayment()) } }
    }

    fun getByMember(memberId: Long): List<Payment> {
        val cursor = db.readableDatabase.query(
            TABLE_PAYMENTS, null,
            "${PaymentEntry.MEMBER_ID}=?", arrayOf(memberId.toString()),
            null, null, "${PaymentEntry.CREATED_AT} DESC"
        )
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toPayment()) } }
    }

    fun getAll(): List<Payment> {
        val cursor = db.readableDatabase.query(TABLE_PAYMENTS, null, null, null, null, null, null)
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toPayment()) } }
    }

    // ── Mappers ────────────────────────────────────────────────────────────
    private fun Cursor.toPayment() = Payment(
        id           = getLong(getColumnIndexOrThrow(PaymentEntry.ID)),
        memberId     = getLong(getColumnIndexOrThrow(PaymentEntry.MEMBER_ID)),
        memberName   = getString(getColumnIndexOrThrow(PaymentEntry.MEMBER_NAME)) ?: "",
        memberAvatar = getString(getColumnIndexOrThrow(PaymentEntry.MEMBER_AVATAR)) ?: "",
        type         = getString(getColumnIndexOrThrow(PaymentEntry.TYPE)) ?: "",
        amount       = getLong(getColumnIndexOrThrow(PaymentEntry.AMOUNT)),
        principal    = getLong(getColumnIndexOrThrow(PaymentEntry.PRINCIPAL)),
        interest     = getLong(getColumnIndexOrThrow(PaymentEntry.INTEREST)),
        penalty      = getLong(getColumnIndexOrThrow(PaymentEntry.PENALTY)),
        memberFee    = getLong(getColumnIndexOrThrow(PaymentEntry.MEMBER_FEE)),
        date         = getString(getColumnIndexOrThrow(PaymentEntry.DATE)) ?: "",
        status       = getString(getColumnIndexOrThrow(PaymentEntry.STATUS)) ?: "paid",
        fundId       = getLong(getColumnIndexOrThrow(PaymentEntry.FUND_ID)),
        createdAt    = getLong(getColumnIndexOrThrow(PaymentEntry.CREATED_AT)),
        note         = getString(getColumnIndexOrThrow(PaymentEntry.NOTE)) ?: ""
    )

    private fun Payment.toContentValues() = ContentValues().apply {
        put(PaymentEntry.MEMBER_ID,     memberId)
        put(PaymentEntry.MEMBER_NAME,   memberName)
        put(PaymentEntry.MEMBER_AVATAR, memberAvatar)
        put(PaymentEntry.TYPE,          type)
        put(PaymentEntry.AMOUNT,        amount)
        put(PaymentEntry.PRINCIPAL,     principal)
        put(PaymentEntry.INTEREST,      interest)
        put(PaymentEntry.PENALTY,       penalty)
        put(PaymentEntry.MEMBER_FEE,    memberFee)
        put(PaymentEntry.DATE,          date)
        put(PaymentEntry.STATUS,        status)
        put(PaymentEntry.FUND_ID,       fundId)
        put(PaymentEntry.NOTE,          note)
        put(PaymentEntry.CREATED_AT,    createdAt)
    }
}
