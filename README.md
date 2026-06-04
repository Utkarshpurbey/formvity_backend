# Formvity API

Spring Boot 4 + PostgreSQL + JWT. Maven project (`pom.xml`, `mvnw`).

## Local run

```bash
./mvnw spring-boot:run
```

API: `http://localhost:8081/api/v1`

## Deploy to Render (Docker)

The repo uses **Maven**, not Gradle. The `Dockerfile` builds with `./mvnw package`.

### 1. Create PostgreSQL on Render

Dashboard → **New** → **PostgreSQL**. Note the database name.

### 2. Create Web Service

- **New** → **Web Service** → connect this repo  
- **Runtime:** Docker  
- **Dockerfile path:** `./Dockerfile`  
- **Root directory:** (leave empty = repo root)

### 3. Environment variables

| Key | Value |
|-----|--------|
| `PORT` | `8081` (Render may override; app uses `${PORT}`) |
| `JWT_SECRET` | Long random string (32+ characters) |
| `SPRING_DATASOURCE_URL` | **Internal** JDBC URL, e.g. `jdbc:postgresql://dpg-xxx-a/formvity` |
| `SPRING_DATASOURCE_USERNAME` | From Render DB dashboard |
| `SPRING_DATASOURCE_PASSWORD` | From Render DB dashboard |

Render’s `DATABASE_URL` is often `postgres://...` — Spring JDBC needs `jdbc:postgresql://...`. Use the **Internal Database URL** from the Postgres service and prefix with `jdbc:` if needed.

Example:

```text
jdbc:postgresql://dpg-xxxxx-a.oregon-postgres.render.com/formvity
```

### 4. CORS (frontend)

Set allowed origins in `SecurityConfig.java` or add env-based CORS for your Vercel/Render frontend URL.

### Optional: Blueprint

```bash
# render.yaml in repo root — deploy stack via Blueprint
```

## API routes (context path `/api/v1`)

- `POST /auth/login` — returns `{ token, id, displayName }`
- `GET /workspaces` — workspace list (JWT required)
- `GET /workspaces/{id}/forms` — forms in workspace
- `GET /public/forms/{slug}` — public form (no auth)

## Frontend

See [frontend/README.md](frontend/README.md).
