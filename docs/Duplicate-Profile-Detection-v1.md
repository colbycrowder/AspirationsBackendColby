# Duplicate Profile Detection v1

Date: June 26, 2026  
Checkpoint: 36C — Duplicate Profile Detection and Staff Warning  
Status: Pilot safety safeguard

## Purpose

This checkpoint adds a lightweight, read-only warning for staff when a selected youth profile may have a duplicate or stale profile.

The goal is visibility, not cleanup. The platform does not merge, archive, delete, or modify profiles as part of this checkpoint.

## What Is Detected

Staff Youth Management now checks for possible duplicate youth profiles using:

- exact ASPN Participant ID match
- normalized lowercase email match

The currently selected youth profile is excluded from its own duplicate list.

When a possible duplicate is found, staff see a non-blocking warning:

> Possible duplicate profile found. This youth may have another profile with the same email or ASPN Participant ID.

The warning includes read-only summary fields:

- Firebase UID / document ID
- name
- email
- ASPN Participant ID
- profile status
- whether dashboard-backed records appear to exist

## What Is Not Changed

This checkpoint does not:

- merge profiles
- archive profiles
- delete profiles
- modify Firestore schema
- change authentication
- change role permissions
- change attendance creation
- change credential awarding
- change service-hour storage
- change youth dashboard behavior
- remove the Checkpoint 35 duplicate/stale Student Record fallback

## Why Merge / Archive Is Deferred

Profile merge and archive actions are higher-risk operations because youth records may be connected to:

- earned credentials
- attendance records
- service-hour records
- program enrollments
- learning progress
- notifications
- external reporting or research identifiers

Before any merge/archive workflow exists, staff need a safe way to identify possible duplicates and confirm which profile contains records. This checkpoint provides that visibility without changing data.

## Pilot Staff Guidance

If a duplicate warning appears:

1. Do not delete or merge profiles during pilot validation.
2. Open the Student Record panel.
3. Confirm which profile contains credentials, attendance, and service hours.
4. Continue using the profile with dashboard-backed records for pilot operations.
5. Use `Canonical-Profile-Decision-Runbook-v1.md` when staff need to decide which profile to treat as the working profile during pilot.
6. Flag the duplicate for post-validation cleanup.

If no duplicate warning appears, continue normal Youth Management workflow.

## Current Limitations

- Detection only checks exact ASPN Participant ID and normalized email.
- It does not detect similar names, misspelled emails, or likely duplicates across different participant IDs.
- It does not decide which profile is canonical.
- The dashboard-records indicator is a safe summary only; staff should still verify record details in Student Record.

## Recommended Next Safeguard

After pilot validation, add a duplicate profile review workflow that lets authorized staff:

- view grouped duplicate candidates
- identify the canonical Firebase UID
- mark stale profiles for review
- prepare a merge/archive plan with audit history

Do not implement merge/archive until data ownership and audit requirements are finalized.
