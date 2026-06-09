# RBAC Guide — Workspace & Form Authorization (Formvity)

This document describes how to enable **Role-Based Access Control (RBAC)** for workspace-scoped operations and form-related activity in Formvity.

---

## 1. Current State

### What exists today

| Piece | Status |
|-------|--------|
| JWT authentication (`ROLE_USER` only) | ✅ Proves *who* the user is |
| `FormRoles` on `workspace_members` | ✅ Stored: `ADMIN`, `EDITOR`, `VIEWER`, `ATTENDEE` |
| `WorkspaceAccessService` | ⚠️ Membership check only — no role enforcement |
| Workspace creator | ✅ Gets `ADMIN` on create |
| Endpoint protection | ❌ Inconsistent across services |

### Roles enum

```java
public enum FormRoles {
    ADMIN,
    EDITOR,
    VIEWER,
    ATTENDEE
}
```

Stored on `WorkspaceMemberEntity.role` in table `workspace_members`.

### Current enforcement

**Protected:**
- `FormServiceImpl.getUserAllforms` — checks active membership
- `WorkSpaceServiceImpl.getWorkspaceDashboard` — uses `WorkspaceAccessService.requireUserExistInWorkSpace`

**Not protected (gaps):**
- `FormServiceImpl`: `createForm`, `getFormDef`, `patchForm`, `replaceForm`, `deleteForm`
- `FormPublicationServiceImpl`: `publishForm`, `unPublishForm` (uses `findById(formId)` only — ignores `workspaceId`)
- `WorkSpaceServiceImpl`: `getWorkSpaceMetaData`, `deleteWorkspace`, `getMembersList`

**Risk:** Any authenticated user who knows a `workspaceId` or `formId` can mutate data today.

---

## 2. RBAC Model

### Two layers

1. **Authentication (JWT)** — global: “Is this a valid logged-in user?”
2. **Authorization (workspace RBAC)** — per request: “Can this user perform action X in workspace Y?”

Keep workspace roles **in the database**, not in the JWT. Roles can change without forcing re-login.

### Role definitions

| Role | Workspace permissions | Form permissions |
|------|----------------------|------------------|
| **ADMIN** | Delete workspace, manage members, change roles | Full CRUD + publish/unpublish + hard delete |
| **EDITOR** | View dashboard, list members | Create/edit/delete (soft) forms, publish/unpublish |
| **VIEWER** | View workspace + dashboard | List forms + read drafts (no edits) |
| **ATTENDEE** | No workspace UI access (optional) | Future: submit form responses only |

### Role hierarchy

Higher role includes all lower permissions:

```
ADMIN ≥ EDITOR ≥ VIEWER ≥ ATTENDEE
```

Use an **explicit rank map** (do not rely on enum `ordinal()` without careful ordering):

```java
private static final Map<FormRoles, Integer> RANK = Map.of(
    FormRoles.ADMIN, 4,
    FormRoles.EDITOR, 3,
    FormRoles.VIEWER, 2,
    FormRoles.ATTENDEE, 1
);

private boolean hasAtLeast(FormRoles actual, FormRoles required) {
    return RANK.get(actual) >= RANK.get(required);
}
```

---

## 3. Permission Matrix

### Workspace APIs (`/workspaces`)

| Method | Path | Minimum role |
|--------|------|--------------|
| GET | `/workspaces` | Authenticated (own memberships only) |
| POST | `/workspaces` | Authenticated |
| GET | `/workspaces/{id}` | VIEWER |
| GET | `/workspaces/{id}/dashboard` | VIEWER |
| GET | `/workspaces/{id}/members` | VIEWER |
| DELETE | `/workspaces/{id}` | ADMIN |
| POST | `/workspaces/{id}/members` *(future)* | ADMIN |
| PATCH | `/workspaces/{id}/members/{userId}` *(future)* | ADMIN |
| DELETE | `/workspaces/{id}/members/{userId}` *(future)* | ADMIN |

### Form APIs (`/workspaces/{workspaceId}/forms`)

| Method | Path | Minimum role |
|--------|------|--------------|
| GET | `.../forms` | VIEWER |
| GET | `.../forms/{formId}` | VIEWER |
| POST | `.../forms` | EDITOR |
| PATCH | `.../forms/{formId}` | EDITOR |
| PUT | `.../forms/{formId}` | EDITOR |
| DELETE | `.../forms/{formId}` (soft) | EDITOR |
| DELETE | `.../forms/{formId}?hard=true` | ADMIN |
| POST | `.../forms/{formId}/publish` | EDITOR |
| POST | `.../forms/{formId}/unpublish` | EDITOR |

### Public APIs

| Method | Path | Auth |
|--------|------|------|
| GET | `/public/forms/{slug}` | None (`permitAll`) |

---

## 4. Architecture

Use **service-layer authorization** via an expanded `WorkspaceAccessService`. This matches the existing `FormvityException.forbidden` pattern.

```
Controller → Service → WorkspaceAccessService → WorkspaceMemberRepository
                              ↓
                    FormvityException (403)
```

Do **not** put workspace roles in the JWT unless you need Spring `@PreAuthorize` on every endpoint.

### Key files

| File | Purpose |
|------|---------|
| `WorkspaceMemberRepository` | Load membership + role |
| `WorkspaceAccessService` | Central RBAC checks |
| `WorkSpaceServiceImpl` | Workspace operations |
| `FormServiceImpl` | Form CRUD |
| `FormPublicationServiceImpl` | Publish/unpublish |
| `WorkspaceController` | Workspace HTTP layer |
| `WorkspaceFormsController` | Form HTTP layer |

---

## 5. Implementation Steps

### Step 1 — Extend repository

Add to `WorkspaceMemberRepository`:

```java
Optional<WorkspaceMemberEntity> findByWorkspace_WorkSpaceIdAndUserIdAndActiveTrue(
    UUID workspaceId, UUID userId);
```

### Step 2 — Expand `WorkspaceAccessService`

```java
@Component
@AllArgsConstructor
public class WorkspaceAccessService {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceMemberEntity requireActiveMember(UUID userId, UUID workspaceId) {
        return workspaceMemberRepository
            .findByWorkspace_WorkSpaceIdAndUserIdAndActiveTrue(workspaceId, userId)
            .orElseThrow(() -> FormvityException.forbidden(
                "You are not an active member of workspace " + workspaceId));
    }

    public void requireRole(UUID userId, UUID workspaceId, FormRoles minimumRole) {
        WorkspaceMemberEntity member = requireActiveMember(userId, workspaceId);
        if (!hasAtLeast(member.getRole(), minimumRole)) {
            throw FormvityException.forbidden(
                "Requires " + minimumRole + " role in this workspace");
        }
    }

    // hasAtLeast() using explicit RANK map — see Section 2
}
```

Replace existing `requireUserExistInWorkSpace` calls with `requireRole` or `requireActiveMember` as appropriate.

### Step 3 — Optional: permission enum

```java
public enum WorkspacePermission {
    VIEW_WORKSPACE(FormRoles.VIEWER),
    MANAGE_FORMS(FormRoles.EDITOR),
    MANAGE_WORKSPACE(FormRoles.ADMIN);

    private final FormRoles minimumRole;
}
```

### Step 4 — Wire `WorkSpaceServiceImpl`

| Method | Required check |
|--------|----------------|
| `getWorkspaceDashboard` | `requireRole(userId, workspaceId, VIEWER)` |
| `getWorkSpaceMetaData` | Add `userId` param + `requireRole(..., VIEWER)` |
| `getMembersList` | Add `userId` + `requireRole(..., VIEWER)` |
| `deleteWorkspace` | Add `userId` + `requireRole(..., ADMIN)` |

Update `WorkspaceController` to pass `Utils.getLoggedInUserId()` into methods that lack it.

### Step 5 — Wire `FormServiceImpl`

Every method must accept `userId` and check before work:

| Method | Minimum role |
|--------|--------------|
| `getUserAllforms` | VIEWER |
| `getFormDef` | VIEWER |
| `createForm` | EDITOR |
| `patchForm` / `replaceForm` | EDITOR |
| `deleteForm` (soft) | EDITOR |
| `deleteForm` (hard) | ADMIN |

Update `FormService` interface and `WorkspaceFormsController` to pass `userId` on **every** endpoint.

### Step 6 — Fix `FormPublicationServiceImpl` (critical)

**Before (insecure):**
```java
formRepository.findById(formId)
```

**After:**
1. `requireRole(userId, workspaceId, EDITOR)`
2. `formRepository.findByIdAndWorkspace_WorkSpaceId(formId, workspaceId)`
3. Apply same pattern to `unPublishForm`

### Step 7 — Member management APIs (new)

```
POST   /workspaces/{id}/members           — add member (ADMIN)
PATCH  /workspaces/{id}/members/{userId}  — change role (ADMIN)
DELETE /workspaces/{id}/members/{userId}  — remove member (ADMIN)
```

Business rules:
- Cannot remove/demote the last ADMIN
- Cannot change your own role if you are the only ADMIN
- Default new members to `VIEWER` or `EDITOR`

### Step 8 — Optional: Spring Method Security

1. Add `@EnableMethodSecurity` to `SecurityConfig`
2. Create custom `@RequireWorkspaceRole(EDITOR)` annotation + aspect

Service-layer checks are recommended for this codebase today.

---

## 6. Controller Checklist

Every protected handler must:

1. Read `UUID userId = Utils.getLoggedInUserId()`
2. Pass `workspaceId` + `userId` into the service
3. Never trust a client-sent `userId`

| Controller method | Currently passes `userId`? | Action |
|-------------------|------------------------------|--------|
| `listForms` | ✅ | Keep |
| `createForm` | ✅ | Keep |
| `getFormDraft` | ❌ | Add |
| `patchForm` / `replaceForm` / `deleteForm` | ❌ | Add |
| `publishForm` / `unpublishForm` | ✅ | Enforce in service |
| `getWorkspace` | ❌ | Add |
| `listMembers` / `deleteWorkspace` | ❌ | Add |

---

## 7. HTTP Error Semantics

| Status | When |
|--------|------|
| **401** | Missing or invalid JWT |
| **403** | Authenticated but not a member, or insufficient role |
| **404** | Workspace/form not found |

**Recommendation:** Return **403** for non-members rather than **404**, to avoid leaking whether a workspace ID exists.

---

## 8. Testing Matrix

| User role | Action | Expected |
|-----------|--------|----------|
| Non-member | GET forms | 403 |
| VIEWER | GET forms | 200 |
| VIEWER | POST form | 403 |
| EDITOR | PATCH form | 200 |
| EDITOR | DELETE hard | 403 |
| ADMIN | DELETE workspace | 200 |
| EDITOR | Publish form | 200 |
| Wrong `workspaceId` in URL | Publish | 403 or 404 |

Use `@SpringBootTest` with seeded `workspace_members` rows, or integration tests with Testcontainers.

---

## 9. Frontend Impact

1. On **403**, show “You don’t have permission” (not a generic error).
2. Expose current user’s role via `GET /workspaces/{id}/me` or include in dashboard response.
3. Hide/disable UI by role:
   - VIEWER: no Create / Edit / Publish / Delete
   - EDITOR: no workspace delete / member management
   - ADMIN: full access

---

## 10. Implementation Order

1. Add explicit `FormRoles` rank map
2. Extend `WorkspaceMemberRepository` + `WorkspaceAccessService`
3. Lock down form mutations + publish (highest risk)
4. Lock down workspace delete + members list
5. Add member invite/role APIs
6. Add integration tests + frontend role hints

---

## 11. Definition of Done

RBAC is fully enabled when:

- [ ] Every `workspaceId` operation verifies **active membership**
- [ ] Every mutating operation verifies **minimum role**
- [ ] Publish/unpublish validates **form belongs to workspace** from URL path
- [ ] Workspace delete requires **ADMIN**
- [ ] No endpoint uses `findById(formId)` without workspace scope
- [ ] All controllers pass `userId` from JWT into services
- [ ] Integration tests cover the permission matrix above

---

## 12. Related Code References

- `src/main/java/com/example/uttuCodes/formvity/enums/FormRoles.java`
- `src/main/java/com/example/uttuCodes/formvity/entity/WorkspaceMemberEntity.java`
- `src/main/java/com/example/uttuCodes/formvity/utils/WorkspaceAccessService.java`
- `src/main/java/com/example/uttuCodes/formvity/service/impl/WorkSpaceServiceImpl.java`
- `src/main/java/com/example/uttuCodes/formvity/service/impl/FormServiceImpl.java`
- `src/main/java/com/example/uttuCodes/formvity/service/impl/FormPublicationServiceImpl.java`
- `src/main/java/com/example/uttuCodes/formvity/web/WorkspaceController.java`
- `src/main/java/com/example/uttuCodes/formvity/web/WorkspaceFormsController.java`
- `src/main/java/com/example/uttuCodes/formvity/config/SecurityConfig.java`
