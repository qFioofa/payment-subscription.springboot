COMPOSE := docker compose
GRADLE_IMAGE := gradle:8.14-jdk21

.PHONY: check-tools run-all run-database run-backend build-images stop-all restart-all show-logs show-status open-psql run-tests clean-all

check-tools:
	@command -v docker >/dev/null || { echo "нет docker"; exit 1; }
	@$(COMPOSE) version >/dev/null 2>&1 || { echo "нет docker compose v2 (плагин compose)"; exit 1; }
	@docker info >/dev/null 2>&1 || { echo "демон docker не запущен"; exit 1; }
	@echo "ок: все нужные инструменты установлены"

.env: .env.example
	@cp .env.example .env && echo "создан .env"

run-all: check-tools .env
	$(COMPOSE) up -d --build
	@echo "swagger: http://localhost:$$(grep '^BACKEND_PORT=' .env | cut -d= -f2)/docs"

run-database: check-tools .env
	$(COMPOSE) up -d --build database

run-backend: check-tools .env
	$(COMPOSE) up -d --build backend

build-images: check-tools .env
	$(COMPOSE) build

stop-all:
	$(COMPOSE) down

restart-all: stop-all run-all

show-logs:
	$(COMPOSE) logs -f

show-status:
	$(COMPOSE) ps

open-psql: .env
	$(COMPOSE) exec database sh -c 'psql -U "$$POSTGRES_USER" -d "$$POSTGRES_DB"'

run-tests:
	docker run --rm -v "$(CURDIR)/backend:/src" -w /src $(GRADLE_IMAGE) gradle --no-daemon test

clean-all:
	$(COMPOSE) down -v --remove-orphans
