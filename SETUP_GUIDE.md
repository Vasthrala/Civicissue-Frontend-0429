# CivicIssue — Complete Setup Guide

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| **Python** | 3.10+ | https://www.python.org/downloads/ |
| **XAMPP** (MySQL/MariaDB) | Any recent | https://www.apachefriends.org/download.html |
| **Android Studio** | Latest | https://developer.android.com/studio |
| **Git** | Any | https://git-scm.com/downloads |
| **JDK** | 17+ | Bundled with Android Studio |

---

## Step 1: Clone the Repository

```bash
git clone <your-repo-url>
cd CivicIssue
```

---

## Step 2: Backend Setup

### 2.1 Start MySQL

1. Open **XAMPP Control Panel**
2. Click **Start** next to **MySQL**
3. Verify it's running (port 3306)

### 2.2 Create the Database

Open a terminal and run:

```bash
# Windows (XAMPP default path)
"C:/xampp/mysql/bin/mysql" -u root -e "CREATE DATABASE IF NOT EXISTS civicissue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Linux/Mac
mysql -u root -e "CREATE DATABASE IF NOT EXISTS civicissue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

> If your MySQL has a root password, add `-p` flag and enter it when prompted.

### 2.3 Configure Environment Variables

```bash
cd backend
cp .env.example .env
```

Edit `backend/.env` with your values:

```env
# Database — update password if your MySQL root has one
DATABASE_URL=mysql+pymysql://root:@localhost:3306/civicissue
# If root has a password: mysql+pymysql://root:YOUR_PASSWORD@localhost:3306/civicissue

# JWT — generate a secure key
# Run: python -c "import secrets; print(secrets.token_urlsafe(64))"
JWT_SECRET_KEY=PASTE_GENERATED_KEY_HERE
JWT_ALGORITHM=HS256
JWT_EXPIRY_MINUTES=1440

# Gemini AI — get from https://aistudio.google.com/apikey
GEMINI_API_KEY=YOUR_GEMINI_API_KEY

# SMTP Email (for OTP verification)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=YOUR_GMAIL_APP_PASSWORD

# File Uploads
UPLOAD_DIR=./uploads
MAX_FILE_SIZE_MB=10

# Geocoding
NOMINATIM_USER_AGENT=civicissue-app
```

### 2.4 Gmail App Password (for OTP emails)

1. Go to https://myaccount.google.com/security
2. Enable **2-Step Verification**
3. Go to https://myaccount.google.com/apppasswords
4. Select **Mail** → Generate
5. Copy the 16-character password → paste as `SMTP_PASSWORD` in `.env`

### 2.5 Gemini API Key

1. Go to https://aistudio.google.com/apikey
2. Click **Create API Key**
3. Copy → paste as `GEMINI_API_KEY` in `.env`

### 2.6 Install Python Dependencies

```bash
cd backend
pip install -r requirements.txt
```

> **Important**: If you get bcrypt errors, ensure bcrypt 4.1.3 is installed:
> ```bash
> pip install bcrypt==4.1.3
> ```

### 2.7 Run Database Migrations

```bash
cd backend
alembic upgrade head
```

This creates all 13 tables and seeds default data (categories, departments, admin user).

### 2.8 Start the Backend

```bash
cd backend
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

**Verify:** Open http://localhost:8000/docs in a browser — you should see the Swagger UI.

**Default Admin Credentials:**
```
Email:    admin@civicissue.com
Password: admin123
```

---

## Step 3: Frontend Setup (Android)

### 3.1 Find Your Laptop's IP Address

```bash
# Windows
ipconfig | findstr "IPv4"

# Mac
ifconfig | grep "inet " | grep -v 127.0.0.1

# Linux
hostname -I
```

Note your IP (e.g., `192.168.1.100`).

### 3.2 Update Backend IP in the App

Edit: `app/src/main/java/com/simats/civicissue/RetrofitClient.kt`

```kotlin
// Line ~11 — change to your laptop's IP
const val BASE_URL = "http://YOUR_LAPTOP_IP:8000/"
```

**Examples:**
- Physical device on same WiFi: `http://192.168.1.100:8000/`
- Android Emulator: `http://10.0.2.2:8000/`

### 3.3 Google Maps API Key

1. Go to https://console.cloud.google.com/
2. Create or select a project
3. Go to **APIs & Services → Library** → Enable **"Maps SDK for Android"**
4. Go to **APIs & Services → Credentials** → **Create Credentials → API Key**
5. Copy the key

Edit: `app/src/main/AndroidManifest.xml`

```xml
<!-- Find this line and replace the value -->
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY" />
```

### 3.4 Build the App

Open the project in Android Studio, or from terminal:

```bash
# Debug build
./gradlew assembleDebug

# Release build (signed)
./gradlew assembleRelease
```

APK locations:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### 3.5 Install on Device

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or run directly from Android Studio.

---

## Step 4: Verify Everything Works

### Backend Checklist

| Test | Command | Expected |
|------|---------|----------|
| Health check | `curl http://localhost:8000/health` | `{"status":"ok"}` |
| Swagger docs | Open `http://localhost:8000/docs` | Interactive API docs |
| Admin login | POST `/api/auth/login` with admin creds | Returns JWT token |
| Categories | GET `/api/admin/categories` with token | Returns 8 categories |

### Frontend Checklist

| Test | Action | Expected |
|------|--------|----------|
| Splash screen | Open app | Logo + "CivicIssue" text, navigates to role selection |
| Login | Enter admin@civicissue.com / admin123 | Navigates to admin dashboard |
| Signup | Create new citizen account | OTP sent to email, verify, account created |
| Report issue | Capture photo, locate, submit | Complaint created with AI analysis |
| Admin panel | View complaints, assign officers | Full admin functionality |

---

## Project Structure

```
CivicIssue/
├── app/                          # Android app (Kotlin + Jetpack Compose)
│   ├── src/main/java/com/simats/civicissue/
│   │   ├── MainActivity.kt       # Navigation graph (all routes)
│   │   ├── RetrofitClient.kt     # ★ BASE_URL config here
│   │   ├── CivicApiService.kt    # All 40+ API endpoints
│   │   ├── TokenManager.kt       # JWT token storage
│   │   ├── ComplaintModels.kt    # All data models
│   │   ├── ImageUtils.kt         # Bitmap/Uri → MultipartBody helpers
│   │   ├── MapLocationPicker.kt  # Google Maps location picker
│   │   ├── MapViewCard.kt        # Embedded map display card
│   │   ├── FullScreenImageViewer.kt
│   │   └── *Screen.kt            # All UI screens (~30 files)
│   ├── src/main/AndroidManifest.xml  # ★ Maps API key here
│   └── build.gradle.kts
│
├── backend/                      # FastAPI backend (Python)
│   ├── main.py                   # App entry point + router wiring + seed data
│   ├── config.py                 # Settings from .env
│   ├── database.py               # SQLAlchemy engine + session
│   ├── .env                      # ★ All secrets here (gitignored)
│   ├── .env.example              # Template for .env
│   ├── requirements.txt          # Python dependencies
│   ├── alembic.ini               # Migration config
│   ├── alembic/                  # Database migrations
│   ├── models/                   # 13 SQLAlchemy ORM models
│   ├── schemas/                  # Pydantic request/response schemas
│   ├── routers/                  # 9 API route groups (48 endpoints)
│   ├── services/                 # Business logic (auth, complaints, AI, etc.)
│   ├── middleware/               # JWT auth middleware
│   ├── utils/                    # Security + helpers
│   └── uploads/                  # Stored images (gitignored)
│
├── assets/                       # App logo
├── docs/plans/                   # Implementation plan
├── civicissue-release.jks        # Release keystore (gitignored)
├── SETUP_GUIDE.md                # This file
├── BACKEND_PRD.md                # Product requirements document
└── .gitignore
```

---

## Quick Reference

### Files You'll Change on a New Laptop

| File | What to Change | Why |
|------|---------------|-----|
| `backend/.env` | Database URL, API keys, SMTP credentials | Secrets per environment |
| `RetrofitClient.kt` (line ~11) | `BASE_URL` IP address | Your laptop's local IP |
| `AndroidManifest.xml` | Google Maps API key | Per Google Cloud project |

### Common Commands

```bash
# Start backend
cd backend && uvicorn main:app --reload --host 0.0.0.0 --port 8000

# Find your IP (Windows)
ipconfig | findstr "IPv4"

# Kill process on port 8000
netstat -ano | findstr :8000
taskkill /PID <PID> /F

# Build Android debug APK
./gradlew assembleDebug

# Build Android release APK
./gradlew assembleRelease

# Run database migrations
cd backend && alembic upgrade head

# Generate new JWT secret
python -c "import secrets; print(secrets.token_urlsafe(64))"
```

### API Endpoints Summary

| Group | Prefix | Endpoints |
|-------|--------|-----------|
| Auth | `/api/auth` | signup, login, verify-email, forgot-password, verify-otp, reset-password, change-password, resend-otp |
| Users | `/api/users` | me (GET/PUT), me/avatar |
| Complaints | `/api/complaints` | CRUD, status update, assign, resolve, history, similar, map-data, stats |
| Images | `/api/images` | upload, analyze (Gemini AI) |
| AI | `/api/ai` | analyze-text, chatbot, chatbot/history |
| Notifications | `/api/notifications` | list, unread-count, mark read, mark all read |
| Admin | `/api/admin` | categories CRUD, departments CRUD, officers, system-logs, dashboard-stats |
| Groups | `/api/groups` | list, detail |
| Geocoding | `/api/geo` | reverse geocode |

### Release Keystore Info

```
File:     civicissue-release.jks
Alias:    civicissue
Password: CivicIssue2026
Validity: 10,000 days (~27 years)
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `bcrypt` error on startup | `pip install bcrypt==4.1.3` |
| Port 8000 already in use | `netstat -ano \| findstr :8000` then `taskkill /PID <PID> /F` |
| MySQL connection refused | Start MySQL in XAMPP Control Panel |
| Android can't reach backend | Ensure phone and laptop are on same WiFi, check IP in RetrofitClient.kt |
| Images not loading in app | Check BASE_URL matches backend IP, ensure backend is running |
| OTP email not sending | Verify Gmail App Password in .env, ensure 2FA is enabled on Gmail |
| Maps not showing | Check Google Maps API key in AndroidManifest.xml, ensure "Maps SDK for Android" is enabled in Google Cloud Console |
| GPS not working | Check location permissions in app settings, ensure GPS is turned on |
| `alembic upgrade head` fails | Ensure MySQL is running and DATABASE_URL in .env is correct |
| App crashes on launch | Check `adb logcat` for errors, usually a missing dependency or wrong IP |
