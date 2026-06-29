# ASPN Platform Build Manual Addendum v0.5 Expanded

## Executive Summary

ASPN Platform v0.5 extends the v0.4 pilot-preparation baseline into a pilot operations platform. The major shift in this version is from core youth/staff feature construction toward operational readiness, relationship management, and pilot measurement.

Version 0.5 includes three major work streams:
- External stakeholder infrastructure
- Staff relationship reporting
- Pilot readiness, metrics, and evaluation dashboards

The platform is now classified as **Pilot-Capable**. It is not yet approved for broad public launch.

## Platform Evolution Since v0.4

The v0.4 baseline completed youth MVP, staff/admin MVP, legacy endpoint security remediation, and Firebase staging validation. Version 0.5 added the operational infrastructure needed for ASPN staff to prepare for, manage, and monitor a controlled pilot.

Completed checkpoints:
- 25A — Educator Management Foundation
- 25B — Partner Organization Foundation
- 25C — Government Organization Foundation
- 25D — Stakeholder Relationship Notes Foundation
- 25E — Stakeholder Relationship Reporting
- 26A — Pilot Readiness Dashboard
- 26B — Pilot Metrics Dashboard
- 26C — Pilot Evaluation Dashboard

## Current Platform Scope

The current platform supports youth development, credentialing, program participation, service-hour tracking, RWD learning progress, notifications, staff management, stakeholder management, and pilot operations reporting.

The platform still deliberately avoids:
- Public youth profiles
- Youth public search
- Direct messaging
- Follower systems
- Popularity mechanics
- Social media-style engagement loops
- Public launch tooling
- CRM integration
- Export generation
- External matching engines

## Feature Descriptions

### Educator Management

Educator management gives ASPN staff a protected internal directory for school, nonprofit, government, higher education, and other educational contacts.

Primary fields:
- educator ID
- name
- email
- phone
- title
- organization name
- organization type
- active status
- notes
- timestamps

Staff workflow:
1. Staff opens `/staff/educators`.
2. Staff creates or selects an educator record.
3. Staff updates contact, organization, or notes fields.
4. Staff activates or deactivates the relationship as needed.

### Partner Organization Management

Partner organization management provides a staff-managed directory for organizations that may support youth development, civic learning, funding, workforce readiness, or community programming.

Supported organization types include:
- nonprofit
- foundation
- workforce
- business
- higher education
- faith-based
- government-affiliated
- community
- other

### Government Organization Management

Government organization management supports ASPN's long-term public-sector and Future Ready strategy.

Supported government levels:
- municipal
- county
- regional
- state
- federal
- international

Supported organization types:
- city government
- county government
- state agency
- school district
- workforce board
- public authority
- public university
- other

Additional flags:
- workforce partner
- credential partner

### Stakeholder Relationship Notes

Relationship notes connect conceptually across educator, partner organization, and government organization directories. Notes are staff-only and support lightweight relationship tracking without creating portals, messaging, or CRM dependencies.

Supported stakeholder types:
- `educator`
- `partner_organization`
- `government_organization`

Supported relationship statuses:
- `prospect`
- `contacted`
- `meeting_scheduled`
- `active_partner`
- `inactive_partner`
- `declined`

### Stakeholder Relationship Reporting

Stakeholder reporting summarizes the relationship pipeline:
- total notes
- active notes
- inactive notes
- notes by stakeholder type
- notes by relationship status
- notes by staff owner UID
- upcoming follow-ups
- overdue follow-ups
- upcoming follow-ups by stakeholder type
- overdue follow-ups by stakeholder type

### Pilot Readiness Dashboard

Pilot readiness answers whether the platform appears operationally ready for controlled pilot launch preparation.

Readiness status values:
- `ready`
- `caution`
- `not_ready`

Core outputs:
- readiness score
- readiness status
- generated timestamp
- readiness metrics
- blockers
- warnings
- checklist items

### Pilot Metrics Dashboard

Pilot metrics centralizes current pilot measurement. It reports what is happening across youth, program, credential, service, stakeholder, and staff operation activity.

Metric groups:
- registration funnel
- program engagement
- credential engagement
- service engagement
- stakeholder engagement
- operational activity

### Pilot Evaluation Dashboard

Pilot evaluation summarizes how well the pilot is performing using outcome categories.

Outcome categories:
- youth outcomes
- program outcomes
- credential outcomes
- service outcomes
- stakeholder outcomes
- operations outcomes

Evaluation status values:
- `green`
- `yellow`
- `red`

The evaluation dashboard is descriptive only. It does not perform causal inference.

## Endpoint Inventory

### Educators

- `GET /api/staff/educators`
- `GET /api/staff/educators/{educatorId}`
- `POST /api/staff/educators`
- `PATCH /api/staff/educators/{educatorId}`
- `PATCH /api/staff/educators/{educatorId}/activate`
- `PATCH /api/staff/educators/{educatorId}/deactivate`
- `GET /api/staff/educators/totals`

### Partner Organizations

- `GET /api/staff/partners`
- `GET /api/staff/partners/{partnerOrganizationId}`
- `POST /api/staff/partners`
- `PATCH /api/staff/partners/{partnerOrganizationId}`
- `PATCH /api/staff/partners/{partnerOrganizationId}/activate`
- `PATCH /api/staff/partners/{partnerOrganizationId}/deactivate`
- `GET /api/staff/partners/totals`

### Government Organizations

- `GET /api/staff/government-organizations`
- `GET /api/staff/government-organizations/{governmentOrganizationId}`
- `POST /api/staff/government-organizations`
- `PATCH /api/staff/government-organizations/{governmentOrganizationId}`
- `PATCH /api/staff/government-organizations/{governmentOrganizationId}/activate`
- `PATCH /api/staff/government-organizations/{governmentOrganizationId}/deactivate`
- `GET /api/staff/government-organizations/totals`

### Stakeholder Relationship Notes

- `GET /api/staff/stakeholders/notes`
- `GET /api/staff/stakeholders/notes/{stakeholderRelationshipNoteId}`
- `POST /api/staff/stakeholders/notes`
- `PATCH /api/staff/stakeholders/notes/{stakeholderRelationshipNoteId}`
- `DELETE /api/staff/stakeholders/notes/{stakeholderRelationshipNoteId}`
- `GET /api/staff/stakeholders/notes/totals`

### Pilot Operations

- `GET /api/staff/pilot/readiness`
- `GET /api/staff/pilot/metrics`
- `GET /api/staff/pilot/evaluation`

All staff endpoints require:
- Firebase bearer token
- `AuthService.requireStaff(...)`

Allowed roles:
- `staff`
- `admin`

## Collection Inventory

Core platform:
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

Analytics and operations:
- `platformEvents`
- `staffOperationEvents`
- `externalDatasets`
- `participantExternalLinks`

Stakeholder infrastructure:
- `educators`
- `partnerOrganizations`
- `governmentOrganizations`
- `stakeholderRelationshipNotes`

## Workflow Descriptions

### Staff Relationship Workflow

1. Staff creates an educator, partner, or government organization record.
2. Staff opens the relationship notes area.
3. Staff creates a note linked to the stakeholder type and ID.
4. Staff records status, owner, last contact, and next follow-up date.
5. Staff reviews upcoming and overdue follow-up summaries.

### Pilot Readiness Workflow

1. Staff opens `/staff/pilot-readiness`.
2. The backend aggregates existing platform data.
3. The dashboard displays score, status, blockers, warnings, and checklist items.
4. Staff uses the results to identify launch-preparation gaps.

### Pilot Metrics Workflow

1. Staff opens `/staff/pilot-metrics`.
2. The backend aggregates current platform activity.
3. Staff reviews registration, program, credential, service, stakeholder, and operations metrics.

### Pilot Evaluation Workflow

1. Staff opens `/staff/pilot-evaluation`.
2. The backend calculates outcome-category scores.
3. Staff reviews overall status, strengths, concerns, and recommended actions.
4. Results inform pilot monitoring and staff operations decisions.

## Operational Readiness Details

The platform is ready for pilot launch preparation when:
- staff/admin accounts are configured
- active programs exist
- active credential definitions exist
- youth account creation and profile completion are validated
- staff workflows are validated
- pilot dashboards load
- Firebase Authentication is enabled
- Firestore `(default)` is confirmed as the active database
- legacy public endpoints remain disabled
- production readiness review is complete

## Current Platform Status

Status: **Pilot-Capable**  
Public launch: **Not approved**  
Next recommended phase: **Documentation v0.5, Production Readiness Review, Staff QA, Mobile Optimization, UX/UI Refresh**

