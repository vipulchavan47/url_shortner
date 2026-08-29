# URL Shortener

A full-stack web-application to generate short URLs, generate QR codes, and configure URL expiration. Built with Spring Boot backend and React frontend.

## Features

- Generate short URLs with auto-generated or custom codes
- Redirect to original URLs with expiration checks
- QR code generation and download
- Configurable URL expiration
- Automatic cleanup of expired URLs
- Endpoint-level rate limiting
- PostgreSQL database for persistence

## Tech Stack

**Backend:** Java 17 | Spring Boot 4.0.5 | Spring Data JPA | PostgreSQL  
**Frontend:** React 18 | Vite | qrcode.react (QR library)

## Project Structure

```text
urlshortener/
├── src/main/java/com/vipul/urlshortener/
│   ├── controller/     → HTTP request handling
│   ├── service/        → Business logic
│   ├── repository/     → Database operations (JPA)
│   ├── entity/         → UrlMapping
│   ├── dto/            → Request/Response objects
│   └── util/           → Base62 encoding utility
├── frontend/
│   └── src/
│       ├── App.jsx           → Main React component
│       ├── components/       → UI components
│       └── App.css           → Styling
├── application.properties    → Database config
└── pom.xml                   → Maven dependencies
```

## How to Run

### Backend

```bash
cd /home/vipul/IdeaProjects/urlshortener

# Update database credentials in src/main/resources/application.properties

./mvnw spring-boot:run
```

Backend runs on `http://localhost:8080`

### Frontend

```bash
cd frontend

npm install
npm run dev
```

Frontend runs on `http://localhost:3000`

## API Endpoints

| Method | Endpoint            | Purpose            |
| ------ | ------------------- | ------------------ |
| POST   | `/api/shorten`      | Create short URL   |
| GET    | `/{shortCode}`      | Redirect to URL    |

## Example Request

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

## Live Demo

Frontend: [https://bitshortner.vercel.app/](https://bitshortner.vercel.app/)

Backend: Deployed on Render