import { useEffect, useState } from "react";
import { fetchMyProfile, saveMyProfile } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

const interestFields = [
  { key: "collegeInterests", label: "College Interests" },
  { key: "careerInterests", label: "Career Interests" },
  { key: "civicInterests", label: "Civic Interests" },
  { key: "communityInterests", label: "Community Interests" },
  { key: "publicServiceInterests", label: "Public Service Interests" },
];

const initialForm = {
  firstName: "",
  lastName: "",
  email: "",
  school: "",
  graduationYear: "",
  collegeInterests: "",
  careerInterests: "",
  civicInterests: "",
  communityInterests: "",
  publicServiceInterests: "",
};

export function ProfileCompletionPage({ navigate }) {
  const { user } = useAuth();
  const [form, setForm] = useState(initialForm);
  const [profileStatus, setProfileStatus] = useState("pending_onboarding");
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
        if (!isActive) {
          return;
        }

        if (profile?.user) {
          setForm(toForm(profile.user, user));
          setProfileStatus(profile.user.profileStatus || "pending_onboarding");
        } else {
          setForm({ ...initialForm, email: user?.email || "" });
          setProfileStatus("pending_onboarding");
        }
      } catch (nextError) {
        if (isActive) {
          setError(nextError.message);
          setForm({ ...initialForm, email: user?.email || "" });
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
      setMessage("Profile saved. Your dashboard will use these details.");
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

        <div className="profile-actions">
          <button className="primary-action" disabled={saving} type="submit">
            {saving ? "Saving..." : "Save Profile"}
          </button>
          <button className="text-action" type="button" onClick={() => navigate("/dashboard")}>
            Return to Dashboard
          </button>
        </div>
      </form>
    </div>
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
    collegeInterests: toCommaText(profile.collegeInterests),
    careerInterests: toCommaText(profile.careerInterests),
    civicInterests: toCommaText(profile.civicInterests),
    communityInterests: toCommaText(profile.communityInterests),
    publicServiceInterests: toCommaText(profile.publicServiceInterests),
  };
}

function toPayload(form) {
  return {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    email: form.email.trim(),
    school: form.school.trim(),
    graduationYear: form.graduationYear.trim(),
    collegeInterests: toList(form.collegeInterests),
    careerInterests: toList(form.careerInterests),
    civicInterests: toList(form.civicInterests),
    communityInterests: toList(form.communityInterests),
    publicServiceInterests: toList(form.publicServiceInterests),
  };
}

function toCommaText(value) {
  return Array.isArray(value) ? value.join(", ") : "";
}

function toList(value) {
  return String(value || "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}
