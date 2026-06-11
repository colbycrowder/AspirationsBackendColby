import { useEffect, useState } from "react";
import { fetchYouthDashboard } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

export function YouthDashboard() {
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
