import { useEffect, useState } from "react";
import { fetchYouthDashboard, trackPlatformEvent } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

export function YouthDashboard({ navigate }) {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadDashboard() {
      setLoading(true);
      setError("");

      try {
        const data = await fetchYouthDashboard(user);
        if (isActive) {
          setDashboard(data);
          trackPlatformEvent(user, "DASHBOARD_VIEW").catch(() => {});
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

    loadDashboard();

    return () => {
      isActive = false;
    };
  }, [user]);

  if (loading) {
    return <DashboardState title="Loading dashboard" message="Retrieving your ASPN dashboard." />;
  }

  if (error) {
    return (
      <DashboardState
        title="Dashboard unavailable"
        message={error}
        note="If this is a new Firebase account, ASPN may still need to create the matching Firestore profile document."
      />
    );
  }

  return (
    <div className="dashboard-stack">
      <ProfileSummary profile={dashboard?.profileSummary} unreadCount={dashboard?.unreadNotificationCount} />

      <GuidanceGrid dashboard={dashboard} navigate={navigate} />

      <DashboardSection title="Programs">
        <ProgramList programs={dashboard?.programs} />
      </DashboardSection>

      <DashboardSection title="Earned Credentials">
        <CredentialList credentials={dashboard?.earnedCredentials} emptyText="No earned credentials yet." />
      </DashboardSection>

      <DashboardSection title="Available Credentials">
        <CredentialList
          credentials={dashboard?.availableCredentials}
          emptyText="No available credentials for enrolled programs yet."
          showRequirement
        />
      </DashboardSection>

      <DashboardSection title="Attendance">
        <RecordList
          records={dashboard?.attendanceRecords}
          emptyText="No attendance records yet."
          renderRecord={(record) => (
            <>
              <strong>{record.eventName || "Attendance record"}</strong>
              <span>{formatDate(record.eventDate)} · {record.attendanceStatus || "status unavailable"}</span>
            </>
          )}
        />
      </DashboardSection>

      <DashboardSection title="Service Hours">
        <RecordList
          records={dashboard?.serviceHourRecords}
          emptyText="No service-hour records yet."
          renderRecord={(record) => (
            <>
              <strong>{record.description || "Service-hour record"}</strong>
              <span>{formatDate(record.serviceDate)} · {record.hours ?? 0} hours · {record.verificationStatus || "pending"}</span>
            </>
          )}
        />
        {dashboard?.serviceHourRequestFormUrl ? (
          <a className="inline-link" href={dashboard.serviceHourRequestFormUrl} target="_blank" rel="noreferrer">
            Request service-hour verification
          </a>
        ) : (
          <p className="empty-text">Service-hour request link is not configured yet.</p>
        )}
      </DashboardSection>

      <DashboardSection title="RWD Learning Center">
        <RecordList
          records={dashboard?.rwdLearningCenter}
          emptyText="No RWD activities are available yet."
          renderRecord={(item) => (
            <>
              <strong>{item.title || item.countryName || "RWD activity"}</strong>
              <span>{item.completionStatus || "not_started"} · {item.passed ? "passed" : "not passed"}</span>
              {item.externalUrl ? (
                <a className="inline-link" href={item.externalUrl} target="_blank" rel="noreferrer">
                  Watch activity
                </a>
              ) : null}
            </>
          )}
        />
      </DashboardSection>

      <DashboardSection title="Opportunities">
        <RecordList
          records={dashboard?.opportunities}
          emptyText="No opportunity links are available yet."
          renderRecord={(item) => (
            <>
              <strong>{item.title || "Opportunity"}</strong>
              {item.url ? (
                <a className="inline-link" href={item.url} target="_blank" rel="noreferrer">
                  {item.url}
                </a>
              ) : (
                <span>Link unavailable</span>
              )}
            </>
          )}
        />
      </DashboardSection>
    </div>
  );
}

function GuidanceGrid({ dashboard, navigate }) {
  const profile = dashboard?.profileSummary || {};
  const earnedCredentials = asArray(dashboard?.earnedCredentials);
  const availableCredentials = asArray(dashboard?.availableCredentials);
  const serviceHourRecords = asArray(dashboard?.serviceHourRecords);
  const rwdItems = asArray(dashboard?.rwdLearningCenter);
  const programs = asArray(dashboard?.programs);
  const completion = getProfileCompletion(profile);
  const serviceSummary = getServiceHourSummary(serviceHourRecords);
  const rwdSummary = getRwdSummary(rwdItems);

  return (
    <>
      <section className="dashboard-guidance-grid" aria-label="Dashboard guidance">
        <article className="guidance-card">
          <div className="guidance-card-header">
            <div>
              <span className="eyebrow">Profile</span>
              <h3>Profile Completion</h3>
            </div>
            <strong>{completion.percent}%</strong>
          </div>
          <div className="progress-track" aria-label={`Profile completion ${completion.percent} percent`}>
            <span style={{ width: `${completion.percent}%` }} />
          </div>
          {completion.missing.length ? (
            <p>Missing: {completion.missing.join(", ")}</p>
          ) : (
            <p>Your basic profile is ready for onboarding.</p>
          )}
          <button className="text-action" type="button" onClick={() => navigate("/profile")}>
            Open Profile
          </button>
        </article>

        <article className="guidance-card">
          <span className="eyebrow">Next Actions</span>
          <h3>Keep Moving</h3>
          <div className="next-action-list">
            {getNextActions({ completion, programs, rwdItems, serviceHourRecords, earnedCredentials }).map((action) => (
              <button className="next-action-button" key={action.path} type="button" onClick={() => navigate(action.path)}>
                <strong>{action.label}</strong>
                <span>{action.note}</span>
              </button>
            ))}
          </div>
        </article>
      </section>

      <section className="dashboard-summary-grid" aria-label="Dashboard summary">
        <SummaryTile label="Earned Credentials" value={earnedCredentials.length} />
        <SummaryTile label="Available Credentials" value={availableCredentials.length} />
        <SummaryTile label="Approved Hours" value={formatHours(serviceSummary.approved)} />
        <SummaryTile label="Pending Hours" value={formatHours(serviceSummary.pending)} />
        <SummaryTile label="RWD Complete" value={rwdSummary.completed} />
        <SummaryTile label="RWD Remaining" value={rwdSummary.remaining} />
        <SummaryTile label="Unread Notices" value={dashboard?.unreadNotificationCount ?? 0} />
      </section>
    </>
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

function ProfileSummary({ profile, unreadCount }) {
  const name = [profile?.firstName, profile?.lastName].filter(Boolean).join(" ") || "Youth Profile";
  const firstInitial = profile?.firstInitial || name.charAt(0).toUpperCase() || "A";

  return (
    <section className="dashboard-hero">
      <div className="profile-avatar">
        {profile?.profileImageUrl ? (
          <img src={profile.profileImageUrl} alt="" />
        ) : (
          <span>{firstInitial}</span>
        )}
      </div>
      <div>
        <span className="eyebrow">Profile Summary</span>
        <h2>{name}</h2>
        <p>{profile?.email || "Email unavailable"}</p>
        <div className="summary-chips">
          <span>{profile?.profileStatus || "profile status unavailable"}</span>
          <span>{profile?.aspnParticipantId || "ASPN ID pending"}</span>
          <span>{profile?.school || "school not added"}</span>
          <span>{profile?.graduationYear || "graduation year not added"}</span>
          <span>{unreadCount ?? 0} unread notifications</span>
        </div>
      </div>
    </section>
  );
}

function DashboardSection({ title, children }) {
  return (
    <section className="dashboard-section">
      <h3>{title}</h3>
      {children}
    </section>
  );
}

function ProgramList({ programs }) {
  const safePrograms = asArray(programs);

  if (!safePrograms.length) {
    return <p className="empty-text">No active enrolled programs yet.</p>;
  }

  return (
    <div className="dashboard-card-grid">
      {safePrograms.map((program) => (
        <article className="dashboard-card" key={program.programId || program.programName}>
          <strong>{program.programName || "Program"}</strong>
          <span>{program.category || "category unavailable"}</span>
          <p>{program.description || "No program description yet."}</p>
          <small>{program.programLeader || "Program leader unavailable"}</small>
        </article>
      ))}
    </div>
  );
}

function CredentialList({ credentials, emptyText, showRequirement = false }) {
  const safeCredentials = asArray(credentials);

  if (!safeCredentials.length) {
    return <p className="empty-text">{emptyText}</p>;
  }

  return (
    <div className="dashboard-card-grid">
      {safeCredentials.map((credential) => (
        <article className="dashboard-card" key={credential.earnedCredentialID || credential.credentialID}>
          <strong>{credential.credentialName || "Credential"}</strong>
          <span>{credential.category || "category unavailable"}</span>
          <p>{credential.description || "No credential description yet."}</p>
          {showRequirement ? (
            <small>{credential.requirementText || credential.status || "Requirement details unavailable"}</small>
          ) : (
            <small>{formatDate(credential.awardedAt || credential.earnedAt)}</small>
          )}
        </article>
      ))}
    </div>
  );
}

function RecordList({ records, emptyText, renderRecord }) {
  const safeRecords = asArray(records);

  if (!safeRecords.length) {
    return <p className="empty-text">{emptyText}</p>;
  }

  return (
    <div className="record-list">
      {safeRecords.map((record, index) => (
        <article className="record-row" key={record.id || record.recordId || record.progressId || index}>
          {renderRecord(record)}
        </article>
      ))}
    </div>
  );
}

function getProfileCompletion(profile) {
  const checks = [
    { label: "first name", complete: hasText(profile.firstName) },
    { label: "last name", complete: hasText(profile.lastName) },
    { label: "email", complete: hasText(profile.email) },
    { label: "school", complete: hasText(profile.school) },
    { label: "graduation year", complete: hasText(profile.graduationYear) },
  ];
  const completed = checks.filter((check) => check.complete).length;
  return {
    missing: checks.filter((check) => !check.complete).map((check) => check.label),
    percent: Math.round((completed / checks.length) * 100),
  };
}

function getNextActions({ completion, programs, rwdItems, serviceHourRecords, earnedCredentials }) {
  const actions = [];

  if (completion.percent < 100) {
    actions.push({
      label: "Complete profile",
      note: "Add school and graduation year.",
      path: "/profile",
    });
  }

  if (!programs.length) {
    actions.push({
      label: "Join a program",
      note: "Enroll in an active ASPN program.",
      path: "/programs",
    });
  }

  if (getRwdSummary(rwdItems).remaining > 0) {
    actions.push({
      label: "Start RWD activity",
      note: "Open the RWD Learning Center.",
      path: "/rwd-learning-center",
    });
  }

  if (!serviceHourRecords.length) {
    actions.push({
      label: "Submit service hours",
      note: "Use the current request link.",
      path: "/service-hours",
    });
  }

  actions.push({
    label: "View credentials",
    note: earnedCredentials.length ? "Review earned credentials." : "See available credentials.",
    path: "/credentials",
  });

  return actions.slice(0, 5);
}

function getServiceHourSummary(records) {
  return records.reduce(
    (summary, record) => {
      const status = (record.verificationStatus || "pending").toLowerCase();
      const hours = Number(record.hours || 0);
      if (status === "verified") {
        summary.approved += hours;
      }
      if (status === "pending") {
        summary.pending += hours;
      }
      return summary;
    },
    { approved: 0, pending: 0 }
  );
}

function getRwdSummary(items) {
  const completed = items.filter((item) => item?.progress?.completionStatus === "completed").length;
  return {
    completed,
    remaining: Math.max(items.length - completed, 0),
  };
}

function hasText(value) {
  return typeof value === "string" && value.trim().length > 0;
}

function formatHours(value) {
  const hours = Number(value || 0);
  return Number.isInteger(hours) ? String(hours) : hours.toFixed(1);
}

function DashboardState({ title, message, note }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {note ? <p>{note}</p> : null}
    </section>
  );
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
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
