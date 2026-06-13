import { useEffect, useMemo, useState } from "react";
import {
  activateStaffPartnerOrganization,
  createStaffPartnerOrganization,
  deactivateStaffPartnerOrganization,
  fetchStaffPartnerOrganization,
  fetchStaffPartnerOrganizations,
  fetchStaffPartnerOrganizationTotals,
  updateStaffPartnerOrganization,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const organizationTypes = [
  "nonprofit",
  "foundation",
  "workforce",
  "business",
  "higher_education",
  "faith_based",
  "government_affiliated",
  "community",
  "other",
];
const emptyFilters = { organizationName: "", organizationType: "", active: "" };
const emptyPartner = {
  organizationName: "",
  organizationType: "nonprofit",
  website: "",
  primaryContactName: "",
  primaryContactEmail: "",
  primaryContactPhone: "",
  active: true,
  notes: "",
};

export function PartnerOrganizationManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [search, setSearch] = useState("");
  const [partners, setPartners] = useState([]);
  const [totals, setTotals] = useState({});
  const [selectedPartner, setSelectedPartner] = useState(null);
  const [form, setForm] = useState(emptyPartner);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const selectedPartnerId = selectedPartner ? getPartnerId(selectedPartner) : "";
  const visiblePartners = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) {
      return partners;
    }

    return partners.filter((partner) => {
      const haystack = [
        partner.organizationName,
        partner.primaryContactName,
        partner.primaryContactEmail,
        partner.website,
      ].join(" ").toLowerCase();
      return haystack.includes(query);
    });
  }, [partners, search]);

  useEffect(() => {
    loadPartners();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadPartners(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const clean = cleanFilters(nextFilters);
      const [partnerData, totalData] = await Promise.all([
        fetchStaffPartnerOrganizations(user, clean),
        fetchStaffPartnerOrganizationTotals(user),
      ]);
      setPartners(partnerData);
      setTotals(totalData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setPartners([]);
      setTotals({});
    } finally {
      setLoading(false);
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadPartners(filters);
  }

  async function handleSelect(partner) {
    const partnerId = getPartnerId(partner);
    setMessage("");
    setError("");

    try {
      const detail = await fetchStaffPartnerOrganization(user, partnerId);
      setSelectedPartner(detail);
      setForm(toForm(detail));
    } catch (nextError) {
      setSelectedPartner(partner);
      setForm(toForm(partner));
      setError(getStaffPageError(nextError));
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setBusy("save");
    setMessage("");
    setError("");

    try {
      if (selectedPartnerId) {
        await updateStaffPartnerOrganization(user, selectedPartnerId, buildPartnerPayload(form));
        setMessage("Partner organization was updated.");
        await refreshSelected(selectedPartnerId);
      } else {
        const partnerId = await createStaffPartnerOrganization(user, buildPartnerPayload(form));
        setMessage(`Partner organization was created: ${partnerId}`);
        setForm(emptyPartner);
        setSelectedPartner(null);
      }
      await loadPartners();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleStatus(active) {
    if (!selectedPartnerId) {
      return;
    }

    setBusy(active ? "activate" : "deactivate");
    setMessage("");
    setError("");

    try {
      if (active) {
        await activateStaffPartnerOrganization(user, selectedPartnerId);
        setMessage("Partner organization was activated.");
      } else {
        await deactivateStaffPartnerOrganization(user, selectedPartnerId);
        setMessage("Partner organization was deactivated.");
      }
      await loadPartners();
      await refreshSelected(selectedPartnerId);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function refreshSelected(partnerId) {
    const detail = await fetchStaffPartnerOrganization(user, partnerId);
    setSelectedPartner(detail);
    setForm(toForm(detail));
  }

  function handleNewPartner() {
    setSelectedPartner(null);
    setForm(emptyPartner);
    setMessage("");
    setError("");
  }

  if (loading) {
    return <StaffState title="Loading partners" message="Retrieving partner organization records and totals." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Partner Organization Management</h2>
        <p>Manage external partner organization relationships for pilot and post-pilot coordination.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <SummaryGrid
        items={[
          { label: "Total Partners", value: totals.totalPartners ?? partners.length },
          { label: "Active Partners", value: totals.activePartners ?? 0 },
          { label: "Inactive Partners", value: totals.inactivePartners ?? 0 },
          { label: "Types Represented", value: totals.organizationTypesRepresented ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <form className="compact-form" onSubmit={handleFilterSubmit}>
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

          <h3>Search</h3>
          <input
            className="staff-search-input"
            placeholder="Organization, contact, email, or website"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />

          <GroupedCountPanel title="Partners By Organization Type" values={totals.partnersByOrganizationType} />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Partner Organizations</h3>
              <p>{visiblePartners.length} partner organization{visiblePartners.length === 1 ? "" : "s"} shown.</p>
            </div>
            <button className="text-action" type="button" onClick={handleNewPartner}>
              New Partner
            </button>
          </div>

          <div className="staff-record-list">
            {visiblePartners.length === 0 ? <p>No partner organizations match the current filters.</p> : null}
            {visiblePartners.map((partner) => {
              const partnerId = getPartnerId(partner);
              return (
                <button
                  className={selectedPartnerId === partnerId ? "staff-record-card selected" : "staff-record-card"}
                  key={partnerId}
                  type="button"
                  onClick={() => handleSelect(partner)}
                >
                  <strong>{partner.organizationName || "Unnamed partner"}</strong>
                  <span>{partner.organizationType || "other"} · {partner.active ? "active" : "inactive"}</span>
                  <span>{partner.primaryContactName || "No contact"} · {partner.primaryContactEmail || "No email"}</span>
                </button>
              );
            })}
          </div>

          <section className="staff-detail-panel">
            <div className="section-header">
              <div>
                <h3>{selectedPartnerId ? "Partner Detail" : "Create Partner"}</h3>
                <p>{selectedPartnerId || "Add a staff-managed partner organization relationship record."}</p>
              </div>
              {selectedPartner ? <span className="status-pill">{selectedPartner.active ? "Active" : "Inactive"}</span> : null}
            </div>

            {selectedPartner ? (
              <dl className="staff-detail-list">
                <div><dt>Website</dt><dd>{selectedPartner.website || "Not set"}</dd></div>
                <div><dt>Contact</dt><dd>{selectedPartner.primaryContactName || "No contact"}</dd></div>
                <div><dt>Email</dt><dd>{selectedPartner.primaryContactEmail || "No email"}</dd></div>
                <div><dt>Phone</dt><dd>{selectedPartner.primaryContactPhone || "No phone"}</dd></div>
                <div><dt>Notes</dt><dd>{selectedPartner.notes || "No notes"}</dd></div>
              </dl>
            ) : null}

            <PartnerForm
              busy={busy === "save"}
              form={form}
              onSubmit={handleSubmit}
              selectedPartnerId={selectedPartnerId}
              setForm={setForm}
            />

            {selectedPartnerId ? (
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

function PartnerForm({ busy, form, onSubmit, selectedPartnerId, setForm }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <input required placeholder="Organization name" value={form.organizationName} onChange={(event) => setForm({ ...form, organizationName: event.target.value })} />
      <select value={form.organizationType} onChange={(event) => setForm({ ...form, organizationType: event.target.value })}>
        {organizationTypes.map((type) => (
          <option key={type} value={type}>{type}</option>
        ))}
      </select>
      <input placeholder="Website" value={form.website} onChange={(event) => setForm({ ...form, website: event.target.value })} />
      <input placeholder="Primary contact name" value={form.primaryContactName} onChange={(event) => setForm({ ...form, primaryContactName: event.target.value })} />
      <input placeholder="Primary contact email" type="email" value={form.primaryContactEmail} onChange={(event) => setForm({ ...form, primaryContactEmail: event.target.value })} />
      <input placeholder="Primary contact phone" value={form.primaryContactPhone} onChange={(event) => setForm({ ...form, primaryContactPhone: event.target.value })} />
      <select value={String(form.active)} onChange={(event) => setForm({ ...form, active: event.target.value === "true" })}>
        <option value="true">active</option>
        <option value="false">inactive</option>
      </select>
      <textarea placeholder="Notes" rows="3" value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} />
      <button className="primary-action" disabled={busy} type="submit">
        {busy ? "Saving..." : selectedPartnerId ? "Update Partner" : "Create Partner"}
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

function buildPartnerPayload(form) {
  return {
    organizationName: form.organizationName.trim(),
    organizationType: form.organizationType,
    website: form.website.trim(),
    primaryContactName: form.primaryContactName.trim(),
    primaryContactEmail: form.primaryContactEmail.trim(),
    primaryContactPhone: form.primaryContactPhone.trim(),
    active: form.active,
    notes: form.notes.trim(),
  };
}

function toForm(partner) {
  return {
    organizationName: partner.organizationName || "",
    organizationType: partner.organizationType || "nonprofit",
    website: partner.website || "",
    primaryContactName: partner.primaryContactName || "",
    primaryContactEmail: partner.primaryContactEmail || "",
    primaryContactPhone: partner.primaryContactPhone || "",
    active: partner.active !== false,
    notes: partner.notes || "",
  };
}

function getPartnerId(partner) {
  return getRecordId(partner, ["partnerOrganizationId", "partnerOrganizationID"]);
}
