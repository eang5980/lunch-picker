# Lunch Picker - GovTech SWE Challenge

Full-stack lunch decision application with Spring Boot backend and Angular frontend.

## Quick Start

### Prerequisites
- Java 17 or higher ([Download](https://adoptium.net/))
- Node.js 18 or higher ([Download](https://nodejs.org/))
- (Optional) Docker ([Download](https://www.docker.com/get-started))

### Run Application

**Terminal 1 - Backend:**
```bash
cd backend
mvnw.cmd spring-boot:run
```
Backend runs on http://localhost:8080

**Terminal 2 - Frontend:**
```bash
cd frontend
npm install
npm start
```
Frontend runs on http://localhost:4200

### Access the Application
- **Main Application:** http://localhost:4200
- **API Documentation (Swagger UI):** http://localhost:8080/swagger-ui.html
- **Health Check:** http://localhost:8080/actuator/health
- **H2 Database Console:** http://localhost:8080/h2-console

---

## Requirements Checklist

### Core Requirements 
- [x] **Task 1:** Web page that allows users to submit restaurant choices via API
- [x] **Task 2:** Backend API that returns a random restaurant from submitted choices
- [x] **Task 3:** Multiple users can submit restaurants
- [x] **Task 4:** No further submissions allowed after random choice is shown
- [x] **Task 5:** Multiple users can initiate different sessions with isolated choices
- [x] **Task 6:** Pre-defined users loaded from CSV via Spring Batch on startup

### Stretch Goals 
- [x] **Stretch 1:** Only the user who submitted the first restaurant can request random pick
- [x] **Stretch 2:** Program can be compiled and run on any machine through automation (Maven Wrapper + Docker)

### Expected Artifacts 
- [x] Backend developed using Java Spring Boot
- [x] Frontend developed using Angular
- [x] Code committed to GitHub repository
- [x] APIs documented (Swagger UI)
- [x] Demonstrates characteristics of a quality application (tests, error handling, validation)
- [x] Spring Batch used for Task 6 (user loading)

---

## 🏗️ Technology Stack

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.5.10
- **Modules:** Spring Batch, Spring Data JPA, Spring Validation, Spring Actuator
- **Database:** H2 (in-memory)
- **Build Tool:** Maven
- **Documentation:** OpenAPI 3 / Swagger UI
- **Containerization:** Docker

### Frontend
- **Language:** TypeScript
- **Framework:** Angular 18
- **Architecture:** Standalone Components
- **HTTP Client:** Angular HttpClient
- **Styling:** Vanilla CSS (minimal, clean design)

---

## 📁 Project Structure

```
lunch-picker/
│
├── backend/                    # Spring Boot REST API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/lunch_picker/
│   │   │   │   ├── batch/              # Spring Batch configuration
│   │   │   │   ├── config/             # CORS, OpenAPI config
│   │   │   │   ├── controller/         # REST controllers
│   │   │   │   ├── dto/                # Request/Response DTOs
│   │   │   │   ├── model/              # JPA entities
│   │   │   │   ├── repository/         # Data access layer
│   │   │   │   └── service/            # Business logic
│   │   │   └── resources/
│   │   │       ├── application.yaml    # Application config
│   │   │       └── users.csv           # Pre-defined users
│   │   └── test/                       # Integration tests
│   ├── pom.xml                         # Maven dependencies
│   ├── Dockerfile                      # Docker configuration
│   ├── docker-compose.yml              # Docker Compose setup
│   ├── mvnw, mvnw.cmd                  # Maven wrapper
│   └── README.md                       # Backend documentation
│
├── frontend/                   # Angular Single Page Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── app.component.ts        # Main component logic
│   │   │   ├── app.component.html      # UI template
│   │   │   └── api.service.ts          # Backend API service
│   │   ├── styles.css                  # Global styles
│   │   ├── index.html                  # Main HTML
│   │   └── main.ts                     # Bootstrap file
│   ├── package.json                    # NPM dependencies
│   ├── angular.json                    # Angular configuration
│   └── README.md                       # Frontend documentation
│
├── .gitignore                  # Git ignore rules
└── README.md                   # This file
```

---

## How It Works

### User Flow

1. **Load Pre-defined Users**
   - On startup, Spring Batch reads `users.csv`
   - Loads users: alice, bob, charlie, david, eve
   - Only these users can **create** sessions

2. **Create Session**
   - User selects their name from dropdown
   - Clicks "Create Session"
   - Backend generates unique UUID for session
   - Session status set to "OPEN"

3. **Submit Restaurants**
   - Any user (including guests) can submit to an existing session
   - Enter name and restaurant
   - Backend validates: session is open, no duplicates
   - Restaurant saved with submitter name

4. **Pick Random Winner**
   - Only the **first submitter** can trigger random pick
   - Backend validates authorization
   - Randomly selects one restaurant
   - Session status changes to "CLOSED"
   - No more submissions allowed

5. **View Results**
   - Winner displayed prominently
   - All submitted restaurants shown
   - Session remains accessible for viewing

---

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users` | List all pre-defined users | No |
| POST | `/api/sessions?user={username}` | Create new session | Pre-defined user only |
| GET | `/api/sessions/{id}` | Get session details | No |
| POST | `/api/sessions/{id}/restaurants` | Submit restaurant choice | No |
| POST | `/api/sessions/{id}/pick?user={username}` | Pick random restaurant | First submitter only |

**Complete API Documentation:** http://localhost:8080/swagger-ui.html

---

## Testing

### Run Backend Tests
```bash
cd backend
mvnw.cmd test
```

**Test Coverage:**
- ✅ User loading via Spring Batch
- ✅ Session creation and retrieval
- ✅ Restaurant submission with validation
- ✅ Duplicate prevention (case-insensitive)
- ✅ Random pick authorization
- ✅ Session closure after pick
- ✅ Session isolation
- ✅ Error handling

**Expected Result:** 20+ tests, all passing

### Manual End-to-End Test

1. Start backend: `cd backend && mvnw.cmd spring-boot:run`
2. Start frontend: `cd frontend && npm install && npm start`
3. Open http://localhost:4200
4. Create session as "alice"
5. Submit restaurant "Pizza Hut" as "alice"
6. Submit restaurant "KFC" as "bob"
7. Pick random as "alice" (must be first submitter)
8. Verify winner is displayed
9. Verify no more submissions allowed

---

## 🐳 Docker Deployment

### Run Backend with Docker Compose

```bash
cd backend
docker-compose up --build
```

Backend available at http://localhost:8080

### Manual Docker Build

```bash
cd backend
docker build -t lunch-picker-backend .
docker run -p 8080:8080 lunch-picker-backend
```

**Note:** Frontend must be run separately with `npm start`

---

## ⚙️ Configuration

### Backend Configuration

**File:** `backend/src/main/resources/application.yaml`

Key settings:
- Server port: 8080
- Database: H2 in-memory
- CORS allowed origin: http://localhost:4200
- H2 console enabled at `/h2-console`
- Swagger UI at `/swagger-ui.html`

### Frontend Configuration

**File:** `frontend/src/app/api.service.ts`

```typescript
private baseUrl = 'http://localhost:8080/api';
```

Change this if backend runs on different port.

---

## 🔍 Design Decisions

### Backend Architecture

- **Layered Architecture:** Controllers → Services → Repositories
- **DTOs:** Separate API contracts from database entities
- **Spring Batch:** Proper Reader → Writer pipeline for CSV loading
- **Optimistic Locking:** `@Version` field prevents race conditions in distributed systems
- **Global Exception Handler:** Consistent error responses across all endpoints
- **Validation:** Bean Validation (`@NotBlank`) + business logic validation

### Frontend Design

- **Standalone Components:** Modern Angular 18 approach (no NgModule)
- **Minimal UI:** Simple, clean design without external CSS frameworks
- **Reactive Programming:** Observables for async HTTP calls
- **Two-way Binding:** `[(ngModel)]` for form synchronization
- **Error Handling:** User-friendly error messages from backend

### Session Management

- **UUID-based IDs:** Globally unique session identifiers
- **Status Enum:** OPEN/CLOSED states
- **Eager Loading:** Restaurant choices loaded with session
- **Isolation:** JPA relationships ensure proper scoping

---

## 🚨 Important Notes

### Pre-defined Users

Only these users can **create** sessions:
- alice
- bob
- charlie
- david
- eve

Anyone (including guests) can **submit** restaurants to existing sessions.

### First Submitter Rule (Stretch Goal 1)

The user who submitted the **first** restaurant (not the session creator) is the only one who can trigger the random pick. This is validated server-side.

### Session Isolation (Task 5)

Each session has a unique ID. Restaurants submitted to one session are not visible in another session. Multiple sessions can run simultaneously.

### Data Persistence

Uses H2 in-memory database. Data is lost on application restart. For production, configure PostgreSQL/MySQL in `application.yaml`.

## 🐳 Docker Deployment (Stretch Goal 2)

### Quick Start with Docker

**Prerequisites:** Docker Desktop installed

**Run Backend:**
```bash
cd backend
docker-compose up --build
```

Backend available at http://localhost:8080

**Run Frontend:** (in separate terminal)
```bash
cd frontend
npm install
npm start
```

Frontend available at http://localhost:4200

### What Docker Does Automatically

✅ Downloads correct Java version (17)
✅ Downloads correct Maven version (3.9)
✅ Resolves all dependencies from pom.xml
✅ Compiles Spring Boot application
✅ Creates production-ready container
✅ Starts the application