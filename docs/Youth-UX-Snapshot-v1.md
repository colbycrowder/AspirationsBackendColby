# ASPN Platform Youth UX Snapshot

**Version:** Checkpoint 30G  
**Date:** June 22, 2026

---

# Platform Identity

The current youth experience presents ASPN as a private development platform that brings learning, program participation, credentials, service, and future preparation into one authenticated workspace. The Home dashboard is the center of the experience; the other youth sections provide the detail behind its summaries and next actions.

## Development Portfolio

My Journey functions as a developing record of a youth participant's accomplishments. It combines active programs, earned credentials, completed Global Civic Movements experiences, verified service-hour milestones, and attendance milestones into a timeline and pathway-oriented view. It is an internal developmental record, not a public portfolio.

## Credential Wallet

The Credential Explorer shows ASPN's six core and four advanced credentials. Earned credentials are highlighted using authenticated dashboard data. Registry metadata explains how each credential connects to pathways, programs, and example civic role families. Approved credential artwork gives the system a consistent visual identity.

## Participation Record

The platform records program enrollment, learning completion, attendance, service hours, and credential attainment. Home and My Journey summarize these records for youth. Operational details remain controlled by ASPN staff and backend authorization.

## Opportunity Preparation System

ASPN connects current participation to future civic pathways and role families without presenting matching scores, job recommendations, or guaranteed outcomes. Current opportunity presentation is lightweight: Home can show links supplied by the dashboard response, while the credential and pathway views provide exploratory context.

## What ASPN Is Not

- **Not social media:** There are no follower counts, popularity metrics, public youth discovery, or public activity feeds.
- **Not a discussion forum:** Youth do not post public discussions or comments. Legacy discussion routes are retired.
- **Not a job board:** The platform does not list employers, recommend jobs, accept applications, or perform workforce matching.
- **Not a learning management system:** Global Civic Movements links to guided external experiences and tracks lightweight progress; it does not currently provide a full course-authoring, grading, assignment, or classroom-management environment.

---

# Youth Navigation Structure

Authenticated youth users receive a role-specific navigation experience. On desktop, the sidebar separates primary Journey navigation from additional tools. On screens at or below 860 pixels, the long youth navigation is hidden and a fixed five-tab bottom bar appears.

## Home

**Route:** `/dashboard`  
Home is the central youth workspace. It summarizes profile identity, credentials, programs, approved service hours, Global Civic Movements progress, next actions, upcoming activity placeholders, and opportunities.

## My Journey

**Route:** `/journey`  
My Journey turns operational records into a developmental story. It summarizes accomplishments, builds a milestone timeline, explains earned credential connections, and introduces the six ASPN pathways without artificial progress percentages.

## Programs

**Route:** `/programs`  
Programs lists active ASPN programs, identifies enrollment state, and allows an authenticated youth user to enroll using the verified Firebase identity.

## Global Civic Movements

**Route:** `/rwd-learning-center`  
The Learning Center presents externally hosted civic learning experiences, progress states, completion controls, and linked credential information. Youth-facing language uses Global Civic Movements or Learning rather than the backend acronym.

## Credential Explorer

**Route:** `/credentials`  
Credential Explorer describes the full ten-credential ASPN system and highlights earned credentials. It is a secondary desktop navigation item and is linked from Home and My Journey.

## Profile

**Route:** `/profile`  
Profile supports protected profile completion and updates. Youth can provide basic education and interest information while viewing their profile status and private ASPN Participant ID.

## Mobile Navigation Note

The current bottom tabs are **Home**, **Journey**, **Programs**, **Learning**, and **Profile**. Credential Explorer does not currently occupy a mobile bottom tab; youth reach it through Home or My Journey. Signed-out users do not see youth navigation.

---

# Home Dashboard

## Current Cards and Summary Metrics

The Home experience includes:

- A welcome card with first-name greeting, profile image or first-initial fallback, school, graduation year, ASPN Participant ID, and a profile action.
- **Credentials Earned:** count of earned credential records.
- **Programs Active:** count of programs returned in the authenticated dashboard response.
- **Service Hours:** total hours from records with verified status.
- **Global Civic Movements:** number of completed learning experiences and remaining activity guidance.
- **Continue Your Journey:** up to five contextual next actions.
- **Upcoming Activities:** an honest placeholder when no activities are assigned.
- **Opportunities:** up to three dashboard-provided opportunities or an empty state.

Profile completion is calculated from first name, last name, email, school, and graduation year. It influences whether Home shows a Complete Profile or View Profile action. The percentage itself is not currently shown as a progress bar on Home.

## Current Progress and Activity Behavior

Next actions are assembled from existing dashboard data. They may prompt the youth to complete a profile, join a program, continue Global Civic Movements, view credentials, submit or review service hours, or read notifications. Program, credential, service, and learning cards link directly to their supporting pages.

Home records a dashboard-view platform event after a successful load. Upcoming activities are not yet populated by a youth scheduling system.

## Notification Behavior

Home reads the unread notification count from the dashboard response. When unread notifications exist, a Read Notifications action is added to the next-action list. Home does not display notification bodies. The dedicated Notifications page remains available in the desktop secondary navigation and supports the existing notification workflow.

## New Youth User Experience

A new authenticated youth may initially see missing school, missing graduation year, pending ASPN ID, no programs, no credentials, no approved service hours, and no completed learning experiences. Home responds with empty states and actions directing the youth toward profile completion, program enrollment, learning, credentials, and service hours. A matching protected profile record is required for dashboard data to load.

## Returning Youth User Experience

A returning youth sees their saved identity information, active programs, accumulated credentials, verified service hours, learning completion, opportunities, and relevant next actions. Completed work reduces introductory prompts and contributes to My Journey.

---

# My Journey

## Journey Summary

My Journey summarizes credentials earned, active programs, verified service hours, and completed Global Civic Movements experiences. Each summary card links to the detailed supporting section.

## Journey Timeline

The timeline is generated from existing dashboard data. It may include:

- Program enrollment entries.
- Credential awards, with award dates when available.
- Completed Global Civic Movements experiences.
- Verified service milestones at 10, 25, 50, and 100 hours.
- Attendance milestones at 5, 10, and 20 present sessions.

Items with usable dates are ordered newest first. Program entries currently display "Date not available" because the dashboard program summary does not supply an enrollment date.

## Credential Meaning and Milestones

Earned credentials are matched to the ecosystem registry by credential ID or normalized credential name. Matched credentials show approved artwork, related pathways, related programs, advanced credentials to explore, and example civic role families. Unmatched earned credentials remain visible with an initials fallback and an honest message that a registry connection is unavailable.

## Participation and Growth Visualization

Participation is expressed through summary counts, milestone cards, timeline entries, and pathway states. A pathway is marked Started only when an earned credential maps to it. The interface does not calculate percentages, matching scores, or unsupported completion claims.

All six official pathways are displayed: Civic Research, Data & Evaluation, Public Communication, Community Engagement, Project Implementation, and Civic Administration.

---

# Programs

## Program Discovery

The Programs page loads the active program catalog, sorts programs by name, and displays program name, description, category, leader, image when available, and status. When no active programs exist, the page uses a direct empty-state message.

## Enrollment

Enrollment requires a signed-in Firebase user. The frontend sends the user's ID token, while ownership is determined by the verified backend identity rather than a client-supplied UID. After successful enrollment, the page refreshes dashboard program data and changes the program card to Enrolled.

## Participation Display and Status Handling

Active programs are available for enrollment. Enrolled programs are disabled from duplicate enrollment and appear on Home and My Journey after dashboard refresh. Archived or inactive programs are unavailable for enrollment and normally do not appear in the active youth catalog. Loading, backend error, missing profile connection, success, duplicate/error, and empty states are supported.

---

# Global Civic Movements

## Activity Discovery

The Learning Center loads activities from the authenticated dashboard response. A hero explains the civic-learning purpose, summary cards report completed, available, in-progress, and credential-connected experiences, and Continue Learning selects the first in-progress or not-started item.

## Progress and Completion Tracking

Each learning card can display country, title, description, completion status, external URL, associated credential ID, quiz score, pass state, and completion date when those fields exist. Youth may mark an activity In Progress or Completed using the existing protected progress endpoint. The page reloads dashboard data after a successful update.

Status values shown to youth are Not Started, In Progress, and Completed. Completed cards receive a distinct visual state. Opening an external learning experience records the existing learning-view platform event.

## Credential Relationships

The page reports whether an activity has an associated credential ID and counts credential-connected experiences. It does not currently translate that ID into the full registry artwork or credential description on the learning card.

---

# Credential Explorer

## Credential Catalog

Credential Explorer is driven by `aspnEcosystemRegistry.js`. It displays six core credentials and four advanced credentials with descriptions, tier labels, related pathways, related programs, and example civic role families.

## Earned and Available State

The page matches authenticated earned credential records to the registry. Matched cards display Earned and an award date when available; other registry credentials display Explore. This is an exploratory catalog state, not a calculation of completion percentage or guaranteed eligibility.

Earned records that do not match the registry appear in Additional Earned Credentials. They are not discarded or incorrectly mapped.

## Credential Icon Display

All ten registry credentials reference approved artwork stored in `frontend/src/assets/credentials/`. A shared credential icon component renders the circular mark within compact rounded frames on Credential Explorer and My Journey. Artwork preserves its proportions and scales for mobile layouts. Missing or failed artwork falls back to a two-letter initials badge.

## Pathway Presentation

Credential cards identify one or more related pathways and example civic role families. These are explanatory ecosystem relationships. They are not recommendations, employment matches, or user-specific pathway scores.

---

# Profile

## Identity Information

Youth can view and update first name, last name, and email through the protected profile flow. The backend uses the verified Firebase UID; the form does not expose UID, role, public-profile, verification, or permission controls.

## Education Information

The form supports school and graduation year. These fields feed Home and remove the corresponding missing-information messages.

## Interest Information

The current UI supports comma-separated College Interests, Career Interests, Civic Interests, Community Interests, and Public Service Interests. The frontend converts these values to lists before submission.

## Participation Information

Profile displays profile status and the private ASPN Participant ID. Programs, credentials, attendance, learning, and service records are not edited from Profile; they appear in their operational sections and dashboard summaries.

## Privacy Assumptions

Youth profiles are private by default. The experience provides no public youth directory, search, username, follower system, or public portfolio. ASPN Participant IDs are internal research and analytics identifiers, not public handles. Profile privacy still depends on backend authorization, Firebase configuration, and operational controls being maintained.

---

# Mobile Experience

## Bottom Navigation

At 860 pixels and below, authenticated youth receive a fixed bottom tab bar with Home, Journey, Programs, Learning, and Profile. The desktop youth navigation is hidden, page content receives safe-area-aware bottom padding, and signed-out or staff users do not receive the youth bottom bar.

## Responsive Layouts

The refreshed Home, My Journey, Global Civic Movements, and Credential Explorer layouts collapse multi-column grids progressively at 1040 and 560 pixels. Cards stack into a single column on small phones. The application maintains a 320-pixel minimum viewport width.

## Small-Screen Usability

Primary interactive controls generally use a minimum height near 44 pixels. Mobile content spacing is reduced without removing section boundaries. The bottom bar uses text labels, an active background, and a top indicator so state is not communicated by color alone.

Credential artwork is shown in fixed-size, overflow-hidden frames that crop the source artboard around the approved circular mark without distorting its aspect ratio. My Journey uses a slightly larger icon treatment than Credential Explorer.

---

# Design System Snapshot

## Color Palette

The refreshed youth surfaces use:

| Token | Value | Use |
|---|---|---|
| `--aspn-bg` | `#03045e` | Primary youth workspace background |
| `--aspn-surface` | `#0e0e64` | Section and navigation surfaces |
| `--aspn-accent` | `#b2dfff` | Primary actions, active states, and emphasis |
| `--aspn-accent-hover` | `#81c7f8` | Hover and interactive emphasis |
| `--aspn-text` | `#f9fbff` | Primary text on dark surfaces |
| `--aspn-text-soft` | `rgba(203, 207, 226, 0.86)` | Supporting text |
| `--aspn-border` | `rgba(177, 223, 255, 0.22)` | Subtle boundaries |

Some older youth surfaces, including Programs and Profile, continue to use the earlier shared light workspace styles.

## Typography

The application uses Inter when available, followed by system UI fonts. Headings use strong weight and restrained sizing. Labels and kickers use compact uppercase text for hierarchy. Letter spacing remains neutral.

## Cards and Sections

Youth cards generally use 7- or 8-pixel corner radii, subtle translucent surfaces, and one-pixel borders. Page sections remain un-nested and use responsive grids for repeated cards.

## Buttons

Primary youth actions use light blue fill with dark navy text. Secondary actions use transparent surfaces and bordered treatments. Buttons are text-based commands with clear labels and mobile-friendly heights.

## Credential Icon Standards

- Registry metadata is the source of truth for matched credential artwork.
- Approved PNG artboards remain frontend assets; no Firestore icon field is required.
- Compact frames crop to the central credential mark without stretching the image.
- My Journey and Credential Explorer use the same shared renderer.
- Unmatched credentials retain initials fallback behavior.

## Spacing

Desktop refreshed pages typically use 20- to 24-pixel outer and section spacing, 12- to 18-pixel card gaps, and 16- to 22-pixel section padding. Small-screen layouts reduce outer padding to approximately 12 to 16 pixels.

---

# Youth User Journey

```text
Create Account
      ↓
Complete Profile
      ↓
Enroll in Program
      ↓
Participate
      ↓
Earn Credentials
      ↓
Build Journey
      ↓
Access Future Opportunities
```

1. **Create Account:** Firebase Authentication establishes the signed-in identity.
2. **Complete Profile:** The protected profile flow creates or updates the private youth record and assigns an ASPN Participant ID when appropriate.
3. **Enroll in Program:** Youth explore active programs and enroll using their verified identity.
4. **Participate:** Program attendance, service, and Global Civic Movements activity contribute to the participation record.
5. **Earn Credentials:** Staff awards and supported automated workflows add earned credentials.
6. **Build Journey:** Home and My Journey assemble records into summaries, milestones, and pathway context.
7. **Access Future Opportunities:** Current opportunity links and pathway context prepare for later opportunity systems without claiming personalized matching.

---

# Current Strengths

## UX Strengths

- Home provides one authenticated starting point for the major youth workflows.
- Empty, loading, success, and failure states are present across the principal pages.
- My Journey converts records into understandable accomplishments without inventing progress.
- Responsive card layouts and mobile bottom navigation reduce navigation friction on phones.
- Registry-driven credential metadata keeps credential meaning and artwork consistent.

## Youth-Engagement Strengths

- Next actions translate platform data into practical participation prompts.
- Credentials, milestones, and pathway context make participation feel cumulative.
- Global Civic Movements presents civic learning as exploration rather than administration.
- Honest empty states give new users direction without fabricating activity.

## Platform-Identity Strengths

- The product is clearly oriented toward youth development, civic learning, credentials, service, and preparation.
- Private-by-default design avoids public popularity and discovery mechanics.
- The distinction between completed work, exploratory pathways, and future opportunities is generally maintained.
- Approved credential artwork gives the ten-credential system a recognizable visual language.

---

# Current UX Gaps

This section documents current gaps only; it does not prescribe a redesign.

## Potentially Confusing Areas

- Credential Explorer is present in desktop secondary navigation but not in the mobile bottom bar, so mobile discoverability depends on links from Home and My Journey.
- The refreshed dark youth surfaces coexist with older light Programs and Profile screens, creating a visible transition between sections.
- The application shell top bar and some page-level heroes repeat similar page titles.
- Learning cards expose an associated credential ID rather than the credential's youth-facing name and artwork.
- Program timeline entries lack enrollment dates and therefore display an unavailable date.
- Available credential cards use Explore; the interface does not currently explain exact earning requirements from the registry catalog view.

## Missing Guidance

- Account creation redirects into the authenticated experience, but there is no multi-step onboarding checklist or guided first-session tour.
- Home calculates profile completeness internally but does not show a visible completion percentage.
- Upcoming Activities is a placeholder rather than a populated schedule.
- Opportunity preparation is contextual; no dedicated youth Opportunities page or matching workflow exists.
- Youth do not currently receive an in-product explanation of how ASPN Participant IDs are used.

## Potential Onboarding Issues

- A Firebase Authentication account and Firestore profile are distinct; profile/API failures can interrupt the first dashboard experience.
- New users may encounter several empty sections before staff seed programs, learning activities, credentials, and opportunities.
- Email is editable in the profile form, which may be conceptually confusing when Firebase Authentication remains the login identity.
- Interest entry expects comma-separated text rather than structured selection controls.

## Potential Accessibility Issues

- A formal keyboard, screen-reader, zoom, reduced-motion, and color-contrast audit has not been recorded for the refreshed youth experience.
- Dynamic success and error messages are visible but are not consistently declared as live regions.
- Credential source artboards contain text, but the compact UI intentionally treats their cropped marks as decorative because adjacent card headings provide the accessible credential name.
- Some status distinctions use color-supported badges; textual status labels are present, but cross-browser high-contrast behavior still needs validation.
- The fixed mobile navigation and safe-area spacing should receive device testing on iOS Safari and Android browsers.
- External learning links open new tabs; this behavior is not explicitly announced in link text.

---

# Snapshot Boundary

This document records the youth frontend as implemented through Checkpoint 30F. It does not establish new requirements, change routes, modify data behavior, or approve public-profile, matching, messaging, or social features.
