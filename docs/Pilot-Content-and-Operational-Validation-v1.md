# ASPN Platform Pilot Content and Operational Validation

**Version:** Checkpoint 31F  
**Date:** June 23, 2026  
**Scope:** Pilot content readiness, youth/staff operational test flows, credential/program/learning/attendance/service/notification/reporting validation, and launch go/no-go conditions.

---

# 1. Executive Summary

The ASPN Platform is **Ready with Setup** for controlled pilot onboarding. The application has the necessary frontend and backend workflows to support youth onboarding, staff operations, credential awards, enrollment, learning progress, service-hour review, notifications, and reporting. However, the live pilot environment still must be validated with approved seed content and real staff/youth test accounts before onboarding youth.

This checkpoint did not seed Firestore data or change backend behavior. The review found that:

- The frontend contains the official six core credentials and four advanced credentials in the ecosystem registry.
- All ten youth-facing credential icon assets are present in the frontend bundle.
- The backend/staff UI supports creating programs, credential definitions, Global Civic Movements activities, attendance records, service-hour records, and the service-hour request URL.
- Pilot Readiness already checks for active programs, active credential definitions, youth profiles, attendance/service records, platform events, and stakeholder relationships.
- The actual live environment content status must still be verified manually through the staff UI or protected staff APIs.

**Go/No-Go recommendation:** **No-Go for real youth onboarding until required seed content is confirmed and the manual test flows in this document pass.** Once those setup items pass, the platform can proceed to a controlled pilot with staff training and youth orientation.

---

# 2. Content Readiness Status

| Item | Status | Evidence / Validation Method | Required Before Pilot |
|---|---|---|---|
| At least one active program | Needs Manual Testing | Staff Program Management and Pilot Readiness can verify active programs. No local seed file was found. | Yes |
| Six core credential definitions | Ready with Setup | Frontend registry contains all six official core credentials; live Firestore credential definitions must be created/verified. | Yes |
| Four advanced credential definitions | Ready with Setup | Frontend registry contains all four official advanced credentials; live Firestore credential definitions must be created/verified. | Recommended for full pilot credential catalog |
| At least one Global Civic Movements activity | Needs Manual Testing | Staff RWD Management supports activity creation; youth Learning reads active activities from backend. No local seed file was found. | Yes |
| Service-hour request URL configured | Needs Manual Testing | Backend supports `/staff/settings/service-hour-request-url`; youth Service Hours has honest unavailable state if absent. | Recommended before onboarding |
| Youth-facing credential icon assets | Ready | All ten credential image files are present in `frontend/src/assets/credentials/`. | Yes |
| Empty states acceptable if content is missing | Ready with Setup | Youth pages use honest empty states, but repeated empty states are a pilot confidence risk. | Seed at least minimal content |
| Pilot reporting dashboards | Needs Manual Testing | Staff dashboards exist and load from protected endpoints; live data must be checked. | Yes |

## Frontend registry credentials present

Core credentials:

1. Civic Research
2. Data & Evaluation Basics
3. Public Communication
4. Community Engagement
5. Project Implementation
6. Civic Administration

Advanced credentials:

1. Regulatory Literacy
2. Digital Civic Operations
3. Grants & Resource Development
4. Public-Sector Career Readiness

## Credential icon assets present

The following files are present:

- `CIVIC ADMINISTRATION.png`
- `CIVIC RESEARCH.png`
- `COMMUNITY ENGAGEMENT.png`
- `DATA & EVALUATION BASICS.png`
- `DIGITAL CIVIC OPERATIONS.png`
- `GRANTS & RESOURCE DEVELOPMENT.png`
- `PROJECT IMPLEMENTATION.png`
- `PUBLIC COMMUNICATION.png`
- `PUBLIC-SECTOR CAREER READINESS.png`
- `REGULATORY LITERACY.png`

---

# 3. Required Seed Content

The live Firestore pilot environment should be seeded through the existing staff UI/API, not by direct schema changes.

| Seed Item | Minimum Required | Status | Notes |
|---|---:|---|---|
| Active program | 1 | Needs Manual Testing | Should include youth-facing name, description, category, leader, and active status. |
| Core credential definitions | 6 | Ready with Setup | Use official credential names. Link relevant program IDs where known. |
| Advanced credential definitions | 4 | Ready with Setup | Recommended so Credential Explorer and staff definitions align for the full catalog. |
| Global Civic Movements activity | 1 | Needs Manual Testing | Must include title, country/name context, description, external URL, active status, and optional credential definition ID. |
| Service-hour request URL | 1 | Needs Manual Testing | If not available, youth page honestly says the link is not configured. |
| Staff account | 1–5 pilot staff | Needs Manual Testing | Must have staff/admin role in existing role model. |
| Youth test account | At least 1 | Needs Manual Testing | Use a non-real pilot test youth before onboarding real participants. |
| Attendance record | At least 1 test record | Needs Manual Testing | Needed to validate attendance reporting and My Journey attendance milestones where supported. |
| Service-hour records | Pending, verified, rejected examples | Needs Manual Testing | Needed to validate staff review and youth summaries. |
| Notification example | At least credential-earned notification | Needs Manual Testing | Manual credential award should create notification if backend notification flow succeeds. |
| Platform events | At least one recent event | Needs Manual Testing | Dashboard views, learning views, and credential awards can generate events. |

Seed data should avoid real youth information until staff have completed privacy and workflow training.

---

# 4. Youth Test Flow

Status: **Needs Manual Testing**

Use a new test youth account before real onboarding.

1. Create Account
   - Expected: account creation routes to Profile Setup, not a Home error.
   - Pass condition: youth can reach Profile without staff intervention.

2. Profile Setup
   - Complete first name, last name, email, school, graduation year, and optional interests.
   - Expected: profile saves and youth can continue to Home.
   - Pass condition: Home loads with profile identity and no missing-profile blocker.

3. Home
   - Expected: Home shows profile card, progress cards, Continue Your Journey, honest empty states, and any seeded programs/learning/service data.
   - Pass condition: no dead-end errors; next actions are understandable.

4. Enroll in Program
   - Open Programs.
   - Enroll in an active seeded program.
   - Pass condition: enrollment succeeds and duplicate enrollment is blocked.

5. View Learning
   - Open Global Civic Movements.
   - Open external learning link.
   - Mark activity in progress or completed.
   - Pass condition: status updates and no unclear external-link behavior.

6. View Credentials
   - Open Credential Explorer.
   - Expected: ten registry credentials display, earned status appears if a credential was awarded.
   - Pass condition: credential icons render and earned credentials are highlighted.

7. View My Journey
   - Expected: enrolled program, completed learning, earned credentials, verified service milestones, and attendance milestones appear when data exists.
   - Pass condition: Journey updates after each underlying workflow.

8. Notifications
   - If a credential was awarded, open Notifications.
   - Mark unread notification as read.
   - Pass condition: unread count updates and Home next actions reflect the read state.

---

# 5. Staff Test Flow

Status: **Needs Manual Testing**

Use a staff/admin test account with the existing role model.

1. Staff Login
   - Expected: staff access verification succeeds.
   - Pass condition: staff navigation appears and youth-only bottom nav does not.

2. Staff Dashboard
   - Expected: key metrics load.
   - Pass condition: no staff gate failure from metrics endpoint.

3. User Management
   - Search for the test youth.
   - Verify name, email, UID, ASPN Participant ID, profile status, and review flags.
   - Training needed: explain User Management vs Youth Management.

4. Program Management
   - Confirm at least one active program exists.
   - Create or update a test program if the environment is empty.
   - Training needed: archive vs active program behavior.

5. Credential Management
   - Confirm the six core and four advanced credential definitions exist.
   - Award a test credential to the test youth.
   - Training needed: UID confirmation, ASPN Participant ID, credential definition IDs, and 31D confirmation workflow.

6. Attendance Management
   - Create a present attendance record for the test youth and seeded program.
   - Update status and confirm warning language.
   - Training needed: present attendance may trigger credential evaluation if configured.

7. Service Hour Management
   - Create pending, verified, and rejected examples for the test youth.
   - Confirm verified hours appear on youth Home/My Journey.
   - Training needed: verify/reject/delete conventions and youth-summary impact.

8. Reporting
   - Open Staff Dashboard, Platform Metrics, Pilot Readiness, Pilot Metrics, Pilot Evaluation, Reporting, and Operations Reporting.
   - Pass condition: each dashboard loads with understandable values or honest empty states.
   - Training needed: definitions, denominators, data freshness, and dashboard distinctions.

---

# 6. Workflow Validation Checklist

## Credential Workflow

Status: **Needs Manual Testing**

| Step | Expected Result |
|---|---|
| Confirm credential definition exists | Staff Credential Management lists active definition. |
| Confirm frontend registry match | Credential Explorer displays the official credential card and icon. |
| Award credential manually | Staff sees youth/credential confirmation before award. |
| Verify youth Home | Credential count increases after dashboard refresh. |
| Verify Credential Explorer | Awarded credential displays as Earned. |
| Verify My Journey | Credential appears in timeline and pathway context. |
| Verify notification | Credential-earned notification appears if backend notification creation succeeds. |

Notes:

- Credential definitions created in Firestore use generated IDs. To match frontend registry icons and pathway context, definitions should use official credential names exactly.
- Attendance/rwd auto-awards exist in backend foundations, but staff should not rely on them until configured and tested.

## Program Enrollment

Status: **Needs Manual Testing**

| Step | Expected Result |
|---|---|
| Active program exists | Youth Programs page lists the program. |
| Youth enrolls | Enrollment succeeds using verified Firebase identity. |
| Duplicate enrollment attempted | Duplicate is blocked with clear message. |
| Home refresh | Active/enrolled program count reflects enrollment. |
| My Journey refresh | Program appears as a journey participation item. |

## Learning

Status: **Needs Manual Testing**

| Step | Expected Result |
|---|---|
| Active Global Civic Movements activity exists | Youth Learning page lists activity. |
| External link opens | Link opens in new tab with clear label. |
| Mark in progress | Status changes to In Progress. |
| Mark completed | Status changes to Completed. |
| Linked credential behavior | Associated credential ID is visible; staff explains relationship if not youth-friendly. |

## Attendance

Status: **Needs Manual Testing**

| Step | Expected Result |
|---|---|
| Staff creates attendance | Record appears in Attendance Management. |
| Status set to present | Staff sees warning about credential-rule impact. |
| Youth dashboard support | Attendance contributes to Home/My Journey only where dashboard logic supports it. |
| Reporting updates | Attendance totals update in staff reporting. |

## Service Hours

Status: **Needs Manual Testing**

| Step | Expected Result |
|---|---|
| Staff creates service-hour record | Record appears in Service Hour Management. |
| Pending state | Record appears as pending where youth/staff views support it. |
| Verified state | Verified hours appear on youth Home and My Journey summaries. |
| Rejected state | Rejected hours remain distinct and do not count as verified. |
| Service-hour request URL | Link opens if configured; otherwise honest unavailable state appears. |

## Notifications

Status: **Needs Manual Testing**

| Step | Expected Result |
|---|---|
| Credential awarded | Credential-earned notification is created if backend notification flow succeeds. |
| Youth opens Notifications | Notification appears in inbox. |
| Mark read | Notification changes to read. |
| Home unread next action | Home reflects unread count where supported. |

## Reporting

Status: **Needs Manual Testing**

| Dashboard | Expected Result | Staff Explanation Needed |
|---|---|---|
| Staff Dashboard | Summary counts load. | What each shortcut is for. |
| Platform Metrics | Platform-wide counts load. | Difference from Reporting/Pilot Metrics. |
| Pilot Readiness | Readiness score, blockers, warnings, checklist load. | Blockers vs warnings; score threshold. |
| Pilot Metrics | Pilot activity metrics load. | What is descriptive, not causal. |
| Pilot Evaluation | Outcome scores and recommendations load. | Denominators, thresholds, and interpretation. |
| Operations Reporting | Staff-operation events load. | Tracked platform actions vs external/manual changes. |
| Reporting | Participation, retention, credentials, programs, operations load. | Difference from other dashboards. |

---

# 7. Known Operational Gaps

| Gap | Status | Pilot Handling |
|---|---|---|
| Live seed content not confirmed in this repository review | Needs Manual Testing | Staff must verify active programs, credential definitions, learning activity, service-hour URL, and reporting data in the target environment. |
| No automatic seed scripts found | Ready with Setup | Use existing staff UI/API; do not direct-edit schema. |
| Staff workflows still rely on typed IDs | Ready with Setup | 31D safeguards reduce risk; train staff on UID and ASPN Participant ID use. |
| Staff reporting destinations overlap conceptually | Ready with Setup | Explain dashboards before pilot. |
| Global Civic Movements linked credential display can show IDs | Recommended During Pilot | Staff/youth orientation should explain credential connections. |
| Staff mobile is inefficient for routine work | Deferred | Staff should use laptop/tablet for pilot operations. |
| Accessibility physical-device QA remains unrecorded | Needs Manual Testing | Complete device/browser matrix before real onboarding. |
| Service-hour request form may be external | Ready with Setup | Link now announces new-tab behavior; youth orientation should explain external context. |

---

# 8. Staff Training Requirements

Status: **Ready with Setup**

Before pilot onboarding, staff should be trained on:

1. Account and role separation
   - Youth users vs staff users.
   - Staff-only routes and protected backend authorization.

2. Identifier discipline
   - Firebase UID as operational backend identifier.
   - ASPN Participant ID as private/internal participant identifier.
   - Confirming youth identity before awards, attendance, and service records.

3. Program operations
   - Active vs archived program behavior.
   - How youth enrollment appears on Home/My Journey.

4. Credential operations
   - Creating official credential definitions.
   - Matching official credential names to registry display.
   - Manual awards and 31D confirmation flow.
   - Attendance/learning auto-award limitations.

5. Attendance operations
   - Present/absent/excused/pending meaning.
   - Present attendance may trigger credential evaluation if configured.

6. Service-hour operations
   - Pending, verified, rejected, and delete conventions.
   - Verified hours affect youth summaries.

7. Reporting interpretation
   - Staff Dashboard vs Metrics vs Reporting vs Pilot Metrics vs Pilot Evaluation vs Operations Reporting.
   - Data freshness and denominator limitations.
   - Platform operation events do not include every external/manual change.

8. Privacy and device handling
   - Secure devices.
   - Logout expectations.
   - No shared-device staff sessions.
   - Sensitive youth/contact data handling.

---

# 9. Youth Orientation Requirements

Status: **Ready with Setup**

Before or during the first session, youth should receive a short orientation covering:

1. What ASPN is
   - A private youth development operating system.
   - Not social media, not a public profile, not a job board.

2. First login
   - Create account.
   - Complete Profile.
   - Go to Home.

3. Home
   - Progress cards.
   - Continue Your Journey.
   - Honest empty states.

4. Programs
   - How to join an available program.
   - What enrolled means.

5. Global Civic Movements
   - Learning activities may open outside ASPN.
   - Mark in progress/completed only when appropriate.

6. Credentials
   - Credentials are recognition of learning and participation.
   - Credential Explorer shows what exists, not guaranteed eligibility.

7. My Journey
   - Private record of growth, programs, learning, credentials, and service.

8. Service Hours
   - Request link may open externally.
   - Verified hours are what count in summaries.

9. Privacy
   - Profile is private by default.
   - ASPN Participant ID is internal/private, not a public username.

---

# 10. Go/No-Go Recommendation

## Current recommendation

**No-Go for real youth onboarding until required seed content and manual test flows are completed.**

## Conditional Go criteria

Move to **Go for controlled pilot** when all of the following are true:

1. At least one active program exists and appears on the youth Programs page.
2. Six core credential definitions exist and are active.
3. Four advanced credential definitions exist or are formally deferred with pilot sign-off.
4. At least one Global Civic Movements activity exists and appears to youth.
5. Service-hour request URL is configured, or the unavailable state is formally accepted for the pilot.
6. A test youth completes Create Account → Profile Setup → Home without staff intervention.
7. Test youth can enroll in a program and sees it on Home/My Journey.
8. Staff can manually award a credential and youth sees it on Home, Credential Explorer, My Journey, and Notifications where supported.
9. Staff can create attendance and service-hour records; youth summaries update for supported verified/present states.
10. Staff Dashboard, Platform Metrics, Pilot Readiness, Pilot Metrics, Pilot Evaluation, Reporting, and Operations Reporting load.
11. Staff training and youth orientation are completed or scheduled before onboarding.
12. 31E accessibility/device manual tests are completed or formally mitigated.

## Next checkpoint

Recommended next checkpoint: **31G — Reporting and Staff Workflow Clarity**.
