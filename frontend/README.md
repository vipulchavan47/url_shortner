# URL Shortener Frontend

A minimal React frontend for the URL Shortener application.

## Setup

```bash
npm install
```

## Development

Run the development server (with backend proxy):

```bash
npm run dev
```

Open http://localhost:3000

## Build for Production

```bash
npm run build
```

This creates a `dist/` folder ready for deployment to Vercel or any static hosting.

## Environment Variables

Create a `.env.local` file to set the API base URL:

```
VITE_API_URL=https://your-backend-api.com
```

For development, it defaults to `http://localhost:8080`.

## Deployment to Vercel

1. Push code to GitHub
2. Go to [vercel.com](https://vercel.com)
3. Click "New Project"
4. Select your GitHub repo
5. Vercel auto-detects it's a Vite app
6. Add environment variable `VITE_API_URL` pointing to your backend
7. Deploy!

That's it! Vercel handles everything automatically.
