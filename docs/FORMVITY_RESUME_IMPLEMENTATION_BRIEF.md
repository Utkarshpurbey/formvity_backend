# Formvity — Implementation Brief for Resume Writing

**Audience:** Another AI agent or writer turning this project into resume bullets, LinkedIn project descriptions, or interview talking points.

**Instruction to the agent:** Use only claims supported below. Prefer outcome-oriented bullets (built, deployed, designed, optimized). Quantify where numbers are given. Do not invent user counts, revenue, or team size. The author appears to be solo or primary builder on a full-stack product shipped to production.

---

## 1. Project summary (elevator pitch)

**Formvity** is a production SaaS-style **form builder**: users create workspaces, design multi-field forms in a visual builder, publish shareable public URLs, collect anonymous or identified responses, and view analytics dashboards. It is a **monorepo** with a **Spring Boot REST API** and a **Next.js** web app, backed by **PostgreSQL**, deployed with **Docker on Render**, with the database hosted on **Supabase** (connection via Supabase pooler for IPv4-compatible cloud deploys).

**Public-facing product:** [https://www.formvity.in](https://www.formvity.in) (and apex `formvity.in`).

**Backend API (example deployment):** `https://formvity-backend.onrender.com/api/v1`

---

## 2. Repository layout

| Path | Role |
|------|------|
| `formvity/` | Backend API — Java 21, Spring Boot 4, Maven (~105 Java source files) |
| `Form-Builder-UI/` | Frontend — Next.js 14, React 18, TypeScript, Redux Toolkit, Tailwind CSS v4 |

Context path for all API routes: **`/api/v1`**.

---

## 3. Technology stack (ATS keywords)

### Backend
- Java 21, Spring Boot 4, Spring Web MVC, Spring Data JPA, Spring Security
- PostgreSQL, Hibernate, JSONB columns for form schemas and submission payloads
- JWT (JJWT) stateless authentication
- HikariCP connection pooling (custom-tuned for remote DB)
- ModelMapper, Lombok, Jakarta Validation
- NanoId (`jnanoid`) for URL-safe public form IDs
- Maven, Docker multi-stage build (`eclipse-temurin:21`)
- SLF4J / Logback

### Frontend
- Next.js 14 App Router, React 18, TypeScript
- Redux Toolkit (`auth`, `workspace`, `forms`, `analytics` slices)
- React Hook Form + Zod validation
- Tailwind CSS v4
- Environment profiles for local vs production API (`scripts/next-dev.mjs`)
- Optional Vercel Analytics / Speed Insights dependencies

### Infrastructure & ops
- Render (web service + Docker)
- Supabase PostgreSQL (region `ap-south-1`, **transaction pooler** on port 6543 for Render compatibility)
- CORS configured via env (`CORS_ALLOWED_ORIGINS`) for multiple production origins
- Custom `DatabaseUrlParser` + `DatabaseConfig` for Render-style `postgres://` URLs and JDBC

---

## 4. Domain model & data design

Core entities (JPA):

| Entity | Purpose |
|--------|---------|
| `UserEntity` | Accounts; email (unique, normalized lowercase), bcrypt password, display name |
| `WorkSpacesEntity` | Multi-tenant workspace container; soft-delete via `active` flag |
| `WorkspaceMemberEntity` | User ↔ workspace membership with `FormRoles` |
| `WorkspaceInvitationEntity` | Email invite tokens for new users (expiry, status workflow) |
| `FormEntity` | Form draft metadata + **`draftPageDef` (JSONB)**; status (`DRAFT`, `PUBLISHED`, `UNPUBLISHED`, `ARCHIVED`) |
| `FormPublicationEntity` | Versioned published snapshots; **`publishedPageDef` (JSONB)**; public slug + nano ID; `is_current` flag |
| `SubmissionEntity` | Responses linked to form + publication version; **`answers` / `respondent` / `metadata` as JSONB** |

**Design choices worth mentioning on a resume:**
- **Schema-less form definitions in JSONB** — builder stores page/field graph in PostgreSQL without rigid per-field tables.
- **Versioned publishing** — each publish creates a new publication row; submissions retain publication version for analytics integrity.
- **Public URLs** — human-readable slug + suffix public ID (e.g. `my-survey-abc123xyz`).
- **Indexes** on hot paths: workspace+form listings, submissions by form+time, publication lookup by `public_id`, workspace membership checks.

---

## 5. Major product features (what the system does)

### Authentication & users
- Register, login (email or display name), logout, `GET /auth/me`
- JWT in `Authorization: Bearer` header; stateless sessions
- Password hashing via Spring Security `PasswordEncoder`

### Workspaces
- Create, list, rename, soft-delete workspace
- Dashboard: workspace metadata + active forms list
- Workspace cards with form counts

### Team & invites
- Invite users by email with role (`ADMIN`, `EDITOR`, `VIEWER`)
- Existing users added directly; new users get tokenized invite link (`/welcome?token=...`)
- Invite preview + activation flow creates user, workspace membership, returns JWT
- Member listing per workspace

### Form builder & lifecycle (API)
- CRUD on forms within a workspace (create, get definition, patch, replace, soft/hard delete)
- Draft stored as JSON page definition
- **Publish / unpublish** with version increment and slug management
- Publish status endpoint

### Public responder (no auth)
- `GET /public/forms/{slug}` — load published definition
- `POST /public/forms/{slug}/submit` — persist submission against current publication

### Analytics (authenticated)
Rich server-side analytics pipeline, exposed as multiple REST resources under `/workspaces/{id}/forms/{formId}/`:
- **Overview** — summary + timeline + per-question breakdown + insights bundle
- **Summary** — totals, today / 7d / 30d counts, first/last response, completion metrics
- **Timeline** — daily submission buckets (SQL aggregation)
- **Questions** — distribution / stats per field type
- **Insights** — audience (unique/returning respondents), traffic/metadata, temporal (peak hour/day), completion rates, publication version mix
- **Submissions** — paginated list with per-row completion rate

Implementation detail: `SubmissionInsightsAnalyzer`, `QuestionAnalyticsBuilder`, `FormAnalyticsSupport` — substantial in-process analytics over submission JSON (not just SQL counts).

### Frontend routes (Form-Builder-UI)
- Marketing / legal: `/`, `/login`, `/register`, `/privacy`, `/terms`, `/license`
- App shell (protected): `/workspaces`, `/workspace`, `/builder`, `/templates`
- Workspace: `/workspaces/[workspaceId]`, settings, per-form editor, **analytics page**
- Public form: `/r/[slug]`
- Invite onboarding: `/welcome`

---

## 6. API surface (representative)

All prefixed with `/api/v1`.

| Area | Methods / paths |
|------|-----------------|
| Health | `GET /health` |
| Auth | `POST /auth/login`, `/register`, `/logout`; `GET /auth/me` |
| Activation | `GET /auth/invite/{token}`, `POST /auth/activate` |
| Workspaces | `GET/POST /workspaces`, `GET/PATCH/DELETE /workspaces/{id}`, `GET .../dashboard` |
| Members | `GET/POST /workspaces/{id}/members` |
| Forms | `GET/POST /workspaces/{id}/forms`, `GET/PATCH/PUT/DELETE .../{formId}`, publish/unpublish |
| Analytics | `GET .../analytics`, `/summary`, `/timeline`, `/questions`, `/insights`, `/submissions` |
| Public | `GET/POST /public/forms/{slug}`, `.../submit` |

---

## 7. Security & authorization (honest scope)

**Implemented:**
- JWT filter on protected routes; public routes for auth registration/login, health, and `/public/**`
- CORS allowlist from environment (supports multiple origins, e.g. `www` and apex domain)
- Workspace **membership** checks via `WorkspaceAccessService` on many sensitive operations
- Global `@RestControllerAdvice` for consistent API error responses
- Secrets via env (`JWT_SECRET`, DB credentials) — not committed

**Partial / documented gaps (do not oversell as “full RBAC”):**
- Roles are **stored** on members (`ADMIN`, `EDITOR`, `VIEWER`) but **fine-grained role enforcement is incomplete** on some endpoints (see internal `docs/RBAC.md`).
- Safe resume phrasing: “Designed role model and membership gates; documented RBAC rollout plan” rather than “Implemented enterprise RBAC everywhere.”

---

## 8. Engineering highlights (good resume material)

1. **Full-stack product ownership** — API + UI + deployment + custom domain (`formvity.in`).
2. **Multi-tenant workspace model** — isolation by workspace ID and membership checks.
3. **Versioned form publishing** — immutable published snapshots for analytics and auditability.
4. **JSONB-centric form engine** — flexible field definitions without schema migrations per field type.
5. **Public share links** — slug + nano ID, unauthenticated submit path.
6. **Analytics subsystem** — multi-endpoint analytics with aggregation, insights (temporal, audience, completion), paginated submission export.
7. **Invite/onboarding flow** — token expiration, activation creates user + membership + session.
8. **Cloud-native DB wiring** — custom parser for `postgres://` vs JDBC; Supabase pooler + SSL; Hikari pool sizing via env vars after migration from Render Postgres.
9. **Dockerized Java 21 build** — multi-stage Maven build optimized for Render memory limits.
10. **Production debugging** — resolved JDBC URL parsing bugs (passwords with `@`), IPv6 direct Supabase vs IPv4 pooler, CORS origin mismatches (`www` vs non-`www`).

---

## 9. Deployment & configuration (factual)

- Backend: Render web service, Docker entrypoint `java -jar app.jar`, port from `PORT` env.
- Database: Supabase PostgreSQL, pooler host pattern `aws-1-ap-south-1.pooler.supabase.com:6543`.
- Typical env vars: `SPRING_DATASOURCE_*`, `JWT_SECRET`, `APP_FRONTEND_URL`, `CORS_ALLOWED_ORIGINS`, optional `DB_POOL_*` tuning.
- Frontend: configured for production API URL via `config/render.env` and dev-local profile.
- Hibernate `ddl-auto=update` — schema driven from entities (no Flyway/Liquibase in repo).

**Latency note (context only):** Backend on Render US-East + DB in Supabase Mumbai adds cross-region latency; mitigations include pooler, connection pool tuning, and indexes — not a “10x faster” claim unless measured.

---

## 10. Suggested resume bullet templates (raw material)

The writing agent should rewrite these in the user’s voice and trim to 1–2 lines each.

**Full-stack / product**
- Built and deployed **Formvity**, a multi-tenant form builder (Next.js + Spring Boot + PostgreSQL), enabling teams to design forms, publish public links, and analyze submissions at **formvity.in**.
- Owned end-to-end delivery: REST API design, React/Redux UI, Docker deployment on Render, and PostgreSQL on Supabase with production CORS and JWT auth.

**Backend**
- Designed REST API (~100+ Java classes) with workspace-scoped resources, JWT security, and JSONB storage for dynamic form schemas and submission payloads.
- Implemented **versioned form publishing** (draft → published snapshots) and public unauthenticated submit endpoints keyed by URL slug.
- Built **form analytics service** with timeline aggregation, per-question breakdowns, and insight metrics (completion, audience, peak traffic times).
- Added **workspace invitation flow** (tokenized email invites, activation, auto-provisioning of members and JWT session).

**Frontend**
- Developed **Next.js 14** app with protected App Router layouts, visual form builder, public responder route (`/r/[slug]`), and analytics dashboards backed by Redux Toolkit.
- Integrated **React Hook Form + Zod** for client validation and typed API contracts against the Spring backend.

**DevOps / reliability**
- Containerized Spring Boot 4 / Java 21 with multi-stage Docker build; configured cloud DB connectivity (Supabase pooler, SSL, HikariCP pool tuning).
- Resolved production connectivity issues (JDBC URL encoding, IPv4/IPv6 database endpoints, multi-origin CORS for custom domain).

**Architecture / data**
- Modeled multi-tenant **workspaces**, role-bearing memberships, and soft-deleted resources; indexed query paths for listings, public lookups, and submission analytics.

---

## 11. Interview talking points (STAR-friendly)

- **Publishing:** Why version publications instead of overwriting — submissions reference publication version for accurate analytics when form changes.
- **JSONB:** Tradeoff — flexibility vs querying; native SQL for timelines, Java-side analysis for nested answer maps.
- **Security:** Stateless JWT vs server sessions; why roles live in DB not token claims.
- **Supabase on Render:** Direct DB host IPv6 vs pooler IPv4; splitting credentials out of URL when password contains `@`.
- **CORS:** Browser `Origin` header vs `Referer`; apex vs `www` as distinct origins.

---

## 12. What to avoid claiming

- No evidence of large-scale load testing or specific QPS/latency SLAs in repo.
- RBAC is **not** fully enforced on every endpoint — see `docs/RBAC.md`.
- No automated E2E test suite called out for frontend; backend uses Spring Boot test starter but coverage level not quantified here.
- Do not list specific database passwords, JWT secrets, or internal credentials.

---

## 13. Key file references (for verification)

| Topic | Path |
|-------|------|
| Security / CORS | `formvity/src/main/java/.../config/SecurityConfig.java` |
| JWT | `formvity/src/main/java/.../security/JwtService.java`, `JwtAuthFilter.java` |
| DataSource / pool | `formvity/src/main/java/.../config/DatabaseConfig.java`, `DatabaseUrlParser.java` |
| Publish flow | `formvity/src/main/java/.../service/impl/FormPublicationServiceImpl.java` |
| Analytics | `formvity/src/main/java/.../service/impl/FormAnalyticsServiceImpl.java`, `SubmissionInsightsAnalyzer.java` |
| Invites | `formvity/src/main/java/.../service/impl/WorkspaceMemberServiceImpl.java`, `UserActivationServiceImpl.java` |
| Entities / indexes | `formvity/src/main/java/.../entity/*.java` |
| Docker | `formvity/Dockerfile` |
| RBAC status | `formvity/docs/RBAC.md` |
| Frontend app routes | `Form-Builder-UI/app/**/page.tsx` |
| Redux | `Form-Builder-UI/src/store/slices/*.ts` |

---

## 14. One-line role descriptions (pick one)

- **Full-Stack Engineer:** Built a production form-builder SaaS from API to UI with JWT auth, JSONB form engine, and analytics.
- **Backend Engineer:** Spring Boot multi-tenant form platform with versioned publishing, public submit API, and analytics aggregation on PostgreSQL JSONB.
- **Founding Engineer / Solo Builder:** End-to-end Formvity — design, implementation, deployment, and custom domain launch.

---

*Document generated for resume/agent use. Update live URLs and deployment details if infrastructure changes.*
