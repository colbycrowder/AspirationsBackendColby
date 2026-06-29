# ASPN Platform Build Manual Addendum v0.4

Version: v0.4  
Date: June 11, 2026  
Status: Pilot Preparation Addendum  
Scope: Checkpoints 21A-21L

## Purpose

This addendum records the build history after the v0.3 youth MVP baseline. It documents the completion of the staff/admin MVP, Firebase staging validation, and the security hardening needed before a controlled Fall 2026 pilot.

## Build Phase Summary

The v0.4 build phase moved ASPN from youth-facing MVP completion into pilot operations readiness.

The main objective was to reduce dependence on Firebase Console and Firestore manual editing by giving staff/admin users enough frontend capability to operate the pilot safely.

## Checkpoint History

### Checkpoint 21A: Frontend App Scaffold

Created the Vite + React frontend in the `frontend` directory.

Added placeholder routes for youth and staff/admin screens, a navigation shell, environment configuration patterns, and README setup instructions.

### Checkpoint 21B: Firebase Auth Client Setup

Added Firebase client configuration through environment variables.

Implemented login, account creation, logout, auth state context, and protected route behavior.

### Checkpoint 21C: Youth Dashboard API Integration

Connected the youth dashboard to:

- GET /api/me/dashboard

The frontend retrieves the Firebase ID token and sends:

- Authorization: Bearer <Firebase ID token>

Dashboard sections include profile, programs, credentials, attendance, service hours, RWD learning, notifications, service-hour URL, and opportunities link.

### Checkpoint 21D: Youth Programs and Enrollment UI

Built youth-facing program browsing and self-enrollment using:

- GET /api/programs
- POST /api/me/program-enrollments

Validated that active programs appear, archived programs are excluded, duplicate enrollment is rejected, and enrolled programs appear on the youth dashboard.

### Checkpoint 21E: Credentials UI

Built youth-facing credential display using the dashboard response.

Displayed earned credentials and available credentials connected to active enrolled programs.

### Checkpoint 21F: Youth Service Hours UI

Built youth-facing service-hour display using the dashboard response.

Displayed approved/pending hour summaries, service-hour records, status, and the configured request form URL.

### Checkpoint 21G: RWD Learning Center UI

Built youth-facing RWD Learning Center display and progress actions.

Used dashboard RWD data and existing progress endpoints. External videos remain externally hosted.

### Checkpoint 21H: Notifications UI

Built youth-facing notifications page.

Displayed notification list, unread counts, and read tracking using protected youth notification endpoints.

### Checkpoint 21I: Profile Completion UI

Built protected youth profile completion flow.

The backend uses the verified Firebase UID. Youth cannot submit or alter UID, role, public profile status, staff verification, or permissions.

### Checkpoint 21J: Dashboard Enhancement

Enhanced the youth dashboard with:

- Profile completion card
- Next actions card
- Credential summary
- Service-hour summary
- RWD progress summary
- Notification summary

The dashboard remains the centerpiece of the youth experience.

### Checkpoint 21K-A: Staff Metrics Foundation

Built staff/admin API helper foundation and metrics dashboard.

Staff access is verified by backend role enforcement through Firebase token verification and Firestore role checks.

### Checkpoint 21K-B: Staff Youth and Program Management

Built staff/admin frontend screens for:

- Youth management
- Program management
- Enrollment management

Existing backend routes were reused.

### Checkpoint 21K-C: Staff Credentials and Service Hours Management

Built staff/admin frontend screens for:

- Credential definition creation
- Manual credential awarding
- User-specific service-hour lookup
- Service-hour record creation/review
- Service-hour request URL setting

Limitations were preserved and documented rather than inventing new backend architecture.

### Checkpoint 21K-D: Staff RWD Activity Management

Built staff/admin RWD activity management.

Staff/admin can create, update, and deactivate active RWD activities. Associated credential IDs can be entered manually when known.

### Checkpoint 21L: Legacy Endpoint Security Fix

Disabled legacy public profile/user and discussion/post endpoints with HTTP 410 Gone.

This reduced the largest remaining security blocker before real youth onboarding.

## Architecture Decisions Preserved

- Spring Boot backend remains the API layer.
- Firebase Admin SDK verifies backend tokens.
- Firestore remains the active datastore.
- React + Vite remains the frontend.
- Staff/admin roles are enforced by backend logic, not frontend assumptions.
- Youth profiles remain private by default.
- The dashboard remains the center of the youth experience.
- Social media, direct messaging, follower systems, and public youth discovery remain out of scope.

## Active Firestore Decision

The active MVP database is:

- (default)

The separate Firestore database named:

- aspirationnetworkusers

is legacy/prototype and must not be used for active MVP validation or pilot work unless explicitly approved after a database audit.

## Remaining Build Gaps

- Attendance Management UI
- Credential definition list/search for staff
- Earned credential reporting
- Service-hour pending queue
- Archived program recovery UI
- Inactive RWD activity recovery UI
- Mobile navigation refinement
- Production deployment hardening

## Pilot Build Status

The build is now in Pilot Preparation Phase. It is ready for controlled pilot operation with staff oversight, not broad public launch.

