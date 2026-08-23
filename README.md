# URL Shortener

A full-stack web-application to generate short URLs, track clicks, generate QR codes, and view detailed URL analytics. Built with Spring Boot backend and React frontend.

## Features

- Generate short URLs with auto-generated or custom codes
- Redirect to original URLs with click tracking
- QR code generation and download
- Configurable URL expiration
- Persistent URL analytics without user accounts
- Analytics dashboard with:
    - Total and daily clicks
    - Clicks over time
    - Device breakdown
    - Referrer/source breakdown
    - Recent click activity
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
│   ├── entity/         → UrlMapping & AnalyticsEvent
│   ├── dto/            → Request/Response objects
│   ├── config/         → Application configuration
│   └── util/           → Base62 encoding utility
├── frontend/
│   └── src/
│       ├── App.jsx           → Main React component
│       ├── components/       → UI components
│       └── App.css           → Styling
├── application.properties    → Database config
└── pom.xml                   → Maven dependencies
````

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

| Method | Endpoint                          | Purpose                   |
| ------ | --------------------------------- | ------------------------- |
| POST   | `/api/shorten`                    | Create short URL          |
| GET    | `/{shortCode}`                    | Redirect and record click |
| GET    | `/api/analytics/{analyticsToken}` | Get URL analytics         |

## Analytics

Each URL receives a unique analytics token that provides access to its analytics dashboard without requiring an account.

Analytics include:

* Total clicks
* Daily click statistics
* Device breakdown
* Referrer/source statistics
* Recent clicks

Click events are stored separately from the URL mapping to support persistent analytics.

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
  "analyticsUrl": "http://localhost:3000/analytics/8fK2...",
  "shortCode": "mylink"
}
```

## Live Demo

Frontend: [https://bitshortner.vercel.app/](https://bitshortner.vercel.app/)

Backend: Deployed on Render



