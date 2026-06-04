import { useEffect, useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { api, WorkspaceCard } from "../api/client";
import { useAuth } from "../context/AuthContext";

export function AppShell() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [workspaces, setWorkspaces] = useState<WorkspaceCard[]>([]);

  const workspaceMatch = location.pathname.match(
    /\/app\/workspaces\/([^/]+)/
  );
  const activeWorkspaceId = workspaceMatch?.[1];

  useEffect(() => {
    api<WorkspaceCard[]>("/workspaces")
      .then(setWorkspaces)
      .catch(console.error);
  }, [location.pathname]);

  return (
    <div className="flex min-h-screen">
      {/* Workspace rail — teams only, never forms */}
      <aside className="w-64 shrink-0 border-r border-[var(--color-border)] bg-white flex flex-col">
        <div className="px-4 py-5 border-b border-[var(--color-border)]">
          <Link to="/app/workspaces" className="flex items-center gap-2">
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-[var(--color-brand)] text-white font-bold text-lg">
              F
            </span>
            <span className="text-lg font-medium text-[#202124]">Formvity</span>
          </Link>
        </div>

        <div className="px-3 py-3">
          <p className="px-2 text-xs font-medium uppercase tracking-wide text-[#5f6368] mb-2">
            Workspaces
          </p>
          <nav className="space-y-0.5">
            {workspaces.map((ws) => (
              <Link
                key={ws.workspaceId}
                to={`/app/workspaces/${ws.workspaceId}`}
                className={`block rounded-lg px-3 py-2 text-sm transition-colors ${
                  activeWorkspaceId === ws.workspaceId
                    ? "bg-[#e8f0fe] text-[var(--color-brand)] font-medium"
                    : "text-[#3c4043] hover:bg-[#f1f3f4]"
                }`}
              >
                <span className="truncate block">{ws.workspaceName}</span>
                <span className="text-xs text-[#5f6368]">
                  {ws.formCount} form{ws.formCount === 1 ? "" : "s"}
                </span>
              </Link>
            ))}
          </nav>
          <button
            type="button"
            onClick={() => navigate("/app/workspaces")}
            className="mt-2 w-full rounded-lg border border-dashed border-[var(--color-border)] px-3 py-2 text-sm text-[#5f6368] hover:border-[var(--color-brand)] hover:text-[var(--color-brand)]"
          >
            + New workspace
          </button>
        </div>

        <div className="mt-auto border-t border-[var(--color-border)] p-4">
          <p className="text-sm font-medium truncate">{user?.displayName}</p>
          <button
            type="button"
            onClick={logout}
            className="mt-1 text-sm text-[#5f6368] hover:text-[var(--color-brand)]"
          >
            Sign out
          </button>
        </div>
      </aside>

      {/* Main content — forms live only inside workspace routes */}
      <main className="flex-1 min-w-0">
        <Outlet />
      </main>
    </div>
  );
}
