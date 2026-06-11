import { useEffect, useMemo, useState } from "react";
import { fetchYouthDashboard } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

export function ServiceHoursPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadServiceHours() {
      setLoading(true);
      setError("");

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

    loadServiceHours();

    return () => {
      isActive = false;
    };
  }, [user]);

  const records = useMemo(
    () => asArray(dashboard?.serviceHourRecords).sort(compareServiceDates),
    [dashboard]
  );
  const approvedHours = sumHoursByStatus(records, "verified");
  const pendingHours = sumHoursByStatus(records, "pending");
  const rejectedHours = sumHoursByStatus(records, "rejected");

  if (loading) {
    return <ServiceHoursState title="Loading service hours" message="Retrieving your ASPN service-hour records." />;
  }

  if (error) {
    return (
      <ServiceHoursState
        title="Service hours unavailable"
        message={error}
        note="If this is a new Firebase account, ASPN may still need to create the matching Firestore profile document."
      />
    );
  }

  return (
    <div className="service-hours-stack">
      <section className="page-intro">
        <span className="eyebrow">Youth Service Hours</span>
        <h2>Service-Hour Records</h2>
        <p>
          Review submitted service hours, approval status, and the current ASPN request link when it is available.
        </p>
      </section>

      <section className="credential-summary-grid" aria-label="Service-hour summary">
        <SummaryTile label="Approved Hours" value={formatHours(approvedHours)} />
        <SummaryTile label="Pending Hours" value={formatHours(pendingHours)} />
        <SummaryTile label="Rejected Hours" value={formatHours(rejectedHours)} />
      </section>

      <section className="dashboard-section">
        <h3>Request Service-Hour Verification</h3>
        {dashboard?.serviceHourRequestFormUrl ? (
          <a className="primary-action link-action" href={dashboard.serviceHourRequestFormUrl} target="_blank" rel="noreferrer">
            Open Request Form
          </a>
        ) : (
          <p className="empty-text">Service-hour request link is not configured yet.</p>
        )}
      </section>

      <section className="dashboard-section">
        <h3>Submitted Records</h3>
        {!records.length ? (
          <p className="empty-text">No service-hour records have been submitted yet.</p>
        ) : (
          <div className="service-record-list">
            {records.map((record) => (
              <article className="service-record" key={record.serviceHourRecordId || record.description}>
                <div className="service-record-header">
                  <div>
                    <strong>{record.description || "Service activity"}</strong>
                    <span>{formatDate(record.serviceDate)}</span>
                  </div>
                  <span className={getStatusClass(record.verificationStatus)}>
                    {record.verificationStatus || "pending"}
                  </span>
                </div>

                <dl className="program-meta">
                  <div>
                    <dt>Hours</dt>
                    <dd>{formatHours(record.hours)}</dd>
                  </div>
                  <div>
                    <dt>Submitted</dt>
                    <dd>{formatDate(record.submittedAt)}</dd>
                  </div>
                  <div>
                    <dt>Program</dt>
                    <dd>{record.programId || "Not listed"}</dd>
                  </div>
                  <div>
                    <dt>Source</dt>
                    <dd>{record.verificationSource || "Not listed"}</dd>
                  </div>
                </dl>
              </article>
            ))}
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

function ServiceHoursState({ title, message, note }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {note ? <p>{note}</p> : null}
    </section>
  );
}

function sumHoursByStatus(records, status) {
  return records.reduce((total, record) => {
    const recordStatus = (record.verificationStatus || "pending").toLowerCase();
    return recordStatus === status ? total + Number(record.hours || 0) : total;
  }, 0);
}

function compareServiceDates(first, second) {
  return getTime(second.submittedAt || second.serviceDate) - getTime(first.submittedAt || first.serviceDate);
}

function getTime(value) {
  const date = new Date(value || 0);
  return Number.isNaN(date.getTime()) ? 0 : date.getTime();
}

function getStatusClass(status) {
  const normalizedStatus = (status || "pending").toLowerCase();
  if (normalizedStatus === "verified") {
    return "status-tag enrolled";
  }

  if (normalizedStatus === "rejected") {
    return "status-tag danger";
  }

  return "status-tag";
}

function formatHours(value) {
  const hours = Number(value || 0);
  if (Number.isInteger(hours)) {
    return String(hours);
  }

  return hours.toFixed(1);
}

function formatDate(value) {
  if (!value) {
    return "date unavailable";
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
