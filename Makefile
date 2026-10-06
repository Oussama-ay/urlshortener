.DEFAULT_GOAL := help

COMPOSE ?= docker compose

.PHONY: help install deps backend-local frontend-local test frontend-check down

help:
	@printf '%s\n' \
	  'make install       Install frontend dependencies' \
	  'make deps          Start PostgreSQL and Redis locally' \
	  'make backend-local Run the Spring Boot API with the local profile' \
	  'make frontend-local Run the React frontend' \
	  'make test          Prepare a separate test database and run backend tests' \
	  'make frontend-check Install, build, and lint the frontend' \
	  'make down          Stop the local Docker stack'

install:
	cd frontend && npm ci

deps:
	$(COMPOSE) up -d --wait postgres redis

backend-local: deps
	cd backend && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run

frontend-local:
	cd frontend && npm run dev

test: deps
	$(COMPOSE) exec -T postgres psql -U postgres -d postgres -v ON_ERROR_STOP=1 < backend/scripts/create-test-database.sql
	./backend/mvnw -f backend/pom.xml clean test

frontend-check:
	cd frontend && npm ci && npm run build && npm run lint

down:
	$(COMPOSE) down
