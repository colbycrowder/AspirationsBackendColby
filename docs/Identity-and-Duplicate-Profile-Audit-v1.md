# Identity and Duplicate Profile Audit v1

Date: June 26, 2026  
Checkpoint: 36B — Identity and Duplicate Profile Audit  
Status: Ready with monitoring

## Executive Summary

The platform’s operational source of truth is the Firebase UID, which also functions as the Firestore youth profile document ID for canonical records.

ASPN Participant ID is the preferred staff-facing and human-facing youth identifier. Email is supported as a recovery and lookup identifier in selected staff workflows, but it should not be treated as the primary source of truth because it can change and may not be guaranteed unique across stale profiles.

Current pilot-critical workflows are aligned well enough to proceed:

- Youth dashboard and My Journey read records by canonical Firebase UID.
- Staff Student Record resolves Firebase UID, ASPN Participant ID, or email before reading youth dashboard-backed records.
- Credential awards, attendance records, and service-hour records save operational data under a resolved `userUID`.
- The duplicate/stale profile fallback added during Checkpoint 35 remains in place and should not be removed before pilot.

No application code changes were required for this checkpoint.

## Current Identity Model

| Identifier | Current role | Source-of-truth status | Notes |
| --- | --- | --- | --- |
| Firebase UID | Authentication identity and canonical record key | Primary | Used by youth dashboard, earned credentials, attendance, service hours, and program participation records. |
| Firestore user document ID | User profile document key | Primary fallback | `UserInfoService` hydrates `User.uid` from the document ID when older documents do not contain a `uid` field. |
| `User.uid` | Backend model’s canonical user identifier | Primary when present | Should match the Firestore document ID for stable profile records. |
| ASPN Participant ID | Human-facing participant identifier | Secondary lookup / staff-facing ID | Preferred for staff workflows because staff should not need Firebase UIDs. Intended to be unique, but uniqueness is not fully enforced as a Firestore constraint. |
| Email | Contact/auth and recovery lookup | Recovery lookup | Useful for staff lookup and duplicate detection, but mutable and not ideal as a storage key. |

## Source of Truth

The canonical storage key for youth operational records is:

```text
Firebase UID / Firestore youth profile document ID
```

The following collections and flows should continue to store youth-linked records using `userUID` after identifier resolution:

- Earned credentials
- Attendance records
- Service-hour records
- Program participation/enrollment records
- Youth dashboard records
- My Journey records

ASPN Participant ID and email should resolve to a canonical Firebase UID before operational records are written or read.

## Backend Identity Resolution Audit

### UserInfoController

`UserInfoController` is the main staff/youth API entry point.

Confirmed behavior:

- Youth dashboard loads from the authenticated Firebase UID.
- Staff Student Record endpoint accepts an identifier path parameter.
- Staff Student Record resolves the selected youth through `UserInfoService`.
- Staff Student Record then loads the dashboard-backed record source using the resolved Firebase UID.
- If the selected youth profile is stale or empty, the duplicate/stale fallback searches other youth profiles with the same ASPN Participant ID or email and returns the non-empty dashboard record when found.

Risk level: Recommended During Pilot

The fallback is intentionally useful for pilot recovery, but it also confirms that duplicate/stale youth profile records can exist. Staff should treat duplicate identity matches as a data-cleanup signal.

### UserInfoService

`UserInfoService` is the strongest current identity-resolution layer.

Confirmed behavior:

- Direct user lookups start with Firestore document ID / Firebase UID.
- Staff youth lookup then falls back to `aspnParticipantId`.
- Staff youth lookup then falls back to `email`.
- Older user documents missing `uid` are hydrated with the Firestore document ID.
- Staff youth lists return only youth profiles when `youthProfile` is true.

Risk level: Important Before / During Pilot

ASPN Participant ID and email lookups are not guaranteed by database-level uniqueness. Duplicate profile detection should become an explicit staff safeguard in a future checkpoint.

### DashboardService

`DashboardService` uses the authenticated or resolved Firebase UID as the read key.

Confirmed behavior:

- Loads the youth profile using `userInfoService.getUser(userUID)`.
- Reads earned credentials by `userUID`.
- Reads attendance records by `userUID`.
- Reads service-hour records by `userUID`.
- Reads enrolled/active program data by `userUID`.

Risk level: Low

Dashboard behavior is aligned with the canonical identity model. The main risk is upstream: if a record was saved under the wrong UID, DashboardService will correctly omit it.

### CredentialService

Credential awarding resolves staff-entered youth identifiers before writing earned credential records.

Confirmed behavior:

- Staff award flow accepts a youth identifier.
- Direct Firestore document ID / Firebase UID is supported.
- ASPN Participant ID resolution is supported.
- Awarded credential records are written under the resolved `userUID`.
- Credential reads use `getEarnedCredentialsForUser(userUID)`.

Risk level: Recommended During Pilot

Credential awarding currently appears optimized for Firebase UID and ASPN Participant ID. Email is not the primary credential-award identifier path. This is acceptable for pilot because staff should use ASPN Participant ID, but the platform would benefit from a shared identity-resolution service so all staff write workflows support the same identifier types consistently.

### AttendanceService

Attendance creation resolves staff-entered youth identifiers before writing attendance records.

Confirmed behavior:

- Attendance creation accepts `userIdentifier` with `userUID` as fallback.
- ASPN Participant ID resolution is supported.
- Email resolution is supported.
- Non-ASPN/non-email identifiers are treated as Firebase UID.
- Attendance records are written under the resolved `userUID`.
- Youth dashboard and My Journey read attendance by `userUID`.

Risk level: Low

Attendance is now aligned with the pilot-validated workflow. The remaining risk is that a manually entered valid-but-wrong Firebase UID cannot be distinguished from an intentional UID unless the UI nudges staff toward roster/ASPN selection.

### ServiceHourService

Service-hour creation resolves youth identifiers before writing records.

Confirmed behavior:

- Service-hour creation accepts `userIdentifier` with `userUID` as fallback.
- ASPN Participant ID resolution is supported.
- Non-ASPN identifiers are treated as Firebase UID.
- Service-hour records are written under the resolved `userUID`.
- Youth dashboard reads service-hour records by `userUID`.

Risk level: Recommended During Pilot

Service-hour creation is compatible with ASPN Participant ID and Firebase UID. It does not currently appear to mirror the attendance path’s email-resolution behavior. This is not a pilot blocker if staff are trained to use ASPN Participant ID, but it is a consistency gap.

## Identifier Use by Workflow

| Workflow | Staff-facing input | Backend resolution | Storage key | Assessment |
| --- | --- | --- | --- | --- |
| Youth Dashboard | Authenticated Firebase UID | None needed | Firebase UID | Ready |
| My Journey | Dashboard-backed Firebase UID | None needed | Firebase UID | Ready |
| Staff Youth Management | Firebase UID / ASPN Participant ID / email | Resolves through `UserInfoService` | Firebase UID | Ready |
| Staff Student Record | Firebase UID / ASPN Participant ID / email | Resolves through `UserInfoService`, then dashboard-backed fallback | Firebase UID | Ready with monitoring |
| Credential Award | ASPN Participant ID or Firebase UID | Resolves before awarding | Firebase UID | Ready |
| Attendance Record | ASPN Participant ID, email, or Firebase UID | Resolves before creation | Firebase UID | Ready |
| Service-Hour Record | ASPN Participant ID or Firebase UID | Resolves before creation | Firebase UID | Ready with consistency note |

## Known Duplicate / Stale Profile Risk

The platform still has a known duplicate/stale profile risk:

1. A Firebase-authenticated user may have one profile document keyed by their active Firebase UID.
2. A stale or imported youth profile may exist with the same email or ASPN Participant ID.
3. Operational records may be attached to one Firebase UID while staff select another profile row.
4. Without explicit duplicate detection, staff may not know that two profile documents represent the same youth.

Current mitigation:

- Staff Student Record uses duplicate/stale fallback behavior.
- Fallback compares ASPN Participant ID and email across youth profiles.
- If the selected profile has no records but a matched duplicate profile has records, the Student Record can display the non-empty source.

This fallback is valuable for pilot recovery and should remain in place until a deliberate duplicate-profile cleanup workflow exists.

## Remaining Identifier Risks

### Important Before / During Pilot

- ASPN Participant ID uniqueness is intended but not enforced as a hard database constraint.
- Email-based lookup can return stale matches if old profiles are not retired.
- Staff may still encounter duplicate youth rows if duplicate profile documents exist.

### Recommended During Pilot

- Credential, attendance, and service-hour write paths do not all support the exact same set of identifier inputs.
- Manual Firebase UID entry remains technically supported for recovery, but staff-facing UI should continue to prioritize ASPN Participant ID or roster selection.
- Legacy records saved before identifier-resolution fixes may remain attached to stale UIDs until reviewed.

### Post-Pilot

- Introduce a formal duplicate-profile merge or archive workflow.
- Add audit metadata showing which identifier type was used at record creation time.
- Centralize identity resolution into a shared service used by credential, attendance, service-hour, and student-record workflows.

## Recommended Future Safeguards

1. Create a staff duplicate-profile review report grouped by:
   - normalized email
   - ASPN Participant ID
   - display name plus birth/school metadata if available

2. Add a staff warning when a selected youth profile shares an ASPN Participant ID or email with another youth profile.

3. Add backend uniqueness validation when assigning or updating ASPN Participant IDs.

4. Create a shared `IdentityResolutionService` so all staff write/read workflows resolve identifiers consistently.

5. Add a safe administrative merge plan:
   - identify canonical Firebase UID
   - move or relink operational records
   - archive stale profile document
   - preserve audit history

## Pilot Readiness Assessment

Status: Ready with monitoring

Checkpoint 36B does not identify a new blocker in the current pilot-validated workflows. The identity model is usable for controlled onboarding as long as staff continue using ASPN Participant IDs and the existing Student Record fallback remains available.

The main recommendation is not immediate schema change. The next safest step is a staff-facing duplicate-profile detection and warning checkpoint before any merge or cleanup workflow is attempted.
