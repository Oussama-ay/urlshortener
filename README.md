# URL Shortener

A URL shortening service with a Spring Boot backend and a React + TypeScript frontend scaffold.

## Project structure

- `backend/`: Spring Boot API, Maven wrapper, tests, and Dockerfile
- `frontend/`: React + TypeScript + Vite starter
- `docker-compose.yml`: backend, PostgreSQL, and Redis
- `Makefile`: development and verification commands

## Run locally

You need Docker with Compose, Node.js compatible with the frontend dependencies, npm, and Make. Java 17 is also required for local backend tests or running Spring Boot outside Docker.

From the repository root, install frontend dependencies once (and again when the lockfile changes), then start development:

```bash
make install
make dev
```

`make dev` builds and starts the backend, PostgreSQL, and Redis in Docker, then runs Vite locally in the foreground. The API runs at `http://localhost:8080`, with Swagger at `http://localhost:8080/swagger-ui/index.html`.

Open the URL printed by Vite (normally `http://localhost:5173`). The frontend currently shows the default React starter page; API integration is the next step.

Press Ctrl+C to stop Vite. The Docker services keep running until you stop them:

```bash
make down
```

`make down` preserves database data. Use `make up` to start just the Docker stack, `make logs` to follow its logs, and `make ps` to see service status. Run `make help` for all commands.

To run the backend directly with Java 17 instead of its container:

```bash
make deps
cd backend
./mvnw spring-boot:run
```

Stop the backend container first if it is already using port 8080.

## Checks

From the repository root:

```bash
make test
```

This starts PostgreSQL and Redis, runs the backend tests, then runs frontend lint and build checks. Use `make test-backend` or `make test-frontend` to check one side. Tests use the configured database and Redis; use a development database. The dependency containers stay running until `make down`.

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
