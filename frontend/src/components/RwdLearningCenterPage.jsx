import { useEffect, useMemo, useState } from "react";
import { fetchYouthDashboard, saveRwdProgress } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

export function RwdLearningCenterPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [savingActivityId, setSavingActivityId] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadRwdLearningCenter() {
      setLoading(true);
      setError("");
      setMessage("");

      try {
        const data = await fetchYouthDashboard(user);
        if (isActive) {
          setDashboard(data);
        }
      } catch (nextError) {
        if (isActive) {
          setError(nextError.message);
          setDashboard(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadRwdLearningCenter();

    return () => {
      isActive = false;
    };
  }, [user]);

  const items = useMemo(
    () => asArray(dashboard?.rwdLearningCenter).sort(compareRwdItems),
    [dashboard]
  );
  const completedCount = items.filter((item) => getProgressStatus(item) === "completed").length;
  const inProgressCount = items.filter((item) => getProgressStatus(item) === "in_progress").length;

  async function handleProgressUpdate(item, completionStatus) {
    setError("");
    setMessage("");
    setSavingActivityId(item.rwdActivityId);

    try {
      await saveRwdProgress(user, {
        rwdActivityId: item.rwdActivityId,
        completionStatus,
      });
      const data = await fetchYouthDashboard(user);
      setDashboard(data);
      setMessage(`${item.title || item.countryName || "RWD activity"} marked ${formatStatus(completionStatus)}.`);
    } catch (nextError) {
      setError(nextError.message);
    } finally {
      setSavingActivityId("");
    }
  }

  if (loading) {
    return <RwdState title="Loading RWD Learning Center" message="Retrieving available RWD learning items." />;
  }

  if (error && !dashboard) {
    return (
      <RwdState
        title="RWD Learning Center unavailable"
        message={error}
        note="If this is a new Firebase account, ASPN may still need to create the matching Firestore profile document."
      />
    );
  }

  return (
    <div className="rwd-stack">
      <section className="page-intro">
        <span className="eyebrow">RWD Learning Center</span>
        <h2>Movement Map Activities</h2>
        <p>
          Open externally hosted RWD learning items, track completion status, and see linked credential information when
          available.
        </p>
      </section>

      <section className="credential-summary-grid" aria-label="RWD progress summary">
        <SummaryTile label="Available Items" value={items.length} />
        <SummaryTile label="In Progress" value={inProgressCount} />
        <SummaryTile label="Completed" value={completedCount} />
      </section>

      {message ? <MessagePanel tone="success" message={message} /> : null}
      {error ? <MessagePanel tone="error" message={error} /> : null}

      <section className="dashboard-section">
        <h3>Learning Items</h3>
        {!items.length ? (
          <p className="empty-text">No RWD learning items are available yet.</p>
        ) : (
          <div className="rwd-grid">
            {items.map((item) => {
              const status = getProgressStatus(item);
              const progress = item.progress || {};
              const isSaving = savingActivityId === item.rwdActivityId;

              return (
                <article className="rwd-card" key={item.rwdActivityId || item.title || item.countryName}>
                  <div className="rwd-card-header">
                    <div>
                      <span className="eyebrow">{item.countryName || "Country unavailable"}</span>
                      <h3>{item.title || "RWD activity"}</h3>
                    </div>
                    <span className={getStatusClass(status)}>{formatStatus(status)}</span>
                  </div>

                  <p>{item.description || "No activity description is available yet."}</p>

                  <div className="progress-track" aria-label={`Progress ${getProgressPercent(status)} percent`}>
                    <span style={{ width: `${getProgressPercent(status)}%` }} />
                  </div>

                  <dl className="program-meta">
                    <div>
                      <dt>Quiz</dt>
                      <dd>{formatQuiz(progress)}</dd>
                    </div>
                    <div>
                      <dt>Credential</dt>
                      <dd>{item.associatedCredentialId || "No linked credential listed"}</dd>
                    </div>
                    <div>
                      <dt>Completed</dt>
                      <dd>{formatDate(progress.completedAt)}</dd>
                    </div>
                  </dl>

                  <div className="rwd-actions">
                    {item.externalUrl ? (
                      <a className="primary-action link-action" href={item.externalUrl} target="_blank" rel="noreferrer">
                        Open Activity
                      </a>
                    ) : null}
                    <button
                      className="text-action"
                      disabled={isSaving || status === "in_progress" || status === "completed"}
                      type="button"
                      onClick={() => handleProgressUpdate(item, "in_progress")}
                    >
                      {isSaving ? "Saving..." : "Mark In Progress"}
                    </button>
                    <button
                      className="text-action"
                      disabled={isSaving || status === "completed"}
                      type="button"
                      onClick={() => handleProgressUpdate(item, "completed")}
                    >
                      {isSaving ? "Saving..." : "Mark Completed"}
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );
}

function SummaryTile({ label, value }) {
  return (
    <article className="summary-tile">
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  );
}

function MessagePanel({ tone, message }) {
  return (
    <section className={`message-panel ${tone}`}>
      <p>{message}</p>
    </section>
  );
}

function RwdState({ title, message, note }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {note ? <p>{note}</p> : null}
    </section>
  );
}

function compareRwdItems(first, second) {
  return getRwdSortName(first).localeCompare(getRwdSortName(second));
}

function getRwdSortName(item) {
  return item?.countryName || item?.title || "";
}

function getProgressStatus(item) {
  return item?.progress?.completionStatus || "not_started";
}

function getProgressPercent(status) {
  if (status === "completed") {
    return 100;
  }

  if (status === "in_progress") {
    return 50;
  }

  return 0;
}

function getStatusClass(status) {
  if (status === "completed") {
    return "status-tag enrolled";
  }

  if (status === "in_progress") {
    return "status-tag";
  }

  return "status-tag muted";
}

function formatStatus(status) {
  return String(status || "not_started").replaceAll("_", " ");
}

function formatQuiz(progress) {
  if (!progress?.quizScore && progress?.quizScore !== 0) {
    return "No quiz score";
  }

  return `${progress.quizScore}% · ${progress.passed ? "passed" : "not passed"}`;
}

function formatDate(value) {
  if (!value) {
    return "Not completed";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return date.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}
