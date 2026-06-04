# Formvity UI

SaaS-style frontend: workspaces and forms are separate areas.

## Run

```bash
cd frontend
npm install
npm run dev
```

Opens http://localhost:3000 — API at http://localhost:8081/api/v1

## Routes

| Route | Purpose |
|-------|---------|
| `/login` | Sign in (JWT) |
| `/app/workspaces` | Workspace home (no forms) |
| `/app/workspaces/:id` | Forms list for one workspace |
| `/app/workspaces/:id/forms/:formId` | Form editor |
| `/f/:slug` | Public published form |

## API (backend)

- `GET /workspaces` — workspace cards with form counts
- `GET /workspaces/:id/dashboard` — workspace + forms
- `GET /workspaces/:id/forms` — form list
- `POST /workspaces/:id/forms/:formId/publish` — publish

Send `Authorization: Bearer <token>` on protected requests.
