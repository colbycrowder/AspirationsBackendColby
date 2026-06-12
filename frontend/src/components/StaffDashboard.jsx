import { useEffect, useState } from "react";
import { fetchStaffMetrics, fetchStaffOperationsReporting } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getStaffPageError, StaffState, SummaryGrid } from "./staffUi.jsx";

export function StaffDashboard({ metrics: providedMetrics, navigate }) {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState(providedMetrics || null);
  const [operations, setOperations] = useState(null);
  const [loading, setLoading] = useState(!providedMetrics);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadDashboard() {
      setLoading(true);
      setError("");

      try {
        const [metricsData, operationsData] = await Promise.all([
          providedMetrics ? Promise.resolve(providedMetrics) : fetchStaffMetrics(user),
          fetchStaffOperationsReporting(user),
        ]);

        if (isActive) {
          setMetrics(metricsData);
          setOperations(operationsData);
        }
      } catch (nextError) {
        if (isActive) {
          setError(getStaffPageError(nextError));
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
  }, [providedMetrics, user]);

  if (loading) {
    return <StaffState title="Loading staff dashboard" message="Retrieving pilot operations summary." />;
  }

  if (error) {
    return <StaffState title="Staff dashboard unavailable" message={error} />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Staff Dashboard</h2>
        <p>Operational snapshot for pilot staff using protected backend reporting endpoints.</p>
      </section>

      <SummaryGrid
        items={[
          { label: "Total Youth", value: metrics?.totalYouthUsers ?? 0 },
          { label: "Active Youth", value: metrics?.activeYouthUsers ?? 0 },
          { label: "Programs", value: metrics?.activePrograms ?? 0 },
          { label: "Credentials Awarded", value: metrics?.earnedCredentials ?? 0 },
          { label: "Service Hours Submitted", value: metrics?.serviceHourRecords ?? 0 },
          { label: "Attendance Records", value: metrics?.attendanceRecords ?? 0 },
          { label: "Staff Operations Last 30 Days", value: operations?.operationsLast30Days ?? 0 },
        ]}
      />

      <section className="dashboard-section">
        <div className="section-header">
          <div>
            <h3>Staff Work Areas</h3>
            <p>Use the staff modules to manage pilot records without opening Firestore directly.</p>
          </div>
        </div>
        <div className="staff-action-grid">
          {[
            ["/staff/users", "Users"],
            ["/staff/program-management", "Programs"],
            ["/staff/credential-management", "Credentials"],
            ["/staff/attendance-management", "Attendance"],
            ["/staff/service-hour-management", "Service Hours"],
            ["/staff/operations-reporting", "Operations Reporting"],
          ].map(([path, label]) => (
            <button className="text-action" key={path} type="button" onClick={() => navigate(path)}>
              {label}
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}
