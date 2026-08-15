# Flooring Job Profit Calculator

A simple internal tool for a small flooring business in Malaysia. It answers
one question, quickly:

> For this job - how much did I collect, how much did it cost, and how much
> profit did I make?

This is **not** an accounting, ERP, payroll, inventory or CRM system. It has
one real screen - **Jobs** - plus **Settings**, and one core entity: the
**Job**, which is just a name, an optional date/customer/location/notes, and
a freeform list of **entries** the owner writes himself. Each entry is a
description, an amount, and a category - `Income`, `Materials`, `Worker`,
`Delivery` or `Other` - rather than a fixed set of cost fields.

```
Total Income = sum of entries tagged Income
Total Cost   = sum of every other entry
Profit       = Total Income - Total Cost
```

All of this is computed on the backend, in one place
(`JobCalculationService`), never in Angular. There is no profit-margin
percentage shown anywhere in the app - just the profit figure itself, in
large green or red text.

---

## Tech stack

| Layer      | Technology |
|------------|------------|
| Frontend   | Angular 19 (standalone components, signals), PrimeNG + PrimeFlex, self-hosted Inter font, Nginx |
| Backend    | Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, Flyway |
| Database   | PostgreSQL 16 |
| Auth       | JWT in an httpOnly/SameSite=Strict cookie (single owner account) |
| Deployment | Docker, Docker Compose, Jenkins, Ubuntu Server, Cloudflare Tunnel |

The UI follows a flat, lined look (bordered panels instead of drop shadows)
with light/dark mode, toggleable from the top bar or the login page.

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
one aggregate: `Job` plus `JobEntry` (one line item each - a category, a
free-text description and an amount).

Frontend layout: `core / shared / features/{auth,jobs,settings}`.

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
- Backend API: reachable at `/api` through the frontend's Nginx proxy - not
  published directly to the host (see Docker deployment below)
- Postgres: internal to the Docker network only; use
  `docker compose exec postgres psql -U finance -d flooring_finance` if you
  need a shell into it

With `SPRING_PROFILES_ACTIVE=dev` (the default), the backend seeds five
hand-written flooring jobs (Taman Melawati House, Shah Alam Office, Kajang
House, Seremban Shop, Johor Bahru Project) plus ~65 programmatically
generated ones spread across the last ~20 months, each with a realistic
entries breakdown, so the Jobs list has real numbers immediately.

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
| `FRONTEND_PORT` | frontend | The only port published to the host - change if it clashes with something else |

Never commit a real `.env` file - it's gitignored.

---

## Calculation logic

Everything financial lives in one backend class:
`backend/src/main/java/com/flooring/finance/service/JobCalculationService.java`.
It sums a job's entries into `Totals(totalIncome, totalCost, profit)` - a
single pass, no per-category subtotals stored anywhere, no percentage. If
the owner ever wants the formula to work differently, this is the only file
that needs to change.

---

## Docker deployment

- **backend/Dockerfile**: Maven build (via `./mvnw`) → `eclipse-temurin:17-jre-alpine` runtime, non-root user.
- **frontend/Dockerfile**: `npm run build` → static files served by `nginx:alpine`, which also reverse-proxies `/api/*` to the `backend` service on the Compose network (see `frontend/nginx.conf`).

`docker-compose.yml` wires up `postgres`, `backend` and `frontend` with a
named volume for Postgres data and environment variables sourced from `.env`.
Only `frontend` publishes a port to the host - Nginx reaches `backend`, and
`backend` reaches `postgres`, entirely over the internal Compose network.

---

## Jenkins deployment

The included `Jenkinsfile` is a simple declarative pipeline that just drives
Docker Compose - it runs on an agent labeled `ubuntu`, which needs Docker
available (and nothing else; there's no host-side Java/Node build step, since
each `Dockerfile` compiles from source itself):

1. **Checkout** - pulls this repository.
2. **Stop Existing Stack** - `docker compose down`.
3. **Build & Deploy** - `docker compose up -d --build --remove-orphans`.
4. **Verify Deployment** - polls the frontend root until it responds, up to
   ~75 seconds, failing the build if it never comes up.

`.env` is set up **once**, by hand, directly in the server's Jenkins workspace
directory - not through a Jenkins credential:

```bash
cd /var/jenkins_home/workspace/<job-name>   # or wherever this job checks out to
cp .env.example .env
nano .env                                    # set real POSTGRES_PASSWORD, JWT_SECRET, etc.
```

Since `.env` is gitignored, `checkout scm` never touches it on later builds -
it just sits there and gets reused by every subsequent deploy.

---

## Cloudflare Tunnel

In production this app is reached at `finance.experimentbuild.com` via a
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
- No SST/tax, EPF/SOCSO/PCB, payroll, or accounting-ledger logic - out of
  scope by design.

---

## What's intentionally NOT built

Per the brief, this stays a small tool, not a platform:

- No separate Customer/Supplier/Worker/Inventory entities - customer name
  is a free-text field on the job, and worker cost is just another entry.
- No quotations, invoicing, payment/deposit tracking, payroll, or multi-user
  permissions.
- No job status or state field, no dashboard, no reports/charts, no profit
  margin percentage - these existed in earlier iterations and were
  deliberately cut down to "one screen, one number that matters."
- Entries support add/delete only (no in-place edit) - fixing a typo means
  removing the entry and re-adding it, which keeps the API surface small.

These are deliberate gaps, ready to be extended if the owner asks for them
later.
