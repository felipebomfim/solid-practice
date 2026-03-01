# Solid Practice

Projeto Spring Boot para **prática e aprendizado dos princípios SOLID**. A aplicação começa **intencionalmente sem seguir SOLID**, para que você possa refatorá-la passo a passo aplicando cada princípio.

---

## O que é esta aplicação?

Uma **API REST de tarefas (Tasks)** — um CRUD simples onde você pode listar, criar, editar, marcar como concluída e excluir tarefas. Os dados ficam em **PostgreSQL**, com schema e dados iniciais gerenciados pelo **Flyway**.

### Por que o código está “ruim” de propósito?

O código atual **viola vários princípios SOLID** de forma deliberada, por exemplo:

-   **SRP (Single Responsibility)**  
    O `TaskController` concentra validação, mapeamento para DTO, regras de negócio e acesso a dados.

-   **DIP (Dependency Inversion)**  
    O controller depende diretamente do `TaskRepository` (implementação concreta), sem abstração de serviço.

-   **OCP (Open/Closed)**  
    Incluir novos comportamentos tende a exigir alterar o próprio controller em vez de estender pontos bem definidos.

O objetivo é você **refatorar** esse código aplicando SOLID, com os **testes de integração** garantindo que o comportamento da API não quebre.

---

## Requisitos

-   **Java 21+**
-   **Maven 3.9+**
-   **Docker e Docker Compose** (para subir o PostgreSQL)

---

## Como rodar

### 1. Subir o banco (PostgreSQL)

```bash
docker compose up -d
```

Isso sobe um PostgreSQL 16 na porta **5432** com:

-   **Banco:** `solid_practice`
-   **Usuário:** `app`
-   **Senha:** `appsecret`

### 2. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

A API fica em **http://localhost:8080**.

### 3. Rodar os testes

Os testes de integração rodam com **H2 em memória** (perfil `test`), então **não é preciso ter Docker rodando** para executá-los. O schema e os dados iniciais são aplicados via Flyway (as mesmas migrations usadas em produção).

```bash
./mvnw test
```

---

## API – Endpoints

| Método | Endpoint                     | Descrição                                                       |
| ------ | ---------------------------- | --------------------------------------------------------------- |
| GET    | `/api/health`                | Status da aplicação                                             |
| GET    | `/api/tasks`                 | Lista todas as tarefas                                          |
| GET    | `/api/tasks?completed=true`  | Lista só concluídas                                             |
| GET    | `/api/tasks?completed=false` | Lista só pendentes                                              |
| GET    | `/api/tasks/{id}`            | Busca uma tarefa por ID                                         |
| POST   | `/api/tasks`                 | Cria uma tarefa (body: `title`, opcional `description`)         |
| PUT    | `/api/tasks/{id}`            | Atualiza uma tarefa (body: `title`, `description`, `completed`) |
| PATCH  | `/api/tasks/{id}/complete`   | Marca a tarefa como concluída                                   |
| DELETE | `/api/tasks/{id}`            | Remove uma tarefa                                               |

### Exemplos (curl)

```bash
# Listar tarefas
curl -s http://localhost:8080/api/tasks | jq

# Criar tarefa
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Minha tarefa","description":"Detalhes"}' | jq

# Atualizar
curl -s -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Título novo","completed":true}' | jq

# Marcar como concluída
curl -s -X PATCH http://localhost:8080/api/tasks/1/complete | jq

# Excluir
curl -s -X DELETE http://localhost:8080/api/tasks/1 -w "%{http_code}"
```

---

## Dados iniciais (Flyway)

Ao subir a aplicação (ou ao rodar os testes com Testcontainers), o Flyway aplica as migrations em `src/main/resources/db/migration/`:

-   **V1\_\_create_tasks_table.sql** – Cria a tabela `tasks`.
-   **V2\_\_insert_initial_tasks.sql** – Insere 4 tarefas iniciais (ex.: “Learn SOLID principles”, “Refactor TaskController”, etc.).

Assim, ao acessar `GET /api/tasks` logo após subir o app, você já vê tarefas de exemplo.

---

## Testes

-   **Integração:**  
    `TaskControllerIntegrationTest` cobre todos os endpoints (listagem, filtro por `completed`, get por id, create, update, mark complete, delete). Os testes usam o perfil `test` com **H2 em memória** (configurado em `application-test.yml`). A base `AbstractIntegrationTest` ativa esse perfil e sobe o contexto completo da aplicação.

-   **Contexto:**  
    `SolidPracticeApplicationTests` garante que o contexto Spring sobe (também usando o mesmo suporte a Testcontainers).

-   **Health:**  
    `HealthControllerTest` garante que `/api/health` retorna status e nome da aplicação.

Ao refatorar para SOLID, use **sempre** `./mvnw test` para garantir que a API continua com o mesmo comportamento.

---

## Estrutura do projeto

```
solid-practice/
├── docker-compose.yml          # PostgreSQL para desenvolvimento
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/.../solidpractice/
    │   │   ├── SolidPracticeApplication.java
    │   │   ├── controller/
    │   │   │   └── HealthController.java
    │   │   └── task/
    │   │       ├── Task.java           # Entidade JPA
    │   │       ├── TaskRepository.java # Spring Data JPA
    │   │       └── TaskController.java  # API de tarefas (intencionalmente não-SOLID)
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           ├── V1__create_tasks_table.sql
    │           └── V2__insert_initial_tasks.sql
    └── test/
        └── java/.../solidpractice/
            ├── AbstractIntegrationTest.java   # Testcontainers + Spring Boot
            ├── SolidPracticeApplicationTests.java
            ├── controller/HealthControllerTest.java
            └── task/TaskControllerIntegrationTest.java
```

---

## Próximos passos (SOLID)

1. **SRP** – Extrair validação, mapeamento e regras de negócio do `TaskController` para serviços e DTOs dedicados.
2. **OCP** – Introduzir extensões (por exemplo, regras de conclusão ou notificações) sem alterar o controller/serviço existente.
3. **LSP** – Garantir que implementações de repositório ou de serviços possam ser trocadas sem quebrar contratos.
4. **ISP** – Quebrar interfaces “gordas” em interfaces menores e específicas por uso.
5. **DIP** – Fazer o controller depender de abstrações (interfaces de serviço), não do repositório direto.

Mantenha os testes de integração passando a cada refatoração.
