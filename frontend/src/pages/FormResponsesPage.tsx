import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client";

type Summary = {
  totalResponses: number;
  responsesToday: number;
  firstResponseAt: string | null;
  lastResponseAt: string | null;
  currentPublicationVersion: number | null;
};

type TimelineBucket = { date: string; count: number };

type QuestionDistribution = {
  value: string;
  label: string;
  count: number;
  percent: number;
};

type QuestionAnalytics = {
  fieldId: string;
  type: string;
  label: string;
  responseCount: number;
  skippedCount: number;
  average?: number;
  min?: number;
  max?: number;
  distribution?: QuestionDistribution[];
};

type Overview = {
  summary: Summary;
  timeline: TimelineBucket[];
  questions: QuestionAnalytics[];
};

export function FormResponsesPage() {
  const { workspaceId, formId } = useParams<{
    workspaceId: string;
    formId: string;
  }>();
  const [overview, setOverview] = useState<Overview | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!workspaceId || !formId) return;
    api<Overview>(
      `/workspaces/${workspaceId}/forms/${formId}/analytics?days=30`
    )
      .then(setOverview)
      .catch((e) => setError(e instanceof Error ? e.message : "Failed to load"));
  }, [workspaceId, formId]);

  if (error) {
    return (
      <div className="p-8">
        <p className="text-red-600">{error}</p>
        <Link
          to={`/app/workspaces/${workspaceId}/forms/${formId}`}
          className="text-sm text-[var(--color-brand)] mt-4 inline-block"
        >
          ← Back to editor
        </Link>
      </div>
    );
  }

  if (!overview) {
    return <div className="p-8 text-[#5f6368]">Loading responses…</div>;
  }

  const { summary, timeline, questions } = overview;
  const maxCount = Math.max(...timeline.map((b) => b.count), 1);

  return (
    <div className="p-8 max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div>
          <Link
            to={`/app/workspaces/${workspaceId}/forms/${formId}`}
            className="text-sm text-[#5f6368] hover:text-[var(--color-brand)]"
          >
            ← Back to editor
          </Link>
          <h1 className="text-2xl font-semibold text-[#202124] mt-2">
            Responses
          </h1>
        </div>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <StatCard label="Total responses" value={String(summary.totalResponses)} />
        <StatCard label="Today" value={String(summary.responsesToday)} />
        <StatCard
          label="First response"
          value={formatDate(summary.firstResponseAt)}
        />
        <StatCard
          label="Last response"
          value={formatDate(summary.lastResponseAt)}
        />
      </div>

      <section className="bg-white rounded-2xl border border-[var(--color-border)] p-6 mb-8">
        <h2 className="text-sm font-medium uppercase text-[#5f6368] mb-4">
          Responses over time (30 days)
        </h2>
        {timeline.length === 0 ? (
          <p className="text-sm text-[#5f6368]">No responses yet.</p>
        ) : (
          <div className="flex items-end gap-1 h-32">
            {timeline.map((bucket) => (
              <div
                key={bucket.date}
                className="flex-1 flex flex-col items-center justify-end min-w-0"
                title={`${bucket.date}: ${bucket.count}`}
              >
                <div
                  className="w-full bg-[var(--color-brand)] rounded-t opacity-80 min-h-[2px]"
                  style={{
                    height: `${Math.max((bucket.count / maxCount) * 100, bucket.count > 0 ? 8 : 2)}%`,
                  }}
                />
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="space-y-4">
        <h2 className="text-sm font-medium uppercase text-[#5f6368]">
          Question insights
        </h2>
        {questions.length === 0 ? (
          <p className="text-sm text-[#5f6368]">
            No questions in the published form yet.
          </p>
        ) : (
          questions.map((q) => (
            <div
              key={q.fieldId}
              className="bg-white rounded-2xl border border-[var(--color-border)] p-6"
            >
              <div className="flex justify-between gap-4 mb-3">
                <p className="font-medium text-[#202124]">{q.label}</p>
                <span className="text-xs text-[#5f6368] shrink-0">
                  {q.responseCount} answered · {q.skippedCount} skipped
                </span>
              </div>
              {q.average != null && (
                <p className="text-sm text-[#5f6368] mb-3">
                  Avg {q.average.toFixed(1)}
                  {q.min != null && q.max != null
                    ? ` · range ${q.min}–${q.max}`
                    : ""}
                </p>
              )}
              {q.distribution && q.distribution.length > 0 && (
                <ul className="space-y-2">
                  {q.distribution.map((d) => (
                    <li key={d.value} className="text-sm">
                      <div className="flex justify-between mb-1">
                        <span>{d.label}</span>
                        <span className="text-[#5f6368]">
                          {d.count} ({d.percent}%)
                        </span>
                      </div>
                      <div className="h-2 bg-[#f1f3f4] rounded-full overflow-hidden">
                        <div
                          className="h-full bg-[var(--color-brand)] rounded-full"
                          style={{ width: `${d.percent}%` }}
                        />
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          ))
        )}
      </section>
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-white rounded-xl border border-[var(--color-border)] p-4">
      <p className="text-xs text-[#5f6368] uppercase tracking-wide">{label}</p>
      <p className="text-2xl font-semibold text-[#202124] mt-1">{value}</p>
    </div>
  );
}

function formatDate(iso: string | null) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  });
}
