import { useEffect, useMemo, useState } from "react";
import {
  createStaffStakeholderRelationshipNote,
  deleteStaffStakeholderRelationshipNote,
  fetchStaffStakeholderRelationshipNote,
  fetchStaffStakeholderRelationshipNotes,
  fetchStaffStakeholderRelationshipNoteTotals,
  updateStaffStakeholderRelationshipNote,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { formatDate, getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const stakeholderTypes = ["educator", "partner_organization", "government_organization"];
const relationshipStatuses = [
  "prospect",
  "contacted",
  "meeting_scheduled",
  "active_partner",
  "inactive_partner",
  "declined",
];
const stakeholderTypeLabels = {
  educator: "Educators",
  partner_organization: "Partner Organizations",
  government_organization: "Government Organizations",
};
const relationshipStatusLabels = {
  prospect: "Prospect",
  contacted: "Contacted",
  meeting_scheduled: "Meeting Scheduled",
  active_partner: "Active Partner",
  inactive_partner: "Inactive Partner",
  declined: "Declined",
};
const emptyFilters = {
  stakeholderType: "",
  stakeholderId: "",
  relationshipStatus: "",
  relationshipOwnerUID: "",
  active: "",
  nextFollowUpBefore: "",
  nextFollowUpAfter: "",
};
const emptyNote = {
  stakeholderType: "educator",
  stakeholderId: "",
  stakeholderName: "",
  noteText: "",
  relationshipStatus: "prospect",
  relationshipOwnerUID: "",
  lastContactDate: "",
  nextFollowUpDate: "",
  active: true,
};

export function StakeholderRelationshipNotes() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [search, setSearch] = useState("");
  const [notes, setNotes] = useState([]);
  const [totals, setTotals] = useState({});
  const [selectedNote, setSelectedNote] = useState(null);
  const [form, setForm] = useState(emptyNote);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const selectedNoteId = selectedNote ? getNoteId(selectedNote) : "";
  const visibleNotes = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) {
      return notes;
    }

    return notes.filter((note) => {
      const haystack = [
        note.stakeholderName,
        note.stakeholderType,
        note.stakeholderId,
        note.relationshipStatus,
        note.relationshipOwnerUID,
        note.noteText,
      ]
        .join(" ")
        .toLowerCase();
      return haystack.includes(query);
    });
  }, [notes, search]);
  const upcomingNotes = useMemo(() => getFollowUpNotes(notes, "upcoming"), [notes]);
  const overdueNotes = useMemo(() => getFollowUpNotes(notes, "overdue"), [notes]);

  useEffect(() => {
    loadNotes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function loadNotes(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const clean = cleanFilters(nextFilters);
      const [noteData, totalData] = await Promise.all([
        fetchStaffStakeholderRelationshipNotes(user, clean),
        fetchStaffStakeholderRelationshipNoteTotals(user),
      ]);
      setNotes(noteData);
      setTotals(totalData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setNotes([]);
      setTotals({});
    } finally {
      setLoading(false);
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadNotes(filters);
  }

  async function handleSelect(note) {
    const noteId = getNoteId(note);
    setMessage("");
    setError("");

    try {
      const detail = await fetchStaffStakeholderRelationshipNote(user, noteId);
      setSelectedNote(detail);
      setForm(toForm(detail));
    } catch (nextError) {
      setSelectedNote(note);
      setForm(toForm(note));
      setError(getStaffPageError(nextError));
    }
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setBusy("save");
    setMessage("");
    setError("");

    try {
      if (selectedNoteId) {
        await updateStaffStakeholderRelationshipNote(user, selectedNoteId, buildNotePayload(form));
        setMessage("Relationship note was updated.");
        await refreshSelected(selectedNoteId);
      } else {
        const noteId = await createStaffStakeholderRelationshipNote(user, buildNotePayload(form));
        setMessage(`Relationship note was created: ${noteId}`);
        setForm(emptyNote);
        setSelectedNote(null);
      }
      await loadNotes();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleDelete() {
    if (!selectedNoteId) {
      return;
    }

    const confirmed = window.confirm(buildRelationshipDeleteConfirmation(selectedNote, selectedNoteId));
    if (!confirmed) {
      return;
    }

    setBusy("delete");
    setMessage("");
    setError("");

    try {
      await deleteStaffStakeholderRelationshipNote(user, selectedNoteId);
      setMessage("Relationship note was deleted.");
      setSelectedNote(null);
      setForm(emptyNote);
      await loadNotes();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function refreshSelected(noteId) {
    const detail = await fetchStaffStakeholderRelationshipNote(user, noteId);
    setSelectedNote(detail);
    setForm(toForm(detail));
  }

  function handleNewNote() {
    setSelectedNote(null);
    setForm(emptyNote);
    setMessage("");
    setError("");
  }

  if (loading) {
    return <StaffState title="Loading relationship notes" message="Retrieving stakeholder relationship records." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Stakeholder Relationship Notes</h2>
        <p>Track partnership stage, follow-up timing, and engagement history across external stakeholder directories.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Relationship Pipeline Summary</h3>
            <p>Current relationship-note volume and follow-up pressure across external stakeholder records.</p>
          </div>
        </div>
      </section>

      <SummaryGrid
        items={[
          { label: "Total Notes", value: totals.totalNotes ?? notes.length },
          { label: "Active Notes", value: totals.activeNotes ?? 0 },
          { label: "Inactive Notes", value: totals.inactiveNotes ?? 0 },
          { label: "Upcoming Follow-Ups", value: totals.upcomingFollowUps ?? upcomingNotes.length },
          { label: "Overdue Follow-Ups", value: totals.overdueFollowUps ?? overdueNotes.length },
        ]}
      />

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <form className="compact-form" onSubmit={handleFilterSubmit}>
            <select value={filters.stakeholderType} onChange={(event) => setFilters({ ...filters, stakeholderType: event.target.value })}>
              <option value="">All stakeholder types</option>
              {stakeholderTypes.map((type) => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
            <input placeholder="Stakeholder ID" value={filters.stakeholderId} onChange={(event) => setFilters({ ...filters, stakeholderId: event.target.value })} />
            <select value={filters.relationshipStatus} onChange={(event) => setFilters({ ...filters, relationshipStatus: event.target.value })}>
              <option value="">All relationship statuses</option>
              {relationshipStatuses.map((status) => (
                <option key={status} value={status}>{status}</option>
              ))}
            </select>
            <input placeholder="Owner UID" value={filters.relationshipOwnerUID} onChange={(event) => setFilters({ ...filters, relationshipOwnerUID: event.target.value })} />
            <select value={filters.active} onChange={(event) => setFilters({ ...filters, active: event.target.value })}>
              <option value="">All active states</option>
              <option value="true">active</option>
              <option value="false">inactive</option>
            </select>
            <label>
              Follow-up after
              <input type="date" value={filters.nextFollowUpAfter} onChange={(event) => setFilters({ ...filters, nextFollowUpAfter: event.target.value })} />
            </label>
            <label>
              Follow-up before
              <input type="date" value={filters.nextFollowUpBefore} onChange={(event) => setFilters({ ...filters, nextFollowUpBefore: event.target.value })} />
            </label>
            <button className="primary-action" type="submit">Apply Filters</button>
          </form>

          <h3>Search</h3>
          <input
            className="staff-search-input"
            placeholder="Stakeholder, status, owner, ID, or note text"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />

          <GroupedCountPanel
            expectedKeys={stakeholderTypes}
            labels={stakeholderTypeLabels}
            title="Stakeholder Type Breakdown"
            values={totals.notesByStakeholderType}
          />
          <GroupedCountPanel
            expectedKeys={relationshipStatuses}
            labels={relationshipStatusLabels}
            title="Relationship Status Breakdown"
            values={totals.notesByRelationshipStatus}
          />
          <GroupedCountPanel title="Staff Ownership View" values={totals.notesByRelationshipOwnerUID} />
          <GroupedCountPanel
            expectedKeys={stakeholderTypes}
            labels={stakeholderTypeLabels}
            title="Upcoming Follow-Ups By Type"
            values={totals.upcomingFollowUpsByStakeholderType}
          />
          <GroupedCountPanel
            expectedKeys={stakeholderTypes}
            labels={stakeholderTypeLabels}
            title="Overdue Follow-Ups By Type"
            values={totals.overdueFollowUpsByStakeholderType}
          />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Relationship Notes</h3>
              <p>{visibleNotes.length} note{visibleNotes.length === 1 ? "" : "s"} shown.</p>
            </div>
            <button className="text-action" type="button" onClick={handleNewNote}>
              New Note
            </button>
          </div>

          <section className="staff-detail-panel">
            <h3>Follow-Up Management</h3>
            <p>Use these tables to see which external relationships need staff attention next.</p>
          </section>
          <FollowUpPanel title="Overdue Follow-Ups" notes={overdueNotes} onSelect={handleSelect} />
          <FollowUpPanel title="Upcoming Follow-Ups" notes={upcomingNotes} onSelect={handleSelect} />

          <div className="staff-record-list">
            {visibleNotes.length === 0 ? <p>No relationship notes match the current filters.</p> : null}
            {visibleNotes.map((note) => {
              const noteId = getNoteId(note);
              return (
                <button
                  className={selectedNoteId === noteId ? "staff-record-card selected" : "staff-record-card"}
                  key={noteId}
                  type="button"
                  onClick={() => handleSelect(note)}
                >
                  <strong>{note.stakeholderName || note.stakeholderId || "Unnamed stakeholder"}</strong>
                  <span>{note.stakeholderType || "stakeholder"} · {note.relationshipStatus || "prospect"} · {note.active === false ? "inactive" : "active"}</span>
                  <span>Owner: {note.relationshipOwnerUID || "unassigned"} · Next follow-up: {formatDate(note.nextFollowUpDate)}</span>
                </button>
              );
            })}
          </div>

          <section className="staff-detail-panel">
            <div className="section-header">
              <div>
                <h3>{selectedNoteId ? "Relationship Detail" : "Create Relationship Note"}</h3>
                <p>{selectedNoteId || "Add a staff-managed note for an educator, partner, or government organization."}</p>
              </div>
              {selectedNote ? <span className="status-pill">{selectedNote.active === false ? "Inactive" : "Active"}</span> : null}
            </div>

            {selectedNote ? (
              <dl className="staff-detail-list">
                <div><dt>Stakeholder ID</dt><dd>{selectedNote.stakeholderId || "Not set"}</dd></div>
                <div><dt>Owner UID</dt><dd>{selectedNote.relationshipOwnerUID || "Backend default"}</dd></div>
                <div><dt>Last Contact</dt><dd>{formatDate(selectedNote.lastContactDate)}</dd></div>
                <div><dt>Next Follow-Up</dt><dd>{formatDate(selectedNote.nextFollowUpDate)}</dd></div>
                <div><dt>Note</dt><dd>{selectedNote.noteText || "No note text"}</dd></div>
              </dl>
            ) : null}

            <RelationshipNoteForm
              busy={busy === "save"}
              form={form}
              onSubmit={handleSubmit}
              selectedNoteId={selectedNoteId}
              setForm={setForm}
            />

            {selectedNoteId ? (
              <div className="staff-inline-actions">
                <button className="text-action danger" disabled={busy === "delete"} type="button" onClick={handleDelete}>
                  {busy === "delete" ? "Deleting..." : "Delete Relationship Note"}
                </button>
              </div>
            ) : null}
          </section>
        </div>
      </section>
    </div>
  );
}

function RelationshipNoteForm({ busy, form, onSubmit, selectedNoteId, setForm }) {
  return (
    <form className="compact-form" onSubmit={onSubmit}>
      <select required value={form.stakeholderType} onChange={(event) => setForm({ ...form, stakeholderType: event.target.value })}>
        {stakeholderTypes.map((type) => (
          <option key={type} value={type}>{type}</option>
        ))}
      </select>
      <input required placeholder="Stakeholder ID" value={form.stakeholderId} onChange={(event) => setForm({ ...form, stakeholderId: event.target.value })} />
      <input placeholder="Stakeholder name" value={form.stakeholderName} onChange={(event) => setForm({ ...form, stakeholderName: event.target.value })} />
      <select value={form.relationshipStatus} onChange={(event) => setForm({ ...form, relationshipStatus: event.target.value })}>
        {relationshipStatuses.map((status) => (
          <option key={status} value={status}>{status}</option>
        ))}
      </select>
      <input placeholder="Relationship owner UID" value={form.relationshipOwnerUID} onChange={(event) => setForm({ ...form, relationshipOwnerUID: event.target.value })} />
      <label>
        Last contact
        <input type="date" value={form.lastContactDate} onChange={(event) => setForm({ ...form, lastContactDate: event.target.value })} />
      </label>
      <label>
        Next follow-up
        <input type="date" value={form.nextFollowUpDate} onChange={(event) => setForm({ ...form, nextFollowUpDate: event.target.value })} />
      </label>
      <select value={String(form.active)} onChange={(event) => setForm({ ...form, active: event.target.value === "true" })}>
        <option value="true">active</option>
        <option value="false">inactive</option>
      </select>
      <textarea required placeholder="Relationship note" rows="4" value={form.noteText} onChange={(event) => setForm({ ...form, noteText: event.target.value })} />
      <button className="primary-action" disabled={busy} type="submit">
        {busy ? "Saving..." : selectedNoteId ? "Update Note" : "Create Note"}
      </button>
    </form>
  );
}

function FollowUpPanel({ notes, onSelect, title }) {
  return (
    <section className="staff-detail-panel">
      <h3>{title}</h3>
      {notes.length === 0 ? (
        <p>No {title.toLowerCase()}.</p>
      ) : (
        <div className="staff-mini-table">
          {notes.slice(0, 5).map((note) => (
            <button key={getNoteId(note)} type="button" onClick={() => onSelect(note)}>
              <span>
                <strong>{note.stakeholderName || note.stakeholderId || "Stakeholder"}</strong>
                <small>{note.stakeholderType || "stakeholder"} · {note.relationshipStatus || "prospect"} · {note.relationshipOwnerUID || "unassigned"}</small>
              </span>
              <strong>{formatDate(note.nextFollowUpDate)}</strong>
            </button>
          ))}
        </div>
      )}
    </section>
  );
}

function GroupedCountPanel({ expectedKeys = [], labels = {}, title, values = {} }) {
  const merged = { ...(values || {}) };
  expectedKeys.forEach((key) => {
    if (merged[key] === undefined) {
      merged[key] = 0;
    }
  });
  const rows = Object.entries(merged);

  return (
    <section className="staff-detail-panel">
      <h3>{title}</h3>
      {rows.length === 0 ? (
        <p>No data available yet.</p>
      ) : (
        <div className="staff-mini-table">
          {rows.map(([label, count]) => (
            <div key={label}>
              <span>{labels[label] || label || "other"}</span>
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

function buildNotePayload(form) {
  return {
    stakeholderType: form.stakeholderType,
    stakeholderId: form.stakeholderId.trim(),
    stakeholderName: form.stakeholderName.trim(),
    noteText: form.noteText.trim(),
    relationshipStatus: form.relationshipStatus,
    relationshipOwnerUID: form.relationshipOwnerUID.trim(),
    lastContactDate: form.lastContactDate || null,
    nextFollowUpDate: form.nextFollowUpDate || null,
    active: form.active,
  };
}

function toForm(note) {
  return {
    stakeholderType: note.stakeholderType || "educator",
    stakeholderId: note.stakeholderId || "",
    stakeholderName: note.stakeholderName || "",
    noteText: note.noteText || "",
    relationshipStatus: note.relationshipStatus || "prospect",
    relationshipOwnerUID: note.relationshipOwnerUID || "",
    lastContactDate: dateInputValue(note.lastContactDate),
    nextFollowUpDate: dateInputValue(note.nextFollowUpDate),
    active: note.active !== false,
  };
}

function getFollowUpNotes(notes, mode) {
  const today = startOfToday();
  return notes
    .filter((note) => note.active !== false && note.nextFollowUpDate)
    .filter((note) => {
      const date = new Date(note.nextFollowUpDate);
      if (Number.isNaN(date.getTime())) {
        return false;
      }
      return mode === "overdue" ? date < today : date >= today;
    })
    .sort((a, b) => new Date(a.nextFollowUpDate).getTime() - new Date(b.nextFollowUpDate).getTime());
}

function dateInputValue(value) {
  if (!value) {
    return "";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return date.toISOString().slice(0, 10);
}

function startOfToday() {
  const date = new Date();
  date.setHours(0, 0, 0, 0);
  return date;
}

function getNoteId(note) {
  return getRecordId(note, ["stakeholderRelationshipNoteId", "stakeholderRelationshipNoteID"]);
}

function buildRelationshipDeleteConfirmation(note, noteId) {
  return [
    `Delete this relationship note for ${note?.stakeholderName || note?.stakeholderId || "this stakeholder"}?`,
    "",
    `Record type: Relationship note`,
    `Note ID: ${noteId || "Not available"}`,
    `Stakeholder ID: ${note?.stakeholderId || "Not set"}`,
    `Relationship status: ${note?.relationshipStatus || "Not set"}`,
    "This may affect staff relationship tracking and follow-up reporting.",
  ].join("\n");
}
