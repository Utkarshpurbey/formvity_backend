const API_BASE =
  import.meta.env.VITE_API_BASE ?? "http://localhost:8081/api/v1";

export type ApiResponse<T> = {
  status: number;
  data: T;
  message: string;
};

function getToken(): string | null {
  return localStorage.getItem("formvity_token");
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem("formvity_token", token);
  else localStorage.removeItem("formvity_token");
}

export async function api<T>(
  path: string,
  options: RequestInit & { auth?: boolean } = {}
): Promise<T> {
  const { auth = true, headers, ...rest } = options;
  const h = new Headers(headers);
  h.set("Content-Type", "application/json");
  if (auth) {
    const token = getToken();
    if (token) h.set("Authorization", `Bearer ${token}`);
  }
  const res = await fetch(`${API_BASE}${path}`, { ...rest, headers: h });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(
      (err as { errorMessage?: string }).errorMessage ?? res.statusText
    );
  }
  if (res.status === 204) return undefined as T;
  const json = await res.json();
  if (
    json &&
    typeof json === "object" &&
    "data" in json &&
    (json as ApiResponse<T>).data !== undefined
  ) {
    return (json as ApiResponse<T>).data;
  }
  return json as T;
}

export type LoginResponse = {
  token: string;
  id: string;
  displayName: string;
};

export type WorkspaceCard = {
  workspaceId: string;
  workspaceName: string;
  formCount: number;
};

export type FormSummary = {
  id: string;
  workspaceId: string;
  title: string;
  status: string;
  updatedAt: string;
};

export type WorkspaceDashboard = {
  workspaceId: string;
  workspaceName: string;
  formCount: number;
  recentForms: FormSummary[];
};
