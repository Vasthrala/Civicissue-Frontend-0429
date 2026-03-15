# CivicIssue — MVP Backend PRD

## Crowdsourced Civic Issue Reporting and Resolution System using AI

---

## 1. Overview

This document defines the complete backend implementation plan for the CivicIssue mobile application. The app has two portals — **Citizen Portal** and **Admin Portal** — enabling citizens to report civic issues (potholes, garbage, broken street lights, water leakage, etc.) and administrators to manage, assign, track, and resolve them.

The backend powers all features including authentication, complaint management, AI-based image and text analysis, nearby issue grouping, notifications, geocoding, and an AI chatbot.

---

## 2. Tech Stack

| Layer              | Technology                                   |
| ------------------ | -------------------------------------------- |
| **Framework**      | FastAPI (Python)                             |
| **Database**       | MySQL + SQLAlchemy ORM + Alembic (migrations)|
| **Authentication** | JWT (python-jose) + bcrypt (passlib)         |
| **All AI**         | Google Gemini 2.5 Flash                      |
| **Image Storage**  | Local filesystem served via FastAPI static    |
| **Notifications**  | In-app only (DB-backed, polled via REST)     |
| **Geocoding**      | Nominatim (OpenStreetMap — free)             |
| **Email / OTP**    | SMTP (Gmail app password)                    |
| **HTTP Client**    | httpx (for Nominatim external calls)         |

> **Not included (MVP scope):** Redis, Celery, Firebase Cloud Messaging, Docker, local ML models, spaCy, TensorFlow.
> All AI functionality is handled by Gemini 2.5 Flash API calls — no local model infrastructure needed.

---

## 3. Current Frontend State

The Android app (Jetpack Compose + Kotlin) already has:

- **40 screens** across Citizen and Admin portals
- **Retrofit HTTP client** at `http://10.0.2.2:8000/`
- Camera/gallery image capture (returns `Bitmap` / `Uri`)
- Navigation graph with role-based routing
- Complaint data models, status enums, priority enums
- Mock data for notifications, reports, officers, and AI detection
- Distance calculation for nearby issues (2km radius)
- Category list: `Pothole, Street Light, Waste Collection, Water Leakage, Drainage, Other`

### Current Retrofit Interface

```kotlin
@GET("api/complaints")       suspend fun getComplaints(): List<Complaint>
@POST("api/complaints")      suspend fun createComplaint(@Body complaint: Complaint): Complaint
@GET("api/reports/recent")   suspend fun getRecentReports(): List<CitizenReportDto>
```

### What the Backend Must Replace

| Frontend Feature           | Current State           | Required Backend                               |
| -------------------------- | ----------------------- | ---------------------------------------------- |
| Login / Signup             | Plain text passwords    | JWT tokens + bcrypt hashing                    |
| Image capture              | Local bitmap only       | Upload to server + Gemini analysis             |
| AI severity detection      | 2-second fake delay     | Real Gemini 2.5 Flash inference                |
| Notifications              | Hardcoded mock data     | Real DB-backed notifications                   |
| Map / Location             | State variables only    | Geocoding API + map data endpoints             |
| Officer assignment         | Mock data               | Real officer management                        |
| Status history             | Not connected           | Full audit trail                               |
| Categories / Departments   | Hardcoded lists         | Dynamic from database                          |
| Chatbot                    | Hardcoded responses     | Real Gemini-powered chatbot                    |
| Nearby issue grouping      | Client-side mock        | Server-side grouping engine                    |
| Reports / Analytics        | Mock data               | Real aggregated statistics                     |

---

## 4. Database Schema

### 4.1 `users`

```sql
id              CHAR(36) PRIMARY KEY                    -- UUID
full_name       VARCHAR(255) NOT NULL
email           VARCHAR(255) UNIQUE NOT NULL
phone_number    VARCHAR(20)
country_code    VARCHAR(5)
password_hash   VARCHAR(255) NOT NULL                   -- bcrypt hashed
role            ENUM('citizen', 'admin', 'officer') NOT NULL
avatar_url      VARCHAR(500) NULL
is_verified     BOOLEAN DEFAULT FALSE
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

### 4.2 `complaints`

```sql
id                    CHAR(36) PRIMARY KEY
complaint_number      VARCHAR(20) UNIQUE                -- #CE-XXXX format
citizen_id            CHAR(36) NOT NULL                  -- FK → users.id
title                 VARCHAR(255) NOT NULL
description           TEXT
category              VARCHAR(100)                      -- final category (user can override AI)
ai_detected_category  VARCHAR(100)                      -- from Gemini image analysis
ai_text_category      VARCHAR(100)                      -- from Gemini text analysis
location_text         VARCHAR(500)                      -- human-readable address
latitude              DOUBLE NOT NULL
longitude             DOUBLE NOT NULL
priority              ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM'
severity_level        ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM'
status                ENUM('UNASSIGNED', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'RESOLVED') DEFAULT 'UNASSIGNED'
assigned_officer_id   CHAR(36) NULL                     -- FK → users.id
group_id              CHAR(36) NULL                     -- FK → issue_groups.id
ai_confidence         FLOAT                             -- 0.0 to 1.0
ai_keywords           JSON                              -- extracted keywords from text
resolution_notes      TEXT NULL
resolved_at           TIMESTAMP NULL
created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

### 4.3 `complaint_images`

```sql
id              CHAR(36) PRIMARY KEY
complaint_id    CHAR(36) NOT NULL                       -- FK → complaints.id
image_url       VARCHAR(500) NOT NULL
ai_analysis     JSON                                    -- full Gemini response
uploaded_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 4.4 `issue_groups`

```sql
id               CHAR(36) PRIMARY KEY
category         VARCHAR(100) NOT NULL
center_lat       DOUBLE NOT NULL
center_lng       DOUBLE NOT NULL
radius_meters    FLOAT DEFAULT 2000
complaint_count  INT DEFAULT 1
avg_severity     VARCHAR(20)
status           ENUM('ACTIVE', 'RESOLVED') DEFAULT 'ACTIVE'
created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

### 4.5 `notifications`

```sql
id              CHAR(36) PRIMARY KEY
recipient_id    CHAR(36) NOT NULL                       -- FK → users.id
complaint_id    CHAR(36) NULL                           -- FK → complaints.id
title           VARCHAR(255) NOT NULL
message         TEXT NOT NULL
type            ENUM('STATUS_UPDATE', 'NEW_ISSUE', 'ASSIGNMENT', 'RESOLUTION', 'SYSTEM') NOT NULL
priority        ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM'
is_read         BOOLEAN DEFAULT FALSE
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 4.6 `complaint_status_history`

```sql
id              CHAR(36) PRIMARY KEY
complaint_id    CHAR(36) NOT NULL                       -- FK → complaints.id
old_status      VARCHAR(50)
new_status      VARCHAR(50) NOT NULL
changed_by      CHAR(36) NOT NULL                       -- FK → users.id
notes           TEXT NULL
created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 4.7 `officers`

```sql
id              CHAR(36) PRIMARY KEY
user_id         CHAR(36) NOT NULL UNIQUE                -- FK → users.id
department      VARCHAR(100)
designation     VARCHAR(100)
workload_count  INT DEFAULT 0
is_available    BOOLEAN DEFAULT TRUE
```

### 4.8 `categories`

```sql
id          CHAR(36) PRIMARY KEY
name        VARCHAR(100) UNIQUE NOT NULL
description TEXT NULL
icon        VARCHAR(50) NULL
is_active   BOOLEAN DEFAULT TRUE
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 4.9 `departments`

```sql
id          CHAR(36) PRIMARY KEY
name        VARCHAR(100) UNIQUE NOT NULL
description TEXT NULL
is_active   BOOLEAN DEFAULT TRUE
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 4.10 `system_logs`

```sql
id            CHAR(36) PRIMARY KEY
action        VARCHAR(100) NOT NULL
entity_type   VARCHAR(50)
entity_id     CHAR(36)
performed_by  CHAR(36) NOT NULL                         -- FK → users.id
details       JSON NULL
created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 4.11 `otp_verifications`

```sql
id          CHAR(36) PRIMARY KEY
user_id     CHAR(36) NOT NULL                           -- FK → users.id
otp_code    VARCHAR(6) NOT NULL
purpose     ENUM('EMAIL_VERIFY', 'PASSWORD_RESET') NOT NULL
is_used     BOOLEAN DEFAULT FALSE
expires_at  TIMESTAMP NOT NULL
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 4.12 `chatbot_sessions`

```sql
id          CHAR(36) PRIMARY KEY
user_id     CHAR(36) NOT NULL                           -- FK → users.id
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### 4.13 `chatbot_messages`

```sql
id          CHAR(36) PRIMARY KEY
session_id  CHAR(36) NOT NULL                           -- FK → chatbot_sessions.id
text        TEXT NOT NULL
is_user     BOOLEAN NOT NULL
created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

---

## 5. API Endpoints

### 5.1 Authentication — `/api/auth`

| Method | Endpoint             | Description                              | Auth Required |
| ------ | -------------------- | ---------------------------------------- | ------------- |
| POST   | `/signup`            | Register new user, send OTP email        | No            |
| POST   | `/login`             | Validate credentials, return JWT + user  | No            |
| POST   | `/verify-email`      | Verify email with OTP code               | No            |
| POST   | `/forgot-password`   | Send password reset OTP to email         | No            |
| POST   | `/verify-otp`        | Validate OTP code for password reset     | No            |
| POST   | `/reset-password`    | Set new password after OTP verified      | No            |
| PUT    | `/change-password`   | Change password while logged in          | Yes           |

**Login Response:**

```json
{
  "access_token": "eyJhbGci...",
  "token_type": "bearer",
  "user": {
    "id": "uuid",
    "full_name": "John Doe",
    "email": "john@example.com",
    "role": "citizen",
    "is_verified": true,
    "avatar_url": null
  }
}
```

---

### 5.2 Complaints — `/api/complaints`

| Method | Endpoint              | Description                                           | Auth     |
| ------ | --------------------- | ----------------------------------------------------- | -------- |
| POST   | `/`                   | Create complaint (multipart: images + JSON body)      | Citizen  |
| GET    | `/`                   | List complaints (citizen sees own, admin sees all)    | Yes      |
| GET    | `/{id}`               | Get full complaint detail                             | Yes      |
| PUT    | `/{id}/status`        | Update status → logs history → notifies citizen       | Admin    |
| PUT    | `/{id}/assign`        | Assign officer → updates workload → notifies both     | Admin    |
| PUT    | `/{id}/resolve`       | Resolve with notes → notifies citizen                 | Admin    |
| GET    | `/{id}/history`       | Get status change timeline                            | Yes      |
| GET    | `/{id}/similar`       | Get nearby complaints (same category, within 2km)     | Yes      |
| GET    | `/map-data`           | All complaints as markers: `{id, lat, lng, category, severity, status}` | Yes |
| GET    | `/stats`              | Dashboard stats for admin                             | Admin    |

**Query Parameters for `GET /`:**

| Parameter  | Type   | Description                    |
| ---------- | ------ | ------------------------------ |
| `status`   | string | Filter by status               |
| `category` | string | Filter by category             |
| `priority` | string | Filter by priority             |
| `severity` | string | Filter by severity             |
| `search`   | string | Search in title/description    |
| `page`     | int    | Page number (default: 1)       |
| `limit`    | int    | Items per page (default: 20)   |

**Stats Response:**

```json
{
  "total": 150,
  "unassigned": 30,
  "assigned": 25,
  "in_progress": 40,
  "resolved": 55,
  "by_category": { "pothole": 45, "garbage": 30, ... },
  "by_severity": { "LOW": 20, "MEDIUM": 60, "HIGH": 50, "CRITICAL": 20 },
  "recent_7_days": 23
}
```

---

### 5.3 Image Upload & AI Analysis — `/api/images`

| Method | Endpoint    | Description                                          | Auth |
| ------ | ----------- | ---------------------------------------------------- | ---- |
| POST   | `/upload`   | Upload image → save to disk → return URL             | Yes  |
| POST   | `/analyze`  | Upload image → Gemini analysis → return results      | Yes  |

**Analyze Response:**

```json
{
  "image_url": "/uploads/abc123.jpg",
  "detected_category": "pothole",
  "severity_level": "HIGH",
  "confidence_score": 0.92,
  "tags": ["road_damage", "pothole", "asphalt", "crack"],
  "description_suggestion": "Large pothole detected on road surface with visible cracks"
}
```

---

### 5.4 AI Text Processing & Chatbot — `/api/ai`

| Method | Endpoint            | Description                                         | Auth |
| ------ | ------------------- | --------------------------------------------------- | ---- |
| POST   | `/analyze-text`     | Gemini analyzes description → keywords + category   | Yes  |
| POST   | `/chatbot`          | Send message → Gemini responds with context         | Yes  |
| GET    | `/chatbot/history`  | Get user's chat history                             | Yes  |

**Analyze Text Response:**

```json
{
  "detected_category": "water_leakage",
  "keywords": ["pipe", "burst", "water", "flooding", "road"],
  "suggested_priority": "HIGH",
  "urgency_indicator": "Contains urgent language: 'flooding', 'burst pipe'"
}
```

**Chatbot Request:**

```json
{
  "message": "What is the status of my complaint #CE-1234?",
  "session_id": "uuid-or-null"
}
```

---

### 5.5 Issue Groups — `/api/groups`

| Method | Endpoint   | Description                                      | Auth  |
| ------ | ---------- | ------------------------------------------------ | ----- |
| GET    | `/`        | List all active groups with complaint counts     | Admin |
| GET    | `/{id}`    | Group detail with all member complaints          | Admin |

---

### 5.6 Notifications — `/api/notifications`

| Method | Endpoint        | Description                  | Auth |
| ------ | --------------- | ---------------------------- | ---- |
| GET    | `/`             | Get user's notifications     | Yes  |
| GET    | `/unread-count` | Return `{ "count": N }`     | Yes  |
| PUT    | `/{id}/read`    | Mark one as read             | Yes  |
| PUT    | `/read-all`     | Mark all as read             | Yes  |

**Auto-Generated Notifications:**

| Event                       | Recipient(s)         | Title                                                    |
| --------------------------- | -------------------- | -------------------------------------------------------- |
| New complaint created       | All admins           | "New {category} issue reported at {location}"            |
| Status → ASSIGNED           | Reporting citizen    | "Your issue #{number} has been assigned to {officer}"    |
| Status → IN_PROGRESS        | Reporting citizen    | "Work has started on your issue #{number}"               |
| Status → RESOLVED           | Reporting citizen    | "Your issue #{number} has been resolved"                 |
| Officer assigned            | Assigned officer     | "You have been assigned to issue #{number}"              |

---

### 5.7 Users & Profile — `/api/users`

| Method | Endpoint      | Description                       | Auth |
| ------ | ------------- | --------------------------------- | ---- |
| GET    | `/me`         | Get current user profile          | Yes  |
| PUT    | `/me`         | Update name, phone, country code  | Yes  |
| PUT    | `/me/avatar`  | Upload avatar image               | Yes  |

---

### 5.8 Admin Management — `/api/admin`

| Method | Endpoint             | Description                           | Auth  |
| ------ | -------------------- | ------------------------------------- | ----- |
| GET    | `/categories`        | List all categories                   | Admin |
| POST   | `/categories`        | Create category                       | Admin |
| PUT    | `/categories/{id}`   | Update category                       | Admin |
| DELETE | `/categories/{id}`   | Soft delete (set is_active = false)   | Admin |
| GET    | `/departments`       | List all departments                  | Admin |
| POST   | `/departments`       | Create department                     | Admin |
| PUT    | `/departments/{id}`  | Update department                     | Admin |
| DELETE | `/departments/{id}`  | Soft delete                           | Admin |
| GET    | `/officers`          | List officers with workload info      | Admin |
| POST   | `/officers`          | Create officer (user + officer record)| Admin |
| GET    | `/system-logs`       | Get paginated audit logs              | Admin |
| GET    | `/dashboard-stats`   | Full dashboard statistics             | Admin |

---

### 5.9 Geocoding — `/api/geo`

| Method | Endpoint               | Description                                        | Auth |
| ------ | ---------------------- | -------------------------------------------------- | ---- |
| GET    | `/reverse?lat=X&lng=Y` | Reverse geocode via Nominatim → return address     | Yes  |

---

## 6. Gemini 2.5 Flash — AI Integration

All AI functionality is handled through a single `gemini_service.py` that wraps the `google-generativeai` Python SDK. No local models, no TensorFlow, no spaCy. One API key, one SDK, three prompt templates.

```
┌──────────────────────────────────────────────────────┐
│                 gemini_service.py                     │
│                                                      │
│  analyze_image(image_path) → dict                    │
│    Sends image + classification prompt to Gemini     │
│    Returns: category, severity, confidence, tags     │
│                                                      │
│  analyze_text(description) → dict                    │
│    Sends text + extraction prompt to Gemini          │
│    Returns: category, keywords, suggested_priority   │
│                                                      │
│  chat(message, history, user_role, context) → str    │
│    Sends message with system prompt + history        │
│    Returns: AI response string                       │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### Image Analysis Prompt

```text
You are a civic issue analysis AI. Analyze this image of a civic/infrastructure issue.

Return ONLY valid JSON with these fields:
- "category": one of ["pothole", "garbage", "street_light", "water_leakage",
  "drainage", "road_damage", "broken_infrastructure", "other"]
- "severity": one of ["LOW", "MEDIUM", "HIGH", "CRITICAL"]
- "confidence": float between 0.0 and 1.0
- "tags": list of 3-5 descriptive keyword strings
- "description": one-line summary of what you see
```

### Text Analysis Prompt

```text
You are a civic issue text analyzer. Analyze this citizen's issue description
and extract structured information.

Description: "{user_text}"

Return ONLY valid JSON with these fields:
- "category": one of ["pothole", "garbage", "street_light", "water_leakage",
  "drainage", "road_damage", "broken_infrastructure", "other"]
- "keywords": list of relevant keywords extracted from the text
- "suggested_priority": one of ["LOW", "MEDIUM", "HIGH"]
- "urgency_indicator": brief reason for the suggested priority level
```

### Chatbot System Prompt

```text
You are CivicBot, an AI assistant for the CivicIssue app — a civic issue
reporting platform. You help citizens report and track issues, and help
admins manage and prioritize complaints.

User role: {citizen/admin}

For citizens: Help with reporting issues, explain the process, answer FAQs
about complaint status, and provide guidance.

For admins: Help with prioritization, summarize complaint trends, and
provide management recommendations.

Keep responses concise and helpful. If asked about a specific complaint,
use the provided context data.
```

---

## 7. Complaint Creation Flow (End-to-End)

```
Citizen opens "Report Issue" screen
    │
    ├── Step 1: Capture / Select Image
    │     └── POST /api/images/analyze
    │           → Image sent to Gemini 2.5 Flash
    │           → Returns: category, severity, confidence, tags
    │           → Frontend displays AI detection result + severity badge
    │
    ├── Step 2: Write Description
    │     └── POST /api/ai/analyze-text
    │           → Text sent to Gemini 2.5 Flash
    │           → Returns: category, keywords, suggested priority
    │           → Frontend cross-references with image AI result
    │
    ├── Step 3: Location Captured (device GPS → lat/lng)
    │     └── GET /api/geo/reverse?lat=X&lng=Y
    │           → Nominatim returns human-readable address
    │           → Frontend displays location name
    │
    └── Step 4: Submit Complaint
          └── POST /api/complaints (multipart: images + JSON)
                │
                ├── Save uploaded images to /uploads/
                ├── Create complaint record in MySQL
                ├── Store AI analysis results (category, severity, keywords)
                ├── Run nearby grouping check:
                │     ├── Query: same category + within 2km + last 30 days + not resolved
                │     ├── Match found → add to existing group, update count
                │     └── No match → leave ungrouped
                ├── Create notification for all admin users
                └── Return complaint with ID (#CE-XXXX)
```

---

## 8. Nearby Issue Grouping Logic

Runs automatically when a new complaint is created.

### Algorithm

```
1. Input: new complaint with (latitude, longitude, category)

2. Query existing complaints WHERE:
   - category = new complaint's category
   - status NOT IN ('COMPLETED', 'RESOLVED')
   - created_at >= (now - 30 days)
   - Distance from new complaint <= 2km

3. Distance calculation (Haversine formula):
   d = 2 * R * arcsin(√(sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlng/2)))
   where R = 6371 km

4. If matching complaints found:
   a. Check if any belong to an existing group
      - YES → Add new complaint to that group
      - NO  → Create new group with matched + new complaint
   b. Update group: center_lat, center_lng (average), complaint_count, avg_severity

5. If no matching complaints found:
   - Complaint stays ungrouped (group_id = NULL)
```

---

## 9. Notification Triggers

All notifications are created as database records. The frontend polls `/api/notifications/unread-count` periodically (e.g., every 30 seconds) and fetches full list when needed.

| Trigger Event                          | Recipients           | Notification Type   | Priority |
| -------------------------------------- | -------------------- | ------------------- | -------- |
| New complaint created                  | All admin users      | `NEW_ISSUE`         | Matches complaint |
| Status changed → ASSIGNED              | Reporting citizen    | `STATUS_UPDATE`     | MEDIUM   |
| Status changed → IN_PROGRESS           | Reporting citizen    | `STATUS_UPDATE`     | MEDIUM   |
| Status changed → COMPLETED / RESOLVED  | Reporting citizen    | `RESOLUTION`        | HIGH     |
| Officer assigned to complaint          | Assigned officer     | `ASSIGNMENT`        | HIGH     |

---

## 10. Backend Project Structure

```
backend/
├── main.py                         # FastAPI app, CORS, router includes, startup event
├── config.py                       # Settings loaded from .env via pydantic-settings
├── database.py                     # MySQL engine, SessionLocal, Base
│
├── alembic.ini                     # Alembic migration configuration
├── alembic/
│   ├── env.py
│   └── versions/                   # Auto-generated migration files
│
├── models/                         # SQLAlchemy ORM models
│   ├── __init__.py
│   ├── user.py
│   ├── complaint.py
│   ├── notification.py
│   ├── issue_group.py
│   ├── officer.py
│   ├── category.py
│   ├── department.py
│   ├── system_log.py
│   ├── otp.py
│   └── chatbot.py
│
├── schemas/                        # Pydantic request/response schemas
│   ├── __init__.py
│   ├── auth.py
│   ├── complaint.py
│   ├── notification.py
│   ├── user.py
│   └── admin.py
│
├── routers/                        # API route handlers
│   ├── __init__.py
│   ├── auth.py
│   ├── complaints.py
│   ├── images.py
│   ├── ai.py
│   ├── groups.py
│   ├── notifications.py
│   ├── users.py
│   ├── admin.py
│   └── geo.py
│
├── services/                       # Business logic layer
│   ├── __init__.py
│   ├── auth_service.py             # Signup, login, OTP, password management
│   ├── complaint_service.py        # CRUD, filtering, stats
│   ├── gemini_service.py           # ALL Gemini API calls (image, text, chatbot)
│   ├── grouping_service.py         # Nearby issue grouping algorithm
│   ├── notification_service.py     # Create & manage notifications
│   ├── geocoding_service.py        # Nominatim reverse geocoding
│   └── email_service.py            # SMTP OTP email sending
│
├── middleware/
│   └── auth.py                     # JWT verification dependency
│
├── utils/
│   ├── security.py                 # bcrypt hashing + JWT token creation/verification
│   └── helpers.py                  # UUID generation, complaint number formatting
│
├── uploads/                        # Stored image files (gitignored)
│
├── requirements.txt
├── .env                            # Environment variables
└── .env.example                    # Template for .env
```

---

## 11. Dependencies

### requirements.txt

```
fastapi
uvicorn[standard]
sqlalchemy
alembic
pymysql
cryptography
python-jose[cryptography]
passlib[bcrypt]
python-multipart
pydantic[email]
pydantic-settings
google-generativeai
httpx
python-dotenv
Pillow
```

---

## 12. Environment Variables

### .env

```env
# Database
DATABASE_URL=mysql+pymysql://root:password@localhost:3306/civicissue

# JWT
JWT_SECRET_KEY=your-random-secret-key-here
JWT_ALGORITHM=HS256
JWT_EXPIRY_MINUTES=1440

# Gemini AI
GEMINI_API_KEY=your-gemini-api-key

# SMTP (Gmail)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASSWORD=your-gmail-app-password

# File Uploads
UPLOAD_DIR=./uploads
MAX_FILE_SIZE_MB=10

# Geocoding
NOMINATIM_USER_AGENT=civicissue-app
```

---

## 13. Security

| Area               | Implementation                                              |
| ------------------ | ------------------------------------------------------------ |
| **Passwords**      | bcrypt hashing via `passlib` — never stored in plain text    |
| **Auth Tokens**    | JWT with HS256, 24-hour expiry                               |
| **Protected Routes** | All endpoints except `/api/auth/*` require valid JWT       |
| **Role-Based Access** | Citizen, Admin, Officer — enforced at route level         |
| **File Uploads**   | Validate MIME type (JPEG/PNG only), max 10MB, sanitize names |
| **Input Validation** | Pydantic models validate all request bodies                |
| **SQL Injection**  | SQLAlchemy ORM with parameterized queries                    |
| **CORS**           | Allow all origins in dev, restrict in production             |

---

## 14. Frontend Wiring Changes Required

After the backend is built, the Android app's Retrofit layer needs these updates:

### 14.1 Add JWT to RetrofitClient

```kotlin
// Add OkHttp interceptor for Authorization header
val client = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val token = TokenManager.getToken()
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        chain.proceed(request)
    }
    .build()
```

### 14.2 Update CivicApiService

```kotlin
interface CivicApiService {
    // Auth
    @POST("api/auth/signup")    suspend fun signup(@Body body: SignupRequest): AuthResponse
    @POST("api/auth/login")     suspend fun login(@Body body: LoginRequest): AuthResponse

    // Complaints
    @GET("api/complaints")      suspend fun getComplaints(@QueryMap filters: Map<String, String>): PaginatedResponse<Complaint>
    @Multipart
    @POST("api/complaints")     suspend fun createComplaint(@Part images: List<MultipartBody.Part>, @Part("data") body: RequestBody): Complaint
    @PUT("api/complaints/{id}/status")  suspend fun updateStatus(@Path("id") id: String, @Body body: StatusUpdate): Complaint

    // AI
    @Multipart
    @POST("api/images/analyze") suspend fun analyzeImage(@Part image: MultipartBody.Part): ImageAnalysisResult
    @POST("api/ai/analyze-text") suspend fun analyzeText(@Body body: TextAnalysisRequest): TextAnalysisResult
    @POST("api/ai/chatbot")     suspend fun chat(@Body body: ChatRequest): ChatResponse

    // Notifications
    @GET("api/notifications")           suspend fun getNotifications(): List<Notification>
    @GET("api/notifications/unread-count") suspend fun getUnreadCount(): UnreadCountResponse

    // Geocoding
    @GET("api/geo/reverse")     suspend fun reverseGeocode(@Query("lat") lat: Double, @Query("lng") lng: Double): GeoResponse

    // ... remaining endpoints
}
```

### 14.3 Add Multipart Image Upload Support

```kotlin
// Convert Bitmap to MultipartBody.Part
fun Bitmap.toMultipartPart(name: String): MultipartBody.Part {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 85, stream)
    val requestBody = stream.toByteArray().toRequestBody("image/jpeg".toMediaType())
    return MultipartBody.Part.createFormData(name, "image.jpg", requestBody)
}
```

---

## 15. Implementation Order

| #  | Task                                                    | Depends On |
| -- | ------------------------------------------------------- | ---------- |
| 1  | Project setup: FastAPI structure, MySQL connection, Alembic config | —  |
| 2  | All SQLAlchemy models + initial Alembic migration       | 1          |
| 3  | Auth: signup, login, JWT, bcrypt, OTP email verification | 2         |
| 4  | User profile endpoints (get, update, avatar upload)     | 3          |
| 5  | Image upload endpoint + static file serving             | 1          |
| 6  | Gemini service (image analysis, text analysis, chatbot) | 1          |
| 7  | Complaint CRUD with filtering and pagination            | 3, 5, 6   |
| 8  | Complaint status updates + status history logging       | 7          |
| 9  | Nearby issue grouping service                           | 7          |
| 10 | Notification system (auto-create on events + read/unread) | 7        |
| 11 | Officer management + complaint assignment               | 3, 7      |
| 12 | Admin endpoints: categories, departments, system logs, stats | 3     |
| 13 | Geocoding endpoint (Nominatim reverse geocode)          | 1          |
| 14 | Chatbot endpoints with session history                  | 6, 3      |
| 15 | Map data + similar issues endpoints                     | 7, 9      |
| 16 | Seed data (default categories, sample admin user)       | 2          |
| 17 | Wire up Android Retrofit client with JWT + new endpoints | All       |

---

## 16. Seed Data

On first run, the backend should create:

### Default Categories

```
Pothole, Garbage/Waste, Street Light, Water Leakage, Drainage, Road Damage, Broken Infrastructure, Other
```

### Default Departments

```
Roads & Infrastructure, Sanitation, Electricity, Water Supply, General Maintenance
```

### Default Admin User

```
Email: admin@civicissue.com
Password: admin123 (bcrypt hashed)
Role: admin
```

---

## 17. API Error Response Format

All errors follow a consistent format:

```json
{
  "detail": "Human-readable error message",
  "error_code": "VALIDATION_ERROR",
  "status_code": 422
}
```

Common error codes: `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `VALIDATION_ERROR`, `DUPLICATE_EMAIL`, `INVALID_OTP`, `EXPIRED_OTP`, `FILE_TOO_LARGE`, `INVALID_FILE_TYPE`

---
