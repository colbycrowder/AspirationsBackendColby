import { useEffect, useMemo, useState } from "react";
import { ApiAccessError, fetchStaffMetrics } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";

const metricLabels = [
  { key: "totalYouthUsers", label: "Total Youth Users" },
  { key: "activeYouthUsers", label: "Active Youth Users" },
  { key: "activePrograms", label: "Active Programs" },
  { key: "enrollments", label: "Enrollments" },
  { key: "earnedCredentials", label: "Earned Credentials" },
  { key: "attendanceRecords", label: "Attendance Records" },
  { key: "serviceHourRecords", label: "Service-Hour Records" },
  { key: "completedRwdActivities", label: "Completed RWD Activities" },
  { key: "unreadNotifications", label: "Unread Notifications" },
];

export function StaffMetricsDashboard({ metrics: providedMetrics }) {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState(providedMetrics || null);
  const [loading, setLoading] = useState(!providedMetrics);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadMetrics() {
      if (providedMetrics) {
        setMetrics(providedMetrics);
        setLoading(false);
        return;
      }

      setLoading(true);
      setError("");

      try {
        const data = await fetchStaffMetrics(user);
        if (isActive) {
          setMetrics(data);
        }
      } catch (nextError) {
        if (isActive) {
          setError(getStaffErrorMessage(nextError));
          setMetrics(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadMetrics();

    return () => {
      isActive = false;
    };
  }, [providedMetrics, user]);

  const metricItems = useMemo(
    () => metricLabels.map((item) => ({ ...item, value: Number(metrics?.[item.key] ?? 0) })),
    [metrics]
  );
  const hasMetricData = metrics && metricLabels.some((item) => Object.prototype.hasOwnProperty.call(metrics, item.key));

  if (loading) {
    return <StaffState title="Loading staff dashboard" message="Retrieving platform metrics." />;
  }

  if (error) {
    return <StaffState title="Staff dashboard unavailable" message={error} />;
  }

  if (!hasMetricData) {
    return <StaffState title="No metrics available" message="The backend returned no platform metrics." />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Platform Metrics</h2>
        <p>Use Platform Metrics for simple current totals across users, programs, credentials, attendance, service hours, learning, and unread notifications.</p>
      </section>

      <section className="staff-metrics-grid" aria-label="Platform metrics">
        {metricItems.map((item) => (
          <article className="summary-tile" key={item.key}>
            <strong>{item.value}</strong>
            <span>{item.label}</span>
          </article>
        ))}
      </section>
    </div>
  );
}

function StaffState({ title, message }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
    </section>
  );
}

function getStaffErrorMessage(error) {
  if (error instanceof ApiAccessError && error.status === 401) {
    return "Sign in with a valid Firebase account before opening staff/admin tools.";
  }

  if (error instanceof ApiAccessError && error.status === 403) {
    return "Access denied. This account does not have staff or admin access.";
  }

  return error.message || "Staff/admin data is unavailable. Confirm the backend is running and try again.";
}
