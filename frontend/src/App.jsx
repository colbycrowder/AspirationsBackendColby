import { useEffect, useMemo, useState } from "react";
import { appConfig } from "./config.js";
import { useAuth } from "./auth/AuthContext.jsx";
import { CredentialsPage } from "./components/CredentialsPage.jsx";
import { ProgramsPage } from "./components/ProgramsPage.jsx";
import { ServiceHoursPage } from "./components/ServiceHoursPage.jsx";
import { YouthDashboard } from "./components/YouthDashboard.jsx";

const youthRoutes = [
  { path: "/login", label: "Login", title: "Login", public: true },
  { path: "/create-account", label: "Create Account", title: "Create Account", public: true },
  { path: "/dashboard", label: "Youth Dashboard", title: "Youth Dashboard", protected: true },
  { path: "/programs", label: "Programs", title: "Programs", protected: true },
  { path: "/credentials", label: "Credentials", title: "Credentials", protected: true },
  { path: "/rwd-learning-center", label: "RWD Learning Center", title: "RWD Learning Center", protected: true },
  { path: "/notifications", label: "Notifications", title: "Notifications", protected: true },
  { path: "/attendance", label: "Attendance", title: "Attendance", protected: true },
  { path: "/service-hours", label: "Service Hours", title: "Service Hours", protected: true },
];

const staffRoutes = [
  { path: "/staff", label: "Staff Dashboard", title: "Staff Dashboard", protected: true, staff: true },
  { path: "/staff/youth-management", label: "Youth Management", title: "Youth Management", protected: true, staff: true },
  { path: "/staff/program-management", label: "Program Management", title: "Program Management", protected: true, staff: true },
  { path: "/staff/credential-management", label: "Credential Management", title: "Credential Management", protected: true, staff: true },
  { path: "/staff/attendance-management", label: "Attendance Management", title: "Attendance Management", protected: true, staff: true },
  { path: "/staff/service-hour-management", label: "Service Hour Management", title: "Service Hour Management", protected: true, staff: true },
  { path: "/staff/metrics", label: "Metrics", title: "Metrics", protected: true, staff: true },
];

const routes = [
  { path: "/", label: "Home", title: "ASPN Platform", public: true },
  ...youthRoutes,
  ...staffRoutes,
];

const pageDetails = {
  "/": {
    eyebrow: "Frontend Scaffold",
    description: "Firebase Auth is now wired for login, account creation, logout, and protected route shells.",
    items: ["Backend API calls are not connected yet.", "Staff role checks are placeholders for a later checkpoint."],
  },
  "/dashboard": {
    eyebrow: "Youth Dashboard",
    description: "Future dashboard powered primarily by GET /api/me/dashboard.",
    items: ["Profile summary", "Programs", "Credentials", "Attendance", "Service hours", "RWD", "Notifications"],
  },
  "/programs": {
    eyebrow: "Programs",
    description: "Future active-program browsing and self-enrollment screen.",
    items: ["List active programs", "View program details", "Enroll using verified Firebase UID"],
  },
  "/credentials": {
    eyebrow: "Credentials",
    description: "Future credential wallet and available credential display.",
    items: ["Earned credentials", "Available credentials", "Requirement text", "Placeholder icon support"],
  },
  "/rwd-learning-center": {
    eyebrow: "RWD Learning Center",
    description: "Future tracking hub for the 16 externally hosted RWD activities.",
    items: ["External video links", "Progress status", "Quiz score submission", "Credential status"],
  },
  "/notifications": {
    eyebrow: "Notifications",
    description: "Future credential-earned notification inbox.",
    items: ["Unread count", "Notification list", "Mark as read"],
  },
  "/attendance": {
    eyebrow: "Attendance",
    description: "Future youth attendance record display.",
    items: ["Present", "Absent", "Excused", "Pending"],
  },
  "/service-hours": {
    eyebrow: "Service Hours",
    description: "Future service-hour record display and request link.",
    items: ["Verified records", "Pending records", "Service-hour request form URL"],
  },
  "/staff": {
    eyebrow: "Staff/Admin",
    description: "Future staff dashboard shell.",
    items: ["Protected staff routes require sign-in.", "Role-based staff/admin authorization will be added later."],
  },
  "/staff/youth-management": {
    eyebrow: "Staff/Admin",
    description: "Future youth profile review and limited staff-managed field updates.",
    items: ["List youth users", "Review profile", "Update staff-managed fields"],
  },
  "/staff/program-management": {
    eyebrow: "Staff/Admin",
    description: "Future program creation and update workflow.",
    items: ["Create programs", "Update programs", "Archive programs"],
  },
  "/staff/credential-management": {
    eyebrow: "Staff/Admin",
    description: "Future credential definition and manual award workflow.",
    items: ["Create credential definitions", "Award credentials", "No hardcoded credential catalog"],
  },
  "/staff/attendance-management": {
    eyebrow: "Staff/Admin",
    description: "Future attendance recording workflow.",
    items: ["Record attendance", "Trigger attendance-count auto-awards", "Review attendance records"],
  },
  "/staff/service-hour-management": {
    eyebrow: "Staff/Admin",
    description: "Future service-hour review workflow.",
    items: ["Create or review records", "Configure request URL", "No Google Form sync yet"],
  },
  "/staff/metrics": {
    eyebrow: "Staff/Admin",
    description: "Future aggregate metrics display.",
    items: ["Youth users", "Programs", "Enrollments", "Credentials", "Attendance", "Service hours", "RWD", "Notifications"],
  },
};

function getCurrentPath() {
  return window.location.pathname === "/" ? "/" : window.location.pathname.replace(/\/$/, "");
}

function App() {
  const auth = useAuth();
  const [currentPath, setCurrentPath] = useState(getCurrentPath());

  useEffect(() => {
    const onPopState = () => setCurrentPath(getCurrentPath());
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  const route = useMemo(
    () => routes.find((item) => item.path === currentPath) || routes[0],
    [currentPath]
  );
  const detail = pageDetails[route.path] || pageDetails["/"];

  function navigate(path) {
    window.history.pushState({}, "", path);
    setCurrentPath(path);
  }

  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Main navigation">
        <a className="brand" href="/" onClick={(event) => handleNavClick(event, "/", navigate)}>
          <span className="brand-mark">A</span>
          <span>
            <strong>ASPN</strong>
            <small>Platform MVP</small>
          </span>
        </a>

        <AuthStatus navigate={navigate} />

        <nav className="nav-section">
          <p>Youth</p>
          {youthRoutes.map((item) => (
            <NavLink key={item.path} item={item} currentPath={currentPath} navigate={navigate} />
          ))}
        </nav>

        <nav className="nav-section">
          <p>Staff/Admin</p>
          {staffRoutes.map((item) => (
            <NavLink key={item.path} item={item} currentPath={currentPath} navigate={navigate} />
          ))}
        </nav>
      </aside>

      <main className="main-panel">
        <header className="topbar">
          <div>
            <span className="eyebrow">{detail.eyebrow}</span>
            <h1>{route.title}</h1>
          </div>
          <div className="status-pill">{auth.isSignedIn ? "Signed In" : "Signed Out"}</div>
        </header>

        <section className="workspace">
          <RouteContent route={route} detail={detail} navigate={navigate} />
        </section>
      </main>
    </div>
  );
}

function RouteContent({ route, detail, navigate }) {
  const auth = useAuth();

  if (auth.loading) {
    return <StatePanel title="Checking sign-in status" message="Loading authentication state." />;
  }

  if (route.path === "/login") {
    return <AuthForm mode="login" navigate={navigate} />;
  }

  if (route.path === "/create-account") {
    return <AuthForm mode="create" navigate={navigate} />;
  }

  if (route.protected && !auth.isSignedIn) {
    return (
      <StatePanel
        title="Sign in required"
        message="This screen is protected. Sign in before opening youth or staff/admin pages."
      >
        <button className="primary-action" type="button" onClick={() => navigate("/login")}>
          Go to Login
        </button>
      </StatePanel>
    );
  }

  if (route.path === "/dashboard") {
    return <YouthDashboard />;
  }

  if (route.path === "/programs") {
    return <ProgramsPage />;
  }

  if (route.path === "/credentials") {
    return <CredentialsPage />;
  }

  if (route.path === "/service-hours") {
    return <ServiceHoursPage />;
  }

  return <PlaceholderPage route={route} detail={detail} />;
}

function AuthForm({ mode, navigate }) {
  const auth = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const isCreate = mode === "create";

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      if (isCreate) {
        await auth.createAccount(email, password);
      } else {
        await auth.login(email, password);
      }
      navigate("/dashboard");
    } catch (nextError) {
      setError(getAuthErrorMessage(nextError, isCreate));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="auth-card" aria-label={isCreate ? "Create account" : "Login"}>
      <span className="eyebrow">{isCreate ? "Youth Onboarding" : "Youth Access"}</span>
      <h2>{isCreate ? "Create Account" : "Login"}</h2>
      <p>
        {isCreate
          ? "Create a Firebase Authentication account for future ASPN platform access."
          : "Sign in with a Firebase Authentication account."}
      </p>

      <form className="auth-form" onSubmit={handleSubmit}>
        <label>
          Email
          <input
            autoComplete="email"
            name="email"
            onChange={(event) => setEmail(event.target.value)}
            required
            type="email"
            value={email}
          />
        </label>

        <label>
          Password
          <input
            autoComplete={isCreate ? "new-password" : "current-password"}
            minLength={6}
            name="password"
            onChange={(event) => setPassword(event.target.value)}
            required
            type="password"
            value={password}
          />
        </label>

        {error ? <p className="form-error">{error}</p> : null}

        <button className="primary-action" disabled={submitting} type="submit">
          {submitting ? "Working..." : isCreate ? "Create Account" : "Login"}
        </button>
      </form>

      <button
        className="text-action"
        type="button"
        onClick={() => navigate(isCreate ? "/login" : "/create-account")}
      >
        {isCreate ? "Already have an account? Login" : "Need an account? Create one"}
      </button>
    </section>
  );
}

function PlaceholderPage({ route, detail }) {
  return (
    <>
      {route.staff ? (
        <section className="notice-panel">
          <strong>Staff/admin role check pending.</strong>
          <span> This shell only verifies sign-in. Backend role validation will be connected later.</span>
        </section>
      ) : null}

      <div className="page-intro">
        <h2>{route.title}</h2>
        <p>{detail.description}</p>
      </div>

      <div className="placeholder-grid">
        {detail.items.map((item) => (
          <article className="placeholder-card" key={item}>
            <span className="card-dot" aria-hidden="true" />
            <p>{item}</p>
          </article>
        ))}
      </div>

      <section className="config-panel" aria-label="Environment configuration">
        <h3>Environment Pattern</h3>
        <dl>
          <div>
            <dt>API Base URL</dt>
            <dd>{appConfig.apiBaseUrl}</dd>
          </div>
          <div>
            <dt>Firebase Project ID</dt>
            <dd>{appConfig.firebase.projectId || "not configured"}</dd>
          </div>
        </dl>
      </section>
    </>
  );
}

function AuthStatus({ navigate }) {
  const auth = useAuth();

  if (auth.loading) {
    return <div className="auth-status">Checking sign-in status...</div>;
  }

  if (!auth.isSignedIn) {
    return (
      <div className="auth-status">
        <span>Signed out</span>
        <button type="button" onClick={() => navigate("/login")}>
          Login
        </button>
      </div>
    );
  }

  return (
    <div className="auth-status">
      <span>{auth.user.email || "Signed in"}</span>
      <button type="button" onClick={auth.logout}>
        Logout
      </button>
    </div>
  );
}

function StatePanel({ title, message, children }) {
  return (
    <section className="state-panel">
      <h2>{title}</h2>
      <p>{message}</p>
      {children}
    </section>
  );
}

function NavLink({ item, currentPath, navigate }) {
  const isActive = currentPath === item.path;

  return (
    <a
      className={isActive ? "nav-link active" : "nav-link"}
      href={item.path}
      aria-current={isActive ? "page" : undefined}
      onClick={(event) => handleNavClick(event, item.path, navigate)}
    >
      {item.label}
    </a>
  );
}

function handleNavClick(event, path, navigate) {
  event.preventDefault();
  navigate(path);
}

function getAuthErrorMessage(error, isCreate) {
  const code = error?.code || "";

  if (code.includes("invalid-credential") || code.includes("wrong-password")) {
    return "Login failed. Check the email and password, then try again.";
  }

  if (code.includes("user-not-found")) {
    return "No account was found for that email.";
  }

  if (code.includes("email-already-in-use")) {
    return "An account already exists for that email.";
  }

  if (code.includes("weak-password")) {
    return "Use a password with at least six characters.";
  }

  if (code.includes("invalid-email")) {
    return "Enter a valid email address.";
  }

  return isCreate
    ? "Account creation failed. Check the Firebase configuration and try again."
    : "Login failed. Check the Firebase configuration and try again.";
}

export default App;
