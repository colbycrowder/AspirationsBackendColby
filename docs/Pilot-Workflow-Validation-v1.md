# Pilot Workflow Validation v1

Date: June 26, 2026  
Checkpoint: 37B — End-to-End Pilot Workflow Validation  
Status: Ready for final live pilot smoke test

## Validation Basis

This validation report is based on:

- implemented frontend routes and workflow screens
- implemented backend services and staff/youth endpoints
- prior live validation from Checkpoint 35 for attendance, Student Record, and dashboard-backed record sync
- duplicate-profile safeguards from Checkpoints 36C–36E
- current frontend production build
- current backend test suite

Live browser validation with real pilot staff/youth credentials was not performed in this checkpoint because no authenticated test session or credentials were provided in-thread. Items marked `PASS WITH MINOR ISSUE` are implemented but still require a final live smoke test with seeded pilot data before production deployment.

Status categories:

- `PASS` — implemented and supported by code/tests/prior live checkpoint evidence.
- `PASS WITH MINOR ISSUE` — implemented, but needs final live data/session confirmation or staff setup.
- `FAIL` — blocking defect found.

Severity categories:

- `Critical` — blocks pilot launch.
- `High` — should be fixed before production deployment.
- `Medium` — acceptable for controlled pilot with staff awareness.
- `Low` — polish/training issue.

## Executive Summary

Overall MVP readiness: `PASS WITH MINOR ISSUE`

Pilot Go / No-Go recommendation: `GO for controlled pilot smoke testing; NO-GO for broad production deployment until live staff/youth smoke test passes.`

No blocking code defects were found during this checkpoint. The core MVP workflows are implemented and validation commands passed. The remaining risk is operational: the live pilot environment must contain seeded programs, credential definitions, youth/staff accounts, and representative records before ASPN can confirm every end-to-end workflow in the browser.

Critical issues remaining: None identified in code during this checkpoint.

## Youth Workflow Validation

| Workflow | Status | Root cause / evidence | Severity | Recommended fix | Blocking? |
| --- | --- | --- | --- | --- | --- |
| Create Account | PASS WITH MINOR ISSUE | Account creation routes to Profile setup; final live Firebase account creation should be smoke tested. | Low | Create one final test youth account in the production-like environment. | No for controlled smoke test; yes before broad deployment. |
| Login | PASS WITH MINOR ISSUE | Login route and Firebase Auth handling are implemented; live credential confirmation required. | Low | Confirm staff/youth test accounts can sign in after deployment configuration is final. | No |
| Complete Profile | PASS | 31C onboarding recovery routes new youth to Profile; profile save creates/updates youth profile. | Low | Smoke test one new youth profile save after deployment. | No |
| Update Profile | PASS | Profile page loads existing profile, supports save/update, and preserves youth-facing fields. | Low | Confirm profile refresh after save in live environment. | No |
| View Home | PASS | Home reads youth dashboard and has loading/error/empty states. | Low | Confirm with seeded credential/program/service data. | No |
| View My Journey | PASS | My Journey reads dashboard-backed credentials, learning, service, and attendance log. | Low | Confirm latest attendance/credential/service records appear after live workflow actions. | No |
| View Credential Explorer | PASS | Credential Explorer displays official credential registry and earned status from dashboard data. | Low | Confirm earned state after a live staff credential award. | No |
| View Credential Detail | PASS | Credential detail route `/credentials/:credentialId` displays earned/not-earned status and pathway context. | Low | Open Civic Research, Data & Evaluation Basics, and Public Communication after deployment. | No |
| View Programs | PASS WITH MINOR ISSUE | Programs page supports active program browsing and youth enrollment; requires at least one active seeded program. | Medium | Confirm active pilot programs are created and visible. | No if seed data exists. |
| View Service Hours | PASS | Youth Service Hours reads dashboard-backed service-hour records and request link state. | Low | Confirm verified service-hour record appears after staff creation. | No |
| View Notifications | PASS WITH MINOR ISSUE | Notifications page is implemented; notification creation depends on live backend event behavior. | Low | Award a credential and confirm notification appears/read state updates. | No |
| View Global Civic Movements | PASS WITH MINOR ISSUE | Learning page supports active activities, external links, and progress save; requires seeded activity. | Medium | Seed at least one active Global Civic Movements activity and verify progress save. | No if seeded. |

## Staff Workflow Validation

| Workflow | Status | Root cause / evidence | Severity | Recommended fix | Blocking? |
| --- | --- | --- | --- | --- | --- |
| Login | PASS WITH MINOR ISSUE | Staff gate verifies backend staff/admin access through protected metrics endpoint; final role/token smoke test required. | Low | Confirm one staff account has correct role claims/profile access. | No |
| View Dashboard | PASS | Staff Dashboard loads protected metrics and summary cards with safe states. | Low | Confirm metrics load in live environment. | No |
| View Youth Management | PASS | Youth Management lists youth profiles, review fields, duplicate warnings, review queue, and Student Record. | Low | Confirm list loads with live staff account. | No |
| Refresh Student Record | PASS | Checkpoint 35 validated Student Record sync for credentials, attendance, and service hours. | Low | Re-test with current Colby test account after deployment restart. | No |
| Award Credential | PASS | Staff Credential Management supports youth identifier resolution and credential dropdown award flow. | Low | Award Civic Research to test youth and confirm downstream views. | No |
| Create Attendance | PASS | Checkpoint 35 validated working attendance creation path and youth My Journey Attendance Log sync. | Low | Create a new present record and confirm My Journey + Student Record. | No |
| Create Service Hours | PASS WITH MINOR ISSUE | Staff Service Hours Management supports record creation/review; final live service-hour sync test should be performed. | Low | Create verified service-hour record and confirm youth/staff views. | No |
| Create Program | PASS WITH MINOR ISSUE | Program Management supports create/update/archive/restore; live seed program creation should be confirmed. | Medium | Create or verify active pilot program before onboarding. | No if active program already exists. |
| View Program Roster | PASS | Program Management displays read-only roster using existing enrollment data. | Low | Confirm roster updates after youth enrollment. | No |
| View Duplicate Profile Review | PASS | 36D added read-only grouped duplicate review queue; no cleanup actions exist. | Low | Confirm no-warning and warning states with live duplicate/stale test data if available. | No |
| View Reports | PASS WITH MINOR ISSUE | Reporting dashboards exist and load protected reporting endpoints; values depend on seeded/live data. | Medium | Staff should open each dashboard after seed data exists and confirm expected empty/data states. | No |

## Cross-System Workflow Validation

### Credential Sync

```text
Award Credential
↓
Youth Credential Explorer updates
↓
Profile updates
↓
My Journey updates
```

Status: `PASS`

Root cause / evidence: Credential awards are stored as earned credentials under resolved youth UID. Credential Explorer, Profile, Home, and My Journey read earned credentials from the youth dashboard/source-of-truth data.

Severity: Low

Recommended fix: Final live smoke test after awarding Civic Research to the pilot test youth.

Blocking: No.

### Attendance Sync

```text
Attendance
↓
Youth Attendance Log
↓
Staff Student Record
```

Status: `PASS`

Root cause / evidence: Checkpoint 35 was pilot validated. Manual staff attendance creation updates youth My Journey Attendance Log and Staff Student Record.

Severity: Low

Recommended fix: Repeat one live attendance record after deployment restart.

Blocking: No.

### Service Hours Sync

```text
Service Hours
↓
Youth Service Hours
↓
Staff Student Record
```

Status: `PASS WITH MINOR ISSUE`

Root cause / evidence: Service-hour record creation and dashboard-backed reads are implemented. Final live smoke test is still needed with a verified service-hour record.

Severity: Low

Recommended fix: Staff creates one verified service-hour record for the test youth and confirms Youth Service Hours and Student Record show it.

Blocking: No.

### Program Enrollment Sync

```text
Program Enrollment
↓
Roster
↓
Youth Programs
```

Status: `PASS WITH MINOR ISSUE`

Root cause / evidence: Youth Programs supports active program enrollment. Program Management reads existing enrollment/roster data. Requires live active program and youth test account.

Severity: Medium

Recommended fix: Confirm one youth enrolls in one active program and appears in Program Management roster.

Blocking: No if an active program is already seeded; yes before broad onboarding if no program exists.

### Duplicate Detection

```text
Duplicate Detection
↓
Warning
↓
Review Queue
↓
Student Record
```

Status: `PASS`

Root cause / evidence: 36C added selected-youth duplicate warnings. 36D added grouped Duplicate Profile Review. 36E documented canonical-profile decision guidance. Student Record fallback from Checkpoint 35 remains intact.

Severity: Low

Recommended fix: Staff should verify warning behavior against known duplicate/stale test data if available.

Blocking: No.

## Detailed Validation Checklist

### Youth

- [x] Create Account — `PASS WITH MINOR ISSUE`
- [x] Login — `PASS WITH MINOR ISSUE`
- [x] Complete Profile — `PASS`
- [x] Update Profile — `PASS`
- [x] View Home — `PASS`
- [x] View My Journey — `PASS`
- [x] View Credential Explorer — `PASS`
- [x] View Credential Detail — `PASS`
- [x] View Programs — `PASS WITH MINOR ISSUE`
- [x] View Service Hours — `PASS`
- [x] View Notifications — `PASS WITH MINOR ISSUE`
- [x] View Global Civic Movements — `PASS WITH MINOR ISSUE`

### Staff

- [x] Login — `PASS WITH MINOR ISSUE`
- [x] View Dashboard — `PASS`
- [x] View Youth Management — `PASS`
- [x] Refresh Student Record — `PASS`
- [x] Award Credential — `PASS`
- [x] Create Attendance — `PASS`
- [x] Create Service Hours — `PASS WITH MINOR ISSUE`
- [x] Create Program — `PASS WITH MINOR ISSUE`
- [x] View Program Roster — `PASS`
- [x] View Duplicate Profile Review — `PASS`
- [x] View Reports — `PASS WITH MINOR ISSUE`

## Production Deployment Smoke Test Required

Before production deployment, ASPN should complete one final live smoke test using:

- one staff/admin test account
- one youth test account
- one active program
- one active Global Civic Movements activity
- official credential definitions
- one awarded credential
- one present attendance record
- one verified service-hour record
- one notification if credential notification creation is supported in the target environment

Minimum smoke-test path:

1. Youth creates account.
2. Youth completes profile.
3. Staff confirms youth in Youth Management.
4. Youth enrolls in active program.
5. Staff confirms Program Roster.
6. Staff awards Civic Research credential.
7. Youth confirms Credential Explorer, Profile, Home, and My Journey update.
8. Staff creates present attendance.
9. Youth confirms Attendance Log.
10. Staff creates verified service-hour record.
11. Youth confirms Service Hours.
12. Staff refreshes Student Record and confirms credential, attendance, and service hours.
13. Staff opens Duplicate Profile Review and confirms read-only behavior.
14. Staff opens reports and confirms safe empty/data states.

## Critical Issues Remaining

None identified in code during this checkpoint.

## Known Minor Issues / Setup Dependencies

- Live seed data must be present before broad onboarding.
- Reporting dashboards depend on live data volume and staff understanding.
- Notifications should be smoke tested after credential award in the target environment.
- Global Civic Movements requires at least one active activity.
- Program enrollment/roster validation requires at least one active program.
- Service-hour sync should be verified with one live verified record.

## Overall MVP Readiness

Status: `PASS WITH MINOR ISSUE`

The ASPN MVP is ready for a controlled final pilot smoke test. The implemented workflows are coherent, protected, and connected through the youth dashboard/source-of-truth record model. No blocking code defect was found in this checkpoint.

## Pilot Go / No-Go Recommendation

Recommendation: `GO for final controlled pilot smoke test; conditional GO for production deployment after smoke test passes.`

Do not proceed to broad youth onboarding until the live smoke-test path above has passed with real pilot configuration and seed data.

## Recommended Next Checkpoint

Recommended next checkpoint:

`37C — Production Deployment Smoke Test and Launch Checklist`
