import { useEffect, useMemo, useState } from "react";
import {
  ApiAccessError,
  createStaffProgram,
  fetchActivePrograms,
  fetchStaffProgramEnrollments,
  removeStaffProgramEnrollment,
  updateStaffProgram,
} from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

const emptyProgramForm = {
  programName: "",
  description: "",
  category: "",
  programImageUrl: "",
  programLeader: "",
  capacity: "",
  programStatus: "active",
};

export function StaffProgramManagementPage() {
  const { user } = useAuth();
  const [programs, setPrograms] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [selectedProgramId, setSelectedProgramId] = useState("");
  const [programForm, setProgramForm] = useState(emptyProgramForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [removingEnrollmentId, setRemovingEnrollmentId] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadStaffProgramData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const selectedProgram = useMemo(
    () => programs.find((program) => getProgramId(program) === selectedProgramId) || null,
    [programs, selectedProgramId]
  );

  const visibleEnrollments = useMemo(() => {
    if (!selectedProgramId) {
      return enrollments;
    }
    return enrollments.filter((enrollment) => enrollment.programId === selectedProgramId);
  }, [enrollments, selectedProgramId]);

  useEffect(() => {
    if (!selectedProgram) {
      return;
    }

    setProgramForm({
      programName: selectedProgram.programName || "",
      description: selectedProgram.description || "",
      category: selectedProgram.category || "",
      programImageUrl: selectedProgram.programImageUrl || "",
      programLeader: selectedProgram.programLeader || "",
      capacity: selectedProgram.capacity ?? "",
      programStatus: selectedProgram.programStatus || "active",
    });
  }, [selectedProgram]);

  async function loadStaffProgramData() {
    setLoading(true);
    setError("");

    try {
      const [activePrograms, staffEnrollments] = await Promise.all([
        fetchActivePrograms(),
        fetchStaffProgramEnrollments(user),
      ]);
      setPrograms(activePrograms);
      setEnrollments(staffEnrollments);
      setSelectedProgramId((currentId) => {
        if (currentId && activePrograms.some((program) => getProgramId(program) === currentId)) {
          return currentId;
        }
        return activePrograms[0] ? getProgramId(activePrograms[0]) : "";
      });
    } catch (nextError) {
      setError(getStaffPageError(nextError));
      setPrograms([]);
      setEnrollments([]);
      setSelectedProgramId("");
    } finally {
      setLoading(false);
    }
  }

  function startCreateProgram() {
    setSelectedProgramId("");
    setProgramForm(emptyProgramForm);
    setMessage("");
    setError("");
  }

  async function handleSaveProgram(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");

    try {
      const payload = buildProgramPayload(programForm);
      if (selectedProgramId) {
        await updateStaffProgram(user, selectedProgramId, payload);
        setMessage("Program was updated.");
      } else {
        await createStaffProgram(user, payload);
        setMessage("Program was created.");
        setProgramForm(emptyProgramForm);
      }
      await loadStaffProgramData();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setSaving(false);
    }
  }

  async function handleArchiveProgram() {
    if (!selectedProgramId) {
      return;
    }

    setSaving(true);
    setError("");
    setMessage("");

    try {
      await updateStaffProgram(user, selectedProgramId, { programStatus: "archived" });
      setMessage("Program was archived. Archived programs are not returned by the current active-program list.");
      setSelectedProgramId("");
      setProgramForm(emptyProgramForm);
      await loadStaffProgramData();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setSaving(false);
    }
  }

  async function handleRemoveEnrollment(enrollmentId) {
    setRemovingEnrollmentId(enrollmentId);
    setError("");
    setMessage("");

    try {
      await removeStaffProgramEnrollment(user, enrollmentId);
      setMessage("Enrollment was removed.");
      await loadStaffProgramData();
    } catch (nextError) {
      setError(getStaffPageError(nextError));
    } finally {
      setRemovingEnrollmentId("");
    }
  }

  if (loading) {
    return <StaffState title="Loading programs" message="Retrieving active programs and enrollments." />;
  }

  if (error && programs.length === 0 && enrollments.length === 0) {
    return <StaffState title="Program management unavailable" message={error} />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Program Management</h2>
        <p>Create active pilot programs, update active programs, archive programs, and review enrollments.</p>
      </section>

      {message ? (
        <section className="message-panel success">
          <p>{message}</p>
        </section>
      ) : null}

      {error ? (
        <section className="message-panel error">
          <p>{error}</p>
        </section>
      ) : null}

      <section className="staff-management-grid">
        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Active Programs</h3>
              <p>{programs.length} active program{programs.length === 1 ? "" : "s"} available.</p>
            </div>
            <button className="text-action" type="button" onClick={startCreateProgram}>
              New Program
            </button>
          </div>

          {programs.length === 0 ? (
            <p>No active programs are available yet.</p>
          ) : (
            <div className="staff-record-list" aria-label="Active programs">
              {programs.map((program) => {
                const programId = getProgramId(program);
                const activeEnrollmentCount = enrollments.filter(
                  (enrollment) => enrollment.programId === programId && enrollment.enrollmentStatus === "active"
                ).length;

                return (
                  <button
                    className={selectedProgramId === programId ? "staff-record-card selected" : "staff-record-card"}
                    key={programId}
                    type="button"
                    onClick={() => setSelectedProgramId(programId)}
                  >
                    <strong>{program.programName || "Untitled program"}</strong>
                    <span>{program.category || "Category not added"} · {program.programLeader || "Leader not added"}</span>
                    <span>{activeEnrollmentCount} active enrollment{activeEnrollmentCount === 1 ? "" : "s"}</span>
                    <span className="status-tag">{program.programStatus || "active"}</span>
                  </button>
                );
              })}
            </div>
          )}
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>{selectedProgramId ? "Update Program" : "Create Program"}</h3>
              <p>Staff/admin changes are saved through protected backend routes.</p>
            </div>
          </div>

          <form className="profile-form" onSubmit={handleSaveProgram}>
            <div className="form-grid">
              <label>
                Program Name
                <input
                  required
                  value={programForm.programName}
                  onChange={(event) => updateProgramForm("programName", event.target.value)}
                />
              </label>

              <label>
                Category
                <input
                  value={programForm.category}
                  onChange={(event) => updateProgramForm("category", event.target.value)}
                />
              </label>

              <label>
                Program Leader
                <input
                  value={programForm.programLeader}
                  onChange={(event) => updateProgramForm("programLeader", event.target.value)}
                />
              </label>

              <label>
                Capacity
                <input
                  min="0"
                  type="number"
                  value={programForm.capacity}
                  onChange={(event) => updateProgramForm("capacity", event.target.value)}
                />
              </label>

              <label>
                Status
                <select
                  value={programForm.programStatus}
                  onChange={(event) => updateProgramForm("programStatus", event.target.value)}
                >
                  <option value="active">Active</option>
                  <option value="archived">Archived</option>
                </select>
              </label>

              <label>
                Program Image URL
                <input
                  value={programForm.programImageUrl}
                  onChange={(event) => updateProgramForm("programImageUrl", event.target.value)}
                />
              </label>

              <label className="full-width-field">
                Description
                <textarea
                  rows="4"
                  value={programForm.description}
                  onChange={(event) => updateProgramForm("description", event.target.value)}
                />
              </label>
            </div>

            <div className="profile-actions">
              <button className="primary-action" disabled={saving} type="submit">
                {saving ? "Saving..." : selectedProgramId ? "Update Program" : "Create Program"}
              </button>
              {selectedProgramId ? (
                <button className="text-action" disabled={saving} type="button" onClick={handleArchiveProgram}>
                  Archive Program
                </button>
              ) : null}
              <button className="text-action" disabled={saving} type="button" onClick={loadStaffProgramData}>
                Refresh
              </button>
            </div>
          </form>
        </div>
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Enrollment Review</h3>
            <p>
              Showing {visibleEnrollments.length} enrollment{visibleEnrollments.length === 1 ? "" : "s"}
              {selectedProgramId ? " for the selected active program." : " across all programs."}
            </p>
          </div>
          {selectedProgramId ? (
            <button className="text-action" type="button" onClick={() => setSelectedProgramId("")}>
              Show All
            </button>
          ) : null}
        </div>

        {visibleEnrollments.length === 0 ? (
          <p>No enrollments are available for this view.</p>
        ) : (
          <div className="staff-enrollment-list" aria-label="Program enrollments">
            {visibleEnrollments.map((enrollment) => {
              const enrollmentId = enrollment.enrollmentId || `${enrollment.userUID}-${enrollment.programId}`;
              const isActive = enrollment.enrollmentStatus === "active";

              return (
                <article className="service-record" key={enrollmentId}>
                  <div className="service-record-header">
                    <div>
                      <strong>{getProgramName(programs, enrollment.programId)}</strong>
                      <span>User UID: {enrollment.userUID || "missing"}</span>
                      <span>Enrollment ID: {enrollment.enrollmentId || "missing"}</span>
                    </div>
                    <span className={isActive ? "status-tag" : "status-tag muted"}>
                      {enrollment.enrollmentStatus || "status missing"}
                    </span>
                  </div>

                  <dl className="program-meta">
                    <div>
                      <dt>Program</dt>
                      <dd>{enrollment.programId || "missing"}</dd>
                    </div>
                    <div>
                      <dt>Enrolled</dt>
                      <dd>{formatDate(enrollment.enrolledAt)}</dd>
                    </div>
                    <div>
                      <dt>Updated</dt>
                      <dd>{formatDate(enrollment.updatedAt)}</dd>
                    </div>
                  </dl>

                  {isActive ? (
                    <button
                      className="text-action danger-action"
                      disabled={removingEnrollmentId === enrollment.enrollmentId}
                      type="button"
                      onClick={() => handleRemoveEnrollment(enrollment.enrollmentId)}
                    >
                      {removingEnrollmentId === enrollment.enrollmentId ? "Removing..." : "Remove Enrollment"}
                    </button>
                  ) : null}
                </article>
              );
            })}
          </div>
        )}
      </section>
    </div>
  );

  function updateProgramForm(field, value) {
    setProgramForm((current) => ({ ...current, [field]: value }));
  }
}

function StaffState({ title, message }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
    </section>
  );
}

function buildProgramPayload(form) {
  const payload = {};
  const textFields = ["programName", "description", "category", "programImageUrl", "programLeader", "programStatus"];

  textFields.forEach((field) => {
    if (form[field] !== "") {
      payload[field] = form[field];
    }
  });

  if (form.capacity !== "") {
    payload.capacity = Number(form.capacity);
  }

  return payload;
}

function getProgramId(program) {
  return program?.programId || "";
}

function getProgramName(programs, programId) {
  return programs.find((program) => getProgramId(program) === programId)?.programName || programId || "Unknown program";
}

function formatDate(value) {
  if (!value) {
    return "Not recorded";
  }

  const date = typeof value === "string" ? new Date(value) : new Date(value.seconds ? value.seconds * 1000 : value);
  if (Number.isNaN(date.getTime())) {
    return "Not recorded";
  }

  return date.toLocaleDateString();
}

function getStaffPageError(error) {
  if (error instanceof ApiAccessError && error.status === 401) {
    return "Sign in with a valid Firebase account before opening staff/admin tools.";
  }

  if (error instanceof ApiAccessError && error.status === 403) {
    return "Access denied. This account does not have staff or admin access.";
  }

  return error.message || "Staff/admin data is unavailable. Confirm the backend is running and try again.";
}
