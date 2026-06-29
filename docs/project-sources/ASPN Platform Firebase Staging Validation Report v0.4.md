# ASPN Platform Firebase Staging Validation Report v0.4

Version: v0.4  
Date: June 11, 2026  
Status: Passed Locally

## Executive Summary

Firebase staging validation for ASPN Platform v0.4 passed locally after completion of the youth MVP, staff/admin MVP, and Legacy Endpoint Security Fix.

The platform successfully validated the core youth and staff/admin workflows using the active Firestore database `(default)`.

The separate Firestore database named `aspirationnetworkusers` remains legacy/prototype and must not be used for active MVP validation.

## Repository Readiness

Status: PASS

Validated:

- Main branch clean after checkpoint completion
- Latest checkpoint committed and pushed
- Backend tests passed locally
- Frontend production build passed locally
- No application code changes pending during documentation phase

Latest relevant checkpoint:

- 21L: Legacy Endpoint Security Fix

## Firebase Readiness

Status: PASS

Validated:

- Firebase Authentication operational
- Firestore operational
- Firebase Admin service account / ADC setup operational locally
- Backend starts against Firebase successfully
- Frontend Firebase Auth works
- Active Firestore database confirmed as `(default)`

Important:

- Do not use the separate Firestore database named `aspirationnetworkusers` for active MVP validation.
- Treat that database as legacy/prototype until a future database audit confirms whether it contains useful historical data.

## Test Accounts

Status: PASS

Required account types:

- Youth/member test account
- Staff test account
- Optional admin test account

Staff/admin profile requirement:

```text
(default)/aspirationnetworkusers/{FirebaseUID}
role = staff
```

or:

```text
(default)/aspirationnetworkusers/{FirebaseUID}
role = admin
```

Youth profile expectation:

```text
(default)/aspirationnetworkusers/{FirebaseUID}
role = member
youthProfile = true
publicProfile = false
```

## Seed Data

Status: PASS with ongoing maintenance

Required seed data:

- At least one active program
- At least one active credential definition
- At least one active RWD activity
- systemSettings/serviceHourRequestFormUrl

Optional seed data:

- earned credential
- service-hour record
- notification record
- attendance record

## Youth Workflow Results

Status: PASS

Validated:

- Create account
- Login
- Complete profile
- Dashboard loads
- Join active program
- View credentials
- View service hours
- Open RWD Learning Center
- Mark RWD progress
- View notifications

## Staff/Admin Workflow Results

Status: PASS

Validated:

- Staff metrics dashboard
- Youth management
- Program management
- Enrollment management
- Credential creation
- Manual credential awarding
- Service-hour management
- Service-hour request URL update
- RWD activity creation/update/deactivation

## Access Control Results

Status: PASS

Validated:

- Signed-out users cannot access protected frontend routes
- Youth/member account denied from staff/admin routes
- Staff/admin account can access staff/admin routes
- Backend remains source of truth for role enforcement

Reserved roles:

- educator
- partner
- government

These roles are not staff/admin roles and should not receive staff access unless intentionally added later.

## Legacy Endpoint Security Validation

Status: PASS after 21L

The following endpoints now return HTTP 410 Gone:

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

## Blockers

No current staging blockers identified after 21L.

## Non-Blocking Issues

- Attendance Management UI remains deferred.
- Credential definition listing is not available in staff UI.
- Service-hour all-pending queue is not available.
- Archived programs and inactive RWD activities cannot be listed in staff UI.
- Mobile QA requires continued device testing.

## Recommendation

Proceed to controlled internal demo and controlled 60-100 youth pilot preparation.

Do not proceed to public launch until production security, mobile QA, monitoring, support workflows, and policy documentation are complete.

