package com.khaga.ksociety.database

import android.content.ContentValues
import android.database.Cursor
import com.khaga.ksociety.database.DbContract.FundEntry
import com.khaga.ksociety.database.DbContract.TABLE_FUNDS
import com.khaga.ksociety.model.Fund

class FundDao(private val db: AppDatabase) {

    fun insert(fund: Fund): Long {
        return db.writableDatabase.insert(TABLE_FUNDS, null, fund.toContentValues())
    }

    fun update(fund: Fund): Boolean {
        val rows = db.writableDatabase.update(
            TABLE_FUNDS, fund.toContentValues(),
            "${FundEntry.ID}=?", arrayOf(fund.id.toString())
        )
        return rows > 0
    }

    fun delete(id: Long): Boolean {
        val rows = db.writableDatabase.delete(TABLE_FUNDS, "${FundEntry.ID}=?", arrayOf(id.toString()))
        return rows > 0
    }

    fun getAll(): List<Fund> {
        val cursor = db.readableDatabase.query(
            TABLE_FUNDS, null, null, null, null, null,
            "${FundEntry.CREATED_AT} DESC"
        )
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toFund()) } }
    }

    fun getById(id: Long): Fund? {
        val cursor = db.readableDatabase.query(
            TABLE_FUNDS, null,
            "${FundEntry.ID}=?", arrayOf(id.toString()),
            null, null, null
        )
        return cursor.use { c -> if (c.moveToFirst()) c.toFund() else null }
    }

    fun getOverdueCount(): Int {
        val cursor = db.readableDatabase.rawQuery(
            "SELECT SUM(${FundEntry.OVERDUE}) FROM $TABLE_FUNDS WHERE ${FundEntry.STATUS}='overdue'", null
        )
        return cursor.use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    // ── Mappers ─────────────────────────────────────────────────────────────
    private fun Cursor.toFund() = Fund(
        id           = getLong(getColumnIndexOrThrow(FundEntry.ID)),
        name         = getString(getColumnIndexOrThrow(FundEntry.NAME)) ?: "",
        totalMembers = getInt(getColumnIndexOrThrow(FundEntry.TOTAL_MEMBERS)),
        monthlyAmount= getLong(getColumnIndexOrThrow(FundEntry.MONTHLY)),
        memberFee    = getLong(getColumnIndexOrThrow(FundEntry.MEMBER_FEE)),
        interestRate = getFloat(getColumnIndexOrThrow(FundEntry.INT_RATE)),
        status       = getString(getColumnIndexOrThrow(FundEntry.STATUS)) ?: "allpaid",
        overdueCount = getInt(getColumnIndexOrThrow(FundEntry.OVERDUE)),
        closedDate   = getString(getColumnIndexOrThrow(FundEntry.CLOSED_DATE)),
        dotColor     = getString(getColumnIndexOrThrow(FundEntry.DOT_COLOR)) ?: "#1F5C3A",
        createdAt    = getLong(getColumnIndexOrThrow(FundEntry.CREATED_AT))
    )

    private fun Fund.toContentValues() = ContentValues().apply {
        put(FundEntry.NAME,          name)
        put(FundEntry.TOTAL_MEMBERS, totalMembers)
        put(FundEntry.MONTHLY,       monthlyAmount)
        put(FundEntry.MEMBER_FEE,    memberFee)
        put(FundEntry.INT_RATE,      interestRate)
        put(FundEntry.STATUS,        status)
        put(FundEntry.OVERDUE,       overdueCount)
        put(FundEntry.CLOSED_DATE,   closedDate)
        put(FundEntry.DOT_COLOR,     dotColor)
        put(FundEntry.CREATED_AT,    createdAt)
    }
}
