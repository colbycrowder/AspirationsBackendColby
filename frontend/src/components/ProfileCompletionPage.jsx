import { useEffect, useState } from "react";
import { fetchMyProfile, fetchYouthDashboard, saveMyProfile } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { CredentialIcon } from "./CredentialIcon.jsx";
import {
  aspnEcosystemRegistry,
  findAspnRegistryCredentialMatch,
  getAspnRegistryCredentials,
} from "../data/aspnEcosystemRegistry.js";
import { getUniqueEarnedCredentials } from "../utils/credentialDeduplication.js";

const interestFields = [
  {
    helperText: "Fields of study you'd like to pursue.",
    key: "educationInterests",
    label: "Education Interests",
  },
  {
    helperText: "Careers you'd like to explore.",
    key: "careerInterests",
    label: "Career Interests",
  },
  {
    helperText: "Issues and causes you care about.",
    key: "communityInterests",
    label: "Community Interests",
  },
];

const initialForm = {
  firstName: "",
  lastName: "",
  email: "",
  school: "",
  graduationYear: "",
  educationInterests: "",
  careerInterests: "",
  communityInterests: "",
};

export function ProfileCompletionPage({ navigate }) {
  const { user } = useAuth();
  const [form, setForm] = useState(initialForm);
  const [profileStatus, setProfileStatus] = useState("pending_onboarding");
  const [aspnParticipantId, setAspnParticipantId] = useState("");
  const [earnedCredentials, setEarnedCredentials] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadProfile() {
      setLoading(true);
      setError("");
      setMessage("");

      try {
        const profile = await fetchMyProfile(user);
        const dashboard = await fetchYouthDashboard(user).catch(() => null);
        if (!isActive) {
          return;
        }

        if (profile?.user) {
          setForm(toForm(profile.user, user));
          setProfileStatus(profile.user.profileStatus || "pending_onboarding");
          setAspnParticipantId(profile.user.aspnParticipantId || "");
        } else {
          setForm({ ...initialForm, email: user?.email || "" });
          setProfileStatus("pending_onboarding");
          setAspnParticipantId("");
        }
        setEarnedCredentials(asArray(dashboard?.earnedCredentials));
      } catch (nextError) {
        if (isActive) {
          setError(nextError.message);
          setForm({ ...initialForm, email: user?.email || "" });
          setEarnedCredentials([]);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadProfile();

    return () => {
      isActive = false;
    };
  }, [user]);

  function updateField(fieldName, value) {
    setForm((currentForm) => ({
      ...currentForm,
      [fieldName]: value,
    }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");

    try {
      const savedProfile = await saveMyProfile(user, toPayload(form));
      setForm(toForm(savedProfile?.user, user));
      setProfileStatus(savedProfile?.user?.profileStatus || "pending_onboarding");
      setAspnParticipantId(savedProfile?.user?.aspnParticipantId || "");
      setMessage("Profile saved. You can continue to Home whenever you are ready.");
    } catch (nextError) {
      setError(nextError.message);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <ProfileState title="Loading profile" message="Retrieving your ASPN profile." />;
  }

  return (
    <div className="profile-stack">
      <section className="page-intro">
        <span className="eyebrow">Youth Profile</span>
        <h2>Complete Your Profile</h2>
        <p>
          Add the basic details ASPN needs for onboarding. Your profile remains private by default.
        </p>
      </section>

      <section className="notice-panel">
        <strong>Status:</strong>
        <span> {profileStatus || "pending_onboarding"}</span>
        <strong>ASPN Participant ID:</strong>
        <span> {aspnParticipantId || "Assigned after profile save"}</span>
      </section>

      {message ? <MessagePanel tone="success" message={message} /> : null}
      {error ? <MessagePanel tone="error" message={error} /> : null}

      <form className="profile-form" onSubmit={handleSubmit}>
        <section className="dashboard-section">
          <h3>Basic Information</h3>
          <div className="form-grid">
            <label>
              First Name
              <input
                name="firstName"
                onChange={(event) => updateField("firstName", event.target.value)}
                required
                type="text"
                value={form.firstName}
              />
            </label>
            <label>
              Last Name
              <input
                name="lastName"
                onChange={(event) => updateField("lastName", event.target.value)}
                required
                type="text"
                value={form.lastName}
              />
            </label>
            <label>
              Email
              <input
                name="email"
                onChange={(event) => updateField("email", event.target.value)}
                required
                type="email"
                value={form.email}
              />
            </label>
            <label>
              School
              <input
                name="school"
                onChange={(event) => updateField("school", event.target.value)}
                type="text"
                value={form.school}
              />
            </label>
            <label>
              Graduation Year
              <input
                inputMode="numeric"
                name="graduationYear"
                onChange={(event) => updateField("graduationYear", event.target.value)}
                type="text"
                value={form.graduationYear}
              />
            </label>
          </div>
        </section>

        <section className="dashboard-section">
          <h3>Interests</h3>
          <div className="form-grid">
            {interestFields.map((field) => (
              <label key={field.key}>
                {field.label}
                <small>{field.helperText}</small>
                <textarea
                  name={field.key}
                  onChange={(event) => updateField(field.key, event.target.value)}
                  rows={3}
                  value={form[field.key]}
                />
              </label>
            ))}
          </div>
        </section>

        <CredentialPortfolioSection earnedCredentials={earnedCredentials} navigate={navigate} />

        <div className="profile-actions">
          <button className="primary-action" disabled={saving} type="submit">
            {saving ? "Saving..." : "Save Profile"}
          </button>
          <button className="text-action" type="button" onClick={() => navigate("/dashboard")}>
            {message ? "Continue to Home" : "Return to Home"}
          </button>
        </div>
      </form>
    </div>
  );
}

function CredentialPortfolioSection({ earnedCredentials, navigate }) {
  const credentials = getUniqueEarnedCredentials(earnedCredentials);
  const totalCredentialCount = getAspnRegistryCredentials().length;

  return (
    <section className="dashboard-section profile-credentials-section" aria-labelledby="profile-credentials-title">
      <div className="section-header">
        <div>
          <span className="eyebrow">MY CREDENTIALS</span>
          <h3 id="profile-credentials-title">My Credentials</h3>
        </div>
      </div>

      <article className="profile-credential-summary">
        <strong>Credentials Earned</strong>
        <span>{credentials.length} of {totalCredentialCount} Credentials Earned</span>
      </article>

      {!credentials.length ? (
        <p className="empty-text">No credentials earned yet.</p>
      ) : (
        <div className="profile-credential-grid" aria-label="Earned credentials">
          {credentials.map((credential, index) => (
            <CredentialPortfolioCard
              credential={credential}
              key={credential.earnedCredentialID || credential.credentialID || index}
              navigate={navigate}
            />
          ))}
        </div>
      )}
    </section>
  );
}

function CredentialPortfolioCard({ credential, navigate }) {
  const registryCredential = findAspnRegistryCredentialMatch(credential);
  const pathwayNames = getRelatedPathwayNames(registryCredential);
  const credentialName = credential?.credentialName || registryCredential?.name || "ASPN Credential";
  const credentialCategory = credential?.category || formatCredentialType(registryCredential?.type);
  const credentialDetailId = registryCredential?.id || credential?.credentialID;

  return (
    <article className="profile-credential-card">
      <div className="profile-credential-card-header">
        <CredentialIcon credential={registryCredential} name={credentialName} />
        <div>
          <strong>{credentialName}</strong>
          <span>{credentialCategory || "Category not listed"}</span>
        </div>
      </div>

      <dl className="profile-credential-meta">
        <div>
          <dt>Award Date</dt>
          <dd>{formatAwardDate(credential?.awardedAt || credential?.earnedAt)}</dd>
        </div>
        <div>
          <dt>Related Pathway</dt>
          <dd>{pathwayNames || "Pathway not listed"}</dd>
        </div>
      </dl>
      {credentialDetailId ? (
        <button
          className="credential-detail-link"
          type="button"
          onClick={() => navigate(`/credentials/${encodeURIComponent(credentialDetailId)}`)}
        >
          View credential details
        </button>
      ) : null}
    </article>
  );
}

function ProfileState({ title, message }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
    </section>
  );
}

function MessagePanel({ tone, message }) {
  return (
    <section className={`message-panel ${tone}`}>
      <p>{message}</p>
    </section>
  );
}

function toForm(userProfile, firebaseUser) {
  const profile = userProfile || {};
  return {
    firstName: profile.firstName || "",
    lastName: profile.lastName || "",
    email: profile.email || firebaseUser?.email || "",
    school: profile.school || "",
    graduationYear: profile.graduationYear || "",
    educationInterests: toCommaText(profile.collegeInterests),
    careerInterests: toCommaText(profile.careerInterests),
    communityInterests: toCommaText([
      ...asArray(profile.civicInterests),
      ...asArray(profile.communityInterests),
      ...asArray(profile.publicServiceInterests),
    ]),
  };
}

function toPayload(form) {
  const communityInterestList = toList(form.communityInterests);

  return {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    email: form.email.trim(),
    school: form.school.trim(),
    graduationYear: form.graduationYear.trim(),
    collegeInterests: toList(form.educationInterests),
    careerInterests: toList(form.careerInterests),
    civicInterests: communityInterestList,
    communityInterests: communityInterestList,
    publicServiceInterests: communityInterestList,
  };
}

function toCommaText(value) {
  return Array.isArray(value) ? [...new Set(value.filter(Boolean))].join(", ") : "";
}

function toList(value) {
  return String(value || "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

function getRelatedPathwayNames(registryCredential) {
  if (!registryCredential?.pathwayIds?.length) {
    return "";
  }

  return registryCredential.pathwayIds
    .map((pathwayId) => aspnEcosystemRegistry.pathways.find((pathway) => pathway.id === pathwayId)?.name)
    .filter(Boolean)
    .join(", ");
}

function formatCredentialType(type) {
  if (type === "core") {
    return "Core Credential";
  }

  if (type === "advanced") {
    return "Advanced Credential";
  }

  return "";
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

  return date.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}
