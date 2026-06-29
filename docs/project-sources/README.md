# ASPN Platform Project Sources

This directory contains permanent project sources for the ASPN Platform project.
Future development should consult these sources before making major architecture,
scope, or safety decisions.

## Source Hierarchy

1. [ASPN Platform AGENTS.md](./ASPN%20Platform%20AGENTS.md%206.9.26.pdf)
   - Platform philosophy
   - Safety principles
   - Design constraints

2. [ASPN Platform Build Manual](./ASPN%20Platform%20Build%20Manual%206.9.26.pdf)
   - Complete development history
   - Primary institutional knowledge source for platform evolution, checkpoint
     history, architecture decisions, safety decisions, and September onboarding
     preparation

3. [ASPN Platform Developer Handoff Guide](./ASPN%20Platform%20Developer%20Handoff%20Guide.pdf)
   - Technical onboarding guide
   - Current architecture, backend capabilities, Firestore collections,
     authentication approach, completed checkpoints, MVP scope, priorities, and
     roadmap

4. [ASPN Platform Release Notes v0.1 (6.9.26)](./ASPN%20Platform%20Release%20Notes%20v0.1%206.9.26.pdf)
   - Release documentation
   - Official historical record for Version 0.1, including completed work,
     known limitations, readiness status, and next development phase

5. [ASPN September MVP Functional Specification v1.0](./ASPN%20September%20MVP%20Functional%20Specification%20v1.0.pdf)
   - Authoritative specification for the September 2026 MVP development phase
   - Defines required youth, staff, credential, program, attendance,
     service-hour, RWD, dashboard, staging, and deferred-feature scope

6. [ASPN Platform Firebase Staging Checklist v0.1](./ASPN%20Platform%20Firebase%20Staging%20Checklist%20v0.1.md)
   - Firebase staging validation checklist
   - Required validation steps before production deployment

7. [ASPN Platform Firestore Migration Notes v0.1](./ASPN%20Platform%20Firestore%20Migration%20Notes%20v0.1.md)
   - Firestore schema change record
   - Guidance for handling older user records and v0.1 collections

8. [ASPN Platform Release Notes v0.2](./ASPN%20Platform%20Release%20Notes%20v0.2%206.10.26.pdf)
   - Release documentation for the v0.2 backend foundation and readiness state
   - Use as the current release record unless superseded by a later version

9. [ASPN Platform Build Manual Addendum v0.2](./ASPN%20Platform%20Build%20Manual%20Addendum%20v0.2%206.10.2026.docx)
   - Addendum to the build history after the v0.1 foundation
   - Use with the full Build Manual for checkpoint history and implementation context

10. [ASPN Platform Firestore Migration Notes v0.2](./ASPN%20Platform%20Firestore%20Migration%20Notes%20v0.2%206.10.26.pdf)
    - Updated Firestore schema and migration notes for v0.2
    - Use before staging data setup or schema-affecting changes

11. [ASPN Platform Firebase Staging Checklist v0.2](./ASPN%20Platform%20Firebase%20Staging%20Checklist%20v0.2%206.10.26.docx)
    - Updated Firebase staging validation checklist
    - Use as the current staging checklist unless superseded by a later version

12. [ASPN Platform Developer Handoff Guide Addendum v0.2](./ASPN%20Platform%20Developer%20Handoff%20Guide%20Addendum%20v0.2%206.10.26.docx)
    - Technical handoff addendum for v0.2
    - Use with the primary Developer Handoff Guide before continuing development

13. [ASPN Platform Simplified Addendum v0.2](./ASPN%20Platform%20Simplified%20Addendum%20v0.2%206.10.26.docx)
    - Simplified project addendum for non-technical review
    - Use for high-level alignment before frontend MVP work

14. [ASPN Platform Release Notes v0.4](./ASPN%20Platform%20Release%20Notes%20v0.4.md)
    - Current release record after youth MVP, staff/admin MVP, Firebase staging validation, and Legacy Endpoint Security Fix
    - Use as the current release baseline for pilot preparation

15. [ASPN Platform Build Manual Addendum v0.4](./ASPN%20Platform%20Build%20Manual%20Addendum%20v0.4.md)
    - Build history addendum covering Checkpoints 21A-21L
    - Use with the full Build Manual before proposing future checkpoint work

16. [ASPN Platform Developer Handoff Addendum v0.4](./ASPN%20Platform%20Developer%20Handoff%20Addendum%20v0.4.md)
    - Current developer handoff after Staff/Admin MVP and security remediation
    - Use as the current technical onboarding supplement

17. [ASPN Platform Firebase Staging Validation Report v0.4](./ASPN%20Platform%20Firebase%20Staging%20Validation%20Report%20v0.4.md)
    - Firebase staging validation record for v0.4
    - Confirms active Firestore database decision and validation results

18. [ASPN Platform Pilot Readiness Review v2](./ASPN%20Platform%20Pilot%20Readiness%20Review%20v2.md)
    - Historical pilot readiness review before the 21L security fix
    - Superseded by Pilot Readiness Review v3 for current readiness

19. [ASPN Platform Security Review Addendum v0.4](./ASPN%20Platform%20Security%20Review%20Addendum%20v0.4.md)
    - Security review and Legacy Endpoint Security Fix documentation
    - Use before reintroducing any public profile, discussion, or social feature

20. [ASPN Platform Update Simplified Version June 2026 v0.4](./ASPN%20Platform%20Update%20Simplified%20Version%20June%202026%20v0.4.md)
    - Simplified non-technical update for stakeholders
    - Use for high-level pilot-readiness alignment

21. [ASPN Platform Mobile Browser QA Report v0.4](./ASPN%20Platform%20Mobile%20Browser%20QA%20Report%20v0.4.md)
    - Repository-based mobile/browser QA audit
    - Use before mobile polish or live device testing

22. [ASPN Platform Pilot Readiness Review v3](./ASPN%20Platform%20Pilot%20Readiness%20Review%20v3.md)
    - Current pilot readiness assessment after 21L and staging validation
    - Use as the current go/no-go reference for controlled pilot planning

23. [ASPN Platform Build Manual Addendum v0.4 Expanded](./ASPN%20Platform%20Build%20Manual%20Addendum%20v0.4%20Expanded.md)
    - Expanded institutional build record for v0.4
    - Use for leadership, partner, grant, and future developer context

24. [ASPN Platform Developer Handoff Addendum v0.4 Expanded](./ASPN%20Platform%20Developer%20Handoff%20Addendum%20v0.4%20Expanded.md)
    - Expanded technical onboarding document for developers with no prior ASPN context
    - Use as the most detailed developer handoff for the post-21L platform

25. [ASPN Platform Firebase Staging Validation Report v0.4 Expanded](./ASPN%20Platform%20Firebase%20Staging%20Validation%20Report%20v0.4%20Expanded.md)
    - Expanded Firebase staging validation report
    - Use as the detailed staging validation record for v0.4

26. [ASPN Platform Release Notes v0.5](./ASPN%20Platform%20Release%20Notes%20v0.5.md)
    - Current release record after Phase 25 stakeholder foundations and Phase 26 pilot operations dashboards
    - Documents completion through Checkpoint 26C and transition into pilot operations readiness

27. [ASPN Platform Build Manual Addendum v0.5](./ASPN%20Platform%20Build%20Manual%20Addendum%20v0.5.md)
    - Build history addendum covering Checkpoints 25A-26C
    - Use as the concise institutional record for educator, partner, government, stakeholder, readiness, metrics, and evaluation work

28. [ASPN Platform Build Manual Addendum v0.5 Expanded](./ASPN%20Platform%20Build%20Manual%20Addendum%20v0.5%20Expanded.md)
    - Expanded build record for v0.5
    - Includes endpoint inventories, collection inventories, workflow descriptions, and operational readiness details

29. [ASPN Platform Build Manual Addendum v0.5 Simplified](./ASPN%20Platform%20Build%20Manual%20Addendum%20v0.5%20Simplified.md)
    - Plain-language v0.5 summary for ASPN leadership, educators, and community stakeholders
    - Use for non-technical pilot-readiness communication

30. [ASPN Platform Developer Handoff Addendum v0.5](./ASPN%20Platform%20Developer%20Handoff%20Addendum%20v0.5.md)
    - Current technical handoff after stakeholder management and pilot operations dashboards
    - Use as the primary developer supplement for post-26C work

31. [ASPN Platform Security Review Addendum v0.5](./ASPN%20Platform%20Security%20Review%20Addendum%20v0.5.md)
    - Security review for staff-only routes, Firebase role model, youth privacy protections, stakeholder data protections, and pilot risks
    - Use before changing authorization, public access, stakeholder records, or youth data exposure

32. [ASPN Platform Pilot Readiness Review v4](./ASPN%20Platform%20Pilot%20Readiness%20Review%20v4.md)
    - Current pilot readiness review after completion of Checkpoint 26C
    - Use as the current go/no-go reference for controlled pilot launch preparation

33. [ASPN Platform Firebase Staging Validation Report v0.5](./ASPN%20Platform%20Firebase%20Staging%20Validation%20Report%20v0.5.md)
    - Firebase staging validation checklist and report for v0.5
    - Use before pilot launch validation and before any production-readiness decision

34. [ASPN Platform Pilot Operations Runbook v0.5](./ASPN%20Platform%20Pilot%20Operations%20Runbook%20v0.5.md)
    - Staff operating guide for the first controlled ASPN Platform pilot cohort
    - Use for staff account setup, youth onboarding, program operations, credential operations, attendance, service hours, stakeholder management, dashboard review, QA checklists, and launch preparation

35. ASPN Platform Prototype 2025
   - Original backend source code
   - Status: referenced project source; add here when available

36. ASPN 3.25.26 Expedited Platform Build White Paper
   - Strategic platform vision
   - Status: referenced project source; add here when available

## Usage Guidance

- Treat the Build Manual as the historical record of how the platform reached
  the September MVP foundation.
- Treat the Developer Handoff Guide as the current technical onboarding guide
  unless superseded by a newer version.
- Treat the v0.1 Release Notes as the release record for the September MVP
  backend foundation.
- Treat the September MVP Functional Specification v1.0 as the authoritative
  specification for the next development phase.
- Use the Firebase Staging Checklist before any production deployment decision.
- Use the Firestore Migration Notes before changing user or collection schemas.
- Treat the v0.2 addendum documents as the current supplemental source set for
  work after the v0.1 backend foundation, including Firebase staging and
  frontend MVP preparation.
- Treat the v0.4 documents as the current pilot-preparation baseline after
  completion of the youth MVP, staff/admin MVP, Firebase staging validation,
  and Legacy Endpoint Security Fix.
- Treat Pilot Readiness Review v3 as the current controlled-pilot go/no-go
  reference.
- Treat the expanded v0.4 documents as the most detailed institutional
  documentation package for leadership review, developer onboarding, and
  partner/funder context.
- Treat the v0.5 documents as the current pilot-operations baseline after
  completion of Educator Management, Partner Organization Management,
  Government Organization Management, Stakeholder Relationship Notes,
  Stakeholder Reporting, Pilot Readiness, Pilot Metrics, and Pilot Evaluation.
- Treat Pilot Readiness Review v4 as the current launch-preparation go/no-go
  reference. The platform is pilot-capable, but public launch is not approved.
- Use the Pilot Operations Runbook v0.5 as the staff operating guide for
  controlled pilot QA, pilot launch preparation, weekly pilot operations, and
  staff workflow validation.
- Treat the active Firestore database as `(default)`. The separate
  `aspirationnetworkusers` database remains legacy/prototype and should not be
  used for new MVP operations without explicit approval.
- Review the full hierarchy before proposing major architectural changes.
