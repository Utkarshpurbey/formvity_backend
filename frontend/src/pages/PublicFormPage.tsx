import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client";

export function PublicFormPage() {
  const { slug } = useParams<{ slug: string }>();
  const [def, setDef] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!slug) return;
    api<Record<string, unknown>>(`/public/forms/${slug}`, { auth: false })
      .then(setDef)
      .catch((e) => setError(e instanceof Error ? e.message : "Form not found"));
  }, [slug]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center p-8">
        <p className="text-red-600">{error}</p>
      </div>
    );
  }

  if (!def) {
    return (
      <div className="min-h-screen flex items-center justify-center p-8 text-[#5f6368]">
        Loading form…
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[var(--color-surface)] py-12 px-4">
      <div className="max-w-xl mx-auto bg-white rounded-2xl border border-[var(--color-border)] shadow-sm p-8">
        <p className="text-sm text-[#5f6368] mb-6">Powered by Formvity</p>
        <p className="text-[#202124]">
          Public respondent view — render fields from published definition.
        </p>
        <pre className="mt-6 text-xs overflow-auto max-h-64 bg-[#f8f9fa] p-4 rounded-lg">
          {JSON.stringify(def, null, 2)}
        </pre>
      </div>
    </div>
  );
}
