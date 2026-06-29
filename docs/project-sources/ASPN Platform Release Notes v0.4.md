# ASPN Platform Release Notes v0.4

Version: v0.4  
Date: June 11, 2026  
Status: Pilot Preparation Baseline  
Associated scope: Checkpoints 21A-21L

## Executive Summary

ASPN Platform v0.4 completes the youth-facing MVP, the first staff/admin MVP, Firebase staging validation, and the Legacy Endpoint Security Fix required before real youth onboarding.

The platform is now considered conditionally ready for a controlled Fall 2026 pilot serving approximately 60-100 youth participants, pending normal operational preparation, mobile/browser QA, and staff onboarding.

This release does not represent broad public production launch readiness.

## Completed Since v0.3

### Youth MVP

The youth MVP is complete through Checkpoint 21J:

- Firebase Authentication account creation and login
- Private youth profile completion
- Youth dashboard aggregation
- Program browsing and enrollment
- Credential display
- Service-hour display and request link access
- RWD Learning Center display and progress tracking
- Notifications display and read tracking
- Dashboard enhancement with profile completion, next actions, and summary cards

### Staff/Admin MVP

The first staff/admin MVP is complete through Checkpoints 21K-A through 21K-D:

- Staff metrics dashboard
- Youth management
- Program management
- Enrollment management
- Credential definition creation
- Manual credential awarding
- Service-hour record creation/review
- Service-hour request URL setting
- RWD activity creation/update/deactivation

### Security

Checkpoint 21L completed the Legacy Endpoint Security Fix.

The following legacy public profile/user endpoints now return HTTP 410 Gone:

- POST /api/createProfile
- GET /api/getUser/{id}
- GET /api/getUserWithCredentials/{id}

The following legacy discussion/post endpoints now return HTTP 410 Gone:

- GET /api/getallpost
- POST /api/createPost
- POST /api/creatPost
- POST /api/createComment
- GET /api/getCommentForPost/{postID}
- POST /api/upVote/{postID}
- DELETE /api/deletePost/{postID}

Protected replacement flows remain active:

- GET /api/me/profile
- PATCH /api/me/profile
- GET /api/me/dashboard
- Protected /api/staff/* routes

## Firebase Staging Validation

Firebase staging validation has passed locally:

- Backend tests passed locally
- Frontend production build passed locally
- Youth workflows loaded successfully
- Staff/admin workflows loaded successfully
- Staff role authorization worked
- Youth/member account was denied from staff routes
- Active Firestore database confirmed as (default)

The separate Firestore database named aspirationnetworkusers must be treated as legacy/prototype and must not be used for active MVP validation unless explicitly approved later.

## Active MVP Collections

Active MVP work should use the (default) Firestore database.

Known active collections:

- aspirationnetworkusers
- programs
- programEnrollments
- credentialDefinitions
- earnedCredentials
- attendanceRecords
- serviceHourRecords
- rwdActivities
- rwdProgress
- notifications
- systemSettings

Legacy/prototype collections may include:

- aspirationnetworkposts
- comments

Discussion/post functionality is not part of the current MVP.

## Current Readiness Scores

- Youth MVP readiness: 94%
- Staff/Admin MVP readiness: 88%
- Firebase staging readiness: 92%
- Security readiness: 88%
- Mobile readiness: 76%
- Overall pilot readiness: 88%

## Remaining Limitations

- Attendance Management UI remains deferred.
- Credential definitions cannot yet be browsed in a staff catalog UI.
- Earned credentials do not have a full staff reporting/search UI.
- Service-hour management does not yet include an all-pending review queue.
- Archived programs cannot currently be listed in the staff UI.
- Inactive RWD activities cannot currently be listed in the staff UI.
- Mobile navigation needs QA and likely refinement before larger youth rollout.
- Public launch requires additional deployment, monitoring, policy, and security work.

## Pilot Recommendation

The platform is ready for:

- Internal demo: yes
- Controlled 60-100 youth pilot: yes, with conditions
- Public launch: no

Conditions before pilot:

- Complete staff operating guide and seed data checklist
- Perform mobile QA on actual devices
- Confirm Firebase project configuration and service account permissions
- Confirm staff/admin roles for pilot staff
- Maintain active MVP data in (default) Firestore only

## Recommended Next Roadmap

1. Mobile/browser QA fixes if issues are found during live device testing
2. Staff pilot operating guide
3. Attendance Management UI only if attendance becomes an active pilot workflow
4. Credential catalog/listing improvements
5. Service-hour pending queue
6. Archived/inactive record management for programs and RWD activities
7. Production deployment readiness review

