package com.khaga.ksociety.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.khaga.ksociety.database.DbContract.FundEntry
import com.khaga.ksociety.database.DbContract.MemberEntry
import com.khaga.ksociety.database.DbContract.PaymentEntry
import com.khaga.ksociety.database.DbContract.ReportEntry
import com.khaga.ksociety.database.DbContract.TABLE_FUNDS
import com.khaga.ksociety.database.DbContract.TABLE_MEMBERS
import com.khaga.ksociety.database.DbContract.TABLE_PAYMENTS
import com.khaga.ksociety.database.DbContract.TABLE_REPORTS

/**
 * AppDatabase — thin SQLiteOpenHelper.
 * Only responsible for schema creation/upgrade and seed data.
 * All CRUD is delegated to separate DAO classes.
 */
class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val TAG        = "AppDatabase"
        private const val DB_NAME    = "bc_committee.db"
        private const val DB_VERSION = 1

        @Volatile private var instance: AppDatabase? = null

        fun getInstance(ctx: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: AppDatabase(ctx).also { instance = it }
            }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON;")
        db.execSQL(FundEntry.CREATE)
        db.execSQL(MemberEntry.CREATE)
        db.execSQL(PaymentEntry.CREATE)
        db.execSQL(ReportEntry.CREATE)
        Log.d(TAG, "Database created (blank — no seed data)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Upgrading database from $oldVersion to $newVersion")
        dropAll(db)
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA foreign_keys = ON;")
    }

    // ── Schema helpers ─────────────────────────────────────────────────────
    private fun dropAll(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPORTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PAYMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEMBERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FUNDS")
    }

    fun clearAllData() {
        writableDatabase.use { db ->
            db.execSQL("PRAGMA foreign_keys = OFF;")
            db.delete(TABLE_REPORTS,  null, null)
            db.delete(TABLE_PAYMENTS, null, null)
            db.delete(TABLE_MEMBERS,  null, null)
            db.delete(TABLE_FUNDS,    null, null)
            runCatching { db.execSQL("DELETE FROM sqlite_sequence") }
            db.execSQL("PRAGMA foreign_keys = ON;")
        }
        Log.d(TAG, "All data cleared")
    }

    fun seedSampleData() {
        val db = writableDatabase
        val now = System.currentTimeMillis()

        // ── Funds ──────────────────────────────────────────────────────────
        fun insertFund(
            name: String, members: Int, monthly: Long, fee: Long,
            rate: Float, status: String, overdue: Int,
            closed: String? = null, color: String = "#1F5C3A"
        ): Long = db.insert(TABLE_FUNDS, null, ContentValues().apply {
            put(FundEntry.NAME, name)
            put(FundEntry.TOTAL_MEMBERS, members)
            put(FundEntry.MONTHLY, monthly)
            put(FundEntry.MEMBER_FEE, fee)
            put(FundEntry.INT_RATE, rate)
            put(FundEntry.STATUS, status)
            put(FundEntry.OVERDUE, overdue)
            put(FundEntry.CLOSED_DATE, closed)
            put(FundEntry.DOT_COLOR, color)
            put(FundEntry.CREATED_AT, now)
        })

        val f1 = insertFund("Sarvajana Chit",   10, 10000, 500, 2f, "overdue", 2, color = "#1F5C3A")
        val f2 = insertFund("Family Gold Chit",  6, 25000, 750, 2f, "allpaid", 0, color = "#D4600A")
                 insertFund("Office Savings",   15,  5000, 300, 2f, "overdue", 1, color = "#1A56A0")
                 insertFund("Festival Chit 2024",8,     0,   0, 0f, "closed",  0, "Dec 2024", "#B0B8B4")

        // ── Members ────────────────────────────────────────────────────────
        fun insertMember(
            name: String, avatar: String, contrib: Long, loan: Long,
            fee: Long, joinDate: String, fundId: Long
        ) = db.insert(TABLE_MEMBERS, null, ContentValues().apply {
            put(MemberEntry.NAME, name); put(MemberEntry.PHONE, "")
            put(MemberEntry.AVATAR, avatar); put(MemberEntry.CONTRIBUTION, contrib)
            put(MemberEntry.BORROWED, loan); put(MemberEntry.FEES, fee)
            put(MemberEntry.JOIN_DATE, joinDate); put(MemberEntry.FUND_ID, fundId)
            put(MemberEntry.CREATED_AT, now)
        })

        insertMember("Ravi Kumar",   "RK", 2000, 10000, 500, "Jan 2025", f1)
        insertMember("Sunita Devi",  "SD", 2000,  8000, 500, "Jan 2025", f1)
        insertMember("Mohan Rao",    "MR", 2000,     0, 500, "Jan 2025", f1)
        insertMember("Priya Sharma", "PS", 2000, 15000, 500, "Jan 2025", f1)
        insertMember("Ajay Singh",   "AS", 2000,  5000, 500, "Feb 2025", f1)
        insertMember("Kavitha N.",   "KN", 2000,     0, 500, "Feb 2025", f1)
        insertMember("Deepak M.",    "DM", 3000, 12000, 750, "Jan 2025", f2)
        insertMember("Lakshmi P.",   "LP", 3000,     0, 750, "Jan 2025", f2)

        // ── Payments ───────────────────────────────────────────────────────
        data class P(val mId: Long, val name: String, val av: String,
                     val type: String, val amt: Long, val interest: Long,
                     val fee: Long, val date: String, val status: String)

        listOf(
            P(1, "Ravi Kumar",   "RK", "Interest",     200,  200,   0, "Apr 05", "paid"),
            P(2, "Sunita Devi",  "SD", "Interest",     160,  160,   0, "Apr 07", "paid"),
            P(4, "Priya Sharma", "PS", "Interest",     300,  300,   0, "Apr 10", "paid"),
            P(5, "Ajay Singh",   "AS", "Interest",     100,  100,   0, "Apr 12", "pending"),
            P(3, "Mohan Rao",    "MR", "Contribution", 2000,   0, 500, "Apr 01", "paid")
        ).forEach { p ->
            db.insert(TABLE_PAYMENTS, null, ContentValues().apply {
                put(PaymentEntry.MEMBER_ID,     p.mId)
                put(PaymentEntry.MEMBER_NAME,   p.name)
                put(PaymentEntry.MEMBER_AVATAR, p.av)
                put(PaymentEntry.TYPE,          p.type)
                put(PaymentEntry.AMOUNT,        p.amt)
                put(PaymentEntry.PRINCIPAL,     0L)
                put(PaymentEntry.INTEREST,      p.interest)
                put(PaymentEntry.PENALTY,       0L)
                put(PaymentEntry.MEMBER_FEE,    p.fee)
                put(PaymentEntry.DATE,          p.date)
                put(PaymentEntry.STATUS,        p.status)
                put(PaymentEntry.FUND_ID,       f1)
                put(PaymentEntry.CREATED_AT,    now)
            })
        }

        // ── Reports ────────────────────────────────────────────────────────
        data class R(val month: String, val collected: Long, val interest: Long,
                     val penalties: Long, val fees: Long, val pool: Long, val def: Int)

        listOf(
            R("April 2025",    12000, 760, 200, 3000, 48000, 1),
            R("March 2025",    12000, 900,   0, 3000, 36000, 0),
            R("February 2025", 12000, 650, 400, 3000, 24000, 2),
            R("January 2025",  12000,   0,   0, 3000, 12000, 0)
        ).forEach { r ->
            db.insert(TABLE_REPORTS, null, ContentValues().apply {
                put(ReportEntry.MONTH,      r.month)
                put(ReportEntry.COLLECTED,  r.collected)
                put(ReportEntry.INTEREST,   r.interest)
                put(ReportEntry.PENALTIES,  r.penalties)
                put(ReportEntry.FEES,       r.fees)
                put(ReportEntry.TOTAL_FUND, r.pool)
                put(ReportEntry.DEFAULTS,   r.def)
                put(ReportEntry.FUND_ID,    f1)
            })
        }

        Log.d(TAG, "Sample data seeded")
    }
}
