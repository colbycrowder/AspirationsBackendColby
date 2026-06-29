# ASPN Platform Developer Handoff Addendum v0.4

Version: v0.4  
Date: June 11, 2026  
Status: Current technical handoff addendum

## Purpose

This addendum updates the technical handoff after completion of the youth MVP, staff/admin MVP, Firebase staging validation, and Legacy Endpoint Security Fix.

Future developers should read this alongside:

- ASPN Platform AGENTS.md
- ASPN Platform Build Manual
- ASPN Platform Developer Handoff Guide
- ASPN September MVP Functional Specification v1.0
- ASPN Platform Release Notes v0.4
- ASPN Platform Security Review Addendum v0.4

## Current Architecture

Backend:

- Spring Boot
- Firebase Admin SDK
- Firebase ID token verification
- Firestore
- Gradle

Frontend:

- Vite
- React
- Firebase Web Authentication
- JavaScript
- Environment-based configuration

Database:

- Firestore active MVP database: (default)
- Legacy/prototype database: aspirationnetworkusers

## Active MVP Collections

Use the (default) Firestore database for active MVP data:

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

Legacy discussion endpoints are disabled and discussion is not part of the pilot MVP.

## Authentication and Authorization

Youth-owned routes require:

- Authorization: Bearer <Firebase ID token>

Staff/admin routes require:

- Authorization: Bearer <Firebase ID token>
- Firestore profile role of `staff` or `admin`

Reserved roles that must not receive staff/admin access unless explicitly added later:

- educator
- partner
- government

Backend role enforcement remains the source of truth.

## Completed Youth MVP

Youth-facing screens:

- Login
- Create Account
- Profile
- Youth Dashboard
- Programs
- Credentials
- Service Hours
- RWD Learning Center
- Notifications

Youth can:

- Create a Firebase Auth account
- Complete a private ASPN profile
- View dashboard data
- Enroll in active programs
- View earned and available credentials
- View service-hour records and request link
- View RWD activities and update progress
- View and mark notifications read

## Completed Staff/Admin MVP

Staff/admin screens:

- Staff Dashboard / Metrics
- Youth Management
- Program Management
- Credential Management
- Service Hour Management
- RWD Management

Staff/admin can:

- View platform metrics
- List youth users
- Review youth profile status
- Update limited youth review fields
- Create/update/archive active programs
- Review/remove enrollments
- Create credential definitions
- Manually award credentials
- Create/review service-hour records by youth UID
- Set the service-hour request URL
- Create/update/deactivate active RWD activities

## Deferred Staff/Admin Work

- Attendance Management UI
- Credential definition catalog/list/search
- Earned credential reporting/search
- Service-hour all-pending queue
- Archived program listing/recovery
- Inactive RWD activity listing/recovery
- Exports and grant reporting
- Advanced metrics

## Legacy Endpoint Security Status

Checkpoint 21L disabled the legacy public routes with HTTP 410 Gone.

Disabled public profile/user routes:

- POST /api/createProfile
- GET /api/getUser/{id}
- GET /api/getUserWithCredentials/{id}

Disabled discussion/post routes:

- GET /api/getallpost
- POST /api/createPost
- POST /api/creatPost
- POST /api/createComment
- GET /api/getCommentForPost/{postID}
- POST /api/upVote/{postID}
- DELETE /api/deletePost/{postID}

Do not re-enable discussion or public profile behavior without a separate safety and moderation design.

## Local Development

Backend:

```bash
cd UserData
./gradlew bootRun
```

Backend tests:

```bash
cd UserData
./gradlew clean test
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend build:

```bash
cd frontend
npm run build
```

## Environment Notes

Backend Firebase Admin credentials use Google Application Default Credentials.

Do not commit:

- service account JSON files
- `.env`
- Firebase private keys
- local Gradle caches

Frontend Firebase config is read from `.env` variables and is ignored by git.

## Current Pilot Recommendation

The platform is ready for:

- Internal demo
- Controlled 60-100 youth Fall 2026 pilot with staff oversight

The platform is not ready for:

- Unrestricted public launch
- Public youth discovery
- Direct messaging
- Peer networking
- Production-scale reporting

