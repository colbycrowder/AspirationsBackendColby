import { useEffect, useMemo, useState } from "react";
import {
  ApiAccessError,
  createStaffRwdActivity,
  fetchActiveRwdActivities,
  updateStaffRwdActivity,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

const emptyRwdForm = {
  countryName: "",
  title: "",
  description: "",
  externalUrl: "https://aspirationsnetwork.org/movement-map/",
  active: true,
  associatedCredentialId: "",
};

export function StaffRwdManagementPage() {
  const { user } = useAuth();
  const [activities, setActivities] = useState([]);
  const [selectedActivityId, setSelectedActivityId] = useState("");
  const [form, setForm] = useState(emptyRwdForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadActivities();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const selectedActivity = useMemo(
    () => activities.find((activity) => getActivityId(activity) === selectedActivityId) || null,
    [activities, selectedActivityId]
  );

  useEffect(() => {
    if (!selectedActivity) {
      return;
    }

    setForm({
      countryName: selectedActivity.countryName || "",
      title: selectedActivity.title || "",
      description: selectedActivity.description || "",
      externalUrl: selectedActivity.externalUrl || "https://aspirationsnetwork.org/movement-map/",
      active: selectedActivity.active !== false,
      associatedCredentialId: selectedActivity.associatedCredentialId || "",
    });
  }, [selectedActivity]);

  async function loadActivities() {
    setLoading(true);
    setError("");

    try {
      const activeActivities = await fetchActiveRwdActivities();
      setActivities(activeActivities.sort(compareRwdActivities));
      setSelectedActivityId((currentId) => {
        if (currentId && activeActivities.some((activity) => getActivityId(activity) === currentId)) {
          return currentId;
        }
        return activeActivities[0] ? getActivityId(activeActivities[0]) : "";
      });
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setActivities([]);
      setSelectedActivityId("");
    } finally {
      setLoading(false);
    }
  }

  function startNewActivity() {
    setSelectedActivityId("");
    setForm(emptyRwdForm);
    setMessage("");
    setError("");
  }

  async function handleSaveActivity(event) {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    setError("");

    try {
      const payload = buildRwdActivityPayload(form);
      if (selectedActivityId) {
        await updateStaffRwdActivity(user, selectedActivityId, payload);
        setMessage("RWD activity was updated.");
      } else {
        const activityId = await createStaffRwdActivity(user, payload);
        setMessage(`RWD activity was created. Activity ID: ${activityId}`);
        setForm(emptyRwdForm);
      }
      await loadActivities();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setSaving(false);
    }
  }

  async function handleDeactivateActivity() {
    if (!selectedActivityId) {
      return;
    }

    setSaving(true);
    setMessage("");
    setError("");

    try {
      await updateStaffRwdActivity(user, selectedActivityId, { active: false });
      setMessage("RWD activity was deactivated. Inactive activities are not returned by the current active-activity list.");
      setSelectedActivityId("");
      setForm(emptyRwdForm);
      await loadActivities();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <StaffState title="Loading RWD activities" message="Retrieving active RWD learning activities." />;
  }

  if (error && activities.length === 0) {
    return <StaffState title="RWD management unavailable" message={error} />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>RWD Activity Management</h2>
        <p>Create and update externally hosted RWD learning activities for the youth Learning Center.</p>
      </section>

      <section className="notice-panel">
        <strong>Active-list limitation.</strong>
        <span> The backend currently lists active RWD activities only; inactive activities disappear after deactivation.</span>
      </section>

      {message ? (
        <section className="message-panel success">
          <p>{message}</p>
        </section>
      ) : null}

      {error ? (
        <section className="message-panel error">
          <p>{error}</p>
        </section>
      ) : null}

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Active RWD Activities</h3>
              <p>{activities.length} active RWD activit{activities.length === 1 ? "y" : "ies"} available.</p>
            </div>
            <button className="text-action" type="button" onClick={startNewActivity}>
              New Activity
            </button>
          </div>

          {activities.length === 0 ? (
            <p className="empty-text">No active RWD activities are available yet.</p>
          ) : (
            <div className="staff-record-list" aria-label="Active RWD activities">
              {activities.map((activity) => {
                const activityId = getActivityId(activity);

                return (
                  <button
                    className={selectedActivityId === activityId ? "staff-record-card selected" : "staff-record-card"}
                    key={activityId || activity.title}
                    type="button"
                    onClick={() => setSelectedActivityId(activityId)}
                  >
                    <strong>{activity.title || "Untitled RWD activity"}</strong>
                    <span>{activity.countryName || "Country not added"}</span>
                    <span>{activity.externalUrl || "External URL not added"}</span>
                    <span className="status-tag">{activity.active === false ? "inactive" : "active"}</span>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>{selectedActivityId ? "Update RWD Activity" : "Create RWD Activity"}</h3>
              <p>Videos remain externally hosted; this record stores the dashboard and Learning Center metadata.</p>
            </div>
          </div>

          <form className="profile-form" onSubmit={handleSaveActivity}>
            <div className="form-grid">
              <label>
                Country
                <input
                  required
                  value={form.countryName}
                  onChange={(event) => updateForm("countryName", event.target.value)}
                />
              </label>

              <label>
                Title
                <input
                  required
                  value={form.title}
                  onChange={(event) => updateForm("title", event.target.value)}
                />
              </label>

              <label className="full-width-field">
                External URL
                <input value={form.externalUrl} onChange={(event) => updateForm("externalUrl", event.target.value)} />
              </label>

              <label className="full-width-field">
                Associated Credential ID
                <input
                  placeholder="Optional credential definition ID"
                  value={form.associatedCredentialId}
                  onChange={(event) => updateForm("associatedCredentialId", event.target.value)}
                />
              </label>

              <label className="checkbox-row">
                <input
                  checked={form.active}
                  type="checkbox"
                  onChange={(event) => updateForm("active", event.target.checked)}
                />
                Active
              </label>

              <label className="full-width-field">
                Description
                <textarea
                  rows="4"
                  value={form.description}
                  onChange={(event) => updateForm("description", event.target.value)}
                />
              </label>
            </div>

            <div className="profile-actions">
              <button className="primary-action" disabled={saving} type="submit">
                {saving ? "Saving..." : selectedActivityId ? "Update Activity" : "Create Activity"}
              </button>
              {selectedActivityId ? (
                <button className="text-action danger-action" disabled={saving} type="button" onClick={handleDeactivateActivity}>
                  Deactivate Activity
                </button>
              ) : null}
              <button className="text-action" disabled={saving} type="button" onClick={loadActivities}>
                Refresh
              </button>
            </div>
          </form>
        </div>
      </section>
    </div>
  );

  function updateForm(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }
}

function StaffState({ title, message }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
    </section>
  );
}

function buildRwdActivityPayload(form) {
  const payload = {
    active: Boolean(form.active),
  };

  ["countryName", "title", "description", "externalUrl", "associatedCredentialId"].forEach((field) => {
    const value = String(form[field] || "").trim();
    if (value) {
      payload[field] = value;
    }
  });

  return payload;
}

function getActivityId(activity) {
  return activity?.rwdActivityId || "";
}

function compareRwdActivities(first, second) {
  const firstLabel = `${first?.countryName || ""} ${first?.title || ""}`;
  const secondLabel = `${second?.countryName || ""} ${second?.title || ""}`;
  return firstLabel.localeCompare(secondLabel);
}

function getStaffPageError(error) {
  if (error instanceof ApiAccessError && error.status === 401) {
    return "Sign in with a valid Firebase account before opening staff/admin tools.";
  }

  if (error instanceof ApiAccessError && error.status === 403) {
    return "Access denied. This account does not have staff or admin access.";
  }

  return error.message || "Staff/admin data is unavailable. Confirm the backend is running and try again.";
}
