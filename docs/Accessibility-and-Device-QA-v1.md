# ASPN Platform Accessibility and Device QA

**Version:** Checkpoint 31E  
**Date:** June 23, 2026  
**Scope:** React/Vite frontend, youth routes, staff routes, shared navigation, responsive CSS, form patterns, external-link behavior, and pilot-readiness accessibility risks.

---

# 1. Executive Summary

The ASPN Platform is **Pilot Ready with Accessibility and Device Risks** for a controlled pilot, assuming staff use laptops or tablets for routine operations and youth receive a short orientation. The youth experience is generally mobile-ready at the layout level, especially Home, My Journey, Global Civic Movements, and Credential Explorer. The staff experience is usable on laptops and tablets, but staff mobile use should remain limited because forms, record lists, and reporting screens are dense.

This checkpoint included a structured static/code-level review of accessibility, keyboard visibility, form patterns, external links, and responsive breakpoints. It did not include physical-device testing with iOS Safari, Android Chrome, screen readers, or forced-colors mode; those remain required before broad onboarding.

Small remediation was applied for obvious low-risk issues:

- Stronger visible keyboard focus treatment for navigation, staff record cards, compact forms, and action buttons.
- Youth-facing external links now state when they open in a new tab.

No backend, Firestore, authentication, or role-permission changes were made.

---

# 2. Accessibility Findings

## Keyboard Navigation

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Important Before Pilot | Compact staff forms and record-card controls relied too heavily on browser/default focus behavior, which could be easy to miss in dense workflows. | Staff management screens, staff mini tables, compact forms | Remediated with explicit `:focus-visible` styles. |
| Important Before Pilot | Long staff pages can require many tab stops before reaching later actions. | Reporting, Pilot Dashboards, Relationship Notes, Program/Credential/Attendance/Service management | Remaining risk; acceptable for pilot with laptop/tablet use and staff training. |
| Recommended During Pilot | Youth secondary tools are reachable by desktop nav and contextual links, but mobile youth cannot tab to hidden desktop routes from the bottom nav. | Credential Explorer, Notifications, Service Hours | Remaining discoverability risk, not a keyboard trap. |

No keyboard trap was identified in the reviewed React structure. Buttons and links are mostly semantic controls rather than non-interactive elements with click handlers.

## Focus Visibility

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Important Before Pilot | Focus visibility needed stronger consistency across nav links, staff action buttons, compact form fields, staff record-card buttons, and mini-table buttons. | Shared CSS, staff screens | Remediated. |
| Recommended During Pilot | Some older light-theme youth screens and dense staff screens use different focus styles from refreshed dark youth screens. | Programs, Profile, Notifications, Service Hours, staff forms | Partially mitigated; full design-system alignment can wait. |

## Form Accessibility

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Important Before Pilot | Many compact staff inputs use placeholder-only labels, which is weak for screen-reader and cognitive-access support. | User, Program, Credential, Attendance, Service Hours, Relationships, Organization screens | Remaining risk. Full label remediation is larger than this checkpoint but should be prioritized. |
| Important Before Pilot | Required fields depend on native browser validation and visible placeholders rather than consistent helper text. | Staff compact forms, some youth forms | Remaining risk. |
| Recommended During Pilot | Profile interest fields use comma-separated free text. | Youth Profile | Existing known issue; no 31E change. |

The youth Profile page uses visible labels and is stronger than the compact staff form pattern.

## Contrast Review

The current ASPN dark palette appears generally suitable for the refreshed youth surfaces:

- `#f9fbff` on `#03045e` / `#0e0e64` provides strong primary text contrast.
- `#b2dfff` and `#81c7f8` are readable accents on dark surfaces.
- `rgba(203,207,226,0.86)` is likely acceptable for supporting text on the dark backgrounds.

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Important Before Pilot | Formal contrast validation has not been recorded for forced-colors, high-contrast mode, or all status chips. | Status pills, chips, credential state labels, staff danger actions | Remaining risk. |
| Recommended During Pilot | Older light-theme staff/youth surfaces use a different palette; most text appears readable, but status labels and muted text should be validated with tooling. | Programs, Profile, Notifications, Service Hours, staff dashboards | Remaining risk. |

No color redesign was performed.

## Screen Reader Readiness

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Important Before Pilot | Placeholder-only compact staff fields lack persistent accessible context when values are entered. | Staff compact forms | Remaining risk. |
| Important Before Pilot | External youth links did not consistently announce that they open a new tab. | Global Civic Movements, Service Hours, Home Opportunities | Remediated. |
| Recommended During Pilot | Decorative credential icons and timeline markers are hidden from assistive tech, which is appropriate, but surrounding text should continue carrying meaning. | Credential Explorer, My Journey | Acceptable. |
| Recommended During Pilot | Dynamic success/error messages are visible but not consistently configured as live regions. | Youth and staff async screens | Remaining risk for future accessibility pass. |

---

# 3. Mobile Findings

## Youth on Phones

| Breakpoint | Assessment |
|---|---|
| 320px | Minimum viewport is explicitly supported at the body level. Refreshed youth grids collapse to one column. Bottom nav remains available, but labels are tight and should be checked on real devices. |
| 375px | Primary youth flows should be usable. Home, Journey, Learning, and Credential Explorer have mobile-specific spacing. |
| 768px | Youth tablet/large-phone layouts have enough room for card grids and bottom navigation if under the 860px threshold. |

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Important Before Pilot | Physical phone validation has not been completed for iOS Safari and Android Chrome, including safe areas and virtual keyboards. | All youth routes | Remaining risk. |
| Important Before Pilot | Credential Explorer, Notifications, and Service Hours are not mobile bottom tabs. | Youth mobile navigation | Remaining discoverability risk; not remediated in 31E because it would change navigation scope. |
| Recommended During Pilot | Older youth pages may feel less visually integrated and should be checked for form spacing on small screens. | Programs, Profile, Notifications, Service Hours | Remaining risk. |

## Mobile Bottom Navigation

The youth bottom navigation appears structurally sound:

- Hidden until youth authenticated context.
- Five clear tabs: Home, Journey, Programs, Learning, Profile.
- Fixed to bottom with safe-area padding.
- Uses text labels and active state, not icon-only navigation.
- Has explicit focus styling after 31E remediation.

The main risk is discoverability of secondary tools, not basic accessibility of the bottom bar.

---

# 4. Tablet Findings

At tablet widths, the platform is generally usable:

- Youth routes either retain comfortable single-column layouts or collapse from dense grids.
- Staff `staff-management-grid` collapses to one column at 860px, which helps tablet portrait mode.
- Staff sidebar remains visible or becomes stacked depending on viewport width.

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Important Before Pilot | Staff tablet use should be validated with real forms and record lists, especially when the virtual keyboard is open. | Credential, Attendance, Service Hours, Relationships, Organization management | Remaining risk. |
| Recommended During Pilot | Reporting and pilot dashboards can become long on tablet and may require extensive vertical scrolling. | Reporting, Pilot Metrics, Pilot Evaluation, Pilot Readiness | Acceptable with staff training. |

---

# 5. Desktop Findings

Desktop/laptop is the strongest environment for staff operations.

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Recommended During Pilot | Staff routes are broad and dense, increasing cognitive load, but grouped navigation is consistent. | Staff navigation and dashboards | Existing operational training issue. |
| Recommended During Pilot | Reporting tables and grouped dashboard panels should receive a future table-caption and screen-reader review. | Reporting, Operations Reporting, Pilot dashboards | Remaining risk. |
| Post-Pilot | Charts, print layouts, saved views, and exports remain outside current frontend scope. | Reporting | Not required for controlled pilot. |

No desktop layout blocker was identified in the reviewed CSS.

---

# 6. Youth Safety Findings

| Classification | Finding | Affected Areas | Status |
|---|---|---|---|
| Critical Before Pilot | Staff-only routes must remain protected by backend authorization, not only hidden navigation. | All staff routes | Existing architecture appears designed for this; no 31E change. |
| Important Before Pilot | Youth navigation remains separated from staff navigation after access verification. | App shell and route groups | Acceptable. |
| Important Before Pilot | Youth profiles remain private by product design; no public directory, followers, social feed, or public portfolio was identified. | Youth Profile, Home, My Journey | Acceptable. |
| Recommended During Pilot | ASPN Participant ID is visible to youth but still needs orientation language explaining that it is private/internal. | Profile, Home | Existing known issue. |
| Recommended During Pilot | External links can take youth out of ASPN. Link language now states new-tab behavior, but youth orientation should still explain external resources. | Global Civic Movements, Service Hours, Opportunities | Partially remediated. |

No staff-only functionality was identified in youth navigation.

---

# 7. Remediation Applied

## Code changes

1. Added explicit keyboard focus styles for:
   - desktop navigation links
   - brand link
   - text actions
   - primary actions
   - staff record cards
   - unstyled buttons
   - staff mini-table buttons
   - staff search inputs
   - compact form inputs, selects, and textareas

2. Updated youth-facing external-link labels:
   - Global Civic Movements links now say “Open experience (opens in new tab)”.
   - Service Hours request form link now says “Open Request Form (opens in new tab)”.
   - Home opportunity links now say “Explore opportunity (opens in new tab)”.

## Files modified for remediation

- `frontend/src/styles.css`
- `frontend/src/components/RwdLearningCenterPage.jsx`
- `frontend/src/components/ServiceHoursPage.jsx`
- `frontend/src/components/YouthDashboard.jsx`

---

# 8. Remaining Risks

| Classification | Risk | Recommended Handling |
|---|---|---|
| Critical Before Pilot | No recorded physical-device/browser QA yet for iOS Safari, Android Chrome, Chrome, Safari, Firefox, and Edge. | Complete a device/browser matrix before real onboarding. |
| Critical Before Pilot | No recorded screen-reader smoke test yet. | Test VoiceOver on iOS/macOS and NVDA or Narrator on Windows for core youth and staff flows. |
| Important Before Pilot | Placeholder-only compact staff forms remain common. | Add persistent labels or accessible labels in a focused accessibility remediation checkpoint. |
| Important Before Pilot | Forced-colors/high-contrast validation has not been completed. | Validate and patch contrast/focus issues found in real mode testing. |
| Important Before Pilot | Dynamic success/error updates are not consistently live-region announcements. | Add live-region semantics to shared message components. |
| Important Before Pilot | Staff tablet workflows need real virtual-keyboard testing. | Validate attendance, credential award, service-hour review, and relationship-note editing on tablet. |
| Recommended During Pilot | Youth secondary tools are not in the mobile bottom nav. | Mitigate with orientation and Home/Journey links during pilot; revisit navigation after pilot data. |
| Recommended During Pilot | Older youth screens remain visually less aligned with refreshed ASPN dark surfaces. | Polish after pilot-blocking QA issues are resolved. |

---

# 9. Pilot Readiness Assessment

**Assessment:** Ready with Accessibility and Device QA Conditions.

The platform can proceed toward controlled pilot onboarding if the following are completed or operationally mitigated:

1. Run physical-device/browser QA for the listed breakpoints and browsers.
2. Complete keyboard-only walkthroughs of youth Home, Profile, Programs, Learning, Credentials, Service Hours, and the core staff record workflows.
3. Complete screen-reader smoke tests for account/profile recovery, youth navigation, staff record creation, and staff destructive-action confirmations.
4. Validate contrast and focus in high-contrast/forced-colors environments.
5. Train staff to use laptops/tablets for operational work; avoid routine staff workflow execution on phones during the pilot.

The next checkpoint should be **31F — Pilot Content and Operational Validation** after the 31E findings are accepted.
