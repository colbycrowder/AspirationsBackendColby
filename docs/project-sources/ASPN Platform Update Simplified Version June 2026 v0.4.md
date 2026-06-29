# ASPN Platform Update Simplified Version - June 2026 v0.4

Version: v0.4  
Date: June 11, 2026  
Audience: Non-technical project stakeholders

## Summary

The ASPN Platform has reached a strong pilot preparation stage.

The youth experience is built. The staff/admin tools needed to manage a controlled pilot are also built. Firebase staging validation has passed locally. A final security issue involving old prototype endpoints has been addressed.

The platform is now considered ready for an internal demo and conditionally ready for a controlled Fall 2026 pilot with 60-100 youth participants.

It is not yet ready for full public launch.

## What Youth Can Do

Youth can:

- Create an account
- Log in
- Complete their private profile
- View a dashboard
- Join active programs
- View earned and available credentials
- View service-hour records
- Access the service-hour request link
- Open RWD Learning Center activities
- Track RWD progress
- View notifications

## What Staff/Admin Can Do

Staff/admin users can:

- View basic platform metrics
- Review youth users
- Update youth onboarding/review status
- Create and update programs
- Review and remove enrollments
- Create credential definitions
- Award credentials
- Create or review service-hour records
- Set the service-hour request form link
- Create and update RWD activities

## What Was Fixed for Safety

Older prototype endpoints were disabled because they were not safe enough for real youth onboarding.

Disabled legacy areas include:

- public profile creation
- public profile lookup
- public credential/profile lookup
- old discussion and post endpoints

The current MVP now relies on safer signed-in routes.

## Firebase Decision

The active Firestore database is:

- (default)

The separate database named:

- aspirationnetworkusers

should be treated as old prototype data and should not be used for the pilot unless formally reviewed later.

## Current Readiness

- Youth MVP: ready
- Staff/Admin MVP: ready for pilot operations
- Firebase staging: passed locally
- Security: improved after legacy endpoint fix
- Mobile experience: usable but needs device QA
- Public launch: not ready yet

## Remaining Limitations

- Attendance staff UI is not built yet.
- Staff cannot browse a full credential catalog in the app yet.
- Staff do not yet have a full pending service-hour queue.
- Archived programs and inactive RWD activities are not easy to recover through the app.
- Mobile polish may be needed after testing on real phones.

## Recommendation

Proceed with:

1. Internal demo
2. Staff training
3. Mobile/browser QA
4. Controlled Fall 2026 pilot planning

Do not proceed with a public launch until production security, policy, support, and monitoring are complete.

