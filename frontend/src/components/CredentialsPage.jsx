import { useEffect, useMemo, useState } from "react";
import { fetchYouthDashboard } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { CredentialIcon } from "./CredentialIcon.jsx";
import {
  aspnEcosystemRegistry,
  findAspnRegistryCredentialMatch,
} from "../data/aspnEcosystemRegistry.js";
import { getUniqueEarnedCredentials } from "../utils/credentialDeduplication.js";

export function CredentialsPage({ navigate }) {
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

  const earnedCredentials = useMemo(() => getUniqueEarnedCredentials(dashboard?.earnedCredentials), [dashboard]);
  const earnedRegistryMatches = useMemo(() => buildEarnedRegistryMatches(earnedCredentials), [earnedCredentials]);
  const unmatchedEarnedCredentials = useMemo(
    () => earnedCredentials.filter((credential) => !findAspnRegistryCredentialMatch(credential)),
    [earnedCredentials]
  );

  if (loading) {
    return <CredentialExplorerState title="Loading Credential Explorer" message="Retrieving your credentials and ASPN credential pathways." />;
  }

  if (error) {
    return (
      <CredentialExplorerState
        title="Credential Explorer unavailable"
        message={error}
        note="If this is a new Firebase account, complete your ASPN profile before opening the Credential Explorer."
      />
    );
  }

  return (
    <div className="credential-explorer-page">
      <section className="credential-explorer-hero">
        <span className="credential-explorer-kicker">Build your civic skills</span>
        <h1>Credential Explorer</h1>
        <p>Explore the skills, experiences, and civic pathways that ASPN credentials represent.</p>
      </section>

      <section className="credential-explorer-summary" aria-label="Earned credential summary">
        <strong>{earnedCredentials.length}</strong>
        <div>
          <h2>{earnedCredentials.length === 1 ? "Credential Earned" : "Credentials Earned"}</h2>
          <p>
            {earnedCredentials.length
              ? "Your earned credentials are highlighted throughout the explorer."
              : "Participate in programs and learning experiences to begin earning credentials."}
          </p>
        </div>
      </section>

      <CredentialCatalogSection
        credentials={aspnEcosystemRegistry.credentials.core}
        earnedRegistryMatches={earnedRegistryMatches}
        eyebrow="Foundational skills"
        navigate={navigate}
        title="Core Credentials"
      />

      <CredentialCatalogSection
        credentials={aspnEcosystemRegistry.credentials.advanced}
        earnedRegistryMatches={earnedRegistryMatches}
        eyebrow="Build deeper expertise"
        navigate={navigate}
        title="Advanced Credentials"
      />

      {unmatchedEarnedCredentials.length ? (
        <section className="credential-explorer-section" aria-labelledby="unmatched-credentials-title">
          <div className="credential-explorer-heading">
            <div>
              <span className="credential-explorer-kicker">Earned recognition</span>
              <h2 id="unmatched-credentials-title">Additional Earned Credentials</h2>
            </div>
          </div>
          <p className="credential-explorer-note">These credentials are earned, but their ecosystem registry connections are not available yet.</p>
          <div className="credential-unmatched-list">
            {unmatchedEarnedCredentials.map((credential, index) => (
              <article key={credential.earnedCredentialID || credential.credentialID || index}>
                <CredentialIcon name={credential.credentialName} />
                <div>
                  <strong>{credential.credentialName || "ASPN Credential"}</strong>
                  <span>Connection not available yet</span>
                </div>
              </article>
            ))}
          </div>
        </section>
      ) : null}
    </div>
  );
}

function CredentialCatalogSection({ credentials, earnedRegistryMatches, eyebrow, navigate, title }) {
  const sectionId = `${title.toLowerCase().replaceAll(" ", "-")}-title`;
  return (
    <section className="credential-explorer-section" aria-labelledby={sectionId}>
      <div className="credential-explorer-heading">
        <div>
          <span className="credential-explorer-kicker">{eyebrow}</span>
          <h2 id={sectionId}>{title}</h2>
        </div>
        <span className="credential-explorer-count">{credentials.length} credentials</span>
      </div>
      <div className="credential-explorer-grid">
        {credentials.map((credential) => (
          <CredentialExplorerCard
            credential={credential}
            earnedCredential={earnedRegistryMatches.get(credential.id) || null}
            key={credential.id}
            navigate={navigate}
          />
        ))}
      </div>
    </section>
  );
}

function CredentialExplorerCard({ credential, earnedCredential, navigate }) {
  const pathways = credential.pathwayIds
    .map((pathwayId) => aspnEcosystemRegistry.pathways.find((pathway) => pathway.id === pathwayId))
    .filter(Boolean);
  const programs = aspnEcosystemRegistry.programs.filter((program) => program.credentialIds.includes(credential.id));
  const roleFamilies = [...new Set(pathways.flatMap((pathway) => pathway.exampleCivicRoleFamilies))].slice(0, 6);
  const isEarned = Boolean(earnedCredential);

  return (
    <article className={isEarned ? "credential-explorer-card earned" : "credential-explorer-card"}>
      <div className="credential-explorer-card-header">
        <CredentialIcon credential={credential} />
        <div>
          <span className="credential-tier">{credential.type === "advanced" ? "Advanced" : "Core"}</span>
          <h3>{credential.name}</h3>
        </div>
        <span className={isEarned ? "credential-earned-state earned" : "credential-earned-state"}>
          {isEarned ? "Earned" : "Explore"}
        </span>
      </div>

      <p>{credential.description}</p>

      <CredentialContext label={pathways.length === 1 ? "Related pathway" : "Related pathways"} items={pathways.map((pathway) => pathway.name)} />
      <CredentialContext label="Related programs" items={programs.map((program) => program.name)} emptyText="No related programs listed yet" />
      <CredentialContext label="Example civic role families" items={roleFamilies} />

      {isEarned ? (
        <small className="credential-award-date">{formatAwardDate(earnedCredential.awardedAt || earnedCredential.earnedAt)}</small>
      ) : (
        <small className="credential-explore-guidance">Explore related programs and learning experiences to understand how this credential can be earned.</small>
      )}
      <button className="credential-detail-link" type="button" onClick={() => navigate(`/credentials/${encodeURIComponent(credential.id)}`)}>
        View credential details
      </button>
    </article>
  );
}

function CredentialContext({ emptyText, items, label }) {
  const uniqueItems = [...new Set(items.filter(Boolean))];
  return (
    <div className="credential-context">
      <span>{label}</span>
      {uniqueItems.length ? (
        <div>{uniqueItems.map((item) => <small key={item}>{item}</small>)}</div>
      ) : (
        <p>{emptyText}</p>
      )}
    </div>
  );
}

function CredentialExplorerState({ title, message, note }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {note ? <p>{note}</p> : null}
    </section>
  );
}

function buildEarnedRegistryMatches(earnedCredentials) {
  const matches = new Map();
  for (const earnedCredential of earnedCredentials) {
    const registryCredential = findAspnRegistryCredentialMatch(earnedCredential);
    if (registryCredential && !matches.has(registryCredential.id)) {
      matches.set(registryCredential.id, earnedCredential);
    }
  }
  return matches;
}

function formatAwardDate(value) {
  if (!value) return "Award date unavailable";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return `Awarded ${date.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" })}`;
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}
