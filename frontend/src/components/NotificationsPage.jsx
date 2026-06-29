import { useEffect, useMemo, useState } from "react";
import { fetchNotifications, fetchYouthDashboard, markNotificationRead } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

export function NotificationsPage() {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [dashboardUnreadCount, setDashboardUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [updatingNotificationId, setUpdatingNotificationId] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadNotifications() {
      setLoading(true);
      setError("");
      setMessage("");

      try {
        const [notificationData, dashboardData] = await Promise.all([
          fetchNotifications(user),
          fetchYouthDashboard(user).catch(() => null),
        ]);

        if (isActive) {
          setNotifications(notificationData);
          setDashboardUnreadCount(dashboardData?.unreadNotificationCount ?? getUnreadCount(notificationData));
        }
      } catch (nextError) {
        if (isActive) {
          setError(nextError.message);
          setNotifications([]);
          setDashboardUnreadCount(0);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadNotifications();

    return () => {
      isActive = false;
    };
  }, [user]);

  const sortedNotifications = useMemo(
    () => [...notifications].sort(compareCreatedAt),
    [notifications]
  );
  const unreadCount = getUnreadCount(sortedNotifications);

  async function handleMarkRead(notification) {
    setError("");
    setMessage("");
    setUpdatingNotificationId(notification.notificationId);

    try {
      await markNotificationRead(user, notification.notificationId);
      const nextNotifications = await fetchNotifications(user);
      setNotifications(nextNotifications);
      setDashboardUnreadCount(getUnreadCount(nextNotifications));
      setMessage("Notification marked as read.");
    } catch (nextError) {
      setError(nextError.message);
    } finally {
      setUpdatingNotificationId("");
    }
  }

  if (loading) {
    return <NotificationsState title="Loading notifications" message="Retrieving your ASPN notifications." />;
  }

  if (error && !notifications.length) {
    return (
      <NotificationsState
        title="Notifications unavailable"
        message={error}
        note="If this is a new Firebase account, complete your ASPN profile before opening Notifications."
      />
    );
  }

  return (
    <div className="notifications-stack">
      <section className="page-intro">
        <span className="eyebrow">Youth Notifications</span>
        <h2>Notifications</h2>
        <p>Review platform updates tied to your ASPN account, including earned credential notices.</p>
      </section>

      <section className="credential-summary-grid" aria-label="Notification summary">
        <SummaryTile label="Unread" value={unreadCount} />
        <SummaryTile label="Dashboard Unread" value={dashboardUnreadCount} />
        <SummaryTile label="Total" value={sortedNotifications.length} />
      </section>

      {message ? <MessagePanel tone="success" message={message} /> : null}
      {error ? <MessagePanel tone="error" message={error} /> : null}

      <section className="dashboard-section">
        <h3>Notification Inbox</h3>
        {!sortedNotifications.length ? (
          <p className="empty-text">No notifications yet.</p>
        ) : (
          <div className="notification-list">
            {sortedNotifications.map((notification) => {
              const isUpdating = updatingNotificationId === notification.notificationId;
              return (
                <article
                  className={notification.read ? "notification-card read" : "notification-card unread"}
                  key={notification.notificationId}
                >
                  <div className="notification-card-header">
                    <div>
                      <strong>{notification.title || "Notification"}</strong>
                      <span>{formatDate(notification.createdAt)}</span>
                    </div>
                    <span className={notification.read ? "status-tag muted" : "status-tag"}>
                      {notification.read ? "Read" : "Unread"}
                    </span>
                  </div>

                  <p>{notification.message || "No notification message is available."}</p>

                  <dl className="program-meta">
                    <div>
                      <dt>Type</dt>
                      <dd>{formatType(notification.notificationType)}</dd>
                    </div>
                    <div>
                      <dt>Related</dt>
                      <dd>{notification.relatedCredentialId || notification.relatedEarnedCredentialId || "Not listed"}</dd>
                    </div>
                  </dl>

                  {!notification.read ? (
                    <button
                      className="text-action"
                      disabled={isUpdating}
                      type="button"
                      onClick={() => handleMarkRead(notification)}
                    >
                      {isUpdating ? "Updating..." : "Mark as Read"}
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
}

function SummaryTile({ label, value }) {
  return (
    <article className="summary-tile">
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  );
}

function MessagePanel({ tone, message }) {
  return (
    <section className={`message-panel ${tone}`}>
      <p>{message}</p>
    </section>
  );
}

function NotificationsState({ title, message, note }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {note ? <p>{note}</p> : null}
    </section>
  );
}

function getUnreadCount(notifications) {
  return notifications.filter((notification) => !notification.read).length;
}

function compareCreatedAt(first, second) {
  return getTime(second.createdAt) - getTime(first.createdAt);
}

function getTime(value) {
  const date = new Date(value || 0);
  return Number.isNaN(date.getTime()) ? 0 : date.getTime();
}

function formatDate(value) {
  if (!value) {
    return "date unavailable";
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return date.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

function formatType(value) {
  return String(value || "notification").replaceAll("_", " ");
}
