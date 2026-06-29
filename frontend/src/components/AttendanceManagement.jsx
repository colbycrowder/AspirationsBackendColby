import { useEffect, useState } from "react";
import {
  createStaffAttendanceRecord,
  deleteStaffAttendanceRecord,
  fetchStaffAttendanceRecords,
  fetchStaffAttendanceTotals,
  fetchStaffProgramEnrollmentsForProgram,
  fetchStaffPrograms,
  fetchStaffYouthUsers,
  updateStaffAttendanceRecord,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import {
  buildProgramRosterOptions,
  findYouthByIdentifier,
  formatYouthName,
  getUserUid,
  StaffRosterYouthSelect,
} from "./StaffRosterYouthSelect.jsx";
import { formatDate, getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const emptyFilters = { userUID: "", programID: "", eventDate: "" };
const emptyForm = { selectedYouthUid: "", userIdentifier: "", programID: "", eventName: "", eventDate: "", attendanceStatus: "present" };

export function AttendanceManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [form, setForm] = useState(emptyForm);
  const [programRoster, setProgramRoster] = useState([]);
  const [records, setRecords] = useState([]);
  const [totals, setTotals] = useState({});
  const [programs, setPrograms] = useState([]);
  const [youthUsers, setYouthUsers] = useState([]);
  const [expandedSessionKey, setExpandedSessionKey] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadAttendance();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadAttendance(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const [recordData, totalData, programData, youthData] = await Promise.all([
        fetchStaffAttendanceRecords(user, cleanFilters(nextFilters)),
        fetchStaffAttendanceTotals(user, cleanFilters(nextFilters)),
        fetchStaffPrograms(user, { active: true }),
        fetchStaffYouthUsers(user),
      ]);
      setRecords(recordData);
      setTotals(totalData);
      setPrograms(programData);
      setYouthUsers(youthData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setRecords([]);
      setTotals({});
      setPrograms([]);
      setYouthUsers([]);
    } finally {
      setLoading(false);
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadAttendance(filters);
  }

  async function handleCreate(event) {
    event.preventDefault();
    const attendanceDetails = buildAttendanceDetails(form, programs, youthUsers);
    const confirmed = window.confirm(buildCreateAttendanceConfirmation(attendanceDetails));
    if (!confirmed) {
      return;
    }

    setBusy("create");
    setMessage("");
    setError("");

    try {
      await createStaffAttendanceRecord(user, {
        userIdentifier: attendanceDetails.userIdentifier,
        userUID: attendanceDetails.firebaseUid || attendanceDetails.userIdentifier,
        programID: form.programID,
        eventName: form.eventName,
        eventDate: form.eventDate ? `${form.eventDate}T00:00:00.000Z` : null,
        attendanceStatus: form.attendanceStatus,
      });
      setMessage("Attendance record was created.");
      setForm(emptyForm);
      setProgramRoster([]);
      setFilters(emptyFilters);
      setExpandedSessionKey("");
      await loadAttendance(emptyFilters);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleProgramChange(programID) {
    setForm((current) => ({
      ...current,
      programID,
      selectedYouthUid: "",
      userIdentifier: "",
    }));
    setProgramRoster([]);
    setError("");
    if (!programID) {
      return;
    }

    try {
      setBusy("program-roster");
      const enrollments = await fetchStaffProgramEnrollmentsForProgram(user, programID);
      const activeEnrollments = enrollments.filter((enrollment) => String(enrollment.enrollmentStatus || "active").toLowerCase() === "active");
      setProgramRoster(buildProgramRosterOptions(activeEnrollments, youthUsers));
    } catch (nextError) {
      setProgramRoster([]);
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  function handleYouthSelect(userIdentifier) {
    const rosterEntry = programRoster.find((entry) => entry.userIdentifier === userIdentifier);
    setForm((current) => ({
      ...current,
      selectedYouthUid: rosterEntry?.userUID || "",
      userIdentifier,
    }));
  }

  async function handleStatusUpdate(record, attendanceStatus) {
    const attendanceRecordID = getRecordId(record, ["attendanceRecordID", "attendanceRecordId"]);
    const confirmed = window.confirm(buildAttendanceConfirmation({ ...record, attendanceStatus }, "Update attendance status"));
    if (!confirmed) {
      return;
    }

    setBusy(attendanceRecordID);
    setMessage("");
    setError("");

    try {
      await updateStaffAttendanceRecord(user, attendanceRecordID, {
        userUID: record.userUID,
        programID: record.programID,
        eventName: record.eventName,
        eventDate: record.eventDate,
        attendanceStatus,
      });
      setMessage("Attendance status was updated.");
      await loadAttendance();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleDelete(record) {
    const attendanceRecordID = getRecordId(record, ["attendanceRecordID", "attendanceRecordId"]);
    const confirmed = window.confirm(
      `Delete this attendance record for UID ${record.userUID || "unknown youth"} in program ${record.programID || "unknown program"} on ${formatDate(record.eventDate)}? This may affect staff reporting and youth participation summaries.`
    );
    if (!confirmed) {
      return;
    }

    setBusy(attendanceRecordID);
    setMessage("");
    setError("");

    try {
      await deleteStaffAttendanceRecord(user, attendanceRecordID);
      setMessage("Attendance record was deleted.");
      await loadAttendance();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  if (loading) {
    return <StaffState title="Loading attendance" message="Retrieving staff attendance records." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Attendance Management</h2>
        <p>Review, filter, create, update, and delete attendance records from the protected backend.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <SummaryGrid
        items={[
          { label: "Total Records", value: totals.totalRecords ?? records.length },
          { label: "Present", value: totals.present ?? 0 },
          { label: "Absent", value: totals.absent ?? 0 },
          { label: "Excused", value: totals.excused ?? 0 },
          { label: "Pending", value: totals.pending ?? 0 },
        ]}
      />

      <section className="staff-management-grid attendance-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <StaffFilterForm filters={filters} setFilters={setFilters} onSubmit={handleFilterSubmit} />
          <h3>Create Attendance Record</h3>
          <AttendanceForm
            busy={busy === "create"}
            form={form}
            loadingRoster={busy === "program-roster"}
            programs={programs}
            programRoster={programRoster}
            onProgramChange={handleProgramChange}
            onYouthSelect={handleYouthSelect}
            setForm={setForm}
            youthUsers={youthUsers}
            onSubmit={handleCreate}
          />
          <ProgramAvailabilityNote programs={programs} />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Attendance Sessions</h3>
              <p>{records.length} record{records.length === 1 ? "" : "s"} grouped by program, date, and event.</p>
            </div>
          </div>
          <AttendanceSessionList
            busy={busy}
            expandedSessionKey={expandedSessionKey}
            programs={programs}
            records={records}
            youthUsers={youthUsers}
            onDelete={handleDelete}
            onStatusUpdate={handleStatusUpdate}
            onToggleSession={setExpandedSessionKey}
          />
        </div>
      </section>
    </div>
  );
}

function StaffFilterForm({ filters, setFilters, onSubmit }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input placeholder="Youth UID" value={filters.userUID} onChange={(event) => setFilters({ ...filters, userUID: event.target.value })} />
      <input placeholder="Program ID" value={filters.programID} onChange={(event) => setFilters({ ...filters, programID: event.target.value })} />
      <input type="date" value={filters.eventDate} onChange={(event) => setFilters({ ...filters, eventDate: event.target.value })} />
      <button className="primary-action" type="submit">Apply Filters</button>
    </form>
  );
}

function AttendanceForm({
  form,
  setForm,
  onSubmit,
  busy,
  loadingRoster,
  programs,
  programRoster,
  youthUsers,
  onProgramChange,
  onYouthSelect,
}) {
  const attendanceDetails = buildAttendanceDetails(form, programs, youthUsers);
  const canCreate = Boolean(
    !busy &&
    attendanceDetails.userIdentifier &&
    form.programID &&
    form.eventName.trim() &&
    form.eventDate &&
    form.attendanceStatus
  );

  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <label>
        Program
        <select required value={form.programID} onChange={(event) => onProgramChange(event.target.value)}>
          <option value="">Select an active program</option>
          {programs.map((program) => {
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
        formProgramId={form.programID}
        loadingRoster={loadingRoster}
        programRoster={programRoster}
        value={form.userIdentifier}
        onYouthSelect={onYouthSelect}
      />
      {form.programID && !loadingRoster && !programRoster.length ? (
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
      <input required placeholder="Event/session name" value={form.eventName} onChange={(event) => setForm({ ...form, eventName: event.target.value })} />
      <input required type="date" value={form.eventDate} onChange={(event) => setForm({ ...form, eventDate: event.target.value })} />
      <select value={form.attendanceStatus} onChange={(event) => setForm({ ...form, attendanceStatus: event.target.value })}>
        <option value="present">present</option>
        <option value="absent">absent</option>
        <option value="excused">excused</option>
        <option value="pending">pending</option>
      </select>
      <section className="staff-detail-panel" aria-label="Attendance record confirmation details">
        <h4>Confirm Attendance Details</h4>
        <dl className="staff-detail-list">
          <div><dt>Youth Name</dt><dd>{attendanceDetails.youthName || "Not resolved yet"}</dd></div>
          <div><dt>Youth Email</dt><dd>{attendanceDetails.youthEmail || "Not resolved yet"}</dd></div>
          <div><dt>ASPN Participant ID</dt><dd>{attendanceDetails.aspnParticipantId || "Not resolved yet"}</dd></div>
          <div><dt>Firebase UID</dt><dd>{attendanceDetails.firebaseUid || "Not resolved yet"}</dd></div>
          <div><dt>Program Name</dt><dd>{attendanceDetails.programName || "Select a program"}</dd></div>
          <div><dt>Program ID</dt><dd>{attendanceDetails.programID || "Select a program"}</dd></div>
          <div><dt>Event / Session</dt><dd>{attendanceDetails.eventName || "Enter an event name"}</dd></div>
          <div><dt>Date</dt><dd>{attendanceDetails.eventDate || "Select a date"}</dd></div>
          <div><dt>Status</dt><dd>{attendanceDetails.attendanceStatus || "pending"}</dd></div>
        </dl>
        {attendanceDetails.userIdentifier && !attendanceDetails.youthName ? (
          <p className="empty-text">Youth identity could not be resolved in the loaded staff list. The backend will still check the ASPN Participant ID or Firebase UID before creating attendance.</p>
        ) : null}
        {form.attendanceStatus === "present" ? (
          <p className="empty-text">Present attendance may trigger attendance-based credential rules.</p>
        ) : null}
      </section>
      <button className="primary-action" disabled={!canCreate} type="submit">{busy ? "Saving..." : "Create Attendance"}</button>
    </form>
  );
}

function ProgramAvailabilityNote({ programs }) {
  const programNames = programs.map((program) => getProgramName(program).toLowerCase());
  const hasYouthAdvisoryBoard = programNames.some((name) => name.includes("brian williams") || name.includes("youth advisory board"));
  const hasYouthCouncil = programNames.some((name) => name.includes("wesley bell") || name.includes("youth council"));

  if (hasYouthAdvisoryBoard && hasYouthCouncil) {
    return null;
  }

  return (
    <p className="empty-text">
      Attendance uses the same active program source as Program Management. If Senator Brian Williams’ Youth Advisory Board
      or Congressman Wesley Bell’s Youth Council is missing here, create it as an active program in Program Management first.
    </p>
  );
}

function AttendanceSessionList({
  busy,
  expandedSessionKey,
  programs,
  records,
  youthUsers,
  onDelete,
  onStatusUpdate,
  onToggleSession,
}) {
  const sessions = groupAttendanceSessions(records, programs, youthUsers);

  if (!sessions.length) {
    return <p>No attendance records match the current filters.</p>;
  }

  return (
    <div className="staff-session-list">
      {sessions.map((session) => {
        const isExpanded = expandedSessionKey === session.key;
        return (
          <article className="staff-session-card" key={session.key}>
            <button
              className="staff-session-summary"
              type="button"
              aria-expanded={isExpanded}
              onClick={() => onToggleSession(isExpanded ? "" : session.key)}
            >
              <div>
                <strong>{session.dateLabel} — {session.eventName}</strong>
                <span>Program: {session.programName}</span>
              </div>
              <div className="staff-session-counts" aria-label="Attendance status counts">
                <span>Present: {session.counts.present}</span>
                <span>Absent: {session.counts.absent}</span>
                <span>Excused: {session.counts.excused}</span>
                <span>Pending: {session.counts.pending}</span>
              </div>
            </button>

            {isExpanded ? (
              <div className="staff-session-details">
                {session.records.map((entry) => {
                  const record = entry.record;
                  const recordId = getRecordId(record, ["attendanceRecordID", "attendanceRecordId"]);
                  return (
                    <article className="staff-record-card" key={recordId || `${record.userUID}-${record.eventDate}`}>
                      <strong>{entry.youthName}</strong>
                      <span>{entry.youthEmail || "Email not listed"} · {entry.aspnParticipantId || "No ASPN ID"}</span>
                      <span>{formatDate(record.eventDate)} · {formatStatus(record.attendanceStatus || "pending")}</span>
                      <div className="staff-inline-actions">
                        {["present", "absent", "excused", "pending"].map((status) => (
                          <button
                            className="text-action"
                            disabled={busy === recordId}
                            key={status}
                            type="button"
                            onClick={() => onStatusUpdate(record, status)}
                          >
                            Mark {formatStatus(status)}
                          </button>
                        ))}
                        <button className="text-action danger" disabled={busy === recordId} type="button" onClick={() => onDelete(record)}>
                          Delete Attendance Record
                        </button>
                      </div>
                    </article>
                  );
                })}
              </div>
            ) : null}
          </article>
        );
      })}
    </div>
  );
}

function groupAttendanceSessions(records, programs, youthUsers) {
  const sessions = new Map();

  for (const record of records) {
    const programId = record.programID || "";
    const eventName = record.eventName || "Attendance session";
    const dateKey = getDateKey(record.eventDate);
    const key = [programId, dateKey, eventName].join("|");
    const existing = sessions.get(key) || {
      key,
      date: parseRecordDate(record.eventDate),
      dateLabel: formatDate(record.eventDate),
      eventName,
      programName: getProgramNameById(programs, programId),
      counts: { present: 0, absent: 0, excused: 0, pending: 0 },
      records: [],
    };
    const status = normalizeAttendanceStatus(record.attendanceStatus);
    existing.counts[status] = (existing.counts[status] || 0) + 1;
    existing.records.push({
      record,
      ...getYouthDetails(youthUsers, record.userUID),
    });
    sessions.set(key, existing);
  }

  return [...sessions.values()]
    .map((session) => ({
      ...session,
      records: session.records.sort((left, right) => left.youthName.localeCompare(right.youthName)),
    }))
    .sort((left, right) => {
      if (left.date && right.date) return right.date.getTime() - left.date.getTime();
      if (left.date) return -1;
      if (right.date) return 1;
      return left.eventName.localeCompare(right.eventName);
    });
}

function cleanFilters(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ""));
}

function buildAttendanceConfirmation(record, action) {
  const lines = [
    `${action}?`,
    "",
    `Youth UID: ${record.userUID || "Not entered"}`,
    `Program ID: ${record.programID || "Not entered"}`,
    `Attendance status: ${record.attendanceStatus || "pending"}`,
  ];

  if (record.attendanceStatus === "present") {
    lines.push("", "Present attendance may trigger attendance-based credential rules.");
  }

  return lines.join("\n");
}

function buildCreateAttendanceConfirmation(details) {
  const lines = [
    "Create attendance record?",
    "",
    `Youth name: ${details.youthName || "Not resolved"}`,
    `Youth email: ${details.youthEmail || "Not resolved"}`,
    `ASPN Participant ID: ${details.aspnParticipantId || "Not resolved"}`,
    `Firebase UID: ${details.firebaseUid || "Not resolved"}`,
    `Program name: ${details.programName || "Not selected"}`,
    `Program ID: ${details.programID || "Not selected"}`,
    `Event/session: ${details.eventName || "Not entered"}`,
    `Date: ${details.eventDate || "Not selected"}`,
    `Attendance status: ${details.attendanceStatus || "pending"}`,
  ];

  if (details.userIdentifier && !details.youthName) {
    lines.push("", "Youth identity could not be resolved in the loaded staff list. The backend will still check the ASPN Participant ID or Firebase UID before creating attendance.");
  }

  if (details.attendanceStatus === "present") {
    lines.push("", "Present attendance may trigger attendance-based credential rules.");
  }

  return lines.join("\n");
}

function buildAttendanceDetails(form, programs, youthUsers) {
  const userIdentifier = getUserIdentifier(form);
  const youth = findYouthByIdentifier(youthUsers, userIdentifier);
  const program = programs.find((item) => getProgramId(item) === form.programID);

  return {
    userIdentifier,
    youthName: youth ? formatYouthName(youth) : "",
    youthEmail: youth?.email || "",
    aspnParticipantId: youth?.aspnParticipantId || (isAspnParticipantId(userIdentifier) ? userIdentifier : ""),
    firebaseUid: youth ? getUserUid(youth) : (isAspnParticipantId(userIdentifier) ? "" : userIdentifier),
    programName: program ? getProgramName(program) : "",
    programID: form.programID,
    eventName: form.eventName.trim(),
    eventDate: form.eventDate,
    attendanceStatus: form.attendanceStatus || "pending",
  };
}

function getUserIdentifier(form) {
  return (form.userIdentifier || form.selectedYouthUid || form.userUID || "").trim();
}

function isAspnParticipantId(value) {
  return value.trim().toUpperCase().startsWith("ASPN-");
}

function getProgramId(program) {
  return program?.programId || program?.programID || "";
}

function getProgramName(program) {
  return program?.programName || program?.title || program?.name || getProgramId(program) || "Untitled program";
}

function getProgramOptionLabel(program) {
  return `${getProgramName(program)} — ${program?.programStatus || program?.status || "active"}`;
}

function getProgramNameById(programs, programId) {
  const program = programs.find((item) => getProgramId(item) === programId);
  return program ? getProgramName(program) : programId || "Program not listed";
}

function getYouthDetails(youthUsers, userUID) {
  const youth = youthUsers.find((item) => getUserUid(item) === userUID);
  if (!youth) {
    return {
      youthName: userUID || "Unknown youth",
      youthEmail: "",
      aspnParticipantId: "",
    };
  }

  return {
    youthName: formatYouthName(youth),
    youthEmail: youth.email || "",
    aspnParticipantId: youth.aspnParticipantId || "",
  };
}

function normalizeAttendanceStatus(status) {
  const normalized = String(status || "pending").toLowerCase();
  return ["present", "absent", "excused", "pending"].includes(normalized) ? normalized : "pending";
}

function parseRecordDate(value) {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function getDateKey(value) {
  const date = parseRecordDate(value);
  return date ? date.toISOString().slice(0, 10) : "no-date";
}

function formatStatus(value) {
  const text = String(value || "").trim();
  return text ? text.charAt(0).toUpperCase() + text.slice(1) : "Status";
}
