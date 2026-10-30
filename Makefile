.DEFAULT_GOAL := help
.PHONY: help build run test clean swagger

MVN := ./mvnw

help: ## Lista os comandos disponíveis
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

build: ## Compila o projeto (sem testes)
	$(MVN) -DskipTests package

test: ## Roda a suíte de testes
	$(MVN) test

run: ## Roda a app local (Spring Boot)
	$(MVN) spring-boot:run

clean: ## Limpa artefatos de build
	$(MVN) clean

swagger: ## Abre o Swagger UI no navegador
	@xdg-open http://localhost:8080/swagger-ui.html >/dev/null 2>&1 || echo "Abra: http://localhost:8080/swagger-ui.html"
