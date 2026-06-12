import { useEffect, useState } from "react";
import {
  createStaffAttendanceRecord,
  deleteStaffAttendanceRecord,
  fetchStaffAttendanceRecords,
  fetchStaffAttendanceTotals,
  updateStaffAttendanceRecord,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { formatDate, getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const emptyFilters = { userUID: "", programID: "", eventDate: "" };
const emptyForm = { userUID: "", programID: "", eventName: "", eventDate: "", attendanceStatus: "present" };

export function AttendanceManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [form, setForm] = useState(emptyForm);
  const [records, setRecords] = useState([]);
  const [totals, setTotals] = useState({});
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
      const [recordData, totalData] = await Promise.all([
        fetchStaffAttendanceRecords(user, cleanFilters(nextFilters)),
        fetchStaffAttendanceTotals(user, cleanFilters(nextFilters)),
      ]);
      setRecords(recordData);
      setTotals(totalData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setRecords([]);
      setTotals({});
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
    setBusy("create");
    setMessage("");
    setError("");

    try {
      await createStaffAttendanceRecord(user, {
        ...form,
        eventDate: form.eventDate ? `${form.eventDate}T00:00:00.000Z` : null,
      });
      setMessage("Attendance record was created.");
      setForm(emptyForm);
      await loadAttendance();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleStatusUpdate(record, attendanceStatus) {
    const attendanceRecordID = getRecordId(record, ["attendanceRecordID", "attendanceRecordId"]);
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

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <StaffFilterForm filters={filters} setFilters={setFilters} onSubmit={handleFilterSubmit} />
          <h3>Create Record</h3>
          <AttendanceForm form={form} setForm={setForm} onSubmit={handleCreate} busy={busy === "create"} />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Attendance Records</h3>
              <p>{records.length} record{records.length === 1 ? "" : "s"} loaded.</p>
            </div>
          </div>
          <div className="staff-record-list">
            {records.length === 0 ? <p>No attendance records match the current filters.</p> : null}
            {records.map((record) => {
              const recordId = getRecordId(record, ["attendanceRecordID", "attendanceRecordId"]);
              return (
                <article className="staff-record-card" key={recordId || `${record.userUID}-${record.eventDate}`}>
                  <strong>{record.eventName || "Attendance record"}</strong>
                  <span>{record.userUID || "Unknown youth"} · {record.programID || "No program"}</span>
                  <span>{formatDate(record.eventDate)} · {record.attendanceStatus || "pending"}</span>
                  <div className="staff-inline-actions">
                    {["present", "absent", "excused", "pending"].map((status) => (
                      <button
                        className="text-action"
                        disabled={busy === recordId}
                        key={status}
                        type="button"
                        onClick={() => handleStatusUpdate(record, status)}
                      >
                        {status}
                      </button>
                    ))}
                    <button className="text-action danger" disabled={busy === recordId} type="button" onClick={() => handleDelete(record)}>
                      Delete
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

function AttendanceForm({ form, setForm, onSubmit, busy }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input required placeholder="Youth UID" value={form.userUID} onChange={(event) => setForm({ ...form, userUID: event.target.value })} />
      <input required placeholder="Program ID" value={form.programID} onChange={(event) => setForm({ ...form, programID: event.target.value })} />
      <input required placeholder="Event name" value={form.eventName} onChange={(event) => setForm({ ...form, eventName: event.target.value })} />
      <input required type="date" value={form.eventDate} onChange={(event) => setForm({ ...form, eventDate: event.target.value })} />
      <select value={form.attendanceStatus} onChange={(event) => setForm({ ...form, attendanceStatus: event.target.value })}>
        <option value="present">present</option>
        <option value="absent">absent</option>
        <option value="excused">excused</option>
        <option value="pending">pending</option>
      </select>
      <button className="primary-action" disabled={busy} type="submit">{busy ? "Saving..." : "Create Attendance"}</button>
    </form>
  );
}

function cleanFilters(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ""));
}
