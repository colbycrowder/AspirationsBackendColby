import { useEffect, useState } from "react";
import { fetchStaffOperationsReporting, fetchStaffPilotReporting } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getStaffPageError, StaffState, SummaryGrid } from "./staffUi.jsx";

export function PilotReportingDashboard() {
  const { user } = useAuth();
  const [pilotReport, setPilotReport] = useState(null);
  const [operationsReport, setOperationsReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadReporting() {
      setLoading(true);
      setError("");

      try {
        const [pilotData, operationsData] = await Promise.all([
          fetchStaffPilotReporting(user),
          fetchStaffOperationsReporting(user),
        ]);

        if (isActive) {
          setPilotReport(pilotData);
          setOperationsReport(operationsData);
        }
      } catch (nextError) {
        if (isActive) {
          setError(getStaffPageError(nextError));
          setPilotReport(null);
          setOperationsReport(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadReporting();

    return () => {
      isActive = false;
    };
  }, [user]);

  if (loading) {
    return <StaffState title="Loading reporting" message="Retrieving pilot and staff operations reporting." />;
  }

  if (error) {
    return <StaffState title="Reporting unavailable" message={error} />;
  }

  if (!pilotReport && !operationsReport) {
    return <StaffState title="No reporting data" message="The backend returned no reporting data." />;
  }

  const participation = pilotReport?.participation || {};
  const activeUsers = pilotReport?.activeUsers || {};
  const retention = pilotReport?.retention || {};
  const credentials = pilotReport?.credentials || {};
  const programs = Array.isArray(pilotReport?.programs) ? pilotReport.programs : [];

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Pilot Reporting Dashboard</h2>
        <p>Use Reporting to answer cross-pilot questions about participation, retention, credentials, program outcomes, service, and staff operations in one read-only view.</p>
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Participation Overview</h3>
            <p>Core pilot participation measures from platform data.</p>
          </div>
        </div>
        <SummaryGrid
          items={[
            { label: "Registered Youth", value: participation.totalRegisteredYouth ?? 0 },
            { label: "Profile Completion", value: formatPercent(participation.profileCompletionPercentage) },
            { label: "Program Participation", value: formatPercent(participation.programParticipationPercentage) },
            { label: "Credential Participation", value: formatPercent(participation.credentialParticipationPercentage) },
            { label: "Active Users 30 Days", value: activeUsers.activeUsersLast30Days ?? 0 },
            { label: "Active Users 60 Days", value: activeUsers.activeUsersLast60Days ?? 0 },
            { label: "Active Users 90 Days", value: activeUsers.activeUsersLast90Days ?? 0 },
          ]}
        />
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Retention Overview</h3>
            <p>Retention among participants with platform activity.</p>
          </div>
        </div>
        <SummaryGrid
          items={[
            { label: "Eligible Participants", value: retention.retentionEligibleParticipants ?? 0 },
            { label: "30-Day Retention", value: formatPercent(retention.retention30DayPercentage) },
            { label: "60-Day Retention", value: formatPercent(retention.retention60DayPercentage) },
            { label: "90-Day Retention", value: formatPercent(retention.retention90DayPercentage) },
          ]}
        />
      </section>

      <section className="staff-management-grid">
        <section className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Credentials Analytics</h3>
              <p>Total earned credentials and category breakdown.</p>
            </div>
          </div>
          <SummaryGrid items={[{ label: "Total Credentials Earned", value: credentials.totalCredentialsEarned ?? 0 }]} />
          <GroupedTable title="Credentials By Category" values={credentials.credentialsByCategory} />
        </section>

        <section className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Program Analytics</h3>
              <p>Registrations, credential completions, and active participants by program.</p>
            </div>
          </div>
          <ProgramTable programs={programs} />
        </section>
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Staff Operations Analytics</h3>
            <p>Operational activity from staff/admin event tracking.</p>
          </div>
        </div>
        <SummaryGrid
          items={[
            { label: "Total Operations", value: operationsReport?.totalOperations ?? 0 },
            { label: "Last 30 Days", value: operationsReport?.operationsLast30Days ?? 0 },
            { label: "Last 60 Days", value: operationsReport?.operationsLast60Days ?? 0 },
            { label: "Last 90 Days", value: operationsReport?.operationsLast90Days ?? 0 },
          ]}
        />
      </section>

      <section className="staff-management-grid">
        <GroupedTable title="Operations By Type" values={operationsReport?.operationsByType} />
        <GroupedTable title="Operations By Staff Member" values={operationsReport?.operationsByStaffUser} />
        <GroupedTable title="Operations By Target Type" values={operationsReport?.operationsByTargetType} />
      </section>
    </div>
  );
}

function GroupedTable({ title, values = {} }) {
  const entries = Object.entries(values || {});

  return (
    <section className="dashboard-section">
      <h3>{title}</h3>
      {entries.length === 0 ? (
        <p>No data available yet.</p>
      ) : (
        <div className="staff-mini-table">
          {entries.map(([label, count]) => (
            <div key={label}>
              <span>{label}</span>
              <strong>{count}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function ProgramTable({ programs }) {
  if (programs.length === 0) {
    return <p>No program reporting data is available yet.</p>;
  }

  return (
    <div className="staff-mini-table">
      {programs.map((program) => (
        <div key={program.programId || program.programName}>
          <span>
            <strong>{program.programName || "Untitled program"}</strong>
            <small>
              {program.programStatus || "status missing"} · {program.category || "uncategorized"}
            </small>
          </span>
          <span>
            {program.activeParticipants ?? 0} active · {program.registrations ?? 0} registrations ·{" "}
            {program.credentialCompletions ?? 0} credentials
          </span>
        </div>
      ))}
    </div>
  );
}

function formatPercent(value) {
  const numericValue = Number(value ?? 0);
  return `${Number.isFinite(numericValue) ? numericValue.toFixed(1) : "0.0"}%`;
}
