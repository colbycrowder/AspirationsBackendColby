import { useEffect, useState } from "react";
import {
  archiveStaffCredentialDefinition,
  awardStaffCredential,
  createStaffCredentialDefinition,
  fetchStaffCredentialDefinitions,
  fetchStaffCredentialTotals,
  fetchStaffPilotReporting,
  fetchStaffProgramEnrollmentsForProgram,
  fetchStaffPrograms,
  fetchStaffYouthUsers,
  restoreStaffCredentialDefinition,
  updateStaffCredentialDefinition,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import {
  buildProgramRosterOptions,
  findYouthByIdentifier,
  StaffRosterYouthSelect,
} from "./StaffRosterYouthSelect.jsx";
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
const emptyAward = { selectedYouthUid: "", userIdentifier: "", programId: "", credentialID: "" };
const emptyDetailForm = {
  credentialName: "",
  description: "",
  icon: "",
  category: "",
  active: true,
  programIds: "",
  requirementText: "",
};

export function CredentialManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [definitions, setDefinitions] = useState([]);
  const [youthUsers, setYouthUsers] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [programRoster, setProgramRoster] = useState([]);
  const [totals, setTotals] = useState({});
  const [pilotReport, setPilotReport] = useState({});
  const [definitionForm, setDefinitionForm] = useState(emptyDefinition);
  const [awardForm, setAwardForm] = useState(emptyAward);
  const [selectedId, setSelectedId] = useState("");
  const [detailForm, setDetailForm] = useState(emptyDetailForm);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const selectedDefinition = definitions.find((definition) => getCredentialId(definition) === selectedId);
  const credentialParticipationPercentage = pilotReport?.participation?.credentialParticipationPercentage;
  const totalCredentialsAwarded =
    totals.totalEarnedCredentials ?? pilotReport?.credentials?.totalCredentialsEarned ?? 0;
  const awardedByCategory = hasEntries(totals.earnedCredentialsByCategory)
    ? totals.earnedCredentialsByCategory
    : pilotReport?.credentials?.credentialsByCategory;
  const awardsByProgram = getAwardsByProgram(pilotReport?.programs);

  useEffect(() => {
    loadCredentials();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  useEffect(() => {
    if (!selectedDefinition) {
      setDetailForm(emptyDetailForm);
      return;
    }

    setDetailForm({
      credentialName: selectedDefinition.credentialName || "",
      description: selectedDefinition.description || "",
      icon: selectedDefinition.icon || "",
      category: selectedDefinition.category || "",
      active: selectedDefinition.active !== false,
      programIds: formatProgramIds(selectedDefinition.programIds),
      requirementText: selectedDefinition.requirementText || "",
    });
  }, [selectedDefinition]);

  async function loadCredentials(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const clean = cleanFilters(nextFilters);
      const [definitionData, totalData, reportData] = await Promise.all([
        fetchStaffCredentialDefinitions(user, clean),
        fetchStaffCredentialTotals(user, clean),
        fetchStaffPilotReporting(user),
      ]);
      const [programData, youthData] = await Promise.all([
        fetchStaffPrograms(user, { active: true }).catch(() => []),
        fetchStaffYouthUsers(user).catch(() => []),
      ]);
      setDefinitions(definitionData);
      setPrograms(programData);
      setYouthUsers(youthData);
      setTotals(totalData);
      setPilotReport(reportData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setDefinitions([]);
      setPrograms([]);
      setProgramRoster([]);
      setYouthUsers([]);
      setTotals({});
      setPilotReport({});
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
    const awardDetails = buildAwardDetails(awardForm, definitions, youthUsers, programs);
    const confirmed = window.confirm(buildAwardConfirmation(awardDetails));
    if (!confirmed) {
      return;
    }

    setBusy("award");
    setMessage("");
    setError("");

    try {
      const earnedCredentialID = await awardStaffCredential(user, {
        userIdentifier: awardDetails.userIdentifier,
        userUID: awardDetails.firebaseUid || awardDetails.userIdentifier,
        credentialID: awardForm.credentialID.trim(),
      });
      setMessage(`Credential awarded: ${earnedCredentialID}`);
      setAwardForm(emptyAward);
      setProgramRoster([]);
      await loadCredentials();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleAwardProgramChange(programId) {
    setAwardForm((current) => ({
      ...current,
      programId,
      selectedYouthUid: "",
      userIdentifier: "",
    }));
    setProgramRoster([]);
    setError("");
    if (!programId) {
      return;
    }

    try {
      setBusy("award-program-roster");
      const enrollments = await fetchStaffProgramEnrollmentsForProgram(user, programId);
      const activeEnrollments = enrollments.filter((enrollment) => String(enrollment.enrollmentStatus || "active").toLowerCase() === "active");
      setProgramRoster(buildProgramRosterOptions(activeEnrollments, youthUsers));
    } catch (nextError) {
      setProgramRoster([]);
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  function handleAwardYouthSelect(userIdentifier) {
    const rosterEntry = programRoster.find((entry) => entry.userIdentifier === userIdentifier);
    setAwardForm((current) => ({
      ...current,
      selectedYouthUid: rosterEntry?.userUID || "",
      userIdentifier,
    }));
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

  async function handleDetailSubmit(event) {
    event.preventDefault();

    if (!selectedDefinition) {
      return;
    }

    const credentialID = getCredentialId(selectedDefinition);
    setBusy(credentialID);
    setMessage("");
    setError("");

    try {
      await updateStaffCredentialDefinition(user, credentialID, buildDefinitionPayload(detailForm));
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
          { label: "Credentials Awarded", value: totalCredentialsAwarded },
          { label: "Credential Participation", value: formatPercent(credentialParticipationPercentage) },
        ]}
      />

      <section className="staff-management-grid">
        <GroupedCountPanel title="Definitions By Category" values={totals.definitionsByCategory} />
        <GroupedCountPanel title="Credentials Awarded By Category" values={awardedByCategory} />
        <GroupedCountPanel title="Credential Awards By Program" values={awardsByProgram} />
      </section>

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <FilterForm filters={filters} setFilters={setFilters} onSubmit={handleFilterSubmit} />
          <h3>Create Definition</h3>
          <DefinitionForm form={definitionForm} setForm={setDefinitionForm} onSubmit={handleCreate} busy={busy === "create"} />
          <h3>Award Credential</h3>
          <AwardForm
            busy={busy === "award"}
            definitions={definitions}
            form={awardForm}
            loadingRoster={busy === "award-program-roster"}
            onSubmit={handleAward}
            onProgramChange={handleAwardProgramChange}
            onYouthSelect={handleAwardYouthSelect}
            programs={programs}
            programRoster={programRoster}
            setForm={setAwardForm}
            youthUsers={youthUsers}
          />
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
                    <button className="text-action" disabled={busy === credentialID} type="button" onClick={() => setSelectedId(credentialID)}>
                      View Detail
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
          <CredentialDetailPanel
            busy={busy === selectedId}
            definition={selectedDefinition}
            form={detailForm}
            onActiveToggle={handleActiveToggle}
            onSubmit={handleDetailSubmit}
            setForm={setDetailForm}
          />
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

function AwardForm({
  busy,
  definitions,
  form,
  loadingRoster,
  onProgramChange,
  onSubmit,
  onYouthSelect,
  programs,
  programRoster,
  setForm,
  youthUsers,
}) {
  const awardDetails = buildAwardDetails(form, definitions, youthUsers, programs);
  const activeDefinitions = definitions.filter((definition) => definition.active !== false);
  const canAward = !busy && Boolean(awardDetails.userIdentifier) && Boolean(form.credentialID.trim()) && activeDefinitions.length > 0;

  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <label>
        Program
        <select required value={form.programId} onChange={(event) => onProgramChange(event.target.value)}>
          <option value="">Select an active program</option>
          {programs.filter(isActiveProgram).map((program) => {
            const programId = getProgramId(program);
            return (
              <option key={programId || getProgramName(program)} value={programId}>
                {getProgramOptionLabel(program)}
              </option>
            );
          })}
        </select>
      </label>
      <StaffRosterYouthSelect
        formProgramId={form.programId}
        loadingRoster={loadingRoster}
        programRoster={programRoster}
        value={form.userIdentifier}
        onYouthSelect={onYouthSelect}
      />
      {form.programId && !loadingRoster && !programRoster.length ? (
        <p className="empty-text">No active enrolled youth found for this program. Use Advanced manual entry only if this is expected.</p>
      ) : null}
      <details className="advanced-manual-entry">
        <summary>Advanced manual entry</summary>
        <label>
          Youth Identifier
          <input
            placeholder="ASPN Participant ID, email, or Firebase UID"
            value={form.userIdentifier}
            onChange={(event) => setForm({ ...form, selectedYouthUid: "", userIdentifier: event.target.value })}
          />
          <small>Use this only when the roster is incomplete. ASPN Participant ID, email, and Firebase UID are supported for recovery.</small>
        </label>
      </details>
      <label>
        Credential
        <select
          required
          value={form.credentialID}
          onChange={(event) => setForm({ ...form, credentialID: event.target.value })}
        >
          <option value="">Select an active credential</option>
          {activeDefinitions.map((definition) => {
            const credentialID = getCredentialId(definition);
            return (
              <option key={credentialID} value={credentialID}>
                {getCredentialOptionLabel(definition)}
              </option>
            );
          })}
        </select>
      </label>
      {!activeDefinitions.length ? (
        <p className="empty-text">No active credential definitions are available for awarding.</p>
      ) : null}
      <section className="staff-detail-panel" aria-label="Credential award confirmation details">
        <h4>Confirm Before Awarding</h4>
        <dl className="staff-detail-list">
          <div><dt>Youth Name</dt><dd>{awardDetails.youthName || "Not resolved"}</dd></div>
          <div><dt>Youth Email</dt><dd>{awardDetails.youthEmail || "Not resolved"}</dd></div>
          <div><dt>ASPN Participant ID</dt><dd>{awardDetails.aspnParticipantId || "Not resolved"}</dd></div>
          <div><dt>Firebase UID</dt><dd>{awardDetails.firebaseUid || "Not resolved"}</dd></div>
          <div><dt>Program</dt><dd>{awardDetails.programName || "Select a program"}</dd></div>
          <div><dt>Credential</dt><dd>{awardDetails.credentialName}</dd></div>
          <div><dt>Credential ID</dt><dd>{awardDetails.credentialID || "Enter a credential ID"}</dd></div>
        </dl>
        {!awardDetails.credentialResolved ? (
          <p className="empty-text">Select an active credential before awarding.</p>
        ) : null}
        {!awardDetails.youthResolved ? (
          <p className="empty-text">Youth identity could not be resolved in the loaded staff list. The backend will still check the ASPN Participant ID or Firebase UID before awarding.</p>
        ) : null}
      </section>
      <button className="primary-action" disabled={!canAward} type="submit">{busy ? "Awarding..." : "Award Credential"}</button>
    </form>
  );
}

function CredentialDetailPanel({ busy, definition, form, onActiveToggle, onSubmit, setForm }) {
  if (!definition) {
    return (
      <div className="staff-detail-panel">
        <h3>Credential Detail</h3>
        <p>Select a credential definition to view details or edit staff-managed catalog fields.</p>
      </div>
    );
  }

  const credentialID = getCredentialId(definition);

  return (
    <div className="staff-detail-panel">
      <div className="section-header">
        <div>
          <h3>Credential Detail</h3>
          <p>{credentialID}</p>
        </div>
        <span className="status-pill">{definition.active ? "Active" : "Archived"}</span>
      </div>

      <dl className="staff-detail-list">
        <div>
          <dt>Category</dt>
          <dd>{definition.category || "Not set"}</dd>
        </div>
        <div>
          <dt>Programs</dt>
          <dd>{formatProgramIds(definition.programIds) || "No programs connected"}</dd>
        </div>
        <div>
          <dt>Requirement</dt>
          <dd>{definition.requirementText || "No requirement text provided"}</dd>
        </div>
      </dl>

      <form className="compact-form" onSubmit={onSubmit}>
        <input required placeholder="Credential name" value={form.credentialName} onChange={(event) => setForm({ ...form, credentialName: event.target.value })} />
        <input placeholder="Category" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} />
        <input placeholder="Icon" value={form.icon} onChange={(event) => setForm({ ...form, icon: event.target.value })} />
        <input placeholder="Program IDs, comma-separated" value={form.programIds} onChange={(event) => setForm({ ...form, programIds: event.target.value })} />
        <textarea placeholder="Description" rows="3" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
        <textarea placeholder="Requirement text" rows="3" value={form.requirementText} onChange={(event) => setForm({ ...form, requirementText: event.target.value })} />
        <button className="primary-action" disabled={busy} type="submit">{busy ? "Saving..." : "Save Credential"}</button>
      </form>

      <div className="staff-inline-actions">
        <button className="text-action" disabled={busy || !definition.active} type="button" onClick={() => onActiveToggle(definition, false)}>
          Archive Credential
        </button>
        <button className="text-action" disabled={busy || definition.active} type="button" onClick={() => onActiveToggle(definition, true)}>
          Restore Credential
        </button>
      </div>
    </div>
  );
}

function GroupedCountPanel({ title, values = {} }) {
  const rows = Object.entries(values || {});

  return (
    <section className="dashboard-section">
      <h3>{title}</h3>
      {rows.length === 0 ? (
        <p>No data available yet.</p>
      ) : (
        <div className="staff-mini-table">
          {rows.map(([label, count]) => (
            <div key={label}>
              <span>{label || "Uncategorized"}</span>
              <strong>{count ?? 0}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
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

function formatProgramIds(programIds) {
  if (Array.isArray(programIds)) {
    return programIds.join(", ");
  }

  return programIds || "";
}

function getAwardsByProgram(programs = []) {
  if (!Array.isArray(programs)) {
    return {};
  }

  return Object.fromEntries(
    programs
      .filter((program) => Number(program.credentialCompletions ?? 0) > 0)
      .map((program) => [
        program.programName || program.programId || "Untitled program",
        program.credentialCompletions ?? 0,
      ])
  );
}

function hasEntries(value) {
  return value && Object.keys(value).length > 0;
}

function formatPercent(value) {
  const numericValue = Number(value ?? 0);
  return `${Number.isFinite(numericValue) ? numericValue.toFixed(1) : "0.0"}%`;
}

function buildAwardDetails(form, definitions, youthUsers, programs = []) {
  const userIdentifier = getAwardIdentifier(form);
  const credentialID = form.credentialID.trim();
  const youth = findYouthByIdentifier(youthUsers, userIdentifier);
  const definition = definitions.find((item) => getCredentialId(item) === credentialID);
  const program = programs.find((item) => getProgramId(item) === form.programId);
  const youthName = youth ? formatYouthName(youth) : "";
  const firebaseUid = youth ? getUserUid(youth) : isLikelyAspnParticipantId(userIdentifier) ? "" : userIdentifier;

  return {
    aspnParticipantId: youth?.aspnParticipantId || "",
    credentialID,
    credentialName: definition?.credentialName || credentialID || "Selected credential",
    credentialResolved: Boolean(definition),
    firebaseUid,
    programName: program ? getProgramName(program) : form.programId,
    userIdentifier,
    youthEmail: youth?.email || "",
    youthName,
    youthResolved: Boolean(youth),
  };
}

function buildAwardConfirmation(details) {
  const youthLabel = details.youthName
    || details.aspnParticipantId
    || details.userIdentifier
    || "the selected youth";
  const lines = [
    `Award ${details.credentialName} to ${youthLabel}?`,
    "",
    `Youth name: ${details.youthName || "Not resolved"}`,
    `Youth email: ${details.youthEmail || "Not resolved"}`,
    `ASPN Participant ID: ${details.aspnParticipantId || "Not resolved"}`,
    `Firebase UID: ${details.firebaseUid || "Not resolved"}`,
    `Program: ${details.programName || "Not selected"}`,
    `Credential ID: ${details.credentialID || "Not entered"}`,
  ];

  if (!details.youthResolved) {
    lines.push("", "Youth identity could not be resolved in the loaded staff list. The backend will still check the ASPN Participant ID or Firebase UID before awarding.");
  }

  return lines.join("\n");
}

function getAwardIdentifier(form) {
  return (form.userIdentifier || form.userUID || "").trim();
}

function getCredentialOptionLabel(definition) {
  const name = definition?.credentialName || "Untitled credential";
  const category = definition?.category || "Uncategorized";
  return `${name} — ${category}`;
}

function getProgramId(program) {
  return getRecordId(program, ["programId", "programID", "id"]);
}

function getProgramName(program) {
  return program?.programName || program?.name || "Untitled program";
}

function getProgramOptionLabel(program) {
  const name = getProgramName(program);
  const status = program?.active === false ? "archived" : "active";
  return `${name} — ${status}`;
}

function isActiveProgram(program) {
  return program?.active !== false;
}

function isLikelyAspnParticipantId(value) {
  return /^ASPN-\d{4}-\d+$/i.test(value || "");
}

function getUserUid(user) {
  return getRecordId(user, ["uid", "userUID", "userUid"]);
}

function formatYouthName(user) {
  const name = [user?.firstName, user?.lastName].filter(Boolean).join(" ").trim();
  return name || user?.email || getUserUid(user) || "";
}
