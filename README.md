# Booking API

[![CI](https://github.com/yetarr/booking-api/actions/workflows/ci.yml/badge.svg)](https://github.com/yetarr/booking-api/actions/workflows/ci.yml)

A multi-tenant appointment booking backend (demo domain: a barbershop) built with Java and Spring Boot.

The core engineering problem is **guaranteeing no double-booking under concurrent load**.

> **Status:** work in progress. Milestone 1 (foundation) is complete. See [Roadmap](#roadmap).

## Tech stack

| Area | Choice |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Security, Data JPA, Validation, Actuator) |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Testing | JUnit 5, Spring Boot Test, Testcontainers |
| Build | Maven (wrapper included) |
| CI | GitHub Actions |
| Local infrastructure | Docker Compose |

## Running locally

### Prerequisites

- Java 21
- Docker (with the Compose plugin)

### Steps

```bash
# 1. Create your local configuration
cp .env.example .env
# edit .env if you want different values

# 2. Start PostgreSQL
docker compose up -d

# 3. Run the application
./mvnw spring-boot:run
```

The application reads its database settings from `.env` (or from real environment variables, which take precedence). The variables are:

| Variable | Meaning |
|---|---|
| `DB_NAME` | Database name created by the Compose service |
| `DB_USER` | Database user |
| `DB_PASSWORD` | Database password |
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/<DB_NAME>` |

Flyway applies the migrations in `src/main/resources/db/migration` on startup.

To check the app is up:

```bash
curl -i localhost:8080/actuator/health
```

Every other endpoint currently requires authentication.

To reset the local database completely (this deletes its data):

```bash
docker compose down -v && docker compose up -d
```

## Running the tests

```bash
./mvnw verify
```

Tests start their own PostgreSQL 16 container with Testcontainers, so they only need Docker running. They do not need `.env` or the Compose database.

## Design decisions

- **Tests run against PostgreSQL, not H2.** The project relies on PostgreSQL features (`btree_gist`, exclusion constraints, range types) that H2 does not provide.
- **Flyway owns the schema.** Schema changes are versioned migrations, and applied migrations are never edited. Constraints are explicitly named so violations can be told apart and mapped to API errors.
- **The database enforces data rules, not only the application.** For example, tenant names must be non-blank and unique ignoring case and surrounding whitespace, enforced by a check constraint and a unique index.
- **Secure by default.** All endpoints require authentication except the health endpoint, which is permitted explicitly.

## Roadmap

- [x] **1. Foundation:** project skeleton, Docker Compose with Postgres, Flyway baseline, security defaults, Testcontainers tests, CI
- [ ] 2. Auth and tenancy: users, roles, JWT, tenant isolation
- [ ] 3. Catalog and scheduling: resources, services, availability rules, time off
- [ ] 4. Availability engine: slot computation with full unit tests
- [ ] 5. Booking core: create and cancel with the exclusion constraint, 409 mapping, idempotency, concurrency test
- [ ] 6. Lifecycle: reschedule, complete and no-show
- [ ] 7. Observability and performance: metrics, dashboard, k6 load test
- [ ] 8. Polish and ship: OpenAPI docs, seed data, architecture diagram, deployment

## Project layout

```
src/main/java/dev/yetar/booking/
├── BookingApiApplication.java
├── common/        # shared configuration (security)
└── ...            # feature packages are added per milestone
src/main/resources/db/migration/   # Flyway migrations
```

The code is organized by feature, not by layer.