# ASPN Platform Security Review Addendum v0.4

Version: v0.4  
Date: June 11, 2026  
Status: Active security addendum

## Purpose

This addendum records the security review and remediation completed before controlled pilot preparation.

## Security Posture Summary

The current platform security model is based on:

- Firebase Authentication for user identity
- Firebase ID token verification in the Spring Boot backend
- Firestore user profile role checks for staff/admin access
- Private-by-default youth profiles
- No public youth directory
- No direct messaging
- No follower/friend system
- No popularity-driven features

## Backend Protected Routes

Youth-owned routes use the verified Firebase token UID:

- GET /api/me/profile
- PATCH /api/me/profile
- GET /api/me/dashboard
- POST /api/me/program-enrollments
- GET /api/me/rwd-progress
- POST /api/me/rwd-progress
- GET /api/me/notifications
- PATCH /api/me/notifications/{notificationId}/read

Staff/admin routes require:

- valid Firebase ID token
- Firestore profile role of `staff` or `admin`

Reserved roles do not receive staff access:

- educator
- partner
- government

## Legacy Endpoint Risk

The security review identified legacy public endpoints as the final major pilot blocker.

Critical risks:

- Public profile creation with client-supplied UID
- Public profile lookup by UID
- Public credential/profile lookup by UID
- Public discussion deletion

High risks:

- Public post creation
- Public comment creation
- Public post listing
- Public comment listing

Medium/high risks:

- Public upvotes
- Public comment retrieval

## Checkpoint 21L Remediation

The following endpoints now return HTTP 410 Gone:

- POST /api/createProfile
- GET /api/getUser/{id}
- GET /api/getUserWithCredentials/{id}
- GET /api/getallpost
- POST /api/createPost
- POST /api/creatPost
- POST /api/createComment
- GET /api/getCommentForPost/{postID}
- POST /api/upVote/{postID}
- DELETE /api/deletePost/{postID}

## Security Impact

Before 21L:

- Public clients could call legacy endpoints without Firebase token verification.
- Some endpoints accepted client-supplied UIDs.
- Some endpoints exposed youth profile or credential data.
- Some endpoints mutated discussion records without authorization.

After 21L:

- Legacy endpoints are disabled.
- The MVP uses protected `/api/me/*` and `/api/staff/*` route patterns.
- Youth-owned routes use verified Firebase UID.
- Staff/admin routes use backend role enforcement.

## Remaining Security Concerns

Before public launch:

- Review Firebase/Firestore security rules
- Review service account permissions
- Review deployment environment variables
- Add production monitoring/logging strategy
- Add backup/restore plan
- Add incident response procedure
- Review CORS origins before production
- Review rate limiting or abuse controls

## Current Recommendation

Security is sufficient for a controlled pilot with staff oversight, after normal operational validation.

Security is not yet sufficient for broad public launch.

