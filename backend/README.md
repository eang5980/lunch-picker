# Lunch Picker - GovTech SWE Challenge

A Spring Boot application that helps teams collectively decide on a restaurant for lunch. Users can create sessions, submit restaurant choices, and randomly pick a winner.

## 🎯 Challenge Requirements Status

### Core Requirements ✅

| # | Requirement | Status | Implementation |
|---|-------------|--------|----------------|
| 1 | Web page with restaurant submission API | ✅ | `POST /api/sessions/{id}/restaurants` |
| 2 | Random restaurant selection API | ✅ | `POST /api/sessions/{id}/pick` |
| 3 | Multiple users can submit | ✅ | Any user (including guests) can submit |
| 4 | No submissions after random pick | ✅ | Session status changes to `CLOSED` |
| 5 | Session isolation | ✅ | Each session has unique UUID |
| 6 | Load users from CSV via Spring Batch | ✅ | `UserBatchConfig` on startup |

### Stretch Goals ✅

| # | Goal | Status | Notes |
|---|------|--------|-------|
| 1 | Only first submitter can pick | ✅ | Enforced in `RestaurantService.pickRandom()` |
| 2 | Docker automation | ✅ | Dockerfile + docker-compose.yml |

---

## 🛠 Tech Stack

- **Java:** 17
- **Framework:** Spring Boot 3.2.2
- **Spring Modules:** Spring Batch, Spring Data JPA, Spring Validation
- **Database:** H2 (in-memory for demo; easily swappable to PostgreSQL/MySQL)
- **Build Tool:** Maven 3.8+
- **Documentation:** OpenAPI 3 / Swagger UI
- **Monitoring:** Spring Boot Actuator
- **Containerization:** Docker

---

## 📋 Prerequisites

- Java 17+ (OpenJDK or Oracle JDK)
- Maven 3.8+
- (Optional) Docker & Docker Compose

---

## 🚀 Quick Start

### Option 1: Run with Maven

```bash
# Clone the repository
git clone <repository-url>
cd lunch-picker

# Run the application
./mvnw spring-boot:run
```

The application starts at **http://localhost:8080**

### Option 2: Run with Docker

```bash
# Build and run
docker-compose up --build

# Or build manually
docker build -t lunch-picker .
docker run -p 8080:8080 lunch-picker
```

### Option 3: Build JAR and Run

```bash
./mvnw clean package
java -jar target/lunch-picker-0.0.1-SNAPSHOT.jar
```

---

## 📖 API Documentation

### Interactive Documentation

After starting the application, visit:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

### API Endpoints

#### Users

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users` | List all pre-defined users | No |

**Example:**
```bash
curl http://localhost:8080/api/users
```

#### Sessions

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/sessions?user={username}` | Create new session | Pre-defined user only |
| GET | `/api/sessions/{id}` | Get session details | No |
| POST | `/api/sessions/{id}/restaurants` | Submit restaurant | No |
| POST | `/api/sessions/{id}/pick?user={username}` | Pick random restaurant | First submitter only |

### Request/Response Examples

#### 1. Create Session

```bash
curl -X POST "http://localhost:8080/api/sessions?user=alice"
```

**Response (201 Created):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "createdBy": "alice",
  "status": "OPEN",
  "chosenRestaurant": null,
  "createdAt": "2025-02-14T10:30:00",
  "restaurants": []
}
```

#### 2. Submit Restaurant

```bash
curl -X POST "http://localhost:8080/api/sessions/{SESSION_ID}/restaurants" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurant": "McDonald'\''s",
    "user": "bob"
  }'
```

**Response (201 Created):**
```json
{
  "id": 1,
  "restaurant": "McDonald's",
  "submittedBy": "bob"
}
```

#### 3. Get Session Details

```bash
curl http://localhost:8080/api/sessions/{SESSION_ID}
```

**Response (200 OK):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "createdBy": "alice",
  "status": "OPEN",
  "chosenRestaurant": null,
  "createdAt": "2025-02-14T10:30:00",
  "restaurants": [
    {
      "id": 1,
      "restaurant": "McDonald's",
      "submittedBy": "bob"
    },
    {
      "id": 2,
      "restaurant": "KFC",
      "submittedBy": "charlie"
    }
  ]
}
```

#### 4. Pick Random Restaurant

```bash
curl -X POST "http://localhost:8080/api/sessions/{SESSION_ID}/pick?user=bob"
```

**Response (200 OK):**
```json
{
  "chosenRestaurant": "McDonald's"
}
```

### Error Responses

All errors follow a consistent format:

```json
{
  "timestamp": "2025-02-14T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Session not found: xyz-123"
}
```

| Status Code | Meaning | Example Scenarios |
|-------------|---------|-------------------|
| 400 | Bad Request | Blank restaurant name, validation failure |
| 403 | Forbidden | Non-pre-defined user creating session, non-first-submitter picking |
| 404 | Not Found | Session doesn't exist |
| 409 | Conflict | Session closed, duplicate restaurant |
| 500 | Internal Server Error | Unexpected system error |

---

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Test Coverage

The application includes comprehensive integration tests covering:
- ✅ User loading via Spring Batch
- ✅ Session creation and retrieval
- ✅ Restaurant submission with validation
- ✅ Duplicate detection (case-insensitive)
- ✅ Random pick authorization
- ✅ Session closure after pick
- ✅ Session isolation
- ✅ Error handling

### Manual Testing Workflow

```bash
# 1. List available users
curl http://localhost:8080/api/users

# 2. Create a session (as alice)
SESSION_ID=$(curl -s -X POST "http://localhost:8080/api/sessions?user=alice" | jq -r .id)
echo "Session ID: $SESSION_ID"

# 3. Submit restaurants
curl -X POST "http://localhost:8080/api/sessions/$SESSION_ID/restaurants" \
  -H "Content-Type: application/json" \
  -d '{"restaurant": "Pizza Hut", "user": "alice"}'

curl -X POST "http://localhost:8080/api/sessions/$SESSION_ID/restaurants" \
  -H "Content-Type: application/json" \
  -d '{"restaurant": "KFC", "user": "bob"}'

# 4. View all submissions
curl "http://localhost:8080/api/sessions/$SESSION_ID"

# 5. Pick random (must be alice, the first submitter)
curl -X POST "http://localhost:8080/api/sessions/$SESSION_ID/pick?user=alice"

# 6. Verify session is closed
curl "http://localhost:8080/api/sessions/$SESSION_ID"
```

---

## 📁 Project Structure

```
src/main/java/com/example/lunch_picker/
├── LunchPickerApplication.java       # Spring Boot entry point
├── batch/
│   └── UserBatchConfig.java          # Spring Batch: Load users from CSV
├── config/
│   ├── CorsConfig.java               # CORS for Angular frontend
│   └── OpenApiConfig.java            # OpenAPI/Swagger configuration
├── controller/
│   ├── GlobalExceptionHandler.java   # Centralized error handling
│   ├── SessionController.java        # Session & restaurant APIs
│   └── UserController.java           # User listing API
├── dto/
│   ├── RestaurantChoiceResponse.java # Response DTO
│   ├── SessionResponse.java          # Response DTO (prevents circular JSON)
│   └── SubmitRestaurantRequest.java  # Request DTO with validation
├── model/
│   ├── LunchSession.java             # Session entity with optimistic locking
│   ├── RestaurantChoice.java         # Restaurant choice entity
│   ├── SessionStatus.java            # OPEN/CLOSED enum
│   └── User.java                     # User entity
├── repository/
│   ├── RestaurantRepository.java     # JPA repository
│   ├── SessionRepository.java        # JPA repository
│   └── UserRepository.java           # JPA repository
└── service/
    ├── RestaurantService.java        # Business logic: submit & pick
    └── SessionService.java           # Business logic: session management

src/main/resources/
├── application.yaml                  # Application configuration
└── users.csv                         # Pre-defined users for Spring Batch
```

---

## 🔧 Configuration

### Application Properties

Key configurations in `application.yaml`:

```yaml
# Database
spring.datasource.url: jdbc:h2:mem:lunchdb

# H2 Console (development only)
spring.h2.console.enabled: true

# API Documentation
springdoc.swagger-ui.path: /swagger-ui.html

# Actuator Health Checks
management.endpoints.web.exposure.include: health,info,metrics

# Logging
logging.level.com.example.lunch_picker: DEBUG
```

### Pre-defined Users

Users are loaded from `src/main/resources/users.csv`:

```
username
alice
bob
charlie
david
eve
```

To add more users, simply append to this file and restart the application.

---

## 💡 Design Decisions

### 1. **DTOs for API Layer**
- Separates API contracts from JPA entities
- Prevents circular JSON serialization
- Allows independent evolution of API and data models

### 2. **Spring Batch for CSV Loading**
- Meets requirement #6 explicitly
- Proper Reader → Writer pipeline
- Transaction management built-in
- Easily extensible to other data sources

### 3. **Optimistic Locking (@Version)**
- Prevents race conditions in distributed systems
- Replaced `synchronized` method (which only works in single instance)
- Throws `OptimisticLockingFailureException` on conflicts

### 4. **Global Exception Handler**
- Consistent error response format across all endpoints
- Centralized error mapping (IllegalArgumentException → 404, etc.)
- Reduces code duplication

### 5. **Session Isolation**
- UUID-based session IDs
- JPA relationships ensure proper scoping
- Integration tests verify isolation

### 6. **Validation Layer**
- `@Valid` with Jakarta Bean Validation
- `@NotBlank` for required fields
- Business logic validation in service layer

---

## 🔒 Security Considerations

### Current Implementation (Demo)
- **Authentication:** Username passed as request parameter (simplified for demo)
- **Authorization:** Pre-defined user check for session creation
- **Session Ownership:** First submitter can pick (Stretch Goal 1)

### Production Recommendations
1. **Add Spring Security** with JWT tokens
2. **HTTPS/TLS** for all endpoints
3. **Rate limiting** to prevent abuse
4. **CSRF protection** if using cookie-based auth
5. **Input sanitization** for XSS prevention

---

## 🎯 Assumptions

1. **In-memory database:** Data is lost on restart. This is intentional for the demo. For production, configure PostgreSQL/MySQL in `application.yaml`.

2. **No authentication:** Users identified by username in requests. Production would use Spring Security + JWT.

3. **Guest submissions allowed:** The requirement states "other users may submit" without specifying authentication. We interpret this to mean any user (including non-pre-defined users) can submit restaurants, but only pre-defined users can create sessions.

4. **First submitter picks:** Implemented as Stretch Goal 1. The user who submitted the *first* restaurant (not session creator) can trigger the random pick.

5. **Idempotent pick:** Calling `/pick` multiple times on a closed session returns the same result (doesn't re-randomize).

6. **Case-insensitive duplicates:** "McDonald's" and "mcdonald's" are treated as the same restaurant.

---

## 🐛 Troubleshooting

### Issue: Application won't start

**Solution:** Ensure Java 17+ is installed:
```bash
java -version
```

### Issue: Port 8080 already in use

**Solution:** Change port in `application.yaml`:
```yaml
server:
  port: 8081
```

### Issue: Database errors on startup

**Solution:** Delete existing H2 database files (if running with file-based H2):
```bash
rm -rf *.db
```

### Issue: Maven build fails

**Solution:** Clear local Maven cache:
```bash
./mvnw dependency:purge-local-repository
```

---

## 📊 Health Checks & Monitoring

### Actuator Endpoints

- **Health Check:** http://localhost:8080/actuator/health
- **Application Info:** http://localhost:8080/actuator/info
- **Metrics:** http://localhost:8080/actuator/metrics

### H2 Console (Development)

Access the H2 database console at:
- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:lunchdb`
- **Username:** `sa`
- **Password:** (leave blank)

---

## 🚢 Production Deployment

### Database Migration

Replace H2 with PostgreSQL:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lunchdb
    username: dbuser
    password: dbpass
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate  # Use Flyway/Liquibase for migrations
```

Add dependency:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

### Environment Variables

Use environment-specific configurations:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://...
export DATABASE_USERNAME=...
export DATABASE_PASSWORD=...
```

---

## 📜 License

This project is created for the GovTech SWE Challenge and is provided as-is for evaluation purposes.

---

## 👤 Author

**GovTech SWE Challenge Candidate**

Submission Date: February 2025

---

## 📝 Notes for Reviewers

### Key Highlights

1. ✅ **All core requirements met** (Tasks 1-6)
2. ✅ **Both stretch goals implemented**
3. ✅ **Comprehensive testing** with 20+ test cases
4. ✅ **Production-ready features:** OpenAPI docs, health checks, error handling
5. ✅ **Clean architecture:** DTOs, service layer, proper separation of concerns
6. ✅ **Docker support** for easy deployment

### Code Quality

- Uses **Lombok** to reduce boilerplate
- **Global exception handler** for consistent error responses
- **Optimistic locking** for distributed system safety
- **Validation** at multiple layers
- **Extensive logging** for debugging
- **Idempotent operations** where appropriate

### Testing Strategy

- Integration tests cover all user journeys
- Edge cases handled (empty sessions, duplicates, authorization)
- Session isolation verified
- Spring Batch user loading tested

---

**Thank you for reviewing this submission!**
