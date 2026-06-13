import { useEffect, useMemo, useState } from "react";
import {
  activateStaffGovernmentOrganization,
  createStaffGovernmentOrganization,
  deactivateStaffGovernmentOrganization,
  fetchStaffGovernmentOrganization,
  fetchStaffGovernmentOrganizations,
  fetchStaffGovernmentOrganizationTotals,
  updateStaffGovernmentOrganization,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const governmentLevels = ["municipal", "county", "regional", "state", "federal", "international"];
const organizationTypes = [
  "city_government",
  "county_government",
  "state_agency",
  "school_district",
  "workforce_board",
  "public_authority",
  "public_university",
  "other",
];
const emptyFilters = {
  organizationName: "",
  governmentLevel: "",
  organizationType: "",
  active: "",
  workforcePartner: "",
  credentialPartner: "",
};
const emptyGovernmentOrganization = {
  organizationName: "",
  governmentLevel: "municipal",
  organizationType: "city_government",
  website: "",
  primaryContactName: "",
  primaryContactTitle: "",
  primaryContactEmail: "",
  primaryContactPhone: "",
  active: true,
  workforcePartner: false,
  credentialPartner: false,
  notes: "",
};

export function GovernmentOrganizationManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [search, setSearch] = useState("");
  const [organizations, setOrganizations] = useState([]);
  const [totals, setTotals] = useState({});
  const [selectedOrganization, setSelectedOrganization] = useState(null);
  const [form, setForm] = useState(emptyGovernmentOrganization);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const selectedOrganizationId = selectedOrganization ? getGovernmentOrganizationId(selectedOrganization) : "";
  const visibleOrganizations = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) {
      return organizations;
    }

    return organizations.filter((organization) => {
      const haystack = [
        organization.organizationName,
        organization.primaryContactName,
        organization.primaryContactTitle,
        organization.primaryContactEmail,
        organization.website,
      ].join(" ").toLowerCase();
      return haystack.includes(query);
    });
  }, [organizations, search]);

  useEffect(() => {
    loadOrganizations();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadOrganizations(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const clean = cleanFilters(nextFilters);
      const [organizationData, totalData] = await Promise.all([
        fetchStaffGovernmentOrganizations(user, clean),
        fetchStaffGovernmentOrganizationTotals(user),
      ]);
      setOrganizations(organizationData);
      setTotals(totalData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setOrganizations([]);
      setTotals({});
    } finally {
      setLoading(false);
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadOrganizations(filters);
  }

  async function handleSelect(organization) {
    const organizationId = getGovernmentOrganizationId(organization);
    setMessage("");
    setError("");

    try {
      const detail = await fetchStaffGovernmentOrganization(user, organizationId);
      setSelectedOrganization(detail);
      setForm(toForm(detail));
    } catch (nextError) {
      setSelectedOrganization(organization);
      setForm(toForm(organization));
      setError(getStaffPageError(nextError));
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setBusy("save");
    setMessage("");
    setError("");

    try {
      if (selectedOrganizationId) {
        await updateStaffGovernmentOrganization(user, selectedOrganizationId, buildGovernmentOrganizationPayload(form));
        setMessage("Government organization was updated.");
        await refreshSelected(selectedOrganizationId);
      } else {
        const organizationId = await createStaffGovernmentOrganization(user, buildGovernmentOrganizationPayload(form));
        setMessage(`Government organization was created: ${organizationId}`);
        setForm(emptyGovernmentOrganization);
        setSelectedOrganization(null);
      }
      await loadOrganizations();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleStatus(active) {
    if (!selectedOrganizationId) {
      return;
    }

    setBusy(active ? "activate" : "deactivate");
    setMessage("");
    setError("");

    try {
      if (active) {
        await activateStaffGovernmentOrganization(user, selectedOrganizationId);
        setMessage("Government organization was activated.");
      } else {
        await deactivateStaffGovernmentOrganization(user, selectedOrganizationId);
        setMessage("Government organization was deactivated.");
      }
      await loadOrganizations();
      await refreshSelected(selectedOrganizationId);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function refreshSelected(organizationId) {
    const detail = await fetchStaffGovernmentOrganization(user, organizationId);
    setSelectedOrganization(detail);
    setForm(toForm(detail));
  }

  function handleNewOrganization() {
    setSelectedOrganization(null);
    setForm(emptyGovernmentOrganization);
    setMessage("");
    setError("");
  }

  if (loading) {
    return <StaffState title="Loading government organizations" message="Retrieving government/public-sector organization records and totals." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Government Partner Management</h2>
        <p>Manage public-sector relationships for long-term municipal workforce and Future Ready strategy.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <SummaryGrid
        items={[
          { label: "Government Organizations", value: totals.totalGovernmentOrganizations ?? organizations.length },
          { label: "Active", value: totals.activeGovernmentOrganizations ?? 0 },
          { label: "Inactive", value: totals.inactiveGovernmentOrganizations ?? 0 },
          { label: "Workforce Partners", value: totals.workforcePartners ?? 0 },
          { label: "Credential Partners", value: totals.credentialPartners ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <form className="compact-form" onSubmit={handleFilterSubmit}>
            <input placeholder="Organization" value={filters.organizationName} onChange={(event) => setFilters({ ...filters, organizationName: event.target.value })} />
            <select value={filters.governmentLevel} onChange={(event) => setFilters({ ...filters, governmentLevel: event.target.value })}>
              <option value="">All government levels</option>
              {governmentLevels.map((level) => (
                <option key={level} value={level}>{level}</option>
              ))}
            </select>
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
            <select value={filters.workforcePartner} onChange={(event) => setFilters({ ...filters, workforcePartner: event.target.value })}>
              <option value="">All workforce flags</option>
              <option value="true">workforce partner</option>
              <option value="false">not workforce partner</option>
            </select>
            <select value={filters.credentialPartner} onChange={(event) => setFilters({ ...filters, credentialPartner: event.target.value })}>
              <option value="">All credential flags</option>
              <option value="true">credential partner</option>
              <option value="false">not credential partner</option>
            </select>
            <button className="primary-action" type="submit">Apply Filters</button>
          </form>

          <h3>Search</h3>
          <input
            className="staff-search-input"
            placeholder="Organization, contact, title, email, or website"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />

          <GroupedCountPanel title="Organizations By Government Level" values={totals.organizationsByGovernmentLevel} />
          <GroupedCountPanel title="Organizations By Type" values={totals.organizationsByOrganizationType} />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Government Organizations</h3>
              <p>{visibleOrganizations.length} government organization{visibleOrganizations.length === 1 ? "" : "s"} shown.</p>
            </div>
            <button className="text-action" type="button" onClick={handleNewOrganization}>
              New Government Partner
            </button>
          </div>

          <div className="staff-record-list">
            {visibleOrganizations.length === 0 ? <p>No government organizations match the current filters.</p> : null}
            {visibleOrganizations.map((organization) => {
              const organizationId = getGovernmentOrganizationId(organization);
              return (
                <button
                  className={selectedOrganizationId === organizationId ? "staff-record-card selected" : "staff-record-card"}
                  key={organizationId}
                  type="button"
                  onClick={() => handleSelect(organization)}
                >
                  <strong>{organization.organizationName || "Unnamed government organization"}</strong>
                  <span>{organization.governmentLevel || "level missing"} · {organization.organizationType || "type missing"} · {organization.active ? "active" : "inactive"}</span>
                  <span>{organization.workforcePartner ? "workforce" : "no workforce flag"} · {organization.credentialPartner ? "credential" : "no credential flag"}</span>
                  <span>{organization.primaryContactName || "No contact"} · {organization.primaryContactEmail || "No email"}</span>
                </button>
              );
            })}
          </div>

          <section className="staff-detail-panel">
            <div className="section-header">
              <div>
                <h3>{selectedOrganizationId ? "Government Detail" : "Create Government Partner"}</h3>
                <p>{selectedOrganizationId || "Add a staff-managed government/public-sector relationship record."}</p>
              </div>
              {selectedOrganization ? <span className="status-pill">{selectedOrganization.active ? "Active" : "Inactive"}</span> : null}
            </div>

            {selectedOrganization ? (
              <dl className="staff-detail-list">
                <div><dt>Website</dt><dd>{selectedOrganization.website || "Not set"}</dd></div>
                <div><dt>Contact</dt><dd>{selectedOrganization.primaryContactName || "No contact"}</dd></div>
                <div><dt>Title</dt><dd>{selectedOrganization.primaryContactTitle || "No title"}</dd></div>
                <div><dt>Email</dt><dd>{selectedOrganization.primaryContactEmail || "No email"}</dd></div>
                <div><dt>Phone</dt><dd>{selectedOrganization.primaryContactPhone || "No phone"}</dd></div>
                <div><dt>Notes</dt><dd>{selectedOrganization.notes || "No notes"}</dd></div>
              </dl>
            ) : null}

            <GovernmentOrganizationForm
              busy={busy === "save"}
              form={form}
              onSubmit={handleSubmit}
              selectedOrganizationId={selectedOrganizationId}
              setForm={setForm}
            />

            {selectedOrganizationId ? (
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

function GovernmentOrganizationForm({ busy, form, onSubmit, selectedOrganizationId, setForm }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input required placeholder="Organization name" value={form.organizationName} onChange={(event) => setForm({ ...form, organizationName: event.target.value })} />
      <select value={form.governmentLevel} onChange={(event) => setForm({ ...form, governmentLevel: event.target.value })}>
        {governmentLevels.map((level) => (
          <option key={level} value={level}>{level}</option>
        ))}
      </select>
      <select value={form.organizationType} onChange={(event) => setForm({ ...form, organizationType: event.target.value })}>
        {organizationTypes.map((type) => (
          <option key={type} value={type}>{type}</option>
        ))}
      </select>
      <input placeholder="Website" value={form.website} onChange={(event) => setForm({ ...form, website: event.target.value })} />
      <input placeholder="Primary contact name" value={form.primaryContactName} onChange={(event) => setForm({ ...form, primaryContactName: event.target.value })} />
      <input placeholder="Primary contact title" value={form.primaryContactTitle} onChange={(event) => setForm({ ...form, primaryContactTitle: event.target.value })} />
      <input placeholder="Primary contact email" type="email" value={form.primaryContactEmail} onChange={(event) => setForm({ ...form, primaryContactEmail: event.target.value })} />
      <input placeholder="Primary contact phone" value={form.primaryContactPhone} onChange={(event) => setForm({ ...form, primaryContactPhone: event.target.value })} />
      <select value={String(form.active)} onChange={(event) => setForm({ ...form, active: event.target.value === "true" })}>
        <option value="true">active</option>
        <option value="false">inactive</option>
      </select>
      <label className="checkbox-row">
        <input checked={form.workforcePartner} type="checkbox" onChange={(event) => setForm({ ...form, workforcePartner: event.target.checked })} />
        Workforce partner
      </label>
      <label className="checkbox-row">
        <input checked={form.credentialPartner} type="checkbox" onChange={(event) => setForm({ ...form, credentialPartner: event.target.checked })} />
        Credential partner
      </label>
      <textarea placeholder="Notes" rows="3" value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} />
      <button className="primary-action" disabled={busy} type="submit">
        {busy ? "Saving..." : selectedOrganizationId ? "Update Government Partner" : "Create Government Partner"}
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

function buildGovernmentOrganizationPayload(form) {
  return {
    organizationName: form.organizationName.trim(),
    governmentLevel: form.governmentLevel,
    organizationType: form.organizationType,
    website: form.website.trim(),
    primaryContactName: form.primaryContactName.trim(),
    primaryContactTitle: form.primaryContactTitle.trim(),
    primaryContactEmail: form.primaryContactEmail.trim(),
    primaryContactPhone: form.primaryContactPhone.trim(),
    active: form.active,
    workforcePartner: form.workforcePartner,
    credentialPartner: form.credentialPartner,
    notes: form.notes.trim(),
  };
}

function toForm(organization) {
  return {
    organizationName: organization.organizationName || "",
    governmentLevel: organization.governmentLevel || "municipal",
    organizationType: organization.organizationType || "city_government",
    website: organization.website || "",
    primaryContactName: organization.primaryContactName || "",
    primaryContactTitle: organization.primaryContactTitle || "",
    primaryContactEmail: organization.primaryContactEmail || "",
    primaryContactPhone: organization.primaryContactPhone || "",
    active: organization.active !== false,
    workforcePartner: Boolean(organization.workforcePartner),
    credentialPartner: Boolean(organization.credentialPartner),
    notes: organization.notes || "",
  };
}

function getGovernmentOrganizationId(organization) {
  return getRecordId(organization, ["governmentOrganizationId", "governmentOrganizationID"]);
}
