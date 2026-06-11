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

14. ASPN Platform Prototype 2025
   - Original backend source code
   - Status: referenced project source; add here when available

15. ASPN 3.25.26 Expedited Platform Build White Paper
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
- Review the full hierarchy before proposing major architectural changes.
