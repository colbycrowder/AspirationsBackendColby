import { useState } from "react";
import { ApiAccessError, awardStaffCredential, createStaffCredentialDefinition } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

const emptyDefinitionForm = {
  credentialName: "",
  description: "",
  icon: "",
  category: "",
  active: true,
  programIds: "",
  requirementText: "",
  autoAwardEnabled: false,
  requirementType: "",
  requiredAttendanceCount: "",
};

const emptyAwardForm = {
  userUID: "",
  credentialID: "",
};

export function StaffCredentialManagementPage() {
  const { user } = useAuth();
  const [definitionForm, setDefinitionForm] = useState(emptyDefinitionForm);
  const [awardForm, setAwardForm] = useState(emptyAwardForm);
  const [createdCredentialId, setCreatedCredentialId] = useState("");
  const [awardedCredentialId, setAwardedCredentialId] = useState("");
  const [savingDefinition, setSavingDefinition] = useState(false);
  const [awarding, setAwarding] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function handleCreateDefinition(event) {
    event.preventDefault();
    setSavingDefinition(true);
    setMessage("");
    setError("");
    setCreatedCredentialId("");

    try {
      const credentialId = await createStaffCredentialDefinition(user, buildCredentialDefinitionPayload(definitionForm));
      setCreatedCredentialId(credentialId);
      setAwardForm((current) => ({ ...current, credentialID: credentialId }));
      setDefinitionForm(emptyDefinitionForm);
      setMessage("Credential definition was created. Save the credential ID for manual awards or dashboard testing.");
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setSavingDefinition(false);
    }
  }

  async function handleAwardCredential(event) {
    event.preventDefault();
    setAwarding(true);
    setMessage("");
    setError("");
    setAwardedCredentialId("");

    try {
      const earnedCredentialId = await awardStaffCredential(user, {
        userUID: awardForm.userUID.trim(),
        credentialID: awardForm.credentialID.trim(),
      });
      setAwardedCredentialId(earnedCredentialId);
      setMessage("Credential was awarded to the youth profile.");
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setAwarding(false);
    }
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Credential Management</h2>
        <p>Create credential definitions and manually award credentials through protected backend routes.</p>
      </section>

      <section className="notice-panel">
        <strong>Listing limitation.</strong>
        <span> The backend does not currently expose staff routes to list credential definitions or earned credentials.</span>
      </section>

      {message ? (
        <section className="message-panel success">
          <p>{message}</p>
          {createdCredentialId ? <p>Credential ID: {createdCredentialId}</p> : null}
          {awardedCredentialId ? <p>Earned Credential ID: {awardedCredentialId}</p> : null}
        </section>
      ) : null}

      {error ? (
        <section className="message-panel error">
          <p>{error}</p>
        </section>
      ) : null}

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Create Credential Definition</h3>
              <p>Credential names, descriptions, icons, and requirements remain staff-configurable.</p>
            </div>
          </div>

          <form className="profile-form" onSubmit={handleCreateDefinition}>
            <div className="form-grid">
              <label>
                Credential Name
                <input
                  required
                  value={definitionForm.credentialName}
                  onChange={(event) => updateDefinitionForm("credentialName", event.target.value)}
                />
              </label>

              <label>
                Category
                <input
                  value={definitionForm.category}
                  onChange={(event) => updateDefinitionForm("category", event.target.value)}
                />
              </label>

              <label>
                Icon
                <input
                  placeholder="Optional icon text or URL"
                  value={definitionForm.icon}
                  onChange={(event) => updateDefinitionForm("icon", event.target.value)}
                />
              </label>

              <label>
                Program IDs
                <input
                  placeholder="Comma-separated program IDs"
                  value={definitionForm.programIds}
                  onChange={(event) => updateDefinitionForm("programIds", event.target.value)}
                />
              </label>

              <label className="full-width-field">
                Description
                <textarea
                  rows="3"
                  value={definitionForm.description}
                  onChange={(event) => updateDefinitionForm("description", event.target.value)}
                />
              </label>

              <label className="full-width-field">
                Requirement Text
                <textarea
                  rows="3"
                  value={definitionForm.requirementText}
                  onChange={(event) => updateDefinitionForm("requirementText", event.target.value)}
                />
              </label>

              <label className="checkbox-row">
                <input
                  checked={definitionForm.active}
                  type="checkbox"
                  onChange={(event) => updateDefinitionForm("active", event.target.checked)}
                />
                Active
              </label>

              <label className="checkbox-row">
                <input
                  checked={definitionForm.autoAwardEnabled}
                  type="checkbox"
                  onChange={(event) => updateDefinitionForm("autoAwardEnabled", event.target.checked)}
                />
                Auto-award enabled
              </label>

              <label>
                Requirement Type
                <select
                  value={definitionForm.requirementType}
                  onChange={(event) => updateDefinitionForm("requirementType", event.target.value)}
                >
                  <option value="">Manual or none</option>
                  <option value="attendance_count">Attendance count</option>
                  <option value="rwd_quiz_passed">RWD quiz passed</option>
                  <option value="service_hours">Service hours</option>
                  <option value="form_completion">Form completion</option>
                  <option value="manual_award">Manual award</option>
                </select>
              </label>

              <label>
                Required Attendance Count
                <input
                  min="0"
                  type="number"
                  value={definitionForm.requiredAttendanceCount}
                  onChange={(event) => updateDefinitionForm("requiredAttendanceCount", event.target.value)}
                />
              </label>
            </div>

            <div className="profile-actions">
              <button className="primary-action" disabled={savingDefinition} type="submit">
                {savingDefinition ? "Creating..." : "Create Credential Definition"}
              </button>
            </div>
          </form>
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Award Credential</h3>
              <p>Manual awards require an existing active credential definition ID and a youth UID.</p>
            </div>
          </div>

          <form className="profile-form" onSubmit={handleAwardCredential}>
            <div className="form-grid">
              <label>
                Youth UID
                <input
                  required
                  value={awardForm.userUID}
                  onChange={(event) => updateAwardForm("userUID", event.target.value)}
                />
              </label>

              <label>
                Credential Definition ID
                <input
                  required
                  value={awardForm.credentialID}
                  onChange={(event) => updateAwardForm("credentialID", event.target.value)}
                />
              </label>
            </div>

            <div className="profile-actions">
              <button className="primary-action" disabled={awarding} type="submit">
                {awarding ? "Awarding..." : "Award Credential"}
              </button>
            </div>
          </form>
        </div>
      </section>
    </div>
  );

  function updateDefinitionForm(field, value) {
    setDefinitionForm((current) => ({ ...current, [field]: value }));
  }

  function updateAwardForm(field, value) {
    setAwardForm((current) => ({ ...current, [field]: value }));
  }
}

function buildCredentialDefinitionPayload(form) {
  const payload = {
    active: Boolean(form.active),
    autoAwardEnabled: Boolean(form.autoAwardEnabled),
    programIds: splitCommaSeparated(form.programIds),
    requirements: [],
  };

  const textFields = ["credentialName", "description", "icon", "category", "requirementText", "requirementType"];
  textFields.forEach((field) => {
    const value = String(form[field] || "").trim();
    if (value) {
      payload[field] = value;
    }
  });

  if (form.requiredAttendanceCount !== "") {
    payload.requiredAttendanceCount = Number(form.requiredAttendanceCount);
  }

  return payload;
}

function splitCommaSeparated(value) {
  return String(value || "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

function getStaffPageError(error) {
  if (error instanceof ApiAccessError && error.status === 401) {
    return "Sign in with a valid Firebase account before opening staff/admin tools.";
  }

  if (error instanceof ApiAccessError && error.status === 403) {
    return "Access denied. This account does not have staff or admin access.";
  }

  return error.message || "Staff/admin data is unavailable. Confirm the backend is running and try again.";
}
