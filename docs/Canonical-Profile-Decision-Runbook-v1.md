# Canonical Profile Decision Runbook v1

Date: June 26, 2026  
Checkpoint: 36E — Canonical Profile Decision Criteria and Staff Runbook  
Status: Pilot operations guidance

## Purpose

This runbook helps ASPN staff review possible duplicate youth profiles and decide which profile should be treated as the canonical profile during pilot operations.

This is a decision-support document only. It does not authorize merge, archive, delete, relink, or manual record-edit actions during pilot validation.

## When Staff Should Use This Runbook

Use this runbook when:

- Staff Youth Management shows a possible duplicate profile warning.
- The Duplicate Profile Review queue shows two or more profiles grouped by matching ASPN Participant ID.
- The Duplicate Profile Review queue shows two or more profiles grouped by matching email.
- A youth’s Student Record appears empty, but another profile with the same email or ASPN Participant ID appears to contain records.
- Staff need to decide which profile to use for day-to-day pilot operations.

Do not use this runbook to perform cleanup. Use it to guide review and temporary pilot operations decisions.

## What “Canonical Profile” Means

For pilot operations, the canonical profile means:

> The youth profile staff should use as the working source for viewing records and making operational decisions during the pilot.

Canonical does not mean:

- the other profile should be deleted
- records should be moved manually
- Firebase UID should be changed
- Firestore document IDs should be changed
- duplicate profiles should be merged immediately

The canonical profile is a staff-reviewed operational choice, not an automatic system decision.

## What Staff Should Not Do During Pilot

During pilot validation, staff should not:

- delete duplicate profiles
- merge duplicate profiles
- archive duplicate profiles
- manually relink credentials
- manually relink attendance records
- manually relink service-hour records
- manually edit Firebase UID values
- manually edit Firestore document IDs
- change ASPN Participant IDs without a clear administrative process
- assume the profile with the newest visible form data is automatically canonical

If a duplicate is found, preserve both profiles and flag the group for post-pilot cleanup planning.

## Step-by-Step Duplicate Review Process

1. Open Staff → Youth Management.

2. Review the Duplicate Profile Review section.

3. If a duplicate group appears, note the grouping reason:
   - matching ASPN Participant ID
   - matching email

4. Select the first profile in the group from the Youth Profiles list.

5. Open or refresh the Student Record panel.

6. Record whether the profile has:
   - earned credentials
   - attendance records
   - service-hour records
   - active program participation if visible

7. Select the next profile in the duplicate group.

8. Repeat the Student Record review.

9. Compare evidence using the checklist below.

10. For pilot operations, use the profile with the strongest evidence of being the current working youth record.

11. Do not alter the duplicate profile. Mark the group for post-pilot cleanup planning.

## Canonical Profile Decision Criteria

No single criterion automatically decides the canonical profile. Staff should weigh evidence together.

Stronger canonical indicators include:

1. Active Firebase Auth UID
   - Prefer the profile tied to the youth’s active login when known.

2. Dashboard-backed records
   - Prefer the profile whose Student Record shows current credentials, attendance, or service hours.

3. Earned credentials
   - A profile with awarded credentials is usually stronger than a profile with no records.

4. Verified attendance
   - A profile with present or excused attendance records from active pilot programs is a strong signal.

5. Verified service hours
   - A profile with verified service hours is a strong operational signal.

6. Active program enrollment
   - A profile enrolled in the current active program is stronger than an unenrolled stale profile.

7. Correct ASPN Participant ID
   - Prefer the profile with the ASPN Participant ID staff are using for pilot operations.

8. Most recent youth login or profile update, if available
   - More recent youth activity may indicate the active profile.

9. Staff verification status
   - A staff-verified profile is stronger than an unreviewed profile, especially when other evidence aligns.

## Evidence Checklist

For each duplicate profile, staff should capture:

| Evidence | Profile A | Profile B | Notes |
| --- | --- | --- | --- |
| Firebase UID / document ID |  |  | Do not edit. |
| Name |  |  | Confirm spelling but do not rely on name alone. |
| Email |  |  | Normalize case when comparing. |
| ASPN Participant ID |  |  | Preferred staff-facing identifier. |
| Profile status |  |  | Active is stronger than pending/inactive. |
| Staff verified |  |  | Helpful but not decisive alone. |
| Student Record has credentials |  |  | Strong canonical signal. |
| Student Record has attendance |  |  | Strong canonical signal. |
| Student Record has service hours |  |  | Strong canonical signal. |
| Active program enrollment |  |  | Strong pilot operations signal. |
| Most recent youth activity |  |  | Use if available. |
| Staff notes |  |  | Capture context for post-pilot cleanup. |

## Example Staff Decision Scenarios

### Scenario 1: One profile has records, one profile is empty

Evidence:

- Profile A has Civic Research and YAB attendance records.
- Profile B has the same email but no Student Record activity.

Pilot-safe decision:

- Treat Profile A as canonical for pilot operations.
- Do not delete Profile B.
- Flag the group for post-pilot cleanup.

### Scenario 2: One profile is staff verified, but another has records

Evidence:

- Profile A is staff verified but has no credentials or attendance.
- Profile B is not staff verified but contains earned credentials and attendance.

Pilot-safe decision:

- Treat Profile B as the likely operational profile.
- Staff may review Profile B status if normal Youth Management review rules allow it.
- Do not move records manually.

### Scenario 3: Same ASPN Participant ID, different emails

Evidence:

- Two profiles share ASPN Participant ID.
- Emails differ.
- One profile has current program participation and records.

Pilot-safe decision:

- Treat the profile with records and current program participation as canonical for operations.
- Confirm the correct email with the youth or staff records outside the platform process.
- Do not overwrite identity fields unless ASPN has a clear administrative correction policy.

### Scenario 4: Same email, different ASPN Participant IDs

Evidence:

- Two profiles share the same email.
- ASPN Participant IDs differ.
- Both have some records.

Pilot-safe decision:

- Do not choose automatically.
- Escalate to ASPN operations lead.
- Preserve both profiles until a canonical decision can be made with full context.

### Scenario 5: Both profiles have meaningful records

Evidence:

- Profile A has credentials.
- Profile B has attendance or service hours.

Pilot-safe decision:

- Do not merge manually.
- Identify which profile should be used for new pilot activity going forward.
- Flag as high-priority post-pilot cleanup because record relinking may be needed later.

## Pilot-Safe Action Guidance

Staff may:

- view duplicate groups
- inspect Student Records
- use the profile with current records for day-to-day pilot operations
- update normal review fields when already allowed by Youth Management
- document suspected duplicate groups for post-pilot cleanup
- ask the youth or program lead to confirm the active login/email when needed

Staff should avoid:

- changing identifiers to “force” a match
- copying records between profiles
- deleting stale profiles
- editing backend data directly
- treating the review queue as an automatic cleanup tool

## Post-Pilot Merge / Archive Planning Notes

Before ASPN implements merge/archive tooling, the team should define:

1. Who can approve a canonical profile decision.
2. What evidence is required before records can be relinked.
3. Which records can be safely moved.
4. Which records must remain immutable for audit/history.
5. How to preserve original Firebase UID/document ID history.
6. How to notify staff that a profile was archived or merged.
7. How to reverse or audit a merge if a mistake is discovered.
8. Whether ASPN Participant ID uniqueness should be enforced before merge/archive begins.

Recommended future workflow:

```text
Detect duplicates
↓
Review evidence
↓
Choose canonical profile with approval
↓
Plan record relinking
↓
Archive stale profile with audit trail
```

The platform should not perform automatic merge or archive actions until those governance rules are finalized.

## Pilot Readiness Assessment

Status: Ready with guidance

The current duplicate-profile tooling is intentionally read-only. Staff can identify possible duplicate profiles, inspect Student Records, and choose a working profile for pilot operations without changing identity data or risking record loss.
