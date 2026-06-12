import { useEffect, useState } from "react";
import {
  archiveStaffCredentialDefinition,
  awardStaffCredential,
  createStaffCredentialDefinition,
  fetchStaffCredentialDefinitions,
  fetchStaffCredentialTotals,
  restoreStaffCredentialDefinition,
  updateStaffCredentialDefinition,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const emptyFilters = { category: "", active: "", programId: "" };
const emptyDefinition = {
  credentialName: "",
  description: "",
  icon: "",
  category: "",
  active: true,
  programIds: "",
  requirementText: "",
};
const emptyAward = { userUID: "", credentialID: "" };

export function CredentialManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [definitions, setDefinitions] = useState([]);
  const [totals, setTotals] = useState({});
  const [definitionForm, setDefinitionForm] = useState(emptyDefinition);
  const [awardForm, setAwardForm] = useState(emptyAward);
  const [selectedId, setSelectedId] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadCredentials();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadCredentials(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const clean = cleanFilters(nextFilters);
      const [definitionData, totalData] = await Promise.all([
        fetchStaffCredentialDefinitions(user, clean),
        fetchStaffCredentialTotals(user, clean),
      ]);
      setDefinitions(definitionData);
      setTotals(totalData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setDefinitions([]);
      setTotals({});
    } finally {
      setLoading(false);
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadCredentials(filters);
  }

  async function handleCreate(event) {
    event.preventDefault();
    setBusy("create");
    setMessage("");
    setError("");

    try {
      const credentialID = await createStaffCredentialDefinition(user, buildDefinitionPayload(definitionForm));
      setMessage(`Credential definition created: ${credentialID}`);
      setAwardForm((current) => ({ ...current, credentialID }));
      setDefinitionForm(emptyDefinition);
      await loadCredentials();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleAward(event) {
    event.preventDefault();
    setBusy("award");
    setMessage("");
    setError("");

    try {
      const earnedCredentialID = await awardStaffCredential(user, {
        userUID: awardForm.userUID.trim(),
        credentialID: awardForm.credentialID.trim(),
      });
      setMessage(`Credential awarded: ${earnedCredentialID}`);
      setAwardForm(emptyAward);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleActiveToggle(definition, active) {
    const credentialID = getCredentialId(definition);
    setBusy(credentialID);
    setMessage("");
    setError("");

    try {
      if (active) {
        await restoreStaffCredentialDefinition(user, credentialID);
        setMessage("Credential definition restored.");
      } else {
        await archiveStaffCredentialDefinition(user, credentialID);
        setMessage("Credential definition archived.");
      }
      await loadCredentials();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleRename(definition) {
    const credentialID = getCredentialId(definition);
    const nextName = window.prompt("Credential name", definition.credentialName || "");
    if (nextName === null) {
      return;
    }

    setBusy(credentialID);
    setMessage("");
    setError("");

    try {
      await updateStaffCredentialDefinition(user, credentialID, { credentialName: nextName });
      setMessage("Credential definition updated.");
      await loadCredentials();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  if (loading) {
    return <StaffState title="Loading credentials" message="Retrieving credential definitions and totals." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Credential Management</h2>
        <p>Manage the credential catalog and manually award credentials through protected staff routes.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <SummaryGrid
        items={[
          { label: "Definitions", value: totals.totalDefinitions ?? definitions.length },
          { label: "Active", value: totals.activeDefinitions ?? 0 },
          { label: "Archived", value: totals.archivedDefinitions ?? 0 },
          { label: "Earned Credentials", value: totals.totalEarnedCredentials ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <FilterForm filters={filters} setFilters={setFilters} onSubmit={handleFilterSubmit} />
          <h3>Create Definition</h3>
          <DefinitionForm form={definitionForm} setForm={setDefinitionForm} onSubmit={handleCreate} busy={busy === "create"} />
          <h3>Award Credential</h3>
          <AwardForm form={awardForm} setForm={setAwardForm} onSubmit={handleAward} busy={busy === "award"} />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Credential Definitions</h3>
              <p>{definitions.length} definition{definitions.length === 1 ? "" : "s"} loaded.</p>
            </div>
          </div>
          <div className="staff-record-list">
            {definitions.length === 0 ? <p>No credential definitions match the current filters.</p> : null}
            {definitions.map((definition) => {
              const credentialID = getCredentialId(definition);
              const isSelected = selectedId === credentialID;
              return (
                <article className={isSelected ? "staff-record-card selected" : "staff-record-card"} key={credentialID}>
                  <button className="unstyled-button" type="button" onClick={() => setSelectedId(credentialID)}>
                    <strong>{definition.credentialName || "Untitled credential"}</strong>
                    <span>{definition.category || "Uncategorized"} · {definition.active ? "active" : "archived"}</span>
                    <span>{credentialID}</span>
                  </button>
                  <p>{definition.requirementText || definition.description || "No requirement text provided."}</p>
                  <div className="staff-inline-actions">
                    <button className="text-action" disabled={busy === credentialID} type="button" onClick={() => handleRename(definition)}>
                      Rename
                    </button>
                    <button className="text-action" disabled={busy === credentialID || !definition.active} type="button" onClick={() => handleActiveToggle(definition, false)}>
                      Archive
                    </button>
                    <button className="text-action" disabled={busy === credentialID || definition.active} type="button" onClick={() => handleActiveToggle(definition, true)}>
                      Restore
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        </div>
      </section>
    </div>
  );
}

function FilterForm({ filters, setFilters, onSubmit }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input placeholder="Category" value={filters.category} onChange={(event) => setFilters({ ...filters, category: event.target.value })} />
      <select value={filters.active} onChange={(event) => setFilters({ ...filters, active: event.target.value })}>
        <option value="">All statuses</option>
        <option value="true">Active</option>
        <option value="false">Archived</option>
      </select>
      <input placeholder="Program ID" value={filters.programId} onChange={(event) => setFilters({ ...filters, programId: event.target.value })} />
      <button className="primary-action" type="submit">Apply Filters</button>
    </form>
  );
}

function DefinitionForm({ form, setForm, onSubmit, busy }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input required placeholder="Name" value={form.credentialName} onChange={(event) => setForm({ ...form, credentialName: event.target.value })} />
      <input placeholder="Category" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} />
      <input placeholder="Icon" value={form.icon} onChange={(event) => setForm({ ...form, icon: event.target.value })} />
      <input placeholder="Program IDs, comma-separated" value={form.programIds} onChange={(event) => setForm({ ...form, programIds: event.target.value })} />
      <textarea placeholder="Description" rows="3" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
      <textarea placeholder="Requirement text" rows="3" value={form.requirementText} onChange={(event) => setForm({ ...form, requirementText: event.target.value })} />
      <button className="primary-action" disabled={busy} type="submit">{busy ? "Creating..." : "Create Definition"}</button>
    </form>
  );
}

function AwardForm({ form, setForm, onSubmit, busy }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input required placeholder="Youth UID" value={form.userUID} onChange={(event) => setForm({ ...form, userUID: event.target.value })} />
      <input required placeholder="Credential ID" value={form.credentialID} onChange={(event) => setForm({ ...form, credentialID: event.target.value })} />
      <button className="primary-action" disabled={busy} type="submit">{busy ? "Awarding..." : "Award Credential"}</button>
    </form>
  );
}

function buildDefinitionPayload(form) {
  return {
    credentialName: form.credentialName.trim(),
    description: form.description.trim(),
    icon: form.icon.trim(),
    category: form.category.trim(),
    active: true,
    programIds: form.programIds.split(",").map((value) => value.trim()).filter(Boolean),
    requirementText: form.requirementText.trim(),
  };
}

function cleanFilters(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ""));
}

function getCredentialId(definition) {
  return getRecordId(definition, ["credentialID", "credentialId"]);
}
