import { useEffect, useMemo, useState } from "react";
import { fetchYouthDashboard } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

export function CredentialsPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadCredentials() {
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

    loadCredentials();

    return () => {
      isActive = false;
    };
  }, [user]);

  const earnedCredentials = useMemo(
    () => asArray(dashboard?.earnedCredentials).sort(compareCredentialNames),
    [dashboard]
  );
  const availableCredentials = useMemo(
    () => asArray(dashboard?.availableCredentials).sort(compareCredentialNames),
    [dashboard]
  );

  if (loading) {
    return <CredentialsState title="Loading credentials" message="Retrieving your ASPN credentials." />;
  }

  if (error) {
    return (
      <CredentialsState
        title="Credentials unavailable"
        message={error}
        note="If this is a new Firebase account, ASPN may still need to create the matching Firestore profile document."
      />
    );
  }

  return (
    <div className="credentials-stack">
      <section className="page-intro">
        <span className="eyebrow">Youth Credentials</span>
        <h2>Credential Wallet</h2>
        <p>
          Review credentials you have earned and credentials connected to your active enrolled programs.
        </p>
      </section>

      <section className="credential-summary-grid" aria-label="Credential summary">
        <SummaryTile label="Earned" value={earnedCredentials.length} />
        <SummaryTile label="Available" value={availableCredentials.length} />
      </section>

      <CredentialSection
        title="Earned Credentials"
        credentials={earnedCredentials}
        emptyText="No earned credentials yet."
        type="earned"
      />

      <CredentialSection
        title="Available Credentials"
        credentials={availableCredentials}
        emptyText="No available credentials for enrolled programs yet."
        type="available"
      />
    </div>
  );
}

function CredentialSection({ title, credentials, emptyText, type }) {
  return (
    <section className="dashboard-section">
      <h3>{title}</h3>
      {!credentials.length ? (
        <p className="empty-text">{emptyText}</p>
      ) : (
        <div className="credential-grid">
          {credentials.map((credential) => (
            <CredentialCard credential={credential} key={getCredentialKey(credential)} type={type} />
          ))}
        </div>
      )}
    </section>
  );
}

function CredentialCard({ credential, type }) {
  const isEarned = type === "earned";
  const icon = credential.icon || "A";

  return (
    <article className="credential-card">
      <div className="credential-icon" aria-hidden="true">
        {icon}
      </div>
      <div className="credential-content">
        <div className="credential-card-header">
          <strong>{credential.credentialName || "Credential"}</strong>
          <span className={isEarned ? "status-tag enrolled" : "status-tag muted"}>
            {isEarned ? credential.status || "earned" : credential.status || "locked"}
          </span>
        </div>
        <span>{credential.category || "category unavailable"}</span>
        <p>{credential.description || "No credential description yet."}</p>
        {isEarned ? (
          <small>{formatAwardDate(credential.awardedAt || credential.earnedAt)}</small>
        ) : (
          <small>{credential.requirementText || "Requirement details are not available yet."}</small>
        )}
      </div>
    </article>
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

function CredentialsState({ title, message, note }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {note ? <p>{note}</p> : null}
    </section>
  );
}

function compareCredentialNames(first, second) {
  return getCredentialName(first).localeCompare(getCredentialName(second));
}

function getCredentialName(credential) {
  return credential?.credentialName || "Credential";
}

function getCredentialKey(credential) {
  return credential.earnedCredentialID || credential.credentialID || getCredentialName(credential);
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}

function formatAwardDate(value) {
  if (!value) {
    return "Award date unavailable";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return `Awarded ${date.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  })}`;
}
