# ASPN Platform Build Manual Addendum v0.4 Expanded

Version: v0.4 Expanded  
Date: June 11, 2026  
Status: Institutional Build Record  
Audience: ASPN leadership, future developers, university partners, government partners, grant funders

## 1. Executive Summary

The ASPN Platform v0.4 build phase represents the transition from backend foundation and youth-facing MVP into a pilot-ready platform with operational staff/admin tools and a remediated legacy security surface.

As of this addendum, the platform has completed:

- Youth MVP Checkpoints 21A-21J
- Staff/Admin MVP Checkpoints 21K-A through 21K-D
- Legacy Endpoint Security Fix Checkpoint 21L
- Firebase staging validation
- Pilot Readiness Review v3

The current pilot target is 60-100 youth participants for a controlled Fall 2026 pilot.

The platform is conditionally ready for controlled pilot operation. It is not yet approved for broad public launch.

The most important architectural decision in this phase is that the active MVP Firestore database is `(default)`. The separate Firestore database named `aspirationnetworkusers` must be treated as legacy/prototype and must not be used for active MVP operations unless a future database audit explicitly approves it.

The current product philosophy remains unchanged:

- youth safety first
- private-by-default youth profiles
- staff oversight
- educational and civic purpose
- no public youth discovery
- no direct messaging
- no follower/friend system
- no popularity-driven social engagement

## 2. Platform Evolution

### From MVP Foundation

The earliest MVP foundation established the Spring Boot backend, Firebase Admin SDK, Firestore persistence, youth profile data structures, credential foundations, attendance foundations, service-hour foundations, RWD foundations, notification foundations, and metrics foundations.

The backend established the pattern that later became central to the platform:

- Firebase Authentication provides identity.
- The backend verifies Firebase ID tokens.
- Firestore stores youth, program, credential, service-hour, RWD, notification, and staff-related data.
- Staff/admin access is determined by Firestore user profile role.
- Youth-owned operations must derive the user UID from the verified Firebase token, not from client-supplied UID fields.

This foundation was intentionally conservative. It created structures for future expansion without building unsafe public profile, social, or direct messaging behavior.

### Through Youth MVP

The frontend MVP began with a Vite + React scaffold in the `frontend` directory. It introduced Firebase client authentication, protected route shells, dashboard integration, and then a series of youth-facing pages.

The youth MVP became complete through Checkpoint 21J. Youth can now:

- create a Firebase Authentication account
- log in
- complete a private profile
- view a central dashboard
- browse active programs
- enroll in programs
- view earned and available credentials
- view service-hour records and request link
- open RWD Learning Center activities
- update RWD progress
- view and mark notifications as read

The dashboard became the center of the youth experience. Programs, credentials, service hours, RWD learning, notifications, and profile completion all support the dashboard rather than replacing it.

### Through Staff/Admin MVP

After youth MVP completion, the platform moved into Staff/Admin MVP work. The goal was to reduce staff dependence on Firebase Console and Firestore manual editing during pilot operations.

Staff/Admin MVP completed:

- 21K-A Staff Metrics Foundation
- 21K-B Youth + Program Management
- 21K-C Credential + Service Hour Management
- 21K-D RWD Activity Management

Staff/admin can now:

- view platform metrics
- list and review youth users
- update limited staff-managed youth review fields
- create, update, and archive active programs
- review and remove enrollments
- create credential definitions
- manually award credentials
- create/review service-hour records for a youth
- configure the service-hour request form URL
- create, update, and deactivate RWD activities

Staff/admin routes remain protected by backend Firebase token verification and role checks. The frontend does not grant staff access by assumption.

### Through Security Hardening

Pilot Readiness Review v2 identified legacy public endpoints as the final major blocker before real youth onboarding. These endpoints came from earlier prototype discussion/profile functionality and were not consistent with the current protected route model.

Checkpoint 21L disabled these legacy endpoints with HTTP 410 Gone:

- POST /api/createProfile
- GET /api/getUser/{id}
- GET /api/getUserWithCredentials/{id}
- GET /api/getallpost
- POST /api/createPost
- POST /api/creatPost
- POST /api/createComment
- GET /api/getCommentForPost/{postID}
- POST /api/upVote/{postID}
- DELETE /api/deletePost/{postID}

This remediation preserved current youth and staff/admin MVP functionality while removing the public legacy surface that could expose or mutate youth/profile/discussion data.

## 3. Current Platform Scope

The current ASPN Platform scope is pilot preparation for a controlled 60-100 youth Fall 2026 pilot.

Current in-scope capabilities:

- youth account creation
- youth login
- private youth profile completion
- youth dashboard
- active program browsing
- youth self-enrollment
- credential display
- staff credential definition creation
- staff manual credential awarding
- service-hour display
- staff service-hour creation/review
- staff service-hour request URL configuration
- RWD Learning Center activity display
- youth RWD progress tracking
- staff RWD activity management
- notifications
- staff metrics
- staff youth management
- staff program management
- staff enrollment management

Current out-of-scope capabilities:

- public youth profiles
- public youth search
- direct messaging
- follower/friend networks
- social feeds
- discussion boards
- workforce matching
- government pathway matching
- scholarship matching
- educator portals
- partner portals
- government portals
- public portfolios
- exports
- advanced analytics
- full production monitoring

## 4. Youth Workflow Walkthrough

### Create Account

Youth account creation uses Firebase Authentication through the React frontend. The create-account page collects email and password and creates a Firebase Auth user.

Firebase Auth account creation alone does not fully complete ASPN onboarding. The user also needs a Firestore youth profile document in the active `(default)` Firestore database.

### Login

Youth login uses Firebase Authentication. After login, the frontend listens to auth state and receives the signed-in Firebase user.

Protected youth API calls retrieve a Firebase ID token and send:

```text
Authorization: Bearer <Firebase ID token>
```

### Profile Completion

The protected profile completion flow uses:

- GET /api/me/profile
- PATCH /api/me/profile

The backend uses the verified Firebase token UID. Youth users cannot submit or modify:

- UID
- role
- staff/admin status
- publicProfile
- staffVerified
- staffReviewRequired
- permission flags

Youth profiles remain private by default.

### Dashboard

The youth dashboard uses:

- GET /api/me/dashboard

The dashboard returns a consolidated response including:

- profile summary
- enrolled programs
- earned credentials
- available credentials
- attendance records
- service-hour records
- RWD Learning Center items and progress
- unread notification count
- service-hour request form URL
- opportunities link

The dashboard also provides guidance:

- profile completion status
- next actions
- credential summary
- service-hour summary
- RWD progress summary
- notification summary

### Program Enrollment

Youth users can browse active programs through:

- GET /api/programs

Youth enrollment uses:

- POST /api/me/program-enrollments

The backend uses the verified Firebase UID for the enrolled user. Youth cannot enroll another UID. Archived programs are not available through the active program list.

### Credentials

Youth credentials appear from the dashboard response.

The platform supports:

- earned credentials
- available credentials by enrolled active program
- credential requirement text
- placeholder/default icon behavior
- manual staff awards
- attendance-count auto-award foundation
- RWD linked credential award foundation

The final ASPN credential catalog is not hardcoded.

### Service Hours

Youth can view service-hour records through the dashboard response and service-hours page.

Youth can access the configured service-hour request form URL when staff/admin has set it.

Youth service-hour submission is not a native in-app workflow yet. The current pilot model supports external request form linkage and staff/admin record creation/review.

### RWD Learning Center

Youth can view RWD Learning Center items from the dashboard response and RWD page.

RWD activities remain externally hosted. The platform stores metadata and progress records:

- country name
- title
- description
- external URL
- active status
- optional associated credential ID
- youth progress

Youth can mark progress through the existing progress endpoint.

### Notifications

Youth notifications are retrieved through:

- GET /api/me/notifications

Youth can mark notifications read through:

- PATCH /api/me/notifications/{notificationId}/read

Notifications currently support credential-earned events and unread counts.

## 5. Staff Workflow Walkthrough

### Metrics

Staff/admin metrics use:

- GET /api/staff/metrics

Metrics include aggregate platform counts suitable for pilot oversight.

### Youth Management

Staff/admin can list youth users, view basic profile details, and update limited staff-managed review fields:

- profileStatus
- staffReviewRequired
- staffVerified

Staff cannot edit youth identity, UID, role, public profile status, or youth-owned profile fields through this UI.

### Program Management

Staff/admin can:

- create programs
- update active programs
- archive programs
- review enrollments
- remove enrollments

Current limitation: archived programs are not listed after archival because the backend exposes active program listing but not all-program listing.

### Credential Management

Staff/admin can:

- create credential definitions
- enter program IDs
- enter requirement text
- enable active/inactive status
- configure future requirement metadata
- manually award credentials to youth by UID and credential definition ID

Current limitation: staff cannot browse all credential definitions in a catalog UI yet.

### Service-Hour Management

Staff/admin can:

- look up service-hour records by youth UID
- create or review service-hour records
- set verification status
- enter service date, hours, description, source, and optional form response URL
- configure the service-hour request form URL

Current limitation: no all-pending service-hour queue exists yet.

### RWD Management

Staff/admin can:

- view active RWD activities
- create RWD activities
- update active RWD activities
- deactivate activities
- enter optional associatedCredentialId

Current limitation: inactive RWD activities are not listed after deactivation.

## 6. Authentication Architecture

The platform uses Firebase Authentication for user sign-in and account creation.

Frontend:

- initializes Firebase client config from environment variables
- creates accounts
- logs users in and out
- listens to auth state
- retrieves Firebase ID tokens for protected backend calls

Backend:

- receives `Authorization: Bearer <Firebase ID token>`
- verifies tokens through Firebase Admin SDK
- derives UID from the verified token
- uses UID for youth-owned actions
- uses UID to load Firestore profile for staff/admin role checks

## 7. Role Architecture

### member

Default youth/member role.

Members can:

- complete their own private profile
- view their dashboard
- enroll in active programs
- view credentials
- view service hours
- view RWD activities
- update their own RWD progress
- view notifications

Members cannot access staff/admin routes.

### staff

Operational staff role.

Staff can access protected staff/admin routes and perform pilot management workflows.

### admin

Administrative role.

Admins receive staff/admin route access. Future expansion may differentiate staff and admin permissions more deeply.

### educator

Reserved future role. Not currently granted staff/admin access.

### partner

Reserved future role. Not currently granted staff/admin access.

### government

Reserved future role. Not currently granted staff/admin access.

## 8. Firestore Architecture

The active MVP database is:

```text
(default)
```

The legacy/prototype database is:

```text
aspirationnetworkusers
```

Do not use the legacy/prototype database for active MVP operations.

The backend uses Firestore collections through service classes. User profile documents are stored in:

```text
(default)/aspirationnetworkusers/{FirebaseUID}
```

## 9. Current Collection Inventory

Active MVP collections:

- aspirationnetworkusers
- programs
- programEnrollments
- credentialDefinitions
- earnedCredentials
- attendanceRecords
- serviceHourRecords
- rwdActivities
- rwdProgress
- notifications
- systemSettings

Legacy/prototype collections:

- aspirationnetworkposts
- comments

## 10. Security Model

The current security model relies on:

- Firebase Authentication
- Firebase Admin backend verification
- verified UID for youth-owned actions
- Firestore role checks for staff/admin access
- private-by-default youth profiles
- disabled legacy public endpoints
- absence of public profile discovery
- absence of direct messaging and follower networks

Backend role enforcement remains the source of truth. Frontend route guards improve UX but do not grant permissions.

## 11. Legacy Endpoint Security Fix Summary

Checkpoint 21L disabled legacy public endpoints with HTTP 410 Gone.

The fix prevents:

- unauthenticated profile creation with client-supplied UID
- unauthenticated youth profile lookup
- unauthenticated credential/profile lookup
- unauthenticated post/comment creation
- unauthenticated discussion deletion
- unauthenticated upvote mutation

This preserved the MVP while removing unsafe prototype behavior.

## 12. Pilot Operations Guidance

Before pilot onboarding:

- confirm all staff/admin accounts have correct Firestore roles
- seed active programs
- seed credential definitions
- seed RWD activities
- configure service-hour request URL
- test youth account creation and profile completion
- test staff management pages
- confirm mobile/browser QA
- keep all MVP data in `(default)`

During pilot:

- staff should use the staff/admin UI before using Firebase Console
- staff should document any manual Firestore interventions
- youth profile privacy should remain default
- do not re-enable discussion or public profile features

## 13. Known Limitations

- Attendance Management UI is deferred.
- Staff cannot browse all credential definitions.
- Staff cannot browse all earned credentials.
- Staff cannot view all pending service-hour records.
- Archived programs cannot be recovered through the UI.
- Inactive RWD activities cannot be recovered through the UI.
- Mobile navigation is usable but not polished.
- Public launch readiness is incomplete.

## 14. Post-Pilot Roadmap

Recommended post-pilot roadmap:

1. Pilot feedback review
2. Attendance Management UI if needed
3. Credential catalog/listing UI
4. Service-hour pending queue
5. Archived/inactive management workflows
6. Reporting and exports
7. Accessibility review
8. Mobile navigation polish
9. Production security hardening
10. Partner/educator/government role design
11. Workforce, government pathway, and scholarship matching foundations

## 15. Maintenance Notes

Future developers must:

- read project source hierarchy before architecture changes
- preserve youth safety constraints
- keep profiles private by default
- avoid public youth discovery
- avoid direct messaging unless explicitly approved with safety design
- use `(default)` Firestore for MVP data
- avoid deleting the legacy database without explicit approval
- update release notes, build manual, handoff guide, migration notes, and staging checklist after major changes

