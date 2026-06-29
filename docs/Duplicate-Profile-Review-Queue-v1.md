# Duplicate Profile Review Queue v1

Date: June 26, 2026  
Checkpoint: 36D — Duplicate Profile Review Queue / Canonical Profile Planning  
Status: Read-only pilot safeguard

## Purpose

The Duplicate Profile Review queue gives staff a read-only view of youth profiles that may represent the same participant.

This checkpoint adds visibility only. It does not merge, archive, delete, relink, or modify youth profiles or operational records.

## Grouping Logic

Possible duplicate youth profiles are grouped when two or more youth profiles share:

- exact ASPN Participant ID
- normalized lowercase email

Each group explains why the profiles were grouped:

- Matching ASPN Participant ID
- Matching email

The queue returns safe summary fields only:

- Firebase UID / Firestore document ID
- name
- email
- ASPN Participant ID
- profile status
- staff verified status
- whether dashboard-backed records appear to exist

## What Staff Should Do During Pilot

If duplicate groups appear:

1. Treat the queue as a review list only.
2. Select each youth profile in Youth Management.
3. Use the Student Record panel to inspect credentials, attendance, and service hours.
4. Use `Canonical-Profile-Decision-Runbook-v1.md` to compare evidence before treating one profile as the working profile.
5. Prefer the profile that contains the current dashboard-backed records for day-to-day pilot operations when the evidence is clear.
6. Flag the duplicate group for later cleanup planning.

If no duplicate groups appear, staff can continue normal Youth Management workflows.

## What Staff Should Not Do

During pilot validation, staff should not:

- delete profiles
- merge profiles
- archive profiles
- manually relink credentials
- manually relink attendance
- manually relink service-hour records
- change Firebase UID values
- change Firestore document IDs

## Canonical Profile Criteria for Future Planning

Before ASPN builds a merge/archive workflow, staff and administrators should agree on canonical-profile criteria.

Recommended criteria to consider:

1. The profile attached to the active Firebase Auth UID.
2. The profile containing the most complete Student Record.
3. The profile with earned credentials.
4. The profile with verified attendance and service-hour records.
5. The profile with current program enrollment.
6. The profile with the correct ASPN Participant ID.
7. The profile with the most recent successful youth login or profile update.
8. The profile that staff have verified.

No single criterion should automatically decide canonical status without staff review.

## Why Merge / Archive Remains Deferred

Merge and archive workflows are intentionally deferred because youth identity touches multiple operational records:

- credentials
- attendance
- service hours
- program enrollments
- learning progress
- notifications
- research and reporting exports

Moving or relinking those records requires audit history, rollback planning, staff permissions, and explicit data ownership rules.

The safe pilot approach is:

```text
Detect → Review → Confirm canonical plan later
```

not:

```text
Detect → Auto-merge
```

## Current Pilot Readiness

Status: Ready with monitoring

The Duplicate Profile Review queue reduces staff blind spots without altering any working pilot workflows. It should be used as a staff awareness and planning tool until ASPN is ready to design a formal merge/archive process.
