package com.khaga.ksociety.database

import android.content.ContentValues
import android.database.Cursor
import com.khaga.ksociety.database.DbContract.MemberEntry
import com.khaga.ksociety.database.DbContract.TABLE_MEMBERS
import com.khaga.ksociety.model.Member

class MemberDao(private val db: AppDatabase) {

    fun insert(member: Member): Long {
        val database = db.writableDatabase
        return database.insert(TABLE_MEMBERS, null, member.toContentValues())
    }

    fun update(member: Member): Boolean {
        val database = db.writableDatabase
        val rows = database.update(
            TABLE_MEMBERS,
            member.toContentValues(),
            "${MemberEntry.ID}=?",
            arrayOf(member.id.toString())
        )
        return rows > 0
    }

    fun delete(id: Long): Boolean {
        val database = db.writableDatabase
        val rows = database.delete(TABLE_MEMBERS, "${MemberEntry.ID}=?", arrayOf(id.toString()))
        return rows > 0
    }

    fun getByFund(fundId: Long): List<Member> {
        val database = db.readableDatabase
        val cursor = database.query(
            TABLE_MEMBERS, null,
            "${MemberEntry.FUND_ID}=?", arrayOf(fundId.toString()),
            null, null, "${MemberEntry.NAME} ASC"
        )
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toMember()) } }
    }

    fun search(fundId: Long, query: String): List<Member> {
        val database = db.readableDatabase
        val like = "%$query%"
        val cursor = database.query(
            TABLE_MEMBERS, null,
            "${MemberEntry.FUND_ID}=? AND (${MemberEntry.NAME} LIKE ? OR ${MemberEntry.PHONE} LIKE ?)",
            arrayOf(fundId.toString(), like, like),
            null, null, "${MemberEntry.NAME} ASC"
        )
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toMember()) } }
    }

    fun getById(id: Long): Member? {
        val database = db.readableDatabase
        val cursor = database.query(
            TABLE_MEMBERS, null,
            "${MemberEntry.ID}=?", arrayOf(id.toString()),
            null, null, null
        )
        return cursor.use { c -> if (c.moveToFirst()) c.toMember() else null }
    }

    fun getAll(): List<Member> {
        val database = db.readableDatabase
        val cursor = database.query(TABLE_MEMBERS, null, null, null, null, null, null)
        return cursor.use { c -> buildList { while (c.moveToNext()) add(c.toMember()) } }
    }

    fun getTotalCount(): Int {
        val database = db.readableDatabase
        val cursor = database.rawQuery("SELECT COUNT(*) FROM $TABLE_MEMBERS", null)
        return cursor.use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }
    }

    // ── Mappers ────────────────────────────────────────────────────────────
    private fun Cursor.toMember() = Member(
        id           = getLong(getColumnIndexOrThrow(MemberEntry.ID)),
        name         = getString(getColumnIndexOrThrow(MemberEntry.NAME)) ?: "",
        phone        = getString(getColumnIndexOrThrow(MemberEntry.PHONE)) ?: "",
        avatar       = getString(getColumnIndexOrThrow(MemberEntry.AVATAR)) ?: "",
        contribution = getLong(getColumnIndexOrThrow(MemberEntry.CONTRIBUTION)),
        amtBorrowed  = getLong(getColumnIndexOrThrow(MemberEntry.BORROWED)),
        fees         = getLong(getColumnIndexOrThrow(MemberEntry.FEES)),
        joinDate     = getString(getColumnIndexOrThrow(MemberEntry.JOIN_DATE)) ?: "",
        fundId       = getLong(getColumnIndexOrThrow(MemberEntry.FUND_ID)),
        createdAt    = getLong(getColumnIndexOrThrow(MemberEntry.CREATED_AT))
    )

    private fun Member.toContentValues() = ContentValues().apply {
        put(MemberEntry.NAME,         name)
        put(MemberEntry.PHONE,        phone)
        put(MemberEntry.AVATAR,       avatar)
        put(MemberEntry.CONTRIBUTION, contribution)
        put(MemberEntry.BORROWED,     amtBorrowed)
        put(MemberEntry.FEES,         fees)
        put(MemberEntry.JOIN_DATE,    joinDate)
        put(MemberEntry.FUND_ID,      fundId)
        put(MemberEntry.CREATED_AT,   createdAt)
    }
}
