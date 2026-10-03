.DEFAULT_GOAL := help

COMPOSE ?= docker compose

.PHONY: help install dev up down logs ps deps test test-backend test-frontend

help:
	@printf '%s\n' \
	  'make install        Install frontend dependencies from the lockfile' \
	  'make dev            Start the Docker backend stack, then Vite locally' \
	  'make up             Build and start the Docker backend stack' \
	  'make down           Stop the Docker stack (keep database data)' \
	  'make logs           Follow Docker service logs' \
	  'make ps             Show Docker service status' \
	  'make deps           Start PostgreSQL and Redis only' \
	  'make test           Run backend and frontend checks' \
	  'make test-backend   Start dependencies and run Maven tests' \
	  'make test-frontend  Run ESLint and the TypeScript/Vite build'

install:
	cd frontend && npm ci

dev: up
	cd frontend && npm run dev

up:
	$(COMPOSE) up --build -d --wait

down:
	$(COMPOSE) down

logs:
	$(COMPOSE) logs -f

ps:
	$(COMPOSE) ps

deps:
	$(COMPOSE) up -d --wait postgres redis

test: test-backend test-frontend

test-backend: deps
	cd backend && ./mvnw clean test

test-frontend:
	cd frontend && npm run lint
	cd frontend && npm run build
