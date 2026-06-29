# ASPN Platform Reporting and Staff Workflow Clarity Review

**Version:** Checkpoint 31G  
**Date:** June 23, 2026  
**Scope:** Staff Dashboard, Reporting, Operations Reporting, Platform Metrics, Pilot Metrics, Pilot Evaluation, Pilot Readiness, staff workflow discoverability, and copy-only clarity improvements.

---

# 1. Executive Summary

The ASPN staff reporting system is **Ready with Setup** for a controlled pilot, but it depends on staff understanding which dashboard answers which question. The existing reporting destinations are functionally distinct, yet their names overlap enough that new staff may confuse “Metrics,” “Reporting,” “Pilot Metrics,” “Pilot Evaluation,” and “Pilot Readiness.”

31G reduced that confusion with small copy-only changes to page introductions and the Staff Dashboard work-area description. No dashboard calculations, backend reporting logic, Firestore architecture, metrics, permissions, or layouts were changed.

Primary clarity recommendation:

- Treat **Staff Dashboard** as the starting point.
- Treat **Pilot Readiness** as the pre-launch blocker/warning check.
- Treat **Platform Metrics** as simple current totals.
- Treat **Reporting** as cross-pilot participation/program/credential/retention reporting.
- Treat **Operations Reporting** as the staff/admin action log.
- Treat **Pilot Metrics** as pilot engagement measurement.
- Treat **Pilot Evaluation** as outcome interpretation and recommended actions.

---

# 2. Reporting Destinations Reviewed

## Staff Dashboard

**Status:** Ready with Setup

Current purpose:

- A landing page for staff/admin users.
- Gives a quick operational snapshot.
- Provides shortcuts into record-management and reporting work areas.

Strengths:

- Strong starting point for staff.
- Shows high-level counts for youth, programs, credentials, service hours, attendance, and recent staff operations.
- Gives direct access to operational modules.

Missing guidance:

- It does not prioritize urgent work queues such as pending service hours, required youth review, overdue follow-ups, or today’s attendance.
- It previously did not clearly explain when to use each reporting destination.

What staff should do after landing:

1. Check high-level counts for obvious setup gaps.
2. Open the management module if they need to create or correct records.
3. Open Pilot Readiness before launch.
4. Open Reporting/Pilot Metrics/Pilot Evaluation depending on whether they need participation, engagement, or outcome interpretation.

31G copy change:

- The dashboard now explains that staff should start with the snapshot and then open the specific module/report that matches their question.
- The Staff Work Areas description now distinguishes Readiness, Reporting, Metrics, Pilot Metrics, Evaluation, and Operations Reporting.

## Reporting

**Status:** Ready with Setup

Questions it answers:

- How many youth are registered?
- How complete are youth profiles?
- How much program participation exists?
- How much credential participation exists?
- What does retention look like?
- Which programs have registrations, active participants, and credential completions?
- What staff operations are appearing alongside participation data?

Use Reporting when staff need a combined read-only view of:

- participation
- enrollment/program results
- credentials
- service and retention context
- staff operations summary

31G copy change:

- Reporting now explicitly says it answers cross-pilot questions about participation, retention, credentials, program outcomes, service, and staff operations.

## Operations Reporting

**Status:** Ready with Setup

Questions it answers:

- What staff/admin actions have been recorded?
- Which action types are most common?
- Which staff UIDs performed recorded actions?
- What target types are being changed?
- How much operational activity occurred in the last 30, 60, and 90 days?

Use Operations Reporting when staff need an activity-log style view of platform operations.

Important limitation:

- It reflects tracked platform actions, not every external, historical, or direct Firestore change.

31G copy change:

- Operations Reporting now explicitly says it reviews staff/admin actions by action type, staff UID, target type, and recent activity window.

## Platform Metrics

**Status:** Ready with Setup

Questions it answers:

- How many total youth users exist?
- How many active youth users exist?
- How many active programs exist?
- How many enrollments exist?
- How many credentials have been earned?
- How many attendance and service-hour records exist?
- How many learning completions and unread notifications exist?

Use Platform Metrics when staff need simple current totals.

31G copy change:

- Platform Metrics now describes itself as simple current totals across users, programs, credentials, attendance, service hours, learning, and notifications.

## Pilot Metrics

**Status:** Ready with Setup

Questions it answers:

- What is happening in the pilot?
- Are youth registering and completing profiles?
- Are youth active in the last 30/60/90 days?
- Are programs, credentials, service hours, stakeholders, and operations showing activity?
- Which programs have participation?

Use Pilot Metrics for pilot engagement measurement.

Important limitation:

- Pilot Metrics is measurement, not causal evaluation.

31G copy change:

- Pilot Metrics now names the engagement domains it monitors and repeats that it is not causal evaluation.

## Pilot Evaluation

**Status:** Ready with Setup

Questions it answers:

- How effective does the pilot appear based on current outcome indicators?
- What are the youth, program, credential, service, stakeholder, and operations outcome scores?
- What strengths are emerging?
- What concerns require attention?
- What recommended actions does the backend return?

Use Pilot Evaluation when staff or leadership need outcome interpretation rather than raw counts.

Important limitation:

- It is descriptive evaluation support, not causal analysis.

31G copy change:

- Pilot Evaluation now explicitly says it is for effectiveness, outcome scores, strengths, concerns, and recommended actions.

## Pilot Readiness

**Status:** Ready with Setup

Questions it answers:

- Is the platform ready to launch?
- What launch blockers exist?
- What warnings should staff address?
- What is the readiness score?
- Which operational checklist items are complete?

Use Pilot Readiness before onboarding youth or before a launch review.

31G copy change:

- Pilot Readiness now explicitly says it checks blockers, warnings, readiness score, and the operational checklist before launch.

---

# 3. Workflow Mapping

| When staff want to know… | Open… | Why |
|---|---|---|
| Participation | Reporting | Shows registered youth, profile completion, program participation, credential participation, and retention context. |
| Program Outcomes | Reporting or Pilot Metrics | Reporting gives program registrations/active participants/credential completions; Pilot Metrics gives broader engagement by program. |
| Launch Readiness | Pilot Readiness | Shows blockers, warnings, readiness score, and readiness checklist. |
| Platform Activity | Operations Reporting | Shows recorded staff/admin operational actions and recent activity windows. |
| Pilot Success | Pilot Evaluation | Shows outcome scores, strengths, concerns, and recommended actions. |
| Operational Health | Staff Dashboard first, then Pilot Readiness or Operations Reporting | Dashboard gives the quick snapshot; Readiness shows launch health; Operations shows staff activity health. |
| Simple Current Counts | Platform Metrics | Gives total users, programs, enrollments, credentials, attendance, service hours, learning, and unread notifications. |
| Staff/Admin Action History | Operations Reporting | Shows operation counts by type, staff UID, and target type. |
| Pilot Engagement | Pilot Metrics | Shows registration, program, credential, service, stakeholder, and operations engagement. |
| Evaluation Narrative | Pilot Evaluation | Shows interpreted strengths, concerns, and recommended actions. |

---

# 4. UI Label Review

| Issue | Classification | Finding | Recommendation / Status |
|---|---|---|---|
| Reporting destination names overlap | Important Before Pilot | “Reporting,” “Metrics,” and “Pilot Metrics” sound similar to new staff. | Copy-only page introductions updated in 31G. Staff training still required. |
| Operations Reporting appears near Pilot Tools | Recommended During Pilot | Operations Reporting is operational/log-oriented, not a pilot outcome dashboard, though it supports pilot oversight. | Keep for now; explain in guide. Future nav grouping can be reviewed after pilot. |
| Platform Metrics page state messages used “staff dashboard” wording | Recommended During Pilot | Loading/error labels may make Platform Metrics feel like Staff Dashboard. | Documented as remaining minor copy risk; not changed beyond page intro. |
| Staff Dashboard does not indicate immediate next action | Important Before Pilot | Staff may see counts but not know where to go next. | Staff Dashboard intro and work-area description updated. |
| Pilot Evaluation vs Pilot Metrics | Important Before Pilot | Staff may confuse measurement with interpretation. | Pilot Metrics and Pilot Evaluation intros now distinguish measurement vs evaluation. |
| Pilot Readiness vs Pilot Evaluation | Important Before Pilot | Staff may confuse launch readiness with pilot effectiveness. | Pilot Readiness intro now clearly says pre-launch blockers/warnings/checklist. |
| Reporting includes staff operations summary | Recommended During Pilot | This can overlap with Operations Reporting. | Reporting intro now frames operations as part of cross-pilot reporting; Operations Reporting remains the detailed action-log view. |

---

# 5. Clarity Improvements Applied

Files modified:

- `frontend/src/components/StaffDashboard.jsx`
- `frontend/src/components/PilotReportingDashboard.jsx`
- `frontend/src/components/OperationsReporting.jsx`
- `frontend/src/components/StaffMetricsDashboard.jsx`
- `frontend/src/components/PilotMetricsDashboard.jsx`
- `frontend/src/components/PilotEvaluationDashboard.jsx`
- `frontend/src/components/PilotReadinessDashboard.jsx`

Copy-only improvements:

1. Staff Dashboard now says it is the starting point for a snapshot and staff should open the module/report matching their question.
2. Staff Work Areas now names which dashboard to use for launch blockers, participation, simple totals, engagement, outcomes, and staff activity.
3. Reporting now identifies cross-pilot participation, retention, credential, program outcome, service, and staff operations questions.
4. Operations Reporting now identifies recorded staff/admin actions by action type, staff UID, target type, and recent activity window.
5. Platform Metrics now identifies simple current totals across core platform records.
6. Pilot Metrics now identifies pilot engagement/activity measurement and distinguishes itself from causal evaluation.
7. Pilot Evaluation now identifies effectiveness, outcome scores, strengths, concerns, and recommended actions.
8. Pilot Readiness now identifies blockers, warnings, readiness score, and operational checklist before launch.

No layout, calculation, backend, metric, Firestore, authentication, or role-permission changes were made.

---

# 6. One-Page Staff Reporting Guide

## If I need X information, where do I go?

| I need to know… | Go to… |
|---|---|
| “What should I check first today?” | Staff Dashboard |
| “Are we ready to launch?” | Pilot Readiness |
| “What launch blockers or warnings exist?” | Pilot Readiness |
| “How many youth, programs, credentials, attendance records, or service-hour records exist?” | Platform Metrics |
| “How are participation, profile completion, retention, credentials, and programs doing together?” | Reporting |
| “Which programs have registrations, active participants, or credential completions?” | Reporting |
| “What is happening across the pilot right now?” | Pilot Metrics |
| “How engaged are youth with programs, credentials, service, and operations?” | Pilot Metrics |
| “Is the pilot working?” | Pilot Evaluation |
| “What are our strengths, concerns, and recommended actions?” | Pilot Evaluation |
| “What staff/admin actions were recorded?” | Operations Reporting |
| “Which staff UID performed recorded operations?” | Operations Reporting |
| “Do I need to create or fix a record?” | Open the matching management module, not a report. |

## Short definitions

- **Staff Dashboard:** first stop; quick snapshot and shortcuts.
- **Platform Metrics:** simple current totals.
- **Reporting:** combined participation, retention, program, credential, and operations reporting.
- **Operations Reporting:** staff/admin action log and operational activity.
- **Pilot Metrics:** pilot engagement measurement.
- **Pilot Evaluation:** outcome interpretation and recommended actions.
- **Pilot Readiness:** pre-launch blockers, warnings, score, and checklist.

---

# 7. Remaining Staff Workflow Risks

| Risk | Classification | Recommended Handling |
|---|---|---|
| Staff may still confuse Reporting, Metrics, and Pilot Metrics without orientation. | Important Before Pilot | Include the one-page guide in staff training. |
| Staff may treat Pilot Evaluation as causal proof. | Important Before Pilot | Train staff that evaluation is descriptive support, not causal analysis. |
| Staff may expect Operations Reporting to include changes made outside the platform. | Important Before Pilot | Train staff that it covers tracked platform actions only. |
| Staff Dashboard still does not surface urgent queues. | Recommended During Pilot | Monitor pilot support needs; consider queue cards in a future checkpoint. |
| Staff UIDs are less readable than staff names. | Recommended During Pilot | Use training for pilot; consider name resolution later if supported. |
| Long reporting pages remain dense for new staff. | Recommended During Pilot | Use laptop/tablet workflows and orientation. |
| Reporting definitions, formulas, thresholds, and denominators need operational sign-off. | Important Before Pilot | Include definitions in staff runbook and launch training. |

---

# 8. Pilot Readiness Assessment

**Assessment:** Ready with Setup.

31G improves staff comprehension without changing reporting scope. The reporting destinations are now more self-explanatory at the page level, and the guide provides a simple route map for staff training.

Before pilot onboarding, ASPN should:

1. Walk staff through the one-page reporting guide.
2. Confirm which dashboard is the source of truth for each recurring staff meeting question.
3. Explain that Pilot Metrics measures activity while Pilot Evaluation interprets outcomes.
4. Explain that Operations Reporting tracks platform-recorded operations, not every external action.
5. Keep record creation/correction in management modules, not reports.

Recommended next checkpoint: **31H — Youth Navigation and Visual Consistency Review**.
