package com.bccommittee.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bccommittee.model.Fund;
import com.bccommittee.model.Member;
import com.bccommittee.model.MonthlyReport;
import com.bccommittee.model.Payment;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "bc_committee.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_FUNDS    = "funds";
    public static final String TABLE_MEMBERS  = "members";
    public static final String TABLE_PAYMENTS = "payments";
    public static final String TABLE_REPORTS  = "monthly_reports";

    // Common columns
    public static final String COL_ID         = "id";
    public static final String COL_CREATED_AT = "created_at";

    // Funds columns
    public static final String COL_FUND_NAME         = "name";
    public static final String COL_FUND_TOTAL_MEMBERS = "total_members";
    public static final String COL_FUND_MONTHLY       = "monthly_amount";
    public static final String COL_FUND_MEMBER_FEE    = "member_fee";
    public static final String COL_FUND_INT_RATE      = "interest_rate";
    public static final String COL_FUND_STATUS        = "status";
    public static final String COL_FUND_OVERDUE       = "overdue_count";
    public static final String COL_FUND_CLOSED_DATE   = "closed_date";
    public static final String COL_FUND_DOT_COLOR     = "dot_color";

    // Members columns
    public static final String COL_MEM_NAME         = "name";
    public static final String COL_MEM_PHONE        = "phone";
    public static final String COL_MEM_AVATAR       = "avatar";
    public static final String COL_MEM_CONTRIBUTION = "contribution";
    public static final String COL_MEM_BORROWED     = "amt_borrowed";
    public static final String COL_MEM_FEES         = "fees";
    public static final String COL_MEM_JOIN_DATE    = "join_date";
    public static final String COL_MEM_FUND_ID      = "fund_id";

    // Payments columns
    public static final String COL_PAY_MEMBER_ID     = "member_id";
    public static final String COL_PAY_MEMBER_NAME   = "member_name";
    public static final String COL_PAY_MEMBER_AVATAR = "member_avatar";
    public static final String COL_PAY_TYPE          = "type";
    public static final String COL_PAY_AMOUNT        = "amount";
    public static final String COL_PAY_PRINCIPAL     = "principal";
    public static final String COL_PAY_INTEREST      = "interest";
    public static final String COL_PAY_PENALTY       = "penalty";
    public static final String COL_PAY_MEMBER_FEE    = "member_fee";
    public static final String COL_PAY_DATE          = "date";
    public static final String COL_PAY_STATUS        = "status";
    public static final String COL_PAY_FUND_ID       = "fund_id";
    public static final String COL_PAY_NOTE          = "note";

    // Reports columns
    public static final String COL_REP_MONTH      = "month";
    public static final String COL_REP_COLLECTED  = "collected";
    public static final String COL_REP_INTEREST   = "interest_in";
    public static final String COL_REP_PENALTIES  = "penalties";
    public static final String COL_REP_FEES       = "fees";
    public static final String COL_REP_TOTAL_FUND = "total_fund";
    public static final String COL_REP_DEFAULTS   = "defaults";
    public static final String COL_REP_FUND_ID    = "fund_id";

    // CREATE TABLE statements
    private static final String CREATE_FUNDS =
        "CREATE TABLE " + TABLE_FUNDS + " (" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_FUND_NAME + " TEXT NOT NULL, " +
        COL_FUND_TOTAL_MEMBERS + " INTEGER DEFAULT 0, " +
        COL_FUND_MONTHLY + " INTEGER DEFAULT 0, " +
        COL_FUND_MEMBER_FEE + " INTEGER DEFAULT 0, " +
        COL_FUND_INT_RATE + " REAL DEFAULT 2.0, " +
        COL_FUND_STATUS + " TEXT DEFAULT 'allpaid', " +
        COL_FUND_OVERDUE + " INTEGER DEFAULT 0, " +
        COL_FUND_CLOSED_DATE + " TEXT, " +
        COL_FUND_DOT_COLOR + " TEXT DEFAULT '#1F5C3A', " +
        COL_CREATED_AT + " INTEGER NOT NULL" +
        ");";

    private static final String CREATE_MEMBERS =
        "CREATE TABLE " + TABLE_MEMBERS + " (" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_MEM_NAME + " TEXT NOT NULL, " +
        COL_MEM_PHONE + " TEXT, " +
        COL_MEM_AVATAR + " TEXT, " +
        COL_MEM_CONTRIBUTION + " INTEGER DEFAULT 0, " +
        COL_MEM_BORROWED + " INTEGER DEFAULT 0, " +
        COL_MEM_FEES + " INTEGER DEFAULT 0, " +
        COL_MEM_JOIN_DATE + " TEXT, " +
        COL_MEM_FUND_ID + " INTEGER NOT NULL, " +
        COL_CREATED_AT + " INTEGER NOT NULL, " +
        "FOREIGN KEY(" + COL_MEM_FUND_ID + ") REFERENCES " + TABLE_FUNDS + "(" + COL_ID + ") ON DELETE CASCADE" +
        ");";

    private static final String CREATE_PAYMENTS =
        "CREATE TABLE " + TABLE_PAYMENTS + " (" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_PAY_MEMBER_ID + " INTEGER, " +
        COL_PAY_MEMBER_NAME + " TEXT, " +
        COL_PAY_MEMBER_AVATAR + " TEXT, " +
        COL_PAY_TYPE + " TEXT, " +
        COL_PAY_AMOUNT + " INTEGER DEFAULT 0, " +
        COL_PAY_PRINCIPAL + " INTEGER DEFAULT 0, " +
        COL_PAY_INTEREST + " INTEGER DEFAULT 0, " +
        COL_PAY_PENALTY + " INTEGER DEFAULT 0, " +
        COL_PAY_MEMBER_FEE + " INTEGER DEFAULT 0, " +
        COL_PAY_DATE + " TEXT, " +
        COL_PAY_STATUS + " TEXT DEFAULT 'paid', " +
        COL_PAY_FUND_ID + " INTEGER, " +
        COL_PAY_NOTE + " TEXT, " +
        COL_CREATED_AT + " INTEGER NOT NULL, " +
        "FOREIGN KEY(" + COL_PAY_FUND_ID + ") REFERENCES " + TABLE_FUNDS + "(" + COL_ID + ") ON DELETE CASCADE" +
        ");";

    private static final String CREATE_REPORTS =
        "CREATE TABLE " + TABLE_REPORTS + " (" +
        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COL_REP_MONTH + " TEXT, " +
        COL_REP_COLLECTED + " INTEGER DEFAULT 0, " +
        COL_REP_INTEREST + " INTEGER DEFAULT 0, " +
        COL_REP_PENALTIES + " INTEGER DEFAULT 0, " +
        COL_REP_FEES + " INTEGER DEFAULT 0, " +
        COL_REP_TOTAL_FUND + " INTEGER DEFAULT 0, " +
        COL_REP_DEFAULTS + " INTEGER DEFAULT 0, " +
        COL_REP_FUND_ID + " INTEGER, " +
        "FOREIGN KEY(" + COL_REP_FUND_ID + ") REFERENCES " + TABLE_FUNDS + "(" + COL_ID + ") ON DELETE CASCADE" +
        ");";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys = ON;");
        db.execSQL(CREATE_FUNDS);
        db.execSQL(CREATE_MEMBERS);
        db.execSQL(CREATE_PAYMENTS);
        db.execSQL(CREATE_REPORTS);
        seedSampleData(db);
        Log.d(TAG, "Database created & seeded");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FUNDS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    // ─────────────────── SEED DATA ───────────────────
    private void seedSampleData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();

        // Funds
        ContentValues cv = new ContentValues();
        cv.put(COL_FUND_NAME, "Sarvajana Chit");
        cv.put(COL_FUND_TOTAL_MEMBERS, 10);
        cv.put(COL_FUND_MONTHLY, 10000);
        cv.put(COL_FUND_MEMBER_FEE, 500);
        cv.put(COL_FUND_INT_RATE, 2.0f);
        cv.put(COL_FUND_STATUS, "overdue");
        cv.put(COL_FUND_OVERDUE, 2);
        cv.put(COL_FUND_DOT_COLOR, "#1F5C3A");
        cv.put(COL_CREATED_AT, now);
        long f1 = db.insert(TABLE_FUNDS, null, cv);

        cv.clear();
        cv.put(COL_FUND_NAME, "Family Gold Chit");
        cv.put(COL_FUND_TOTAL_MEMBERS, 6);
        cv.put(COL_FUND_MONTHLY, 25000);
        cv.put(COL_FUND_MEMBER_FEE, 750);
        cv.put(COL_FUND_INT_RATE, 2.0f);
        cv.put(COL_FUND_STATUS, "allpaid");
        cv.put(COL_FUND_OVERDUE, 0);
        cv.put(COL_FUND_DOT_COLOR, "#D4600A");
        cv.put(COL_CREATED_AT, now);
        long f2 = db.insert(TABLE_FUNDS, null, cv);

        cv.clear();
        cv.put(COL_FUND_NAME, "Office Savings");
        cv.put(COL_FUND_TOTAL_MEMBERS, 15);
        cv.put(COL_FUND_MONTHLY, 5000);
        cv.put(COL_FUND_MEMBER_FEE, 300);
        cv.put(COL_FUND_INT_RATE, 2.0f);
        cv.put(COL_FUND_STATUS, "overdue");
        cv.put(COL_FUND_OVERDUE, 1);
        cv.put(COL_FUND_DOT_COLOR, "#1A56A0");
        cv.put(COL_CREATED_AT, now);
        long f3 = db.insert(TABLE_FUNDS, null, cv);

        cv.clear();
        cv.put(COL_FUND_NAME, "Festival Chit 2024");
        cv.put(COL_FUND_TOTAL_MEMBERS, 8);
        cv.put(COL_FUND_MONTHLY, 0);
        cv.put(COL_FUND_MEMBER_FEE, 0);
        cv.put(COL_FUND_INT_RATE, 0.0f);
        cv.put(COL_FUND_STATUS, "closed");
        cv.put(COL_FUND_OVERDUE, 0);
        cv.put(COL_FUND_CLOSED_DATE, "Dec 2024");
        cv.put(COL_FUND_DOT_COLOR, "#B0B8B4");
        cv.put(COL_CREATED_AT, now);
        db.insert(TABLE_FUNDS, null, cv);

        // Members for f1
        String[][] membersF1 = {
            {"Ravi Kumar",    "", "RK", "2000", "10000", "500", "Jan 2025"},
            {"Sunita Devi",   "", "SD", "2000", "8000",  "500", "Jan 2025"},
            {"Mohan Rao",     "", "MR", "2000", "0",     "500", "Jan 2025"},
            {"Priya Sharma",  "", "PS", "2000", "15000", "500", "Jan 2025"},
            {"Ajay Singh",    "", "AS", "2000", "5000",  "500", "Feb 2025"},
            {"Kavitha N.",    "", "KN", "2000", "0",     "500", "Feb 2025"},
        };
        for (String[] m : membersF1) {
            cv.clear();
            cv.put(COL_MEM_NAME, m[0]);
            cv.put(COL_MEM_PHONE, m[1]);
            cv.put(COL_MEM_AVATAR, m[2]);
            cv.put(COL_MEM_CONTRIBUTION, Long.parseLong(m[3]));
            cv.put(COL_MEM_BORROWED, Long.parseLong(m[4]));
            cv.put(COL_MEM_FEES, Long.parseLong(m[5]));
            cv.put(COL_MEM_JOIN_DATE, m[6]);
            cv.put(COL_MEM_FUND_ID, f1);
            cv.put(COL_CREATED_AT, now);
            db.insert(TABLE_MEMBERS, null, cv);
        }

        // Members for f2
        String[][] membersF2 = {
            {"Deepak M.",  "", "DM", "3000", "12000", "750", "Jan 2025"},
            {"Lakshmi P.", "", "LP", "3000", "0",     "750", "Jan 2025"},
        };
        for (String[] m : membersF2) {
            cv.clear();
            cv.put(COL_MEM_NAME, m[0]);
            cv.put(COL_MEM_PHONE, m[1]);
            cv.put(COL_MEM_AVATAR, m[2]);
            cv.put(COL_MEM_CONTRIBUTION, Long.parseLong(m[3]));
            cv.put(COL_MEM_BORROWED, Long.parseLong(m[4]));
            cv.put(COL_MEM_FEES, Long.parseLong(m[5]));
            cv.put(COL_MEM_JOIN_DATE, m[6]);
            cv.put(COL_MEM_FUND_ID, f2);
            cv.put(COL_CREATED_AT, now);
            db.insert(TABLE_MEMBERS, null, cv);
        }

        // Sample payments
        Object[][] payments = {
            {1L, "Ravi Kumar",   "RK", "Interest",     200L,  0L, 200L,  0L, 0L,   "Apr 05", "paid"},
            {2L, "Sunita Devi",  "SD", "Interest",     160L,  0L, 160L,  0L, 0L,   "Apr 07", "paid"},
            {4L, "Priya Sharma", "PS", "Interest",     300L,  0L, 300L,  0L, 0L,   "Apr 10", "paid"},
            {5L, "Ajay Singh",   "AS", "Interest",     100L,  0L, 100L,  0L, 0L,   "Apr 12", "pending"},
            {3L, "Mohan Rao",    "MR", "Contribution", 2000L, 0L, 0L,    0L, 500L, "Apr 01", "paid"},
        };
        for (Object[] p : payments) {
            cv.clear();
            cv.put(COL_PAY_MEMBER_ID, (Long) p[0]);
            cv.put(COL_PAY_MEMBER_NAME, (String) p[1]);
            cv.put(COL_PAY_MEMBER_AVATAR, (String) p[2]);
            cv.put(COL_PAY_TYPE, (String) p[3]);
            cv.put(COL_PAY_AMOUNT, (Long) p[4]);
            cv.put(COL_PAY_PRINCIPAL, (Long) p[5]);
            cv.put(COL_PAY_INTEREST, (Long) p[6]);
            cv.put(COL_PAY_PENALTY, (Long) p[7]);
            cv.put(COL_PAY_MEMBER_FEE, (Long) p[8]);
            cv.put(COL_PAY_DATE, (String) p[9]);
            cv.put(COL_PAY_STATUS, (String) p[10]);
            cv.put(COL_PAY_FUND_ID, f1);
            cv.put(COL_CREATED_AT, now);
            db.insert(TABLE_PAYMENTS, null, cv);
        }

        // Sample monthly reports
        Object[][] reports = {
            {"April 2025",    12000L, 760L, 200L,  3000L, 48000L, 1},
            {"March 2025",    12000L, 900L, 0L,    3000L, 36000L, 0},
            {"February 2025", 12000L, 650L, 400L,  3000L, 24000L, 2},
            {"January 2025",  12000L, 0L,   0L,    3000L, 12000L, 0},
        };
        for (Object[] r : reports) {
            cv.clear();
            cv.put(COL_REP_MONTH, (String) r[0]);
            cv.put(COL_REP_COLLECTED, (Long) r[1]);
            cv.put(COL_REP_INTEREST, (Long) r[2]);
            cv.put(COL_REP_PENALTIES, (Long) r[3]);
            cv.put(COL_REP_FEES, (Long) r[4]);
            cv.put(COL_REP_TOTAL_FUND, (Long) r[5]);
            cv.put(COL_REP_DEFAULTS, (int) r[6]);
            cv.put(COL_REP_FUND_ID, f1);
            db.insert(TABLE_REPORTS, null, cv);
        }
    }

    // ─────────────────── FUNDS CRUD ───────────────────
    public long insertFund(Fund fund) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = fundToContentValues(fund);
        long id = db.insert(TABLE_FUNDS, null, cv);
        db.close();
        return id;
    }

    public boolean updateFund(Fund fund) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = fundToContentValues(fund);
        int rows = db.update(TABLE_FUNDS, cv, COL_ID + "=?",
                new String[]{String.valueOf(fund.getId())});
        db.close();
        return rows > 0;
    }

    public boolean deleteFund(long fundId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_FUNDS, COL_ID + "=?",
                new String[]{String.valueOf(fundId)});
        db.close();
        return rows > 0;
    }

    public List<Fund> getAllFunds() {
        List<Fund> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_FUNDS, null, null, null, null, null,
                COL_FUND_STATUS + " ASC, " + COL_CREATED_AT + " DESC");
        if (c.moveToFirst()) {
            do { list.add(cursorToFund(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public Fund getFundById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_FUNDS, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Fund fund = null;
        if (c.moveToFirst()) fund = cursorToFund(c);
        c.close();
        db.close();
        return fund;
    }

    // ─────────────────── MEMBERS CRUD ───────────────────
    public long insertMember(Member member) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = memberToContentValues(member);
        long id = db.insert(TABLE_MEMBERS, null, cv);
        db.close();
        return id;
    }

    public boolean updateMember(Member member) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = memberToContentValues(member);
        int rows = db.update(TABLE_MEMBERS, cv, COL_ID + "=?",
                new String[]{String.valueOf(member.getId())});
        db.close();
        return rows > 0;
    }

    public boolean deleteMember(long memberId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_MEMBERS, COL_ID + "=?",
                new String[]{String.valueOf(memberId)});
        db.close();
        return rows > 0;
    }

    public List<Member> getMembersByFund(long fundId) {
        List<Member> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_MEMBERS, null, COL_MEM_FUND_ID + "=?",
                new String[]{String.valueOf(fundId)}, null, null,
                COL_MEM_NAME + " ASC");
        if (c.moveToFirst()) {
            do { list.add(cursorToMember(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public Member getMemberById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_MEMBERS, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Member member = null;
        if (c.moveToFirst()) member = cursorToMember(c);
        c.close();
        db.close();
        return member;
    }

    public List<Member> searchMembers(long fundId, String query) {
        List<Member> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String where = COL_MEM_FUND_ID + "=? AND (" + COL_MEM_NAME + " LIKE ? OR " + COL_MEM_PHONE + " LIKE ?)";
        String like = "%" + query + "%";
        Cursor c = db.query(TABLE_MEMBERS, null, where,
                new String[]{String.valueOf(fundId), like, like}, null, null, COL_MEM_NAME);
        if (c.moveToFirst()) {
            do { list.add(cursorToMember(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // ─────────────────── PAYMENTS CRUD ───────────────────
    public long insertPayment(Payment payment) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = paymentToContentValues(payment);
        long id = db.insert(TABLE_PAYMENTS, null, cv);
        db.close();
        return id;
    }

    public List<Payment> getPaymentsByFund(long fundId) {
        List<Payment> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PAYMENTS, null, COL_PAY_FUND_ID + "=?",
                new String[]{String.valueOf(fundId)}, null, null,
                COL_CREATED_AT + " DESC");
        if (c.moveToFirst()) {
            do { list.add(cursorToPayment(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public List<Payment> getPaymentsByMember(long memberId) {
        List<Payment> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PAYMENTS, null, COL_PAY_MEMBER_ID + "=?",
                new String[]{String.valueOf(memberId)}, null, null,
                COL_CREATED_AT + " DESC");
        if (c.moveToFirst()) {
            do { list.add(cursorToPayment(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // ─────────────────── REPORTS CRUD ───────────────────
    public long insertReport(MonthlyReport report) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = reportToContentValues(report);
        long id = db.insert(TABLE_REPORTS, null, cv);
        db.close();
        return id;
    }

    public List<MonthlyReport> getReportsByFund(long fundId) {
        List<MonthlyReport> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_REPORTS, null, COL_REP_FUND_ID + "=?",
                new String[]{String.valueOf(fundId)}, null, null,
                COL_ID + " DESC");
        if (c.moveToFirst()) {
            do { list.add(cursorToReport(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public List<MonthlyReport> getAllReports() {
        List<MonthlyReport> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_REPORTS, null, null, null, null, null, COL_ID + " DESC");
        if (c.moveToFirst()) {
            do { list.add(cursorToReport(c)); } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // ─────────────────── STATS QUERIES ───────────────────
    public int getTotalMembersCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MEMBERS, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close(); db.close();
        return count;
    }

    public long getTotalCollected() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COALESCE(SUM(" + COL_PAY_AMOUNT + "), 0) FROM " + TABLE_PAYMENTS +
                " WHERE " + COL_PAY_STATUS + "='paid'", null);
        long total = 0;
        if (c.moveToFirst()) total = c.getLong(0);
        c.close(); db.close();
        return total;
    }

    public int getOverdueFundsCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FUNDS +
                " WHERE " + COL_FUND_STATUS + "='overdue'", null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close(); db.close();
        return count;
    }

    // Backup: get ALL data
    public List<Fund>         getAllFundsForBackup()    { return getAllFunds(); }
    public List<Member>       getAllMembersForBackup() {
        List<Member> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_MEMBERS, null, null, null, null, null, null);
        if (c.moveToFirst()) { do { list.add(cursorToMember(c)); } while (c.moveToNext()); }
        c.close(); db.close();
        return list;
    }
    public List<Payment>      getAllPaymentsForBackup() {
        List<Payment> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PAYMENTS, null, null, null, null, null, null);
        if (c.moveToFirst()) { do { list.add(cursorToPayment(c)); } while (c.moveToNext()); }
        c.close(); db.close();
        return list;
    }

    // ─────────────────── CURSOR MAPPERS ───────────────────
    private Fund cursorToFund(Cursor c) {
        return new Fund(
            c.getLong(c.getColumnIndexOrThrow(COL_ID)),
            c.getString(c.getColumnIndexOrThrow(COL_FUND_NAME)),
            c.getInt(c.getColumnIndexOrThrow(COL_FUND_TOTAL_MEMBERS)),
            c.getLong(c.getColumnIndexOrThrow(COL_FUND_MONTHLY)),
            c.getLong(c.getColumnIndexOrThrow(COL_FUND_MEMBER_FEE)),
            c.getFloat(c.getColumnIndexOrThrow(COL_FUND_INT_RATE)),
            c.getString(c.getColumnIndexOrThrow(COL_FUND_STATUS)),
            c.getInt(c.getColumnIndexOrThrow(COL_FUND_OVERDUE)),
            c.getString(c.getColumnIndexOrThrow(COL_FUND_CLOSED_DATE)),
            c.getString(c.getColumnIndexOrThrow(COL_FUND_DOT_COLOR)),
            c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT))
        );
    }

    private Member cursorToMember(Cursor c) {
        return new Member(
            c.getLong(c.getColumnIndexOrThrow(COL_ID)),
            c.getString(c.getColumnIndexOrThrow(COL_MEM_NAME)),
            c.getString(c.getColumnIndexOrThrow(COL_MEM_PHONE)),
            c.getString(c.getColumnIndexOrThrow(COL_MEM_AVATAR)),
            c.getLong(c.getColumnIndexOrThrow(COL_MEM_CONTRIBUTION)),
            c.getLong(c.getColumnIndexOrThrow(COL_MEM_BORROWED)),
            c.getLong(c.getColumnIndexOrThrow(COL_MEM_FEES)),
            c.getString(c.getColumnIndexOrThrow(COL_MEM_JOIN_DATE)),
            c.getLong(c.getColumnIndexOrThrow(COL_MEM_FUND_ID)),
            c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT))
        );
    }

    private Payment cursorToPayment(Cursor c) {
        return new Payment(
            c.getLong(c.getColumnIndexOrThrow(COL_ID)),
            c.getLong(c.getColumnIndexOrThrow(COL_PAY_MEMBER_ID)),
            c.getString(c.getColumnIndexOrThrow(COL_PAY_MEMBER_NAME)),
            c.getString(c.getColumnIndexOrThrow(COL_PAY_MEMBER_AVATAR)),
            c.getString(c.getColumnIndexOrThrow(COL_PAY_TYPE)),
            c.getLong(c.getColumnIndexOrThrow(COL_PAY_AMOUNT)),
            c.getLong(c.getColumnIndexOrThrow(COL_PAY_PRINCIPAL)),
            c.getLong(c.getColumnIndexOrThrow(COL_PAY_INTEREST)),
            c.getLong(c.getColumnIndexOrThrow(COL_PAY_PENALTY)),
            c.getLong(c.getColumnIndexOrThrow(COL_PAY_MEMBER_FEE)),
            c.getString(c.getColumnIndexOrThrow(COL_PAY_DATE)),
            c.getString(c.getColumnIndexOrThrow(COL_PAY_STATUS)),
            c.getLong(c.getColumnIndexOrThrow(COL_PAY_FUND_ID)),
            c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT)),
            c.getString(c.getColumnIndexOrThrow(COL_PAY_NOTE))
        );
    }

    private MonthlyReport cursorToReport(Cursor c) {
        return new MonthlyReport(
            c.getLong(c.getColumnIndexOrThrow(COL_ID)),
            c.getString(c.getColumnIndexOrThrow(COL_REP_MONTH)),
            c.getLong(c.getColumnIndexOrThrow(COL_REP_COLLECTED)),
            c.getLong(c.getColumnIndexOrThrow(COL_REP_INTEREST)),
            c.getLong(c.getColumnIndexOrThrow(COL_REP_PENALTIES)),
            c.getLong(c.getColumnIndexOrThrow(COL_REP_FEES)),
            c.getLong(c.getColumnIndexOrThrow(COL_REP_TOTAL_FUND)),
            c.getInt(c.getColumnIndexOrThrow(COL_REP_DEFAULTS)),
            c.getLong(c.getColumnIndexOrThrow(COL_REP_FUND_ID))
        );
    }

    // ─────────────────── ContentValues HELPERS ───────────────────
    private ContentValues fundToContentValues(Fund f) {
        ContentValues cv = new ContentValues();
        cv.put(COL_FUND_NAME, f.getName());
        cv.put(COL_FUND_TOTAL_MEMBERS, f.getTotalMembers());
        cv.put(COL_FUND_MONTHLY, f.getMonthlyAmount());
        cv.put(COL_FUND_MEMBER_FEE, f.getMemberFee());
        cv.put(COL_FUND_INT_RATE, f.getInterestRate());
        cv.put(COL_FUND_STATUS, f.getStatus());
        cv.put(COL_FUND_OVERDUE, f.getOverdueCount());
        cv.put(COL_FUND_CLOSED_DATE, f.getClosedDate());
        cv.put(COL_FUND_DOT_COLOR, f.getDotColor());
        cv.put(COL_CREATED_AT, f.getCreatedAt());
        return cv;
    }

    private ContentValues memberToContentValues(Member m) {
        ContentValues cv = new ContentValues();
        cv.put(COL_MEM_NAME, m.getName());
        cv.put(COL_MEM_PHONE, m.getPhone());
        cv.put(COL_MEM_AVATAR, m.getAvatar());
        cv.put(COL_MEM_CONTRIBUTION, m.getContribution());
        cv.put(COL_MEM_BORROWED, m.getAmtBorrowed());
        cv.put(COL_MEM_FEES, m.getFees());
        cv.put(COL_MEM_JOIN_DATE, m.getJoinDate());
        cv.put(COL_MEM_FUND_ID, m.getFundId());
        cv.put(COL_CREATED_AT, m.getCreatedAt());
        return cv;
    }

    private ContentValues paymentToContentValues(Payment p) {
        ContentValues cv = new ContentValues();
        cv.put(COL_PAY_MEMBER_ID, p.getMemberId());
        cv.put(COL_PAY_MEMBER_NAME, p.getMemberName());
        cv.put(COL_PAY_MEMBER_AVATAR, p.getMemberAvatar());
        cv.put(COL_PAY_TYPE, p.getType());
        cv.put(COL_PAY_AMOUNT, p.getAmount());
        cv.put(COL_PAY_PRINCIPAL, p.getPrincipal());
        cv.put(COL_PAY_INTEREST, p.getInterest());
        cv.put(COL_PAY_PENALTY, p.getPenalty());
        cv.put(COL_PAY_MEMBER_FEE, p.getMemberFee());
        cv.put(COL_PAY_DATE, p.getDate());
        cv.put(COL_PAY_STATUS, p.getStatus());
        cv.put(COL_PAY_FUND_ID, p.getFundId());
        cv.put(COL_PAY_NOTE, p.getNote());
        cv.put(COL_CREATED_AT, p.getCreatedAt());
        return cv;
    }

    private ContentValues reportToContentValues(MonthlyReport r) {
        ContentValues cv = new ContentValues();
        cv.put(COL_REP_MONTH, r.getMonth());
        cv.put(COL_REP_COLLECTED, r.getCollected());
        cv.put(COL_REP_INTEREST, r.getInterestIn());
        cv.put(COL_REP_PENALTIES, r.getPenalties());
        cv.put(COL_REP_FEES, r.getFees());
        cv.put(COL_REP_TOTAL_FUND, r.getTotalFund());
        cv.put(COL_REP_DEFAULTS, r.getDefaults());
        cv.put(COL_REP_FUND_ID, r.getFundId());
        return cv;
    }
}
