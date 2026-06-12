import { useEffect, useState } from "react";
import {
  archiveStaffProgram,
  createStaffProgram,
  fetchStaffProgramDetail,
  fetchStaffPrograms,
  fetchStaffProgramTotals,
  restoreStaffProgram,
  updateStaffProgram,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const emptyFilters = { active: "", programType: "" };
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
  const [totals, setTotals] = useState({});
  const [detail, setDetail] = useState(null);
  const [form, setForm] = useState(emptyProgram);
  const [selectedId, setSelectedId] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadPrograms();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadPrograms(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const [programData, totalData] = await Promise.all([
        fetchStaffPrograms(user, cleanFilters(nextFilters)),
        fetchStaffProgramTotals(user),
      ]);
      setPrograms(programData);
      setTotals(totalData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setPrograms([]);
      setTotals({});
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
      setDetail(await fetchStaffProgramDetail(user, programId));
    } catch (nextError) {
      setDetail(null);
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
          { label: "Attendance Records", value: totals.totalAttendanceRecords ?? 0 },
          { label: "Service Hours", value: totals.totalServiceHours ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <form className="compact-form" onSubmit={handleFilterSubmit}>
            <select value={filters.active} onChange={(event) => setFilters({ ...filters, active: event.target.value })}>
              <option value="">All statuses</option>
              <option value="true">Active</option>
              <option value="false">Archived</option>
            </select>
            <input placeholder="Program type/category" value={filters.programType} onChange={(event) => setFilters({ ...filters, programType: event.target.value })} />
            <button className="primary-action" type="submit">Apply Filters</button>
          </form>

          <h3>{selectedId ? "Update Program" : "Create Program"}</h3>
          <ProgramForm form={form} setForm={setForm} onSubmit={handleSave} busy={busy === "save"} selectedId={selectedId} />
          <button className="text-action" type="button" onClick={() => { setSelectedId(""); setDetail(null); setForm(emptyProgram); }}>
            New Program
          </button>
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Programs</h3>
              <p>{programs.length} program{programs.length === 1 ? "" : "s"} loaded.</p>
            </div>
          </div>
          <div className="staff-record-list">
            {programs.length === 0 ? <p>No programs match the current filters.</p> : null}
            {programs.map((program) => {
              const programId = getProgramId(program);
              return (
                <article className={selectedId === programId ? "staff-record-card selected" : "staff-record-card"} key={programId}>
                  <button className="unstyled-button" type="button" onClick={() => handleSelect(program)}>
                    <strong>{program.programName || "Untitled program"}</strong>
                    <span>{program.category || "Uncategorized"} · {program.programStatus || "active"}</span>
                    <span>{programId}</span>
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
                  { label: "Credentials", value: detail.credentialCount ?? 0 },
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
            </div>
          ) : null}
        </div>
      </section>
    </div>
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

function cleanFilters(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ""));
}

function getProgramId(program) {
  return getRecordId(program, ["programId", "programID"]);
}
