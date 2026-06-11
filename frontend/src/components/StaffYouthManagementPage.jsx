import { useEffect, useMemo, useState } from "react";
import { ApiAccessError, fetchStaffYouthUsers, updateStaffYouthUser } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

const profileStatuses = ["pending_onboarding", "active", "inactive"];

export function StaffYouthManagementPage() {
  const { user } = useAuth();
  const [youthUsers, setYouthUsers] = useState([]);
  const [selectedUid, setSelectedUid] = useState("");
  const [form, setForm] = useState({
    profileStatus: "pending_onboarding",
    staffReviewRequired: true,
    staffVerified: false,
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadYouthUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const selectedUser = useMemo(
    () => youthUsers.find((item) => getUserUid(item) === selectedUid) || null,
    [selectedUid, youthUsers]
  );

  useEffect(() => {
    if (!selectedUser) {
      return;
    }

    setForm({
      profileStatus: selectedUser.profileStatus || "pending_onboarding",
      staffReviewRequired: Boolean(selectedUser.staffReviewRequired),
      staffVerified: Boolean(selectedUser.staffVerified),
    });
  }, [selectedUser]);

  async function loadYouthUsers() {
    setLoading(true);
    setError("");
    setMessage("");

    try {
      const users = await fetchStaffYouthUsers(user);
      setYouthUsers(users);
      setSelectedUid((currentUid) => {
        if (currentUid && users.some((item) => getUserUid(item) === currentUid)) {
          return currentUid;
        }
        return users[0] ? getUserUid(users[0]) : "";
      });
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setYouthUsers([]);
      setSelectedUid("");
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (!selectedUid) {
      setError("Select a youth profile before saving.");
      return;
    }

    setSaving(true);
    setError("");
    setMessage("");

    try {
      await updateStaffYouthUser(user, selectedUid, form);
      setMessage("Youth review fields were updated.");
      await loadYouthUsers();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <StaffState title="Loading youth profiles" message="Retrieving youth users for staff review." />;
  }

  if (error && youthUsers.length === 0) {
    return <StaffState title="Youth management unavailable" message={error} />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Youth Management</h2>
        <p>Review youth onboarding status and update limited staff-managed review fields.</p>
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

      {youthUsers.length === 0 ? (
        <StaffState title="No youth profiles found" message="No youth users are available for staff review yet." />
      ) : (
        <section className="staff-management-grid">
          <div className="dashboard-section">
            <div className="section-header">
              <div>
                <h3>Youth Profiles</h3>
                <p>{youthUsers.length} youth profile{youthUsers.length === 1 ? "" : "s"} found.</p>
              </div>
            </div>

            <div className="staff-record-list" aria-label="Youth profiles">
              {youthUsers.map((youthUser) => {
                const uid = getUserUid(youthUser);
                const isSelected = selectedUid === uid;

                return (
                  <button
                    className={isSelected ? "staff-record-card selected" : "staff-record-card"}
                    key={uid || youthUser.email}
                    type="button"
                    onClick={() => setSelectedUid(uid)}
                  >
                    <strong>{getUserDisplayName(youthUser)}</strong>
                    <span>{youthUser.email || "Email not added"}</span>
                    <span>{youthUser.school || "School not added"} · {youthUser.graduationYear || "Graduation year not added"}</span>
                    <span className="status-tag muted">{youthUser.profileStatus || "status missing"}</span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="dashboard-section">
            <div className="section-header">
              <div>
                <h3>Review Fields</h3>
                <p>These controls do not change UID, role, public profile settings, or youth-owned profile fields.</p>
              </div>
            </div>

            {selectedUser ? (
              <form className="profile-form" onSubmit={handleSubmit}>
                <dl className="program-meta">
                  <div>
                    <dt>Name</dt>
                    <dd>{getUserDisplayName(selectedUser)}</dd>
                  </div>
                  <div>
                    <dt>Email</dt>
                    <dd>{selectedUser.email || "Email not added"}</dd>
                  </div>
                  <div>
                    <dt>UID</dt>
                    <dd>{getUserUid(selectedUser)}</dd>
                  </div>
                  <div>
                    <dt>School</dt>
                    <dd>{selectedUser.school || "School not added"}</dd>
                  </div>
                  <div>
                    <dt>Graduation</dt>
                    <dd>{selectedUser.graduationYear || "Graduation year not added"}</dd>
                  </div>
                </dl>

                <div className="form-grid">
                  <label>
                    Profile Status
                    <select
                      value={form.profileStatus}
                      onChange={(event) => setForm((current) => ({ ...current, profileStatus: event.target.value }))}
                    >
                      {profileStatuses.map((status) => (
                        <option key={status} value={status}>
                          {formatLabel(status)}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="checkbox-row">
                    <input
                      checked={form.staffReviewRequired}
                      type="checkbox"
                      onChange={(event) =>
                        setForm((current) => ({ ...current, staffReviewRequired: event.target.checked }))
                      }
                    />
                    Staff review required
                  </label>

                  <label className="checkbox-row">
                    <input
                      checked={form.staffVerified}
                      type="checkbox"
                      onChange={(event) => setForm((current) => ({ ...current, staffVerified: event.target.checked }))}
                    />
                    Staff verified
                  </label>
                </div>

                <div className="profile-actions">
                  <button className="primary-action" disabled={saving} type="submit">
                    {saving ? "Saving..." : "Save Review Fields"}
                  </button>
                  <button className="text-action" disabled={saving} type="button" onClick={loadYouthUsers}>
                    Refresh
                  </button>
                </div>
              </form>
            ) : (
              <p>Select a youth profile to review.</p>
            )}
          </div>
        </section>
      )}
    </div>
  );
}

function StaffState({ title, message }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
    </section>
  );
}

function getUserUid(user) {
  return user?.uid || user?.userUID || "";
}

function getUserDisplayName(user) {
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(" ").trim();
  return fullName || user?.email || "Unnamed youth";
}

function formatLabel(value) {
  return String(value || "").replaceAll("_", " ");
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
