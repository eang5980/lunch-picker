# Lunch Picker

A web app to help teams decide where to go for lunch. Built with Spring Boot and Angular for the GovTech SWE Challenge.

## Quick Start

**You need:**
- Java 17+
- Node.js 18+

**Running the app:**

```bash
# Start backend (in one terminal)
cd backend
mvnw.cmd spring-boot:run

# Start frontend (in another terminal)
cd frontend
npm install
npm start
```

Then open http://localhost:4200 in your browser.

## What This Does

- Users can create lunch sessions and invite others
- Everyone submits their restaurant suggestions
- The app randomly picks one restaurant from all the suggestions
- Once picked, the session closes (no more submissions)

The catch: only the person who submitted the FIRST restaurant can trigger the random pick. This prevents someone from waiting to see all options before picking.

## Requirements

I've implemented all the required features:

**Core stuff:**
- Web interface for submitting restaurants ✓
- API that returns a random restaurant ✓
- Multiple users can submit to the same session ✓
- Sessions lock after picking (no more submissions) ✓
- Sessions are isolated from each other ✓
- Pre-defined users loaded from CSV using Spring Batch ✓

**Extra features:**
- First submitter rule (stretch goal) ✓
- Docker support for easy deployment ✓

## Tech Stack

**Backend:**
- Java 17
- Spring Boot 3.5.10
- Spring Batch for loading users
- H2 database (in-memory)
- Maven

**Frontend:**
- Angular 18
- TypeScript
- Basic CSS (kept it simple)

## Project Structure

```
lunch-picker/
├── backend/          # Spring Boot API
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/         # Angular UI
│   ├── src/
│   └── package.json
└── README.md
```

## API Endpoints

The backend exposes these endpoints:

- `GET /api/users` - List all users
- `POST /api/sessions?user={username}` - Create a session
- `GET /api/sessions/{id}` - Get session details
- `POST /api/sessions/{id}/restaurants` - Submit a restaurant
- `POST /api/sessions/{id}/pick?user={username}` - Pick the winner

Full API documentation available at http://localhost:8080/swagger-ui.html when the backend is running.

## How It Works

1. A pre-defined user creates a session
2. People start submitting restaurant names
3. The person who submitted first clicks "Pick Random"
4. App randomly selects a winner
5. Everyone goes to that restaurant!

Pre-defined users (can create sessions): alice, bob, charlie, david, eve

Anyone else can submit restaurants to existing sessions, but only these users can start new ones.

## Testing

Run the backend tests:
```bash
cd backend
mvnw.cmd test
```

There are 20+ tests covering the main functionality.

## Docker

If you have Docker installed:

```bash
cd backend
docker-compose up --build
```

This handles all the dependencies and gets the backend running on port 8080.


## Configuration

Backend runs on port 8080 by default. Frontend expects this.

If you need to change the backend port:
- Update `backend/src/main/resources/application.yaml`
- Update `frontend/src/app/api.service.ts` (baseUrl)

## Running Everything

Full workflow to test:

1. Start backend: `cd backend && mvnw.cmd spring-boot:run`
2. Start frontend: `cd frontend && npm install && npm start`
3. Open http://localhost:4200
4. Select "alice" and create a session
5. Submit a restaurant as alice
6. Submit another restaurant as a different user
7. Click "Pick Random" as alice
8. See the winner

Or just use the Swagger UI at http://localhost:8080/swagger-ui.html to test the APIs directly.
