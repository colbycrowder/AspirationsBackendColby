import { useEffect, useMemo, useState } from "react";
import {
  ApiAccessError,
  fetchStaffPrograms,
  fetchStaffYouthDuplicateProfileGroups,
  fetchStaffYouthDuplicateProfiles,
  fetchStaffYouthStudentRecord,
  fetchStaffYouthUsers,
  updateStaffYouthUser,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getUniqueEarnedCredentials } from "../utils/credentialDeduplication.js";

const profileStatuses = ["pending_onboarding", "active", "inactive"];

export function StaffYouthManagementPage() {
  const { user } = useAuth();
  const [youthUsers, setYouthUsers] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [selectedUid, setSelectedUid] = useState("");
  const [studentRecord, setStudentRecord] = useState(null);
  const [duplicateGroups, setDuplicateGroups] = useState([]);
  const [duplicateProfiles, setDuplicateProfiles] = useState([]);
  const [recordLoading, setRecordLoading] = useState(false);
  const [duplicateLoading, setDuplicateLoading] = useState(false);
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
    () => youthUsers.find((item) => getYouthSelectionKey(item) === selectedUid) || null,
    [selectedUid, youthUsers]
  );

  useEffect(() => {
    if (!selectedUser) {
      setStudentRecord(null);
      setDuplicateProfiles([]);
      return;
    }

    setForm({
      profileStatus: selectedUser.profileStatus || "pending_onboarding",
      staffReviewRequired: Boolean(selectedUser.staffReviewRequired),
      staffVerified: Boolean(selectedUser.staffVerified),
    });
    const identifier = getStudentRecordIdentifier(selectedUser);
    loadStudentRecord(identifier);
    loadDuplicateProfiles(identifier);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedUser]);

  async function loadYouthUsers() {
    setLoading(true);
    setError("");
    setMessage("");

    try {
      const [users, activePrograms] = await Promise.all([
        fetchStaffYouthUsers(user),
        fetchStaffPrograms(user),
      ]);
      const groups = await fetchStaffYouthDuplicateProfileGroups(user).catch(() => []);
      setYouthUsers(users);
      setPrograms(activePrograms);
      setDuplicateGroups(Array.isArray(groups) ? groups : []);
      setSelectedUid((currentUid) => {
        if (currentUid && users.some((item) => getYouthSelectionKey(item) === currentUid)) {
          return currentUid;
        }
        return users[0] ? getYouthSelectionKey(users[0]) : "";
      });
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setYouthUsers([]);
      setPrograms([]);
      setDuplicateGroups([]);
      setSelectedUid("");
    } finally {
      setLoading(false);
    }
  }

  async function loadStudentRecord(uid = selectedUid) {
    if (!uid) {
      setStudentRecord(null);
      return;
    }

    setRecordLoading(true);
    try {
      setStudentRecord(normalizeStudentRecordResponse(await fetchStaffYouthStudentRecord(user, uid)));
    } catch (nextError) {
      setStudentRecord(null);
      setError(getStaffPageError(nextError));
    } finally {
      setRecordLoading(false);
    }
  }

  async function loadDuplicateProfiles(uid = selectedUid) {
    if (!uid) {
      setDuplicateProfiles([]);
      return;
    }

    setDuplicateLoading(true);
    try {
      const duplicates = await fetchStaffYouthDuplicateProfiles(user, uid);
      setDuplicateProfiles(Array.isArray(duplicates) ? duplicates : []);
    } catch (nextError) {
      setDuplicateProfiles([]);
    } finally {
      setDuplicateLoading(false);
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
                const selectionKey = getYouthSelectionKey(youthUser);
                const isSelected = selectedUid === selectionKey;

                return (
                  <button
                    className={isSelected ? "staff-record-card selected" : "staff-record-card"}
                    key={selectionKey || youthUser.email}
                    type="button"
                    onClick={() => setSelectedUid(selectionKey)}
                  >
                    <strong>{getUserDisplayName(youthUser)}</strong>
                    <span>{youthUser.email || "Email not added"}</span>
                    <span>{youthUser.school || "School not added"} · {youthUser.graduationYear || "Graduation year not added"}</span>
                    <span className="status-tag muted">{youthUser.profileStatus || "status missing"}</span>
                  </button>
                );
              })}
            </div>

            <DuplicateProfileReviewPanel groups={duplicateGroups} />
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
                    <dt>ASPN Participant ID</dt>
                    <dd>{selectedUser.aspnParticipantId || "Not assigned yet"}</dd>
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
                  <button
                    className="text-action"
                    disabled={recordLoading || duplicateLoading}
                    type="button"
                    onClick={() => {
                      const identifier = getStudentRecordIdentifier(selectedUser);
                      loadStudentRecord(identifier);
                      loadDuplicateProfiles(identifier);
                    }}
                  >
                    {recordLoading ? "Loading record..." : "Refresh Student Record"}
                  </button>
                </div>
              </form>
            ) : (
              <p>Select a youth profile to review.</p>
            )}

            {selectedUser ? (
              <>
                <DuplicateProfileWarning
                  duplicates={duplicateProfiles}
                  loading={duplicateLoading}
                />
                <StudentRecordPanel
                  loading={recordLoading}
                  programs={programs}
                  record={studentRecord}
                  selectedUser={selectedUser}
                />
              </>
            ) : null}
          </div>
        </section>
      )}
    </div>
  );
}

function DuplicateProfileReviewPanel({ groups }) {
  return (
    <section className="duplicate-review-panel" aria-labelledby="duplicate-review-title">
      <div className="section-header">
        <div>
          <h3 id="duplicate-review-title">Duplicate Profile Review</h3>
          <p>Review only. Do not merge, archive, or delete profiles during pilot validation.</p>
        </div>
      </div>

      {groups.length ? (
        <div className="duplicate-review-groups">
          {groups.map((group, groupIndex) => (
            <div className="duplicate-review-group" key={`${group.matchType}-${group.matchValue}-${groupIndex}`}>
              <div className="duplicate-review-group-header">
                <strong>{group.reason || "Possible duplicate match"}</strong>
                <span>{group.matchValue || "Match value unavailable"}</span>
              </div>

              <div className="duplicate-profile-list">
                {getRecordArray(group, ["profiles"]).map((profile) => (
                  <DuplicateProfileSummaryCard
                    key={profile.uid || profile.email || profile.aspnParticipantId}
                    profile={profile}
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="empty-text">No possible duplicate profile groups detected.</p>
      )}

      <p className="helper-text">
        Use the existing youth selection and Student Record panel to inspect records manually.
      </p>
    </section>
  );
}

function DuplicateProfileWarning({ duplicates, loading }) {
  if (loading || !duplicates.length) {
    return null;
  }

  return (
    <section className="duplicate-profile-warning" aria-labelledby="duplicate-profile-warning-title">
      <div>
        <span className="status-tag warning">Review recommended</span>
        <h3 id="duplicate-profile-warning-title">Possible duplicate profile found</h3>
        <p>
          This youth may have another profile with the same email or ASPN Participant ID.
        </p>
        <p>
          Do not delete or merge profiles during pilot validation. Use the Student Record panel to confirm which
          profile contains records.
        </p>
      </div>

      <div className="duplicate-profile-list">
        {duplicates.map((duplicate) => (
          <DuplicateProfileSummaryCard
            key={duplicate.uid || duplicate.email || duplicate.aspnParticipantId}
            profile={duplicate}
          />
        ))}
      </div>
    </section>
  );
}

function DuplicateProfileSummaryCard({ profile }) {
  return (
    <div className="duplicate-profile-card">
      <strong>{profile.name || "Unnamed youth"}</strong>
      <span>{profile.email || "Email not added"}</span>
      <span>ASPN Participant ID: {profile.aspnParticipantId || "Not assigned"}</span>
      <span>UID: {profile.uid || "UID missing"}</span>
      <span>Status: {formatLabel(profile.profileStatus || "status missing")}</span>
      <span>Staff verified: {profile.staffVerified ? "Yes" : "No"}</span>
      <span>Dashboard records: {profile.dashboardRecordsAvailable ? "Appear to exist" : "Not detected"}</span>
    </div>
  );
}

function StudentRecordPanel({ loading, programs, record, selectedUser }) {
  const credentials = getUniqueEarnedCredentials(getRecordArray(record, ["earnedCredentials", "credentials"]));
  const attendance = getStudentAttendanceRecords(getRecordArray(record, ["attendanceRecords", "attendance"]));
  const serviceHours = getRecordArray(record, ["serviceHourRecords", "serviceHours"]);

  return (
    <section className="staff-detail-panel student-record-panel" aria-labelledby="student-record-title">
      <div className="section-header">
        <div>
          <h3 id="student-record-title">Student Record</h3>
          <p>Attendance, service hours, and credentials for {getUserDisplayName(selectedUser)}.</p>
        </div>
      </div>

      {loading ? <p>Loading student record...</p> : null}

      {!loading ? (
        <>
          <RecordSection title="Credentials" emptyMessage="No credentials awarded yet.">
            {credentials.map((credential, index) => (
              <div className="student-record-row" key={credential.earnedCredentialID || credential.credentialID || index}>
                <strong>{credential.credentialName || "ASPN Credential"}</strong>
                <span>{credential.category || "Category not listed"}</span>
                <span>{formatRecordDate(credential.awardedAt || credential.earnedAt)}</span>
              </div>
            ))}
          </RecordSection>

          <RecordSection title="Attendance" emptyMessage="No attendance records yet.">
            {attendance.map((item, index) => (
              <div className="student-record-row" key={item.attendanceRecordID || `${item.programID}-${item.eventDate}-${index}`}>
                <strong>{formatRecordDate(item.eventDate)}</strong>
                <span>{getProgramName(programs, item.programID)}</span>
                <span>{item.eventName || "Attendance session"} · {formatLabel(item.attendanceStatus || "pending")}</span>
              </div>
            ))}
          </RecordSection>

          <RecordSection title="Service Hours" emptyMessage="No service-hour records yet.">
            {serviceHours.map((item, index) => (
              <div className="student-record-row" key={item.serviceHourRecordId || `${item.programId}-${item.serviceDate}-${index}`}>
                <strong>{formatRecordDate(item.serviceDate)}</strong>
                <span>{getProgramName(programs, item.programId)}</span>
                <span>{formatHours(item.hours)} hours · {formatLabel(item.verificationStatus || "pending")}</span>
              </div>
            ))}
          </RecordSection>
        </>
      ) : null}
    </section>
  );
}

function RecordSection({ children, emptyMessage, title }) {
  const items = Array.isArray(children) ? children.filter(Boolean) : children ? [children] : [];
  return (
    <div className="student-record-section">
      <h4>{title}</h4>
      {items.length ? <div className="student-record-list">{items}</div> : <p className="empty-text">{emptyMessage}</p>}
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
  return user?.uid || user?.userUID || user?.userUid || user?.id || "";
}

function getYouthSelectionKey(user) {
  return getUserUid(user) || user?.aspnParticipantId || user?.email || "";
}

function getStudentRecordIdentifier(user) {
  return getUserUid(user) || user?.aspnParticipantId || user?.email || "";
}

function normalizeStudentRecordResponse(record) {
  if (!record || typeof record !== "object") {
    return {
      earnedCredentials: [],
      attendanceRecords: [],
      serviceHourRecords: [],
    };
  }

  return {
    ...record,
    earnedCredentials: getRecordArray(record, ["earnedCredentials", "credentials"]),
    attendanceRecords: getRecordArray(record, ["attendanceRecords", "attendance"]),
    serviceHourRecords: getRecordArray(record, ["serviceHourRecords", "serviceHours"]),
  };
}

function getRecordArray(record, fieldNames) {
  if (!record) {
    return [];
  }

  for (const fieldName of fieldNames) {
    if (Array.isArray(record[fieldName])) {
      return record[fieldName];
    }
  }

  return [];
}

function getUserDisplayName(user) {
  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(" ").trim();
  return fullName || user?.email || "Unnamed youth";
}

function getStudentAttendanceRecords(records) {
  return [...records]
    .sort((left, right) => compareRecordDates(right.eventDate, left.eventDate));
}

function getProgramName(programs, programId) {
  if (!programId) {
    return "Program not listed";
  }
  const program = programs.find((item) => item.programId === programId || item.programID === programId);
  return program?.programName || program?.title || program?.name || "Program not listed";
}

function formatRecordDate(value) {
  const date = parseRecordDate(value);
  if (!date) {
    return "Date not listed";
  }
  return date.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" });
}

function parseRecordDate(value) {
  if (!value) return null;
  if (typeof value === "object" && value.seconds) {
    const firestoreDate = new Date(value.seconds * 1000);
    return Number.isNaN(firestoreDate.getTime()) ? null : firestoreDate;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function compareRecordDates(left, right) {
  const leftDate = parseRecordDate(left);
  const rightDate = parseRecordDate(right);
  if (leftDate && rightDate) return leftDate.getTime() - rightDate.getTime();
  if (leftDate) return -1;
  if (rightDate) return 1;
  return 0;
}

function formatHours(value) {
  const hours = Number(value || 0);
  return Number.isInteger(hours) ? String(hours) : hours.toFixed(2).replace(/0+$/, "").replace(/\.$/, "");
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
