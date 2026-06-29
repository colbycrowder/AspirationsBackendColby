# ASPN Platform Youth Navigation and Visual Consistency Review v1

**Checkpoint:** 31H — Youth Navigation and Visual Consistency Review  
**Date:** 2026-06-23  
**Scope:** Youth-facing navigation, terminology, visual consistency, empty states, credential/pathway consistency, and mobile coherence across the current React/Vite frontend.

---

## 1. Executive Summary

**Overall consistency rating:** Good  
**Pilot recommendation:** Ready with Minor Fixes

The youth experience now reads as a coherent ASPN youth development operating system rather than a set of disconnected tools. Home, My Journey, Global Civic Movements, and Credential Explorer carry the strongest visual and conceptual identity. They consistently frame the platform around participation, learning, credentials, service, and future-building.

The platform is pilot-usable for a controlled onboarding group, with three remaining consistency risks:

1. **Secondary youth destinations are less discoverable on mobile.** Credential Explorer, Notifications, and Service Hours are available through desktop navigation and contextual Home/Journey links, but they are not bottom navigation tabs.
2. **Some youth screens still use older light styling.** Programs, Profile, Notifications, Service Hours, Login, Create Account, and Profile Completion are functional but less visually aligned with the refreshed dark ASPN system.
3. **A few data-backed labels still expose implementation details.** Global Civic Movements can show connected credential IDs, Notifications can show related credential IDs, and Service Hours can show program IDs or verification source values.

No critical youth navigation or visual consistency blocker was found.

---

## 2. Terminology Audit

### Approved youth labels

| Destination | Current youth-facing status | Assessment |
| --- | --- | --- |
| Home | Used in navigation, loading/error states, onboarding recovery, and progress actions. | Consistent |
| My Journey | Used in navigation, page hero, loading/error states, and journey sections. | Consistent |
| Programs | Used in navigation and program browsing. | Consistent |
| Global Civic Movements | Used as the main youth learning page title and Home/Journey progress label. | Consistent with one mobile shorthand |
| Credential Explorer | Used in navigation, page title, loading/error states, and Home/Journey links. | Consistent |
| Profile | Used in navigation and onboarding recovery. | Consistent |
| Notifications | Used in navigation and inbox page. | Consistent |
| Service Hours | Used in navigation and Home/Journey links. | Mostly consistent |

### Removed or legacy youth terminology

| Term | Finding | Classification |
| --- | --- | --- |
| Youth Dashboard | No longer appears as a primary youth-facing destination. A few internal component/API names still use dashboard language, but they are not youth navigation labels. | Post-Pilot |
| Dashboard | Small youth-facing references were found in Programs copy and corrected to Home during this checkpoint. Internal route `/dashboard` and analytics event `DASHBOARD_VIEW` remain implementation details. | Remediated |
| RWD | Not used as the visible youth learning label. The youth page says Global Civic Movements. Internal component/API/event names still use RWD and staff routes still use RWD Management, which is outside this youth-only checkpoint. | Recommended During Pilot |
| Learning Center | Still appears as a supporting youth label and mobile shorthand. This is acceptable as secondary language because the page title remains Global Civic Movements. | Recommended During Pilot |
| Old navigation labels | No visible old youth navigation labels were found in the primary or secondary youth nav. | Ready |

### Terminology fixes applied in 31H

- Programs page copy changed from “youth dashboard” to “Home.”
- Program enrollment success copy changed from “part of your dashboard” to “part of Home.”
- Notifications recovery note changed from a Firestore-profile explanation to a youth-facing profile completion instruction.
- Service Hours recovery note changed from a Firestore-profile explanation to a youth-facing profile completion instruction.

---

## 3. Navigation Audit

### Desktop youth navigation

Desktop youth navigation is divided into:

- **Journey:** Home, My Journey, Programs, Global Civic Movements, Profile
- **More:** Credential Explorer, Service Hours, Notifications

This structure is understandable for pilot use. Home is the center of action, My Journey is the reflective record, Programs and Global Civic Movements are core participation areas, and Profile remains the onboarding/account support destination.

**Finding:** Credential Explorer is central to the ASPN model but lives in the secondary More group. This is acceptable for pilot because Home and My Journey both link to it contextually, but youth may need a quick orientation.

**Classification:** Important Before Pilot

### Mobile youth navigation

Mobile bottom navigation includes:

- Home
- Journey
- Programs
- Learning
- Profile

The mobile bar covers the core daily youth loop. The “Learning” shorthand is space-efficient and acceptable, but the full page title should continue to say Global Civic Movements so youth learn the official term.

**Finding:** Credential Explorer, Notifications, and Service Hours are not in the mobile bottom navigation.

**Risk:** Youth can still reach these destinations through Home/Journey contextual actions, but discovery depends on content state. For example, Notifications is most visible when unread notices exist.

**Classification:** Important Before Pilot

### Contextual navigation

Home and My Journey successfully provide contextual routes to:

- Credential Explorer
- Programs
- Service Hours
- Global Civic Movements
- Notifications when unread
- Profile when incomplete

No major isolated youth destination was found. The youth Attendance route exists as a protected placeholder but is not exposed in youth primary, secondary, or mobile navigation. This is acceptable because Attendance is not a current youth destination.

---

## 4. Visual Consistency Audit

| Screen | Rating | Notes | Classification |
| --- | --- | --- | --- |
| Home | Excellent | Strong dark ASPN styling, clear hierarchy, honest progress cards, contextual next actions. | Ready |
| My Journey | Excellent | Cohesive journey framing, strong credential/pathway context, honest empty states. | Ready |
| Programs | Needs Improvement | Functional and understandable, but uses older page/card styling and lighter visual system. | Recommended During Pilot |
| Global Civic Movements | Good | Strong refreshed styling and clear learning flow. Minor terminology bridge remains with Learning Center/RWD internals. | Ready |
| Credential Explorer | Excellent | Registry-driven, visual credential identity is strong, official names/pathways are clear. | Ready |
| Profile | Needs Improvement | Functional form and privacy language are clear, but visual style differs from refreshed youth pages. | Recommended During Pilot |
| Notifications | Needs Improvement | Functional inbox; older styling and ID-backed related fields can feel operational. | Recommended During Pilot |
| Service Hours | Needs Improvement | Totals and statuses are clear; older styling and operational fields remain visible. | Recommended During Pilot |
| Login | Good | Functional, simple, and consistent enough for pilot. Dark-system alignment can improve later. | Recommended During Pilot |
| Create Account | Good | Now sends youth to Profile before Home. Copy supports onboarding sequence. | Ready |
| Profile Completion | Good | Correct recovery path and privacy framing; visual alignment can improve later. | Recommended During Pilot |

### Visual pattern findings

- **Headers:** Refreshed pages use strong hero sections and action-oriented language. Older pages use simpler `page-intro` sections.
- **Cards:** Home, My Journey, Global Civic Movements, and Credential Explorer use the strongest card language. Programs and secondary pages are readable but less polished.
- **Buttons:** Actions are generally clear and consistent. Home/Journey refreshed actions feel more youth-centered than older text actions.
- **Forms:** Profile and auth forms are straightforward, labeled, and functional, but not yet visually integrated with the refreshed youth system.
- **Status badges:** Credential and learning badges are strong. Service/notification status badges are readable but visually less aligned.
- **Spacing and typography:** Refreshed youth pages are more spacious and consistent. Older pages are acceptable but visually quieter and more administrative.

---

## 5. Empty State Review

| Empty state | Current behavior | Assessment |
| --- | --- | --- |
| Zero credentials | Home and Credential Explorer honestly state that credentials begin through participation and learning. | Good |
| Zero programs | Home prompts youth to join a program; Programs states no active programs when none exist. | Good |
| Zero service hours | Home says no approved hours yet; Service Hours says no records have been submitted yet and shows unavailable request link if unconfigured. | Good |
| Zero learning completions | Home and Global Civic Movements distinguish no activities from incomplete activities. | Good |
| Zero notifications | Notifications says no notifications yet. | Good |
| Empty My Journey | My Journey invites youth to join a program, complete learning, or earn a credential. | Excellent |

No fake progress, fake credentials, or misleading accomplishment states were found. The zero-state language is honest and pilot-safe.

---

## 6. Credential Ecosystem Consistency

### Strengths

- Credential Explorer is registry-driven and uses the official core and advanced credential catalog.
- Official credential icons are present and used in Credential Explorer and My Journey.
- My Journey connects earned credentials to official pathways and related programs.
- Home, My Journey, Learning, and Credential Explorer all reinforce credentials as developmental evidence rather than a social badge system.

### Remaining consistency risks

| Finding | Screen | Classification |
| --- | --- | --- |
| Global Civic Movements can display `associatedCredentialId` instead of a youth-facing credential name/icon. | Global Civic Movements | Recommended During Pilot |
| Notifications can display related credential IDs rather than youth-facing credential names. | Notifications | Recommended During Pilot |
| Credential Explorer explains general earning context but does not expose exact credential requirements/progress. | Credential Explorer | Recommended During Pilot |
| Unmatched earned credentials are handled honestly, but they may make registry gaps visible to youth. | Credential Explorer | Post-Pilot |

These are not pilot blockers. They are content/data-presentation improvements for deeper pilot maturity.

---

## 7. Pathway Consistency

The youth-facing registry uses only the six official ASPN pathways:

1. Civic Research
2. Data & Evaluation
3. Public Communication
4. Community Engagement
5. Project Implementation
6. Civic Administration

No alternate youth pathway names were found in the registry-driven Credential Explorer or My Journey pathway sections.

**Assessment:** Ready

---

## 8. Mobile Experience Consistency

### Strengths

- The bottom navigation gives youth a stable core loop: Home, Journey, Programs, Learning, Profile.
- Home, My Journey, Global Civic Movements, and Credential Explorer use mobile-friendly stacked card layouts.
- Home provides contextual access to secondary tools through next actions and progress cards.
- My Journey gives contextual links to Credentials, Programs, Service Hours, and Learning.

### Risks

| Risk | Impact | Classification |
| --- | --- | --- |
| Credential Explorer is not a bottom navigation item. | Youth may not immediately understand where credentials live unless Home/Journey links are explained. | Important Before Pilot |
| Notifications is not a bottom navigation item. | Youth may only discover it when unread notifications appear. | Recommended During Pilot |
| Service Hours is not a bottom navigation item. | Youth may rely on Home/Journey actions to find the request/status area. | Important Before Pilot |
| Older light-style screens may feel less connected on phones. | Visual inconsistency, not functional failure. | Recommended During Pilot |
| Profile form can become long on small screens. | Acceptable with scrolling, but should be included in manual device QA. | Important Before Pilot |

Mobile is acceptable for controlled pilot onboarding if youth receive a short orientation that explains Home as the launch point and shows where credentials, notifications, and service hours appear.

---

## 9. Recommended Fixes

### Critical Before Pilot

None found.

### Important Before Pilot

| Issue | Screen(s) | Recommended action | Effort |
| --- | --- | --- | --- |
| Mobile secondary destination discovery depends on contextual links. | Credential Explorer, Service Hours, Notifications | During orientation, explicitly show youth how Home and My Journey link to credentials, service hours, and notifications. | Low |
| Profile form is central to onboarding and should be checked on real phones. | Create Account, Profile Completion, Profile | Complete a manual phone test at 320px and 375px before first onboarding. | Low |
| Service-hour request link may be unconfigured. | Service Hours, Home | Confirm the pilot service-hour request URL or approve the honest unavailable state before youth onboarding. | Low |

### Recommended During Pilot

| Issue | Screen(s) | Recommended action | Effort |
| --- | --- | --- | --- |
| Older light styling creates visual drift from refreshed ASPN youth pages. | Programs, Profile, Notifications, Service Hours, Auth | Align these screens with the dark ASPN youth system in a future visual pass. | Medium |
| Credential IDs appear instead of youth-facing credential names. | Global Civic Movements, Notifications | Map IDs to registry names/icons where available. | Medium |
| Service Hours can expose operational values such as program IDs and verification source. | Service Hours | Translate IDs/source values into youth-friendly labels where data supports it. | Medium |
| “Learning” mobile label is shorter than the official Global Civic Movements page name. | Mobile nav | Keep as shorthand for pilot; reinforce the full name through orientation and page title. | Low |

### Post-Pilot

| Issue | Screen(s) | Recommended action | Effort |
| --- | --- | --- | --- |
| Youth Attendance placeholder exists as a protected route but is not visible in nav. | Attendance | Decide whether youth attendance should become a real destination or remain staff-only. | Medium |
| Exact credential progress/requirements are not visible. | Credential Explorer, My Journey | Add requirement-level progress only after pilot credential rules are stable. | High |
| Full design-system consolidation remains unfinished. | All older youth screens | Complete a comprehensive visual system pass after pilot learnings. | High |

---

## 10. Final UX Verdict

If 60 youth onboarded today, the platform would mostly feel like a coherent youth development operating system, not a collection of disconnected tools. Home provides the next-step center. My Journey explains the developmental record. Programs, Global Civic Movements, Credentials, Service Hours, and Notifications orbit around the youth profile in a way that matches the platform philosophy.

The main pilot risk is not conceptual failure. It is discoverability and polish: younger youth may need a guided walkthrough to understand that Credentials, Notifications, and Service Hours are reached from Home/Journey rather than the mobile bottom bar, and older-styled secondary screens may feel less finished than the refreshed core.

**Final pilot UX verdict:** Ready with Minor Fixes

The platform is appropriate for controlled youth onboarding after completing the small copy remediations in this checkpoint and running manual mobile/device validation before launch.
