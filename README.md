# Let's Travel

A production-ready, full-stack Travel Management System featuring role-based access control, multi-database architecture, real-time search, graph-based recommendations, and Stripe/PayPal payment integrations.

---

## Features

### Traveler
- Browse and search published travels with full-text search and instant autocomplete
- Graph-based personalized recommendations (Neo4j) and trending travels
- Subscribe to travels via Stripe (credit card) or PayPal
- Leave star-rated feedback and comments after completing trips
- File reports against travels or other users
- Personal dashboard showing trip history, spending, and stats
- Manage profile and account (with cascading data cleanup on deletion)

### Manager
- Create, edit, publish, and delete travel listings
- Upload travel images
- View subscriber list per travel
- Income analytics: monthly revenue chart, per-travel breakdown
- Dashboard summary: active travels, total subscribers, average rating

### Admin
- Full user management: view all accounts, promote/demote roles, deactivate users
- Oversight analytics: platform-wide income, top managers, top travels, recent feedback
- Report queue: review and resolve open reports
- All data scoped with audit logging

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2.1, Java 21, Maven multi-module |
| Primary DB | PostgreSQL 16, Flyway migrations (V1–V8), Hibernate ORM |
| Search | Elasticsearch 8.12 (full-text search, autocomplete) |
| Graph DB | Neo4j 5 (personalized recommendations) |
| Cache / Sessions | Redis 7 (JWT refresh tokens, recommendation cache) |
| Payments | Stripe SDK, PayPal REST API |
| Security | Spring Security 6, stateless JWT (access + refresh tokens), BCrypt |
| Frontend | Angular 17 (standalone components, signals) |
| UI | Angular Material 17 |
| State | NgRx 17 (store, effects, router-store) |
| Reverse Proxy | Nginx (TLS termination, security headers, HSTS) |
| Containers | Docker Compose (7-service stack) |

---

## Quick Start (Docker Compose)

**Prerequisites:** Docker 24+ and Docker Compose v2.

```bash
# 1. Clone the repository
git clone https://learn.reboot01.com/git/aabdulhu/travel-plan.git
cd travel-plan

# 2. Copy environment file and add your keys
cp .env.example .env
# Edit .env — at minimum set STRIPE_SECRET_KEY and PAYPAL_CLIENT_ID for payments

# 3. Generate a self-signed TLS certificate (first run only)
bash nginx/generate-certs.sh

# 4. Start the full stack
docker compose up -d

# 5. Open the app
open https://localhost
# Accept the browser warning for the self-signed certificate
```

> **All traffic is routed through Nginx on ports 80 (HTTP→HTTPS redirect) and 443 (HTTPS).  
> The backend and frontend containers are not directly exposed.**

### Default credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@letstravel.com` | `Admin@1234` |

Managers and Traveler accounts are self-registered. Promote a registered user to Manager via **Admin → Users → Change Role**.

---

## Development Setup

### Backend only

```bash
# Start only the infrastructure services
docker compose up -d postgres redis elasticsearch neo4j

# Run the backend
cd backend
mvn spring-boot:run
```

Backend starts on `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI JSON: `http://localhost:8080/api-docs`

### Frontend only

```bash
cd frontend
npm install
npm start
```

Opens at `http://localhost:4200`. The Angular dev-server proxies `/api` requests to `http://localhost:8080`.

---

## Environment Variables

Create a `.env` file at the project root (use `.env.example` as a template):

| Variable | Description |
|----------|-------------|
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `REDIS_PASSWORD` | Redis password (production) |
| `NEO4J_PASSWORD` | Neo4j password |
| `APP_JWT_SECRET` | JWT signing secret — must be ≥ 32 characters |
| `STRIPE_SECRET_KEY` | Stripe secret key (`sk_test_...` or `sk_live_...`) |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret (`whsec_...`) |
| `PAYPAL_CLIENT_ID` | PayPal application client ID |
| `PAYPAL_CLIENT_SECRET` | PayPal application client secret |

Payment keys accept placeholder values (`sk_test_placeholder`) — all non-payment features work without real keys. Subscribe will return the appropriate payment provider error.

---

## Project Structure

```
lets-travel/
├── backend/                        Spring Boot application (Maven module)
│   └── src/main/
│       ├── java/com/letstravel/
│       │   ├── config/             Security, CORS, app properties
│       │   ├── controller/         REST controllers (auth, travels, search, admin…)
│       │   ├── domain/             JPA entities and enums
│       │   ├── dto/                Request/Response records (per feature package)
│       │   ├── event/              Application events (Elasticsearch travel indexing)
│       │   ├── exception/          Global exception handler, custom exceptions
│       │   ├── neo4j/              Neo4j node entities and repositories
│       │   ├── repository/         Spring Data JPA repositories
│       │   ├── search/             Elasticsearch document, repository, SearchService
│       │   ├── security/           JWT filter, UserDetailsService
│       │   └── service/            Business logic services
│       └── resources/
│           ├── application.yml     Main configuration (env-var overridable)
│           └── db/migration/       Flyway SQL migrations V1–V8
├── frontend/                       Angular 17 SPA
│   └── src/app/
│       ├── core/                   Services, models, auth guards, interceptors
│       ├── features/               Feature modules (auth, travels, admin, manager…)
│       └── shared/                 Reusable UI components
├── nginx/
│   ├── nginx-dev.conf              Dev config (self-signed TLS)
│   ├── nginx-prod.conf             Production config (Let's Encrypt, OCSP stapling)
│   └── generate-certs.sh          Self-signed cert generation script
├── docker-compose.yml              Development stack (7 services)
├── docker-compose.prod.yml         Production stack
├── .env.example                    Environment variable template
└── pom.xml                         Maven root POM (multi-module)
```

### Database Migrations

| Migration | Description |
|-----------|-------------|
| V1 | Users table |
| V2 | Manager profiles |
| V3 | Travels |
| V4 | Subscriptions and payments |
| V5 | Feedback and reports |
| V6 | Audit logs |
| V7 | Manager specialties (join table) |
| V8 | Travel tags and images (join tables) |

---

## API Overview

All endpoints are prefixed with `/api`. Authentication uses Bearer JWT tokens.

| Prefix | Access | Description |
|--------|--------|-------------|
| `/api/auth/**` | Public | Register, login, refresh token, logout |
| `/api/travels/**` | Public (read) / Manager (write) | Travel CRUD, image upload |
| `/api/search/**` | Public | Full-text search, autocomplete |
| `/api/recommendations/**` | Public / Traveler | Personalized and trending |
| `/api/subscriptions/**` | Traveler | Subscribe, cancel, list |
| `/api/feedback/**` | Traveler | Submit and view feedback |
| `/api/reports/**` | Traveler | File reports |
| `/api/users/me` | Any authenticated | Profile management |
| `/api/travelers/**` | Traveler | Stats, history |
| `/api/manager/**` | Manager | Travels, subscribers, analytics |
| `/api/admin/**` | Admin | Users, analytics, reports |
| `/api/payments/**` | System | Stripe/PayPal webhooks |

Full interactive documentation: `http://localhost:8080/swagger-ui.html` (when backend is running directly).

---

## Running Tests

```bash
# Backend unit tests
cd backend && mvn test

# Frontend E2E (requires full Docker stack running)
cd frontend && npx cypress run
```

---

## Production Deployment

A `docker-compose.prod.yml` is provided for production use. Key differences from the dev stack:

- All secrets sourced from `.env` (no hardcoded defaults)
- `nginx-prod.conf` uses Let's Encrypt certificates with OCSP stapling, TLSv1.2/1.3 only, and a 2-year HSTS preload header
- `restart: always` on all services

Update `nginx/nginx-prod.conf` with your domain name, then:

```bash
# Obtain a Let's Encrypt certificate (certbot, or your preferred ACME client)
# then:
docker compose -f docker-compose.prod.yml up -d
```

---

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

| Workflow | Trigger | Steps |
|----------|---------|-------|
| `ci.yml` | Push / PR to main | Backend tests → Frontend build → Docker build validation |
| `nightly.yml` | Scheduled (nightly) | Full Docker stack → E2E Cypress tests |
