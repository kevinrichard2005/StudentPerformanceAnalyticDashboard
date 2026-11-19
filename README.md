# 🎓 StudentPerformanceAnalyticDashboard — Student Management System

A production-quality, full-stack Student Management System built with **Java 17+ / Spring Boot 3**, **MySQL 8**, and a premium, fully responsive **glassmorphism** frontend (HTML5, CSS3, ES6+, Chart.js, Lucide icons).

Administrators can register, view, search, filter, update and delete student records. The backend calculates grades automatically and is the single source of truth for them.

---

## ✨ Features

| Module | Highlights |
|---|---|
| **Login & Registration** | Glassmorphism Login & Register pages supporting **Student**, **Teacher**, and **Admin** roles, role tabs, 1-click quick demo login buttons, salted PBKDF2 password hashing, and HMAC-SHA256 bearer tokens |
| **Role-Based Access (RBAC)** | **Admin**: full CRUD + user management + student deletion. **Teacher**: add students, update marks/grades, view analytics (cannot delete). **Student**: personal academic profile banner, transcript view, read-only directory |
| **Dashboard** | Role-aware portal banner (Student academic card / Teacher faculty bar), 6 live stat cards, gender doughnut, grade distribution bar chart, enrollment trend line chart, recent students, recent activities, quick actions |
| **Add Student** | Grouped form (personal / academic), client + server validation, duplicate-email prevention, live grade preview, loading states, toasts |
| **All Students** | Global search (ID, name, email, course, department), department & grade filters that combine with search, column sorting, pagination, detail modal, edit, delete. Uses a **card layout on mobile** instead of a wide table |
| **Edit Student** | Pre-populated form, read-only Student ID, re-validation, automatic grade recalculation |
| **Delete Student** | Confirmation dialog that names the student, list refreshes afterwards, success/failure toasts |
| **Grade Calculation** | Backend `GradeCalculator` utility, pass/fail status, grade badges, class average, pass percentage |
| **Analytics** | Summary cards, 4 charts (gender, grade, department, pass/fail), grade breakdown table |

### Grading scale

| Marks | Grade | Result |
|---|---|---|
| 90–100 | O | Excellent |
| 80–89 | A+ | Outstanding |
| 70–79 | A | Very Good |
| 60–69 | B+ | Good |
| 50–59 | B | Average |
| 40–49 | C | Pass |
| 0–39 | F | Fail |

---

## 🧱 Technology Stack

- **Backend:** Java 17+ (tested on JDK 25), Spring Boot 3.4, Spring Web, Spring Data JPA, Hibernate 6, Bean Validation
- **Database:** MySQL 8+ (H2 in-memory, MySQL mode, for automated tests only)
- **Frontend:** HTML5, CSS3 (custom design system built on CSS variables), vanilla JavaScript (ES6+), Chart.js, Lucide icons, Inter font
- **Build:** Maven
- **Testing:** JUnit 5, Mockito, Spring Boot Test, MockMvc, AssertJ

---

## 🏗 Architecture

```
Browser (static HTML/CSS/JS)  ──REST/JSON──▶  Controller ─▶ Service ─▶ Repository ─▶ MySQL
                                                │             │
                                                │             └─ GradeCalculator (util)
                                                └─ GlobalExceptionHandler ─▶ consistent ApiResponse JSON
```

```
src/main/java/com/studentmanagement
├── StudentManagementApplication.java
├── config/       WebConfig            – CORS, root forwarding
├── controller/   StudentController    – REST endpoints only, no business logic
├── dto/          StudentDTO, StudentStatsDTO, ApiResponse<T>
├── entity/       Student              – JPA entity, constraints, indexes, timestamps
├── exception/    GlobalExceptionHandler, ResourceNotFound, DuplicateResource, InvalidRequest
├── repository/   StudentRepository    – derived + JPQL queries, aggregates
├── service/      StudentService       – CRUD, grading, search, stats, transactions
└── util/         GradeCalculator      – the grading scale

src/main/resources
├── application.properties             – env-var driven configuration
├── application.properties.example     – sample configuration template
└── static/
    ├── index.html                     – Dashboard
    ├── css/styles.css                 – Glassmorphism design system + responsive breakpoints
    ├── js/app.js                      – API client, Toast, Modal, Sidebar, FormValidator, GradeUtil, Utils
    └── pages/  login.html, register.html, students.html, add-student.html, edit-student.html, student-details.html, analytics.html, settings.html

database/setup.sql                     – schema + sample data
src/test/...                           – unit + integration tests
```

Here are the main design decisions:
- The **controllers** stay thin. **Services** are `@Transactional`, and reads use `readOnly = true`.
- The **DTOs** decouple the API from the entity. Clients can't set `studentCode`, `grade` or `result`.
- **Sort fields are whitelisted**, and page sizes are capped at 100. This keeps arbitrary property access out of the API.
- Emails are trimmed and lower-cased, so duplicate detection ignores case. The database also enforces uniqueness.
- The **frontend** is served by Spring Boot itself, so there's no separate server and no CORS problems in normal use.

---

## 🗄 Database Setup

1. Make sure MySQL 8 is running.
2. Create the schema (optional, because the app can create the DB and table itself):

```bash
mysql -u root -p < database/setup.sql
```

`setup.sql` creates `student_management_db`, the `students` table (primary key, unique `student_code` and `email`, indexes on department/grade/gender/created_at, automatic timestamps), and 10 sample students.

> The application connects with `createDatabaseIfNotExist=true` and `ddl-auto=update`, so it can also create the schema on first start. For production, run `setup.sql` and set `DDL_AUTO=validate`.

---

## ⚙️ Backend Setup

Configuration comes from environment variables. **Never commit real credentials.**

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/student_management_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` | JDBC URL |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | *(empty)* | DB password |
| `SERVER_PORT` | `8080` | HTTP port |
| `DDL_AUTO` | `update` | Hibernate schema mode |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:8080` | Comma-separated origins allowed to call the API |

**Windows (PowerShell)** — uses the included Maven Wrapper (no global Maven required)
```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_password"
.\mvnw.cmd spring-boot:run
```

**macOS / Linux**
```bash
export DB_USERNAME=root DB_PASSWORD=your_password
./mvnw spring-boot:run
```

Or build a runnable jar:
```bash
./mvnw clean package   # or .\mvnw.cmd clean package on Windows
java -jar target/student-management-system-1.0.0.jar
```

## 🖥 Frontend Setup

You don't need a separate setup step. Spring Boot serves the static frontend from `src/main/resources/static`. Open **http://localhost:8080** once the backend is running.

The frontend loads Chart.js, Lucide and the Inter font from CDNs, so the browser needs internet access to render icons, charts and the font.

---

## 📡 API Documentation

Base URL: `/api/students`. Every response uses the same envelope:

```json
{ "success": true, "message": "Operation successful", "data": { }, "timestamp": "2026-11-18T22:00:00" }
```

| Method | Endpoint | Description | Success | Errors |
|---|---|---|---|---|
| GET | `/api/students?page=0&size=10&sortBy=createdAt&sortDir=desc` | Paginated, sorted list | 200 | 400 (bad sort/page) |
| GET | `/api/students/{id}` | Single student | 200 | 404 |
| POST | `/api/students` | Create student (code + grade generated) | 201 | 400 validation, 409 duplicate email |
| PUT | `/api/students/{id}` | Update (student code unchanged, grade recalculated) | 200 | 400, 404, 409 |
| DELETE | `/api/students/{id}` | Delete student | 200 | 404 |
| GET | `/api/students/search?keyword=value&page=0&size=10` | Keyword search on ID, name, email, department, course | 200 | 400 |
| GET | `/api/students/filter?keyword=&department=&grade=&page=&size=&sortBy=&sortDir=` | Combined search + filters | 200 | 400 |
| GET | `/api/students/stats` | Dashboard statistics and distributions | 200 | |
| GET | `/api/students/recent` | Five most recently added students | 200 | |
| POST | `/api/auth/register` | Register as `STUDENT`, `TEACHER`, or `ADMIN` (auto-creates student profile for `STUDENT`) | 201 | 400, 409 |
| POST | `/api/auth/login` | Sign in with email, password, and optional role | 200 | 401, 403 |
| GET | `/api/auth/me` | Current user profile + linked student record ID | 200 | 401 |
| GET | `/api/auth/my-student-profile` | Academic record linked to logged-in student | 200 | 401, 404 |
| GET | `/api/auth/users` | List all registered portal users (`ADMIN` only) | 200 | 401, 403 |

Sortable fields: `id, studentCode, fullName, email, department, course, semester, marks, grade, createdAt, updatedAt`.

**Create request example**
```json
{
  "fullName": "Asha Rao",
  "email": "asha@uni.edu",
  "phone": "+919876543210",
  "gender": "Female",
  "dateOfBirth": "2003-05-14",
  "department": "Computer Science",
  "course": "B.Tech CS",
  "yearOfStudy": 2,
  "semester": "3rd",
  "address": "12 Campus Road",
  "marks": 91
}
```

**Validation error response (400)**
```json
{ "success": false, "message": "Validation failed",
  "data": { "email": "Invalid email format", "marks": "Marks cannot exceed 100" } }
```

**Stats response `data`**
```json
{ "totalStudents": 10, "maleStudents": 5, "femaleStudents": 5, "averageMarks": 68.0,
  "passedStudents": 9, "failedStudents": 1, "passPercentage": 90.0,
  "gradeDistribution": { "O": 1, "A+": 2, "A": 2, "B+": 2, "B": 1, "C": 1, "F": 1 },
  "departmentDistribution": { "Computer Science": 3, "Electronics": 2 },
  "genderDistribution": { "Female": 5, "Male": 5 },
  "enrollmentTrend": { "Nov 2026": 10 } }
```

---

## ▶️ How to Run

1. Start MySQL 8+ and set `DB_USERNAME` / `DB_PASSWORD`.
2. Optional: create the database and load 10 sample students:
   ```powershell
   Get-Content database\setup.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pGet-Content database\setup.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pGet-Content database\setup.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
   ```
   *(Or on macOS/Linux: `mysql -u root -p < database/setup.sql`)*
3. Run the application:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. Open **http://localhost:8080**

## 🧪 Testing

```powershell
.\mvnw.cmd test   # or ./mvnw test on macOS/Linux
```

The tests run against an isolated H2 in-memory database in MySQL mode (`src/test/resources/application.properties`), so they never touch your real data.

| Test class | Type | Covers |
|---|---|---|
| `GradeCalculatorTest` | Unit | Every grade boundary, out-of-range marks, pass threshold |
| `StudentServiceTest` | Unit (Mockito) | Code generation, email normalization, duplicate email on create/update, student code preserved on update, grade recalculation, not-found, sort whitelist, page size limits |
| `StudentControllerIntegrationTest` | Integration (MockMvc + JPA) | Full CRUD lifecycle, duplicate email → 409, validation errors → 400 with field messages, negative/over-100 marks, malformed JSON, search by name and ID, department/grade/keyword filters, sorting, pagination, stats accuracy, 404s, static pages served |

## ♿ Accessibility & Performance

- Semantic landmarks (`aside`, `header`, `main`, `nav`), labelled controls, `aria-modal` dialogs, and Escape closes modals and the drawer.
- Touch targets are at least 44px, and the layout is mobile-first with breakpoints at 375 / 480 / 767 / 1023 / 1440px. The sidebar becomes an off-canvas drawer on tablet and mobile.
- `prefers-reduced-motion` is respected.
- Search input is debounced. A single combined filter endpoint avoids redundant API calls.
- Queries are indexed. Statistics use aggregate queries instead of loading every row.
- Gzip compression and static asset caching are enabled.

## 📸 Screenshots

| Page | Screenshot |
|---|---|
| Dashboard | `docs/screenshots/dashboard.png` |
| All Students (desktop) | `docs/screenshots/students.png` |
| All Students (mobile cards) | `docs/screenshots/students-mobile.png` |
| Add Student | `docs/screenshots/add-student.png` |
| Analytics | `docs/screenshots/analytics.png` |

*Add your own screenshots to `docs/screenshots/` after running the app.*

---

## 🔒 Security Notes

This project has no authentication layer, because the scope is a single-admin tool. Before you expose it publicly, add Spring Security (for example form login or OAuth2) and restrict `CORS_ALLOWED_ORIGINS`.
