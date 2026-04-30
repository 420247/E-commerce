# 🛍 E-Commerce Platform

**English** | [Deutsch](#-e-commerce-plattform)

---

A full-stack e-commerce application built with **Spring Boot**, **Angular 21**, and **PostgreSQL**.
Features JWT authentication, wishlist management, and AI-powered product search via the **Claude API**.


**Live demo:** [e-commerce-dmxk.onrender.com](https://e-commerce-dmxk.onrender.com):
! DB spins up in ~60s on free tier. AI search requires active API token !

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA |
| Frontend | Angular 21, Angular Material, TypeScript, SCSS |
| Database | PostgreSQL 16, Flyway migrations |
| Auth | JWT — access token 15 min + refresh token 7 days |
| AI Search | Anthropic Claude API (natural language → product filters) |
| DevOps | Docker, Docker Compose, GitHub Actions, Render |
| Code Quality | Checkstyle, ESLint, Prettier |
| Testing | JUnit 5, Mockito, MockMvc, @WebMvcTest |

---

## Features

- 🔐 **Authentication** — register, login, JWT session management
- 🛒 **Product catalog** — browse and filter by category, price, rating
- 🤖 **AI Search** — describe what you want in plain English, Claude extracts filters
- ❤️ **Wishlist** — save and remove products (requires login)
- 🚀 **CI/CD** — automated lint, tests, and deploy on every push to `main`
- 🐳 **Docker** — full local stack with a single command

---

## Project Structure

```
E-commerce/
├── backend/                            # Spring Boot application
│   ├── src/main/java/com/shop/ecommerce/
│   │   ├── config/                     # SecurityConfig — CORS, JWT filter chain
│   │   ├── controller/                 # REST: Auth, Product, Wishlist, AI Search
│   │   ├── dto/                        # Request / response DTOs
│   │   ├── model/                      # JPA entities: User, Product, WishlistItem
│   │   ├── repository/                 # Spring Data JPA repositories
│   │   ├── security/                   # JwtService, JwtAuthenticationFilter
│   │   └── service/                    # Business logic
│   ├── src/main/resources/
│   │   ├── application.yml             # App configuration
│   │   └── db/migration/               # Flyway SQL migrations (V1–V5)
│   ├── checkstyle.xml                  # Java code style rules
│   └── Dockerfile                      # Multi-stage: JDK builder → JRE runtime
│
├── frontend/                           # Angular 21 application
│   ├── src/app/
│   │   ├── core/
│   │   │   ├── guards/                 # authGuard — protects wishlist route
│   │   │   ├── interceptors/           # jwtInterceptor — attaches Bearer token
│   │   │   └── services/              # AuthService, ProductService, WishlistService
│   │   ├── features/
│   │   │   ├── auth/                   # Login and register pages
│   │   │   ├── products/              # Product list and detail pages
│   │   │   └── wishlist/              # Wishlist page
│   │   └── shared/
│   │       ├── components/navbar/      # Navigation bar with wishlist badge
│   │       └── models/                # TypeScript interfaces
│   ├── nginx.conf                      # Serves SPA + proxies /api to backend
│   ├── .npmrc                          # pnpm hoisting config
│   └── Dockerfile                      # Node builder → nginx runtime
│
├── .github/workflows/
│   ├── ci.yml                          # Auto CI on every PR (lint, test, build)
│   └── deploy.yml                      # Manual deploy trigger (production / staging)
│
├── docker-compose.yml                  # Full local stack (postgres + backend + frontend)
├── .env.example                        # Environment variable template
└── README.md
```

---

## Quick Start

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/)

### Run with Docker

```bash
# 1. Clone the repository
git clone https://github.com/420247/E-commerce.git
cd E-commerce

# 2. Create your .env file
cp .env.example .env
# Fill in: POSTGRES_USER, POSTGRES_PASSWORD, ANTHROPIC_API_KEY, JWT_SECRET

# 3. Start all services
docker compose up --build

# Frontend → http://localhost
# Backend  → http://localhost:8080/api
```

### Run backend locally

```bash
cd backend

export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
export SPRING_DATASOURCE_USERNAME=your_user
export SPRING_DATASOURCE_PASSWORD=your_password
export APPLICATION_SECURITY_JWT_SECRET_KEY=your_jwt_secret
export APPLICATION_ANTHROPIC_API_KEY=your_anthropic_key

./mvnw spring-boot:run
```

### Run frontend locally

```bash
cd frontend
pnpm install
pnpm start
# Open http://localhost:4200
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values.

| Variable | Description |
|----------|-------------|
| `POSTGRES_DB` | Database name (e.g. `ecommerce`) |
| `POSTGRES_USER` | PostgreSQL username |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `ANTHROPIC_API_KEY` | Claude API key from [console.anthropic.com](https://console.anthropic.com) |
| `JWT_SECRET` | Base64-encoded HMAC-SHA256 key (min 256 bits) |

Generate a secure JWT secret:
```bash
openssl rand -hex 32
```

---

## API Endpoints

### Auth
```
POST /api/auth/register   — create account, returns JWT tokens
POST /api/auth/login      — authenticate, returns JWT tokens
```

### Products
```
GET  /api/products                           — list all products
GET  /api/products?category=electronics      — filter by category
GET  /api/products?minPrice=10&maxPrice=500  — filter by price range
GET  /api/products?minRating=4.0             — filter by rating
GET  /api/products/{id}                      — get single product
```

### Wishlist (requires JWT)
```
GET    /api/wishlist        — get current user's wishlist
POST   /api/wishlist/{id}   — add product to wishlist
DELETE /api/wishlist/{id}   — remove product from wishlist
```

### AI Search
```
POST /api/ai/search
Body: { "query": "cheap phone with good camera under 500 euros" }
```

---

## Database Migrations

Flyway runs automatically on startup.

| File | Description |
|------|-------------|
| `V1__create_users.sql` | Users table with email, BCrypt password hash, name |
| `V2__create_products.sql` | Products with name, price, category, rating, stock |
| `V3__create_wishlist.sql` | Wishlist items joining users and products |
| `V4__add_role_to_users.sql` | Adds `role` column (`USER` / `ADMIN`) |
| `V5__insert_sample_products.sql` | 10 sample products across 5 categories |

---

## Testing

```bash
cd backend
./mvnw test
```

| Class | What is tested |
|-------|---------------|
| `AuthServiceTest` | Registration, duplicate email, password hashing, role assignment, login |
| `ProductServiceTest` | Filtering, empty results, product not found |
| `WishlistServiceTest` | Get list, add, duplicate prevention, product not found, remove |
| `JwtServiceTest` | Token generation, email extraction, validation, expiry, access ≠ refresh |
| `AuthControllerTest` | HTTP 200 / 400, `@Valid` field validation via MockMvc |

---

## CI/CD Pipeline

```
Pull Request opened →
  ├── Backend Checkstyle   (code style enforcement)
  ├── Backend Tests        (unit + integration against real PostgreSQL)
  ├── Frontend ESLint      (TypeScript + Angular rules)
  └── Frontend Build       (TypeScript compile + Angular production bundle)

All green → merge allowed

Manual deploy trigger (GitHub Actions → Deploy → Run workflow):
  ┌────────────────────────┐
  │ Environment: production│
  │ Branch:      main      │
  │ Reason:      ...       │
  └────────────────────────┘
  → Triggers Render deploy hooks for backend and frontend
```

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `JWT_SECRET` | Same key used by the backend |
| `RENDER_BACKEND_DEPLOY_HOOK` | Render → E-commerce-1 → Settings → Deploy Hook |
| `RENDER_FRONTEND_DEPLOY_HOOK` | Render → Frontend → Settings → Deploy Hook |

---

## Security

- Passwords hashed with **BCrypt** — never stored as plain text
- JWT signed with **HMAC-SHA256**
- Access tokens expire in **15 minutes**, refresh tokens in **7 days**
- CORS restricted to known frontend origins only
- Docker containers run as **non-root** users
- Secrets loaded from environment variables — never hardcoded
- `@JsonIgnore` on password field — hash never exposed in API responses

---

## License

MIT

---
---

# 🛍 E-Commerce Plattform

[English](#-e-commerce-platform) | **Deutsch**

---

Eine vollständige E-Commerce-Anwendung gebaut mit **Spring Boot**, **Angular 21** und **PostgreSQL**.
Mit JWT-Authentifizierung, Wunschlisten-Verwaltung und KI-gestützter Produktsuche über die **Claude API**.

**Live-Demo:** [e-commerce-dmxk.onrender.com](https://e-commerce-dmxk.onrender.com)

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-bereit-2496ED?logo=docker&logoColor=white)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)
![Lizenz](https://img.shields.io/badge/Lizenz-MIT-green)

---

## Technologie-Stack

| Schicht | Technologie |
|---------|------------|
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA |
| Frontend | Angular 21, Angular Material, TypeScript, SCSS |
| Datenbank | PostgreSQL 16, Flyway-Migrationen |
| Authentifizierung | JWT — Access Token 15 Min. + Refresh Token 7 Tage |
| KI-Suche | Anthropic Claude API (natürliche Sprache → Produktfilter) |
| DevOps | Docker, Docker Compose, GitHub Actions, Render |
| Codequalität | Checkstyle, ESLint, Prettier |
| Tests | JUnit 5, Mockito, MockMvc, @WebMvcTest |

---

## Funktionen

- 🔐 **Authentifizierung** — Registrierung, Login, JWT-Sitzungsverwaltung
- 🛒 **Produktkatalog** — Durchsuchen und filtern nach Kategorie, Preis, Bewertung
- 🤖 **KI-Suche** — Produkt in eigenen Worten beschreiben, Claude extrahiert Filter
- ❤️ **Wunschliste** — Produkte speichern und entfernen (Login erforderlich)
- 🚀 **CI/CD** — Automatische Prüfung, Tests und Deployment bei jedem Push auf `main`
- 🐳 **Docker** — Vollständiger lokaler Stack mit einem einzigen Befehl

---

## Projektstruktur

```
E-commerce/
├── backend/                            # Spring Boot Anwendung
│   ├── src/main/java/com/shop/ecommerce/
│   │   ├── config/                     # SecurityConfig — CORS, JWT-Filterkette
│   │   ├── controller/                 # REST: Auth, Produkt, Wunschliste, KI-Suche
│   │   ├── dto/                        # Request- / Response-DTOs
│   │   ├── model/                      # JPA-Entitäten: User, Product, WishlistItem
│   │   ├── repository/                 # Spring Data JPA Repositories
│   │   ├── security/                   # JwtService, JwtAuthenticationFilter
│   │   └── service/                    # Geschäftslogik
│   ├── src/main/resources/
│   │   ├── application.yml             # Anwendungskonfiguration
│   │   └── db/migration/               # Flyway SQL-Migrationen (V1–V5)
│   ├── checkstyle.xml                  # Java-Codestil-Regeln
│   └── Dockerfile                      # Multi-Stage: JDK Builder → JRE Runtime
│
├── frontend/                           # Angular 21 Anwendung
│   ├── src/app/
│   │   ├── core/
│   │   │   ├── guards/                 # authGuard — schützt Wunschlisten-Route
│   │   │   ├── interceptors/           # jwtInterceptor — hängt Bearer-Token an
│   │   │   └── services/              # AuthService, ProductService, WishlistService
│   │   ├── features/
│   │   │   ├── auth/                   # Login- und Registrierungsseiten
│   │   │   ├── products/              # Produktliste und Detailseite
│   │   │   └── wishlist/              # Wunschliste
│   │   └── shared/
│   │       ├── components/navbar/      # Navigationsleiste mit Wunschlisten-Badge
│   │       └── models/                # TypeScript-Interfaces
│   ├── nginx.conf                      # SPA-Auslieferung + /api-Proxy zum Backend
│   ├── .npmrc                          # pnpm Hoisting-Konfiguration
│   └── Dockerfile                      # Node Builder → nginx Runtime
│
├── .github/workflows/
│   ├── ci.yml                          # Automatisches CI bei jedem PR
│   └── deploy.yml                      # Manueller Deploy-Auslöser
│
├── docker-compose.yml                  # Lokaler Stack (postgres + backend + frontend)
├── .env.example                        # Vorlage für Umgebungsvariablen
└── README.md
```

---

## Schnellstart

### Voraussetzungen

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/)

### Mit Docker starten

```bash
# 1. Repository klonen
git clone https://github.com/420247/E-commerce.git
cd E-commerce

# 2. .env-Datei erstellen
cp .env.example .env
# Werte eintragen: POSTGRES_USER, POSTGRES_PASSWORD, ANTHROPIC_API_KEY, JWT_SECRET

# 3. Alle Services starten
docker compose up --build

# Frontend → http://localhost
# Backend  → http://localhost:8080/api
```

### Backend lokal starten

```bash
cd backend

export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ecommerce
export SPRING_DATASOURCE_USERNAME=dein_benutzer
export SPRING_DATASOURCE_PASSWORD=dein_passwort
export APPLICATION_SECURITY_JWT_SECRET_KEY=dein_jwt_secret
export APPLICATION_ANTHROPIC_API_KEY=dein_anthropic_key

./mvnw spring-boot:run
```

### Frontend lokal starten

```bash
cd frontend
pnpm install
pnpm start
# Öffne http://localhost:4200
```

---

## Umgebungsvariablen

Kopiere `.env.example` nach `.env` und trage deine Werte ein.

| Variable | Beschreibung |
|----------|-------------|
| `POSTGRES_DB` | Datenbankname (z. B. `ecommerce`) |
| `POSTGRES_USER` | PostgreSQL-Benutzername |
| `POSTGRES_PASSWORD` | PostgreSQL-Passwort |
| `ANTHROPIC_API_KEY` | Claude API-Schlüssel von [console.anthropic.com](https://console.anthropic.com) |
| `JWT_SECRET` | Base64-kodierter HMAC-SHA256-Schlüssel (min. 256 Bit) |

Sicheren JWT-Schlüssel generieren:
```bash
openssl rand -hex 32
```

---

## API-Endpunkte

### Authentifizierung
```
POST /api/auth/register   — Konto erstellen, gibt JWT-Tokens zurück
POST /api/auth/login      — Anmelden, gibt JWT-Tokens zurück
```

### Produkte
```
GET  /api/products                           — alle Produkte abrufen
GET  /api/products?category=electronics      — nach Kategorie filtern
GET  /api/products?minPrice=10&maxPrice=500  — nach Preisbereich filtern
GET  /api/products?minRating=4.0             — nach Bewertung filtern
GET  /api/products/{id}                      — einzelnes Produkt abrufen
```

### Wunschliste (JWT erforderlich)
```
GET    /api/wishlist        — Wunschliste des aktuellen Benutzers abrufen
POST   /api/wishlist/{id}   — Produkt zur Wunschliste hinzufügen
DELETE /api/wishlist/{id}   — Produkt aus der Wunschliste entfernen
```

### KI-Suche
```
POST /api/ai/search
Body: { "query": "günstiges Handy mit guter Kamera unter 500 Euro" }
```

---

## Datenbank-Migrationen

Flyway führt Migrationen automatisch beim Start aus.

| Datei | Beschreibung |
|-------|-------------|
| `V1__create_users.sql` | Benutzertabelle mit E-Mail, BCrypt-Hash, Name |
| `V2__create_products.sql` | Produkte mit Name, Preis, Kategorie, Bewertung, Lagerbestand |
| `V3__create_wishlist.sql` | Wunschlisten-Einträge als Verbindung zwischen Benutzern und Produkten |
| `V4__add_role_to_users.sql` | Fügt `role`-Spalte hinzu (`USER` / `ADMIN`) |
| `V5__insert_sample_products.sql` | 10 Beispielprodukte in 5 Kategorien |

---

## Tests

```bash
cd backend
./mvnw test
```

| Klasse | Was getestet wird |
|--------|-------------------|
| `AuthServiceTest` | Registrierung, doppelte E-Mail, Passwort-Hashing, Rollenzuweisung, Login |
| `ProductServiceTest` | Filter, leere Ergebnisse, Produkt nicht gefunden |
| `WishlistServiceTest` | Liste abrufen, hinzufügen, Duplikate verhindern, entfernen |
| `JwtServiceTest` | Token-Generierung, E-Mail-Extraktion, Validierung, Ablauf |
| `AuthControllerTest` | HTTP 200 / 400, `@Valid` Feldvalidierung via MockMvc |

---

## CI/CD-Pipeline

```
Pull Request erstellt →
  ├── Backend Checkstyle    (Codestil-Prüfung)
  ├── Backend Tests         (Unit- + Integrationstests mit echtem PostgreSQL)
  ├── Frontend ESLint       (TypeScript + Angular Regeln)
  └── Frontend Build        (TypeScript-Kompilierung + Angular-Bundle)

Alle grün → Merge erlaubt

Manueller Deploy-Auslöser (GitHub Actions → Deploy → Run workflow):
  ┌──────────────────────────────┐
  │ Umgebung:  production        │
  │ Branch:    main              │
  │ Grund:     ...               │
  └──────────────────────────────┘
  → Löst Render-Deploy-Hooks für Backend und Frontend aus
```

### Erforderliche GitHub-Secrets

| Secret | Beschreibung |
|--------|-------------|
| `JWT_SECRET` | Gleicher Schlüssel wie im Backend |
| `RENDER_BACKEND_DEPLOY_HOOK` | Render → E-commerce-1 → Settings → Deploy Hook |
| `RENDER_FRONTEND_DEPLOY_HOOK` | Render → Frontend → Settings → Deploy Hook |

---

## Sicherheit

- Passwörter mit **BCrypt** gehasht — niemals im Klartext gespeichert
- JWT signiert mit **HMAC-SHA256**
- Access-Token laufen nach **15 Minuten** ab, Refresh-Token nach **7 Tagen**
- CORS nur für bekannte Frontend-Origins erlaubt
- Docker-Container laufen als **Non-Root-Benutzer**
- Secrets aus Umgebungsvariablen — niemals im Code hinterlegt
- `@JsonIgnore` auf Passwortfeld — Hash wird nie in API-Antworten ausgegeben

---

## Lizenz

MIT
