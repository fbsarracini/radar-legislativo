.DEFAULT_GOAL := help
.PHONY: help build run test clean swagger db db-down web web-test web-build

# Variáveis do .env (DB_PASSWORD etc.) viram variáveis de ambiente da app.
-include .env
export

MVN := ./mvnw

help: ## Lista os comandos disponíveis
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

build: ## Compila o projeto (sem testes)
	$(MVN) -DskipTests package

test: ## Roda a suíte de testes (os de banco precisam de Docker; sem ele são pulados)
	$(MVN) test

db: ## Sobe o PostgreSQL (docker compose)
	docker compose up -d db

db-down: ## Derruba o PostgreSQL (mantém os dados)
	docker compose down

run: db ## Roda a app local (sobe o banco antes)
	$(MVN) spring-boot:run

clean: ## Limpa artefatos de build
	$(MVN) clean

swagger: ## Abre o Swagger UI no navegador
	@xdg-open http://localhost:8080/swagger-ui.html >/dev/null 2>&1 || echo "Abra: http://localhost:8080/swagger-ui.html"

web/node_modules: web/package-lock.json
	cd web && npm ci
	@touch web/node_modules

web: web/node_modules ## Roda o front Angular em http://localhost:4200 (proxy de /api para :8080)
	cd web && npx ng serve

web-test: web/node_modules ## Roda os testes do front (Vitest)
	cd web && npx ng test --watch=false

web-build: web/node_modules ## Gera o build de produção do front em web/dist
	cd web && npx ng build
