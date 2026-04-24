# BC Committee — Android Native App

A full-featured **Chit Fund / BC Committee management app** built with **Android native XML layouts**, **SQLite local database**, and **Swagger REST API backup integration**.

---

## 📱 Screens & Features

| Screen | Features |
|---|---|
| **Splash** | Animated launch, DB pre-warm |
| **Home / Funds** | Fund cards with status pills, summary stats, FAB to create |
| **Fund Detail** | 4-tab ViewPager2: Dashboard · Members · Payments · Reports |
| **Dashboard Tab** | Fund pool, monthly collected, interest, quick-action buttons |
| **Members Tab** | Live search, add member with avatar initials, loan display |
| **Payments Tab** | Filter chips (All/Paid/Pending/Interest), record payment dialog |
| **Reports Tab** | Expandable monthly cards, share/export via Intent |
| **Add Fund** | Live preview card, validation, saves to SQLite |
| **Add Member** | Avatar auto-generated from initials, loan amount field |
| **Backup & Restore** | Swagger API integration, configurable endpoint, progress UI |

---

## 🏗️ Project Structure

```
app/src/main/
├── java/com/bccommittee/
│   ├── activity/
│   │   ├── SplashActivity.java
│   │   ├── MainActivity.java          ← Fund list + header stats
│   │   ├── FundDetailActivity.java    ← ViewPager2 with 4 tabs
│   │   ├── AddFundActivity.java
│   │   ├── AddMemberActivity.java
│   │   └── BackupActivity.java        ← Swagger API backup/restore
│   ├── fragment/
│   │   ├── DashboardFragment.java
│   │   ├── MembersFragment.java
│   │   ├── PaymentsFragment.java
│   │   └── ReportsFragment.java
│   ├── adapter/
│   │   ├── FundAdapter.java
│   │   ├── MemberAdapter.java
│   │   ├── PaymentAdapter.java
│   │   └── ReportAdapter.java
│   ├── database/
│   │   └── DatabaseHelper.java        ← SQLite: 4 tables, CRUD, seed data
│   ├── model/
│   │   ├── Fund.java
│   │   ├── Member.java
│   │   ├── Payment.java
│   │   ├── MonthlyReport.java
│   │   └── BackupData.java
│   ├── api/
│   │   ├── BCCommitteeApiService.java ← Retrofit2 + Swagger endpoints
│   │   ├── RetrofitClient.java        ← OkHttp singleton with logging
│   │   ├── ApiResponse.java
│   │   └── BackupSummary.java
│   └── util/
│       ├── BackupManager.java
│       └── CurrencyUtil.java
└── res/
    ├── layout/                        ← 14 XML layout files
    ├── values/                        ← colors, strings, themes, dimens
    └── drawable/                      ← 18 shape drawables
```

---

## 🗄️ SQLite Database Schema

### Tables

**funds**
```sql
id INTEGER PK, name TEXT, total_members INTEGER,
monthly_amount INTEGER, member_fee INTEGER,
interest_rate REAL, status TEXT,
overdue_count INTEGER, closed_date TEXT,
dot_color TEXT, created_at INTEGER
```

**members**
```sql
id INTEGER PK, name TEXT, phone TEXT, avatar TEXT,
contribution INTEGER, amt_borrowed INTEGER, fees INTEGER,
join_date TEXT, fund_id INTEGER FK, created_at INTEGER
```

**payments**
```sql
id INTEGER PK, member_id INTEGER, member_name TEXT,
member_avatar TEXT, type TEXT, amount INTEGER,
principal INTEGER, interest INTEGER, penalty INTEGER,
member_fee INTEGER, date TEXT, status TEXT,
fund_id INTEGER FK, note TEXT, created_at INTEGER
```

**monthly_reports**
```sql
id INTEGER PK, month TEXT, collected INTEGER,
interest_in INTEGER, penalties INTEGER, fees INTEGER,
total_fund INTEGER, defaults INTEGER, fund_id INTEGER FK
```

---

## 🌐 Swagger API Integration

### Default endpoint
```
https://petstore.swagger.io/v2/
```
*(Demo only — swap with your own backend)*

### API Endpoints (Retrofit2 interface)

```java
POST   /backup              → Upload full BackupData JSON
GET    /backup/{deviceId}   → Download latest backup
GET    /backup/list          → List all backup snapshots
POST   /backup/validate     → Validate backup integrity
```

### BackupData JSON structure
```json
{
  "appVersion": "1.0.0",
  "backupTimestamp": 1714000000000,
  "deviceId": "abc123",
  "funds":    [ { "id":1, "name":"Sarvajana Chit", ... } ],
  "members":  [ { "id":1, "name":"Ravi Kumar", ... } ],
  "payments": [ { "id":1, "amount":200, ... } ],
  "reports":  [ { "month":"April 2025", ... } ]
}
```

### OkHttp interceptors added
- `HttpLoggingInterceptor` — full request/response logging
- Custom header interceptor — adds `X-App-Name`, `X-App-Version`, `Content-Type`

---

## 🚀 How to Build

### Requirements
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34
- Gradle 8.4

### Steps
```bash
# 1. Open project in Android Studio
File → Open → select BCCommitteeApp/

# 2. Sync Gradle
Android Studio → Sync Project with Gradle Files

# 3. Run on device or emulator
Run → Run 'app'  (minSdk 24 = Android 7.0+)
```

---

## 🎨 Design System (matches prototype)

| Token | Value |
|---|---|
| Primary Green | `#1F5C3A` |
| Accent Green | `#1F7A4A` |
| Background | `#F5F6F0` |
| Surface | `#FFFFFF` |
| Text Primary | `#1A1F1C` |
| Text Muted | `#8A9690` |
| Orange | `#D4600A` |
| Blue | `#1A56A0` |
| Red | `#C0392B` |

---

## 📦 Key Dependencies

```groovy
// UI
com.google.android.material:material:1.11.0
androidx.constraintlayout:constraintlayout:2.1.4
androidx.viewpager2:viewpager2 (via navigation)

// Networking (Swagger/REST)
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0
com.squareup.okhttp3:logging-interceptor:4.12.0

// Charts (Reports)
com.github.PhilJay:MPAndroidChart:v3.1.0

// Animation
com.airbnb.android:lottie:6.3.0
```

---

## 🔧 Customising the API Server

1. Open **Backup & Restore** screen in the app
2. Enter your server base URL (e.g. `https://api.yourserver.com/v1/`)
3. Tap **Save Endpoint**
4. Your server must implement the 4 REST endpoints above
5. See `BCCommitteeApiService.java` for the Retrofit interface

---

## 📊 Seed Data (auto-loaded on first run)

| Fund | Members | Status |
|---|---|---|
| Sarvajana Chit | 6 | Overdue (2) |
| Family Gold Chit | 2 | All Paid |
| Office Savings | 0 | Overdue (1) |
| Festival Chit 2024 | 0 | Closed |

---

*Built to match the BC Committee React prototype — same color palette, same data model, same UX flows.*
