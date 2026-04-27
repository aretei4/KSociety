package com.khaga.ksociety.database

object DbContract {

    // ── Table names ────────────────────────────────────────────────────────
    const val TABLE_FUNDS    = "funds"
    const val TABLE_MEMBERS  = "members"
    const val TABLE_PAYMENTS = "payments"
    const val TABLE_REPORTS  = "monthly_reports"

    // ── Funds columns ──────────────────────────────────────────────────────
    object FundEntry {
        const val ID           = "id"
        const val NAME         = "name"
        const val TOTAL_MEMBERS = "total_members"
        const val MONTHLY      = "monthly_amount"
        const val MEMBER_FEE   = "member_fee"
        const val INT_RATE     = "interest_rate"
        const val STATUS       = "status"
        const val OVERDUE      = "overdue_count"
        const val CLOSED_DATE  = "closed_date"
        const val DOT_COLOR    = "dot_color"
        const val CREATED_AT   = "created_at"

        const val CREATE = """
            CREATE TABLE $TABLE_FUNDS (
                $ID            INTEGER PRIMARY KEY AUTOINCREMENT,
                $NAME          TEXT NOT NULL,
                $TOTAL_MEMBERS INTEGER DEFAULT 0,
                $MONTHLY       INTEGER DEFAULT 0,
                $MEMBER_FEE    INTEGER DEFAULT 0,
                $INT_RATE      REAL    DEFAULT 2.0,
                $STATUS        TEXT    DEFAULT 'allpaid',
                $OVERDUE       INTEGER DEFAULT 0,
                $CLOSED_DATE   TEXT,
                $DOT_COLOR     TEXT    DEFAULT '#1F5C3A',
                $CREATED_AT    INTEGER NOT NULL
            )"""
    }

    // ── Members columns ────────────────────────────────────────────────────
    object MemberEntry {
        const val ID           = "id"
        const val NAME         = "name"
        const val PHONE        = "phone"
        const val AVATAR       = "avatar"
        const val CONTRIBUTION = "contribution"
        const val BORROWED     = "amt_borrowed"
        const val FEES         = "fees"
        const val JOIN_DATE    = "join_date"
        const val FUND_ID      = "fund_id"
        const val CREATED_AT   = "created_at"

        const val CREATE = """
            CREATE TABLE $TABLE_MEMBERS (
                $ID           INTEGER PRIMARY KEY AUTOINCREMENT,
                $NAME         TEXT NOT NULL,
                $PHONE        TEXT,
                $AVATAR       TEXT,
                $CONTRIBUTION INTEGER DEFAULT 0,
                $BORROWED     INTEGER DEFAULT 0,
                $FEES         INTEGER DEFAULT 0,
                $JOIN_DATE    TEXT,
                $FUND_ID      INTEGER NOT NULL,
                $CREATED_AT   INTEGER NOT NULL,
                FOREIGN KEY($FUND_ID) REFERENCES $TABLE_FUNDS($ID) ON DELETE CASCADE
            )"""
    }

    // ── Payments columns ───────────────────────────────────────────────────
    object PaymentEntry {
        const val ID            = "id"
        const val MEMBER_ID     = "member_id"
        const val MEMBER_NAME   = "member_name"
        const val MEMBER_AVATAR = "member_avatar"
        const val TYPE          = "type"
        const val AMOUNT        = "amount"
        const val PRINCIPAL     = "principal"
        const val INTEREST      = "interest"
        const val PENALTY       = "penalty"
        const val MEMBER_FEE    = "member_fee"
        const val DATE          = "date"
        const val STATUS        = "status"
        const val FUND_ID       = "fund_id"
        const val NOTE          = "note"
        const val CREATED_AT    = "created_at"

        const val CREATE = """
            CREATE TABLE $TABLE_PAYMENTS (
                $ID            INTEGER PRIMARY KEY AUTOINCREMENT,
                $MEMBER_ID     INTEGER,
                $MEMBER_NAME   TEXT,
                $MEMBER_AVATAR TEXT,
                $TYPE          TEXT,
                $AMOUNT        INTEGER DEFAULT 0,
                $PRINCIPAL     INTEGER DEFAULT 0,
                $INTEREST      INTEGER DEFAULT 0,
                $PENALTY       INTEGER DEFAULT 0,
                $MEMBER_FEE    INTEGER DEFAULT 0,
                $DATE          TEXT,
                $STATUS        TEXT DEFAULT 'paid',
                $FUND_ID       INTEGER,
                $NOTE          TEXT,
                $CREATED_AT    INTEGER NOT NULL,
                FOREIGN KEY($FUND_ID) REFERENCES $TABLE_FUNDS($ID) ON DELETE CASCADE
            )"""
    }

    // ── Reports columns ────────────────────────────────────────────────────
    object ReportEntry {
        const val ID         = "id"
        const val MONTH      = "month"
        const val COLLECTED  = "collected"
        const val INTEREST   = "interest_in"
        const val PENALTIES  = "penalties"
        const val FEES       = "fees"
        const val TOTAL_FUND = "total_fund"
        const val DEFAULTS   = "defaults"
        const val FUND_ID    = "fund_id"

        const val CREATE = """
            CREATE TABLE $TABLE_REPORTS (
                $ID         INTEGER PRIMARY KEY AUTOINCREMENT,
                $MONTH      TEXT,
                $COLLECTED  INTEGER DEFAULT 0,
                $INTEREST   INTEGER DEFAULT 0,
                $PENALTIES  INTEGER DEFAULT 0,
                $FEES       INTEGER DEFAULT 0,
                $TOTAL_FUND INTEGER DEFAULT 0,
                $DEFAULTS   INTEGER DEFAULT 0,
                $FUND_ID    INTEGER,
                FOREIGN KEY($FUND_ID) REFERENCES $TABLE_FUNDS($ID) ON DELETE CASCADE
            )"""
    }
}
