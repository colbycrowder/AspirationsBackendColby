import { useEffect, useMemo, useState } from "react";
import { fetchYouthDashboard } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { CredentialIcon } from "./CredentialIcon.jsx";
import {
  aspnEcosystemRegistry,
  findAspnRegistryCredentialMatch,
  getAspnRegistryCredentialById,
} from "../data/aspnEcosystemRegistry.js";
import { getUniqueEarnedCredentials } from "../utils/credentialDeduplication.js";

export function CredentialDetailPage({ credentialId, navigate }) {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadCredentialDetail() {
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

    loadCredentialDetail();
    return () => {
      isActive = false;
    };
  }, [user]);

  const earnedCredentials = useMemo(() => getUniqueEarnedCredentials(dashboard?.earnedCredentials), [dashboard]);
  const availableCredentials = useMemo(() => asArray(dashboard?.availableCredentials), [dashboard]);
  const detail = useMemo(
    () => buildCredentialDetail(credentialId, earnedCredentials, availableCredentials),
    [availableCredentials, credentialId, earnedCredentials]
  );

  if (loading) {
    return <CredentialDetailState title="Loading Credential" message="Retrieving credential details and your earned status." />;
  }

  if (error) {
    return <CredentialDetailState title="Credential unavailable" message={error} />;
  }

  if (!detail.credential) {
    return (
      <CredentialDetailState
        title="Credential not found"
        message="This credential is not available in the ASPN credential registry yet."
      >
        <button className="credential-detail-secondary-action" type="button" onClick={() => navigate("/credentials")}>
          Back to Credential Explorer
        </button>
      </CredentialDetailState>
    );
  }

  return (
    <div className="credential-detail-page">
      <section className="credential-detail-hero">
        <button className="credential-detail-back" type="button" onClick={() => navigate("/credentials")}>
          ← Credential Explorer
        </button>
        <div className="credential-detail-hero-content">
          <CredentialIcon credential={detail.credential} size="journey" />
          <div>
            <span className="credential-detail-kicker">{detail.category}</span>
            <h1>{detail.credential.name}</h1>
            <p>{detail.credential.description}</p>
          </div>
          <span className={detail.isEarned ? "credential-detail-status earned" : "credential-detail-status"}>
            {detail.isEarned ? "Earned" : "Not Yet Earned"}
          </span>
        </div>
      </section>

      <section className={detail.isEarned ? "credential-detail-callout earned" : "credential-detail-callout"}>
        <strong>{detail.isEarned ? "Credential earned and added to your ASPN Journey." : "Participate in related programs to earn this credential."}</strong>
        {detail.isEarned ? <span>{formatAwardDate(detail.earnedCredential?.awardedAt || detail.earnedCredential?.earnedAt)}</span> : null}
      </section>

      <section className="credential-detail-grid" aria-label="Credential details">
        <CredentialDetailPanel title="Requirements">
          <p>{detail.requirements}</p>
        </CredentialDetailPanel>

        <CredentialDetailPanel title={detail.pathways.length === 1 ? "Related Pathway" : "Related Pathways"}>
          <TagList items={detail.pathways.map((pathway) => pathway.name)} emptyText="No related pathway listed yet." />
        </CredentialDetailPanel>

        <CredentialDetailPanel title="Related Programs">
          <TagList items={detail.programs.map((program) => program.name)} emptyText="No related programs listed yet." />
        </CredentialDetailPanel>

        <CredentialDetailPanel title="Example Civic Role Families">
          <TagList items={detail.roleFamilies} emptyText="No civic role families listed yet." />
        </CredentialDetailPanel>
      </section>
    </div>
  );
}

function CredentialDetailPanel({ children, title }) {
  return (
    <article className="credential-detail-panel">
      <h2>{title}</h2>
      {children}
    </article>
  );
}

function TagList({ emptyText, items }) {
  const uniqueItems = [...new Set(asArray(items).filter(Boolean))];
  if (!uniqueItems.length) {
    return <p>{emptyText}</p>;
  }

  return (
    <ul className="credential-detail-tags">
      {uniqueItems.map((item) => <li key={item}>{item}</li>)}
    </ul>
  );
}

function CredentialDetailState({ children, message, title }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {children}
    </section>
  );
}

function buildCredentialDetail(credentialId, earnedCredentials, availableCredentials) {
  const earnedCredential = asArray(earnedCredentials).find((credential) => {
    const registryMatch = findAspnRegistryCredentialMatch(credential);
    return registryMatch?.id === credentialId
      || credential.credentialID === credentialId
      || credential.credentialId === credentialId
      || credential.id === credentialId;
  });
  const availableCredential = asArray(availableCredentials).find((credential) => {
    const registryMatch = findAspnRegistryCredentialMatch(credential);
    return registryMatch?.id === credentialId
      || credential.credentialID === credentialId
      || credential.credentialId === credentialId
      || credential.id === credentialId;
  });
  const credential = getAspnRegistryCredentialById(credentialId)
    || (earnedCredential ? findAspnRegistryCredentialMatch(earnedCredential) : null);

  if (!credential) {
    return { credential: null };
  }

  const pathways = credential.pathwayIds
    .map((pathwayId) => aspnEcosystemRegistry.pathways.find((pathway) => pathway.id === pathwayId))
    .filter(Boolean);
  const programs = aspnEcosystemRegistry.programs.filter((program) => program.credentialIds.includes(credential.id));
  const roleFamilies = [...new Set(pathways.flatMap((pathway) => pathway.exampleCivicRoleFamilies))];

  return {
    category: formatCredentialType(credential.type),
    credential,
    earnedCredential,
    isEarned: Boolean(earnedCredential),
    pathways,
    programs,
    requirements: availableCredential?.requirementText
      || earnedCredential?.requirementText
      || credential.requirementText
      || "Complete related ASPN programs, learning experiences, or staff-reviewed requirements to earn this credential.",
    roleFamilies,
  };
}

function formatCredentialType(type) {
  if (type === "advanced") {
    return "Advanced Credential";
  }

  return "Core Credential";
}

function formatAwardDate(value) {
  if (!value) {
    return "Award date unavailable";
  }

  const normalizedValue = typeof value === "object" && value.seconds
    ? value.seconds * 1000
    : value;
  const date = new Date(normalizedValue);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return `Awarded ${date.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" })}`;
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}
