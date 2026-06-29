# ASPN Platform v1.0.0-rc1

## 1. Release Information

Release Name: ASPN Platform v1.0.0-rc1  
Release Date: June 26, 2026  
Status: Release Candidate  
Target Pilot: September 2026

## 2. Executive Summary

ASPN Platform v1.0.0-rc1 is the first official Release Candidate for the ASPN Youth Development Operating System.

The MVP feature set is complete and frozen for pilot readiness. The platform now supports youth onboarding, private youth profiles, program participation, credential discovery and awards, attendance records, service-hour records, learning activities, staff operations, reporting, and pilot readiness review.

The pilot objective is to support a controlled September 2026 youth onboarding pilot with enough operational infrastructure for ASPN staff to manage participants, programs, credentials, attendance, service, and reporting without direct Firestore editing.

Intended users:

- Youth participants ages 8–24
- ASPN staff
- educators
- community partners
- government partners
- researchers and evaluation stakeholders

Major platform capabilities include:

- Firebase-authenticated youth and staff access
- private youth profile and journey record
- staff-managed program and credential operations
- staff-created attendance and service-hour records
- dashboard-backed youth Home, My Journey, Profile, and Credential Explorer synchronization
- duplicate/stale profile detection and read-only review tools
- pilot reporting and operational dashboards
- responsive youth mobile navigation

Remaining work before production is operational rather than feature-oriented:

- execute the production smoke test
- verify live seed data
- confirm staff/youth test accounts
- confirm production environment variables and Firebase service-account configuration
- complete final pilot Go/No-Go sign-off

## 3. Included Features

### Authentication

Firebase Auth supports account creation, login, protected routes, staff authorization gates, and youth/staff navigation separation.

### Youth Profiles

Youth can complete and update a private ASPN profile with identity, school, graduation, interests, profile status, ASPN Participant ID display, and earned credential portfolio context.

### Staff Portal

Staff have protected access to operational dashboards, user management, youth management, program management, credential management, attendance, service hours, stakeholder records, and reporting.

### Program Management

Staff can create, update, archive, restore, filter, and review programs. Program detail includes operational context and read-only program rosters.

### Credential Framework

Staff can manage credential definitions and award credentials using youth-friendly identifiers. The system preserves backend credential IDs while reducing pilot staff reliance on raw UUIDs.

### Credential Explorer

Youth can view the official ASPN credential ecosystem, including six core credentials and four advanced credentials, with earned state, icons, pathway context, and related programs.

### Credential Detail Pages

Youth can open dedicated credential detail pages showing name, category, earned status, award date, description, requirements, related pathways, related programs, role families, and progress guidance.

### Attendance Management

Staff can create attendance records through the working pilot-safe attendance creation flow. Attendance supports program, youth, event/session, date, and status tracking.

### Attendance Log

Youth attendance appears in My Journey as a separate Attendance Log, not as a journey milestone, preserving the distinction between participation records and developmental milestones.

### Service Hours

Staff can create and review service-hour records. Youth can view service-hour records and request-link availability through the youth Service Hours page.

### Student Record

Staff Youth Management includes a Student Record panel that shows credentials, attendance, and service hours from the same dashboard-backed source used by youth-facing views.

### Program Rosters

Program Management includes read-only program roster visibility so staff can confirm enrolled youth by name, email, ASPN Participant ID, and profile status where available.

### Duplicate Profile Detection

Staff Youth Management warns when a selected youth may share an email or ASPN Participant ID with another youth profile.

### Duplicate Review Queue

Staff Youth Management includes a read-only Duplicate Profile Review queue grouped by matching ASPN Participant ID or normalized email.

### Journey Timeline

My Journey presents credentials, programs, learning, service, pathway context, and youth growth narrative through a developmental journey structure.

### Global Civic Movements

Youth can view Global Civic Movements learning activities, access external resources, and save learning progress where activities are configured.

### Notifications

Youth can view notifications, unread states, and mark notifications as read where notification records exist.

### Reporting

Staff can access reporting destinations for participation, operational activity, platform metrics, pilot metrics, pilot evaluation, and pilot readiness.

### Pilot Metrics

Pilot Metrics summarizes pilot participation, engagement, credential, service, stakeholder, and operations data for staff review.

### Platform Metrics

Platform Metrics provides aggregate counts for youth, programs, enrollments, credentials, attendance, service hours, learning completion, and unread notifications.

### Operations Reporting

Operations Reporting summarizes staff operational activity and platform events where records exist.

### Responsive Mobile Navigation

Youth-facing mobile navigation supports Home, Journey, Programs, Learning, and Profile, with redundant route headers removed from youth pages.

## 4. Major Improvements Since Prototype

Major engineering milestones since prototype include:

- Identity resolution for staff workflows using Firebase UID, ASPN Participant ID, and email where supported.
- Dashboard synchronization across Home, My Journey, Credential Explorer, Profile, Service Hours, and Staff Student Record.
- ASPN Participant ID support for staff credential and attendance workflows.
- Roster-informed attendance UX, later simplified to the stable pilot-safe attendance creation path.
- Student Record synchronization for credentials, attendance, and service hours.
- Credential ecosystem integration with official credential names, categories, icons, pathways, related programs, and role families.
- Credential detail pages that explain requirements and pathway meaning.
- My Journey redesign with credential context, pathway context, and separate Attendance Log.
- Responsive mobile UX with youth bottom navigation and cleaner youth page headers.
- Duplicate profile safeguards, including selected-youth warnings, grouped review queue, and canonical profile decision runbook.
- Production-readiness documentation, including workflow validation and smoke-test checklist.

## 5. Known Issues

Known legitimate remaining issues:

- Existing Vite bundle-size warning appears during frontend production build. This is not currently blocking the pilot but should be revisited post-pilot with code splitting or bundle tuning.
- Live production smoke test is still pending.
- Live seed data validation is still pending.
- Duplicate profile merge/archive workflow does not exist. This is intentional for pilot safety; current duplicate tooling is read-only.
- Credential, attendance, service-hour, and reporting workflows require final live staff/youth smoke testing in the production-like environment.
- Reporting dashboards require staff orientation so users understand which dashboard answers which operational question.
- Notifications must be confirmed in the target environment after a live credential award.

## 6. Breaking Changes

None expected for pilot.

No Firestore schema migration, authentication change, role-permission change, or workflow-breaking behavior is expected as part of this Release Candidate.

## 7. Deployment Requirements

Production deployment requires verification of:

- Firebase project selection
- Firestore availability and expected collections
- Firebase Auth configuration
- Firebase Hosting or selected frontend hosting target
- Firebase Storage if used by deployment assets
- frontend environment variables, including production API URL
- backend environment variables
- `GOOGLE_APPLICATION_CREDENTIALS` for backend Firebase Admin access
- frontend production build
- backend test suite
- CORS configuration between deployed frontend and backend
- staff/admin test account
- youth test account
- required pilot seed data

Reference deployment checklist:

- `docs/Production-Deployment-Smoke-Test-v1.md`

## 8. Validation Summary

Backend validation:

- `./gradlew test` passed for this Release Candidate checkpoint.

Frontend validation:

- `npm run build` passed for this Release Candidate checkpoint.
- Existing Vite chunk-size warning remains and is non-blocking for pilot.

Workflow validation:

- `docs/Pilot-Workflow-Validation-v1.md` documents youth, staff, and cross-system workflow validation.
- No blocking workflow defects were identified.
- Final live smoke test remains required before production deployment.

Pilot readiness:

- MVP is ready for controlled production smoke testing.
- Production pilot launch should wait until smoke test and seed data validation pass.

## 9. Release Recommendation

Recommendation:

Promote ASPN Platform v1.0.0-rc1 to Version 1.0 after:

1. Production Smoke Test passes.
2. Seed Data Validation passes.
3. Final Pilot Go/No-Go is approved.

Until those steps pass, this release should remain a Release Candidate.

## 10. Change Log

### Credential Ecosystem

- Credential Explorer refreshed around official ASPN credentials.
- Credential icon integration completed.
- Credential detail pages added.
- Staff credential award workflow improved with ASPN Participant ID support and credential dropdown selection.
- Youth Profile, Home, My Journey, and Credential Explorer now reflect earned credentials consistently.

### Attendance Redesign

- Attendance moved out of Journey Timeline and into a separate Attendance Log.
- Staff attendance creation stabilized around the working pilot-safe record creation path.
- Attendance records sync to youth My Journey and Staff Student Record.

### Identity Stabilization

- User model aligned with active Firestore fields.
- Staff Student Record lookup supports Firebase UID, ASPN Participant ID, and email.
- Dashboard-backed source-of-truth behavior preserved.
- Duplicate/stale identity fallback preserved.

### Pilot Readiness

- Accessibility and device QA completed.
- Pilot content and operational validation documented.
- Reporting and staff workflow clarity reviewed.
- Youth navigation and header consistency improved.
- Error and empty-state handling audited.

### Production Readiness

- Production deployment smoke-test checklist created.
- Release Candidate metadata created.
- Backend tests and frontend build pass.
- Known production blockers are operational smoke-test items, not known code defects.

## 11. Future Roadmap (Post-Pilot)

The following items are explicitly post-pilot and are not included in v1.0.0-rc1:

- Opportunity Matching
- Government hiring integration
- Advanced analytics
- Educator dashboards
- Messaging
- Public youth profiles
- Municipal workforce pathways
- Credential automation and rule simulation
- AI recommendations
- Duplicate profile merge/archive workflow
- Deeper notification automation
- Native youth service-hour submission
- Advanced staff task queues
- Expanded mobile/tablet refinement
- Data export governance and research workflows

Public youth profiles, messaging, AI recommendations, and hiring integrations should receive separate privacy, safety, permissions, and governance reviews before implementation.

## 12. Version Tag Recommendation

Recommended Git tag:

```text
v1.0.0-rc1
```

Recommended next version:

```text
v1.0.0
```

Promote to `v1.0.0` only after successful production smoke test, seed data validation, and final pilot Go/No-Go approval.
