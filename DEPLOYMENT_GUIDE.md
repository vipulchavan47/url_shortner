# URL Shortener - Deployment Guide

## Quick Start

### Local Development

```bash
# Build
mvn clean package

# Run
java -jar target/urlshortener-0.0.1-SNAPSHOT.jar

# Access
http://localhost:8080
```

## Railway Deployment

### Prerequisites
- Railway account
- PostgreSQL Neon database connection string
- Git repository

### Steps

1. **Create Railway Project**
   - Go to https://railway.app
   - Create new project
   - Connect GitHub repository

2. **Configure Database**
   - Create PostgreSQL database (use Neon)
   - Copy connection string

3. **Set Environment Variables**
   ```
   JAVA_HOME=/usr/lib/jvm/java-17-openjdk
   app.base.url=https://your-app.railway.app
   SPRING_DATASOURCE_URL=jdbc:postgresql://[neon-host]/[database]?sslmode=require&channelBinding=require
   SPRING_DATASOURCE_USERNAME=[neon-user]
   SPRING_DATASOURCE_PASSWORD=[neon-password]
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   ```

4. **Deploy**
   - Push to GitHub
   - Railway automatically builds and deploys
   - Access your app at https://your-app.railway.app

## Environment Configuration

### Local Development
```properties
app.base.url=http://localhost:8080
spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortener
```

### Production (Railway)
```properties
app.base.url=https://your-app.railway.app
spring.datasource.url=jdbc:postgresql://[neon-host]/neondb?sslmode=require&channelBinding=require
```

## API Documentation

### Create Short URL

**Endpoint:** `POST /api/shorten`

**Request:**
```json
{
  "longUrl": "https://example.com/very/long/url",
  "customCode": "optional"
}
```

**Response (Success):**
```json
{
  "shortUrl": "https://your-app.railway.app/abc123",
  "shortCode": "abc123"
}
```

**Response (Error):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Custom alias already exists"
}
```

### Redirect

**Endpoint:** `GET /{shortCode}`

- Returns 302 redirect to original URL
- Example: `https://your-app.railway.app/abc123` → redirects to long URL

## Frontend

The frontend is automatically served from `/` path and includes:

- **HTML**: `index.html` - Single page application
- **CSS**: `css/style.css` - Responsive styling
- **JavaScript**: `js/app.js` - Client-side logic

All files are embedded in the JAR during build.

## Database Schema

```sql
CREATE TABLE url_mapping (
    id BIGSERIAL PRIMARY KEY,
    long_url TEXT NOT NULL,
    short_code VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    click_count BIGINT DEFAULT 0
);
```

Tables are automatically created when the application starts.

## Monitoring

### Check Application Status
```bash
curl https://your-app.railway.app/
```

### Health Check
```bash
curl https://your-app.railway.app/api/shorten -X POST \
  -H "Content-Type: application/json" \
  -d '{"longUrl":"https://example.com"}'
```

## Troubleshooting

### Issue: Database Connection Failed
- Verify Neon connection string is correct
- Check network connectivity to Neon
- Ensure SSL certificates are valid

### Issue: Frontend Not Loading
- Clear browser cache
- Check browser console for errors
- Verify static files are in JAR: `jar tf target/urlshortener-0.0.1-SNAPSHOT.jar | grep static`

### Issue: Redirect Not Working
- Check short code exists in database
- Verify correct short code is being used
- Check original URL format

## Performance

- Single JAR deployment
- Stateless API (can scale horizontally)
- PostgreSQL connection pooling (HikariCP)
- Frontend caching via HTTP headers
- No external dependencies

## Security Considerations

- CORS enabled for frontend (adjust origins if needed)
- Input validation on both frontend and backend
- SQL injection prevention via parameterized queries
- HTTPS required in production
- Environment variables for sensitive data

## Next Steps for Production

1. **Add SSL/TLS** - Railway provides free SSL certificates
2. **Set up monitoring** - Use Railway's built-in monitoring
3. **Configure custom domain** - Point your domain to Railway
4. **Add rate limiting** - Prevent abuse
5. **Set up analytics** - Track URL usage
6. **Implement logging** - Better debugging
7. **Add authentication** - For API access control

## Maintenance

### Backup Database
```bash
pg_dump [connection-string] > backup.sql
```

### Check Logs
- In Railway dashboard, check "Deployments" tab
- View real-time logs for your application

### Update Application
1. Make code changes
2. Commit and push to GitHub
3. Railway automatically rebuilds and deploys

