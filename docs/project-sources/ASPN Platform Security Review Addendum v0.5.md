# ASPN Platform Security Review Addendum v0.5

## Purpose

This addendum documents the security posture after completion of Checkpoints 25A through 26C.

Current status: **Pilot-Capable with staff oversight**  
Public launch status: **Not approved**

## Authorization Model

All staff routes require:
- Firebase ID token
- backend token verification
- `AuthService.requireStaff(...)`

Allowed roles:
- `staff`
- `admin`

Denied or reserved roles:
- `member`
- `youth`
- `educator`
- `partner`
- `government`

Reserved roles must not receive staff access unless explicitly approved in a future security review.

## Staff-Only Route Groups

Staff-only route groups include:
- user management
- program management
- enrollment management
- credential management
- attendance management
- service-hour management
- RWD management
- metrics reporting
- operations reporting
- educator management
- partner organization management
- government organization management
- stakeholder relationship notes
- pilot readiness
- pilot metrics
- pilot evaluation

## Youth Privacy Protections

Current protections:
- youth profiles remain private by default
- no public youth directory
- no public profile discovery
- no peer-to-peer messaging
- no follower system
- no popularity metrics
- no youth public search
- youth-owned routes use verified Firebase UID where applicable
- staff-controlled credential awarding
- staff-controlled attendance records
- staff-controlled service-hour review

## Stakeholder Data Protections

Protected stakeholder records:
- educator contacts
- partner organization contacts
- government organization contacts
- relationship notes
- follow-up dates
- relationship owner UID

These records are staff-only. No educator, partner, or government portal was created in v0.5.

## Legacy Endpoint Security

Legacy public profile and discussion endpoints were previously disabled with `HTTP 410 Gone`.

These endpoints should remain disabled unless a future checkpoint explicitly redesigns and secures the related feature area.

Legacy endpoints to keep disabled:
- `POST /api/createProfile`
- `GET /api/getUser/{id}`
- `GET /api/getUserWithCredentials/{id}`
- `GET /api/getallpost`
- `POST /api/createPost`
- `POST /api/creatPost`
- `POST /api/createComment`
- `GET /api/getCommentForPost/{postID}`
- `POST /api/upVote/{postID}`
- `DELETE /api/deletePost/{postID}`

## Firebase Security Assumptions

The backend enforces staff/admin access through Firebase ID token verification and Firestore user role checks.

Before public launch, the project still needs:
- Firestore rules review
- Firebase Hosting review
- environment variable audit
- service account audit
- monitoring/logging plan
- backup and recovery plan

## Pilot Risks

Operational risks:
- staff training required
- manual data entry quality
- incomplete stakeholder records
- incomplete credential records
- inconsistent follow-up dates
- incomplete platform event history

Technical risks:
- production readiness review pending
- limited load testing
- limited mobile QA
- limited browser QA
- no automated backup verification documented
- Firestore rules audit still needed before broad public launch

Risk level: **Moderate for controlled pilot; not acceptable for broad public launch without further review.**

## Security Determination

The platform is appropriate for controlled pilot preparation with trained staff and limited youth onboarding.

The platform is not yet approved for:
- public launch
- public youth discovery
- public stakeholder portals
- external integrations
- open registration at scale

