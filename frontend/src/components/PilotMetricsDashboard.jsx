import { useEffect, useState } from "react";
import { fetchStaffPilotMetrics } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getStaffPageError, StaffState, SummaryGrid } from "./staffUi.jsx";

export function PilotMetricsDashboard() {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadMetrics() {
      setLoading(true);
      setError("");

      try {
        const data = await fetchStaffPilotMetrics(user);
        if (isActive) {
          setMetrics(data);
        }
      } catch (nextError) {
        if (isActive) {
          setError(getStaffPageError(nextError));
          setMetrics(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadMetrics();

    return () => {
      isActive = false;
    };
  }, [user]);

  if (loading) {
    return <StaffState title="Loading pilot metrics" message="Retrieving pilot data collection metrics." />;
  }

  if (error) {
    return <StaffState title="Pilot metrics unavailable" message={error} />;
  }

  if (!metrics) {
    return <StaffState title="No pilot metrics" message="The backend returned no pilot metrics data." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Pilot Metrics Dashboard</h2>
        <p>Centralized pilot data collection measures for staff review. This is measurement only, not causal evaluation.</p>
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Registration Funnel</h3>
            <p>Participant account and profile completion indicators.</p>
          </div>
        </div>
        <SummaryGrid
          items={[
            { label: "Registrations", value: metrics.totalRegistrations ?? 0 },
            { label: "Active Users", value: metrics.activeUsers ?? 0 },
            { label: "Profile Completions", value: metrics.profileCompletions ?? 0 },
            { label: "Profile Completion", value: formatPercent(metrics.profileCompletionRate) },
            { label: "Active Last 30 Days", value: metrics.activeLast30Days ?? 0 },
            { label: "Active Last 60 Days", value: metrics.activeLast60Days ?? 0 },
            { label: "Active Last 90 Days", value: metrics.activeLast90Days ?? 0 },
          ]}
        />
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Program Engagement</h3>
            <p>Program catalog, enrollment, participation, and attendance indicators.</p>
          </div>
        </div>
        <SummaryGrid
          items={[
            { label: "Total Programs", value: metrics.totalPrograms ?? 0 },
            { label: "Active Programs", value: metrics.activePrograms ?? 0 },
            { label: "Enrollments", value: metrics.totalEnrollments ?? 0 },
            { label: "Active Participants", value: metrics.activeParticipants ?? 0 },
            { label: "Attendance Records", value: metrics.attendanceRecords ?? 0 },
            { label: "Attendance Rate", value: formatPercent(metrics.attendanceRate) },
          ]}
        />
        <GroupedTable title="Participation By Program" values={metrics.programParticipationCounts} />
      </section>

      <section className="staff-management-grid">
        <section className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Credential Engagement</h3>
              <p>Credential definitions and awards by category and program.</p>
            </div>
          </div>
          <SummaryGrid
            items={[
              { label: "Credential Definitions", value: metrics.credentialDefinitions ?? 0 },
              { label: "Active Definitions", value: metrics.activeCredentialDefinitions ?? 0 },
              { label: "Credentials Awarded", value: metrics.credentialsAwarded ?? 0 },
            ]}
          />
          <GroupedTable title="Credentials By Category" values={metrics.credentialsByCategory} />
          <GroupedTable title="Credentials By Program" values={metrics.credentialsByProgram} />
        </section>

        <section className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Service Engagement</h3>
              <p>Service-hour submissions and approved hours.</p>
            </div>
          </div>
          <SummaryGrid
            items={[
              { label: "Submissions", value: metrics.serviceHourSubmissions ?? 0 },
              { label: "Approved Submissions", value: metrics.approvedServiceHourSubmissions ?? 0 },
              { label: "Approved Hours", value: formatNumber(metrics.totalApprovedServiceHours) },
            ]}
          />
          <GroupedTable title="Approved Hours By Program" values={metrics.serviceHoursByProgram} numberFormat="decimal" />
        </section>
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Stakeholder Engagement</h3>
            <p>External relationship records supporting pilot operations.</p>
          </div>
        </div>
        <SummaryGrid
          items={[
            { label: "Educators", value: metrics.educators ?? 0 },
            { label: "Partners", value: metrics.partnerOrganizations ?? 0 },
            { label: "Government Organizations", value: metrics.governmentOrganizations ?? 0 },
            { label: "Relationship Notes", value: metrics.relationshipNotes ?? 0 },
            { label: "Active Relationship Notes", value: metrics.activeRelationshipNotes ?? 0 },
            { label: "Upcoming Follow-Ups", value: metrics.upcomingFollowUps ?? 0 },
            { label: "Overdue Follow-Ups", value: metrics.overdueFollowUps ?? 0 },
          ]}
        />
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Operational Activity</h3>
            <p>Staff operation activity from existing staff operation event tracking.</p>
          </div>
        </div>
        <SummaryGrid
          items={[
            { label: "Operations Last 30 Days", value: metrics.staffOperationsLast30Days ?? 0 },
            { label: "Operations Last 60 Days", value: metrics.staffOperationsLast60Days ?? 0 },
            { label: "Operations Last 90 Days", value: metrics.staffOperationsLast90Days ?? 0 },
          ]}
        />
      </section>
    </div>
  );
}

function GroupedTable({ numberFormat = "integer", title, values = {} }) {
  const rows = Object.entries(values || {});

  return (
    <section className="staff-detail-panel">
      <h3>{title}</h3>
      {rows.length === 0 ? (
        <p>No data available yet.</p>
      ) : (
        <div className="staff-mini-table">
          {rows.map(([label, count]) => (
            <div key={label}>
              <span>{label || "uncategorized"}</span>
              <strong>{numberFormat === "decimal" ? formatNumber(count) : count ?? 0}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function formatPercent(value) {
  return `${Number(value ?? 0).toFixed(1)}%`;
}

function formatNumber(value) {
  return Number(value ?? 0).toFixed(1);
}
