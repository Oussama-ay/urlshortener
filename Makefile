.DEFAULT_GOAL := help

COMPOSE ?= docker compose

.PHONY: help install deps backend-local frontend-local down

help:
	@printf '%s\n' \
	  'make install       Install frontend dependencies' \
	  'make deps          Start PostgreSQL and Redis locally' \
	  'make backend-local Run the Spring Boot API with the local profile' \
	  'make frontend-local Run the React frontend' \
	  'make down          Stop the local Docker stack'

install:
	cd frontend && npm ci

deps:
	$(COMPOSE) up -d --wait postgres redis

backend-local: deps
	cd backend && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run

frontend-local:
	cd frontend && npm run dev

down:
	$(COMPOSE) down
