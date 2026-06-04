import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client";

export function FormEditorPage() {
  const { workspaceId, formId } = useParams<{
    workspaceId: string;
    formId: string;
  }>();
  const [title, setTitle] = useState("");
  const [draft, setDraft] = useState<object | null>(null);
  const [saving, setSaving] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!workspaceId || !formId) return;
    api<Record<string, unknown>>(`/workspaces/${workspaceId}/forms/${formId}`)
      .then((def) => {
        setDraft(def);
      })
      .catch(console.error);
  }, [workspaceId, formId]);

  async function saveDraft() {
    if (!workspaceId || !formId || !draft) return;
    setSaving(true);
    setMessage("");
    try {
      await api(`/workspaces/${workspaceId}/forms/${formId}`, {
        method: "PATCH",
        body: JSON.stringify({ draftPageDef: draft }),
      });
      setMessage("Saved");
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "Save failed");
    } finally {
      setSaving(false);
    }
  }

  async function publish() {
    if (!workspaceId || !formId) return;
    setPublishing(true);
    setMessage("");
    try {
      await saveDraft();
      const pub = await api<{ slug?: string }>(
        `/workspaces/${workspaceId}/forms/${formId}/publish`,
        { method: "POST" }
      );
      setMessage(`Published${pub?.slug ? ` · /f/${pub.slug}` : ""}`);
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "Publish failed");
    } finally {
      setPublishing(false);
    }
  }

  return (
    <div className="flex flex-col min-h-screen bg-[var(--color-surface)]">
      <header className="sticky top-0 z-10 bg-white border-b border-[var(--color-border)] px-6 py-3 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3 min-w-0">
          <Link
            to={`/app/workspaces/${workspaceId}`}
            className="text-sm text-[#5f6368] hover:text-[var(--color-brand)] shrink-0"
          >
            ← Forms
          </Link>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Form title"
            className="text-lg font-medium border-0 border-b border-transparent focus:border-[var(--color-brand)] outline-none bg-transparent min-w-[200px]"
          />
        </div>
        <div className="flex items-center gap-2 shrink-0">
          {message && (
            <span className="text-sm text-[#5f6368] mr-2">{message}</span>
          )}
          <button
            type="button"
            onClick={saveDraft}
            disabled={saving}
            className="rounded-lg border border-[var(--color-border)] px-4 py-2 text-sm font-medium hover:bg-[#f1f3f4]"
          >
            Save draft
          </button>
          <button
            type="button"
            onClick={publish}
            disabled={publishing}
            className="rounded-lg bg-[var(--color-brand)] text-white px-4 py-2 text-sm font-medium hover:bg-[var(--color-brand-hover)]"
          >
            Publish
          </button>
        </div>
      </header>

      <div className="flex flex-1">
        <aside className="w-56 border-r border-[var(--color-border)] bg-white p-4 hidden md:block">
          <p className="text-xs font-medium uppercase text-[#5f6368] mb-3">
            Components
          </p>
          <p className="text-sm text-[#5f6368]">
            Drag fields here (builder coming soon)
          </p>
        </aside>
        <section className="flex-1 p-8 flex justify-center">
          <div className="w-full max-w-2xl rounded-2xl bg-white border border-[var(--color-border)] shadow-sm min-h-[400px] p-8">
            <p className="text-[#5f6368] text-center mt-20">
              Form canvas — connect your page builder to{" "}
              <code className="text-xs bg-[#f1f3f4] px-1 rounded">draftPageDef</code>
            </p>
            {draft && (
              <pre className="mt-8 text-xs overflow-auto max-h-48 bg-[#f8f9fa] p-4 rounded-lg">
                {JSON.stringify(draft, null, 2)}
              </pre>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
