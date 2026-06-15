# URL Shortener

A full-stack application to generate short URLs, track clicks, and generate QR codes. Built with Spring Boot backend and React frontend.

##  Features

- Generate short URLs with auto-generated or custom codes
- Redirect to original URLs with click tracking
- QR code generation and download
- View analytics (click count per short URL)
- PostgreSQL database for persistence

## Tech Stack

**Backend:** Java 17 | Spring Boot 4.0.5 | Spring Data JPA | PostgreSQL  
**Frontend:** React 18 | Vite | qrcode.react (QR library)

##  Project Structure

```
urlshortener/
├── src/main/java/com/vipul/urlshortener/
│   ├── controller/     → HTTP request handling
│   ├── service/        → Business logic (URL encoding, click tracking)
│   ├── repository/     → Database operations (JPA)
│   ├── entity/         → UrlMapping (DB table)
│   ├── dto/            → Request/Response objects
│   └── util/           → Base62 encoding utility
├── frontend/
│   └── src/
│       ├── App.jsx           → Main React component
│       ├── components/       → QRCodeDisplay component
│       └── App.css           → Styling
├── application.properties    → Database config
└── pom.xml                   → Maven dependencies
```

##  How to Run

### Backend
```bash
cd /home/vipul/IdeaProjects/urlshortener

# Update database credentials in src/main/resources/application.properties

# Run the application
./mvnw spring-boot:run
```
Backend runs on `http://localhost:8080`

### Frontend
```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```
Frontend runs on `http://localhost:3000`

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/shorten` | Create short URL |
| GET | `/{shortCode}` | Redirect to original URL |
| GET | `/api/analytics/{shortCode}` | Get click count |

### Example Requests

**Create Short URL:**
```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl":"https://example.com","customCode":"mylink"}'
```

**Response:**
```json
{
  "shortUrl": "http://localhost:8080/mylink",
  "shortCode": "mylink"
}
```

