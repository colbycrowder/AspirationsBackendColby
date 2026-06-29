# ASPN Platform Pilot Operations Runbook v0.5

## Purpose

This runbook provides operating guidance for ASPN staff running the first controlled ASPN Platform pilot cohort after completion of Checkpoint 26C.

The platform status at the time of this runbook is:

- Latest committed checkpoint: `32ee9fc — Checkpoint 26C: add pilot evaluation dashboard`
- Production Readiness Review status:
  - Controlled Pilot QA: Ready
  - Controlled Pilot Launch: Ready with conditions
  - Public Launch: Not yet ready
- Active Firestore database: `(default)`
- Legacy Firestore database: `aspirationnetworkusers`

This runbook is intended for staff operations. It does not approve public launch, public youth discovery, social networking, messaging, or unsupervised youth-to-youth interaction.

## Executive Summary

The ASPN Platform is pilot-capable for a controlled youth cohort with staff oversight. Staff can manage youth users, programs, enrollments, credentials, attendance, service hours, stakeholder directories, relationship notes, pilot readiness, pilot metrics, and pilot evaluation.

The first pilot should be run as a supervised operating cycle:

1. Prepare staff/admin accounts.
2. Seed programs, credentials, RWD activities, and service-hour request settings.
3. Validate dashboards with test accounts.
4. Onboard youth in a controlled group.
5. Review dashboards weekly.
6. Correct data quality issues quickly.
7. Use pilot metrics and evaluation dashboards to guide staff follow-up.

The platform should not be treated as a public launch product until production readiness, mobile/browser QA, Firebase security rules review, monitoring, and UX/UI polish are complete.

## Recommended Staff Roles During Pilot

### Platform Administrator

Primary responsibilities:

- Create and verify staff/admin accounts.
- Confirm Firebase and Firestore setup.
- Manage staff permissions.
- Monitor system-level readiness and access issues.
- Coordinate technical troubleshooting.

Recommended platform role:

- `admin`

### Pilot Coordinator

Primary responsibilities:

- Manage youth onboarding.
- Confirm profile completion.
- Track program participation.
- Review the Pilot Readiness, Pilot Metrics, and Pilot Evaluation dashboards.
- Coordinate weekly operations meetings.

Recommended platform role:

- `staff` or `admin`

### Program Lead

Primary responsibilities:

- Create and update programs.
- Review enrollments.
- Record or verify attendance.
- Coordinate youth participation follow-up.

Recommended platform role:

- `staff`

### Credential Lead

Primary responsibilities:

- Create credential definitions.
- Award credentials when youth meet requirements.
- Verify credential visibility on youth dashboards.
- Review credential participation.

Recommended platform role:

- `staff`

### Service-Hour Reviewer

Primary responsibilities:

- Review service-hour records.
- Approve or reject submitted service hours.
- Correct service-hour records when needed.
- Verify approved hours appear in dashboards.

Recommended platform role:

- `staff`

### Stakeholder Relationship Lead

Primary responsibilities:

- Manage educator records.
- Manage partner organization records.
- Manage government organization records.
- Maintain relationship notes and follow-up dates.

Recommended platform role:

- `staff`

## Staff Procedures

### 1. Staff Account Setup

Staff account setup should be completed before youth onboarding begins.

Procedure:

1. Create staff user accounts through Firebase Authentication or the approved account creation path.
2. Confirm each staff person can sign in to the frontend.
3. Create or verify a matching user profile document in `(default)/aspirationnetworkusers/{Firebase UID}`.
4. Set the staff profile role to `staff` or `admin`.
5. Confirm the staff profile has enough identifying information for operations, such as name and email.
6. Ask each staff person to open the Staff Dashboard.
7. Confirm the staff person can access staff-only pages.
8. Confirm a youth/member account is denied from staff-only pages.

Required role values:

- Staff access: `staff`
- Admin access: `admin`

Do not grant staff privileges to:

- `member`
- `youth`
- `educator`
- `partner`
- `government`

Permission verification checklist:

- Staff can access `/staff`.
- Staff can access `/staff/users`.
- Staff can access `/staff/program-management`.
- Staff can access `/staff/credential-management`.
- Staff can access `/staff/attendance-management`.
- Staff can access `/staff/service-hour-management`.
- Staff can access `/staff/pilot-readiness`.
- Staff can access `/staff/pilot-metrics`.
- Staff can access `/staff/pilot-evaluation`.
- Youth/member account receives access denied for staff pages.

### 2. User Management

Staff should use User Management and Youth Management to monitor onboarding.

Procedure:

1. Open Staff Dashboard.
2. Open User Management or Youth Management.
3. Review registered youth.
4. Confirm each pilot youth has:
   - name
   - email
   - role of `member` unless otherwise approved
   - `youthProfile = true`
   - private profile behavior
   - profile status appropriate to onboarding state
   - ASPN Participant ID when profile completion has occurred
5. Use staff-managed updates only for fields intended for staff review.
6. Do not manually change youth UID values.
7. Do not manually assign youth staff/admin roles.

Escalate if:

- a youth account cannot access the dashboard,
- a youth has no matching Firestore profile after profile completion,
- a youth appears with the wrong role,
- ASPN Participant ID is missing after profile completion,
- duplicate youth profiles appear for the same person.

## Youth Procedures

### 1. Youth Account Creation

Youth account creation should be performed in a controlled session whenever possible.

Procedure:

1. Youth opens the frontend.
2. Youth selects Create Account.
3. Youth enters email and password.
4. Confirm the account appears in Firebase Authentication.
5. Youth signs in.
6. Youth opens the Profile page.
7. Youth completes required profile fields.

Staff should confirm:

- youth can sign in,
- youth can access the Youth Dashboard,
- youth cannot access staff/admin routes,
- youth profile remains private by default.

### 2. Profile Completion

Profile completion is required for useful dashboard data.

Youth should complete:

- first name
- last name
- school
- graduation year
- supported interests if requested during onboarding

Staff should confirm:

- profile completion updates the dashboard,
- school and graduation year no longer show as missing,
- profile status is appropriate,
- ASPN Participant ID exists after protected profile update,
- public profile behavior remains disabled.

### 3. Program Enrollment

Youth should enroll only in active programs.

Procedure:

1. Youth opens Programs.
2. Youth reviews active available programs.
3. Youth selects Enroll for the correct program.
4. Youth sees enrollment confirmation.
5. Youth returns to dashboard.
6. Staff confirms the enrolled program appears on the youth dashboard.

Staff should verify:

- active programs are visible,
- archived programs are not available for youth enrollment,
- duplicate enrollment is rejected,
- enrollment appears in staff enrollment review.

### 4. Credential Visibility

Youth should be able to view earned and available credentials.

Procedure:

1. Youth opens Credentials.
2. Youth reviews earned credentials.
3. Youth reviews available credentials tied to enrolled programs.
4. Youth confirms requirement text is visible where available.
5. Staff awards a test credential when appropriate.
6. Youth refreshes dashboard and Credentials page.
7. Staff confirms credential appears as earned.

Staff should not allow youth to self-award credentials.

## Program Operations

### Creating Programs

Programs should be created before youth onboarding.

Procedure:

1. Open Program Management.
2. Create a program with:
   - program name
   - description
   - start date
   - end date if known
   - category
   - program leader
   - capacity if known
   - active status
3. Save the program.
4. Confirm the program appears in the active programs list.
5. Confirm youth can see the program on the Programs page.

Before launch, create only programs that youth should actually be able to join.

### Managing Enrollments

Procedure:

1. Open Program Management or Enrollment Management.
2. Review enrollments by program.
3. Confirm enrolled youth match expected pilot participants.
4. Remove enrollment only when needed.
5. Document operational reasons for removal outside the platform if additional context is required.

Enrollment correction examples:

- youth enrolled in the wrong program,
- duplicate account was used,
- youth withdrew from the pilot,
- staff created a test record that should not remain active.

### Closing Programs

Programs should be archived when they should no longer accept youth enrollments.

Procedure:

1. Open Program Management.
2. Select the program.
3. Archive the program.
4. Confirm the program no longer appears as available to youth.
5. Confirm existing historical records remain available for staff review.

Use archive/restore actions instead of deleting program records.

## Credential Operations

### Creating Credential Definitions

Credential definitions should be created before staff begin awarding credentials.

Procedure:

1. Open Credential Management.
2. Create a credential definition with:
   - credential name
   - description
   - category
   - icon or placeholder icon value if used
   - active status
   - associated program IDs if applicable
   - requirement text if available
3. Save the credential definition.
4. Confirm it appears in credential management.
5. Confirm available credentials appear for enrolled youth when relevant.

Do not hardcode final credential names outside the staff-managed credential definition workflow.

### Awarding Credentials

Procedure:

1. Confirm the youth user exists.
2. Confirm the credential definition exists and is active.
3. Open Credential Management.
4. Enter or select the youth UID.
5. Enter or select the credential ID.
6. Award the credential.
7. Confirm the earned credential appears in staff records.
8. Ask the youth to refresh Dashboard or Credentials page.
9. Confirm the credential appears as earned.
10. Confirm a notification appears if notification generation is expected.

Do not award credentials to a youth account unless staff have confirmed the youth earned it.

### Verifying Credential Issuance

Verification checklist:

- earned credential record exists,
- youth profile references the earned credential where applicable,
- youth dashboard displays the credential,
- credential no longer appears as available/not-yet-earned if logic supports that distinction,
- notification appears for credential-earned events where supported.

## Attendance Operations

### Recording Attendance

Attendance should be recorded by staff/admin users.

Procedure:

1. Open Attendance Management.
2. Select or enter youth UID.
3. Enter program ID.
4. Enter event or meeting name.
5. Enter event date.
6. Select attendance status:
   - present
   - absent
   - excused
   - pending
7. Save the attendance record.
8. Confirm the record appears in Attendance Management.
9. Confirm the youth dashboard reflects attendance data where applicable.

Credential impact:

- Only `present` attendance should count toward attendance-count credential rules.
- Staff should verify auto-awarded credentials after attendance is recorded for credential-bearing programs.

### Reviewing Attendance Data

Review attendance weekly.

Staff should check:

- attendance records by program,
- attendance records by youth,
- missing attendance dates,
- incorrect statuses,
- whether attendance totals appear reasonable,
- whether attendance-based credentials were awarded correctly.

### Handling Corrections

Use update actions when correcting:

- wrong attendance status,
- wrong event date,
- wrong event name,
- wrong program ID,
- wrong youth UID if the record was attached incorrectly.

Use delete actions cautiously. Deleting attendance removes the record from staff reporting and may affect later auditability.

Recommended practice:

- Prefer correction/update when possible.
- If deletion is required, keep a staff note outside the platform explaining why the record was removed.

## Service Hour Operations

### Reviewing Submissions

Youth-facing service-hour behavior currently depends on staff-managed records and the service-hour request URL.

Procedure:

1. Open Service Hour Management.
2. Review records by youth, status, program, or service date.
3. Confirm description and hours are reasonable.
4. Confirm supporting context from the approved service-hour request process.
5. Decide whether to approve, reject, or leave pending.

### Approving Hours

Procedure:

1. Select the service-hour record.
2. Confirm youth identity.
3. Confirm date, hours, program, and description.
4. Approve the record.
5. Confirm the record status changes to verified/approved according to the platform field behavior.
6. Confirm approved hours appear in the youth Service Hours page and dashboards.

### Rejecting Hours

Procedure:

1. Select the service-hour record.
2. Confirm the reason for rejection.
3. Reject the record.
4. Communicate next steps to youth outside the platform if needed.

The platform does not provide youth messaging.

### Correcting Records

Use staff update/status actions for:

- incorrect hours,
- incorrect service date,
- incorrect program,
- incorrect description,
- incorrect verification status.

Use deletion cautiously because it can reduce auditability.

## Stakeholder Operations

Stakeholder modules support staff relationship tracking. They are not public portals.

### Educators

Use Educator Management to track:

- educator name,
- organization,
- title,
- email,
- phone,
- active status,
- notes.

Recommended use:

- add educators involved in pilot support,
- deactivate records when relationships are no longer active,
- avoid storing sensitive youth information in educator notes.

### Partner Organizations

Use Partner Management to track:

- organization name,
- organization type,
- website,
- primary contact,
- email,
- phone,
- active status,
- notes.

Recommended use:

- track nonprofit, business, foundation, higher education, faith-based, workforce, community, and other partners,
- maintain clean contact records,
- avoid confidential youth-specific information in organization notes.

### Government Organizations

Use Government Management to track:

- organization name,
- government level,
- organization type,
- primary contact,
- workforce partner flag,
- credential partner flag,
- active status,
- notes.

Recommended use:

- track public-sector partners involved in pilot or future workforce strategy,
- distinguish workforce partners from credential partners,
- avoid treating this module as a job-matching system.

### Relationship Notes

Use Relationship Notes to track:

- stakeholder type,
- stakeholder ID,
- stakeholder name,
- note text,
- relationship status,
- relationship owner,
- last contact date,
- next follow-up date,
- active status.

Relationship statuses:

- prospect
- contacted
- meeting_scheduled
- active_partner
- inactive_partner
- declined

Recommended use:

- record staff follow-up obligations,
- review upcoming and overdue follow-ups weekly,
- assign ownership to specific staff,
- avoid recording sensitive youth details.

## Dashboard Procedures

### Pilot Readiness Dashboard

Purpose:

The Pilot Readiness Dashboard answers: **Are we ready?**

It measures:

- youth onboarding readiness,
- profile completion readiness,
- active program availability,
- credential readiness,
- attendance readiness,
- service-hour readiness,
- educator/partner/government records,
- stakeholder relationship notes,
- platform activity,
- blockers,
- warnings,
- checklist status.

Recommended review frequency:

- daily during the two weeks before launch,
- weekly during active pilot operations,
- after any major setup change.

Actions staff should take:

- resolve blockers before onboarding youth,
- review warnings in weekly operations meetings,
- seed missing programs or credentials,
- complete stakeholder setup if needed,
- investigate low platform activity,
- confirm profile completion is on track.

### Pilot Metrics Dashboard

Purpose:

The Pilot Metrics Dashboard answers: **What is happening?**

It measures:

- registrations,
- active users,
- profile completion rate,
- active users in recent time windows,
- program enrollment and participation,
- attendance records,
- credential definitions and awards,
- credentials by category,
- credentials by program,
- service-hour submissions,
- approved service hours,
- stakeholder engagement,
- staff operations.

Recommended review frequency:

- weekly during pilot,
- after onboarding sessions,
- after credential-awarding sessions,
- after service-hour review sessions.

Actions staff should take:

- follow up with youth who have not completed profiles,
- review programs with low participation,
- check credential participation against goals,
- monitor service-hour approval delays,
- identify staff workflow bottlenecks,
- use metrics to plan targeted outreach.

### Pilot Evaluation Dashboard

Purpose:

The Pilot Evaluation Dashboard answers: **How well is the pilot performing?**

It measures outcome-oriented categories:

- youth outcomes,
- program outcomes,
- credential outcomes,
- service outcomes,
- stakeholder outcomes,
- operations outcomes,
- overall score,
- strengths,
- concerns,
- recommended actions.

Recommended review frequency:

- weekly after pilot launch,
- at the end of each month,
- before leadership or partner reporting,
- before any pilot expansion decision.

Actions staff should take:

- use strengths to identify what is working,
- use concerns to prioritize staff interventions,
- use recommended actions for weekly operating priorities,
- compare evaluation signals with staff observations,
- avoid treating early pilot data as causal proof.

## QA Procedures

### Onboarding Validation Checklist

Complete before youth onboarding:

- [ ] Staff/admin account can sign in.
- [ ] Staff/admin can access Staff Dashboard.
- [ ] Youth/member account cannot access staff routes.
- [ ] Youth test account can create account.
- [ ] Youth test account can complete profile.
- [ ] Youth dashboard loads after profile completion.
- [ ] ASPN Participant ID appears where expected.
- [ ] Youth profile remains private.
- [ ] No public youth directory is available.

### Enrollment Validation Checklist

- [ ] At least one active program exists.
- [ ] Archived/inactive programs are not available for youth enrollment.
- [ ] Youth can view active programs.
- [ ] Youth can enroll in an active program.
- [ ] Duplicate enrollment is rejected.
- [ ] Staff can view enrollment records.
- [ ] Youth dashboard shows enrolled active program.

### Credential Validation Checklist

- [ ] At least one active credential definition exists.
- [ ] Credential definition includes name, description, category, and active status.
- [ ] Credential can be associated with a program when needed.
- [ ] Staff can award credential to youth.
- [ ] Youth cannot self-award credential.
- [ ] Earned credential appears on youth dashboard.
- [ ] Earned credential appears on Credentials page.
- [ ] Credential-earned notification appears where expected.

### Attendance Validation Checklist

- [ ] Staff can create attendance record.
- [ ] Attendance record includes youth UID, program ID, event name, event date, and status.
- [ ] Staff can update attendance record.
- [ ] Attendance record appears in staff management.
- [ ] Youth dashboard reflects attendance data.
- [ ] Present attendance counts toward attendance-based credential rules when configured.
- [ ] Absent, excused, and pending records do not count toward attendance-count auto-awards.

### Service-Hour Validation Checklist

- [ ] Service-hour request URL is configured if the pilot uses it.
- [ ] Staff can create or review service-hour record.
- [ ] Record includes youth UID, date, hours, description, and status.
- [ ] Staff can approve service hours.
- [ ] Staff can reject service hours.
- [ ] Approved hours appear in youth view and dashboards.
- [ ] Pending and rejected records display safely.

### Stakeholder Validation Checklist

- [ ] Staff can create educator record.
- [ ] Staff can create partner organization record.
- [ ] Staff can create government organization record.
- [ ] Staff can create relationship note.
- [ ] Upcoming follow-ups appear in relationship notes.
- [ ] Overdue follow-ups appear when dates pass.
- [ ] Youth/member account cannot access stakeholder modules.

### Dashboard Validation Checklist

- [ ] Staff Dashboard loads.
- [ ] Reporting Dashboard loads.
- [ ] Credential Analytics loads.
- [ ] Program Analytics loads.
- [ ] Relationship Notes dashboard loads.
- [ ] Pilot Readiness Dashboard loads.
- [ ] Pilot Metrics Dashboard loads.
- [ ] Pilot Evaluation Dashboard loads.
- [ ] Dashboards display empty states safely.
- [ ] Dashboards display seeded pilot data correctly.

## Pilot Launch Checklist

### Users

- [ ] Staff/admin accounts created.
- [ ] Staff/admin Firestore profiles have role `staff` or `admin`.
- [ ] Youth/member test account created.
- [ ] Youth/member account denied from staff routes.
- [ ] Initial youth cohort account creation plan confirmed.
- [ ] Youth onboarding support staff assigned.

### Programs

- [ ] At least one active pilot program exists.
- [ ] Program information is accurate.
- [ ] Program leader is listed.
- [ ] Archived programs are hidden from youth enrollment.
- [ ] Enrollment workflow tested.

### Credentials

- [ ] Initial credential definitions created.
- [ ] Credential definitions are active where appropriate.
- [ ] Credential categories are accurate enough for pilot reporting.
- [ ] Manual credential award tested.
- [ ] Attendance-based credential behavior tested if used.
- [ ] Youth credential display tested.

### Attendance

- [ ] Staff attendance workflow tested.
- [ ] Attendance statuses confirmed.
- [ ] Attendance correction procedure reviewed.
- [ ] Attendance data appears in dashboards.

### Service Hours

- [ ] Service-hour request URL configured if applicable.
- [ ] Staff service-hour review workflow tested.
- [ ] Approval/rejection workflow tested.
- [ ] Approved service hours appear in dashboards.

### Stakeholders

- [ ] Educator records seeded where needed.
- [ ] Partner organization records seeded where needed.
- [ ] Government organization records seeded where needed.
- [ ] Relationship owners assigned.
- [ ] Follow-up dates reviewed.

### Dashboards

- [ ] Staff Dashboard reviewed.
- [ ] Pilot Readiness Dashboard reviewed.
- [ ] Pilot Metrics Dashboard reviewed.
- [ ] Pilot Evaluation Dashboard reviewed.
- [ ] Blockers resolved or documented.
- [ ] Warnings assigned to staff owners.

### Firebase

- [ ] Firebase Authentication enabled.
- [ ] Email/password sign-in enabled.
- [ ] Active Firestore database confirmed as `(default)`.
- [ ] Legacy database not used for active pilot operations.
- [ ] Service account / Admin SDK credentials verified for backend.
- [ ] Frontend Firebase config verified.
- [ ] Firestore rules reviewed before launch.
- [ ] Hosting/deployment path confirmed.
- [ ] No credentials committed to repository.

### QA Completion

- [ ] Backend validation completed.
- [ ] Frontend build validation completed.
- [ ] Staff QA walkthrough completed.
- [ ] Youth QA walkthrough completed.
- [ ] Mobile/browser smoke test completed.
- [ ] Staff training completed.
- [ ] Escalation contact identified.

## Recommended Weekly Operating Rhythm

### Monday: Readiness And Planning

Staff should:

- review Pilot Readiness Dashboard,
- check unresolved blockers and warnings,
- review upcoming stakeholder follow-ups,
- confirm weekly program schedule,
- assign staff responsibilities.

### Tuesday-Wednesday: Youth Participation Follow-Up

Staff should:

- review profile completion,
- review program enrollment,
- follow up with youth who have not completed onboarding,
- review RWD activity progress,
- check notifications and credential visibility.

### Thursday: Data Quality Review

Staff should:

- review attendance records,
- review service-hour records,
- correct obvious data errors,
- approve or reject pending service-hour records,
- verify credential awards.

### Friday: Metrics And Evaluation Review

Staff should:

- review Pilot Metrics Dashboard,
- review Pilot Evaluation Dashboard,
- identify strengths,
- identify concerns,
- document staff actions for the next week,
- prepare leadership or partner updates if needed.

### End Of Month: Pilot Learning Review

Staff should:

- review youth participation,
- review retention signals,
- review credential completion,
- review service-hour activity,
- review stakeholder engagement,
- record operational lessons learned,
- identify improvements for the next month.

## Incident And Escalation Guidance

Escalate immediately if:

- youth can access staff/admin pages,
- staff cannot access critical staff pages,
- youth profile data appears public,
- credential awards are incorrect or duplicated,
- Firebase authentication fails broadly,
- Firestore writes fail broadly,
- sensitive data appears in the wrong place,
- dashboards show obviously incorrect pilot totals.

Recommended response:

1. Pause affected workflow.
2. Preserve the current state for review.
3. Notify the platform administrator.
4. Identify whether the issue is user data, role setup, frontend display, backend service, or Firebase configuration.
5. Document the issue and resolution.
6. Retest the workflow with a staff account and youth test account.

## Data Privacy And Safety Rules

Staff must preserve the platform's youth safety posture:

- Do not create public youth directories.
- Do not share youth profile data outside approved staff workflows.
- Do not store unnecessary sensitive youth information in stakeholder notes.
- Do not use ASPN Participant ID as a public username.
- Do not grant staff access to non-staff roles.
- Do not re-enable legacy public profile or discussion endpoints.
- Do not treat the platform as social media.

## Runbook Limitations

This runbook does not replace:

- Firebase security rules review,
- production readiness review,
- staff training,
- youth consent procedures,
- university or partner data-governance requirements,
- legal/privacy review,
- mobile/browser QA,
- public launch approval.

## Launch Recommendation

Based on the v0.5 platform state and Production Readiness Review 27B:

- Controlled Pilot QA: Ready
- Controlled Pilot Launch: Ready with conditions
- Public Launch: Not yet ready

The first pilot should proceed only after staff accounts, seed data, Firebase configuration, QA checklists, and staff operating responsibilities are confirmed.
