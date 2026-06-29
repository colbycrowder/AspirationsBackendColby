# ASPN Platform Firebase Staging Validation Report v0.4 Expanded

Version: v0.4 Expanded  
Date: June 11, 2026  
Status: Passed Locally  
Audience: Future developers, ASPN leadership, university partners, government partners, grant funders

## 1. Executive Summary

Firebase staging validation for ASPN Platform v0.4 passed locally after completion of:

- Youth MVP Checkpoints 21A-21J
- Staff/Admin MVP Checkpoints 21K-A through 21K-D
- Legacy Endpoint Security Fix Checkpoint 21L

The validation confirms that core youth workflows, staff/admin workflows, Firebase Authentication, backend token verification, Firestore access, and staff role authorization are functioning against the active Firestore database `(default)`.

The platform is conditionally ready for a controlled 60-100 youth Fall 2026 pilot. It is not approved for public launch.

## 2. Validation Objectives

The validation objectives were:

1. Confirm repository readiness.
2. Confirm backend tests pass.
3. Confirm frontend production build passes.
4. Confirm Firebase Authentication works.
5. Confirm Firebase Admin backend credential setup works.
6. Confirm Firestore reads/writes use `(default)`.
7. Confirm youth workflows function end-to-end.
8. Confirm staff/admin workflows function end-to-end.
9. Confirm staff role authorization works.
10. Confirm youth/member accounts are denied from staff routes.
11. Confirm legacy public endpoints are disabled.
12. Confirm the platform is ready for Pilot Readiness Review v3.

## 3. Validation Environment

Frontend:

- Vite + React
- Local URL: http://localhost:5173
- Firebase Web Authentication
- Environment variables through frontend `.env`

Backend:

- Spring Boot
- Local URL: http://localhost:8080
- Firebase Admin SDK
- Google Application Default Credentials or service-account JSON path through local environment

Firestore:

- Active database: `(default)`
- Legacy database: `aspirationnetworkusers`

The legacy database was not used for MVP validation.

## 4. Repository Validation

### Clean Branch

Status: PASS

The repository was considered clean after latest checkpoint completion, commit, and push.

### Build Validation

Frontend production build:

```bash
cd frontend
npm run build
```

Status: PASS

### Test Validation

Backend test command:

```bash
cd UserData
./gradlew clean test
```

Status: PASS locally

The backend test suite validates protected route behavior, service logic, and the legacy endpoint security changes.

## 5. Firebase Validation

Status: PASS

Validated:

- Firebase project accessible
- Firebase Auth enabled
- Email/password auth working
- Firebase Admin service account / ADC setup works locally
- Backend can start successfully with Firebase Admin credentials
- Backend can verify Firebase ID tokens

## 6. Firestore Validation

Status: PASS

Validated:

- Active database is `(default)`
- Youth profile documents are read from `(default)/aspirationnetworkusers/{FirebaseUID}`
- Programs are read from `(default)/programs`
- Staff/admin role profiles are read from `(default)/aspirationnetworkusers/{FirebaseUID}`

Required active collections:

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

## 7. Authentication Validation

Status: PASS

Validated:

- Create account works in Firebase Auth
- Login works
- Frontend retrieves Firebase ID token
- Backend accepts valid token
- Backend rejects missing/invalid token
- Protected youth routes require authentication

## 8. Youth Workflow Validation

### Create Account

Status: PASS

Youth account creation works through Firebase Authentication.

### Login

Status: PASS

Youth login works and redirects into protected frontend areas.

### Profile Completion

Status: PASS

Youth profile completion works through protected route:

- PATCH /api/me/profile

The backend uses the verified token UID.

### Dashboard

Status: PASS

Dashboard loads through:

- GET /api/me/dashboard

The dashboard displays profile, programs, credentials, attendance, service hours, RWD, notifications, and related summary information.

### Program Enrollment

Status: PASS

Youth program enrollment works through:

- POST /api/me/program-enrollments

Duplicate active enrollment is rejected. Archived programs are not listed through active program routes.

### Credentials

Status: PASS

Youth credentials display through dashboard-derived data.

### Service Hours

Status: PASS

Youth service-hour records and request form URL display through dashboard-derived data.

### RWD Learning Center

Status: PASS

Youth RWD Learning Center loads active RWD items and supports progress updates.

### Notifications

Status: PASS

Youth notifications display and read tracking work through protected notification endpoints.

## 9. Staff Workflow Validation

### Metrics

Status: PASS

Staff metrics dashboard loads through:

- GET /api/staff/metrics

### Youth Management

Status: PASS

Staff/admin can list youth users and update limited staff review fields.

### Program Management

Status: PASS

Staff/admin can create, update, and archive active programs.

### Enrollment Management

Status: PASS

Staff/admin can review and remove enrollments.

### Credential Management

Status: PASS with limitation

Staff/admin can create credential definitions and manually award credentials.

Limitation:

- no staff credential definition list UI

### Service-Hour Management

Status: PASS with limitation

Staff/admin can look up youth-specific service-hour records, create/review service-hour records, and update the service-hour request URL.

Limitation:

- no all-pending service-hour queue

### RWD Management

Status: PASS with limitation

Staff/admin can create, update, and deactivate active RWD activities.

Limitation:

- inactive RWD activities cannot be listed after deactivation

## 10. Access Control Validation

### member denied staff routes

Status: PASS

Youth/member account was denied from staff/admin routes.

### staff allowed

Status: PASS

Staff role account can access staff/admin routes.

### admin allowed

Status: PASS / expected

Admin role is allowed by backend `requireStaff` logic.

### reserved roles denied

Status: PASS / expected

Reserved roles are not included in the staff role set:

- educator
- partner
- government

## 11. Security Validation

Status: PASS for controlled pilot

Validated:

- protected youth routes require Firebase token
- protected staff routes require staff/admin role
- frontend does not expose raw tokens
- backend remains source of truth for authorization
- youth/member users cannot access staff/admin pages

## 12. Legacy Endpoint Validation

Status: PASS after 21L

Legacy profile routes disabled:

- POST /api/createProfile
- GET /api/getUser/{id}
- GET /api/getUserWithCredentials/{id}

Legacy discussion routes disabled:

- GET /api/getallpost
- POST /api/createPost
- POST /api/creatPost
- POST /api/createComment
- GET /api/getCommentForPost/{postID}
- POST /api/upVote/{postID}
- DELETE /api/deletePost/{postID}

Expected response:

- HTTP 410 Gone

## 13. Firestore Database Decision

ACTIVE DATABASE:

```text
(default)
```

LEGACY DATABASE:

```text
aspirationnetworkusers
```

Do not use the legacy database for active MVP operations.

Do not delete the legacy database without explicit approval and database audit.

## 14. Known Issues

- Attendance Management UI not yet built.
- Staff credential catalog/list UI not available.
- Service-hour pending queue not available.
- Archived program list/recovery not available.
- Inactive RWD list/recovery not available.
- Mobile/browser device QA still recommended.

## 15. Non-Blocking Risks

- Staff workflows sometimes require manual UID or credential ID entry.
- Mobile navigation may be long on small screens.
- Date input behavior should be checked on Safari and Firefox.
- Staff may still need Firebase Console for role setup and unusual data correction.

## 16. Pilot Readiness Implications

The platform is ready for:

- internal demo
- staff training
- controlled pilot planning
- controlled 60-100 youth pilot with staff oversight

The platform is not ready for:

- broad public launch
- unsupervised public onboarding
- public profile discovery
- direct messaging
- social networking

## 17. Final Recommendation

Proceed to internal demo and controlled pilot preparation.

Before real youth onboarding:

- perform real-device mobile/browser QA
- prepare staff operating guide
- confirm seed data
- confirm staff/admin roles
- confirm support process

Do not proceed to public launch until production readiness review is complete.

## 18. Formal Sign-Off Section

Validation phase:

- ASPN Platform v0.4 Firebase Staging Validation

Validation result:

- PASS LOCALLY

Pilot readiness result:

- CONDITIONAL GO for controlled 60-100 youth pilot

Required before public launch:

- Production launch readiness review
- Production security review
- Mobile/accessibility QA
- Monitoring and support planning

