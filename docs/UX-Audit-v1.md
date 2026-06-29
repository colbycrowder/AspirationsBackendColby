# ASPN Platform UX Audit and Usability Review

**Version:** Checkpoint 31A  
**Date:** June 22, 2026  
**Scope:** Current React/Vite frontend, youth and staff UX snapshots, route structure, components, CSS, responsive behavior, authentication shell, and dashboard API usage.

---

# 1. Executive Summary

## Overall UX Assessment

The ASPN Platform is functionally broad and sufficiently coherent for controlled pilot QA. Its youth experience now has a recognizable identity centered on Home, My Journey, programs, civic learning, credentials, service, and private development records. Its staff experience supports most pilot operations without routine direct Firestore work.

The product is strongest when it acts as a **youth development operating system**: Home tells youth what to do next, My Journey explains why participation matters, and credentials connect accomplishments to civic pathways. The staff side is appropriately closer to an **administrative dashboard**, with record management and descriptive reporting.

The platform does not currently feel like social media, a job board, or a conventional learning management system. It has lightweight learning tracking, but not course authoring or classroom management. It has exploratory civic role context, but not job matching or applications.

## Pilot Readiness Assessment

**Overall UX readiness: Ready with Issues.**

No reviewed screen reveals a confirmed UX defect that makes controlled pilot QA impossible. Before real youth onboarding, however, ASPN should address or operationally mitigate first-session profile recovery, destructive staff actions, typed identifier errors, accessibility validation, and staff reporting/navigation training.

The platform can support 60 to 100 youth and 5 to 10 staff at pilot scale, but support burden will increase if these issues remain. The central risk is not raw feature absence; it is that a new youth or occasional staff user may understand the system only after intervention from someone who already knows it.

## Highest-Risk Youth Issues

1. Account creation redirects to Home before a complete Firestore profile necessarily exists; the resulting error explains the issue but provides no direct Complete Profile button.
2. Credential Explorer, Notifications, and Service Hours are secondary on desktop and absent from the mobile bottom bar, increasing mobile discoverability risk.
3. Programs, Profile, Notifications, and Service Hours retain older light styling while Home, My Journey, Learning, and Credential Explorer use the refreshed dark youth system.
4. Profile interest entry uses comma-separated free text and reads more administratively than developmentally.
5. Formal keyboard, screen-reader, zoom, contrast, device, and browser validation has not yet been recorded.

## Highest-Risk Staff Issues

1. Credential awards and several record workflows rely on manually typed UIDs and record IDs, creating wrong-person and data-entry risk.
2. Attendance, service-hour, and relationship-note records can be deleted without a consistent confirmation or recovery experience.
3. User Management and Youth Management overlap, while six reporting/metrics destinations require training to distinguish.
4. Staff mobile navigation is a long in-flow sidebar with dense forms and tables; it is suitable for emergency access, not efficient routine mobile operation.
5. RWD activity management lists active records only, so deactivated activities disappear and cannot be restored through the current screen.

## Highest-Value Next Improvements

1. Make first-time profile completion an explicit, recoverable onboarding path.
2. Add staff data-integrity safeguards around destructive actions and typed identifiers.
3. Conduct structured accessibility, mobile-device, and browser QA with representative youth and staff.
4. Clarify staff workflow boundaries and reporting definitions through labels, descriptions, and training.
5. Align remaining youth screens with the current youth design language after functional safeguards are stable.

---

# 2. Information Architecture Audit

## Youth Information Architecture

### Summary

Youth IA is understandable once a participant reaches Home. Primary concepts are developmentally framed and avoid institutional jargon in the refreshed screens. The largest issue is that mobile navigation prioritizes only five destinations, leaving three meaningful supporting tools dependent on contextual links.

| Area | Route | Score | Findings |
|---|---|---|---|
| Home | `/dashboard` | **Good** | Clear center of gravity and useful next actions. First-profile failure recovery needs a direct action. |
| My Journey | `/journey` | **Good** | Distinct purpose; synthesizes accomplishments and pathways without fake percentages. Some concepts may need age-adjusted explanation. |
| Programs | `/programs` | **Needs Improvement** | Enrollment is clear, but presentation uses older styling and program cards lack richer schedule/eligibility context. |
| Global Civic Movements | `/rwd-learning-center` | **Good** | Youth-facing label and learning framing are clear. Credential linkage still displays backend IDs. |
| Credential Explorer | `/credentials` | **Good** | Strong catalog and visual identity. Not a mobile bottom tab; earning requirements remain general. |
| Profile | `/profile` | **Needs Improvement** | Functionally clear but administrative in tone; comma-separated interests and editable email can confuse. |
| Notifications | `/notifications` | **Needs Improvement** | Useful inbox and read state, but mobile discoverability is indirect and summary cards partially duplicate counts. |
| Service Hours | `/service-hours` | **Needs Improvement** | Status and totals are clear; form submission depends on an external link and the page uses older styling. |

### Can Youth Find Major Functions?

Desktop users can find all major destinations through grouped navigation. Mobile users can immediately find Home, Journey, Programs, Learning, and Profile. Credentials are discoverable through Home and Journey; Notifications and Service Hours depend on Home next actions or familiarity with the secondary route structure.

For returning users, this is acceptable. For first-time users aged toward the lower end of the stated 8–24 range, indirect discovery creates support risk.

### Label Clarity for Ages 8–24

- **Home, Programs, Learning, Profile, Notifications, and Service Hours** are broadly intuitive.
- **My Journey** is welcoming but requires content to explain that it is an accomplishment record.
- **Global Civic Movements** is meaningful but may be abstract for younger participants; the subtitle helps.
- **Credential Explorer** is clear for older youth but may require a brief explanation of “credential” during onboarding.
- **Pathway** and **civic role family** are conceptually useful but advanced terms for younger users.

### Secondary Tools

Notifications and Service Hours are appropriately secondary in the desktop sidebar. Credential Explorer is more central to ASPN's identity than its secondary navigation position suggests. The mobile bar's omission of Credential Explorer is the most notable IA tradeoff.

### Missing or Redundant Destinations

- No dedicated youth Opportunities page exists; Home provides lightweight opportunity links. This is an acknowledged future area, not a pilot blocker.
- Youth Attendance remains a placeholder route rather than a functional destination and is omitted from current youth navigation. This avoids presenting an incomplete workflow.
- Home and My Journey are related but not redundant: Home is action-oriented; My Journey is reflective and cumulative.

## Staff Information Architecture

### Summary

Staff IA covers the necessary operational domains but has reached the point where breadth creates cognitive load. The domain grouping is logical; the overlapping user screens and reporting destinations are not self-explanatory enough for occasional staff.

| Area | Route | Score | Findings |
|---|---|---|---|
| Staff Dashboard | `/staff` | **Good** | Useful operational landing page with broad shortcuts. Does not surface urgent queues. |
| User Management | `/staff/users` | **Needs Improvement** | Powerful filtering and detail, but overlaps Youth Management and exposes many identifiers. |
| Youth Management | `/staff/youth-management` | **Needs Improvement** | Safe limited review flow; distinction from User Management requires training. |
| Program Management | `/staff/program-management` | **Good** | Create/edit/archive/detail and totals are cohesive. Individual enrollment operations are not central. |
| Credential Management | `/staff/credential-management` | **Needs Improvement** | Strong catalog foundation; typed IDs and incomplete auto-award visibility increase risk. |
| Attendance Management | `/staff/attendance-management` | **Needs Improvement** | Functional record management; repetitive entry and hidden credential effects need attention. |
| Service Hour Management | `/staff/service-hour-management` | **Needs Improvement** | Functional verification; lacks dedicated pending queue and deletion safeguards. |
| Learning Management | `/staff/rwd-management` | **Needs Improvement** | Basic activity management works; terminology and active-only listing are confusing. |
| Educator Management | `/staff/educators` | **Good** | Clear directory, filters, details, and status workflow. |
| Partner Management | `/staff/partners` | **Good** | Clear organization directory and contact workflow. |
| Government Management | `/staff/government` | **Good** | Appropriate filters and relationship flags; form density is high. |
| Stakeholder Relationships | `/staff/relationships` | **Good** | Notes, status, ownership, and follow-up reporting form a coherent lightweight CRM-like workflow. |
| Reporting | `/staff/reporting` | **Needs Improvement** | Useful combined view; distinctions from Pilot Metrics and Metrics require explanation. |
| Operations Reporting | `/staff/operations-reporting` | **Good** | Focused scope and understandable breakdowns. Staff UIDs reduce readability. |
| Metrics | `/staff/metrics` | **Needs Improvement** | Simple counts are useful but overlap dashboard and pilot metrics. |
| Pilot Readiness | `/staff/pilot-readiness` | **Good** | Clear blockers, warnings, checklist, and status. Formula context is limited. |
| Pilot Metrics | `/staff/pilot-metrics` | **Good** | Strong “what is happening” view. Dense without training. |
| Pilot Evaluation | `/staff/pilot-evaluation` | **Good** | Clear outcome framing and narrative outputs. Thresholds and denominators need operational documentation. |

### Workflow Findability

Staff can find operational domains from the grouped sidebar and dashboard shortcuts. Frequent tasks are present, but the Staff Dashboard does not prioritize pending reviews, overdue follow-ups, service-hour queues, or today's attendance. Staff must know which module to open.

### Duplicate and Fragmented Workflows

- **User Management vs Youth Management:** One is broad administration; the other is constrained onboarding review. Their names do not fully communicate that distinction.
- **Metrics vs Reporting vs Pilot Metrics:** All show aggregate data at different levels. The difference is understandable only after reading page descriptions.
- **Pilot Readiness vs Pilot Evaluation:** Readiness asks whether prerequisites exist; Evaluation asks how outcomes perform. This distinction should be part of staff onboarding.
- **Operations Reporting** is focused and appropriately separate, but also appears within combined Reporting.

### Grouping Quality

The sidebar groups are structurally sound. The Reports and Pilot Tools groups contain the most conceptual overlap. The continued staff-facing RWD label also creates vocabulary drift from youth support conversations.

---

# 3. Youth Experience Audit

## Home

### What Should I Do Next?

Home answers this question better than any other screen. Continue Your Journey converts profile, program, learning, credential, service, and notification data into up to five actions. Progress cards link to supporting pages.

### Prioritization

Profile completion and program enrollment appear when absent. Learning, credentials, service, and notifications follow. The priorities are sensible for pilot onboarding. The list is capped at five, so lower-priority actions can disappear when several needs coexist.

### Zero States

Zero states are direct and honest. They do not fabricate activity. New users may nevertheless face many empty sections simultaneously, making seeded pilot content important.

### Opportunities

Opportunities are presented as a small supporting section with external links. This avoids overpromising matching. The page does not explain selection criteria because no matching system exists.

### First-Time Comprehension

The welcome language is youth-centered, but account creation sends users to Home before profile completion is guaranteed. If dashboard loading fails for a missing profile, the screen instructs the youth to complete the profile but does not provide a button. This is the highest-value youth fix.

**Assessment: Good, with a pre-pilot onboarding recovery issue.**

## My Journey

The timeline clearly separates Program, Credential, Learning, Service, and Attendance milestones. Credential dates and completion dates are useful. Program events lack enrollment dates and show “Date not available,” which weakens narrative quality.

Service and attendance milestones are meaningful and restrained. The pathway section avoids percentages and marks Started only when supported by earned credentials.

Credential-to-pathway cards clearly explain related programs, advanced credentials, and civic role families. Role-family language is exploratory and does not imply job matching. For younger youth, pathway descriptions may need staff orientation.

Approved credential icons improve recognition. Unmatched credentials preserve initials and remain visible.

**Assessment: Good.**

## Programs

Active, available, enrolled, and unavailable states are understandable. Enrollment refreshes dashboard data and disables duplicate action. Archived programs are not open to youth.

Program cards show description, category, leader, optional image, and status. They do not show dates, remaining capacity, eligibility, location, schedule, or a detailed program page. Those omissions may generate questions but are not required for the present enrollment foundation.

The light visual treatment feels older than the youth Home and Journey experience.

**Assessment: Needs Improvement.**

## Global Civic Movements

Learning is framed as exploration rather than an administrative activity list. Summary cards, Continue Learning, and card-based experiences make status understandable.

Youth-facing titles consistently use Global Civic Movements or Learning. The backend route and staff page retain RWD; the youth component itself still records an event named `RWD_ACTIVITY_VIEWED`, but that implementation name is not visible.

Credential relationships display a raw associated credential ID. This is less understandable than the registry-driven name and icon used elsewhere. Quiz score and pass information display only when present.

**Assessment: Good.**

## Credential Explorer

The six core and four advanced credentials are clearly separated. Cards explain description, pathways, related programs, and role families. Earned and Explore states are explicit.

Approved icons provide a strong credential identity and remain mobile-safe. Initials fallback protects unmatched records.

The catalog does not expose exact backend requirement structures or progress, which is honest but may leave youth asking how to earn a specific credential. Mobile access depends on contextual links rather than a bottom tab.

**Assessment: Good.**

## Profile

Required identity fields are straightforward. School and graduation year support onboarding. ASPN Participant ID and profile status are visible without exposing role or UID controls.

Interest fields are understandable as categories, but comma-separated text is error-prone and less welcoming than structured choices. Email appears editable even though Firebase email remains the authentication identity, creating a conceptual mismatch.

The page tells youth the profile is private by default. It does not provide a more detailed privacy explanation or clarify how the ASPN Participant ID is used.

**Assessment: Needs Improvement.**

## Notifications

The inbox shows unread/total counts, title, message, type, date, related credential identifiers, and read state. Mark as Read is clear.

“Unread” and “Dashboard Unread” can differ temporarily and may confuse youth. The page is absent from mobile bottom navigation. Home surfaces it only when unread notices exist.

**Assessment: Needs Improvement.**

## Service Hours

Approved, pending, and rejected totals are clear. Submitted records show activity, date, status, hours, submission date, program ID, and source. The external request link honestly reports when it is unavailable.

Program IDs and verification sources are operational rather than youth-friendly. Opening an external request form sends users outside the platform, and the link does not announce that it opens a new tab.

**Assessment: Needs Improvement.**

---

# 4. Staff Experience Audit

## Staff Dashboard

The landing page gives a useful cross-platform count summary and complete quick-action coverage. It is effective for navigation, not prioritization. It does not surface pending service hours, reviews required, overdue relationships, or urgent exceptions.

**Assessment: Good.**

## User and Youth Management

The broad user screen and constrained youth review screen are both useful. The distinction is not obvious from their names. Staff can safely manage profile status and verification without changing protected identity fields.

UIDs are prominent in search, display, and downstream workflows. ASPN Participant IDs are more readable but do not yet replace operational UIDs in forms. Staff training is needed to avoid confusing authentication identifiers with research identifiers.

**Assessment: Needs Improvement.**

## Program Management

Create, edit, archive, restore, filter, detail, and totals form a cohesive workflow. Staff can understand aggregate enrollments, credentials, attendance, service, and active participation.

The current main component does not provide an obvious named enrollment roster or direct enrollment-removal workflow, so some participant-level program tasks may require another process.

**Assessment: Good.**

## Credential Management

Catalog creation, editing, filtering, archive/restore, analytics, and manual awards are present. Program and category relationships are visible.

Manual awards require typed youth UID and credential ID with no selected-person confirmation. A valid but wrong UID could create an incorrect award. The screen does not explain attendance or learning auto-award behavior, and it does not fully edit auto-award rules.

The staff `icon` field is not visibly aligned with the new frontend asset registry, which could imply that changing it affects youth icons when it may not.

**Assessment: Needs Improvement.**

## Attendance Management

Creation, filters, totals, and status updates are straightforward. The four statuses are clear. Repeated session entry may become slow without batch entry.

Changing a record to present can affect attendance-based credential awards, but the screen does not preview or confirm that consequence. Delete is immediately available without a consistent confirmation experience.

**Assessment: Needs Improvement.**

## Service Hour Management

Pending, verified, and rejected states are understandable, and totals summarize both records and hours. Staff can filter by user, program, status, and date.

There is no dedicated pending queue or youth-name resolution. Typed UIDs increase friction. Delete can undermine auditability if staff use it instead of correction or rejection.

**Assessment: Needs Improvement.**

## Learning Administration

Staff can create and update activity metadata, external URLs, active state, and optional credential linkage. This is sufficient for basic pilot content management.

The RWD label conflicts with youth-facing Global Civic Movements. Credential linkage uses a typed ID. Inactive activities disappear from the only list, preventing restoration through the UI. Participant progress is not visible here.

**Assessment: Needs Improvement.**

## Organization and Stakeholder Management

Educator, partner, and government directories use consistent list/detail/form patterns. Filters align with each directory's operational purpose. Relationship notes add stages, ownership, dates, and follow-ups.

The workflow is useful and coherent for a small staff team. Owner UIDs are less readable than staff names, and delete remains a data-retention concern. Government forms are dense on smaller screens.

**Assessment: Good.**

## Reporting and Pilot Operations

The reporting set is extensive and useful, but distinctions require training:

- **Metrics:** simple platform counts.
- **Reporting:** participation, retention, credentials, programs, and operations.
- **Pilot Metrics:** centralized operational measurement.
- **Pilot Readiness:** prerequisite score, blockers, warnings, and checklist.
- **Pilot Evaluation:** scored outcome domains, strengths, concerns, and actions.
- **Operations Reporting:** tracked staff actions.

Page introductions help, but denominators, thresholds, and data freshness are not always visible near metrics. A staff member can read a percentage without knowing the exact eligible population unless they consult documentation.

Reports are useful at pilot scale, but no exports, saved filters, print views, or charts exist. These are limitations rather than pre-pilot blockers.

**Assessment: Good for trained staff; Needs Improvement for self-service use.**

---

# 5. Mobile UX Audit

## Youth Mobile

| Area | Finding | Priority |
|---|---|---|
| Bottom navigation | Fixed five-tab bar removes the long sidebar and respects safe-area padding. | Post-pilot refinement |
| Home | Responsive cards and next actions work conceptually; long page requires substantial scrolling. | Important before pilot QA |
| My Journey | Single-column cards are readable; timeline and pathway sections make the page long. | Post-pilot improvement |
| Programs | Functional but older visual styling and card density reduce consistency. | Important before pilot |
| Learning | Card layout, touch targets, and Continue Learning are suitable for phones. | Pilot QA validation |
| Profile | Forms stack, but long textareas and comma-separated interests create input burden. | Important before pilot |
| Credential Explorer | Cards stack and icons render safely; no bottom tab means indirect discovery. | Important before pilot |
| Notifications | Usable once reached; no bottom-tab entry. | Post-pilot improvement |
| Service Hours | Summary and records stack; external form introduces a context switch. | Important before pilot QA |
| Touch targets | Many primary actions meet approximately 44 pixels; not every text action has been device-validated. | Critical validation before pilot |
| Empty states | Honest and generally readable. Multiple simultaneous empty states can make onboarding feel unfinished. | Important before pilot |

### Youth Mobile Classification

- **Critical before pilot:** Test account creation, profile completion, bottom navigation, external learning links, and service-hour requests on representative iOS and Android devices; validate zoom and keyboard behavior.
- **Important before pilot:** Improve or operationally support Credential Explorer discoverability and first-profile recovery.
- **Post-pilot:** Reduce long-scroll fatigue and visually align older youth screens.

## Staff Mobile

| Area | Finding | Priority |
|---|---|---|
| Navigation | Full staff sidebar becomes an in-flow block; users scroll through many links before content. | Important before pilot if phones are expected |
| Forms | Inputs stack and remain usable, but long forms and typed IDs are error-prone on phones. | Important before pilot |
| Reports | Summary cards stack; long reports require extensive scrolling. | Post-pilot improvement |
| Tables | Horizontal scroll prevents hard overflow, but comparison becomes difficult. | Post-pilot improvement |
| Record lists | Selectable cards adapt reasonably well. | Pilot-acceptable |
| Operational actions | Dense action groups can wrap unpredictably and need touch-device testing. | Critical validation before pilot |

### Staff Mobile Suitability

Staff mobile use is acceptable for quick review or emergency actions after device testing. It is not efficient for routine data entry, high-volume attendance, service-hour review, or multi-dashboard analysis. Pilot procedures should assume desktop or tablet access for primary staff operations.

---

# 6. Visual Consistency Audit

## Current Theme

The refreshed youth system uses:

```css
--aspn-bg: #03045e;
--aspn-surface: #0e0e64;
--aspn-accent: #b2dfff;
--aspn-accent-hover: #81c7f8;
--aspn-text: #f9fbff;
--aspn-text-soft: rgba(203, 207, 226, 0.86);
--aspn-border: rgba(177, 223, 255, 0.22);
```

## Pages Using the Refreshed Youth Theme

- Home.
- My Journey.
- Global Civic Movements.
- Credential Explorer.
- Youth mobile bottom navigation.

These pages use dark navy surfaces, light blue accents, compact cards, restrained radii, and responsive grids.

## Pages Using Older Shared Styling

- Programs.
- Profile.
- Notifications.
- Service Hours.
- Login and Create Account.
- All staff management and reporting pages.

The staff system's light operational styling is internally coherent and does not need to match the youth theme before pilot. The older youth pages, however, create a visible identity break.

## Card Styling

Refreshed youth cards use translucent dark surfaces and 7–8 pixel radii. Older cards use white/light gray surfaces, subtle shadows, and similar radii. Staff cards are appropriately dense and utilitarian.

## Button Styling

Primary and secondary actions are generally consistent within each visual system. Some staff pages use browser prompts or dense groups of text buttons. Danger actions are visually distinct but lack consistent confirmation behavior.

## Forms

Form controls are readable and use visible labels in the profile flow. Several compact staff forms rely on placeholder text instead of explicit labels, which reduces accessibility and scanability.

## Credential Icons

Approved icons create the strongest cross-page identity system. The shared renderer preserves fallback behavior and mobile-safe sizing. Staff credential forms still expose an unrelated free-text icon field, which may confuse operational expectations.

## Page Headers and Spacing

The application shell top bar and inner page heroes sometimes repeat titles. Refreshed youth pages use more generous spacing than older youth and staff pages. This is noticeable but not functionally disruptive.

## Visual Alignment Priority

### Before Pilot

- Align first-time profile recovery with Home's action language.
- Ensure status, error, and destructive-action treatments are understandable.
- Validate older youth pages on actual phones.

### Can Wait Until Post-Pilot

- Full visual alignment of Programs, Profile, Notifications, and Service Hours.
- Staff visual refresh.
- Consolidated headers and spacing system.
- Charting or richer report presentation.

---

# 7. Accessibility and Youth Safety Review

## Keyboard Navigation

Native links, buttons, inputs, selects, and textareas provide a usable baseline. Auth and main profile fields have focus outlines. Mobile tabs include a visible focus style.

Risks:

- Focus styling is not consistently defined for all custom text actions, record cards, and compact staff controls.
- Long pages lack skip links or landmark shortcuts.
- Browser prompts in Credential Management interrupt the normal interface flow.
- Fixed mobile navigation needs keyboard and virtual-keyboard testing.

## Screen Readers

Strengths:

- Navigation landmarks have labels.
- Active navigation uses `aria-current`.
- Major summary regions and lists use semantic headings and articles.
- Decorative credential art uses adjacent accessible credential names.

Risks:

- Success and error messages are not consistently `aria-live` regions.
- Placeholder-only compact staff inputs may have no programmatic label.
- Tables need caption/context review.
- Status changes after asynchronous actions may not be announced.
- Repeated headings and shell/page titles can create noisy navigation.

## Color and Status

The refreshed youth palette appears intentionally high contrast, but no formal contrast measurement is recorded. Text labels accompany status colors. High-contrast mode and forced-colors behavior remain untested.

## Mobile Safe Areas

Youth bottom navigation uses `env(safe-area-inset-bottom)` and adds workspace padding. This is a strong implementation choice. Physical device validation remains necessary for browser chrome, notches, zoom, and virtual keyboards.

## External Links

Learning, service-hour requests, and opportunities open in new tabs with safe `rel` values. Link text does not announce the new-tab behavior. External content availability and accessibility are outside the direct frontend's control.

## Youth Privacy and Safety

Strengths:

- No public youth search, directory, public portfolio, follower system, direct messaging, or popularity features.
- Youth and staff navigation are role-separated.
- Staff routes use backend authorization as the source of truth.
- ASPN Participant IDs remain internal rather than public handles.
- Legacy public profile and discussion routes were retired previously.

Risks:

- Staff screens expose youth UIDs, participant IDs, school, graduation year, and operational records; staff device/session procedures remain essential.
- A staff account on a shared or unattended device could expose sensitive data.
- Frontend route hiding is not a security boundary; continued backend test coverage is required.
- Profile language states private by default but does not offer a detailed youth-facing privacy explanation.

## Accidental Staff Link Exposure

The frontend determines staff navigation only after a protected backend metrics request succeeds. Youth/member accounts receive the youth shell and backend-denied staff routes. Reserved educator, partner, and government roles do not receive staff access. No reviewed code grants staff privileges based only on a frontend role guess.

---

# 8. Pilot Readiness Assessment

## 60 Youth

**Rating: Ready with Issues**

- **Support burden:** Moderate during account/profile onboarding; manageable after users reach Home.
- **Onboarding risks:** Missing-profile recovery, many initial empty states, credential terminology, and external service-hour form handoff.
- **Training needs:** Short orientation covering Home, Profile, Programs, Learning, Credentials, and privacy.
- **Operational bottlenecks:** Staff review, manual awards, attendance entry, and service-hour verification.
- **UX risks:** Mobile discoverability and mixed youth visual systems.

With seeded programs, learning activities, credential definitions, test notifications, and a staffed support channel, 60 youth is a realistic controlled pilot size.

## 100 Youth

**Rating: Ready with Issues**

- **Support burden:** Moderate to high during synchronized onboarding.
- **Onboarding risks:** The same first-profile issue will occur more frequently and can create a concentrated support queue.
- **Training needs:** Standardized onboarding instructions and staff triage scripts become essential.
- **Operational bottlenecks:** Typed IDs, record-by-record attendance, service-hour queues, and profile verification.
- **UX risks:** Staff must resolve issues quickly without confusing UIDs and participant IDs.

The frontend can display pilot-scale data, but operational discipline matters more at 100 users. Staggered onboarding is preferable to one unsupported launch event.

## 5 Staff

**Rating: Ready with Issues**

- **Support burden:** Manageable if roles are assigned by workflow.
- **Onboarding risks:** Reporting overlap and User/Youth Management ambiguity.
- **Training needs:** One runbook session covering account setup, data definitions, delete policy, and dashboard purpose.
- **Operational bottlenecks:** Shared ownership, duplicate work, and untracked changes outside the platform.
- **UX risks:** Typed identifiers and destructive actions.

Five trained staff can operate the pilot effectively with named owners for onboarding, programs/attendance, credentials/service, and data/reporting.

## 10 Staff

**Rating: Ready with Issues**

- **Support burden:** Lower per person but coordination risk rises.
- **Onboarding risks:** Inconsistent interpretation of statuses, metrics, and archive/delete conventions.
- **Training needs:** Role-based permissions, workflow ownership, naming conventions, and change logging.
- **Operational bottlenecks:** Concurrent edits, duplicated outreach, and fragmented relationship ownership.
- **UX risks:** The platform does not provide task assignment, conflict warnings, or saved staff views.

Ten staff can use the platform, but operational governance is required to prevent inconsistent record handling.

---

# 9. Priority Matrix

## Critical Before Pilot

These are critical remediation or validation items. They do not all imply confirmed code failure.

| Issue | Affected Users | Risk | Recommended Direction | Backend Work Required? |
|---|---|---:|---|---|
| First-time account lands on Home before profile exists, with no direct recovery action in the error state | New youth | Critical | Route new accounts to Profile or add an explicit Complete Profile action when Home detects missing profile data | No, likely frontend only |
| Destructive attendance/service/relationship deletion lacks consistent confirmation and recovery policy | Staff and youth records | Critical | Add confirmation and define delete-versus-correct procedures; prefer auditable status handling where available | Frontend for confirmation; backend only if soft-delete policy changes |
| Manual credential awards and record entry depend on typed UIDs/IDs without identity confirmation | Staff and youth | Critical | Use existing user/catalog data to select and confirm the intended youth and record before submission | Possibly no; depends on current list endpoints |
| Accessibility and device behavior are unverified for representative youth needs | Youth and staff | Critical validation | Run keyboard, screen-reader smoke, 200% zoom, iOS Safari, Android Chrome, and desktop browser QA | No for audit; fixes may be frontend |
| Staff handling of sensitive youth data lacks an in-product safeguard against unattended sessions or mistaken identity | Youth and staff | High/Critical operational | Establish session/device procedures and add identity confirmation around high-impact actions | Mostly operations; future auth/session work may require backend |

## Important Before Pilot

| Issue | Affected Users | Risk | Recommended Direction | Backend Work Required? |
|---|---|---:|---|---|
| Credential Explorer, Notifications, and Service Hours are indirect on mobile | Youth | High | Validate contextual links and consider a mobile More entry in a later implementation plan | No |
| User Management and Youth Management overlap | Staff | High | Clarify labels, descriptions, and runbook ownership | No |
| Six reporting destinations lack visible denominator/threshold guidance | Staff/leadership | High | Add concise definitions or companion guidance near dashboards | Usually no |
| RWD vs Global Civic Movements terminology differs by audience | Staff and youth support | Medium | Document equivalence now; align staff label later | No |
| Programs/Profile/Notifications/Service Hours use older youth styling | Youth | Medium | Align after critical onboarding safeguards | No |
| Service Hours lacks a dedicated pending queue | Staff | High | Use filters and operating rhythm for pilot; plan queue enhancement | Possibly no |
| Attendance entry is record-by-record | Staff | Medium/High | Use small cohorts and staff procedures; plan batch workflow after usage evidence | Likely frontend plus existing APIs; backend may improve efficiency |
| Seed data is required for a credible first session | Youth | High operational | Seed at least one active program, credential set, learning activities, and configured service-hour URL before onboarding | No new architecture |

## Post-Pilot Enhancements

| Issue | Affected Users | Recommended Direction | Backend Work Required? |
|---|---|---|---|
| Long youth pages and repeated page headers | Youth | Refine hierarchy and progressive disclosure | No |
| Program detail lacks schedule, location, eligibility, and capacity context | Youth | Add only fields validated by pilot needs | Possibly |
| Learning cards show credential IDs | Youth | Resolve registry names and icons | No if existing IDs map |
| Interest fields use comma-separated text | Youth | Use structured controls informed by pilot feedback | No schema change if lists remain |
| Staff mobile experience is inefficient | Staff | Create compact staff navigation and prioritize queues | No |
| Inactive RWD records cannot be restored | Staff | Use an all-activities staff list | Backend endpoint may be required |
| Reports lack export, print, and saved views | Staff/research | Add governed exports after data definitions stabilize | Backend and frontend |
| Staff owner reports show UIDs | Staff | Resolve staff display names while retaining UID internally | Possibly backend DTO enhancement |

## Future Vision

| Opportunity | Affected Users | Direction | Backend Work Required? |
|---|---|---|---|
| Dedicated opportunity exploration | Youth | Build after pilot evidence and safety requirements | Yes |
| Workforce, scholarship, and government pathway matching | Youth/partners | Treat as transparent, consent-based future systems | Yes |
| Shareable portfolio | Youth | Require explicit consent, privacy controls, and audience boundaries | Yes |
| Advanced learning pathways | Youth/educators | Expand beyond external links when educational requirements are defined | Yes |
| Causal or predictive analytics | Research/leadership | Establish governance, data quality, and methodological review first | Yes |

---

# 10. Recommended UX Roadmap

## Phase 1 — Before September Pilot

Focus on safe onboarding and error prevention:

1. **Checkpoint 31B — Pre-Pilot UX Fix Plan:** Turn this audit into a bounded implementation and QA sequence with explicit acceptance criteria.
2. **First-Time Onboarding and Recovery:** Ensure account creation leads naturally to profile completion and every missing-profile state has a direct action.
3. **Staff Data-Integrity Safeguards:** Add confirmation and identity context around awards, deletion, and high-impact status changes.
4. **Accessibility and Device QA:** Test representative youth and staff flows across keyboard, zoom, screen reader smoke, iOS Safari, Android Chrome, Chrome, Safari, Firefox, and Edge.
5. **Pilot Content and Empty-State Validation:** Seed realistic content and test new, returning, and partially complete user states.
6. **Staff Workflow Orientation:** Document which screen handles each task and define metrics, archive/delete rules, and staff ownership.

## Phase 2 — During Early Pilot

Use observed behavior rather than assumptions:

1. Track where youth abandon onboarding or require staff help.
2. Review which Home next actions are used and which destinations remain undiscovered.
3. Measure staff time for attendance, awards, service-hour review, and profile verification.
4. Simplify typed-ID workflows using existing user and catalog data.
5. Add or refine operational queues based on actual volume.
6. Align Programs, Profile, Notifications, and Service Hours with the refreshed youth visual system.
7. Clarify reporting labels and metric definitions based on staff interpretation errors.

## Phase 3 — Post-Pilot

Expand only after pilot findings and governance review:

1. Consolidate the design system and component library across youth and staff.
2. Improve staff mobile navigation and high-volume workflows.
3. Add governed reporting exports, print views, and research workflows.
4. Build opportunity exploration and pathway expansion without opaque matching.
5. Evaluate workforce, government, scholarship, and educator integrations.
6. Design consent-based portfolio sharing with explicit privacy controls.
7. Expand learning administration and progress tools if ASPN moves toward deeper LMS functionality.

---

# Audit Conclusion

The ASPN Platform is best understood as a pilot-capable youth development operating system supported by a comprehensive staff administration layer. The youth side now has a clear conceptual center and a distinctive credential/pathway identity. The staff side is operationally broad but requires training and safeguards because its complexity has outgrown purely self-explanatory navigation.

**Controlled pilot QA:** Ready.  
**Controlled pilot launch:** Ready with issues and pre-pilot conditions.  
**Public launch:** Not ready; accessibility validation, production operations, broader security/deployment work, and post-pilot UX refinement remain necessary.

This audit recommends Checkpoint 31B as the next step. No route, component, CSS, backend, API, Firebase, Firestore, authentication, or dependency changes were made.
