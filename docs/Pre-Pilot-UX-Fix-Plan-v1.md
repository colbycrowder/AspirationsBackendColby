# ASPN Platform Pre-Pilot UX Fix Plan

**Version:** Checkpoint 31B  
**Date:** June 22, 2026  
**Inputs:** Youth UX Snapshot v1, Staff UX Snapshot v1, and UX Audit v1.

---

# 1. Executive Summary

The ASPN Platform is **Ready with Issues** for each evaluated controlled-pilot scenario. Core youth and staff workflows exist, role separation is preserved, empty states are generally honest, and the platform has a clear youth-development identity. The remaining pre-pilot work is concentrated in onboarding recovery, data-integrity safeguards, accessibility/device validation, seeded pilot content, and staff operating discipline.

| Pilot Group | Current Rating | Rationale |
|---|---|---|
| 60 youth | **Ready with Issues** | Core flows are usable, but profile recovery, mobile discoverability, accessibility validation, and realistic seed data need attention. Support demand should be manageable with trained staff. |
| 100 youth | **Ready with Issues** | The same issues apply at greater frequency. Synchronized onboarding could create a concentrated support queue, and record-by-record staff workflows may become bottlenecks. |
| 5 staff | **Ready with Issues** | Five trained staff can operate the pilot, but typed identifiers, destructive actions, overlapping management screens, and reporting definitions require safeguards and role assignment. |
| 10 staff | **Ready with Issues** | More staff capacity reduces individual load but increases coordination, concurrent-edit, naming, ownership, and inconsistent-record-handling risk. |

The platform should not onboard real youth until the Required Before Pilot items in Section 4 are completed or formally mitigated and signed off. Visual polish, charts, exports, advanced matching, and full staff-mobile redesign are not prerequisites for the controlled pilot.

---

# 2. Issue Inventory

The inventory below consolidates every actionable gap identified across the three source documents. Similar statements were combined only when they describe the same underlying issue.

## Youth Experience Issues

| ID | Description | Affected Users | Audit Source | Risk |
|---|---|---|---|---|
| Y01 | Account creation can land on Home before a Firestore profile exists; the failure state explains the problem but provides no direct Profile action. | New youth, support staff | UX Audit §§1, 3 Home, 9 | **Critical** |
| Y02 | No multi-step onboarding checklist, guided first-session flow, or visible onboarding sequence exists. | New youth | Youth Snapshot: Missing Guidance; UX Audit §§3, 8 | **High** |
| Y03 | New users may see several empty sections at once, making an unseeded environment feel unfinished. | New youth | Youth Snapshot: Potential Onboarding Issues; UX Audit §§3, 8 | **High** |
| Y04 | Credential Explorer, Notifications, and Service Hours are not mobile bottom tabs and depend on contextual links. | Mobile youth | Youth Snapshot: Mobile Navigation; UX Audit §§2, 5, 9 | **High** |
| Y05 | Credential, pathway, civic role family, My Journey, and Global Civic Movements concepts may need age-adjusted explanation for ages 8–24. | Younger youth, new youth | UX Audit §2 Youth IA | **Medium** |
| Y06 | Home displays at most five next actions, so lower-priority needs can be hidden when many needs coexist. | Youth with incomplete setup | UX Audit §3 Home | **Low** |
| Y07 | Programs lack schedule, location, eligibility, remaining-capacity, and richer detail context. | Youth, program staff | UX Audit §§2, 3 Programs, 9 | **Medium** |
| Y08 | Programs, Profile, Notifications, Service Hours, and authentication use older light styling while refreshed youth pages use the dark ASPN theme. | Youth | Youth Snapshot: Visual Gaps; UX Audit §§1, 6 | **Medium** |
| Y09 | My Journey program entries lack enrollment dates and display “Date not available.” | Youth | Youth Snapshot: My Journey; UX Audit §3 My Journey | **Low** |
| Y10 | Global Civic Movements displays associated credential IDs instead of youth-facing names and icons. | Youth | Youth Snapshot: Credential Relationships; UX Audit §§2, 3 | **Medium** |
| Y11 | Credential Explorer uses Explore state but does not show exact earning requirements or progress. | Youth | Youth Snapshot: Confusing Areas; UX Audit §§2, 3 | **Medium** |
| Y12 | Profile interests use comma-separated text instead of structured controls. | Youth | Youth Snapshot: Onboarding Issues; UX Audit §3 Profile | **Medium** |
| Y13 | Profile email appears editable even though Firebase Authentication remains the login identity. | Youth, support staff | Youth Snapshot: Onboarding Issues; UX Audit §3 Profile | **High** |
| Y14 | Youth receive no in-product explanation of the private ASPN Participant ID and its purpose. | Youth | Youth Snapshot: Missing Guidance; UX Audit §§3, 7 | **Medium** |
| Y15 | Notifications shows both Unread and Dashboard Unread counts, which may temporarily differ and confuse users. | Youth | UX Audit §3 Notifications | **Low** |
| Y16 | Service Hours displays operational program IDs and verification sources instead of consistently youth-friendly labels. | Youth | UX Audit §3 Service Hours | **Medium** |
| Y17 | Learning, opportunity, and service-hour links open external tabs without announcing that behavior; the service form creates an external context switch. | Youth | Youth Snapshot: Accessibility; UX Audit §§3, 7 | **Medium** |
| Y18 | No dedicated Opportunities page or opportunity-selection explanation exists. | Youth | Youth Snapshot: Missing Guidance; UX Audit §§2, 9 | **Low** |
| Y19 | Youth Attendance is a placeholder route and intentionally absent from current youth navigation. | Youth | UX Audit §2 Youth IA | **Low** |
| Y20 | Upcoming Activities is a static placeholder rather than a populated schedule. | Youth | Youth Snapshot: Missing Guidance; UX Audit §3 Home | **Low** |
| Y21 | Shell titles and page-level heroes sometimes repeat page names and add noise. | Youth and staff | Youth Snapshot: Confusing Areas; UX Audit §§6, 7 | **Low** |

## Staff Workflow Issues

| ID | Description | Affected Users | Audit Source | Risk |
|---|---|---|---|---|
| S01 | Eighteen staff routes create a long navigation structure. | Staff | Staff Snapshot: Navigation Complexity; UX Audit §2 Staff IA | **Medium** |
| S02 | User Management and Youth Management overlap, and their distinction is not obvious without training. | Staff | Staff Snapshot: Navigation Complexity; UX Audit §§2, 4 | **High** |
| S03 | Metrics, Reporting, Pilot Metrics, Pilot Readiness, Pilot Evaluation, and Operations Reporting are difficult to distinguish without definitions. | Staff, leadership | Staff Snapshot: Navigation/Reporting; UX Audit §§2, 4 | **High** |
| S04 | Staff uses RWD terminology while youth uses Global Civic Movements. | Staff and youth support | Staff Snapshot: Navigation Complexity; UX Audit §§2, 4, 9 | **Medium** |
| S05 | Staff Dashboard provides shortcuts and counts but does not surface urgent queues such as pending service hours, required profile reviews, or overdue follow-ups. | Staff | UX Audit §§2, 4 Staff Dashboard | **Medium** |
| S06 | Credential awards and several record workflows rely on manually typed UIDs and IDs without strong identity confirmation. | Staff, youth records | Staff Snapshot: Workflow Friction; UX Audit §§1, 4, 9 | **Critical** |
| S07 | Program Management emphasizes aggregate enrollment counts rather than a clear named roster and removal workflow. | Program staff | Staff Snapshot: Program Management; UX Audit §§2, 4 | **High** |
| S08 | Service Hours lacks a dedicated pending-review queue. | Service reviewers | Staff Snapshot: Workflow Friction; UX Audit §§4, 9 | **High** |
| S09 | Deactivated Global Civic Movements activities disappear from the active-only staff list and cannot be restored in the current UI. | Learning staff | Staff Snapshot: Learning; UX Audit §§2, 4, 9 | **High** |
| S10 | Full credential auto-award configuration and behavior are not visible or manageable in the staff UI. | Credential/attendance staff | Staff Snapshot: Credential Management; UX Audit §§2, 4 | **High** |
| S11 | The staff credential `icon` text field is not clearly aligned with the youth frontend asset registry. | Credential staff | UX Audit §§4, 6 | **Medium** |
| S12 | Attendance is entered record by record, which may become slow at 60–100 youth. | Attendance staff | UX Audit §§4, 8, 9 | **High** |
| S13 | Attendance status changes can trigger credential evaluation without visible preview or confirmation. | Attendance/credential staff, youth | UX Audit §4 Attendance | **High** |
| S14 | Relationship ownership and operation reporting show staff UIDs instead of staff names. | Staff | Staff Snapshot: Workflow Friction; UX Audit §§2, 4, 9 | **Medium** |
| S15 | Filter states are not consistently preserved or communicated after reload. | Staff | Staff Snapshot: Workflow Friction | **Low** |
| S16 | Reports lack charts, exports, saved views, and print layouts. | Staff, leadership, research | Staff Snapshot: Reporting Limitations; UX Audit §§4, 9 | **Low** |
| S17 | Pilot aggregation is designed for pilot scale and may depend on broad in-memory/collection processing. | Staff, platform operations | Staff Snapshot: Reporting Limitations; UX Audit §4 | **Medium** |
| S18 | Dashboard thresholds, denominators, data freshness, and formulas are not always visible near metrics. | Staff, leadership | Staff Snapshot: Reporting Limitations; UX Audit §§2, 4, 9 | **High** |
| S19 | Staff-operation reports cover tracked platform actions, not every historical or external change. | Staff, auditors | Staff Snapshot: Reporting Limitations; UX Audit §4 | **Medium** |
| S20 | Attendance, service-hour, and relationship-note deletion lacks consistent confirmation or recovery. | Staff, youth records | Staff Snapshot: Operational Risks; UX Audit §§1, 4, 9 | **Critical** |
| S21 | Sensitive youth/contact data appears in staff screens and requires secure device/session handling. | Youth, staff | Staff Snapshot: Security/Operational Risks; UX Audit §§7, 9 | **Critical** |
| S22 | The frontend staff gate depends on the metrics endpoint; a metrics failure can block otherwise healthy staff screens. | Staff | Staff Snapshot: Operational Risks | **Medium** |
| S23 | Changes made outside the platform may not create staff-operation events. | Staff, auditors | Staff Snapshot: Operational Risks; UX Audit §§4, 8 | **Medium** |
| S24 | Active, inactive, archived, rejected, and deleted conventions vary by record type. | Staff | Staff Snapshot: Operational Risks; UX Audit §8 | **Medium** |
| S25 | Ten staff can create concurrent edits, duplicated outreach, and fragmented ownership; no task assignment or conflict warning exists. | Larger staff team | UX Audit §8: 10 Staff | **Medium** |
| S26 | Staff mobile navigation is long and in-flow; forms, tables, and action groups are inefficient on phones. | Mobile staff | Staff Snapshot: Mobile; UX Audit §5 | **High** |
| S27 | Government and other complex management forms are dense on small screens. | Mobile staff | UX Audit §§4, 5 | **Medium** |
| S28 | Browser prompt-based credential renaming interrupts the normal staff workflow. | Credential staff, assistive-tech users | UX Audit §§6, 7 | **Medium** |
| S29 | Staff learning administration does not show participant-level progress or quiz/completion rosters. | Learning staff | Staff Snapshot: Progress Visibility; UX Audit §4 | **Medium** |

## Accessibility, Safety, and Operational Issues

| ID | Description | Affected Users | Audit Source | Risk |
|---|---|---|---|---|
| A01 | Focus styling is not consistently defined for custom text actions, record cards, and compact controls. | Keyboard users | UX Audit §7 Keyboard | **High** |
| A02 | Long pages lack skip links or landmark shortcuts. | Keyboard and screen-reader users | UX Audit §7 Keyboard | **Medium** |
| A03 | Dynamic success, error, and status updates are not consistently announced with live regions. | Screen-reader users | Youth/Staff Snapshots; UX Audit §7 | **High** |
| A04 | Placeholder-only compact staff inputs may lack programmatic labels. | Screen-reader and cognitive-access users | UX Audit §§6, 7 | **High** |
| A05 | Reporting tables need caption and contextual-label review. | Screen-reader users | UX Audit §7 Screen Readers | **Medium** |
| A06 | Color contrast, forced-colors, high-contrast mode, and status differentiation have not been formally validated. | Low-vision users | Youth/Staff Snapshots; UX Audit §7 | **Critical** |
| A07 | Physical-device and browser validation is incomplete for iOS Safari, Android Chrome, Chrome, Safari, Firefox, and Edge. | All users | Youth/Staff Snapshots; UX Audit §§5, 7, 9 | **Critical** |
| A08 | Fixed mobile navigation and action groups need virtual-keyboard, zoom, safe-area, and touch-target validation. | Mobile youth and staff | UX Audit §§5, 7 | **Critical** |
| A09 | Reduced-motion and 200% zoom behavior have not been formally recorded. | Motion-sensitive and low-vision users | Youth Snapshot: Accessibility; UX Audit §§7, 9 | **Medium** |
| O01 | Pilot seed data is required for programs, credentials, learning activities, service-hour URL, and realistic empty-state testing. | Youth and staff | UX Audit §§3, 8, 9 | **High** |
| O02 | Staff need training on identifiers, delete/archive rules, reporting definitions, and workflow ownership. | Staff | UX Audit §§8, 9, 10 | **High** |
| O03 | Youth need a short orientation covering Home, Profile, Programs, Learning, Credentials, service, and privacy. | Youth | UX Audit §8 | **High** |
| O04 | A 100-youth launch should be staggered to avoid concentrated onboarding and review queues. | Youth and staff | UX Audit §8: 100 Youth | **Medium** |
| O05 | Profile privacy language does not fully explain data use or ASPN Participant IDs. | Youth and families | Youth Snapshot; UX Audit §7 | **Medium** |

## Future-System Issues and Opportunities

| ID | Description | Affected Users | Audit Source | Risk |
|---|---|---|---|---|
| F01 | Dedicated opportunity exploration does not yet exist. | Youth | UX Audit §§2, 9, 10 | **Low** |
| F02 | Workforce, scholarship, and government pathway matching are future systems requiring transparency and consent. | Youth, partners | UX Audit §§9, 10 | **Low** |
| F03 | Shareable portfolios require explicit consent, audience controls, and privacy safeguards. | Youth | UX Audit §§9, 10 | **Low** |
| F04 | Advanced exports, print views, and research reporting are not available in the frontend. | Staff, research | UX Audit §§4, 9, 10 | **Low** |
| F05 | Deeper LMS-style learning administration is not present. | Youth, educators | UX Audit §§1, 9, 10 | **Low** |
| F06 | Causal, predictive, or recommendation analytics require governance and methodological review. | Youth, research, leadership | UX Audit §§4, 9, 10 | **Low** |

---

# 3. Prioritization Matrix

| ID | Severity | Effort | Pilot Requirement |
|---|---|---|---|
| Y01 | Critical | Low | **Required Before Pilot** |
| Y02 | High | Medium | Recommended Before Pilot |
| Y03 | High | Low | **Required Before Pilot** through seed data and QA |
| Y04 | High | Low–Medium | Recommended Before Pilot |
| Y05 | Medium | Low | Can Wait Until Pilot |
| Y06 | Low | Low | Can Wait Until Pilot |
| Y07 | Medium | High | Post-Pilot |
| Y08 | Medium | Medium | Recommended Before Pilot for validation; full alignment Post-Pilot |
| Y09 | Low | Medium | Post-Pilot |
| Y10 | Medium | Low | Recommended Before Pilot |
| Y11 | Medium | Medium | Can Wait Until Pilot |
| Y12 | Medium | Medium | Can Wait Until Pilot |
| Y13 | High | Low–Medium | Recommended Before Pilot |
| Y14 | Medium | Very Low | Recommended Before Pilot |
| Y15 | Low | Very Low | Can Wait Until Pilot |
| Y16 | Medium | Low–Medium | Can Wait Until Pilot |
| Y17 | Medium | Very Low | Recommended Before Pilot |
| Y18 | Low | High | Future Vision |
| Y19 | Low | High | Post-Pilot |
| Y20 | Low | High | Post-Pilot |
| Y21 | Low | Medium | Post-Pilot |
| S01 | Medium | Medium | Post-Pilot |
| S02 | High | Very Low | Recommended Before Pilot |
| S03 | High | Low | Recommended Before Pilot |
| S04 | Medium | Very Low | Recommended Before Pilot |
| S05 | Medium | Medium | Can Wait Until Pilot |
| S06 | Critical | Medium | **Required Before Pilot** |
| S07 | High | Medium | Recommended Before Pilot or mitigate operationally |
| S08 | High | Medium | Recommended Before Pilot or mitigate with filters |
| S09 | High | Medium–High | Can Wait Until Pilot with documented limitation |
| S10 | High | High | Can Wait Until Pilot with manual award procedures |
| S11 | Medium | Very Low | Recommended Before Pilot through documentation |
| S12 | High | High | Can Wait Until Pilot with staffing plan |
| S13 | High | Low | Recommended Before Pilot |
| S14 | Medium | Medium | Can Wait Until Pilot |
| S15 | Low | Medium | Post-Pilot |
| S16 | Low | High | Post-Pilot |
| S17 | Medium | High | Can Wait Until Pilot with monitoring |
| S18 | High | Low | Recommended Before Pilot |
| S19 | Medium | Very Low | Recommended Before Pilot through documentation |
| S20 | Critical | Low–Medium | **Required Before Pilot** |
| S21 | Critical | Very Low operationally | **Required Before Pilot** |
| S22 | Medium | Medium | Can Wait Until Pilot with incident procedure |
| S23 | Medium | Very Low operationally | **Required Before Pilot** through policy |
| S24 | Medium | Very Low operationally | **Required Before Pilot** through training |
| S25 | Medium | Medium | Can Wait Until Pilot with ownership plan |
| S26 | High | High | Recommended Before Pilot if mobile staff use is expected; otherwise Post-Pilot |
| S27 | Medium | Medium | Post-Pilot |
| S28 | Medium | Low | Recommended Before Pilot |
| S29 | Medium | High | Post-Pilot |
| A01 | High | Low | **Required Before Pilot** if QA confirms gaps |
| A02 | Medium | Low | Recommended Before Pilot |
| A03 | High | Low | **Required Before Pilot** |
| A04 | High | Low–Medium | **Required Before Pilot** |
| A05 | Medium | Low | Recommended Before Pilot |
| A06 | Critical | Medium validation | **Required Before Pilot** |
| A07 | Critical | Medium validation | **Required Before Pilot** |
| A08 | Critical | Medium validation | **Required Before Pilot** |
| A09 | Medium | Low validation | Recommended Before Pilot |
| O01 | High | Low | **Required Before Pilot** |
| O02 | High | Low | **Required Before Pilot** |
| O03 | High | Low | **Required Before Pilot** |
| O04 | Medium | Very Low operationally | Required for a 100-youth launch plan |
| O05 | Medium | Very Low | Recommended Before Pilot |
| F01 | Low | Very High | Future Vision |
| F02 | Low | Very High | Future Vision |
| F03 | Low | Very High | Future Vision |
| F04 | Low | High | Post-Pilot |
| F05 | Low | Very High | Future Vision |
| F06 | Low | Very High | Future Vision |

---

# 4. Required Before Pilot

This section contains only items that should be completed or formally mitigated before real youth onboarding.

## Y01 — First-Time Onboarding Recovery

**Why it matters:** A successful Firebase account can still lack the Firestore profile required by Home. A new youth should not reach a dead-end error during their first session.

**Suggested direction:** Route newly created accounts to Profile or provide a prominent Complete Profile action when Home identifies missing profile data. Preserve protected UID handling.

**Backend required:** No expected backend change.  
**Can training alone mitigate:** No. Staff instructions help, but the youth flow should recover without staff intervention.

## S06 — Identity-Safe Staff Record Actions

**Why it matters:** A syntactically valid but incorrect UID can award a credential or create a record for the wrong youth.

**Suggested direction:** Use existing user/catalog data for selection, display youth name plus ASPN Participant ID before submission, and require confirmation for high-impact actions. Retain Firebase UID as the backend operational identifier.

**Backend required:** Possibly not; current list/detail endpoints may be enough.  
**Can training alone mitigate:** Only temporarily. Training reduces but does not remove wrong-record risk.

## S20 — Destructive-Action Safeguards

**Why it matters:** One-click deletion of attendance, service-hour, or relationship records can damage auditability and youth records.

**Suggested direction:** Add consistent confirmation with record identity and consequence. Define when staff should correct, reject, deactivate, archive, or delete. Consider soft deletion only as a separately approved backend policy.

**Backend required:** No for confirmation; yes only for a future soft-delete model.  
**Can training alone mitigate:** Partially, but a UI safeguard is still required.

## A03/A04 — Screen-Reader Status and Form Labels

**Why it matters:** Unannounced results and unlabeled compact inputs can make core staff workflows inaccessible.

**Suggested direction:** Add live-region semantics to asynchronous messages and explicit accessible labels to compact form controls.

**Backend required:** No.  
**Can training alone mitigate:** No.

## A06/A07/A08 — Accessibility, Browser, and Device Validation

**Why it matters:** Current responsive and contrast behavior has been reviewed in code but not validated across representative assistive technology, devices, and browsers.

**Suggested direction:** Execute a documented matrix covering keyboard-only use, screen-reader smoke tests, 200% zoom, contrast, forced colors, iOS Safari, Android Chrome, Chrome, Safari, Firefox, Edge, safe areas, virtual keyboards, and touch targets. Remediate pilot-blocking failures.

**Backend required:** No for testing; discovered issues should normally be frontend-only.  
**Can training alone mitigate:** No.

## S21/S23/S24/O02 — Staff Data-Handling Procedures

**Why it matters:** Staff screens expose sensitive information, external changes may bypass operation tracking, and status/delete conventions differ by record type.

**Suggested direction:** Complete staff training and written procedures for secure devices, logout, shared-device avoidance, identifier use, workflow ownership, tracked versus external changes, archive/delete decisions, and reporting definitions.

**Backend required:** No.  
**Can training alone mitigate:** Yes for the pilot, provided completion is documented and supervised.

## O01/Y03 — Seeded Pilot Content and State Validation

**Why it matters:** An environment with no programs, definitions, learning activities, service request URL, or representative records makes first sessions appear unfinished and prevents realistic QA.

**Suggested direction:** Seed approved pilot content in `(default)` Firestore and validate new, partial, active, and returning youth states before launch.

**Backend required:** No new backend work.  
**Can training alone mitigate:** No; data must exist.

## O03 — Youth Orientation

**Why it matters:** Youth need a common explanation of Home, Profile, Programs, Learning, Credentials, Service Hours, Notifications, privacy, and support channels.

**Suggested direction:** Prepare a short staff-led onboarding script and youth quick-start procedure using the existing screens.

**Backend required:** No.  
**Can training alone mitigate:** Yes; this is an operational requirement.

## O04 — Staggered 100-Youth Launch Plan

**Why it matters:** The profile issue, manual review, attendance, and service-hour workload can create a concentrated support queue at 100 simultaneous users.

**Suggested direction:** Use cohorts or onboarding windows, assign triage ownership, and define escalation before inviting all 100 youth.

**Backend required:** No.  
**Can training alone mitigate:** Training plus scheduling can mitigate this for the controlled pilot.

---

# 5. Recommended Before Pilot

These improvements reduce support and confusion but do not individually block a controlled launch if staff mitigation is in place.

| IDs | Improvement | Recommended Direction |
|---|---|---|
| Y02 | Guided onboarding | Add a concise first-session checklist after Y01 is solved. |
| Y04 | Mobile secondary-tool discovery | Validate Home/Journey links and evaluate a compact More destination. |
| Y08 | Older youth page validation | Test Programs, Profile, Notifications, and Service Hours on phones; defer full redesign if usable. |
| Y10 | Learning credential labels | Resolve associated credential IDs to registry names/icons where possible. |
| Y13 | Email identity clarity | Make the authentication/profile distinction explicit or prevent unsupported edits. |
| Y14/O05 | Participant ID and privacy explanation | Add brief plain-language explanation without exposing internal research details. |
| Y17 | External-link clarity | Indicate that external experiences and forms open outside ASPN. |
| S02 | User/Youth Management distinction | Clarify page descriptions and staff runbook ownership. |
| S03/S18 | Reporting clarity | Add concise metric definitions, denominators, thresholds, and freshness guidance. |
| S04 | Terminology alignment | Document RWD = Global Civic Movements now; rename staff labels later. |
| S07 | Enrollment visibility | Confirm pilot staff can access needed rosters/removal workflows or document the alternative. |
| S08 | Service-hour review | Establish a filtered pending-review operating view and rhythm. |
| S11 | Credential icon field | Document that the youth registry artwork is separate from the staff definition field. |
| S13 | Attendance credential consequence | Add visible explanatory text or confirmation around credential evaluation. |
| S19 | Operation-report scope | Document which actions produce operation events. |
| S26 | Staff mobile expectations | Declare desktop/tablet as the primary staff environment unless mobile QA proves otherwise. |
| S28 | Prompt-based rename | Move rename into the existing detail form when implementing staff safeguards. |
| A01/A02/A05/A09 | Accessibility refinements | Apply findings from 31E, including focus, skip navigation, table context, zoom, and motion review. |

---

# 6. Pilot-Phase Improvements

These items should be informed by real usage rather than implemented entirely from assumption.

1. **Y05:** Observe which terms younger and older participants misunderstand.
2. **Y06:** Measure whether the five-action Home limit hides important tasks.
3. **Y11:** Record questions about credential earning requirements before designing progress presentation.
4. **Y12:** Observe how youth enter interests before choosing structured controls.
5. **Y15:** Track whether notification summary counts cause confusion.
6. **Y16:** Identify which service-hour fields youth actually need.
7. **S05:** Determine which urgent queues staff check most often before changing Staff Dashboard priorities.
8. **S08:** Measure pending service-hour volume and review time.
9. **S09:** Track whether activity restoration is needed during the pilot.
10. **S10:** Determine whether staff need auto-award rule editing or whether fixed configuration is safer.
11. **S12:** Measure attendance-entry time before designing a batch workflow.
12. **S14:** Determine whether owner-name resolution materially improves coordination.
13. **S17:** Monitor report latency and Firestore read behavior at pilot scale.
14. **S22:** Document metrics-endpoint incidents before changing the staff-gate architecture.
15. **S25:** Observe concurrent staff conflicts and duplicate outreach.

---

# 7. Post-Pilot Improvements

## Near-Term Post-Pilot

- Align Programs, Profile, Notifications, Service Hours, and authentication with the current youth visual system.
- Add program enrollment dates and richer program detail if supported by pilot needs.
- Improve long-page hierarchy and remove repeated headers.
- Build a dedicated pending service-hour queue if volume justifies it.
- Add attendance batch entry if staff time data supports it.
- Add an all-activities staff list and restore workflow for Global Civic Movements.
- Resolve staff names in ownership and operations reports.
- Improve staff mobile navigation.
- Add saved filters or views only where repeated pilot behavior demonstrates value.
- Add governed print/export functionality after metric definitions stabilize.

## Workforce Matching

Workforce, scholarship, government, and public-sector pathway matching remain Future Vision items. They should be transparent, consent-based, and governed. No matching should be inferred from exploratory civic role families.

## Portfolio Sharing

Public or shareable portfolios require explicit youth consent, audience controls, revocation, privacy review, and safeguarding. The private My Journey record should not become public by default.

## Advanced Reporting

Potential post-pilot work includes governed exports, print views, saved report configurations, staff-name resolution, denominator documentation, and performance improvements. Causal or predictive claims require separate methodological review.

## Opportunity Systems

A dedicated Opportunities experience should be based on pilot needs and content governance. It should not become an unmoderated job board or opaque recommendation feed.

## Other Long-Term Enhancements

- Deeper LMS-style learning administration if ASPN requirements expand.
- Task ownership and conflict handling for larger staff teams.
- Advanced research integrations and external dataset workflows.
- Consent-based partner, educator, and government experiences only after role and safety design.

---

# 8. Recommended Implementation Sequence

## 31C — First-Time Onboarding Recovery

Resolve Y01 first. It affects every new youth and is independent of visual redesign. Include clear missing-profile recovery and preserve verified UID ownership.

## 31D — Staff Data-Integrity Safeguards

Address S06, S20, and S13. Add identity context, confirmations, and consistent destructive-action handling before staff work with real youth records.

## 31E — Accessibility and Device QA

Execute A01–A09 across representative youth/staff workflows and browsers. Implement only verified pilot-blocking accessibility and responsive fixes.

## 31F — Pilot Content and Operational Validation

Complete O01–O04: seed approved content, test lifecycle states, run staff training, validate workflow ownership, and rehearse 60- and 100-youth onboarding scenarios.

## 31G — Reporting and Staff Workflow Clarity

Clarify S02, S03, S04, S11, S18, S19, S23, and S24 through labels, definitions, and operating guidance. Avoid architecture changes.

## 31H — Youth Navigation and Visual Consistency

Address Y04, Y08, Y10, Y13, Y14, and Y17 after safety and onboarding work. Keep the dashboard-centered product direction.

## Pilot Feedback Checkpoint

After early pilot use, reassess Y05, Y06, Y11, Y12, S05, S08, S09, S10, S12, S14, S17, S22, and S25 before adding new workflow complexity.

---

# 9. Minimal Viable Pilot Checklist

## Youth Onboarding

- [ ] Firebase account creation tested with a brand-new youth account.
- [ ] First-time profile recovery directs the youth to Profile without staff intervention.
- [ ] Profile save creates/updates the private profile and ASPN Participant ID as expected.
- [ ] Home loads for new, partial, and returning profiles.
- [ ] Youth orientation explains Home, Profile, Programs, Learning, Credentials, Service Hours, Notifications, and privacy.

## Youth Workflows

- [ ] At least one approved active program is available and enrollment is tested.
- [ ] Duplicate enrollment behavior is tested.
- [ ] Approved credential definitions and youth-facing registry artwork are present.
- [ ] Manual credential award is tested against a confirmed youth identity.
- [ ] Global Civic Movements activities and progress updates are tested.
- [ ] Service-hour request URL is configured and its external handoff is tested.
- [ ] Youth notifications and mark-read behavior are tested.
- [ ] Home and My Journey update after participation records change.

## Staff Workflows

- [ ] Staff and admin access is verified; member and reserved roles remain denied.
- [ ] User/youth review ownership is assigned.
- [ ] Program create/update/archive/restore is tested.
- [ ] Attendance create/update/delete safeguards are tested.
- [ ] Service-hour pending/verified/rejected workflow and deletion safeguards are tested.
- [ ] Credential award confirmation identifies the intended youth and definition.
- [ ] Global Civic Movements creation/update/deactivation limitation is documented.
- [ ] Educator, partner, government, and relationship-note workflows are tested if used in pilot operations.
- [ ] Staff understand which actions are captured in Operations Reporting.

## Accessibility and Devices

- [ ] Keyboard-only youth and staff smoke tests pass.
- [ ] Screen-reader smoke tests cover account creation, profile, Home, enrollment, credential viewing, and key staff forms.
- [ ] 200% zoom and high-contrast checks pass for critical workflows.
- [ ] iOS Safari and Android Chrome youth flows pass.
- [ ] Chrome, Safari, Firefox, and Edge desktop smoke tests pass.
- [ ] Mobile safe areas, virtual keyboard, bottom navigation, and touch targets pass.
- [ ] All critical inputs have accessible labels and dynamic results are announced.

## Pilot Data and Operations

- [ ] Seed content exists in the active `(default)` Firestore database.
- [ ] Staff accounts use controlled `staff` or `admin` roles.
- [ ] Secure-device, logout, identifier, and data-handling procedures are acknowledged.
- [ ] Archive, reject, correct, and delete rules are documented.
- [ ] Reporting definitions, denominators, and dashboard purposes are reviewed with staff.
- [ ] Staff workflow owners and escalation contacts are assigned.
- [ ] A staggered onboarding plan exists for a 100-youth launch.
- [ ] Support and incident triage procedures are ready.

---

# 10. Go/No-Go Recommendation

## Onboarding 60 Youth Today

**Recommendation: Go with Conditions**

The platform has the required functional coverage, but real youth onboarding should begin only after Y01, S06, S20, A03/A04, A06–A08, O01, O02, and O03 are complete or formally mitigated. A small internal cohort may continue QA while those conditions are addressed.

For 60 youth, support burden is manageable with seeded content, trained staff, verified devices, and a clear escalation process.

## Onboarding 100 Youth Today

**Recommendation: Go with Conditions, using staggered cohorts**

The frontend and backend foundations are pilot-scale, but simultaneous onboarding would magnify profile recovery, support, verification, attendance, service-hour, and typed-ID risks. Do not invite all 100 youth in one unsupported wave.

Proceed only after the same conditions required for 60 youth are complete and O04 provides a staged rollout, assigned staff owners, and queue-management plan.

## Final Decision Standard

This is not an unconditional launch approval. The controlled pilot moves from **Ready with Issues** to operational **Go** when:

1. First-time onboarding is recoverable.
2. High-impact staff actions are identity-safe and deletion-safe.
3. Accessibility and device QA has no unresolved critical failures.
4. Approved pilot content is seeded.
5. Staff and youth orientation procedures are complete.

The next recommended checkpoint is **31C — First-Time Onboarding Recovery**.
