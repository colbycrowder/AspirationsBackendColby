import { useEffect, useMemo, useState } from "react";
import { appConfig } from "./config.js";
import { useAuth } from "./auth/AuthContext.jsx";
import { AttendanceManagement } from "./components/AttendanceManagement.jsx";
import { CredentialManagement } from "./components/CredentialManagement.jsx";
import { CredentialDetailPage } from "./components/CredentialDetailPage.jsx";
import { CredentialsPage } from "./components/CredentialsPage.jsx";
import { EducatorManagement } from "./components/EducatorManagement.jsx";
import { GovernmentOrganizationManagement } from "./components/GovernmentOrganizationManagement.jsx";
import { MyJourneyPage } from "./components/MyJourneyPage.jsx";
import { NotificationsPage } from "./components/NotificationsPage.jsx";
import { OperationsReporting } from "./components/OperationsReporting.jsx";
import { PartnerOrganizationManagement } from "./components/PartnerOrganizationManagement.jsx";
import { PilotEvaluationDashboard } from "./components/PilotEvaluationDashboard.jsx";
import { PilotMetricsDashboard } from "./components/PilotMetricsDashboard.jsx";
import { PilotReadinessDashboard } from "./components/PilotReadinessDashboard.jsx";
import { PilotReportingDashboard } from "./components/PilotReportingDashboard.jsx";
import { ProfileCompletionPage } from "./components/ProfileCompletionPage.jsx";
import { ProgramManagement } from "./components/ProgramManagement.jsx";
import { ProgramsPage } from "./components/ProgramsPage.jsx";
import { RwdLearningCenterPage } from "./components/RwdLearningCenterPage.jsx";
import { ServiceHoursPage } from "./components/ServiceHoursPage.jsx";
import { ServiceHoursManagement } from "./components/ServiceHoursManagement.jsx";
import { StaffDashboard } from "./components/StaffDashboard.jsx";
import { StaffMetricsDashboard } from "./components/StaffMetricsDashboard.jsx";
import { StaffRwdManagementPage } from "./components/StaffRwdManagementPage.jsx";
import { StaffYouthManagementPage } from "./components/StaffYouthManagementPage.jsx";
import { StakeholderRelationshipNotes } from "./components/StakeholderRelationshipNotes.jsx";
import { UserManagement } from "./components/UserManagement.jsx";
import { YouthDashboard } from "./components/YouthDashboard.jsx";
import { ApiAccessError, fetchStaffMetrics } from "./api.js";

const publicRoutes = [
  { path: "/login", label: "Login", title: "Login", public: true },
  { path: "/create-account", label: "Create Account", title: "Create Account", public: true },
];

const youthRoutes = [
  { path: "/dashboard", label: "Home", title: "Home", protected: true },
  { path: "/journey", label: "My Journey", title: "My Journey", protected: true },
  { path: "/profile", label: "Profile", title: "Profile", protected: true },
  { path: "/programs", label: "Programs", title: "Programs", protected: true },
  { path: "/credentials", label: "Credential Explorer", title: "Credential Explorer", protected: true },
  { path: "/credentials/:credentialId", label: "Credential Detail", title: "Credential Detail", protected: true, hidden: true },
  { path: "/rwd-learning-center", label: "Global Civic Movements", title: "Global Civic Movements", protected: true },
  { path: "/notifications", label: "Notifications", title: "Notifications", protected: true },
  { path: "/attendance", label: "Attendance", title: "Attendance", protected: true },
  { path: "/service-hours", label: "Service Hours", title: "Service Hours", protected: true },
];

const staffRoutes = [
  { path: "/staff", label: "Staff Dashboard", title: "Staff Dashboard", protected: true, staff: true },
  { path: "/staff/users", label: "User Management", title: "User Management", protected: true, staff: true },
  { path: "/staff/youth-management", label: "Youth Management", title: "Youth Management", protected: true, staff: true },
  { path: "/staff/program-management", label: "Program Management", title: "Program Management", protected: true, staff: true },
  { path: "/staff/credential-management", label: "Credential Management", title: "Credential Management", protected: true, staff: true },
  { path: "/staff/rwd-management", label: "RWD Management", title: "RWD Management", protected: true, staff: true },
  { path: "/staff/attendance-management", label: "Attendance Management", title: "Attendance Management", protected: true, staff: true },
  { path: "/staff/service-hour-management", label: "Service Hour Management", title: "Service Hour Management", protected: true, staff: true },
  { path: "/staff/educators", label: "Educator Management", title: "Educator Management", protected: true, staff: true },
  { path: "/staff/partners", label: "Partner Management", title: "Partner Management", protected: true, staff: true },
  { path: "/staff/government", label: "Government Management", title: "Government Management", protected: true, staff: true },
  { path: "/staff/relationships", label: "Relationships", title: "Relationships", protected: true, staff: true },
  { path: "/staff/pilot-readiness", label: "Pilot Readiness", title: "Pilot Readiness", protected: true, staff: true },
  { path: "/staff/pilot-metrics", label: "Pilot Metrics", title: "Pilot Metrics", protected: true, staff: true },
  { path: "/staff/pilot-evaluation", label: "Pilot Evaluation", title: "Pilot Evaluation", protected: true, staff: true },
  { path: "/staff/operations-reporting", label: "Operations Reporting", title: "Operations Reporting", protected: true, staff: true },
  { path: "/staff/reporting", label: "Reporting", title: "Reporting", protected: true, staff: true },
  { path: "/staff/metrics", label: "Metrics", title: "Metrics", protected: true, staff: true },
];

const routes = [
  { path: "/", label: "Home", title: "ASPN Platform", public: true },
  ...publicRoutes,
  ...youthRoutes,
  ...staffRoutes,
];

const youthPrimaryNavigation = selectRoutes(youthRoutes, [
  "/dashboard",
  "/journey",
  "/programs",
  "/rwd-learning-center",
  "/profile",
]);

const youthSecondaryNavigation = selectRoutes(youthRoutes, [
  "/credentials",
  "/service-hours",
  "/notifications",
]);

const youthMobileNavigation = [
  { path: "/dashboard", label: "Home" },
  { path: "/journey", label: "Journey" },
  { path: "/programs", label: "Programs" },
  { path: "/rwd-learning-center", label: "Learning" },
  { path: "/profile", label: "Profile" },
];

const staffNavigationGroups = [
  { label: "Overview", paths: ["/staff"] },
  { label: "Youth", paths: ["/staff/users", "/staff/youth-management"] },
  { label: "Programs", paths: ["/staff/program-management"] },
  { label: "Credentials", paths: ["/staff/credential-management"] },
  { label: "Participation", paths: ["/staff/attendance-management", "/staff/service-hour-management", "/staff/rwd-management"] },
  { label: "Organizations", paths: ["/staff/educators", "/staff/partners", "/staff/government"] },
  { label: "Stakeholders", paths: ["/staff/relationships"] },
  { label: "Reports", paths: ["/staff/reporting", "/staff/metrics"] },
  {
    label: "Pilot Tools",
    paths: ["/staff/pilot-readiness", "/staff/pilot-metrics", "/staff/pilot-evaluation", "/staff/operations-reporting"],
  },
].map((group) => ({ ...group, routes: selectRoutes(staffRoutes, group.paths) }));

const pageDetails = {
  "/": {
    eyebrow: "Frontend Scaffold",
    description: "Firebase Auth is now wired for login, account creation, logout, and protected route shells.",
    items: ["Backend API calls are not connected yet.", "Staff role checks are placeholders for a later checkpoint."],
  },
  "/dashboard": {
    eyebrow: "Youth Home",
    description: "Your next steps, progress, learning, and opportunities in one place.",
    items: ["Profile", "Programs", "Credentials", "Service hours", "Global Civic Movements", "Opportunities"],
  },
  "/journey": {
    eyebrow: "Youth Journey",
    description: "A developmental record of your programs, learning, credentials, and service milestones.",
    items: ["Journey summary", "Program participation", "Credentials", "Learning", "Service milestones"],
  },
  "/programs": {
    eyebrow: "Programs",
    description: "Future active-program browsing and self-enrollment screen.",
    items: ["List active programs", "View program details", "Enroll using verified Firebase UID"],
  },
  "/profile": {
    eyebrow: "Youth Profile",
    description: "Complete the private ASPN profile used by the dashboard and onboarding flow.",
    items: ["Basic information", "School", "Graduation year", "Interests"],
  },
  "/credentials": {
    eyebrow: "Credentials",
    description: "Future credential wallet and available credential display.",
    items: ["Earned credentials", "Available credentials", "Requirement text", "Placeholder icon support"],
  },
  "/rwd-learning-center": {
    eyebrow: "Global Civic Movements",
    description: "Explore externally hosted Global Civic Movements activities and track your progress.",
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
  "/staff/rwd-management": {
    eyebrow: "Staff/Admin",
    description: "RWD activity creation and update workflow.",
    items: ["Create RWD activities", "Update active activities", "Link optional credential definition IDs"],
  },
  "/staff/service-hour-management": {
    eyebrow: "Staff/Admin",
    description: "Future service-hour review workflow.",
    items: ["Create or review records", "Configure request URL", "No Google Form sync yet"],
  },
  "/staff/educators": {
    eyebrow: "Staff/Admin",
    description: "Staff-managed educator directory and relationship-management layer.",
    items: ["Create educator records", "Update contact details", "Activate or deactivate educator relationships"],
  },
  "/staff/partners": {
    eyebrow: "Staff/Admin",
    description: "Staff-managed partner organization directory and relationship-management layer.",
    items: ["Create partner organizations", "Update contact details", "Activate or deactivate partner relationships"],
  },
  "/staff/government": {
    eyebrow: "Staff/Admin",
    description: "Staff-managed government and public-sector organization relationship layer.",
    items: ["Create government organization records", "Track workforce and credential partner flags", "Activate or deactivate public-sector relationships"],
  },
  "/staff/relationships": {
    eyebrow: "Staff/Admin",
    description: "Staff-managed relationship notes across educator, partner, and government stakeholder directories.",
    items: ["Track partnership stage", "Record follow-up dates", "Review upcoming and overdue relationship activity"],
  },
  "/staff/pilot-readiness": {
    eyebrow: "Staff/Admin",
    description: "Staff-facing readiness dashboard for controlled pilot launch preparation.",
    items: ["Readiness score", "Blockers and warnings", "Operational checklist"],
  },
  "/staff/pilot-metrics": {
    eyebrow: "Staff/Admin",
    description: "Centralized pilot data collection dashboard for staff measurement review.",
    items: ["Registration funnel", "Program engagement", "Credential engagement", "Service engagement", "Operations activity"],
  },
  "/staff/pilot-evaluation": {
    eyebrow: "Staff/Admin",
    description: "Outcome-oriented pilot evaluation dashboard for staff review.",
    items: ["Executive summary", "Outcome scores", "Strengths", "Concerns", "Recommended actions"],
  },
  "/staff/metrics": {
    eyebrow: "Staff/Admin",
    description: "Future aggregate metrics display.",
    items: ["Youth users", "Programs", "Enrollments", "Credentials", "Attendance", "Service hours", "RWD", "Notifications"],
  },
};

function selectRoutes(sourceRoutes, paths) {
  return paths
    .map((path) => sourceRoutes.find((route) => route.path === path))
    .filter(Boolean);
}

function getCurrentPath() {
  return window.location.pathname === "/" ? "/" : window.location.pathname.replace(/\/$/, "");
}

function shouldHideRouteHeader(path) {
  return path.startsWith("/credentials/")
    || [
    "/dashboard",
    "/journey",
    "/profile",
    "/programs",
    "/service-hours",
    "/notifications",
    "/rwd-learning-center",
    "/credentials",
    "/credentials/:credentialId",
  ].includes(path);
}

function resolveRoute(currentPath) {
  const exactRoute = routes.find((item) => item.path === currentPath);
  if (exactRoute) {
    return exactRoute;
  }

  if (currentPath.startsWith("/credentials/")) {
    const credentialDetailRoute = youthRoutes.find((item) => item.path === "/credentials/:credentialId");
    return {
      ...credentialDetailRoute,
      credentialId: decodeURIComponent(currentPath.replace("/credentials/", "")),
    };
  }

  return routes[0];
}

function App() {
  const auth = useAuth();
  const [currentPath, setCurrentPath] = useState(getCurrentPath());
  const [navigationAccess, setNavigationAccess] = useState("checking");

  useEffect(() => {
    const onPopState = () => setCurrentPath(getCurrentPath());
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, []);

  useEffect(() => {
    let isActive = true;

    if (auth.loading) {
      setNavigationAccess("checking");
      return () => {
        isActive = false;
      };
    }

    if (!auth.isSignedIn) {
      setNavigationAccess("public");
      return () => {
        isActive = false;
      };
    }

    setNavigationAccess("checking");
    fetchStaffMetrics(auth.user)
      .then(() => {
        if (isActive) {
          setNavigationAccess("staff");
        }
      })
      .catch(() => {
        if (isActive) {
          setNavigationAccess("youth");
        }
      });

    return () => {
      isActive = false;
    };
  }, [auth.isSignedIn, auth.loading, auth.user]);

  const route = useMemo(() => resolveRoute(currentPath), [currentPath]);
  const detail = pageDetails[route.path] || pageDetails["/"];
  const hideRouteHeader = shouldHideRouteHeader(route.path);

  function navigate(path) {
    window.history.pushState({}, "", path);
    setCurrentPath(path);
  }

  return (
    <div className={`app-shell access-${navigationAccess}`}>
      <aside className="sidebar" aria-label="Main navigation">
        <a className="brand" href="/" onClick={(event) => handleNavClick(event, "/", navigate)}>
          <span className="brand-mark">A</span>
          <span>
            <strong>ASPN</strong>
            <small>Platform MVP</small>
          </span>
        </a>

        <AuthStatus navigate={navigate} />

        <AppNavigation access={navigationAccess} currentPath={currentPath} navigate={navigate} />
      </aside>

      <main className="main-panel">
        {!hideRouteHeader ? (
          <header className="topbar">
            <div>
              <span className="eyebrow">{detail.eyebrow}</span>
              <h1>{route.title}</h1>
            </div>
            <div className="status-pill">{auth.isSignedIn ? "Signed In" : "Signed Out"}</div>
          </header>
        ) : null}

        <section className={["/dashboard", "/journey", "/rwd-learning-center", "/credentials", "/credentials/:credentialId"].includes(route.path) ? "workspace youth-home-workspace" : "workspace"}>
          <RouteContent route={route} detail={detail} navigate={navigate} />
        </section>
      </main>

      {navigationAccess === "youth" ? (
        <MobileYouthNavigation currentPath={currentPath} navigate={navigate} />
      ) : null}
    </div>
  );
}

function MobileYouthNavigation({ currentPath, navigate }) {
  return (
    <nav className="mobile-youth-navigation" aria-label="Youth mobile navigation">
      {youthMobileNavigation.map((item) => {
        const isActive = currentPath === item.path;
        return (
          <a
            className={isActive ? "mobile-youth-tab active" : "mobile-youth-tab"}
            href={item.path}
            aria-current={isActive ? "page" : undefined}
            key={item.path}
            onClick={(event) => handleNavClick(event, item.path, navigate)}
          >
            {item.label}
          </a>
        );
      })}
    </nav>
  );
}

function AppNavigation({ access, currentPath, navigate }) {
  if (access === "checking") {
    return <div className="nav-access-state">Checking access...</div>;
  }

  if (access === "public") {
    return (
      <NavigationGroup
        currentPath={currentPath}
        label="Welcome"
        navigate={navigate}
        routes={publicRoutes}
      />
    );
  }

  if (access === "staff") {
    return (
      <div className="role-navigation" aria-label="Staff navigation">
        <div className="role-navigation-label">Staff workspace</div>
        {staffNavigationGroups.map((group) => (
          <NavigationGroup
            currentPath={currentPath}
            key={group.label}
            label={group.label}
            navigate={navigate}
            routes={group.routes}
          />
        ))}
      </div>
    );
  }

  return (
    <div className="role-navigation" aria-label="Youth navigation">
      <div className="role-navigation-label">My ASPN</div>
      <NavigationGroup
        currentPath={currentPath}
        label="Journey"
        navigate={navigate}
        routes={youthPrimaryNavigation}
      />
      <NavigationGroup
        currentPath={currentPath}
        label="More"
        navigate={navigate}
        routes={youthSecondaryNavigation}
      />
    </div>
  );
}

function NavigationGroup({ currentPath, label, navigate, routes }) {
  if (!routes.length) {
    return null;
  }

  return (
    <nav className="nav-section" aria-label={label}>
      <p>{label}</p>
      {routes.map((item) => (
        <NavLink key={item.path} item={item} currentPath={currentPath} navigate={navigate} />
      ))}
    </nav>
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

  if (route.staff) {
    return (
      <StaffGate route={route}>
        {(metrics) =>
          route.path === "/staff" ? (
            <StaffDashboard metrics={metrics} navigate={navigate} />
          ) : route.path === "/staff/metrics" ? (
            <StaffMetricsDashboard metrics={metrics} />
          ) : route.path === "/staff/users" ? (
            <UserManagement />
          ) : route.path === "/staff/youth-management" ? (
            <StaffYouthManagementPage />
          ) : route.path === "/staff/program-management" ? (
            <ProgramManagement />
          ) : route.path === "/staff/credential-management" ? (
            <CredentialManagement />
          ) : route.path === "/staff/rwd-management" ? (
            <StaffRwdManagementPage />
          ) : route.path === "/staff/attendance-management" ? (
            <AttendanceManagement />
          ) : route.path === "/staff/service-hour-management" ? (
            <ServiceHoursManagement />
          ) : route.path === "/staff/educators" ? (
            <EducatorManagement />
          ) : route.path === "/staff/partners" ? (
            <PartnerOrganizationManagement />
          ) : route.path === "/staff/government" ? (
            <GovernmentOrganizationManagement />
          ) : route.path === "/staff/relationships" ? (
            <StakeholderRelationshipNotes />
          ) : route.path === "/staff/pilot-readiness" ? (
            <PilotReadinessDashboard />
          ) : route.path === "/staff/pilot-metrics" ? (
            <PilotMetricsDashboard />
          ) : route.path === "/staff/pilot-evaluation" ? (
            <PilotEvaluationDashboard />
          ) : route.path === "/staff/operations-reporting" ? (
            <OperationsReporting />
          ) : route.path === "/staff/reporting" ? (
            <PilotReportingDashboard />
          ) : (
            <PlaceholderPage route={route} detail={detail} />
          )
        }
      </StaffGate>
    );
  }

  if (route.path === "/dashboard") {
    return <YouthDashboard navigate={navigate} />;
  }

  if (route.path === "/journey") {
    return <MyJourneyPage navigate={navigate} />;
  }

  if (route.path === "/profile") {
    return <ProfileCompletionPage navigate={navigate} />;
  }

  if (route.path === "/programs") {
    return <ProgramsPage />;
  }

  if (route.path === "/credentials") {
    return <CredentialsPage navigate={navigate} />;
  }

  if (route.path === "/credentials/:credentialId") {
    return <CredentialDetailPage credentialId={route.credentialId} navigate={navigate} />;
  }

  if (route.path === "/rwd-learning-center") {
    return <RwdLearningCenterPage />;
  }

  if (route.path === "/notifications") {
    return <NotificationsPage />;
  }

  if (route.path === "/service-hours") {
    return <ServiceHoursPage />;
  }

  return <PlaceholderPage route={route} detail={detail} />;
}

function StaffGate({ children }) {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isActive = true;

    async function checkStaffAccess() {
      setLoading(true);
      setError("");

      try {
        const data = await fetchStaffMetrics(user);
        if (isActive) {
          setMetrics(data);
        }
      } catch (nextError) {
        if (isActive) {
          setError(getStaffAccessMessage(nextError));
          setMetrics(null);
        }
      } finally {
        if (isActive) {
          setLoading(false);
        }
      }
    }

    checkStaffAccess();

    return () => {
      isActive = false;
    };
  }, [user]);

  if (loading) {
    return <StatePanel title="Checking staff access" message="Verifying staff/admin authorization with the backend." />;
  }

  if (error) {
    return <StatePanel title="Access denied" message={error} />;
  }

  return children(metrics);
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
      navigate(isCreate ? "/profile" : "/dashboard");
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
          ? "Create your sign-in account, then complete your ASPN profile so Home has the right starting point."
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
          <strong>Staff/admin access verified.</strong>
          <span> This placeholder is behind the protected backend staff/admin authorization check.</span>
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

function getStaffAccessMessage(error) {
  if (error instanceof ApiAccessError && error.status === 401) {
    return "Sign in with a valid Firebase account before opening staff/admin tools.";
  }

  if (error instanceof ApiAccessError && error.status === 403) {
    return "This account does not have staff or admin access.";
  }

  return error.message || "Staff/admin access could not be verified. Confirm the backend is running and try again.";
}

export default App;
