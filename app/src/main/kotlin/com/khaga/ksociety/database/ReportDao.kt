package com.khaga.ksociety.database

import android.content.ContentValues
import android.database.Cursor
import com.khaga.ksociety.database.DbContract.ReportEntry
import com.khaga.ksociety.database.DbContract.TABLE_REPORTS
import com.khaga.ksociety.model.MonthlyReport

class ReportDao(private val db: AppDatabase) {

    fun insert(report: MonthlyReport): Long {
        return db.writableDatabase.insert(TABLE_REPORTS, null, report.toContentValues())
    }

    fun getByFund(fundId: Long): List<MonthlyReport> {
        val cursor = db.readableDatabase.query(
            TABLE_REPORTS, null,
            "${ReportEntry.FUND_ID}=?", arrayOf(fundId.toString()),
            null, null, "${ReportEntry.ID} DESC"
        )
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toReport()) } }
    }

    fun getAll(): List<MonthlyReport> {
        val cursor = db.readableDatabase.query(
            TABLE_REPORTS, null, null, null, null, null, "${ReportEntry.ID} DESC"
        )
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toReport()) } }
    }

    // ── Mappers ────────────────────────────────────────────────────────────
    private fun Cursor.toReport() = MonthlyReport(
        id         = getLong(getColumnIndexOrThrow(ReportEntry.ID)),
        month      = getString(getColumnIndexOrThrow(ReportEntry.MONTH)) ?: "",
        collected  = getLong(getColumnIndexOrThrow(ReportEntry.COLLECTED)),
        interestIn = getLong(getColumnIndexOrThrow(ReportEntry.INTEREST)),
        penalties  = getLong(getColumnIndexOrThrow(ReportEntry.PENALTIES)),
        fees       = getLong(getColumnIndexOrThrow(ReportEntry.FEES)),
        totalFund  = getLong(getColumnIndexOrThrow(ReportEntry.TOTAL_FUND)),
        defaults   = getInt(getColumnIndexOrThrow(ReportEntry.DEFAULTS)),
        fundId     = getLong(getColumnIndexOrThrow(ReportEntry.FUND_ID))
    )

    private fun MonthlyReport.toContentValues() = ContentValues().apply {
        put(ReportEntry.MONTH,      month)
        put(ReportEntry.COLLECTED,  collected)
        put(ReportEntry.INTEREST,   interestIn)
        put(ReportEntry.PENALTIES,  penalties)
        put(ReportEntry.FEES,       fees)
        put(ReportEntry.TOTAL_FUND, totalFund)
        put(ReportEntry.DEFAULTS,   defaults)
        put(ReportEntry.FUND_ID,    fundId)
    }
}
