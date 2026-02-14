# Lunch Picker - Angular Frontend

Simple, bare-bones Angular frontend for the Lunch Picker application.

## Prerequisites

- Node.js 24.13.1 (or compatible)
- Angular CLI 21.1.4
- Backend running on http://localhost:8080

## Quick Start

### 1. Install Dependencies

```bash
npm install
```

### 2. Start Backend First

Make sure your Spring Boot backend is running:

```bash
# In the backend directory
./mvnw spring-boot:run
```

The backend should be accessible at http://localhost:8080

### 3. Start Frontend

```bash
npm start
```

The frontend will be available at http://localhost:4200

## Usage

### Create a Session

1. Select a pre-defined user (alice, bob, charlie, david, or eve)
2. Click "Create Session"
3. Note the Session ID that appears

### Join an Existing Session

1. Enter the Session ID in the input field
2. Click "Join Session"

### Submit a Restaurant

1. Enter your name
2. Enter a restaurant name
3. Click "Submit Restaurant"

### Pick a Winner

1. Enter your name (must be the person who submitted the FIRST restaurant)
2. Click "Pick Random Restaurant"
3. The winner will be displayed at the top

## Features

✅ Create new lunch session (pre-defined users only)  
✅ Join existing session by ID  
✅ Submit restaurant choices  
✅ View all submitted restaurants  
✅ Pick random winner (first submitter only)  
✅ Session status tracking (OPEN/CLOSED)  
✅ Real-time error messages  
✅ Refresh session data  

## Project Structure

```
src/
├── app/
│   ├── app.component.ts       # Main component logic
│   ├── app.component.html     # Main template
│   ├── app.component.css      # Component styles
│   └── api.service.ts         # Backend API service
├── index.html                 # Main HTML file
├── main.ts                    # Angular bootstrap
└── styles.css                 # Global styles (basic, simple)
```

## API Endpoints Used

- `GET /api/users` - Get all users
- `POST /api/sessions?user={username}` - Create session
- `GET /api/sessions/{id}` - Get session details
- `POST /api/sessions/{id}/restaurants` - Submit restaurant
- `POST /api/sessions/{id}/pick?user={username}` - Pick winner

## Styling

The UI uses **minimal, basic CSS** with:
- Simple layout and spacing
- Basic color scheme (green for success, red for errors)
- No external CSS frameworks
- Clean, professional look without complexity

## Development

### Build for Production

```bash
npm run build
```

Output will be in `dist/lunch-picker-frontend/`

### Serve Production Build

After building, you can serve the static files with any web server, or use:

```bash
npx http-server dist/lunch-picker-frontend -p 4200
```

## Troubleshooting

### CORS Errors

If you see CORS errors, ensure your backend has CORS configured:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:4200")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

### Backend Not Running

Make sure the Spring Boot backend is running on port 8080 before starting the frontend.

### Port Already in Use

If port 4200 is already in use, you can change it:

```bash
ng serve --port 4300
```

## Notes

- This is a **basic, functional UI** designed for demonstration
- No advanced Angular features (routing, guards, interceptors, etc.)
- Single component application for simplicity
- Uses standalone components (Angular 18+ feature)
- Minimal dependencies for easier understanding

## Integration with Backend

The frontend expects the backend at `http://localhost:8080/api`. If your backend runs on a different port or URL, update the `baseUrl` in `src/app/api.service.ts`:

```typescript
private baseUrl = 'http://localhost:8080/api';  // Change this if needed
```
