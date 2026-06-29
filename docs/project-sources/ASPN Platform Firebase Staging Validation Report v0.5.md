# ASPN Platform Firebase Staging Validation Report v0.5

## Purpose

This report defines Firebase-related validation items for ASPN Platform v0.5 after completion of Checkpoint 26C.

The goal is to confirm that Firebase Authentication, Firestore, Hosting, security assumptions, and pilot operations are ready for controlled pilot use.

## Platform Status

Version: **v0.5**  
Current stage: **Pilot-Capable**  
Completed through: **Checkpoint 26C — Pilot Evaluation Dashboard**  
Active Firestore database: **`(default)`**  
Legacy Firestore database: **`aspirationnetworkusers`**

Do not use the legacy database for active platform operations unless a future audit explicitly approves it.

## Firebase Authentication Checklist

Required checks:
- Firebase Authentication is enabled.
- Email/password sign-in is enabled.
- Youth account creation works.
- Staff account login works.
- Invalid login attempts fail.
- Firebase ID token is generated after login.
- Frontend sends Firebase ID token to backend.
- Backend rejects missing or invalid tokens.

## Role and Authorization Checklist

Required checks:
- staff users have `role = staff` or `role = admin`
- staff/admin users can access `/staff` pages
- staff/admin users can access staff-only backend routes
- member/youth users cannot access staff-only backend routes
- educator, partner, and government reserved roles are not automatically granted staff access

Allowed staff roles:
- `staff`
- `admin`

Denied or reserved roles:
- `member`
- `youth`
- `educator`
- `partner`
- `government`

## Firestore Collection Checklist

Core collections:
- `aspirationnetworkusers`
- `programs`
- `programEnrollments`
- `credentialDefinitions`
- `earnedCredentials`
- `attendanceRecords`
- `serviceHourRecords`
- `rwdActivities`
- `rwdProgress`
- `notifications`
- `systemSettings`

Analytics and research collections:
- `platformEvents`
- `staffOperationEvents`
- `externalDatasets`
- `participantExternalLinks`

Stakeholder collections:
- `educators`
- `partnerOrganizations`
- `governmentOrganizations`
- `stakeholderRelationshipNotes`

## Staff Workflow Validation

Programs:
- staff can create programs
- staff can update programs
- staff can archive programs
- staff can restore programs
- program records appear in Firestore

Credentials:
- staff can create credential definitions
- staff can archive and restore credential definitions
- staff can award credentials
- earned credentials appear in Firestore
- youth cannot self-award credentials

Attendance:
- staff can create attendance records
- staff can update attendance status
- attendance totals update correctly
- attendance records appear in Firestore

Service hours:
- staff can create service-hour records
- staff can approve service-hour records
- staff can reject service-hour records
- service-hour totals update correctly
- service-hour records appear in Firestore

Stakeholders:
- staff can create educator records
- staff can create partner organization records
- staff can create government organization records
- staff can create stakeholder relationship notes
- follow-up dates appear correctly in reporting

## Youth Workflow Validation

Required checks:
- youth can create an account
- youth can complete a profile
- youth profile remains private by default
- youth can enroll in active programs
- youth dashboard loads programs, credentials, attendance, service hours, RWD progress, and notifications
- youth cannot access staff-only routes

## Dashboard Validation

Confirm staff/admin users can load:
- Staff Dashboard
- Reporting Dashboard
- Credential Analytics Dashboard
- Program Analytics Dashboard
- Relationship Notes Dashboard
- Pilot Readiness Dashboard
- Pilot Metrics Dashboard
- Pilot Evaluation Dashboard

Confirm youth users can load:
- Youth Dashboard
- Programs
- Credentials
- Service Hours
- RWD Learning Center
- Notifications
- Profile Completion

## Hosting Validation

Required checks:
- frontend production build completes
- Firebase Hosting target is configured
- deployed frontend loads
- refresh on protected routes does not break routing
- environment variables point to correct backend/API configuration
- stale deployment assets are not being served
- staff routes are not usable without authentication

Frontend build command:

```bash
cd "/Users/colbycrowder/Documents/AspirationsBackendColby/frontend"
npm run build
```

## Backend Firebase Admin Validation

Required checks:
- Firebase Admin SDK initializes locally
- backend can verify Firebase ID tokens
- backend can read/write Firestore
- service account or application default credentials are configured securely
- credentials are not committed to GitHub
- backend test suite passes

Backend test command:

```bash
cd "/Users/colbycrowder/Documents/AspirationsBackendColby/UserData"
./gradlew clean test
```

## Security Review Checklist

Before broader production/public launch, review:
- Firestore rules
- youth profile access rules
- staff/admin access assumptions
- public read/write restrictions
- direct client access to sensitive collections
- backend-only enforcement assumptions
- legacy prototype paths
- service account handling
- hosting environment variables

## Legacy Endpoint Checklist

Confirm disabled legacy endpoints still return `HTTP 410 Gone`:
- `POST /api/createProfile`
- `GET /api/getUser/{id}`
- `GET /api/getUserWithCredentials/{id}`
- `GET /api/getallpost`
- `POST /api/createPost`
- `POST /api/creatPost`
- `POST /api/createComment`
- `GET /api/getCommentForPost/{postID}`
- `POST /api/upVote/{postID}`
- `DELETE /api/deletePost/{postID}`

## Pilot Launch Readiness Checklist

Minimum required before controlled youth pilot:
- Firebase Authentication enabled
- staff/admin accounts configured
- active Firestore database confirmed as `(default)`
- frontend build validated
- backend test suite passing
- staff routes protected
- youth profiles private by default
- pilot dashboards loading
- legacy endpoints disabled
- staff workflow QA completed
- documentation package v0.5 complete
- production readiness review scheduled or completed

## Final Firebase Determination

Current v0.5 status:
- Firebase environment appears appropriate for controlled pilot preparation.

Public launch status:
- Not yet approved.

Required before public launch:
- Production Readiness Review
- Firestore rules audit
- environment variable audit
- hosting review
- monitoring/logging plan
- mobile/browser QA

