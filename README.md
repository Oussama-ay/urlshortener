# URL Shortener

A URL shortening service with a Spring Boot backend and a React + TypeScript frontend scaffold.

## Project structure

- `backend/`: Spring Boot API, Maven wrapper, tests, and Dockerfile
- `frontend/`: React + TypeScript + Vite starter
- `docker-compose.yml`: backend, PostgreSQL, and Redis

## Run locally

Start the backend and its dependencies from the repository root:

```bash
docker compose up --build
```

The API runs at `http://localhost:8080`, with Swagger at `http://localhost:8080/swagger-ui/index.html`.

In another terminal, start the frontend:

```bash
cd frontend
npm ci
npm run dev
```

Open the URL printed by Vite (normally `http://localhost:5173`). The frontend currently shows the default React starter page; API integration is the next step.

To run the backend directly with Java 17 instead of its container:

```bash
docker compose up -d --wait postgres redis
cd backend
./mvnw spring-boot:run
```

Stop the backend container first if it is already using port 8080.

## Checks

With PostgreSQL and Redis running:

```bash
cd backend
./mvnw clean test
```

From `frontend/`:

```bash
npm run build
npm run lint
```

## Features

- Create short URLs
- 302 redirects
- URL expiration
- Click analytics
- Redis caching with TTL
- Redis-backed rate limiting
- PostgreSQL persistence
- OpenAPI / Swagger documentation
- Dockerized application stack

## Tech Stack

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway
- Docker
- OpenAPI / Swagger
