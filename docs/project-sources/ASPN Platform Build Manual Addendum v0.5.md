# ASPN Platform Build Manual Addendum v0.5

## Purpose

This addendum records the ASPN Platform work completed after the v0.4 pilot-preparation baseline. Version 0.5 marks the platform's transition from core feature construction into pilot operations readiness.

Current platform status: **Pilot-Capable**  
Completed through: **Checkpoint 26C — Pilot Evaluation Dashboard**  
Latest checkpoint baseline: **32ee9fc — Checkpoint 26C: add pilot evaluation dashboard**

## Checkpoints Completed Since v0.4

### 25A — Educator Management Foundation

ASPN staff can manage educator and school-contact records through protected staff/admin tooling.

Primary capabilities:
- Create educator records
- List and filter educators
- View educator detail
- Update educator records
- Activate or deactivate educator relationships
- View educator totals

Firestore collection:
- `educators`

### 25B — Partner Organization Foundation

ASPN staff can manage nonprofit, community, business, foundation, workforce, higher education, faith-based, and other partner organizations.

Primary capabilities:
- Create partner organization records
- List and filter partners
- View partner organization detail
- Update partner organizations
- Activate or deactivate partner organizations
- View partner totals

Firestore collection:
- `partnerOrganizations`

### 25C — Government Organization Foundation

ASPN staff can manage government and public-sector organizations relevant to civic, workforce, credential, and Future Ready strategy.

Primary capabilities:
- Create government organization records
- Track government level
- Track organization type
- Mark workforce partner status
- Mark credential partner status
- Activate or deactivate organizations
- View government organization totals

Firestore collection:
- `governmentOrganizations`

### 25D — Stakeholder Relationship Notes Foundation

ASPN staff can track relationship history and follow-up activity across educator, partner, and government organization directories.

Primary capabilities:
- Create relationship notes
- Track stakeholder type and stakeholder ID
- Track relationship status
- Track relationship owner UID
- Track last contact date
- Track next follow-up date
- Update or delete notes

Firestore collection:
- `stakeholderRelationshipNotes`

### 25E — Stakeholder Relationship Reporting

The relationship notes layer now supports staff-facing reporting for the external stakeholder pipeline.

Primary capabilities:
- Relationship pipeline summary
- Notes by stakeholder type
- Notes by relationship status
- Notes by staff owner UID
- Upcoming follow-ups
- Overdue follow-ups
- Follow-ups by stakeholder type

### 26A — Pilot Readiness Dashboard

The platform now includes a staff-only dashboard that answers: **Are we ready?**

Primary capabilities:
- Readiness score
- Readiness status
- Blockers
- Warnings
- Readiness checklist
- Pilot readiness metrics

Frontend route:
- `/staff/pilot-readiness`

Backend endpoint:
- `GET /api/staff/pilot/readiness`

### 26B — Pilot Metrics Dashboard

The platform now includes a staff-only dashboard that answers: **What is happening?**

Primary capabilities:
- Registration funnel
- Program engagement
- Credential engagement
- Service-hour engagement
- Stakeholder engagement
- Operational activity

Frontend route:
- `/staff/pilot-metrics`

Backend endpoint:
- `GET /api/staff/pilot/metrics`

### 26C — Pilot Evaluation Dashboard

The platform now includes a staff-only dashboard that answers: **How well is the pilot performing?**

Primary capabilities:
- Overall evaluation score
- Overall evaluation status
- Youth outcome score
- Program outcome score
- Credential outcome score
- Service outcome score
- Stakeholder outcome score
- Operations outcome score
- Strengths
- Concerns
- Recommended actions

Frontend route:
- `/staff/pilot-evaluation`

Backend endpoint:
- `GET /api/staff/pilot/evaluation`

## Architecture Summary

Backend:
- Spring Boot
- Firebase Admin SDK
- Firestore
- Staff/admin authorization through `AuthService.requireStaff(...)`

Frontend:
- React
- Vite
- Firebase Authentication
- Staff routes under `/staff/*`

Active Firestore database:
- `(default)`

Legacy Firestore database:
- `aspirationnetworkusers`
- Do not use for active MVP or pilot operations unless a future audit explicitly approves it.

## Current Platform Status

The ASPN Platform is now **pilot-capable** for a controlled youth pilot with staff oversight.

The platform supports:
- Youth accounts and private profiles
- Staff user management
- Programs and enrollments
- Credentials
- Attendance
- Service hours
- RWD learning
- Notifications
- Staff operations reporting
- Educator management
- Partner organization management
- Government organization management
- Stakeholder relationship notes
- Pilot readiness reporting
- Pilot metrics reporting
- Pilot evaluation reporting

## Remaining Major Areas

Recommended next work:
- Documentation package v0.5
- Production Readiness Review
- Staff pilot operations QA
- Mobile optimization
- UX/UI refresh
- Design system
- Pilot launch execution

