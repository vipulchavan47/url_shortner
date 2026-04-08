# URL Shortener

A simple backend application to generate short URLs and redirect users to the original long URLs.

## Tech Stack

* Java
* Spring Boot
* Spring Data JPA
* MySQL

## Features

* Generate short URL from long URL
* Redirect to original URL using short code
* Layered architecture (Controller → Service → Repository)
* DTO-based request/response handling

## API Endpoints

### Create Short URL

POST `/api/url/shorten`

Request:

```json
{
  "originalUrl": "https://example.com"
}
```

Response:

```json
{
  "shortUrl": "http://localhost:8080/abc123"
}
```

### Redirect

GET `/{shortCode}`
Redirects to the original URL

## Project Structure

* Controller → Handles HTTP requests
* Service → Business logic
* Repository → Database interaction
* DTO → Data transfer between layers

## Setup

1. Clone the repository
2. Create `application.properties` and add creds
3. Configure database credentials
4. Run the application

