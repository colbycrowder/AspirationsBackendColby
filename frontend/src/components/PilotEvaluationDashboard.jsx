import { useEffect, useState } from "react";
import { fetchStaffPilotEvaluation } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getStaffPageError, StaffState, SummaryGrid } from "./staffUi.jsx";

const outcomeCards = [
  ["youthOutcomeScore", "Youth Outcomes", "registrations", "active30DayUsers", "retentionRate"],
  ["programOutcomeScore", "Program Outcomes", "enrollments", "activeEnrollments", "attendanceRate"],
  ["credentialOutcomeScore", "Credential Outcomes", "credentialsAwarded", "credentialsPerParticipant", "credentialParticipationRate"],
  ["serviceOutcomeScore", "Service Outcomes", "approvedServiceHours", "serviceHourParticipants", "averageHoursPerParticipant"],
  ["stakeholderOutcomeScore", "Stakeholder Outcomes", "educatorCount", "partnerCount", "relationshipFollowUpCompletionRate"],
  ["operationsOutcomeScore", "Operations Outcomes", "staffActionsLast30Days", "platformEventsLast30Days", "relationshipNoteActivity"],
];

const metricLabels = {
  registrations: "Registrations",
  active30DayUsers: "30-Day Active",
  retentionRate: "Retention",
  enrollments: "Enrollments",
  activeEnrollments: "Active Enrollments",
  attendanceRate: "Attendance",
  credentialsAwarded: "Awarded",
  credentialsPerParticipant: "Per Participant",
  credentialParticipationRate: "Participation",
  approvedServiceHours: "Approved Hours",
  serviceHourParticipants: "Participants",
  averageHoursPerParticipant: "Avg Hours",
  educatorCount: "Educators",
  partnerCount: "Partners",
  relationshipFollowUpCompletionRate: "Follow-Up Completion",
  staffActionsLast30Days: "Staff Actions 30 Days",
  platformEventsLast30Days: "Platform Events 30 Days",
  relationshipNoteActivity: "Active Notes",
};

export function PilotEvaluationDashboard() {
  const { user } = useAuth();
  const [evaluation, setEvaluation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadEvaluation() {
      setLoading(true);
      setError("");

      try {
        const data = await fetchStaffPilotEvaluation(user);
        if (isActive) {
          setEvaluation(data);
        }
      } catch (nextError) {
        if (isActive) {
          setError(getStaffPageError(nextError));
          setEvaluation(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadEvaluation();

    return () => {
      isActive = false;
    };
  }, [user]);

  if (loading) {
    return <StaffState title="Loading pilot evaluation" message="Retrieving pilot outcome evaluation metrics." />;
  }

  if (error) {
    return <StaffState title="Pilot evaluation unavailable" message={error} />;
  }

  if (!evaluation) {
    return <StaffState title="No pilot evaluation" message="The backend returned no pilot evaluation data." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Pilot Evaluation Dashboard</h2>
        <p>Use Pilot Evaluation to interpret pilot effectiveness, outcome scores, strengths, concerns, and recommended actions. This is descriptive evaluation support, not causal analysis.</p>
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Executive Summary</h3>
            <p>Overall status reflects the six pilot outcome categories.</p>
          </div>
          <span className={`status-pill ${statusClass(evaluation.overallStatus)}`}>
            {evaluation.overallStatus || "unknown"}
          </span>
        </div>
        <SummaryGrid
          items={[
            { label: "Overall Score", value: `${evaluation.overallScore ?? 0}/100` },
            { label: "Overall Status", value: evaluation.overallStatus || "unknown" },
            { label: "Generated", value: formatDateTime(evaluation.generatedAt) },
          ]}
        />
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Outcome Cards</h3>
            <p>Each card summarizes one pilot performance domain.</p>
          </div>
        </div>
        <div className="credential-grid">
          {outcomeCards.map(([scoreKey, title, firstMetric, secondMetric, thirdMetric]) => (
            <article className="credential-card" key={scoreKey}>
              <div className="credential-icon">{evaluation[scoreKey] ?? 0}</div>
              <div>
                <h3>{title}</h3>
                <p>Score: {evaluation[scoreKey] ?? 0}/100</p>
                <small>{metricLabels[firstMetric]}: {formatMetric(firstMetric, evaluation[firstMetric])}</small>
                <small>{metricLabels[secondMetric]}: {formatMetric(secondMetric, evaluation[secondMetric])}</small>
                <small>{metricLabels[thirdMetric]}: {formatMetric(thirdMetric, evaluation[thirdMetric])}</small>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="staff-management-grid">
        <NarrativeList title="Strengths" emptyMessage="No strengths identified yet." items={evaluation.strengths} />
        <NarrativeList title="Concerns" emptyMessage="No concerns identified yet." items={evaluation.concerns} />
      </section>

      <NarrativeList
        title="Recommended Actions"
        emptyMessage="No recommended actions returned yet."
        items={evaluation.recommendedActions}
      />
    </div>
  );
}

function NarrativeList({ emptyMessage, items = [], title }) {
  const rows = Array.isArray(items) ? items : [];

  return (
    <section className="dashboard-section">
      <h3>{title}</h3>
      {rows.length === 0 ? (
        <p>{emptyMessage}</p>
      ) : (
        <div className="staff-record-list">
          {rows.map((item) => (
            <article className="staff-record-card" key={item}>
              <strong>{item}</strong>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function formatMetric(key, value) {
  if (key.toLowerCase().includes("rate") || key.toLowerCase().includes("participation")) {
    return `${Number(value ?? 0).toFixed(1)}%`;
  }
  if (key.toLowerCase().includes("hours") || key === "credentialsPerParticipant") {
    return Number(value ?? 0).toFixed(2);
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
  if (status === "green") {
    return "success";
  }
  if (status === "yellow") {
    return "warning";
  }
  return "danger";
}
