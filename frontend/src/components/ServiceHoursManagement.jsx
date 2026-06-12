import { useEffect, useState } from "react";
import {
  approveStaffServiceHour,
  createOrReviewStaffServiceHourRecord,
  deleteStaffServiceHour,
  fetchStaffServiceHours,
  fetchStaffServiceHourTotals,
  rejectStaffServiceHour,
  updateStaffServiceHourStatus,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { formatDate, getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const emptyFilters = { userUID: "", status: "", programId: "", serviceDate: "" };
const emptyForm = {
  userUID: "",
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
  const [records, setRecords] = useState([]);
  const [totals, setTotals] = useState({});
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
      const [recordData, totalData] = await Promise.all([
        fetchStaffServiceHours(user, clean),
        fetchStaffServiceHourTotals(user, clean),
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
    await loadServiceHours(filters);
  }

  async function handleCreate(event) {
    event.preventDefault();
    setBusy("create");
    setMessage("");
    setError("");

    try {
      await createOrReviewStaffServiceHourRecord(user, {
        ...form,
        hours: Number(form.hours || 0),
        serviceDate: form.serviceDate ? `${form.serviceDate}T00:00:00.000Z` : null,
      });
      setMessage("Service-hour record was created.");
      setForm(emptyForm);
      await loadServiceHours();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleStatus(record, status) {
    const recordId = getServiceHourId(record);
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
          <ServiceHourForm form={form} setForm={setForm} onSubmit={handleCreate} busy={busy === "create"} />
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
              return (
                <article className="staff-record-card" key={recordId}>
                  <strong>{record.description || "Service-hour record"}</strong>
                  <span>{record.userUID || "Unknown youth"} · {record.programId || "No program"}</span>
                  <span>{formatDate(record.serviceDate)} · {record.hours ?? 0} hours · {record.verificationStatus || "pending"}</span>
                  <div className="staff-inline-actions">
                    {["pending", "verified", "rejected"].map((status) => (
                      <button className="text-action" disabled={busy === recordId} key={status} type="button" onClick={() => handleStatus(record, status)}>
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

function ServiceHourForm({ form, setForm, onSubmit, busy }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input required placeholder="Youth UID" value={form.userUID} onChange={(event) => setForm({ ...form, userUID: event.target.value })} />
      <input required placeholder="Program ID" value={form.programId} onChange={(event) => setForm({ ...form, programId: event.target.value })} />
      <input required type="date" value={form.serviceDate} onChange={(event) => setForm({ ...form, serviceDate: event.target.value })} />
      <input min="0" required step="0.25" type="number" placeholder="Hours" value={form.hours} onChange={(event) => setForm({ ...form, hours: event.target.value })} />
      <select value={form.verificationStatus} onChange={(event) => setForm({ ...form, verificationStatus: event.target.value })}>
        <option value="pending">pending</option>
        <option value="verified">verified</option>
        <option value="rejected">rejected</option>
      </select>
      <textarea placeholder="Description" rows="3" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
      <button className="primary-action" disabled={busy} type="submit">{busy ? "Saving..." : "Create Record"}</button>
    </form>
  );
}

function cleanFilters(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ""));
}

function getServiceHourId(record) {
  return getRecordId(record, ["serviceHourRecordId", "serviceHourRecordID"]);
}
