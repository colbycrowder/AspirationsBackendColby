# ASPN Platform Developer Handoff Addendum v0.5

## Executive Summary

This addendum updates the developer handoff after completion of Checkpoints 25A through 26C. The ASPN Platform is now pilot-capable and includes staff-facing stakeholder management, relationship reporting, pilot readiness, pilot metrics, and pilot evaluation dashboards.

Platform stage: **Pilot-Capable**  
Backend: **Spring Boot**  
Frontend: **React + Vite**  
Database: **Firestore**  
Authentication: **Firebase Authentication**  
Authorization: **`AuthService.requireStaff(...)` for staff/admin routes**

## Architecture Status

The architecture remains unchanged:
- Spring Boot backend
- Firebase Admin SDK
- Firestore
- React/Vite frontend
- Firebase Auth client login
- Staff routes under `/staff/*`

No new framework was introduced in v0.5.

## Collection Inventory

Active Firestore database:
- `(default)`

Do not use the separate legacy Firestore database named:
- `aspirationnetworkusers`

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

Operational and research collections:
- `platformEvents`
- `staffOperationEvents`
- `externalDatasets`
- `participantExternalLinks`

Stakeholder collections:
- `educators`
- `partnerOrganizations`
- `governmentOrganizations`
- `stakeholderRelationshipNotes`

## Route Inventory Added Since v0.4

### Educator Routes

- `GET /api/staff/educators`
- `GET /api/staff/educators/{educatorId}`
- `POST /api/staff/educators`
- `PATCH /api/staff/educators/{educatorId}`
- `PATCH /api/staff/educators/{educatorId}/activate`
- `PATCH /api/staff/educators/{educatorId}/deactivate`
- `GET /api/staff/educators/totals`

### Partner Organization Routes

- `GET /api/staff/partners`
- `GET /api/staff/partners/{partnerOrganizationId}`
- `POST /api/staff/partners`
- `PATCH /api/staff/partners/{partnerOrganizationId}`
- `PATCH /api/staff/partners/{partnerOrganizationId}/activate`
- `PATCH /api/staff/partners/{partnerOrganizationId}/deactivate`
- `GET /api/staff/partners/totals`

### Government Organization Routes

- `GET /api/staff/government-organizations`
- `GET /api/staff/government-organizations/{governmentOrganizationId}`
- `POST /api/staff/government-organizations`
- `PATCH /api/staff/government-organizations/{governmentOrganizationId}`
- `PATCH /api/staff/government-organizations/{governmentOrganizationId}/activate`
- `PATCH /api/staff/government-organizations/{governmentOrganizationId}/deactivate`
- `GET /api/staff/government-organizations/totals`

### Relationship Note Routes

- `GET /api/staff/stakeholders/notes`
- `GET /api/staff/stakeholders/notes/{stakeholderRelationshipNoteId}`
- `POST /api/staff/stakeholders/notes`
- `PATCH /api/staff/stakeholders/notes/{stakeholderRelationshipNoteId}`
- `DELETE /api/staff/stakeholders/notes/{stakeholderRelationshipNoteId}`
- `GET /api/staff/stakeholders/notes/totals`

### Pilot Operations Routes

- `GET /api/staff/pilot/readiness`
- `GET /api/staff/pilot/metrics`
- `GET /api/staff/pilot/evaluation`

## Frontend Route Inventory Added Since v0.4

- `/staff/educators`
- `/staff/partners`
- `/staff/government`
- `/staff/relationships`
- `/staff/pilot-readiness`
- `/staff/pilot-metrics`
- `/staff/pilot-evaluation`

## Current Operational State

Staff can now manage:
- users
- youth profiles
- programs
- enrollments
- credentials
- attendance
- service hours
- RWD activities
- educators
- partner organizations
- government organizations
- relationship notes
- metrics
- reporting
- readiness
- pilot metrics
- pilot evaluation

Youth users can:
- create accounts
- complete private profiles
- view dashboard
- enroll in active programs
- view credentials
- view service-hour information
- access RWD learning center
- view notifications

## Authorization Model

All staff/admin endpoints require:
- Firebase ID token
- backend verification
- `AuthService.requireStaff(...)`

Allowed roles:
- `staff`
- `admin`

Reserved or denied roles:
- `member`
- `youth`
- `educator`
- `partner`
- `government`

Reserved roles must not be granted staff access unless explicitly approved in a future checkpoint.

## Known Limitations

Current known limitations:
- no mobile app
- no public profiles
- no public youth directory
- no youth messaging
- no social graph
- no matching engine
- no external CRM integration
- no educator portal
- no partner portal
- no government portal
- no export system
- no charting libraries
- no full UX/UI refresh
- limited mobile optimization
- production readiness review pending

## Recommended Next Development Areas

1. Documentation v0.5
2. Production Readiness Review
3. Staff pilot QA walkthrough
4. Mobile optimization
5. UX/UI refresh
6. Design system
7. Credential taxonomy refinement
8. Export/reporting layer
9. Workforce matching roadmap
10. Future Ready Initiative integration planning

## Developer Warnings

- Do not use the legacy Firestore database named `aspirationnetworkusers` for active pilot operations.
- Do not re-enable disabled legacy public endpoints without a security review.
- Do not introduce public youth discovery, direct messaging, follower systems, or social media mechanics.
- Do not grant educator, partner, or government roles staff access by assumption.
- Do not commit Firebase service account credentials or environment secrets.

