import { useEffect, useState } from "react";
import { fetchStaffPilotReadiness } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getStaffPageError, StaffState, SummaryGrid } from "./staffUi.jsx";

const metricItems = [
  ["totalYouthUsers", "Total Youth"],
  ["activeYouthUsers", "Active Youth"],
  ["completedProfiles", "Completed Profiles"],
  ["profileCompletionRate", "Profile Completion"],
  ["activePrograms", "Active Programs"],
  ["activeCredentialDefinitions", "Active Credentials"],
  ["attendanceRecords", "Attendance Records"],
  ["serviceHourRecords", "Service-Hour Records"],
  ["activeEducators", "Active Educators"],
  ["activePartnerOrganizations", "Active Partners"],
  ["activeGovernmentOrganizations", "Active Government"],
  ["activeStakeholderRelationshipNotes", "Active Relationship Notes"],
  ["platformEventsLast30Days", "Events Last 30 Days"],
];

export function PilotReadinessDashboard() {
  const { user } = useAuth();
  const [readiness, setReadiness] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadReadiness() {
      setLoading(true);
      setError("");

      try {
        const data = await fetchStaffPilotReadiness(user);
        if (isActive) {
          setReadiness(data);
        }
      } catch (nextError) {
        if (isActive) {
          setError(getStaffPageError(nextError));
          setReadiness(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadReadiness();

    return () => {
      isActive = false;
    };
  }, [user]);

  if (loading) {
    return <StaffState title="Loading pilot readiness" message="Retrieving pilot readiness status." />;
  }

  if (error) {
    return <StaffState title="Pilot readiness unavailable" message={error} />;
  }

  if (!readiness) {
    return <StaffState title="No readiness data" message="The backend returned no pilot readiness data." />;
  }

  const checklistItems = Array.isArray(readiness.checklistItems) ? readiness.checklistItems : [];
  const blockers = Array.isArray(readiness.blockers) ? readiness.blockers : [];
  const warnings = Array.isArray(readiness.warnings) ? readiness.warnings : [];

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Pilot Readiness Dashboard</h2>
        <p>Operational readiness check for a controlled youth pilot using current platform data.</p>
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Readiness Status</h3>
            <p>Score is based on youth onboarding, programs, credentials, operational records, stakeholder relationships, and recent activity.</p>
          </div>
          <span className={`status-pill ${statusClass(readiness.readinessStatus)}`}>
            {readiness.readinessStatus || "unknown"}
          </span>
        </div>
        <SummaryGrid
          items={[
            { label: "Readiness Score", value: `${readiness.readinessScore ?? 0}/100` },
            { label: "Status", value: readiness.readinessStatus || "unknown" },
            { label: "Generated", value: formatDateTime(readiness.generatedAt) },
          ]}
        />
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Readiness Metrics</h3>
            <p>Key counts used by the pilot readiness calculation.</p>
          </div>
        </div>
        <SummaryGrid items={metricItems.map(([key, label]) => ({ label, value: formatMetric(key, readiness[key]) }))} />
      </section>

      <section className="staff-management-grid">
        <MessageList title="Blockers" emptyMessage="No pilot blockers detected." items={blockers} />
        <MessageList title="Warnings" emptyMessage="No readiness warnings detected." items={warnings} />
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Readiness Checklist</h3>
            <p>Checklist categories reflect the controlled pilot operations baseline.</p>
          </div>
        </div>
        <ChecklistTable items={checklistItems} />
      </section>
    </div>
  );
}

function MessageList({ emptyMessage, items, title }) {
  return (
    <section className="dashboard-section">
      <h3>{title}</h3>
      {items.length === 0 ? (
        <p>{emptyMessage}</p>
      ) : (
        <div className="staff-record-list">
          {items.map((item) => (
            <article className="staff-record-card" key={item}>
              <strong>{item}</strong>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function ChecklistTable({ items }) {
  if (items.length === 0) {
    return <p>No checklist items returned yet.</p>;
  }

  return (
    <div className="staff-table-scroll">
      <table className="staff-table">
        <thead>
          <tr>
            <th>Category</th>
            <th>Status</th>
            <th>Checklist Item</th>
            <th>Detail</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={`${item.category}-${item.label}`}>
              <td>{item.category || "readiness"}</td>
              <td>{item.complete ? "complete" : "needs attention"}</td>
              <td>{item.label || "Checklist item"}</td>
              <td>{item.detail || "No detail"}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function formatMetric(key, value) {
  if (key === "profileCompletionRate") {
    return `${Number(value ?? 0).toFixed(1)}%`;
  }
  return value ?? 0;
}

function formatDateTime(value) {
  if (!value) {
    return "Not generated";
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "Not generated" : date.toLocaleString();
}

function statusClass(status) {
  if (status === "ready") {
    return "success";
  }
  if (status === "caution") {
    return "warning";
  }
  return "danger";
}
