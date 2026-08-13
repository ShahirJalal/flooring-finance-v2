# Flooring Job Profit Calculator

A simple internal tool for a small flooring business in Malaysia. It answers
one question, quickly:

> For this job - how much did I collect, how much did it cost, and how much
> profit did I make?

This is **not** an accounting, ERP, payroll, inventory or CRM system. It has
four sections - Dashboard, Jobs, Reports, Settings - and one core entity:
the **Job**, which carries a Collection amount and five cost buckets
(Materials, Delivery, Other Costs, Worker Salary, Worker Food).

```
Total Cost = Materials + Delivery + Other Costs + Worker Salary + Worker Food
Profit     = Collection - Total Cost
Margin     = Profit / Collection x 100   (0% when Collection is 0)
```

All of this is computed on the backend, in one place
(`JobCalculationService`), never in Angular.

---

## Tech stack

| Layer      | Technology |
|------------|------------|
| Frontend   | Angular 19 (standalone components), PrimeNG + PrimeFlex, Nginx |
| Backend    | Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, Flyway |
| Database   | PostgreSQL 16 |
| Auth       | JWT in an httpOnly/SameSite=Strict cookie (single owner account) |
| Deployment | Docker, Docker Compose, Jenkins, Ubuntu Server, Cloudflare Tunnel |

## Repository structure

```
flooring-finance/            (this repo)
├── frontend/                 Angular application
├── backend/                  Spring Boot application
├── docker-compose.yml
├── Jenkinsfile
├── .env.example
└── README.md
```

Backend package layout: `config / security / common / entity / repository /
dto / mapper / service / controller / exception / seed`. The whole domain is
one aggregate: `Job` plus `MaterialCost / DeliveryCost / OtherCost /
WorkerCost / WorkerFoodCost`, each a simple line item belonging to a job.

Frontend layout: `core / shared / features/{auth,dashboard,jobs,reports,settings}`.

---

## Prerequisites

- **Docker** and **Docker Compose** (v2) - the only hard requirement to run
  the whole stack.
- For backend-only development: **Java 17** (a Maven Wrapper, `./mvnw`, is
  included).
- For frontend-only development: **Node.js 20+** and npm.

---

## Quick start (Docker Compose)

```bash
cp .env.example .env      # then edit .env - see Environment variables below
docker compose up -d --build
```

- Frontend: http://localhost
- Backend API: http://localhost:8080/api
- Postgres: localhost:5432 (credentials from `.env`)

With `SPRING_PROFILES_ACTIVE=dev` (the default), the backend seeds five
realistic flooring jobs (Taman Melawati House, Shah Alam Office, Kajang
House, Seremban Shop, Johor Bahru Project) with real MYR amounts across
every cost category, so the dashboard and reports have real numbers
immediately.

### Seed login (development only)

```
Username: owner
Password: ChangeMe123!
```

Created by `backend/src/main/java/.../seed/DataSeeder.java`, which only runs
under the `dev` profile and only when the `app_user` table is empty.
**Change this password (or switch to `SPRING_PROFILES_ACTIVE=prod` and
create a real account) before this is ever exposed outside your own machine.**

---

## Local development (without Docker)

### Database

```bash
docker compose up -d postgres
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Runs on `http://localhost:8080` with the `dev` profile by default, seeding
sample data and running Flyway migrations automatically.

### Frontend

```bash
cd frontend
npm install
npm start   # ng serve, with proxy.conf.json forwarding /api to localhost:8080
```

Runs on `http://localhost:4200`.

---

## Environment variables

All variables are documented in [`.env.example`](.env.example). Summary:

| Variable | Used by | Purpose |
|---|---|---|
| `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | postgres, backend | Database credentials |
| `SPRING_PROFILES_ACTIVE` | backend | `dev` (seeds sample data) or `prod` |
| `JWT_SECRET` | backend | Signing key for auth JWTs - set a long random value in production |
| `JWT_EXPIRATION_MS` | backend | Session length in milliseconds (default 7 days) |
| `APP_CORS_ALLOWED_ORIGINS` | backend | Allowed frontend origin(s) for CORS |
| `COOKIE_SECURE` | backend | `true` in production (HTTPS); `false` for local http://localhost |
| `POSTGRES_PORT` / `BACKEND_PORT` / `FRONTEND_PORT` | all | Host ports - change if they clash with something else |

Never commit a real `.env` file - it's gitignored.

---

## Calculation logic

Everything financial lives in one backend class:
`backend/src/main/java/com/flooring/finance/service/JobCalculationService.java`.
It exposes `calculateMaterialTotal`, `calculateDeliveryTotal`,
`calculateOtherCostTotal`, `calculateWorkerCostTotal`,
`calculateWorkerFoodTotal`, `calculateTotalCost`, `calculateProfit` and
`calculateProfitMargin` - each a plain `BigDecimal` sum/subtraction, with
profit margin guarded against divide-by-zero when Collection is 0.
`DashboardService` and `ReportService` simply sum these same per-job numbers
across a date range. If the owner ever wants the formula to work
differently, this is the only file that needs to change.

---

## Docker deployment

- **backend/Dockerfile**: Maven build (via `./mvnw`) → `eclipse-temurin:17-jre-alpine` runtime, non-root user.
- **frontend/Dockerfile**: `npm run build` → static files served by `nginx:alpine`, which also reverse-proxies `/api/*` to the `backend` service on the Compose network (see `frontend/nginx.conf`).

`docker-compose.yml` wires up `postgres`, `backend` and `frontend` with a
named volume for Postgres data and environment variables sourced from `.env`.

---

## Jenkins deployment

The included `Jenkinsfile` is a simple declarative pipeline for an Ubuntu
server with Docker, the Compose plugin, Java 17 and Node 20 available:

1. **Checkout** - pulls this repository.
2. **Load environment** - copies a Jenkins "Secret file" credential named
   `flooring-finance-env` into `.env` (create this once in
   *Manage Jenkins → Credentials* with your real production `.env` contents).
3. **Build Backend** - `./mvnw package`.
4. **Build Frontend** - `npm ci && npm run build`.
5. **Build Docker Images** - `docker compose build --pull`.
6. **Stop Existing Containers** - `docker compose down`.
7. **Docker Compose Up** - `docker compose up -d`.
8. **Health Check** - polls `/actuator/health` and the frontend root until
   both respond, failing (with logs) if they don't come up in time.
9. **Cleanup** - `docker image prune -f`.

---

## Cloudflare Tunnel

In production this app is reached at `finance.shahirjalal.com` via a
Cloudflare Tunnel on the Ubuntu server, pointed at `http://localhost` (the
`frontend` container's published port). No Cloudflare-specific
configuration lives inside this repo - the application works the same over
plain HTTP on localhost/Docker networking either way. Configure the tunnel
with `cloudflared` on the server (`cloudflared tunnel create`, add a DNS
route, point the ingress rule at `http://localhost:80`).

---

## Malaysia-specific conventions

- Currency: MYR, always displayed as `RM 1,234.56`.
- Timezone: `Asia/Kuala_Lumpur`.
- Dates: `DD/MM/YYYY` throughout the UI.
- Jobs reference the 13 Malaysian states + 3 federal territories.
- No SST/tax, EPF/SOCSO/PCB, payroll, or accounting-ledger logic - out of
  scope by design.

---

## What's intentionally NOT built

Per the brief, this stays a small tool, not a platform:

- No separate Customer/Supplier/Worker/Inventory entities - customer name
  and worker names are free-text fields on the job and its cost line items.
- No quotations, invoicing, payment/deposit tracking, payroll, or multi-user
  permissions.
- "Collection" is a single figure per job for now (not a list of
  payments) - the field is isolated enough that a deposit/payment list could
  be added later without breaking the API contract.
- Cost line items support add/delete only (no edit) - fixing a typo means
  removing the entry and re-adding it, which keeps the API surface small.

These are deliberate gaps, ready to be extended if the owner asks for them
later.
