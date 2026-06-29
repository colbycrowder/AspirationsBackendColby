import { useEffect, useMemo, useState } from "react";
import { fetchYouthDashboard, saveRwdProgress, trackPlatformEvent } from "../api.js";
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

    async function loadLearningCenter() {
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

    loadLearningCenter();
    return () => {
      isActive = false;
    };
  }, [user]);

  const items = useMemo(() => asArray(dashboard?.rwdLearningCenter).sort(compareLearningItems), [dashboard]);
  const completedCount = items.filter((item) => getProgressStatus(item) === "completed").length;
  const inProgressCount = items.filter((item) => getProgressStatus(item) === "in_progress").length;
  const connectedCredentialCount = items.filter((item) => hasText(item.associatedCredentialId)).length;
  const nextExperience = items.find((item) => getProgressStatus(item) === "in_progress")
    || items.find((item) => getProgressStatus(item) === "not_started")
    || null;

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
      setMessage(`${item.title || item.countryName || "Global Civic Movements activity"} marked ${formatStatus(completionStatus)}.`);
    } catch (nextError) {
      setError(nextError.message);
    } finally {
      setSavingActivityId("");
    }
  }

  function trackLearningView(item) {
    trackPlatformEvent(user, "RWD_ACTIVITY_VIEWED", { activityId: item.rwdActivityId }).catch(() => {});
  }

  if (loading) {
    return <LearningState title="Loading Global Civic Movements" message="Retrieving available learning experiences." />;
  }

  if (error && !dashboard) {
    return (
      <LearningState
        title="Global Civic Movements unavailable"
        message={error}
        note="If this is a new Firebase account, complete your ASPN profile before opening the Learning Center."
      />
    );
  }

  return (
    <div className="learning-page">
      <section className="learning-hero">
        <span className="learning-kicker">Learning Center</span>
        <h1>Global Civic Movements</h1>
        <p>Explore youth movements around the world and build civic knowledge through guided learning experiences.</p>
      </section>

      <section className="learning-section" aria-labelledby="learning-summary-title">
        <div className="learning-section-heading">
          <div>
            <span className="learning-kicker">Your learning</span>
            <h2 id="learning-summary-title">Learning Summary</h2>
          </div>
        </div>
        <div className="learning-summary-grid">
          <LearningSummaryCard label="Completed Experiences" value={completedCount} />
          <LearningSummaryCard label="Available Experiences" value={items.length} />
          <LearningSummaryCard label="In Progress" value={inProgressCount} />
          <LearningSummaryCard label="Credentials Connected" value={connectedCredentialCount} />
        </div>
      </section>

      {message ? <LearningMessage tone="success" message={message} /> : null}
      {error ? <LearningMessage tone="error" message={error} /> : null}

      <section className="learning-section" aria-labelledby="continue-learning-title">
        <div className="learning-section-heading">
          <div>
            <span className="learning-kicker">Next step</span>
            <h2 id="continue-learning-title">Continue Learning</h2>
          </div>
        </div>
        {!items.length ? (
          <LearningEmptyState message="No Global Civic Movements activities are available yet." />
        ) : nextExperience ? (
          <article className="learning-featured-card">
            <div>
              <span className="learning-country">{nextExperience.countryName || "Global learning"}</span>
              <h3>{nextExperience.title || "Global Civic Movements experience"}</h3>
              <p>{nextExperience.description || "Continue this guided civic learning experience."}</p>
            </div>
            <div className="learning-featured-actions">
              <StatusBadge status={getProgressStatus(nextExperience)} />
              {nextExperience.externalUrl ? (
                <a href={nextExperience.externalUrl} target="_blank" rel="noreferrer" onClick={() => trackLearningView(nextExperience)}>
                  Open experience (opens in new tab)
                </a>
              ) : (
                <span className="learning-link-unavailable">External link unavailable</span>
              )}
            </div>
          </article>
        ) : (
          <LearningEmptyState message="You have completed every available Global Civic Movements experience." />
        )}
      </section>

      <section className="learning-section" aria-labelledby="learning-experiences-title">
        <div className="learning-section-heading">
          <div>
            <span className="learning-kicker">Explore the world</span>
            <h2 id="learning-experiences-title">Learning Experiences</h2>
          </div>
          {items.length ? <span className="learning-count">{items.length} available</span> : null}
        </div>

        {!items.length ? (
          <LearningEmptyState message="No Global Civic Movements activities are available yet." />
        ) : (
          <div className="learning-card-grid">
            {items.map((item) => (
              <LearningExperienceCard
                item={item}
                isSaving={savingActivityId === item.rwdActivityId}
                key={item.rwdActivityId || item.title || item.countryName}
                onProgressUpdate={handleProgressUpdate}
                onView={trackLearningView}
              />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function LearningExperienceCard({ isSaving, item, onProgressUpdate, onView }) {
  const status = getProgressStatus(item);
  const progress = item.progress || {};

  return (
    <article className={`learning-card status-${status}`}>
      <div className="learning-card-header">
        <div>
          <span className="learning-country">{item.countryName || "Global learning"}</span>
          <h3>{item.title || "Global Civic Movements experience"}</h3>
        </div>
        <StatusBadge status={status} />
      </div>

      <p>{item.description || "No experience description is available yet."}</p>

      <div className="learning-card-details">
        {hasText(item.associatedCredentialId) ? (
          <span>Connected credential: {item.associatedCredentialId}</span>
        ) : (
          <span>No connected credential listed</span>
        )}
        {progress.quizScore !== null && progress.quizScore !== undefined ? (
          <span>Quiz: {progress.quizScore}% · {progress.passed ? "Passed" : "Not passed"}</span>
        ) : null}
        {progress.completedAt ? <span>Completed {formatDate(progress.completedAt)}</span> : null}
      </div>

      <div className="learning-actions">
        {item.externalUrl ? (
          <a href={item.externalUrl} target="_blank" rel="noreferrer" onClick={() => onView(item)}>
            Open experience (opens in new tab)
          </a>
        ) : (
          <span className="learning-link-unavailable">External link unavailable</span>
        )}
        <button
          disabled={isSaving || status === "in_progress" || status === "completed"}
          type="button"
          onClick={() => onProgressUpdate(item, "in_progress")}
        >
          {isSaving ? "Saving..." : "Mark in progress"}
        </button>
        <button
          disabled={isSaving || status === "completed"}
          type="button"
          onClick={() => onProgressUpdate(item, "completed")}
        >
          {isSaving ? "Saving..." : "Mark completed"}
        </button>
      </div>
    </article>
  );
}

function LearningSummaryCard({ label, value }) {
  return (
    <article className="learning-summary-card">
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  );
}

function StatusBadge({ status }) {
  return <span className={`learning-status status-${status}`}>{formatStatus(status)}</span>;
}

function LearningEmptyState({ message }) {
  return <p className="learning-empty-state">{message}</p>;
}

function LearningMessage({ tone, message }) {
  return <p className={`learning-message ${tone}`}>{message}</p>;
}

function LearningState({ title, message, note }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {note ? <p>{note}</p> : null}
    </section>
  );
}

function compareLearningItems(first, second) {
  const statusOrder = { in_progress: 0, not_started: 1, completed: 2 };
  const statusDifference = (statusOrder[getProgressStatus(first)] ?? 3) - (statusOrder[getProgressStatus(second)] ?? 3);
  if (statusDifference !== 0) return statusDifference;
  return getLearningSortName(first).localeCompare(getLearningSortName(second));
}

function getLearningSortName(item) {
  return item?.countryName || item?.title || "";
}

function getProgressStatus(item) {
  const status = item?.progress?.completionStatus;
  return ["not_started", "in_progress", "completed"].includes(status) ? status : "not_started";
}

function formatStatus(status) {
  return String(status || "not_started").replaceAll("_", " ");
}

function formatDate(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" });
}

function hasText(value) {
  return typeof value === "string" && value.trim().length > 0;
}

function asArray(value) {
  return Array.isArray(value) ? [...value] : [];
}
