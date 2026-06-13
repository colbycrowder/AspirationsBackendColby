import { useEffect, useMemo, useState } from "react";
import {
  activateStaffEducator,
  createStaffEducator,
  deactivateStaffEducator,
  fetchStaffEducator,
  fetchStaffEducators,
  fetchStaffEducatorTotals,
  updateStaffEducator,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const organizationTypes = ["high_school", "middle_school", "college", "nonprofit", "government", "other"];
const emptyFilters = { search: "", organizationName: "", organizationType: "", active: "" };
const emptyEducator = {
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  title: "",
  organizationName: "",
  organizationType: "high_school",
  active: true,
  notes: "",
};

export function EducatorManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [educators, setEducators] = useState([]);
  const [totals, setTotals] = useState({});
  const [selectedEducator, setSelectedEducator] = useState(null);
  const [form, setForm] = useState(emptyEducator);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const selectedEducatorId = selectedEducator ? getEducatorId(selectedEducator) : "";
  const visibleEducators = useMemo(() => educators, [educators]);

  useEffect(() => {
    loadEducators();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadEducators(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const clean = cleanFilters(nextFilters);
      const [educatorData, totalData] = await Promise.all([
        fetchStaffEducators(user, clean),
        fetchStaffEducatorTotals(user),
      ]);
      setEducators(educatorData);
      setTotals(totalData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setEducators([]);
      setTotals({});
    } finally {
      setLoading(false);
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadEducators(filters);
  }

  async function handleSelect(educator) {
    const educatorId = getEducatorId(educator);
    setMessage("");
    setError("");

    try {
      const detail = await fetchStaffEducator(user, educatorId);
      setSelectedEducator(detail);
      setForm(toForm(detail));
    } catch (nextError) {
      setSelectedEducator(educator);
      setForm(toForm(educator));
      setError(getStaffPageError(nextError));
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setBusy("save");
    setMessage("");
    setError("");

    try {
      if (selectedEducatorId) {
        await updateStaffEducator(user, selectedEducatorId, buildEducatorPayload(form));
        setMessage("Educator record was updated.");
        await refreshSelected(selectedEducatorId);
      } else {
        const educatorId = await createStaffEducator(user, buildEducatorPayload(form));
        setMessage(`Educator record was created: ${educatorId}`);
        setForm(emptyEducator);
        setSelectedEducator(null);
      }
      await loadEducators();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleStatus(active) {
    if (!selectedEducatorId) {
      return;
    }

    setBusy(active ? "activate" : "deactivate");
    setMessage("");
    setError("");

    try {
      if (active) {
        await activateStaffEducator(user, selectedEducatorId);
        setMessage("Educator record was activated.");
      } else {
        await deactivateStaffEducator(user, selectedEducatorId);
        setMessage("Educator record was deactivated.");
      }
      await loadEducators();
      await refreshSelected(selectedEducatorId);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function refreshSelected(educatorId) {
    const detail = await fetchStaffEducator(user, educatorId);
    setSelectedEducator(detail);
    setForm(toForm(detail));
  }

  function handleNewEducator() {
    setSelectedEducator(null);
    setForm(emptyEducator);
    setMessage("");
    setError("");
  }

  if (loading) {
    return <StaffState title="Loading educators" message="Retrieving educator directory records and totals." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Educator Management</h2>
        <p>Manage educator contacts and organization relationships for pilot coordination.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <SummaryGrid
        items={[
          { label: "Total Educators", value: totals.totalEducators ?? educators.length },
          { label: "Active Educators", value: totals.activeEducators ?? 0 },
          { label: "Inactive Educators", value: totals.inactiveEducators ?? 0 },
          { label: "Organizations", value: totals.organizationsRepresented ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <form className="compact-form" onSubmit={handleFilterSubmit}>
            <input placeholder="Name, email, or title" value={filters.search} onChange={(event) => setFilters({ ...filters, search: event.target.value })} />
            <input placeholder="Organization" value={filters.organizationName} onChange={(event) => setFilters({ ...filters, organizationName: event.target.value })} />
            <select value={filters.organizationType} onChange={(event) => setFilters({ ...filters, organizationType: event.target.value })}>
              <option value="">All organization types</option>
              {organizationTypes.map((type) => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
            <select value={filters.active} onChange={(event) => setFilters({ ...filters, active: event.target.value })}>
              <option value="">All statuses</option>
              <option value="true">active</option>
              <option value="false">inactive</option>
            </select>
            <button className="primary-action" type="submit">Apply Filters</button>
          </form>

          <GroupedCountPanel title="Educators By Organization Type" values={totals.educatorsByOrganizationType} />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Educator Directory</h3>
              <p>{visibleEducators.length} educator{visibleEducators.length === 1 ? "" : "s"} shown.</p>
            </div>
            <button className="text-action" type="button" onClick={handleNewEducator}>
              New Educator
            </button>
          </div>

          <div className="staff-record-list">
            {visibleEducators.length === 0 ? <p>No educators match the current filters.</p> : null}
            {visibleEducators.map((educator) => {
              const educatorId = getEducatorId(educator);
              return (
                <button
                  className={selectedEducatorId === educatorId ? "staff-record-card selected" : "staff-record-card"}
                  key={educatorId}
                  type="button"
                  onClick={() => handleSelect(educator)}
                >
                  <strong>{formatEducatorName(educator)}</strong>
                  <span>{educator.organizationName || "No organization"} · {educator.title || "No title"}</span>
                  <span>{educator.organizationType || "other"} · {educator.active ? "active" : "inactive"}</span>
                </button>
              );
            })}
          </div>

          <section className="staff-detail-panel">
            <div className="section-header">
              <div>
                <h3>{selectedEducatorId ? "Educator Detail" : "Create Educator"}</h3>
                <p>{selectedEducatorId || "Add a staff-managed educator relationship record."}</p>
              </div>
              {selectedEducator ? <span className="status-pill">{selectedEducator.active ? "Active" : "Inactive"}</span> : null}
            </div>

            {selectedEducator ? (
              <dl className="staff-detail-list">
                <div><dt>Contact</dt><dd>{selectedEducator.email || "No email"} · {selectedEducator.phone || "No phone"}</dd></div>
                <div><dt>Organization</dt><dd>{selectedEducator.organizationName || "Not set"}</dd></div>
                <div><dt>Role/Title</dt><dd>{selectedEducator.title || "Not set"}</dd></div>
                <div><dt>Notes</dt><dd>{selectedEducator.notes || "No notes"}</dd></div>
              </dl>
            ) : null}

            <EducatorForm
              busy={busy === "save"}
              form={form}
              onSubmit={handleSubmit}
              selectedEducatorId={selectedEducatorId}
              setForm={setForm}
            />

            {selectedEducatorId ? (
              <div className="staff-inline-actions">
                <button className="text-action" disabled={busy === "activate"} type="button" onClick={() => handleStatus(true)}>
                  Activate
                </button>
                <button className="text-action danger" disabled={busy === "deactivate"} type="button" onClick={() => handleStatus(false)}>
                  Deactivate
                </button>
              </div>
            ) : null}
          </section>
        </div>
      </section>
    </div>
  );
}

function EducatorForm({ busy, form, onSubmit, selectedEducatorId, setForm }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input required placeholder="First name" value={form.firstName} onChange={(event) => setForm({ ...form, firstName: event.target.value })} />
      <input required placeholder="Last name" value={form.lastName} onChange={(event) => setForm({ ...form, lastName: event.target.value })} />
      <input required placeholder="Email" type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
      <input placeholder="Phone" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} />
      <input placeholder="Title or role" value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} />
      <input required placeholder="Organization" value={form.organizationName} onChange={(event) => setForm({ ...form, organizationName: event.target.value })} />
      <select value={form.organizationType} onChange={(event) => setForm({ ...form, organizationType: event.target.value })}>
        {organizationTypes.map((type) => (
          <option key={type} value={type}>{type}</option>
        ))}
      </select>
      <select value={String(form.active)} onChange={(event) => setForm({ ...form, active: event.target.value === "true" })}>
        <option value="true">active</option>
        <option value="false">inactive</option>
      </select>
      <textarea placeholder="Notes" rows="3" value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} />
      <button className="primary-action" disabled={busy} type="submit">
        {busy ? "Saving..." : selectedEducatorId ? "Update Educator" : "Create Educator"}
      </button>
    </form>
  );
}

function GroupedCountPanel({ title, values = {} }) {
  const rows = Object.entries(values || {});

  return (
    <section className="staff-detail-panel">
      <h3>{title}</h3>
      {rows.length === 0 ? (
        <p>No data available yet.</p>
      ) : (
        <div className="staff-mini-table">
          {rows.map(([label, count]) => (
            <div key={label}>
              <span>{label || "other"}</span>
              <strong>{count ?? 0}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

function cleanFilters(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ""));
}

function buildEducatorPayload(form) {
  return {
    firstName: form.firstName.trim(),
    lastName: form.lastName.trim(),
    email: form.email.trim(),
    phone: form.phone.trim(),
    title: form.title.trim(),
    organizationName: form.organizationName.trim(),
    organizationType: form.organizationType,
    active: form.active,
    notes: form.notes.trim(),
  };
}

function toForm(educator) {
  return {
    firstName: educator.firstName || "",
    lastName: educator.lastName || "",
    email: educator.email || "",
    phone: educator.phone || "",
    title: educator.title || "",
    organizationName: educator.organizationName || "",
    organizationType: educator.organizationType || "high_school",
    active: educator.active !== false,
    notes: educator.notes || "",
  };
}

function getEducatorId(educator) {
  return getRecordId(educator, ["educatorId", "educatorID"]);
}

function formatEducatorName(educator) {
  const name = [educator.firstName, educator.lastName].filter(Boolean).join(" ").trim();
  return name || educator.email || getEducatorId(educator) || "Unnamed educator";
}
