# ASPN Platform Staff UX Snapshot

**Version:** Checkpoint 30H  
**Date:** June 22, 2026

---

# Platform Purpose

The current staff experience presents the ASPN Platform as a protected pilot-operations workspace. Staff and administrators use it to manage youth records, programs, credentials, participation, service, learning activities, stakeholder relationships, and pilot reporting without relying on routine direct Firestore editing.

## Program Administration

Staff can create, update, archive, restore, filter, and review programs. Program detail combines catalog information with enrollment, credential, attendance, and service-hour totals. Youth enrollment behavior remains separate and unchanged.

## Credential Administration

Staff can create and maintain credential definitions, connect definitions to programs, write requirement text, archive or restore definitions, and manually award credentials to youth. The backend also retains existing attendance-count and linked-learning award behavior, although the current staff credential screen does not provide a complete auto-award rule editor.

## Participation Tracking

Attendance and service-hour management provide operational record lists, filters, totals, creation controls, status updates, and deletion actions. Program and pilot reports reuse those records to summarize participation.

## Reporting

Staff reporting covers aggregate platform metrics, pilot participation, retention, credentials, programs, staff operations, readiness, and evaluation. Reports are descriptive and read-only. The frontend does not currently create charts or downloadable reports.

## Research Support

The platform uses private ASPN Participant IDs, platform activity events, program participation, credential data, and retention calculations to support pilot evaluation and future research. Staff-facing reporting avoids presenting causal claims. Research export foundations exist in the backend, but the current staff frontend does not provide export controls.

## Pilot Operations

Pilot Readiness answers whether essential operational conditions are present. Pilot Metrics summarizes what is happening. Pilot Evaluation summarizes how well the pilot is performing against backend-defined outcome logic. Operations Reporting shows staff activity recorded by the operational event system.

---

# Staff Navigation Structure

Staff navigation is rendered only after the frontend verifies backend staff access. The desktop sidebar groups 18 protected routes. Staff and admin accounts use this route set; member, educator, partner, and government roles do not receive staff access through the current authorization model.

## Overview

| Route | Screen | Purpose |
|---|---|---|
| `/staff` | Staff Dashboard | Top-level operational summary and shortcuts to staff work areas. |

## Administration

| Route | Screen | Purpose |
|---|---|---|
| `/staff/users` | User Management | Search and filter all platform users, review identity/status, activate or deactivate profiles, and manage review flags. |
| `/staff/youth-management` | Youth Management | Focused youth onboarding review using limited staff-managed profile fields. |

## Programs

| Route | Screen | Purpose |
|---|---|---|
| `/staff/program-management` | Program Management | Create, edit, filter, inspect, archive, restore, and review program-level totals. |

## Credentials

| Route | Screen | Purpose |
|---|---|---|
| `/staff/credential-management` | Credential Management | Manage credential definitions, analytics, status, program links, and manual youth awards. |

## Attendance

| Route | Screen | Purpose |
|---|---|---|
| `/staff/attendance-management` | Attendance Management | Create, filter, correct, and delete attendance records and review status totals. |

## Service Hours

| Route | Screen | Purpose |
|---|---|---|
| `/staff/service-hour-management` | Service Hour Management | Create, review, verify, reject, filter, and delete service-hour records. |

## Learning Administration

| Route | Screen | Purpose |
|---|---|---|
| `/staff/rwd-management` | RWD Management | Maintain externally hosted Global Civic Movements activity metadata and optional credential links. The staff-facing legacy label remains RWD. |

## Organizations

| Route | Screen | Purpose |
|---|---|---|
| `/staff/educators` | Educator Management | Maintain educator directory records, affiliations, status, and totals. |
| `/staff/partners` | Partner Management | Maintain partner organization contacts, type, status, notes, and totals. |
| `/staff/government` | Government Management | Maintain public-sector organizations, government levels, workforce/credential relationships, contacts, and totals. |

## Relationships

| Route | Screen | Purpose |
|---|---|---|
| `/staff/relationships` | Stakeholder Relationships | Create, update, delete, filter, and report on relationship notes and follow-up activity. |

## Reporting

| Route | Screen | Purpose |
|---|---|---|
| `/staff/reporting` | Pilot Reporting Dashboard | Participation, retention, credential, program, and staff-operation reporting. |
| `/staff/operations-reporting` | Operations Reporting | Staff operation totals and breakdowns by action, staff UID, and target type. |

## Pilot Operations

| Route | Screen | Purpose |
|---|---|---|
| `/staff/pilot-readiness` | Pilot Readiness Dashboard | Readiness score, status, blockers, warnings, metrics, and checklist. |
| `/staff/pilot-metrics` | Pilot Metrics Dashboard | Central pilot data collection view across users, programs, credentials, service, stakeholders, and operations. |
| `/staff/pilot-evaluation` | Pilot Evaluation Dashboard | Outcome scores, status, strengths, concerns, and recommended actions. |

## Metrics

| Route | Screen | Purpose |
|---|---|---|
| `/staff/metrics` | Platform Metrics | Read-only aggregate counts for youth, programs, enrollments, credentials, attendance, service hours, learning completion, and unread notifications. |

---

# Staff Dashboard

## Summary Cards

The Staff Dashboard displays:

- Total Youth.
- Active Youth.
- Active Programs.
- Credentials Awarded.
- Service-Hour Records Submitted.
- Attendance Records.
- Staff Operations in the last 30 days.

These values come from protected staff metrics and operations-reporting endpoints. Loading, access, and backend failure states are shown before operational content.

## Quick Actions and Navigation

The Staff Work Areas section provides direct buttons for Users, Programs, Credentials, Attendance, Service Hours, Educators, Partners, Government, Relationships, Pilot Readiness, Pilot Metrics, Pilot Evaluation, Reporting, and Operations Reporting. The persistent sidebar provides the same destinations grouped by operational domain.

The dashboard is a routing and status hub. It does not duplicate full management forms or detailed reporting tables.

---

# User Management

## All-User Administration

User Management supports server filters for role, active status, youth-profile status, and program participation. A local search covers name, email, ASPN Participant ID, and UID. Summary cards show total, active, youth, staff, educator, partner, and government users.

Selecting a user opens a detail panel with name, email, UID, ASPN Participant ID, role, profile status, school, and graduation year. Staff can activate or deactivate the profile and toggle `staffVerified` and `staffReviewRequired`.

The current screen lists recognized roles but does not provide a general role-editing control. It does not modify the Firebase UID or ASPN Participant ID.

## Youth Review

Youth Management provides a narrower onboarding review workflow. It lists youth profiles and exposes only profile status, staff-review-required, and staff-verified controls. It shows identity, school, graduation year, UID, and ASPN Participant ID for review while explicitly excluding youth-owned profile fields, role, UID, and public-profile settings from editing.

## Verification Process

The operational verification pattern is:

1. Select the youth record.
2. Review identity and onboarding information.
3. Set profile status to pending onboarding, active, or inactive.
4. Mark the profile verified or unverified.
5. Add or clear the staff-review requirement.

These are staff workflow flags. They do not replace Firebase Authentication status or create a public profile.

---

# Program Management

## Program Creation and Editing

Staff can create or update a program with name, description, category, leader, capacity, and program status. The screen lists programs and supports name search, category filtering, and active/archived filtering.

## Archive and Restore

Selected programs can be archived or restored through protected backend actions. Archived programs remain available to staff reporting but are unavailable in the active youth program catalog and youth enrollment flow.

## Program Detail and Enrollment Visibility

The detail panel displays enrollment count, credential count, active participants, attendance count, service-hour record count, and service-hour total when supplied by the backend. Program cards also show active participants, registrations/enrollments, and credential completions from pilot reporting.

The current Program Management screen emphasizes aggregate enrollment visibility. It does not present a full named enrollment roster or enrollment-removal workflow in this component.

## Program Analytics

Summary cards cover total, active, and archived programs plus enrollments and cross-collection totals. Program-level participation comparisons are descriptive and depend on current enrollment, credential, attendance, service-hour, and activity records.

---

# Credential Management

## Definition Management

Staff can create definitions with name, category, icon metadata, related program IDs, description, requirement text, and active status. Definitions can be filtered by category, active/archived state, and program ID.

The detail workflow supports viewing and editing staff-managed catalog fields, renaming, archiving, and restoring. Summary and grouped panels show definition totals, active and archived totals, credentials awarded, credential participation, definitions by category, awards by category, and awards by program.

## Manual Awards

The Award Credential form accepts a youth UID and credential definition ID. The backend verifies staff authorization, creates the earned credential, associates it with the youth record, and preserves duplicate-prevention behavior.

The form currently depends on typed identifiers rather than a searchable youth-and-credential picker.

## Auto-Award Support

Existing backend workflows can award eligible credentials from present attendance counts and linked Global Civic Movements completion. Manual awarding remains available. The current Credential Management UI does not expose the full auto-award configuration structure, requirement-type editor, or simulation tools.

The approved youth-facing credential icon registry is a frontend display system and is separate from the staff definition `icon` text field.

---

# Attendance Management

## Record Creation

Staff can create attendance records using youth UID, program ID, event name, event date, and attendance status. Supported statuses are present, absent, excused, and pending.

## Filtering, Correction, and Totals

Records can be filtered by youth UID, program ID, and event date. Each loaded record can be changed to any supported status or deleted. Summary cards show total records and totals for each status.

## Attendance-to-Credential Relationships

When staff records or updates qualifying attendance, existing backend logic may evaluate active `attendance_count` credential requirements for the relevant program. Only present attendance counts toward those rules. Award creation remains duplicate-protected.

The attendance screen does not preview which credential rules will be evaluated or show an award simulation before a status change.

---

# Service Hour Management

## Record Creation

Staff can create a service-hour record using youth UID, program ID, service date, hours, verification status, and description. Numeric entry supports quarter-hour increments.

## Verification Workflow

Records can be placed in pending, verified, or rejected status. Staff may update records through direct status actions. Verified records contribute to approved-hour totals and youth journey summaries.

## Filtering and Reporting

The screen filters by youth UID, verification status, program ID, and service date. Summary cards report record count, total hours, pending hours, verified hours, and rejected hours.

The current workflow displays a flat record list rather than a dedicated pending-review queue. Records can be deleted, so operational procedures are needed to avoid removing records that should instead retain an auditable rejected or corrected state.

The service-hour request form URL is supported elsewhere in the platform settings foundation, but this management component focuses on records and totals.

---

# Learning Administration

## Global Civic Movements Activity Setup

The staff RWD Management screen creates and updates activity metadata used by the youth Global Civic Movements page. Fields include country, title, description, external URL, active status, and optional associated credential definition ID. Learning media remains externally hosted.

## Activity Status

Staff can deactivate an active activity. The current list uses the public active-activity endpoint, so a deactivated activity disappears from this screen and cannot be selected for restoration through the existing UI.

## Progress Visibility

The current staff activity-management page does not show participant-level learning progress, quiz scores, pass rates, or completion rosters. Aggregate learning completion appears in platform, pilot, and evaluation reports where supported.

## Credential Linkage

Staff may enter an associated credential ID on an activity. Passing/completion award behavior remains enforced by existing backend logic. The UI uses a typed ID and does not provide credential catalog search.

---

# Organization Management

## Educators

Educator Management provides totals, name/email/title search, organization filtering, organization-type filtering, active/inactive filtering, a selectable directory, detail editing, creation, activation, and deactivation.

## Partner Organizations

Partner Management supports organization-name search, organization-type and active-status filters, contact and website information, notes, creation, editing, activation, deactivation, and totals by status/type.

## Government Organizations

Government Management supports filters for organization name, government level, organization type, active status, workforce-partner status, and credential-partner status. Staff can maintain contact details, notes, public-sector classifications, relationship flags, activation status, and aggregate totals.

## Relationship to Youth Workflows

These directories are staff-operational records. They do not create educator, partner, or government portals and do not expose organization directories to youth. Their current connection to youth programming is organizational and reporting context rather than direct opportunity matching or account access.

---

# Stakeholder Management

## Relationship Notes

Staff can create, update, filter, search, and delete relationship notes for educators, partner organizations, and government organizations. Notes include stakeholder type and ID, stakeholder name, relationship status, owner UID, contact and follow-up dates, active status, and note text.

Supported relationship stages include prospect, contacted, meeting scheduled, active partner, inactive partner, and declined. When an owner is not supplied, the backend can default ownership to the verified staff UID.

## Relationship Reporting

The Relationships screen combines management and reporting. It displays total, active, and inactive notes; upcoming and overdue follow-ups; breakdowns by stakeholder type and relationship status; ownership counts; and follow-up lists.

## Current Use Cases

- Record partner-development context.
- Assign relationship ownership.
- Track last contact and next follow-up.
- Identify overdue outreach.
- Review the stakeholder pipeline across the three external directories.

This is a lightweight internal relationship layer, not a messaging system or third-party CRM integration.

---

# Pilot Operations

## Pilot Readiness Dashboard

Pilot Readiness reports a score from 0 to 100 and a status of ready, caution, or not ready. It shows generated time, youth/profile/program/credential/participation/stakeholder/activity metrics, blockers, warnings, and a categorized readiness checklist.

Staff should use it to identify missing operational prerequisites. It is a current-data indicator, not a substitute for staff sign-off, safeguarding review, Firebase validation, or manual QA.

## Pilot Metrics Dashboard

Pilot Metrics centralizes:

- Registration funnel and recent activity.
- Program enrollment, active participation, and attendance.
- Credential definitions and awards by category/program.
- Service submissions and approved hours by program.
- Educator, partner, government, and relationship-note activity.
- Staff operations over 30, 60, and 90 days.

It answers what is happening in the pilot using existing operational data.

## Pilot Evaluation Dashboard

Pilot Evaluation reports an overall score and green/yellow/red status across youth, program, credential, service, stakeholder, and operations outcomes. It provides strengths, concerns, and recommended actions from backend-defined descriptive logic.

It does not establish causality, replace formal research analysis, or independently validate data quality.

## Pilot Reporting Dashboard

Reporting combines registered youth, profile completion, program and credential participation, 30/60/90-day activity and retention, credentials by category, program registrations/completions/active participants, and staff operations.

## Operations Reporting

Operations Reporting summarizes total staff operations; operations in the last 30, 60, and 90 days; and breakdowns by operation type, staff UID, and target type. It depends on actions that currently emit staff-operation events and may not represent every manual or historical action.

## Platform Metrics

The separate Metrics route shows simple aggregate counts for total and active youth, active programs, enrollments, earned credentials, attendance records, service-hour records, completed RWD activities, and unread notifications.

---

# Security Model

## Staff and Admin Access

All staff screens require a signed-in Firebase user. Before rendering a staff route, the frontend calls the protected staff metrics endpoint as an authorization check. A successful response enables the staff navigation shell.

Every staff API request sends:

```text
Authorization: Bearer <Firebase ID token>
```

The backend remains the source of truth and applies `AuthService.requireStaff(...)`. Current allowed Firestore roles are `staff` and `admin`.

## Denied Roles

Youth/member users receive access denied when attempting staff routes. Reserved `educator`, `partner`, and `government` roles do not inherit staff access. The frontend does not grant access based only on locally interpreted role data.

## Protected Data

Staff routes can expose internal UIDs, participant IDs, contact information, review flags, attendance, service, and relationship notes. These views are operationally sensitive and must remain behind backend authorization. Raw Firebase tokens are not displayed in the UI.

## Current Assumptions

- Firebase Authentication establishes identity.
- Firestore profile role data establishes staff/admin authorization through the backend.
- The active MVP Firestore database is `(default)`.
- Staff accounts and roles are provisioned through controlled operational setup.
- Frontend gating improves usability but does not replace backend enforcement.

---

# Design System Snapshot

## Current Visual Design

Staff pages use the original quiet operational design: a dark fixed sidebar, light workspace, compact page introductions, bordered white panels, restrained status colors, and dense but organized records. The recent dark-navy youth refresh does not currently apply to staff modules.

## Navigation

Desktop navigation uses grouped text links in a 280-pixel sticky sidebar. The sidebar contains Staff Workspace context plus Overview, Youth, Programs, Credentials, Participation, Organizations, Stakeholders, Reports, and Pilot Tools groups.

Unlike youth users, staff do not receive a mobile bottom navigation bar.

## Tables and Record Lists

Most management screens use selectable record cards and detail panels rather than large tables. Formal tables are used for structured readiness and reporting data and are wrapped in horizontal scroll containers. Mini tables present grouped counts without chart libraries.

## Forms

Staff forms use compact inputs, selects, textareas, checkboxes, and date/number controls. Common patterns place filters and creation forms in one panel and records/detail in another. Success and error messages appear near the page introduction.

Several cross-record operations require staff to type UIDs, program IDs, or credential IDs. This keeps the current UI simple but increases operational friction and input-error risk.

## Metrics Cards

Summary grids use repeated compact cards with a prominent value and short label. Cards collapse responsively and support zero-data states without charts.

## Mobile Considerations

At 860 pixels and below, the application shell becomes one column, the staff management grid stacks, and the sidebar becomes part of the document flow. Staff must scroll through the full navigation before reaching content. Tables can scroll horizontally, but record-heavy forms and long navigation have not received a dedicated staff-mobile redesign.

---

# Current Strengths

## Administrative Strengths

- Major pilot records can be managed without routine direct Firestore editing.
- Active/archive patterns preserve catalog records for programs and credentials.
- Focused youth review limits staff changes to appropriate onboarding fields.
- Organization directories and relationship notes centralize external stakeholder context.
- Shared loading, empty, success, error, summary, and record-detail patterns make modules reasonably predictable.

## Operational Strengths

- Attendance, service hours, programs, credentials, and users have protected operational workflows.
- Status totals and filters support day-to-day pilot review.
- The Staff Dashboard provides direct access to major work areas.
- Staff-operation event reporting improves accountability for supported actions.
- Readiness blockers and warnings translate platform data into operational attention items.

## Reporting Strengths

- Reporting separates readiness, current metrics, outcome evaluation, platform totals, and staff operations.
- Program and credential analytics are available alongside management workflows.
- Zero-data states are handled without inventing values.
- Reporting remains descriptive and avoids unsupported causal claims.

## Research Strengths

- ASPN Participant IDs support private longitudinal linkage distinct from Firebase UIDs.
- Platform events support active-user and retention calculations.
- Pilot metrics align participation, credential, program, service, stakeholder, and operations data.
- External dataset linkage and research export foundations exist in the backend without exposing export UI prematurely.

---

# Current UX Gaps

This section documents present limitations only; it does not propose a redesign.

## Navigation Complexity

- Eighteen staff routes create a long sidebar with overlapping reporting concepts.
- User Management and Youth Management overlap, and the distinction may not be obvious without training.
- Metrics, Reporting, Pilot Metrics, Pilot Evaluation, Pilot Readiness, and Operations Reporting require staff to understand their different purposes.
- Staff pages retain the `RWD` label while youth pages use Global Civic Movements.
- Mobile staff users must scroll through the full sidebar because there is no condensed staff navigation.

## Workflow Friction

- Credential awards and several record-creation forms require typed youth UIDs and record IDs.
- Program enrollment visibility is aggregate in the current main program component rather than a full roster workflow.
- The service-hour screen has no dedicated pending-review queue.
- Deactivated RWD activities disappear from the active-only list and cannot be restored through the current screen.
- Credential auto-award configuration is not fully manageable in the current staff UI.
- Relationship owner reporting displays UIDs rather than resolved staff names.
- Repeated filter forms do not consistently preserve or communicate active filters after reload.

## Reporting Limitations

- Reports use cards and simple tables only; no charts, exports, saved views, or print layouts are available.
- Most aggregation occurs over current Firestore data and is designed for pilot scale.
- Readiness and evaluation scores depend on backend thresholds and data completeness; the UI does not expose formula details for every score.
- Staff-operation reports cover tracked actions, not necessarily all historical or externally performed changes.
- Descriptive correlation and retention measures do not establish causality.
- Cross-dashboard metric names and denominators require operational documentation to prevent misinterpretation.

## Operational Risks

- Attendance, service-hour, and relationship-note screens include deletion actions; careless deletion can reduce auditability.
- Free-text IDs and categories can create inconsistent data or failed actions.
- Staff contact and youth information appear together in operational screens and require appropriate device and session controls.
- The frontend staff gate depends on the metrics endpoint being available, so a metrics failure can block access to otherwise healthy staff modules.
- Direct changes made outside the platform may not produce staff-operation events.
- Active/inactive and archived conventions differ across record types and need staff training.
- Dynamic messages are visible but are not consistently announced through accessibility live regions.
- Dedicated keyboard, screen-reader, high-contrast, zoom, Safari, Firefox, Edge, and mobile staff QA remains necessary.

---

# Snapshot Boundary

This document records the staff frontend as implemented through Checkpoint 30F and documented at Checkpoint 30H. It does not change routes, permissions, Firestore structure, metrics, thresholds, operational logic, or pilot approval status.
