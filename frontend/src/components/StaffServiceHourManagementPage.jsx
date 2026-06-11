import { useState } from "react";
import {
  ApiAccessError,
  createOrReviewStaffServiceHourRecord,
  fetchStaffServiceHoursForUser,
  updateStaffServiceHourRequestUrl,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

const emptyServiceHourForm = {
  userUID: "",
  programId: "",
  serviceDate: "",
  hours: "",
  description: "",
  verificationStatus: "pending",
  verificationSource: "staff_entry",
  googleFormResponseUrl: "",
};

export function StaffServiceHourManagementPage() {
  const { user } = useAuth();
  const [lookupUid, setLookupUid] = useState("");
  const [serviceHours, setServiceHours] = useState([]);
  const [serviceHourForm, setServiceHourForm] = useState(emptyServiceHourForm);
  const [requestUrl, setRequestUrl] = useState("");
  const [loadingRecords, setLoadingRecords] = useState(false);
  const [savingRecord, setSavingRecord] = useState(false);
  const [savingUrl, setSavingUrl] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function handleLookupRecords(event) {
    event.preventDefault();
    await loadServiceHoursForUser(lookupUid);
  }

  async function loadServiceHoursForUser(userUID) {
    const trimmedUid = userUID.trim();
    if (!trimmedUid) {
      setError("Enter a youth UID before loading service-hour records.");
      return;
    }

    setLoadingRecords(true);
    setMessage("");
    setError("");

    try {
      const records = await fetchStaffServiceHoursForUser(user, trimmedUid);
      setServiceHours(records);
      setLookupUid(trimmedUid);
      setServiceHourForm((current) => ({ ...current, userUID: trimmedUid }));
      setMessage(records.length === 0 ? "No service-hour records found for that youth UID." : "Service-hour records loaded.");
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setServiceHours([]);
    } finally {
      setLoadingRecords(false);
    }
  }

  async function handleCreateOrReviewRecord(event) {
    event.preventDefault();
    setSavingRecord(true);
    setMessage("");
    setError("");

    try {
      const recordId = await createOrReviewStaffServiceHourRecord(user, buildServiceHourPayload(serviceHourForm));
      setMessage(`Service-hour record saved. Record ID: ${recordId}`);
      setServiceHourForm((current) => ({
        ...emptyServiceHourForm,
        userUID: current.userUID,
        programId: current.programId,
      }));

      if (serviceHourForm.userUID.trim()) {
        await loadServiceHoursForUser(serviceHourForm.userUID);
      }
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setSavingRecord(false);
    }
  }

  async function handleSaveRequestUrl(event) {
    event.preventDefault();
    setSavingUrl(true);
    setMessage("");
    setError("");

    try {
      await updateStaffServiceHourRequestUrl(user, requestUrl.trim());
      setMessage("Service-hour request form URL was updated.");
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setSavingUrl(false);
    }
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Service Hour Management</h2>
        <p>Create or review service-hour records for a selected youth and configure the request form URL.</p>
      </section>

      <section className="notice-panel">
        <strong>Queue limitation.</strong>
        <span> The backend does not currently expose all-record or pending-record staff listing routes.</span>
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
              <h3>Youth Service Hours</h3>
              <p>Load records by youth UID using the existing staff lookup endpoint.</p>
            </div>
          </div>

          <form className="profile-actions" onSubmit={handleLookupRecords}>
            <label className="inline-field">
              Youth UID
              <input value={lookupUid} onChange={(event) => setLookupUid(event.target.value)} />
            </label>
            <button className="primary-action" disabled={loadingRecords} type="submit">
              {loadingRecords ? "Loading..." : "Load Records"}
            </button>
          </form>

          {serviceHours.length === 0 ? (
            <p className="empty-text">No service-hour records loaded.</p>
          ) : (
            <div className="service-record-list">
              {serviceHours.map((record) => (
                <article className="service-record" key={record.serviceHourRecordId || `${record.userUID}-${record.createdAt}`}>
                  <div className="service-record-header">
                    <div>
                      <strong>{record.description || "Service-hour record"}</strong>
                      <span>{formatDate(record.serviceDate)} · {record.hours ?? 0} hour{Number(record.hours) === 1 ? "" : "s"}</span>
                    </div>
                    <span className={getStatusTagClass(record.verificationStatus)}>
                      {record.verificationStatus || "pending"}
                    </span>
                  </div>

                  <dl className="program-meta">
                    <div>
                      <dt>Program</dt>
                      <dd>{record.programId || "Not recorded"}</dd>
                    </div>
                    <div>
                      <dt>Source</dt>
                      <dd>{record.verificationSource || "Not recorded"}</dd>
                    </div>
                    <div>
                      <dt>Submitted</dt>
                      <dd>{formatDate(record.submittedAt)}</dd>
                    </div>
                    <div>
                      <dt>Reviewed</dt>
                      <dd>{formatDate(record.reviewedAt)}</dd>
                    </div>
                  </dl>
                </article>
              ))}
            </div>
          )}
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Create or Review Record</h3>
              <p>This creates a staff-reviewed service-hour record using the existing backend workflow.</p>
            </div>
          </div>

          <form className="profile-form" onSubmit={handleCreateOrReviewRecord}>
            <div className="form-grid">
              <label>
                Youth UID
                <input
                  required
                  value={serviceHourForm.userUID}
                  onChange={(event) => updateServiceHourForm("userUID", event.target.value)}
                />
              </label>

              <label>
                Program ID
                <input
                  required
                  value={serviceHourForm.programId}
                  onChange={(event) => updateServiceHourForm("programId", event.target.value)}
                />
              </label>

              <label>
                Service Date
                <input
                  type="date"
                  value={serviceHourForm.serviceDate}
                  onChange={(event) => updateServiceHourForm("serviceDate", event.target.value)}
                />
              </label>

              <label>
                Hours
                <input
                  min="0"
                  step="0.25"
                  type="number"
                  value={serviceHourForm.hours}
                  onChange={(event) => updateServiceHourForm("hours", event.target.value)}
                />
              </label>

              <label>
                Verification Status
                <select
                  value={serviceHourForm.verificationStatus}
                  onChange={(event) => updateServiceHourForm("verificationStatus", event.target.value)}
                >
                  <option value="pending">Pending</option>
                  <option value="verified">Verified</option>
                  <option value="rejected">Rejected</option>
                </select>
              </label>

              <label>
                Verification Source
                <input
                  value={serviceHourForm.verificationSource}
                  onChange={(event) => updateServiceHourForm("verificationSource", event.target.value)}
                />
              </label>

              <label className="full-width-field">
                Google Form Response URL
                <input
                  value={serviceHourForm.googleFormResponseUrl}
                  onChange={(event) => updateServiceHourForm("googleFormResponseUrl", event.target.value)}
                />
              </label>

              <label className="full-width-field">
                Description
                <textarea
                  rows="4"
                  value={serviceHourForm.description}
                  onChange={(event) => updateServiceHourForm("description", event.target.value)}
                />
              </label>
            </div>

            <div className="profile-actions">
              <button className="primary-action" disabled={savingRecord} type="submit">
                {savingRecord ? "Saving..." : "Save Service-Hour Record"}
              </button>
            </div>
          </form>
        </div>
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Service-Hour Request URL</h3>
            <p>Set the Google Form request link that later appears in the youth dashboard.</p>
          </div>
        </div>

        <form className="profile-actions" onSubmit={handleSaveRequestUrl}>
          <label className="inline-field wide">
            Request Form URL
            <input value={requestUrl} onChange={(event) => setRequestUrl(event.target.value)} />
          </label>
          <button className="primary-action" disabled={savingUrl} type="submit">
            {savingUrl ? "Saving..." : "Save URL"}
          </button>
        </form>
      </section>
    </div>
  );

  function updateServiceHourForm(field, value) {
    setServiceHourForm((current) => ({ ...current, [field]: value }));
  }
}

function buildServiceHourPayload(form) {
  const payload = {
    userUID: form.userUID.trim(),
    programId: form.programId.trim(),
    hours: form.hours === "" ? 0 : Number(form.hours),
    verificationStatus: form.verificationStatus,
  };

  const optionalTextFields = ["description", "verificationSource", "googleFormResponseUrl"];
  optionalTextFields.forEach((field) => {
    const value = String(form[field] || "").trim();
    if (value) {
      payload[field] = value;
    }
  });

  if (form.serviceDate) {
    payload.serviceDate = toIsoDate(form.serviceDate);
  }

  if (form.verificationStatus === "verified" || form.verificationStatus === "rejected") {
    payload.reviewedAt = new Date().toISOString();
  }

  return payload;
}

function toIsoDate(value) {
  return new Date(`${value}T00:00:00`).toISOString();
}

function formatDate(value) {
  if (!value) {
    return "Not recorded";
  }

  const date = typeof value === "string" ? new Date(value) : new Date(value.seconds ? value.seconds * 1000 : value);
  if (Number.isNaN(date.getTime())) {
    return "Not recorded";
  }

  return date.toLocaleDateString();
}

function getStatusTagClass(status) {
  if (status === "verified") {
    return "status-tag";
  }

  if (status === "rejected") {
    return "status-tag danger";
  }

  return "status-tag muted";
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
