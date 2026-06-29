import { useEffect, useState } from "react";
import {
  archiveStaffProgram,
  createStaffProgram,
  fetchStaffPilotReporting,
  fetchStaffProgramDetail,
  fetchStaffProgramEnrollmentsForProgram,
  fetchStaffPrograms,
  fetchStaffProgramTotals,
  fetchStaffYouthUsers,
  restoreStaffProgram,
  updateStaffProgram,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const emptyFilters = { search: "", active: "", category: "" };
const emptyProgram = {
  programName: "",
  description: "",
  category: "",
  programLeader: "",
  capacity: "",
  programStatus: "active",
};

export function ProgramManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [programs, setPrograms] = useState([]);
  const [youthUsers, setYouthUsers] = useState([]);
  const [programRoster, setProgramRoster] = useState([]);
  const [totals, setTotals] = useState({});
  const [pilotReport, setPilotReport] = useState({});
  const [detail, setDetail] = useState(null);
  const [form, setForm] = useState(emptyProgram);
  const [selectedId, setSelectedId] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const programReports = Array.isArray(pilotReport?.programs) ? pilotReport.programs : [];
  const visiblePrograms = filterPrograms(programs, filters);
  const selectedProgramReport = programReports.find((program) => program.programId === selectedId);

  useEffect(() => {
    loadPrograms();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadPrograms(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const apiFilters = buildProgramFilters(nextFilters);
      const [programData, totalData, youthData] = await Promise.all([
        fetchStaffPrograms(user, apiFilters),
        fetchStaffProgramTotals(user),
        fetchStaffYouthUsers(user),
      ]);
      setPrograms(programData);
      setTotals(totalData);
      setYouthUsers(youthData);

      try {
        setPilotReport(await fetchStaffPilotReporting(user));
      } catch {
        setPilotReport({});
      }
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setPrograms([]);
      setYouthUsers([]);
      setProgramRoster([]);
      setTotals({});
      setPilotReport({});
    } finally {
      setLoading(false);
    }
  }

  async function handleSelect(program) {
    const programId = getProgramId(program);
    setSelectedId(programId);
    setForm({
      programName: program.programName || "",
      description: program.description || "",
      category: program.category || "",
      programLeader: program.programLeader || "",
      capacity: program.capacity ?? "",
      programStatus: program.programStatus || "active",
    });
    setError("");

    try {
      const [programDetail, enrollments] = await Promise.all([
        fetchStaffProgramDetail(user, programId),
        fetchStaffProgramEnrollmentsForProgram(user, programId),
      ]);
      setDetail(programDetail);
      setProgramRoster(buildProgramRoster(enrollments, youthUsers));
    } catch (nextError) {
      setDetail(null);
      setProgramRoster([]);
      setError(getStaffPageError(nextError));
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadPrograms(filters);
  }

  async function handleSave(event) {
    event.preventDefault();
    setBusy("save");
    setMessage("");
    setError("");

    try {
      const payload = buildProgramPayload(form);
      if (selectedId) {
        await updateStaffProgram(user, selectedId, payload);
        setMessage("Program was updated.");
      } else {
        const programId = await createStaffProgram(user, payload);
        setMessage(`Program was created: ${programId}`);
        setForm(emptyProgram);
      }
      await loadPrograms();
      if (selectedId) {
        setDetail(await fetchStaffProgramDetail(user, selectedId));
      }
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleArchiveRestore(active) {
    if (!selectedId) {
      return;
    }

    setBusy("status");
    setMessage("");
    setError("");

    try {
      if (active) {
        await restoreStaffProgram(user, selectedId);
        setMessage("Program was restored.");
      } else {
        await archiveStaffProgram(user, selectedId);
        setMessage("Program was archived.");
      }
      setDetail(null);
      setProgramRoster([]);
      await loadPrograms();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  if (loading) {
    return <StaffState title="Loading programs" message="Retrieving staff program catalog and totals." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Program Management</h2>
        <p>Manage program catalog records, archive/restore programs, and review program reporting counts.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <SummaryGrid
        items={[
          { label: "Total Programs", value: totals.totalPrograms ?? programs.length },
          { label: "Active Programs", value: totals.activePrograms ?? 0 },
          { label: "Archived Programs", value: totals.archivedPrograms ?? 0 },
          { label: "Enrollments", value: totals.totalEnrollments ?? 0 },
          { label: "Credentials Earned", value: totals.totalCredentialsEarned ?? 0 },
          { label: "Program Participation", value: formatPercent(pilotReport?.participation?.programParticipationPercentage) },
          { label: "Attendance Records", value: totals.totalAttendanceRecords ?? 0 },
          { label: "Service Hours", value: totals.totalServiceHours ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <GroupedCountPanel title="Programs By Status" values={totals.programsByStatus} />
        <ProgramComparisonPanel programs={programReports} />
      </section>

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <form className="compact-form" onSubmit={handleFilterSubmit}>
            <input placeholder="Search program name" value={filters.search} onChange={(event) => setFilters({ ...filters, search: event.target.value })} />
            <select value={filters.active} onChange={(event) => setFilters({ ...filters, active: event.target.value })}>
              <option value="">All statuses</option>
              <option value="true">Active</option>
              <option value="false">Archived</option>
            </select>
            <input placeholder="Category" value={filters.category} onChange={(event) => setFilters({ ...filters, category: event.target.value })} />
            <button className="primary-action" type="submit">Apply Filters</button>
          </form>

          <h3>{selectedId ? "Update Program" : "Create Program"}</h3>
          <ProgramForm form={form} setForm={setForm} onSubmit={handleSave} busy={busy === "save"} selectedId={selectedId} />
          <button className="text-action" type="button" onClick={() => { setSelectedId(""); setDetail(null); setProgramRoster([]); setForm(emptyProgram); }}>
            New Program
          </button>
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Programs</h3>
              <p>{visiblePrograms.length} of {programs.length} program{programs.length === 1 ? "" : "s"} shown.</p>
            </div>
          </div>
          <div className="staff-record-list">
            {visiblePrograms.length === 0 ? <p>No programs match the current filters.</p> : null}
            {visiblePrograms.map((program) => {
              const programId = getProgramId(program);
              const report = programReports.find((programReport) => programReport.programId === programId);
              return (
                <article className={selectedId === programId ? "staff-record-card selected" : "staff-record-card"} key={programId}>
                  <button className="unstyled-button" type="button" onClick={() => handleSelect(program)}>
                    <strong>{program.programName || "Untitled program"}</strong>
                    <span>{program.category || "Uncategorized"} · {program.programStatus || "active"}</span>
                    <span>{programId}</span>
                    <span>
                      {report?.activeParticipants ?? 0} active · {report?.registrations ?? 0} enrollments ·{" "}
                      {report?.credentialCompletions ?? 0} credentials
                    </span>
                  </button>
                </article>
              );
            })}
          </div>

          {detail ? (
            <div className="staff-detail-panel">
              <h3>Program Detail</h3>
              <SummaryGrid
                items={[
                  { label: "Enrollments", value: detail.enrollmentCount ?? 0 },
                  { label: "Credentials", value: detail.credentialCount ?? selectedProgramReport?.credentialCompletions ?? 0 },
                  { label: "Active Participants", value: selectedProgramReport?.activeParticipants ?? 0 },
                  { label: "Attendance", value: detail.attendanceCount ?? 0 },
                  { label: "Service Records", value: detail.serviceHourRecordCount ?? 0 },
                  { label: "Service Hours", value: detail.serviceHourTotal ?? 0 },
                ]}
              />
              <div className="staff-inline-actions">
                <button className="text-action" disabled={busy === "status"} type="button" onClick={() => handleArchiveRestore(false)}>
                  Archive
                </button>
                <button className="text-action" disabled={busy === "status"} type="button" onClick={() => handleArchiveRestore(true)}>
                  Restore
                </button>
              </div>
              <ProgramRoster roster={programRoster} />
            </div>
          ) : null}
        </div>
      </section>
    </div>
  );
}

function ProgramRoster({ roster }) {
  return (
    <section className="program-roster-panel" aria-labelledby="program-roster-title">
      <h3 id="program-roster-title">Program Roster</h3>
      {roster.length ? (
        <div className="program-roster-list">
          <div className="program-roster-row header">
            <span>Youth name</span>
            <span>Email</span>
            <span>ASPN Participant ID</span>
            <span>Profile status</span>
            <span>Staff verified</span>
          </div>
          {roster.map((entry) => (
            <div className="program-roster-row" key={entry.userUID || entry.enrollmentId}>
              <strong className="program-roster-youth">{entry.youthName}</strong>
              <span className="program-roster-email">{entry.email || "Email not listed"}</span>
              <span className="program-roster-aspn-id">{entry.aspnParticipantId || "No ASPN ID"}</span>
              <span className="program-roster-status">{entry.profileStatus || "status missing"}</span>
              <span className="program-roster-verified">{entry.staffVerified ? "Verified" : "Not verified"}</span>
            </div>
          ))}
        </div>
      ) : (
        <p className="empty-text">No youth enrolled in this program yet.</p>
      )}
    </section>
  );
}

function ProgramForm({ form, setForm, onSubmit, busy, selectedId }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input required placeholder="Program name" value={form.programName} onChange={(event) => setForm({ ...form, programName: event.target.value })} />
      <input placeholder="Category" value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })} />
      <input placeholder="Program leader" value={form.programLeader} onChange={(event) => setForm({ ...form, programLeader: event.target.value })} />
      <input min="0" placeholder="Capacity" type="number" value={form.capacity} onChange={(event) => setForm({ ...form, capacity: event.target.value })} />
      <select value={form.programStatus} onChange={(event) => setForm({ ...form, programStatus: event.target.value })}>
        <option value="active">active</option>
        <option value="archived">archived</option>
      </select>
      <textarea placeholder="Description" rows="3" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
      <button className="primary-action" disabled={busy} type="submit">{busy ? "Saving..." : selectedId ? "Update Program" : "Create Program"}</button>
    </form>
  );
}

function buildProgramPayload(form) {
  return {
    programName: form.programName.trim(),
    description: form.description.trim(),
    category: form.category.trim(),
    programLeader: form.programLeader.trim(),
    capacity: form.capacity === "" ? null : Number(form.capacity),
    programStatus: form.programStatus,
  };
}

function buildProgramFilters(filters) {
  const apiFilters = {};

  if (filters.active !== "") {
    apiFilters.active = filters.active;
  }

  if (filters.category) {
    apiFilters.programType = filters.category;
  }

  return apiFilters;
}

function getProgramId(program) {
  return getRecordId(program, ["programId", "programID"]);
}

function buildProgramRoster(enrollments, youthUsers) {
  return enrollments
    .filter((enrollment) => String(enrollment.enrollmentStatus || "active").toLowerCase() === "active")
    .map((enrollment) => {
      const youth = youthUsers.find((item) => getUserUid(item) === enrollment.userUID);
      return {
        enrollmentId: enrollment.enrollmentId,
        userUID: enrollment.userUID,
        youthName: youth ? formatYouthName(youth) : enrollment.userUID || "Unknown youth",
        email: youth?.email || "",
        aspnParticipantId: youth?.aspnParticipantId || "",
        profileStatus: youth?.profileStatus || "",
        staffVerified: Boolean(youth?.staffVerified),
      };
    })
    .sort((left, right) => left.youthName.localeCompare(right.youthName));
}

function getUserUid(user) {
  return getRecordId(user, ["uid", "userUID", "userUid"]);
}

function formatYouthName(user) {
  const name = [user?.firstName, user?.lastName].filter(Boolean).join(" ").trim();
  return name || user?.email || getUserUid(user) || "Unnamed youth";
}

function filterPrograms(programs, filters) {
  const search = filters.search.trim().toLowerCase();
  const category = filters.category.trim().toLowerCase();

  return programs.filter((program) => {
    const nameMatches = !search || (program.programName || "").toLowerCase().includes(search);
    const categoryMatches = !category || (program.category || "").toLowerCase().includes(category);
    return nameMatches && categoryMatches;
  });
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
              <span>{label || "Unknown"}</span>
              <strong>{count ?? 0}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function ProgramComparisonPanel({ programs }) {
  return (
    <section className="dashboard-section">
      <h3>Program Participation Comparison</h3>
      {programs.length === 0 ? (
        <p>No program reporting data is available yet.</p>
      ) : (
        <div className="staff-mini-table">
          {programs.map((program) => (
            <div key={program.programId || program.programName}>
              <span>
                <strong>{program.programName || "Untitled program"}</strong>
                <small>{program.category || "Uncategorized"} · {program.programStatus || "status missing"}</small>
              </span>
              <span>
                {program.activeParticipants ?? 0} active · {program.registrations ?? 0} enrollments ·{" "}
                {program.credentialCompletions ?? 0} credentials
              </span>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function formatPercent(value) {
  const numericValue = Number(value ?? 0);
  return `${Number.isFinite(numericValue) ? numericValue.toFixed(1) : "0.0"}%`;
}
