import { FormEvent, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/app/workspaces" replace />;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(userName, password);
      navigate("/app/workspaces");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--color-surface)] px-4">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-sm border border-[var(--color-border)] p-8">
        <div className="flex items-center gap-3 mb-8">
          <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-[var(--color-brand)] text-white font-bold text-xl">
            F
          </span>
          <div>
            <h1 className="text-2xl font-medium">Sign in to Formvity</h1>
            <p className="text-sm text-[#5f6368]">Build forms your team will love</p>
          </div>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-[#3c4043] mb-1">
              Email or username
            </label>
            <input
              type="text"
              value={userName}
              onChange={(e) => setUserName(e.target.value)}
              className="w-full rounded-lg border border-[var(--color-border)] px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-[var(--color-brand)]"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-[#3c4043] mb-1">
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-lg border border-[var(--color-border)] px-3 py-2.5 focus:outline-none focus:ring-2 focus:ring-[var(--color-brand)]"
              required
            />
          </div>
          {error && (
            <p className="text-sm text-red-600 bg-red-50 rounded-lg px-3 py-2">
              {error}
            </p>
          )}
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-[var(--color-brand)] text-white py-2.5 font-medium hover:bg-[var(--color-brand-hover)] disabled:opacity-60"
          >
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>
      </div>
    </div>
  );
}
