import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { api, LoginResponse, setToken } from "../api/client";

type AuthState = {
  token: string | null;
  user: { id: string; displayName: string } | null;
};

type AuthContextValue = AuthState & {
  login: (userName: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function loadUser(): AuthState["user"] {
  const raw = localStorage.getItem("formvity_user");
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthState["user"];
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() =>
    localStorage.getItem("formvity_token")
  );
  const [user, setUser] = useState<AuthState["user"]>(loadUser);

  const login = useCallback(async (userName: string, password: string) => {
    const res = await api<LoginResponse>("/auth/login", {
      method: "POST",
      auth: false,
      body: JSON.stringify({ userName, password }),
    });
    setToken(res.token);
    setTokenState(res.token);
    const u = { id: res.id, displayName: res.displayName };
    setUser(u);
    localStorage.setItem("formvity_user", JSON.stringify(u));
  }, []);

  const logout = useCallback(() => {
    setToken(null);
    setTokenState(null);
    setUser(null);
    localStorage.removeItem("formvity_user");
  }, []);

  const value = useMemo(
    () => ({
      token,
      user,
      login,
      logout,
      isAuthenticated: !!token,
    }),
    [token, user, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
