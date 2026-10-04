# URL Shortener

A full-stack URL shortener built with React and Spring Boot. It creates short links, redirects visitors, tracks clicks, supports expiration, and caches redirects with Redis.

## Live Demo

Frontend: [urlshortener-gray.vercel.app](https://urlshortener-gray.vercel.app/)

Backend API: [urlshortener-lgiy.onrender.com](https://urlshortener-lgiy.onrender.com/)

The backend runs on Render's free tier, so the first request after inactivity may take a little longer.

## Architecture

![URL Shortener deployment architecture](docs/deployment-architecture.png)

React frontend on Vercel → Spring Boot API on Render → Neon PostgreSQL and Upstash Redis.

## Features

- Create and redirect short URLs
- URL expiration and click analytics
- Redis caching and rate limiting
- API validation and useful error messages
- Copy shortened links to the clipboard
- Responsive frontend
- OpenAPI / Swagger documentation

## Stack

- Frontend: React, TypeScript, Vite, CSS
- Backend: Java 17, Spring Boot, JPA, Flyway
- Services: Neon PostgreSQL, Upstash Redis, Docker

## Run Locally

Requirements: Docker Compose, Java 17, Node.js, npm, and Make.

```bash
make install
make dev
```

Frontend: `http://localhost:5173`

API: `http://localhost:8080`
Swagger: `http://localhost:8080/swagger-ui/index.html`

Stop the local services with:

```bash
make down
```

## Environment

Local and deployment variable names are documented in:

- `backend/.env.example`
- `frontend/.env.example`

The frontend uses `VITE_API_URL`. The backend uses `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_URL`, `APP_BASE_URL`, and `APP_FRONTEND_URL`.

## Checks

```bash
make test
```

Tests use the local PostgreSQL and Redis containers. Never commit real `.env` files or service credentials.
