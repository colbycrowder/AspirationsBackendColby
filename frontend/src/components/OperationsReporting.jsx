import { useEffect, useState } from "react";
import { fetchStaffOperationsReporting } from "../api.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { getStaffPageError, StaffState, SummaryGrid } from "./staffUi.jsx";

export function OperationsReporting() {
  const { user } = useAuth();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function loadOperations() {
      setLoading(true);
      setError("");

      try {
        const data = await fetchStaffOperationsReporting(user);
        if (isActive) {
          setReport(data);
        }
      } catch (nextError) {
        if (isActive) {
          setError(getStaffPageError(nextError));
          setReport(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    loadOperations();

    return () => {
      isActive = false;
    };
  }, [user]);

  if (loading) {
    return <StaffState title="Loading operations" message="Retrieving staff operation reporting." />;
  }

  if (error) {
    return <StaffState title="Operations reporting unavailable" message={error} />;
  }

  return (
    <div className="staff-stack">
      <section className="page-intro">
        <span className="eyebrow">Staff/Admin</span>
        <h2>Operations Reporting</h2>
        <p>Read-only reporting from the staff operation event log.</p>
      </section>

      <SummaryGrid
        items={[
          { label: "Total Operations", value: report?.totalOperations ?? 0 },
          { label: "Last 30 Days", value: report?.operationsLast30Days ?? 0 },
          { label: "Last 60 Days", value: report?.operationsLast60Days ?? 0 },
          { label: "Last 90 Days", value: report?.operationsLast90Days ?? 0 },
        ]}
      />

      <section className="staff-management-grid">
        <OperationGroup title="Operations By Type" values={report?.operationsByType} />
        <OperationGroup title="Operations By Staff User" values={report?.operationsByStaffUser} />
        <OperationGroup title="Operations By Target Type" values={report?.operationsByTargetType} />
      </section>
    </div>
  );
}

function OperationGroup({ title, values = {} }) {
  const entries = Object.entries(values);

  return (
    <section className="dashboard-section">
      <h3>{title}</h3>
      {entries.length === 0 ? (
        <p>No operations have been recorded for this group yet.</p>
      ) : (
        <div className="staff-mini-table">
          {entries.map(([label, count]) => (
            <div key={label}>
              <span>{label}</span>
              <strong>{count}</strong>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
