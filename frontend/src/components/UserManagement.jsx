import { useEffect, useMemo, useState } from "react";
import {
  activateStaffUser,
  deactivateStaffUser,
  fetchStaffUser,
  fetchStaffUsers,
  fetchStaffUserTotals,
  updateStaffUser,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getRecordId, getStaffPageError, StaffMessage, StaffState, SummaryGrid } from "./staffUi.jsx";

const emptyFilters = { role: "", active: "", youthProfile: "", programId: "" };
const roles = ["member", "staff", "admin", "educator", "partner", "government"];

export function UserManagement() {
  const { user } = useAuth();
  const [filters, setFilters] = useState(emptyFilters);
  const [search, setSearch] = useState("");
  const [users, setUsers] = useState([]);
  const [totals, setTotals] = useState({});
  const [selectedUser, setSelectedUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const visibleUsers = useMemo(() => {
    const query = search.trim().toLowerCase();
    if (!query) {
      return users;
    }

    return users.filter((item) => {
      const haystack = [
        item.firstName,
        item.lastName,
        item.email,
        item.aspnParticipantId,
        item.uid,
      ].join(" ").toLowerCase();
      return haystack.includes(query);
    });
  }, [search, users]);

  async function loadUsers(nextFilters = filters) {
    setLoading(true);
    setError("");

    try {
      const clean = cleanFilters(nextFilters);
      const [userData, totalData] = await Promise.all([
        fetchStaffUsers(user, clean),
        fetchStaffUserTotals(user),
      ]);
      setUsers(userData);
      setTotals(totalData);
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setUsers([]);
      setTotals({});
    } finally {
      setLoading(false);
    }
  }

  async function handleFilterSubmit(event) {
    event.preventDefault();
    await loadUsers(filters);
  }

  async function handleSelect(nextUser) {
    const uid = getUserUid(nextUser);
    setError("");
    setMessage("");

    try {
      setSelectedUser(await fetchStaffUser(user, uid));
    } catch (nextError) {
      setSelectedUser(nextUser);
      setError(getStaffPageError(nextError));
    }
  }

  async function handleStatus(profileStatus) {
    if (!selectedUser) {
      return;
    }

    const uid = getUserUid(selectedUser);
    setBusy(profileStatus);
    setError("");
    setMessage("");

    try {
      if (profileStatus === "active") {
        await activateStaffUser(user, uid);
        setMessage("User was activated.");
      } else {
        await deactivateStaffUser(user, uid);
        setMessage("User was deactivated.");
      }
      await loadUsers();
      setSelectedUser(await fetchStaffUser(user, uid));
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  async function handleReviewToggle(field, value) {
    if (!selectedUser) {
      return;
    }

    const uid = getUserUid(selectedUser);
    setBusy(field);
    setError("");
    setMessage("");

    try {
      await updateStaffUser(user, uid, { [field]: value });
      setMessage("User review fields were updated.");
      await loadUsers();
      setSelectedUser(await fetchStaffUser(user, uid));
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setBusy("");
    }
  }

  if (loading) {
    return <StaffState title="Loading users" message="Retrieving staff user records and totals." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>User Management</h2>
        <p>Review users, filter by role or status, and activate or deactivate profiles using protected staff routes.</p>
      </section>

      <StaffMessage>{message}</StaffMessage>
      <StaffMessage type="error">{error}</StaffMessage>

      <SummaryGrid
        items={[
          { label: "Total Users", value: totals.totalUsers ?? users.length },
          { label: "Active Users", value: totals.activeUsers ?? 0 },
          { label: "Youth", value: totals.youthUsers ?? 0 },
          { label: "Staff", value: totals.staffUsers ?? 0 },
          { label: "Educators", value: totals.educatorUsers ?? 0 },
          { label: "Partners", value: totals.partnerUsers ?? 0 },
          { label: "Government", value: totals.governmentUsers ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <h3>Filters</h3>
          <form className="compact-form" onSubmit={handleFilterSubmit}>
            <select value={filters.role} onChange={(event) => setFilters({ ...filters, role: event.target.value })}>
              <option value="">All roles</option>
              {roles.map((role) => (
                <option key={role} value={role}>{role}</option>
              ))}
            </select>
            <select value={filters.active} onChange={(event) => setFilters({ ...filters, active: event.target.value })}>
              <option value="">All statuses</option>
              <option value="true">active</option>
              <option value="false">inactive</option>
            </select>
            <select value={filters.youthProfile} onChange={(event) => setFilters({ ...filters, youthProfile: event.target.value })}>
              <option value="">All profile types</option>
              <option value="true">Youth profiles</option>
              <option value="false">Non-youth profiles</option>
            </select>
            <input placeholder="Program ID" value={filters.programId} onChange={(event) => setFilters({ ...filters, programId: event.target.value })} />
            <button className="primary-action" type="submit">Apply Filters</button>
          </form>

          <h3>Search</h3>
          <input
            className="staff-search-input"
            placeholder="Name, email, ASPN ID, or UID"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>User Table</h3>
              <p>{visibleUsers.length} user{visibleUsers.length === 1 ? "" : "s"} shown.</p>
            </div>
          </div>

          <div className="staff-record-list">
            {visibleUsers.length === 0 ? <p>No users match the current filters.</p> : null}
            {visibleUsers.map((item) => {
              const uid = getUserUid(item);
              return (
                <button
                  className={selectedUser && getUserUid(selectedUser) === uid ? "staff-record-card selected" : "staff-record-card"}
                  key={uid}
                  type="button"
                  onClick={() => handleSelect(item)}
                >
                  <strong>{formatUserName(item)}</strong>
                  <span>{item.email || "No email"} · {item.role || "member"} · {item.profileStatus || "status missing"}</span>
                  <span>{item.aspnParticipantId || "No ASPN ID"} · {uid}</span>
                </button>
              );
            })}
          </div>

          {selectedUser ? (
            <section className="staff-detail-panel">
              <h3>User Detail</h3>
              <dl className="staff-detail-list">
                <div><dt>Name</dt><dd>{formatUserName(selectedUser)}</dd></div>
                <div><dt>Email</dt><dd>{selectedUser.email || "Not set"}</dd></div>
                <div><dt>UID</dt><dd>{getUserUid(selectedUser)}</dd></div>
                <div><dt>ASPN ID</dt><dd>{selectedUser.aspnParticipantId || "Not assigned"}</dd></div>
                <div><dt>Role</dt><dd>{selectedUser.role || "member"}</dd></div>
                <div><dt>Status</dt><dd>{selectedUser.profileStatus || "missing"}</dd></div>
                <div><dt>School</dt><dd>{selectedUser.school || "Not set"}</dd></div>
                <div><dt>Graduation Year</dt><dd>{selectedUser.graduationYear || "Not set"}</dd></div>
              </dl>
              <div className="staff-inline-actions">
                <button className="text-action" disabled={busy === "active"} type="button" onClick={() => handleStatus("active")}>
                  Activate
                </button>
                <button className="text-action danger" disabled={busy === "inactive"} type="button" onClick={() => handleStatus("inactive")}>
                  Deactivate
                </button>
                <button
                  className="text-action"
                  disabled={busy === "staffVerified"}
                  type="button"
                  onClick={() => handleReviewToggle("staffVerified", !selectedUser.staffVerified)}
                >
                  {selectedUser.staffVerified ? "Mark Unverified" : "Mark Verified"}
                </button>
                <button
                  className="text-action"
                  disabled={busy === "staffReviewRequired"}
                  type="button"
                  onClick={() => handleReviewToggle("staffReviewRequired", !selectedUser.staffReviewRequired)}
                >
                  {selectedUser.staffReviewRequired ? "Clear Review" : "Require Review"}
                </button>
              </div>
            </section>
          ) : null}
        </div>
      </section>
    </div>
  );
}

function cleanFilters(filters) {
  return Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== ""));
}

function getUserUid(user) {
  return getRecordId(user, ["uid", "userUID", "userUid"]);
}

function formatUserName(user) {
  const name = [user.firstName, user.lastName].filter(Boolean).join(" ").trim();
  return name || user.email || getUserUid(user) || "Unnamed user";
}
