import { FormEvent, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api, FormSummary, WorkspaceDashboard } from "../api/client";

export function WorkspaceFormsPage() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const [dashboard, setDashboard] = useState<WorkspaceDashboard | null>(null);
  const [showCreate, setShowCreate] = useState(false);
  const [title, setTitle] = useState("Untitled form");
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    if (!workspaceId) return;
    api<WorkspaceDashboard>(`/workspaces/${workspaceId}/dashboard`)
      .then(setDashboard)
      .catch(console.error);
  }, [workspaceId]);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    if (!workspaceId) return;
    setCreating(true);
    try {
      const form = await api<{ id: string }>(`/workspaces/${workspaceId}/forms`, {
        method: "POST",
        body: JSON.stringify({
          title: title.trim(),
          draftPageDef: { pages: [{ id: "p1", title: "Page 1", fields: [] }] },
        }),
      });
      setShowCreate(false);
      window.location.href = `/app/workspaces/${workspaceId}/forms/${form.id}`;
    } catch (err) {
      alert(err instanceof Error ? err.message : "Failed to create form");
    } finally {
      setCreating(false);
    }
  }

  if (!dashboard) {
    return (
      <div className="p-8 text-[#5f6368]">Loading workspace…</div>
    );
  }

  const forms = dashboard.recentForms ?? [];

  return (
    <div className="p-8 max-w-6xl mx-auto">
      <nav className="text-sm text-[#5f6368] mb-4">
        <Link to="/app/workspaces" className="hover:text-[var(--color-brand)]">
          Workspaces
        </Link>
        <span className="mx-2">/</span>
        <span className="text-[#202124]">{dashboard.workspaceName}</span>
      </nav>

      <header className="flex flex-wrap items-start justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-normal text-[#202124]">
            {dashboard.workspaceName}
          </h1>
          <p className="text-[#5f6368] mt-1">
            {dashboard.formCount} form{dashboard.formCount === 1 ? "" : "s"} in
            this workspace
          </p>
        </div>
        <button
          type="button"
          onClick={() => setShowCreate(true)}
          className="rounded-lg bg-[var(--color-brand)] text-white px-5 py-2.5 font-medium hover:bg-[var(--color-brand-hover)] shadow-sm"
        >
          + Blank form
        </button>
      </header>

      {forms.length === 0 ? (
        <div className="rounded-2xl border border-[var(--color-border)] bg-white p-12 text-center">
          <p className="text-lg text-[#3c4043]">No forms yet</p>
          <p className="text-[#5f6368] mt-2 text-sm">
            Create your first form to collect responses.
          </p>
          <button
            type="button"
            onClick={() => setShowCreate(true)}
            className="mt-6 rounded-lg bg-[var(--color-brand)] text-white px-6 py-2.5 font-medium"
          >
            Create form
          </button>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {forms.map((form: FormSummary) => (
            <FormCard key={form.id} form={form} workspaceId={workspaceId!} />
          ))}
        </div>
      )}

      {showCreate && (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center p-4 z-50">
          <form
            onSubmit={handleCreate}
            className="bg-white rounded-2xl p-6 w-full max-w-md shadow-xl"
          >
            <h3 className="text-xl font-medium mb-4">New form</h3>
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
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

function FormCard({
  form,
  workspaceId,
}: {
  form: FormSummary;
  workspaceId: string;
}) {
  const statusColor =
    form.status === "PUBLISHED"
      ? "bg-green-100 text-green-800"
      : form.status === "ARCHIVED"
        ? "bg-gray-100 text-gray-600"
        : "bg-amber-100 text-amber-800";

  return (
    <Link
      to={`/app/workspaces/${workspaceId}/forms/${form.id}`}
      className="rounded-2xl border border-[var(--color-border)] bg-white p-5 hover:shadow-md transition-shadow block"
    >
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-medium text-[#202124] line-clamp-2">{form.title}</h3>
        <span className={`shrink-0 text-xs px-2 py-0.5 rounded-full ${statusColor}`}>
          {form.status}
        </span>
      </div>
      <p className="text-xs text-[#5f6368] mt-3">
        Updated {new Date(form.updatedAt).toLocaleDateString()}
      </p>
    </Link>
  );
}
