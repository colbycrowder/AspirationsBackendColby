import { useEffect, useMemo, useState } from "react";
import { fetchYouthDashboard } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { CredentialIcon } from "./CredentialIcon.jsx";
import {
  aspnEcosystemRegistry,
  findAspnRegistryCredentialMatch,
  getAspnRegistryCredentialById,
} from "../data/aspnEcosystemRegistry.js";
import { getUniqueEarnedCredentials } from "../utils/credentialDeduplication.js";

const SERVICE_MILESTONES = [10, 25, 50, 100];

export function MyJourneyPage({ navigate }) {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadJourney() {
      setLoading(true);
      setError("");

      try {
        const data = await fetchYouthDashboard(user);
        if (isActive) {
          setDashboard(data);
        }
      } catch (nextError) {
        if (isActive) {
          setError(nextError.message);
          setDashboard(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadJourney();
    return () => {
      isActive = false;
    };
  }, [user]);

  const summary = useMemo(() => getJourneySummary(dashboard), [dashboard]);
  const timeline = useMemo(() => buildJourneyTimeline(dashboard), [dashboard]);
  const attendanceLog = useMemo(() => buildAttendanceLog(dashboard), [dashboard]);
  const credentialConnections = useMemo(
    () => buildCredentialConnections(getUniqueEarnedCredentials(dashboard?.earnedCredentials)),
    [dashboard]
  );
  const startedPathwayIds = useMemo(
    () => new Set(credentialConnections.flatMap((connection) => connection.pathways.map((pathway) => pathway.id))),
    [credentialConnections]
  );

  if (loading) {
    return <JourneyState title="Loading My Journey" message="Gathering your accomplishments and participation milestones." />;
  }

  if (error) {
    return <JourneyState title="My Journey unavailable" message={error} />;
  }

  return (
    <div className="journey-page">
      <section className="journey-hero">
        <span className="journey-kicker">My Journey</span>
        <h1>See How You Are Growing</h1>
        <p>Your programs, learning, credentials, and service come together here as a record of what you have accomplished.</p>
      </section>

      <section className="journey-section" aria-labelledby="journey-summary-title">
        <div className="journey-section-heading">
          <div>
            <span className="journey-kicker">Accomplishments</span>
            <h2 id="journey-summary-title">Journey Summary</h2>
          </div>
        </div>

        <div className="journey-summary-grid">
          <JourneySummaryCard
            label="Credentials Earned"
            value={summary.credentials}
            action="View credentials"
            onAction={() => navigate("/credentials")}
          />
          <JourneySummaryCard
            label="Programs Active"
            value={summary.programs}
            action="View programs"
            onAction={() => navigate("/programs")}
          />
          <JourneySummaryCard
            label="Service Hours"
            value={formatNumber(summary.serviceHours)}
            action="View service hours"
            onAction={() => navigate("/service-hours")}
          />
          <JourneySummaryCard
            label="Global Civic Movements"
            value={summary.movements}
            action="Continue learning"
            onAction={() => navigate("/rwd-learning-center")}
          />
        </div>
      </section>

      <section className="journey-section" aria-labelledby="credential-meaning-title">
        <div className="journey-section-heading">
          <div>
            <span className="journey-kicker">Credential meaning</span>
            <h2 id="credential-meaning-title">What Your Credentials Connect To</h2>
          </div>
        </div>

        {credentialConnections.length ? (
          <div className="journey-credential-grid">
            {credentialConnections.map((connection) => (
              <CredentialMeaningCard connection={connection} key={connection.id} navigate={navigate} />
            ))}
          </div>
        ) : (
          <div className="journey-intelligence-empty">
            <h3>Your pathway journey is just beginning.</h3>
            <p>As you participate in programs and earn credentials, ASPN will show how your experiences connect to civic pathways and future opportunities.</p>
          </div>
        )}
      </section>

      <section className="journey-section" aria-labelledby="journey-timeline-title">
        <div className="journey-section-heading">
          <div>
            <span className="journey-kicker">Your story so far</span>
            <h2 id="journey-timeline-title">Journey Timeline</h2>
          </div>
          {timeline.length ? <span className="journey-count">{timeline.length} milestones</span> : null}
        </div>

        {timeline.length ? (
          <ol className="journey-timeline">
            {timeline.map((item) => (
              <li key={item.id}>
                <span className="journey-timeline-marker" aria-hidden="true" />
                <article className="journey-timeline-card">
                  <div className="journey-timeline-meta">
                    <span className="journey-category">{item.category}</span>
                    <time dateTime={item.isoDate || undefined}>{formatJourneyDate(item.date)}</time>
                  </div>
                  <h3>{item.title}</h3>
                  {item.description ? <p>{item.description}</p> : null}
                </article>
              </li>
            ))}
          </ol>
        ) : (
          <div className="journey-empty-state">
            <h3>Your journey is just beginning.</h3>
            <p>Join a program, complete a learning experience, or earn your first credential to start building your journey.</p>
            <button type="button" onClick={() => navigate("/programs")}>Explore programs</button>
          </div>
        )}
      </section>

      <section className="journey-section" aria-labelledby="attendance-log-title">
        <div className="journey-section-heading">
          <div>
            <span className="journey-kicker">Participation record</span>
            <h2 id="attendance-log-title">Attendance Log</h2>
          </div>
          {attendanceLog.length ? <span className="journey-count">{attendanceLog.length} records</span> : null}
        </div>

        {attendanceLog.length ? (
          <div className="journey-record-table" role="table" aria-label="Attendance log">
            <div className="journey-record-row header" role="row">
              <span role="columnheader">Date</span>
              <span role="columnheader">Program</span>
              <span role="columnheader">Event/session</span>
              <span role="columnheader">Status</span>
            </div>
            {attendanceLog.map((record) => (
              <div className="journey-record-row" key={record.id} role="row">
                <span role="cell">{record.dateLabel}</span>
                <span role="cell">{record.programName}</span>
                <span role="cell">{record.eventName}</span>
                <span role="cell">
                  <span className={`journey-status-pill ${record.status}`}>{record.statusLabel}</span>
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="journey-intelligence-empty">
            <h3>No attendance records yet.</h3>
            <p>When staff record your participation, present and excused attendance will appear here.</p>
          </div>
        )}
      </section>

      <section className="journey-section" aria-labelledby="explore-pathways-title">
        <div className="journey-section-heading">
          <div>
            <span className="journey-kicker">Make the connection</span>
            <h2 id="explore-pathways-title">Explore Your Pathways</h2>
          </div>
        </div>
        {!credentialConnections.length ? (
          <p className="journey-pathway-intro">Explore these pathways by joining programs and earning related credentials.</p>
        ) : null}
        <div className="journey-pathway-grid" aria-label="ASPN civic workforce pathways">
          {aspnEcosystemRegistry.pathways.map((pathway) => (
            <PathwayPreviewCard
              isStarted={startedPathwayIds.has(pathway.id)}
              key={pathway.id}
              pathway={pathway}
            />
          ))}
        </div>
      </section>
    </div>
  );
}

function CredentialMeaningCard({ connection, navigate }) {
  const credentialDetailId = connection.registryCredential?.id || connection.earnedCredential.credentialID;

  return (
    <article className="journey-credential-card">
      <div className="journey-credential-header">
        <CredentialIcon
          credential={connection.registryCredential}
          name={connection.earnedCredential.credentialName}
          size="journey"
        />
        <div>
          <span className="journey-kicker">Earned credential</span>
          <h3>{connection.earnedCredential.credentialName || "ASPN Credential"}</h3>
        </div>
        {connection.pathways.length ? <span className="journey-started-badge">Connected</span> : null}
      </div>

      {connection.pathways.length ? (
        <>
          <div className="journey-pathway-contexts">
            {connection.pathways.map((pathway) => (
              <div key={pathway.id}>
                <strong>{pathway.name}</strong>
                <p>{pathway.description}</p>
              </div>
            ))}
          </div>

          <ConnectionList label="Related programs" items={connection.programs.map((program) => program.name)} />
          <ConnectionList label="Advanced credentials to explore" items={connection.advancedCredentials.map((credential) => credential.name)} />
          <ConnectionList label="Example civic role families" items={connection.roleFamilies} />
          {credentialDetailId ? (
            <button
              className="credential-detail-link"
              type="button"
              onClick={() => navigate(`/credentials/${encodeURIComponent(credentialDetailId)}`)}
            >
              View credential details
            </button>
          ) : null}
        </>
      ) : (
        <p className="journey-unmapped-message">A registry connection is not available for this credential yet. Your earned credential remains part of your Journey.</p>
      )}
    </article>
  );
}

function PathwayPreviewCard({ isStarted, pathway }) {
  const coreCredential = getAspnRegistryCredentialById(pathway.relatedCoreCredentialId);
  return (
    <article className={isStarted ? "journey-pathway-card started" : "journey-pathway-card"}>
      <div className="journey-pathway-card-header">
        <h3>{pathway.name}</h3>
        <span className={isStarted ? "journey-started-badge" : "journey-explore-badge"}>
          {isStarted ? "Started" : "Explore"}
        </span>
      </div>
      <p>{pathway.description}</p>
      <div className="journey-pathway-credential">
        <span>Related credential</span>
        <strong>{coreCredential?.name || "Credential details coming soon"}</strong>
      </div>
      <ConnectionList label="Example civic role families" items={pathway.exampleCivicRoleFamilies} />
      {!isStarted ? <small>Explore this pathway by joining programs and earning related credentials.</small> : null}
    </article>
  );
}

function ConnectionList({ items, label }) {
  const uniqueItems = [...new Set(items.filter(Boolean))];
  if (!uniqueItems.length) return null;

  return (
    <div className="journey-connection-list">
      <span>{label}</span>
      <ul>
        {uniqueItems.map((item) => <li key={item}>{item}</li>)}
      </ul>
    </div>
  );
}

function JourneySummaryCard({ action, label, onAction, value }) {
  return (
    <article className="journey-summary-card">
      <span>{label}</span>
      <strong>{value}</strong>
      <button type="button" onClick={onAction}>{action}</button>
    </article>
  );
}

function JourneyState({ title, message }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
    </section>
  );
}

function buildCredentialConnections(earnedCredentials) {
  return asArray(earnedCredentials).map((earnedCredential, index) => {
    const registryCredential = findAspnRegistryCredentialMatch(earnedCredential);
    if (!registryCredential) {
      return {
        id: earnedCredential.earnedCredentialID || earnedCredential.credentialID || `credential-${index}`,
        earnedCredential,
        registryCredential: null,
        pathways: [],
        programs: [],
        advancedCredentials: [],
        roleFamilies: [],
      };
    }

    const pathways = registryCredential.pathwayIds
      .map((pathwayId) => aspnEcosystemRegistry.pathways.find((pathway) => pathway.id === pathwayId))
      .filter(Boolean);
    const advancedCredentialIds = new Set(pathways.flatMap((pathway) => pathway.relatedAdvancedCredentialIds));
    advancedCredentialIds.delete(registryCredential.id);

    return {
      id: earnedCredential.earnedCredentialID || earnedCredential.credentialID || `credential-${index}`,
      earnedCredential,
      registryCredential,
      pathways,
      programs: aspnEcosystemRegistry.programs.filter((program) => program.credentialIds.includes(registryCredential.id)),
      advancedCredentials: [...advancedCredentialIds].map(getAspnRegistryCredentialById).filter(Boolean),
      roleFamilies: [...new Set(pathways.flatMap((pathway) => pathway.exampleCivicRoleFamilies))].slice(0, 6),
    };
  });
}

function getJourneySummary(dashboard) {
  const serviceHours = getVerifiedServiceRecords(dashboard).reduce((total, record) => total + Number(record.hours || 0), 0);
  return {
    credentials: getUniqueEarnedCredentials(dashboard?.earnedCredentials).length,
    programs: asArray(dashboard?.programs).length,
    serviceHours,
    movements: getCompletedMovementItems(dashboard).length,
  };
}

function buildJourneyTimeline(dashboard) {
  if (!dashboard) {
    return [];
  }

  const events = [
    ...buildProgramEvents(dashboard.programs),
    ...buildCredentialEvents(getUniqueEarnedCredentials(dashboard.earnedCredentials)),
    ...buildLearningEvents(dashboard.rwdLearningCenter),
    ...buildServiceMilestones(dashboard.serviceHourRecords),
  ];

  return events.sort((left, right) => {
    if (left.date && right.date) {
      return right.date.getTime() - left.date.getTime();
    }
    if (left.date) return -1;
    if (right.date) return 1;
    return left.title.localeCompare(right.title);
  });
}

function buildProgramEvents(programs) {
  return asArray(programs).map((program, index) => ({
    id: `program-${program.programId || index}`,
    category: "Program",
    title: `Joined ${program.programName || "an ASPN program"}`,
    description: program.description || "Active participation in an ASPN program.",
    date: null,
    isoDate: "",
  }));
}

function buildCredentialEvents(credentials) {
  return asArray(credentials).map((credential, index) => {
    const date = parseDate(credential.awardedAt || credential.earnedAt);
    return {
      id: `credential-${credential.earnedCredentialID || credential.credentialID || index}`,
      category: "Credential",
      title: `Earned ${credential.credentialName || "an ASPN credential"}`,
      description: credential.description || "Recognition of learning and participation.",
      date,
      isoDate: toIsoDate(date),
    };
  });
}

function buildLearningEvents(items) {
  return getCompletedMovementItems({ rwdLearningCenter: items }).map((item, index) => {
    const date = parseDate(item.progress?.completedAt || item.completedAt);
    const experienceName = item.countryName
      ? `${item.countryName} Learning Experience`
      : item.title || "a Global Civic Movements experience";
    return {
      id: `learning-${item.rwdActivityId || item.progress?.progressId || index}`,
      category: "Learning",
      title: `Completed ${experienceName}`,
      description: item.description || "Completed a Global Civic Movements learning experience.",
      date,
      isoDate: toIsoDate(date),
    };
  });
}

function buildServiceMilestones(records) {
  const verified = getVerifiedServiceRecords({ serviceHourRecords: records })
    .map((record) => ({ record, date: parseDate(record.serviceDate || record.reviewedAt || record.createdAt) }))
    .sort(compareDatedRecords);
  const milestones = [];
  let total = 0;
  let thresholdIndex = 0;

  for (const entry of verified) {
    total += Number(entry.record.hours || 0);
    while (thresholdIndex < SERVICE_MILESTONES.length && total >= SERVICE_MILESTONES[thresholdIndex]) {
      const threshold = SERVICE_MILESTONES[thresholdIndex];
      milestones.push({
        id: `service-${threshold}`,
        category: "Service",
        title: `${threshold} Service Hours Completed`,
        description: "A milestone in documented community service.",
        date: entry.date,
        isoDate: toIsoDate(entry.date),
      });
      thresholdIndex += 1;
    }
  }
  return milestones;
}

function buildAttendanceLog(dashboard) {
  return asArray(dashboard?.attendanceRecords)
    .filter((record) => isYouthVisibleAttendanceStatus(record.attendanceStatus))
    .map((record, index) => {
      const status = normalizeAttendanceStatus(record.attendanceStatus);
      const date = parseDate(record.eventDate || record.createdAt);

      return {
        id: `attendance-log-${record.attendanceRecordID || record.eventDate || index}`,
        date,
        dateLabel: formatFullDate(date),
        programName: getProgramNameForAttendance(record.programID, dashboard?.programs) || "Program not listed",
        eventName: record.eventName || "Attendance session",
        status,
        statusLabel: formatAttendanceStatus(status),
      };
    })
    .sort((left, right) => {
      if (left.date && right.date) return right.date.getTime() - left.date.getTime();
      if (left.date) return -1;
      if (right.date) return 1;
      return left.eventName.localeCompare(right.eventName);
    });
}

function getVerifiedServiceRecords(dashboard) {
  return asArray(dashboard?.serviceHourRecords).filter((record) => {
    const status = String(record.verificationStatus || "").toLowerCase();
    return status === "verified" || status === "approved";
  });
}

function getCompletedMovementItems(dashboard) {
  return asArray(dashboard?.rwdLearningCenter).filter((item) => {
    const status = item.progress?.completionStatus || item.completionStatus;
    return status === "completed";
  });
}

function compareDatedRecords(left, right) {
  if (left.date && right.date) return left.date.getTime() - right.date.getTime();
  if (left.date) return -1;
  if (right.date) return 1;
  return 0;
}

function parseDate(value) {
  if (!value) return null;
  if (typeof value === "object" && value.seconds) {
    const firestoreDate = new Date(value.seconds * 1000);
    return Number.isNaN(firestoreDate.getTime()) ? null : firestoreDate;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function toIsoDate(date) {
  return date ? date.toISOString() : "";
}

function formatJourneyDate(date) {
  if (!date) return "Date not available";
  return date.toLocaleDateString(undefined, { month: "long", year: "numeric" });
}

function formatFullDate(date) {
  if (!date) return "Date not available";
  return date.toLocaleDateString(undefined, { month: "long", day: "numeric", year: "numeric" });
}

function formatNumber(value) {
  const number = Number(value || 0);
  return Number.isInteger(number) ? String(number) : number.toFixed(1);
}

function isYouthVisibleAttendanceStatus(status) {
  const normalizedStatus = normalizeAttendanceStatus(status);
  return normalizedStatus === "present" || normalizedStatus === "excused";
}

function normalizeAttendanceStatus(status) {
  return String(status || "pending").toLowerCase();
}

function formatAttendanceStatus(status) {
  if (status === "present") return "Present";
  if (status === "excused") return "Excused";
  if (status === "pending") return "Pending";
  if (status === "absent") return "Absent";
  return "Attendance";
}

function getProgramNameForAttendance(programId, programs) {
  if (!programId) return "";
  const program = asArray(programs).find((item) => item.programId === programId || item.programID === programId);
  return program?.programName || "";
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}
