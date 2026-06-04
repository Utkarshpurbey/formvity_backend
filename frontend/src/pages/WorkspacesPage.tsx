import { FormEvent, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, WorkspaceCard } from "../api/client";

export function WorkspacesPage() {
  const [workspaces, setWorkspaces] = useState<WorkspaceCard[]>([]);
  const [name, setName] = useState("");
  const [creating, setCreating] = useState(false);
  const [showCreate, setShowCreate] = useState(false);

  function load() {
    api<WorkspaceCard[]>("/workspaces").then(setWorkspaces).catch(console.error);
  }

  useEffect(() => {
    load();
  }, []);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    if (!name.trim()) return;
    setCreating(true);
    try {
      await api("/workspaces", {
        method: "POST",
        body: JSON.stringify({ workSpaceName: name.trim() }),
      });
      setName("");
      setShowCreate(false);
      load();
    } catch (err) {
      alert(err instanceof Error ? err.message : "Failed to create workspace");
    } finally {
      setCreating(false);
    }
  }

  return (
    <div className="p-8 max-w-5xl mx-auto">
      <header className="mb-8">
        <h1 className="text-3xl font-normal text-[#202124]">Your workspaces</h1>
        <p className="text-[#5f6368] mt-1">
          Pick a team space. Forms live inside each workspace — not here.
        </p>
      </header>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {workspaces.map((ws) => (
          <Link
            key={ws.workspaceId}
            to={`/app/workspaces/${ws.workspaceId}`}
            className="group rounded-2xl border border-[var(--color-border)] bg-white p-6 hover:shadow-md hover:border-[#d2e3fc] transition-all"
          >
            <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-[#e8f0fe] text-[var(--color-brand)] text-xl font-medium mb-4">
              {ws.workspaceName.charAt(0).toUpperCase()}
            </div>
            <h2 className="text-lg font-medium text-[#202124] group-hover:text-[var(--color-brand)]">
              {ws.workspaceName}
            </h2>
            <p className="text-sm text-[#5f6368] mt-1">
              {ws.formCount} form{ws.formCount === 1 ? "" : "s"}
            </p>
            <p className="text-sm text-[var(--color-brand)] mt-4 font-medium">
              Open forms →
            </p>
          </Link>
        ))}

        <button
          type="button"
          onClick={() => setShowCreate(true)}
          className="rounded-2xl border-2 border-dashed border-[var(--color-border)] p-6 text-left hover:border-[var(--color-brand)] hover:bg-white transition-colors min-h-[180px]"
        >
          <span className="text-3xl text-[#5f6368]">+</span>
          <p className="mt-2 font-medium text-[#3c4043]">Create workspace</p>
          <p className="text-sm text-[#5f6368]">For a team or project</p>
        </button>
      </div>

      {showCreate && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center p-4 z-50">
          <form
            onSubmit={handleCreate}
            className="bg-white rounded-2xl p-6 w-full max-w-md shadow-xl"
          >
            <h3 className="text-xl font-medium mb-4">New workspace</h3>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Marketing, HR, Events"
              className="w-full rounded-lg border border-[var(--color-border)] px-3 py-2.5 mb-4"
              autoFocus
            />
            <div className="flex gap-2 justify-end">
              <button
                type="button"
                onClick={() => setShowCreate(false)}
                className="px-4 py-2 text-[#5f6368]"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={creating}
                className="px-4 py-2 rounded-lg bg-[var(--color-brand)] text-white font-medium"
              >
                Create
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
