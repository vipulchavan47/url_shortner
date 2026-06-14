# URL Shortener - Deployment Guide

## Overview

This project consists of:
- **Backend**: Spring Boot REST API (Java) - deployed to Railway
- **Frontend**: React app (Vite) - deployed to Vercel

Both are separate deployments that communicate via API calls.

---

## Backend Deployment (Spring Boot on Railway)

### Prerequisites
- GitHub repository with Spring Boot code
- Railway account (railway.app)
- Environment variables for database

### Steps

1. **Create new service on Railway**
   - Go to railway.app
   - Click "New Project" → "Deploy from GitHub"
   - Select your repo

2. **Configure environment**
   - Railway auto-detects it's a Java project
   - Add environment variables:
     ```
     SPRING_DATASOURCE_URL=jdbc:postgresql://...
     SPRING_DATASOURCE_USERNAME=...
     SPRING_DATASOURCE_PASSWORD=...
     APP_BASE_URL=https://your-railway-app.railway.app
     ```

3. **Deploy**
   - Railway auto-deploys on every GitHub push
   - Check "Deployments" tab for status

Your backend will be available at: `https://your-railway-app.railway.app/api/shorten`

---

## Frontend Deployment (React on Vercel)

### Prerequisites
- GitHub repo with React code (in `frontend/` directory)
- Vercel account (vercel.com)

### Steps

1. **Push frontend to GitHub**
   ```bash
   cd frontend/
   npm run build  # Test build locally
   git add .
   git commit -m "Add React frontend"
   git push
   ```

2. **Create Vercel project**
   - Go to vercel.com
   - Click "Add New..." → "Project"
   - Import your GitHub repository
   - Select "Frontend" project type
   - Vercel auto-detects Vite

3. **Configure environment**
   - In Vercel Dashboard, go to Settings → Environment Variables
   - Add:
     ```
     VITE_API_URL=https://your-railway-app.railway.app
     ```

4. **Deploy**
   - Vercel auto-deploys on every GitHub push to `frontend/`
   - Check "Deployments" tab for status

Your frontend will be available at: `https://your-project.vercel.app`

---

## Local Development

### Start Backend
```bash
# In project root
mvn clean package -DskipTests
java -jar target/urlshortener-0.0.1-SNAPSHOT.jar
```
Backend runs on: http://localhost:8080

### Start Frontend
```bash
# In frontend/ directory
npm install
npm run dev
```
Frontend runs on: http://localhost:3000

The dev server automatically proxies API calls to `http://localhost:8080/api`

---

## Testing

1. Open http://localhost:3000 (or your Vercel URL in production)
2. Enter a long URL: `https://example.com/very/long/path`
3. Optionally add custom code: `myshortcode`
4. Click "Generate Short URL"
5. You should see: `http://localhost:8080/myshortcode` (or your Railway domain)
6. Click "Copy" or "Open Link" to verify it works

---

## Troubleshooting

### Frontend can't reach backend
- Check `VITE_API_URL` environment variable
- Verify backend is running
- Check CORS configuration in UrlController (should have `@CrossOrigin(origins = "*")`)

### 404 errors on short URL
- Verify backend is serving the `/` endpoint correctly
- Check database has entries with `short_code` field

### Build errors
- Clear `node_modules`: `rm -rf node_modules && npm install`
- Clear Vite cache: `rm -rf dist`
- Rebuild: `npm run build`

---

## Production Checklist

Before deploying to production:

- [ ] Backend environment variables set correctly
- [ ] Frontend `VITE_API_URL` points to production backend
- [ ] Database migrations have run (`spring.jpa.hibernate.ddl-auto=update`)
- [ ] SSL/HTTPS enabled (both Railway and Vercel default to HTTPS)
- [ ] Test the full flow end-to-end
- [ ] Monitor logs for any errors

---

## Summary

| Component | Hosting | URL | Command |
|-----------|---------|-----|---------|
| Backend | Railway | `https://your-app.railway.app` | Automatic (GitHub push) |
| Frontend | Vercel | `https://your-app.vercel.app` | Automatic (GitHub push) |
| Database | Neon | PostgreSQL connection string | Already configured |

Both deployments are **completely automated** - just push to GitHub!
