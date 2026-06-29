import { useEffect, useState } from "react";
import { fetchYouthDashboard, trackPlatformEvent } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getUniqueEarnedCredentials } from "../utils/credentialDeduplication.js";

export function YouthDashboard({ navigate }) {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadDashboard() {
      setLoading(true);
      setError("");

      try {
        const data = await fetchYouthDashboard(user);
        if (isActive) {
          setDashboard(data);
          trackPlatformEvent(user, "DASHBOARD_VIEW").catch(() => {});
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

    loadDashboard();

    return () => {
      isActive = false;
    };
  }, [user]);

  if (loading) {
    return <HomeState title="Loading Home" message="Retrieving your ASPN journey." />;
  }

  if (error) {
    return (
      <HomeState
        title="Home unavailable"
        message={error}
        note="If this is a new Firebase account, complete your ASPN profile before returning Home."
        actionLabel="Complete Profile"
        onAction={() => navigate("/profile")}
      />
    );
  }

  const profile = dashboard?.profileSummary || {};
  const earnedCredentials = getUniqueEarnedCredentials(dashboard?.earnedCredentials);
  const availableCredentials = asArray(dashboard?.availableCredentials);
  const programs = asArray(dashboard?.programs);
  const serviceHourRecords = asArray(dashboard?.serviceHourRecords);
  const movementItems = asArray(dashboard?.rwdLearningCenter);
  const opportunities = asArray(dashboard?.opportunities);
  const profileCompletion = getProfileCompletion(profile);
  const serviceHours = getApprovedServiceHours(serviceHourRecords);
  const movementProgress = getMovementProgress(movementItems);
  const actions = getNextActions({
    availableCredentials,
    earnedCredentials,
    movementItems,
    navigate,
    profileCompletion,
    programs,
    serviceHourRecords,
    unreadNotifications: Number(dashboard?.unreadNotificationCount || 0),
  });

  return (
    <div className="youth-home">
      <WelcomeSection profile={profile} profileCompletion={profileCompletion} navigate={navigate} />

      <section className="home-progress-section" aria-labelledby="home-progress-title">
        <div className="home-section-heading">
          <div>
            <span className="home-kicker">Your progress</span>
            <h2 id="home-progress-title">Keep building your future</h2>
          </div>
        </div>

        <div className="home-progress-grid">
          <ProgressCard
            label="Credentials Earned"
            value={earnedCredentials.length}
            detail={earnedCredentials.length ? "View what you have accomplished." : "Complete your first activity to begin earning credentials."}
            actionLabel="View credentials"
            onAction={() => navigate("/credentials")}
          />
          <ProgressCard
            label="Programs Active"
            value={programs.length}
            detail={programs.length ? "Continue participating in your active programs." : "Join an active program to begin participating."}
            actionLabel="View programs"
            onAction={() => navigate("/programs")}
          />
          <ProgressCard
            label="Service Hours"
            value={formatHours(serviceHours)}
            detail={serviceHours ? "Approved service hours." : "No approved service hours yet."}
            actionLabel="View service hours"
            onAction={() => navigate("/service-hours")}
          />
          <ProgressCard
            label="Global Civic Movements"
            value={movementProgress.completed}
            detail={movementItems.length
              ? movementProgress.completed
                ? `${movementProgress.remaining} activities remaining.`
                : "Complete your first movement activity to begin."
              : "No movement activities are available yet."}
            actionLabel="Open Learning Center"
            onAction={() => navigate("/rwd-learning-center")}
          />
        </div>
      </section>

      <section className="home-section" aria-labelledby="continue-journey-title">
        <div className="home-section-heading">
          <div>
            <span className="home-kicker">Next steps</span>
            <h2 id="continue-journey-title">Continue Your Journey</h2>
          </div>
          <span className="home-section-count">{actions.length} actions</span>
        </div>

        {actions.length ? (
          <div className="home-action-list">
            {actions.map((action) => (
              <button className="home-action" key={`${action.path}-${action.label}`} type="button" onClick={action.onAction}>
                <span>
                  <strong>{action.label}</strong>
                  <small>{action.detail}</small>
                </span>
                <span className="home-action-arrow" aria-hidden="true">→</span>
              </button>
            ))}
          </div>
        ) : (
          <HomeEmptyState message="You are caught up for now. Check back for new activities and opportunities." />
        )}
      </section>

      <div className="home-support-grid">
        <section className="home-section" aria-labelledby="upcoming-activities-title">
          <div className="home-section-heading">
            <div>
              <span className="home-kicker">Plan ahead</span>
              <h2 id="upcoming-activities-title">Upcoming Activities</h2>
            </div>
          </div>
          <HomeEmptyState message="No upcoming activities have been assigned yet." />
        </section>

        <section className="home-section" aria-labelledby="opportunities-title">
          <div className="home-section-heading">
            <div>
              <span className="home-kicker">Explore</span>
              <h2 id="opportunities-title">Opportunities</h2>
            </div>
          </div>
          <OpportunitiesPreview opportunities={opportunities} />
        </section>
      </div>
    </div>
  );
}

function WelcomeSection({ profile, profileCompletion, navigate }) {
  const firstName = hasText(profile.firstName) ? profile.firstName.trim() : "";
  const name = [profile.firstName, profile.lastName].filter(hasText).join(" ") || "Youth Profile";
  const firstInitial = profile.firstInitial || name.charAt(0).toUpperCase() || "A";

  return (
    <section className="home-welcome">
      <div className="home-avatar" aria-hidden="true">
        {profile.profileImageUrl ? <img src={profile.profileImageUrl} alt="" /> : <span>{firstInitial}</span>}
      </div>
      <div className="home-welcome-copy">
        <span className="home-kicker">Welcome back{firstName ? `, ${firstName}` : ""}</span>
        <h1>Continue Building Your Future</h1>
        <p>Your profile brings your learning, participation, credentials, and service together in one place.</p>
        <div className="home-profile-details">
          <span>{profile.school || "School not added"}</span>
          <span>{profile.graduationYear || "Graduation year not added"}</span>
          <span>{profile.aspnParticipantId || "ASPN ID pending"}</span>
        </div>
      </div>
      {profileCompletion.percent < 100 ? (
        <button className="home-primary-action" type="button" onClick={() => navigate("/profile")}>
          Complete profile
        </button>
      ) : (
        <button className="home-secondary-action" type="button" onClick={() => navigate("/profile")}>
          View profile
        </button>
      )}
    </section>
  );
}

function ProgressCard({ actionLabel, detail, label, onAction, value }) {
  return (
    <article className="home-progress-card">
      <span>{label}</span>
      <strong>{value}</strong>
      <p>{detail}</p>
      {onAction ? (
        <button type="button" onClick={onAction}>{actionLabel}</button>
      ) : (
        <small>More pathway tools are coming later.</small>
      )}
    </article>
  );
}

function OpportunitiesPreview({ opportunities }) {
  if (!opportunities.length) {
    return <HomeEmptyState message="No opportunities are available yet." />;
  }

  return (
    <div className="home-opportunity-list">
      {opportunities.slice(0, 3).map((opportunity, index) => (
        <article key={opportunity.url || opportunity.title || index}>
          <strong>{opportunity.title || "Opportunity"}</strong>
          {opportunity.url ? (
            <a href={opportunity.url} target="_blank" rel="noreferrer">Explore opportunity (opens in new tab)</a>
          ) : (
            <span>Link unavailable</span>
          )}
        </article>
      ))}
    </div>
  );
}

function HomeEmptyState({ message }) {
  return <p className="home-empty-state">{message}</p>;
}

function getProfileCompletion(profile) {
  const checks = [profile.firstName, profile.lastName, profile.email, profile.school, profile.graduationYear];
  const completed = checks.filter(hasText).length;
  return { percent: Math.round((completed / checks.length) * 100) };
}

function getNextActions({
  availableCredentials,
  earnedCredentials,
  movementItems,
  navigate,
  profileCompletion,
  programs,
  serviceHourRecords,
  unreadNotifications,
}) {
  const actions = [];
  const movementProgress = getMovementProgress(movementItems);

  if (profileCompletion.percent < 100) {
    actions.push({ label: "Complete your profile", detail: "Add the information that helps ASPN support your journey.", path: "/profile" });
  }
  if (!programs.length) {
    actions.push({ label: "Join a program", detail: "Explore active ASPN programs and choose where to participate.", path: "/programs" });
  }
  if (movementProgress.remaining > 0) {
    actions.push({ label: "Continue Global Civic Movements", detail: `${movementProgress.remaining} activities are ready to explore.`, path: "/rwd-learning-center" });
  }
  if (availableCredentials.length) {
    actions.push({ label: "View available credentials", detail: `${availableCredentials.length} credentials connect to your active programs.`, path: "/credentials" });
  } else if (earnedCredentials.length) {
    actions.push({ label: "Review your credentials", detail: "See the credentials you have already earned.", path: "/credentials" });
  }
  if (!serviceHourRecords.length) {
    actions.push({ label: "Submit service hours", detail: "Use the service-hour request process when you complete service.", path: "/service-hours" });
  } else {
    actions.push({ label: "Review service hours", detail: "Check the status of your submitted service-hour records.", path: "/service-hours" });
  }
  if (unreadNotifications > 0) {
    actions.push({ label: "Read notifications", detail: `${unreadNotifications} unread ${unreadNotifications === 1 ? "notice" : "notices"} waiting for you.`, path: "/notifications" });
  }

  return actions.slice(0, 5).map((action) => ({ ...action, onAction: () => navigate(action.path) }));
}

function getApprovedServiceHours(records) {
  return records.reduce((total, record) => {
    const status = String(record.verificationStatus || "").toLowerCase();
    return status === "verified" ? total + Number(record.hours || 0) : total;
  }, 0);
}

function getMovementProgress(items) {
  const completed = items.filter((item) => item?.progress?.completionStatus === "completed" || item?.completionStatus === "completed").length;
  return { completed, remaining: Math.max(items.length - completed, 0) };
}

function hasText(value) {
  return typeof value === "string" && value.trim().length > 0;
}

function formatHours(value) {
  const hours = Number(value || 0);
  return Number.isInteger(hours) ? String(hours) : hours.toFixed(1);
}

function HomeState({ actionLabel, message, note, onAction, title }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {note ? <p>{note}</p> : null}
      {actionLabel && onAction ? (
        <button className="primary-action" type="button" onClick={onAction}>
          {actionLabel}
        </button>
      ) : null}
    </section>
  );
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}
