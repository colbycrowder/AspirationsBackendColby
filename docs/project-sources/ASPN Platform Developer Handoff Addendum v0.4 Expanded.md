# ASPN Platform Developer Handoff Addendum v0.4 Expanded

Version: v0.4 Expanded  
Date: June 11, 2026  
Status: Current Expanded Developer Handoff  
Audience: Future developers with no prior ASPN context

## 1. Executive Summary

The ASPN Platform is a youth development, credentialing, civic engagement, service-hour, RWD learning, and opportunity platform. It is not a social media platform.

As of v0.4, the youth MVP is complete, the first staff/admin MVP is complete, Firebase staging validation has passed locally, and legacy public endpoints have been disabled.

The platform is conditionally ready for a controlled 60-100 youth Fall 2026 pilot. It is not ready for broad public launch.

The platform uses:

- React + Vite frontend
- Firebase Web Authentication
- Spring Boot backend
- Firebase Admin SDK
- Firestore

The active MVP Firestore database is `(default)`.

The database named `aspirationnetworkusers` is legacy/prototype and must not be used for active MVP operations.

## 2. Architecture Snapshot

Frontend:

- Location: `frontend`
- Framework: React
- Build tool: Vite
- Language: JavaScript
- Auth: Firebase Web SDK
- API calls: `frontend/src/api.js`
- Route shell: `frontend/src/App.jsx`

Backend:

- Location: `UserData`
- Framework: Spring Boot
- Build: Gradle
- Firebase Admin: token verification and Firestore access
- Controller: `UserInfoController`
- Services: one service per major domain

Database:

- Firestore `(default)`
- Documents organized by collection
- User profile document ID equals Firebase UID for active MVP users

## 3. Repository Structure

Top-level relevant structure:

```text
AspirationsBackendColby/
  UserData/
    build.gradle
    gradlew
    settings.gradle
    src/main/java/com/AspirationsNetwork/UserData/
      Controller/
      DTO/
      Models/
      Service/
      config/
    src/test/java/com/AspirationsNetwork/UserData/
  frontend/
    package.json
    src/
      App.jsx
      api.js
      auth/
      components/
      config.js
      firebase.js
      styles.css
  docs/
    project-sources/
```

## 4. Frontend Architecture

### React

The frontend is a React single-page app.

Major pages are implemented as components under:

```text
frontend/src/components
```

Youth components include:

- YouthDashboard
- ProgramsPage
- CredentialsPage
- ServiceHoursPage
- RwdLearningCenterPage
- NotificationsPage
- ProfileCompletionPage

Staff/admin components include:

- StaffMetricsDashboard
- StaffYouthManagementPage
- StaffProgramManagementPage
- StaffCredentialManagementPage
- StaffServiceHourManagementPage
- StaffRwdManagementPage

### Vite

Vite is used for local development and production builds.

Common commands:

```bash
cd frontend
npm install
npm run dev
npm run build
```

### Routing

Routing is implemented manually in `App.jsx` through path state and `window.history.pushState`.

There is no external router library currently.

Youth routes include:

- `/login`
- `/create-account`
- `/dashboard`
- `/profile`
- `/programs`
- `/credentials`
- `/rwd-learning-center`
- `/notifications`
- `/attendance`
- `/service-hours`

Staff/admin routes include:

- `/staff`
- `/staff/youth-management`
- `/staff/program-management`
- `/staff/credential-management`
- `/staff/rwd-management`
- `/staff/attendance-management`
- `/staff/service-hour-management`
- `/staff/metrics`

Attendance route remains a placeholder.

### Authentication

Authentication state is managed through:

```text
frontend/src/auth/AuthContext.jsx
```

Firebase config is initialized through:

```text
frontend/src/firebase.js
frontend/src/config.js
```

Firebase values come from `.env` and must not be hardcoded in components.

### API Layer

All frontend API helpers live in:

```text
frontend/src/api.js
```

Youth API helpers retrieve the signed-in Firebase user's ID token and call protected `/api/me/*` routes.

Staff API helpers call protected `/api/staff/*` routes and rely on backend authorization.

## 5. Backend Architecture

### Spring Boot

The backend is a Spring Boot application under `UserData`.

Main controller:

```text
UserInfoController.java
```

### Firebase Admin

Firebase Admin is configured in:

```text
FireBaseConfig.java
```

It uses:

```java
GoogleCredentials.getApplicationDefault()
```

Local backend execution requires Google Application Default Credentials or `GOOGLE_APPLICATION_CREDENTIALS`.

### Firestore

Firestore is injected into services and used directly for collection reads/writes.

Active database:

```text
(default)
```

## 6. Service Layer Inventory

### AuthService

Responsibilities:

- verifies Firebase ID tokens
- returns authenticated UID
- enforces staff/admin role through Firestore user profile lookup

Allows staff/admin roles:

- staff
- admin

Does not allow reserved future roles.

### DashboardService

Builds the youth dashboard response.

Aggregates:

- profile summary
- enrolled active programs
- earned credentials
- available credentials
- attendance records
- service-hour records
- RWD Learning Center items
- service-hour request URL
- unread notification count
- opportunities placeholder/link

### ProgramService

Handles:

- program creation
- program update
- active program listing
- active program retrieval

Collection:

- programs

### ProgramEnrollmentService

Handles:

- youth self-enrollment
- duplicate active enrollment prevention
- enrollment listing for staff
- enrollment removal

Collection:

- programEnrollments

### CredentialService

Handles:

- credential definition creation
- manual credential award
- earned credential display
- available credential display
- attendance-count auto-award foundation
- RWD linked credential award foundation

Collections:

- credentialDefinitions
- earnedCredentials

### AttendanceService

Handles:

- attendance record creation
- attendance retrieval for a user
- attendance-count credential auto-award trigger

Collection:

- attendanceRecords

Frontend Attendance Management UI is not yet built.

### ServiceHourService

Handles:

- service-hour record creation/review
- user-specific service-hour record retrieval

Collection:

- serviceHourRecords

### NotificationService

Handles:

- credential-earned notifications
- user notification retrieval
- mark-as-read behavior
- unread count support

Collection:

- notifications

### RwdLearningService

Handles:

- RWD activity creation
- RWD activity update
- active RWD activity listing
- youth progress retrieval
- youth progress save
- 80 percent pass threshold
- linked credential award when configured

Collections:

- rwdActivities
- rwdProgress

### MetricsService

Builds aggregate platform metrics for staff/admin.

### UserInfoService

Handles:

- user lookup
- youth profile completion
- staff youth user listing
- limited staff-managed youth update fields

Collection:

- aspirationnetworkusers

## 7. Controller Inventory

Primary controller:

```text
UserInfoController
```

Route groups:

- legacy disabled routes
- protected youth profile routes
- protected youth dashboard route
- protected youth notification routes
- public active program routes
- protected youth enrollment route
- public active RWD activity route
- protected youth RWD progress routes
- protected staff/admin routes

## 8. DTO Inventory

Important DTO groups:

Youth:

- YouthProfileCompletionDTO
- YouthSelfServiceProfileDTO
- YouthDashboardDTO
- ProgramEnrollmentDTO
- RwdProgressDTO

Staff/admin:

- StaffUserUpdateDTO
- ProgramDTO
- CredentialDefinitionCreationDTO
- AwardCredentialDTO
- AttendanceRecordCreationDTO
- ServiceHourRecordDTO
- ServiceHourRequestUrlDTO
- RwdActivityDTO
- PlatformMetricsDTO

Display:

- AvailableCredentialDTO
- EarnedCredentialDisplayDTO
- RwdLearningCenterItemDTO

Legacy:

- UserProfileCreationDTO remains in code but its public endpoint is disabled.

## 9. Firestore Collection Inventory

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

Legacy/prototype:

- aspirationnetworkposts
- comments

## 10. Endpoint Inventory

### Protected Youth Endpoints

- GET /api/me/profile
- PATCH /api/me/profile
- GET /api/me/dashboard
- POST /api/me/program-enrollments
- GET /api/me/rwd-progress
- POST /api/me/rwd-progress
- GET /api/me/notifications
- PATCH /api/me/notifications/{notificationId}/read

### Public Catalog Endpoints

- GET /api/programs
- GET /api/programs/{programId}
- GET /api/rwd/activities

These expose active non-private catalog data.

### Protected Staff/Admin Endpoints

- GET /api/staff/metrics
- GET /api/staff/users/youth
- GET /api/staff/users/youth/{id}
- PATCH /api/staff/users/youth/{id}
- POST /api/staff/programs
- PATCH /api/staff/programs/{programId}
- GET /api/staff/program-enrollments
- GET /api/staff/program-enrollments/program/{programId}
- GET /api/staff/program-enrollments/user/{userUID}
- PATCH /api/staff/program-enrollments/{enrollmentId}/remove
- POST /api/staff/credentials/definitions
- POST /api/staff/credentials/award
- POST /api/staff/service-hours
- GET /api/staff/service-hours/user/{userUID}
- PATCH /api/staff/settings/service-hour-request-url
- POST /api/staff/rwd/activities
- PATCH /api/staff/rwd/activities/{rwdActivityId}
- POST /api/staff/attendance
- GET /api/staff/attendance/user/{userUID}

### Disabled Legacy Endpoints

Return HTTP 410 Gone:

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

## 11. Youth Workflow Architecture

Youth workflows begin with Firebase Authentication.

After sign-in:

1. Frontend retrieves Firebase ID token.
2. Frontend sends token to backend.
3. Backend verifies token.
4. Backend uses verified UID.
5. Firestore profile and related records are loaded.

Youth-owned workflows do not trust client-supplied UID.

## 12. Staff/Admin Workflow Architecture

Staff/admin workflows begin with Firebase Authentication and then backend role validation.

1. Staff user signs in.
2. Frontend calls a staff endpoint with token.
3. Backend verifies token.
4. Backend loads user profile by token UID.
5. Backend allows only role `staff` or `admin`.

The frontend staff gate calls the metrics endpoint as a practical authorization check, but the backend remains authoritative.

## 13. Authentication Flow

Frontend:

- Firebase Web SDK creates/maintains session.
- User object provides `getIdToken()`.
- API helper sends bearer token.

Backend:

- `AuthService.requireAuthenticatedUserUid` verifies token and returns UID.
- `AuthService.requireStaff` verifies token, loads Firestore profile, and checks role.

## 14. Authorization Flow

Youth:

- user can access own data only through token UID

Staff/admin:

- user must have Firestore profile role `staff` or `admin`

Reserved roles:

- educator
- partner
- government

These are intentionally not authorized for staff/admin routes.

## 15. Current Security Model

Security is designed around:

- Firebase identity
- backend token verification
- private profiles
- role-based staff access
- disabled unsafe legacy routes
- no public youth discovery
- no direct messaging
- no follower systems

## 16. Legacy Endpoint Retirement Summary

Legacy public profile and discussion routes were disabled in Checkpoint 21L.

Reason:

- unauthenticated public profile creation
- client-supplied UID risk
- public youth profile exposure risk
- public credential data exposure risk
- public discussion mutation risk

Replacement:

- use `/api/me/profile` for youth profile completion
- use `/api/me/dashboard` for youth profile/dashboard data
- use `/api/staff/*` for staff/admin operations

## 17. Technical Debt Inventory

- Monolithic controller contains many route groups.
- Some legacy services remain unused after endpoint disablement.
- Some staff screens require manual IDs.
- No external router library in frontend.
- Archived/inactive records are not fully manageable.
- Full reporting/export layer does not exist.
- Mobile navigation is functional but not polished.

## 18. Known Backend Gaps

- Staff credential definition listing
- Staff earned credential listing
- Service-hour all-pending queue
- All-program listing including archived programs
- All-RWD activity listing including inactive activities
- Attendance all-record/session management
- Advanced metrics
- Export/reporting endpoints

## 19. Known Frontend Gaps

- Attendance Management UI
- Staff credential catalog UI
- Staff service-hour queue UI
- Archived/inactive management UI
- Mobile navigation polish
- Accessibility polish
- Browser-specific QA fixes if discovered

## 20. Pilot Support Notes

Pilot staff should know:

- role setup still requires Firebase/Firestore admin access
- staff screens cover common workflows but not all reporting needs
- manual IDs may be needed for some credential/service-hour workflows
- all active MVP work must happen in `(default)`
- legacy database must not be used

## 21. Future Development Roadmap

Recommended sequence:

1. Mobile/browser QA fixes
2. Staff operating guide
3. Attendance Management UI if pilot requires it
4. Staff credential catalog
5. Service-hour pending queue
6. Archived/inactive management
7. Reporting and exports
8. Accessibility audit
9. Production launch readiness
10. Partner/educator/government role expansion
11. Workforce matching
12. Government pathway matching
13. Scholarship matching
14. Public portfolios with safety model

## 22. Developer Warnings

Active database:

- Use `(default)`.

Legacy database:

- Do not use or delete `aspirationnetworkusers` database without explicit approval.

Security constraints:

- Do not trust client-supplied UID for youth-owned actions.
- Do not grant reserved roles staff access.
- Do not re-enable legacy public endpoints.

Youth safety constraints:

- No public youth directory.
- No direct messaging.
- No follower/friend systems.
- No popularity features.
- Keep profiles private by default.

