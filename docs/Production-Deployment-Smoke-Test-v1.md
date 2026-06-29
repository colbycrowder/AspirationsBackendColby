# Production Deployment Smoke Test v1

Date: June 26, 2026  
Checkpoint: 38A — Production Deployment Smoke Test & Launch Checklist  
Target pilot window: September 2026  
Status: Ready to execute with production-like environment access

## Purpose

This checklist prepares the ASPN Platform for September pilot deployment by verifying the production environment, confirming seed data, and executing a complete live smoke test.

This checkpoint does not add features or redesign the product. Code should only change if a true deployment-blocking defect is discovered during execution.

## Status Legend

Use these statuses during live validation:

- `PASS` — works as expected.
- `PASS WITH MINOR ISSUE` — usable for pilot; note owner and follow-up.
- `FAIL` — workflow does not work as expected.

For every `FAIL`, mark whether it is blocking.

## Section 1 — Environment Verification

| Item | Status | Blocking? | Owner | Resolution / Notes |
| --- | --- | --- | --- | --- |
| Firebase project | Not Run | TBD | ASPN technical lead | Confirm correct production/staging Firebase project is selected. |
| Firestore | Not Run | TBD | ASPN technical lead | Confirm database exists, rules/config are appropriate, and expected collections are reachable. |
| Firebase Auth | Not Run | TBD | ASPN technical lead | Confirm staff and youth test accounts can authenticate. |
| Hosting | Not Run | TBD | ASPN technical lead | Confirm frontend hosting target and public URL. |
| Storage, if used | Not Run | TBD | ASPN technical lead | Confirm bucket exists if credential/profile assets require it. If not used, mark N/A. |
| Environment variables | Not Run | TBD | ASPN technical lead | Confirm frontend and backend environment variables point to production/staging services. |
| `GOOGLE_APPLICATION_CREDENTIALS` | Not Run | TBD | ASPN technical lead | Confirm backend runtime has the correct Firebase service-account path/secret. |
| Production API URL | Not Run | TBD | ASPN technical lead | Confirm frontend `VITE_API_BASE_URL` points to deployed backend. |
| Frontend build | PASS | No | Codex / ASPN technical lead | `npm run build` passed during checkpoint validation. Re-run in deployment environment. |
| Backend build/tests | PASS | No | Codex / ASPN technical lead | `./gradlew test` passed during checkpoint validation. Re-run in deployment environment. |

Environment verification pass condition:

- All required services are reachable.
- Frontend and backend point to the same intended environment.
- Staff/youth authentication works.
- No service-account or CORS failure appears in normal UI.

## Section 2 — Seed Data

| Item | Minimum required | Status | Blocking? | Owner | Resolution / Notes |
| --- | ---: | --- | --- | --- | --- |
| Programs | 1 active pilot program | Not Run | Yes if absent | Program operations lead | Confirm Youth2Lead, Youth Advisory Board, Youth Council, or approved active pilot program exists. |
| Credential Definitions | 6 core credentials; 4 advanced recommended | Not Run | Yes for core | Credential operations lead | Confirm official credential names match ASPN registry. |
| Learning Activities | 1 active Global Civic Movements activity | Not Run | Yes if learning is part of launch | Learning/program lead | Confirm external link and active status. |
| Staff accounts | At least 1 staff/admin test account | Not Run | Yes | ASPN technical lead | Confirm staff role access and navigation. |
| Test youth | At least 1 non-real test youth account | Not Run | Yes | ASPN technical lead | Use before inviting real youth. |
| Notifications | At least credential-earned notification behavior tested, if supported | Not Run | No | ASPN technical lead | Confirm notification appears after credential award or document unsupported state. |

Seed data pass condition:

- Youth pages do not appear empty because of missing pilot content.
- Staff can perform all pilot workflows without direct Firestore editing.
- Test data avoids real youth PII until staff training and launch approval are complete.

## Section 3 — Smoke Test

### Youth Smoke Test

| Step | Expected result | Status | Blocking? | Owner | Resolution / Notes |
| --- | --- | --- | --- | --- | --- |
| Create Account | New test youth can create Firebase account and land on Profile setup. | Not Run | Yes | ASPN technical lead |  |
| Login | Test youth can sign out and sign back in. | Not Run | Yes | ASPN technical lead |  |
| Complete Profile | Required profile fields save and Home can load afterward. | Not Run | Yes | ASPN technical lead |  |
| View Dashboard | Home shows profile summary, journey cards, and honest empty/data states. | Not Run | Yes | ASPN technical lead |  |
| View Programs | Active pilot program appears and can be opened/enrolled if intended. | Not Run | Yes | Program operations lead |  |
| View Credentials | Credential Explorer loads official credential cards/icons. | Not Run | Yes | Credential operations lead |  |
| View Journey | My Journey loads without errors and shows relevant program/credential/service/attendance records when present. | Not Run | Yes | ASPN technical lead |  |
| View Service Hours | Service Hours page loads and displays records or request-link unavailable state. | Not Run | No | Program operations lead |  |
| View Notifications | Notifications page loads and unread/read behavior works if notifications exist. | Not Run | No | ASPN technical lead |  |
| View Global Civic Movements | Learning page loads active activity and external link language is clear. | Not Run | No | Learning/program lead |  |

### Staff Smoke Test

| Step | Expected result | Status | Blocking? | Owner | Resolution / Notes |
| --- | --- | --- | --- | --- | --- |
| Login | Staff account passes staff gate and sees staff navigation. | Not Run | Yes | ASPN technical lead |  |
| Youth Management | Youth list loads; selected youth shows review fields and Student Record panel. | Not Run | Yes | ASPN technical lead |  |
| Award Credential | Staff awards Civic Research or approved test credential using ASPN Participant ID/youth selector. | Not Run | Yes | Credential operations lead |  |
| Attendance | Staff creates present attendance for the test youth and active program. | Not Run | Yes | Program operations lead |  |
| Service Hours | Staff creates or verifies a service-hour record for the test youth. | Not Run | No | Program operations lead |  |
| Program Roster | Staff opens Program Management and confirms enrolled youth appears in roster. | Not Run | Yes | Program operations lead |  |
| Student Record | Staff refreshes Student Record and sees credential, attendance, and service-hour records. | Not Run | Yes | ASPN technical lead |  |
| Duplicate Review | Staff opens Duplicate Profile Review; confirms read-only behavior and no merge/archive/delete actions. | Not Run | No | ASPN technical lead |  |
| Reporting | Staff opens Dashboard, Reporting, Operations Reporting, Platform Metrics, Pilot Readiness, Pilot Metrics, and Pilot Evaluation. | Not Run | No | ASPN operations lead |  |

Smoke test pass condition:

- No red blocking errors remain.
- Staff can operate the core pilot workflows without direct database access.
- Youth can complete onboarding and see the records staff create.

## Section 4 — Cross-System Validation

### Credential Validation

```text
Award Credential
↓
Explorer
↓
Journey
↓
Profile
```

| Check | Expected result | Status | Blocking? | Owner | Resolution / Notes |
| --- | --- | --- | --- | --- | --- |
| Staff award succeeds | Credential award completes for selected test youth. | Not Run | Yes | Credential operations lead |  |
| Explorer updates | Credential Explorer shows earned state. | Not Run | Yes | Credential operations lead |  |
| Journey updates | My Journey shows credential milestone/detail. | Not Run | Yes | Credential operations lead |  |
| Profile updates | Profile → My Credentials shows credential. | Not Run | Yes | Credential operations lead |  |

### Attendance Validation

```text
Attendance
↓
Attendance Log
↓
Student Record
```

| Check | Expected result | Status | Blocking? | Owner | Resolution / Notes |
| --- | --- | --- | --- | --- | --- |
| Staff attendance creation succeeds | Present attendance record saves. | Not Run | Yes | Program operations lead |  |
| Youth Attendance Log updates | My Journey Attendance Log shows the record. | Not Run | Yes | Program operations lead |  |
| Staff Student Record updates | Student Record shows the same attendance record. | Not Run | Yes | Program operations lead |  |

### Service Hours Validation

```text
Service Hours
↓
Youth View
↓
Student Record
```

| Check | Expected result | Status | Blocking? | Owner | Resolution / Notes |
| --- | --- | --- | --- | --- | --- |
| Staff service-hour creation/review succeeds | Record saves with expected verification status. | Not Run | No | Program operations lead |  |
| Youth view updates | Youth Service Hours page shows record. | Not Run | No | Program operations lead |  |
| Staff Student Record updates | Student Record shows the same service-hour record. | Not Run | No | Program operations lead |  |

Cross-system pass condition:

- Staff-created operational records appear in the youth-facing record source.
- Staff Student Record matches youth-facing views.
- No duplicate/stale profile confusion blocks record review.

## Section 5 — Deployment Sign-Off

Use this table to record all smoke-test findings.

| Area | Status | Blocking? | Owner | Resolution |
| --- | --- | --- | --- | --- |
| Environment verification | Not Run | TBD | ASPN technical lead |  |
| Seed data | Not Run | TBD | ASPN operations lead |  |
| Youth smoke test | Not Run | TBD | ASPN technical lead |  |
| Staff smoke test | Not Run | TBD | ASPN technical lead |  |
| Cross-system credential validation | Not Run | TBD | Credential operations lead |  |
| Cross-system attendance validation | Not Run | TBD | Program operations lead |  |
| Cross-system service-hour validation | Not Run | TBD | Program operations lead |  |
| Reporting validation | Not Run | TBD | ASPN operations lead |  |
| Duplicate-profile safeguards | Not Run | TBD | ASPN technical lead |  |
| Final deployment approval | Not Run | TBD | ASPN pilot owner |  |

Sign-off rule:

- `PASS` across environment, seed data, youth smoke test, staff smoke test, credential sync, attendance sync, and Student Record sync is required before broad youth onboarding.
- `PASS WITH MINOR ISSUE` may be accepted only when the issue is non-blocking, documented, assigned to an owner, and does not affect youth privacy or record integrity.
- Any `FAIL` marked blocking results in No-Go until resolved.

## Section 6 — Final Pilot Go / No-Go

### Recommendation

Current recommendation before live execution:

`GO for controlled production smoke test.`

Production deployment recommendation after execution:

`GO only if all blocking checklist items pass.`

### Known Issues to Track

| Issue | Severity | Blocking? | Owner | Notes |
| --- | --- | --- | --- | --- |
| Live seed data must be verified | High | Yes if missing | ASPN operations lead | Active program, core credentials, learning activity, and test accounts are required. |
| Reporting dashboards require staff interpretation | Medium | No | ASPN operations lead | Staff should understand differences between dashboards. |
| Notifications require final live confirmation | Low | No | ASPN technical lead | Confirm after test credential award. |
| Duplicate-profile cleanup remains deferred | Low | No | ASPN operations lead | Warning/review tools are read-only; no merge/archive during pilot. |

### Rollback Plan

If a blocking deployment issue appears:

1. Stop broader youth onboarding.
2. Keep staff access limited to pilot validators.
3. Preserve current Firestore data; do not manually delete or relink youth records.
4. Record the failed checklist item, owner, root cause, and timestamp.
5. Revert frontend deployment to the last known working build if the issue is frontend-only.
6. Restart or redeploy backend only after confirming environment variables and service-account configuration.
7. Re-run this smoke-test checklist after the fix.

Rollback should prioritize record integrity and youth privacy over speed.

### Post-Launch Monitoring

For the first two weeks of pilot launch, monitor:

- account creation and profile completion failures
- staff authorization failures
- youth dashboard load failures
- credential award failures
- attendance creation failures
- service-hour creation/review failures
- Student Record mismatches
- duplicate-profile warnings
- reporting dashboard load failures
- frontend build/runtime errors
- backend logs for Firebase/Firestore permission or mapping issues

Recommended monitoring cadence:

- launch day: check after every onboarding wave
- week 1: daily staff operations review
- week 2: at least twice weekly
- after week 2: weekly pilot operations review

## Validation Commands

Run before deployment:

```bash
cd "/Users/colbycrowder/Documents/AspirationsBackendColby/UserData"
./gradlew test
```

```bash
cd "/Users/colbycrowder/Documents/AspirationsBackendColby/frontend"
npm run build
```

```bash
cd "/Users/colbycrowder/Documents/AspirationsBackendColby"
git diff --check
```

Checkpoint 38A command results:

| Command | Result | Notes |
| --- | --- | --- |
| Backend `./gradlew test` | PASS | Completed during checkpoint validation. |
| Frontend `npm run build` | PASS | Existing Vite chunk-size warning only. |
| `git diff --check` | PASS | Completed during checkpoint validation. |

## Final Checklist Owner Notes

Assign final live execution owners before September pilot:

- ASPN pilot owner: final Go / No-Go
- ASPN technical lead: environment, auth, backend/frontend deployment
- ASPN operations lead: seed data, reports, staff workflow readiness
- Program operations lead: programs, attendance, service hours
- Credential operations lead: credential definitions and award validation

Do not invite real youth into production until this checklist has been executed with a test youth account and all blocking items are marked `PASS`.
