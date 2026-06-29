import { useEffect, useMemo, useState } from "react";
import { enrollInProgram, fetchActivePrograms, fetchYouthDashboard } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

export function ProgramsPage() {
  const { user } = useAuth();
  const [programs, setPrograms] = useState([]);
  const [enrolledProgramIds, setEnrolledProgramIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [profileNotice, setProfileNotice] = useState("");
  const [enrollmentMessage, setEnrollmentMessage] = useState("");
  const [enrollmentError, setEnrollmentError] = useState("");
  const [enrollingProgramId, setEnrollingProgramId] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadPrograms() {
      if (!user) {
        setLoading(false);
        setLoadError("Sign in before viewing youth programs.");
        return;
      }

      setLoading(true);
      setLoadError("");
      setProfileNotice("");
      setEnrollmentMessage("");
      setEnrollmentError("");

      try {
        const activePrograms = await fetchActivePrograms();
        if (!isActive) {
          return;
        }

        setPrograms(activePrograms);

        try {
          const dashboard = await fetchYouthDashboard(user);
          if (isActive) {
            setEnrolledProgramIds(getEnrolledProgramIds(dashboard?.programs));
          }
        } catch (dashboardError) {
          if (isActive) {
            setEnrolledProgramIds(new Set());
            setProfileNotice(dashboardError.message);
          }
        }
      } catch (programError) {
        if (isActive) {
          setLoadError(programError.message);
          setPrograms([]);
          setEnrolledProgramIds(new Set());
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadPrograms();

    return () => {
      isActive = false;
    };
  }, [user]);

  const sortedPrograms = useMemo(
    () => [...programs].sort((first, second) => getProgramName(first).localeCompare(getProgramName(second))),
    [programs]
  );

  async function handleEnroll(program) {
    setEnrollmentMessage("");
    setEnrollmentError("");

    if (!user) {
      setEnrollmentError("Sign in before enrolling in a program.");
      return;
    }

    const programId = program?.programId;
    if (!programId) {
      setEnrollmentError("This program is missing a program ID, so enrollment cannot continue.");
      return;
    }

    setEnrollingProgramId(programId);

    try {
      await enrollInProgram(user, programId);
      const dashboard = await fetchYouthDashboard(user);
      setEnrolledProgramIds(getEnrolledProgramIds(dashboard?.programs));
      setEnrollmentMessage(`Enrollment confirmed for ${getProgramName(program)}.`);
    } catch (error) {
      setEnrollmentError(error.message);
    } finally {
      setEnrollingProgramId("");
    }
  }

  if (loading) {
    return <ProgramsState title="Loading programs" message="Retrieving active ASPN programs." />;
  }

  if (loadError) {
    return <ProgramsState title="Programs unavailable" message={loadError} />;
  }

  return (
    <div className="programs-stack">
      <section className="page-intro">
        <span className="eyebrow">Youth Programs</span>
        <h2>Available Programs</h2>
        <p>
          Browse active ASPN programs and enroll with your signed-in account. Enrolled programs continue to appear on
          Home.
        </p>
      </section>

      {profileNotice ? (
        <section className="notice-panel">
          <strong>Profile connection needed.</strong>
          <span> {profileNotice}</span>
        </section>
      ) : null}

      {enrollmentMessage ? <MessagePanel tone="success" message={enrollmentMessage} /> : null}
      {enrollmentError ? <MessagePanel tone="error" message={enrollmentError} /> : null}

      {!sortedPrograms.length ? (
        <section className="dashboard-section">
          <p className="empty-text">No active programs are available right now.</p>
        </section>
      ) : (
        <section className="program-grid" aria-label="Available programs">
          {sortedPrograms.map((program) => {
            const programId = program.programId || getProgramName(program);
            const isEnrolled = enrolledProgramIds.has(program.programId);
            const isActive = (program.programStatus || "").toLowerCase() === "active";
            const isBusy = enrollingProgramId === program.programId;

            return (
              <article className="program-card" key={programId}>
                <div className="program-card-header">
                  <span className={isEnrolled ? "status-tag enrolled" : "status-tag"}>
                    {isEnrolled ? "Enrolled" : isActive ? "Available" : "Unavailable"}
                  </span>
                  <span className="status-tag muted">{program.programStatus || "status unavailable"}</span>
                </div>

                {program.programImageUrl ? (
                  <img className="program-image" src={program.programImageUrl} alt="" />
                ) : null}

                <div className="program-body">
                  <h3>{getProgramName(program)}</h3>
                  <p>{program.description || "No program description is available yet."}</p>
                </div>

                <dl className="program-meta">
                  <div>
                    <dt>Category</dt>
                    <dd>{program.category || "Not listed"}</dd>
                  </div>
                  <div>
                    <dt>Leader</dt>
                    <dd>{program.programLeader || "Not listed"}</dd>
                  </div>
                </dl>

                <div className="program-actions">
                  <button
                    className="primary-action"
                    disabled={isEnrolled || !isActive || isBusy}
                    type="button"
                    onClick={() => handleEnroll(program)}
                  >
                    {isBusy ? "Enrolling..." : isEnrolled ? "Enrolled" : "Enroll"}
                  </button>
                  <small>
                    {isEnrolled
                      ? "This program is now part of Home."
                      : isActive
                        ? "Enrollment uses your verified Firebase account."
                        : "Archived programs are not open for enrollment."}
                  </small>
                </div>
              </article>
            );
          })}
        </section>
      )}
    </div>
  );
}

function ProgramsState({ title, message }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
    </section>
  );
}

function MessagePanel({ tone, message }) {
  return (
    <section className={`message-panel ${tone}`}>
      <p>{message}</p>
    </section>
  );
}

function getEnrolledProgramIds(programs) {
  const ids = new Set();
  if (!Array.isArray(programs)) {
    return ids;
  }

  programs.forEach((program) => {
    if (program?.programId) {
      ids.add(program.programId);
    }
  });

  return ids;
}

function getProgramName(program) {
  return program?.programName || program?.title || "Program";
}
