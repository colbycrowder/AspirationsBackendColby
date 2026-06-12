import { ApiAccessError } from "../api.js";

export function StaffState({ title, message }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
    </section>
  );
}

export function StaffMessage({ type = "success", children }) {
  if (!children) {
    return null;
  }

  return (
    <section className={`message-panel ${type}`}>
      <p>{children}</p>
    </section>
  );
}

export function SummaryGrid({ items }) {
  return (
    <section className="staff-metrics-grid" aria-label="Summary metrics">
      {items.map((item) => (
        <article className="summary-tile" key={item.label}>
          <strong>{item.value ?? 0}</strong>
          <span>{item.label}</span>
        </article>
      ))}
    </section>
  );
}

export function getStaffPageError(error) {
  if (error instanceof ApiAccessError && error.status === 401) {
    return "Sign in with a valid Firebase account before opening staff/admin tools.";
  }

  if (error instanceof ApiAccessError && error.status === 403) {
    return "Access denied. This account does not have staff or admin access.";
  }

  return error.message || "Staff/admin data is unavailable. Confirm the backend is running and try again.";
}

export function formatDate(value) {
  if (!value) {
    return "Not set";
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "Not set" : date.toLocaleDateString();
}

export function getRecordId(record, keys) {
  for (const key of keys) {
    if (record?.[key]) {
      return record[key];
    }
  }
  return "";
}
