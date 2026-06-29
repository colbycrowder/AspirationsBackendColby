import { useEffect, useState } from "react";
import {
  approveStaffServiceHour,
  createOrReviewStaffServiceHourRecord,
  deleteStaffServiceHour,
  fetchStaffProgramEnrollmentsForProgram,
  fetchStaffPrograms,
  fetchStaffServiceHours,
  fetchStaffServiceHourTotals,
  fetchStaffYouthUsers,
  rejectStaffServiceHour,
  updateStaffServiceHourStatus,
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

const emptyFilters = { userUID: "", status: "", programId: "", serviceDate: "" };
const emptyForm = {
  selectedYouthUid: "",
  userIdentifier: "",
  programId: "",
  serviceDate: "",
  hours: "",
  description: "",
  verificationStatus: "pending",
};

export function ServiceHoursManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [form, setForm] = useState(emptyForm);
  const [programRoster, setProgramRoster] = useState([]);
  const [records, setRecords] = useState([]);
  const [totals, setTotals] = useState({});
  const [programs, setPrograms] = useState([]);
  const [youthUsers, setYouthUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadServiceHours();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadServiceHours(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const clean = cleanFilters(nextFilters);
      const [recordData, totalData, programData, youthData] = await Promise.all([
        fetchStaffServiceHours(user, clean),
        fetchStaffServiceHourTotals(user, clean),
        fetchStaffPrograms(user),
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
    await loadServiceHours(filters);
  }

  async function handleCreate(event) {
    event.preventDefault();
    const serviceHourDetails = buildServiceHourDetails(form, programs, youthUsers);
    const confirmed = window.confirm(buildCreateServiceHourConfirmation(serviceHourDetails));
    if (!confirmed) {
      return;
    }

    setBusy("create");
    setMessage("");
    setError("");

    try {
      await createOrReviewStaffServiceHourRecord(user, {
        ...form,
        userIdentifier: serviceHourDetails.userIdentifier,
        userUID: serviceHourDetails.firebaseUid || serviceHourDetails.userIdentifier,
        hours: Number(form.hours || 0),
        serviceDate: form.serviceDate ? `${form.serviceDate}T00:00:00.000Z` : null,
      });
      setMessage("Service-hour record was created.");
      setForm(emptyForm);
      setProgramRoster([]);
      await loadServiceHours();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleProgramChange(programId) {
    setForm((current) => ({
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
      setBusy("program-roster");
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

  function handleYouthSelect(userIdentifier) {
    const rosterEntry = programRoster.find((entry) => entry.userIdentifier === userIdentifier);
    setForm((current) => ({
      ...current,
      selectedYouthUid: rosterEntry?.userUID || "",
      userIdentifier,
    }));
  }

  async function handleStatus(record, status) {
    const recordId = getServiceHourId(record);
    const confirmed = window.confirm(buildServiceHourConfirmation({ ...record, verificationStatus: status }, "Update service-hour status"));
    if (!confirmed) {
      return;
    }

    setBusy(recordId);
    setMessage("");
    setError("");

    try {
      if (status === "verified") {
        await approveStaffServiceHour(user, recordId);
      } else if (status === "rejected") {
        await rejectStaffServiceHour(user, recordId);
      } else {
        await updateStaffServiceHourStatus(user, recordId, status);
      }
      setMessage("Service-hour status was updated.");
      await loadServiceHours();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleDelete(record) {
    const recordId = getServiceHourId(record);
    const confirmed = window.confirm(
      `Delete this service-hour record for UID ${record.userUID || "unknown youth"} on ${formatDate(record.serviceDate)}? This may affect staff reporting and youth summaries.`
    );
    if (!confirmed) {
      return;
    }

    setBusy(recordId);
    setMessage("");
    setError("");

    try {
      await deleteStaffServiceHour(user, recordId);
      setMessage("Service-hour record was deleted.");
      await loadServiceHours();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  if (loading) {
    return <StaffState title="Loading service hours" message="Retrieving staff service-hour records." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Service Hours Management</h2>
        <p>Review submitted service hours, update verification status, and calculate totals.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <SummaryGrid
        items={[
          { label: "Records", value: totals.totalRecords ?? records.length },
          { label: "Total Hours", value: totals.totalHours ?? 0 },
          { label: "Pending Hours", value: totals.pendingHours ?? 0 },
          { label: "Verified Hours", value: totals.verifiedHours ?? 0 },
          { label: "Rejected Hours", value: totals.rejectedHours ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <FilterForm filters={filters} setFilters={setFilters} onSubmit={handleFilterSubmit} />
          <h3>Create / Review Record</h3>
          <ServiceHourForm
            busy={busy === "create"}
            form={form}
            loadingRoster={busy === "program-roster"}
            programs={programs}
            programRoster={programRoster}
            setForm={setForm}
            youthUsers={youthUsers}
            onProgramChange={handleProgramChange}
            onYouthSelect={handleYouthSelect}
            onSubmit={handleCreate}
          />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Service-Hour Records</h3>
              <p>{records.length} record{records.length === 1 ? "" : "s"} loaded.</p>
            </div>
          </div>
          <div className="staff-record-list">
            {records.length === 0 ? <p>No service-hour records match the current filters.</p> : null}
            {records.map((record) => {
              const recordId = getServiceHourId(record);
              const youthDetails = getYouthDetails(youthUsers, record.userUID);
              return (
                <article className="staff-record-card" key={recordId}>
                  <strong>{record.description || "Service-hour record"}</strong>
                  <span>{youthDetails.youthName} · {youthDetails.youthEmail || "Email not listed"}</span>
                  <span>{getProgramNameById(programs, record.programId)}</span>
                  <span>{formatDate(record.serviceDate)} · {record.hours ?? 0} hours · {record.verificationStatus || "pending"}</span>
                  <div className="staff-inline-actions">
                    {["pending", "verified", "rejected"].map((status) => (
                      <button className="text-action" disabled={busy === recordId} key={status} type="button" onClick={() => handleStatus(record, status)}>
                        Mark {formatStatus(status)}
                      </button>
                    ))}
                    <button className="text-action danger" disabled={busy === recordId} type="button" onClick={() => handleDelete(record)}>
                      Delete Service-Hour Record
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
      <input placeholder="Youth UID" value={filters.userUID} onChange={(event) => setFilters({ ...filters, userUID: event.target.value })} />
      <select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value })}>
        <option value="">All statuses</option>
        <option value="pending">pending</option>
        <option value="verified">verified</option>
        <option value="rejected">rejected</option>
      </select>
      <input placeholder="Program ID" value={filters.programId} onChange={(event) => setFilters({ ...filters, programId: event.target.value })} />
      <input type="date" value={filters.serviceDate} onChange={(event) => setFilters({ ...filters, serviceDate: event.target.value })} />
      <button className="primary-action" type="submit">Apply Filters</button>
    </form>
  );
}

function ServiceHourForm({
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
  const serviceHourDetails = buildServiceHourDetails(form, programs, youthUsers);
  const canCreate = Boolean(
    !busy &&
    serviceHourDetails.userIdentifier &&
    form.programId &&
    form.serviceDate &&
    form.hours !== "" &&
    Number(form.hours) >= 0 &&
    form.verificationStatus
  );

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
        <p className="empty-text">No active enrolled youth found for this program. Confirm the program roster in Program Management before creating service hours.</p>
      ) : null}
      <input required type="date" value={form.serviceDate} onChange={(event) => setForm({ ...form, serviceDate: event.target.value })} />
      <input min="0" required step="0.25" type="number" placeholder="Hours" value={form.hours} onChange={(event) => setForm({ ...form, hours: event.target.value })} />
      <select value={form.verificationStatus} onChange={(event) => setForm({ ...form, verificationStatus: event.target.value })}>
        <option value="pending">pending</option>
        <option value="verified">verified</option>
        <option value="rejected">rejected</option>
      </select>
      <textarea placeholder="Description" rows="3" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
      <section className="staff-detail-panel" aria-label="Service-hour confirmation details">
        <h4>Confirm Service-Hour Details</h4>
        <dl className="staff-detail-list">
          <div><dt>Youth Name</dt><dd>{serviceHourDetails.youthName || "Not resolved yet"}</dd></div>
          <div><dt>Youth Email</dt><dd>{serviceHourDetails.youthEmail || "Not resolved yet"}</dd></div>
          <div><dt>Program Name</dt><dd>{serviceHourDetails.programName || "Select a program"}</dd></div>
          <div><dt>Service Date</dt><dd>{serviceHourDetails.serviceDate || "Select a service date"}</dd></div>
          <div><dt>Hours</dt><dd>{serviceHourDetails.hours || "Enter hours"}</dd></div>
          <div><dt>Verification Status</dt><dd>{serviceHourDetails.verificationStatus || "pending"}</dd></div>
        </dl>
        {form.verificationStatus === "verified" ? (
          <p className="empty-text">Verified hours may appear in youth Home and My Journey summaries.</p>
        ) : null}
      </section>
      <button className="primary-action" disabled={!canCreate} type="submit">{busy ? "Saving..." : "Create Record"}</button>
    </form>
  );
}

function cleanFilters(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ""));
}

function getServiceHourId(record) {
  return getRecordId(record, ["serviceHourRecordId", "serviceHourRecordID"]);
}

function buildServiceHourConfirmation(record, action) {
  const lines = [
    `${action}?`,
    "",
    `Youth UID: ${record.userUID || "Not entered"}`,
    `Program: ${getProgramNameById([], record.programId)}`,
    `Service date: ${record.serviceDate ? formatDate(record.serviceDate) : "Not entered"}`,
    `Hours: ${record.hours ?? "Not entered"}`,
    `Verification status: ${record.verificationStatus || "pending"}`,
  ];

  if (record.verificationStatus === "verified") {
    lines.push("", "Verified hours may appear in youth Home and My Journey summaries.");
  }

  return lines.join("\n");
}

function buildCreateServiceHourConfirmation(details) {
  const lines = [
    "Create service-hour record?",
    "",
    `Youth name: ${details.youthName || "Not resolved"}`,
    `Youth email: ${details.youthEmail || "Not resolved"}`,
    `Program name: ${details.programName || "Not selected"}`,
    `Service date: ${details.serviceDate || "Not selected"}`,
    `Hours: ${details.hours || "Not entered"}`,
    `Verification status: ${details.verificationStatus || "pending"}`,
  ];

  if (details.verificationStatus === "verified") {
    lines.push("", "Verified hours may appear in youth Home and My Journey summaries.");
  }

  return lines.join("\n");
}

function buildServiceHourDetails(form, programs, youthUsers) {
  const userIdentifier = getUserIdentifier(form);
  const youth = findYouthByIdentifier(youthUsers, userIdentifier);
  const program = programs.find((item) => getProgramId(item) === form.programId);

  return {
    userIdentifier,
    youthName: youth ? formatYouthName(youth) : "",
    youthEmail: youth?.email || "",
    aspnParticipantId: youth?.aspnParticipantId || (isAspnParticipantId(userIdentifier) ? userIdentifier : ""),
    firebaseUid: youth ? getUserUid(youth) : (isAspnParticipantId(userIdentifier) ? "" : userIdentifier),
    programName: program ? getProgramName(program) : "",
    programId: form.programId,
    serviceDate: form.serviceDate,
    hours: form.hours,
    verificationStatus: form.verificationStatus || "pending",
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
  return program ? getProgramName(program) : "Program not listed";
}

function isActiveProgram(program) {
  return String(program?.programStatus || program?.status || "active").toLowerCase() === "active";
}

function getYouthDetails(youthUsers, userUID) {
  const youth = youthUsers.find((item) => getUserUid(item) === userUID);
  if (!youth) {
    return {
      youthName: userUID || "Unknown youth",
      youthEmail: "",
    };
  }

  return {
    youthName: formatYouthName(youth),
    youthEmail: youth.email || "",
  };
}

function formatStatus(value) {
  const text = String(value || "").trim();
  return text ? text.charAt(0).toUpperCase() + text.slice(1) : "Status";
}
