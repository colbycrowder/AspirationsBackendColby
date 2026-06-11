# ASPN Platform Firestore Migration Notes v0.1

## Purpose

This document records Firestore schema changes introduced during ASPN Platform
v0.1 and provides guidance for handling older user records.

## Status

Active

## Version

Firestore Migration Notes v0.1

## Associated Release

ASPN Platform v0.1

## Background

Original Prototype 2025 supported:

- Users
- Posts
- Comments
- Upvotes

ASPN Platform v0.1 added:

- Credential System
- Attendance System
- Service Hour System
- Staff Management System
- Authentication System
- Expanded Profile System
- Program Foundation
- Enrollment Foundation

## Collection: aspirationnetworkusers

### New User Fields Introduced

`profileStatus`

Examples:

- `pending_onboarding`
- `active`
- `inactive`

`profileImageUrl`

Purpose:

- Store a future profile image URL or remain empty for first-initial fallback.

`school`

Purpose:

- Store the youth user's school.

`graduationYear`

Purpose:

- Store the youth user's graduation year without storing grade level.

`youthProfile`

Purpose:

- Identify youth accounts.

`publicProfile`

Default:

- `false`

`programIds`

`programParticipationIds`

Purpose:

- Track ASPN participation.

`collegeInterests`

`desiredMajor`

Purpose:

- Store optional postsecondary pathway interests.

`careerInterests`

`desiredCareerFields`

`governmentCareerInterests`

`workforceInterests`

Purpose:

- Store optional workforce and government pathway interests.

`civicInterests`

`communityInterests`

`publicServiceInterests`

Purpose:

- Store optional civic and community pathway interests.

`earnedCredentialIds`

Purpose:

- Associate users with earned credentials.

`attendanceRecordIds`

Purpose:

- Associate users with attendance records.

`serviceHourRecordIds`

Purpose:

- Associate users with service-hour records.

`staffReviewRequired`

Purpose:

- Support onboarding review workflows.

`staffVerified`

Purpose:

- Support profile verification workflows.

`externalConsentReceived`

Purpose:

- Store consent status only. Do not store consent forms.

## Legacy User Risk

Users created before Checkpoint 2 may not contain:

- `profileStatus`
- `profileImageUrl`
- `school`
- `graduationYear`
- `youthProfile`
- `publicProfile`
- `programIds`
- `programParticipationIds`
- `collegeInterests`
- `desiredMajor`
- `careerInterests`
- `desiredCareerFields`
- `governmentCareerInterests`
- `workforceInterests`
- `civicInterests`
- `communityInterests`
- `publicServiceInterests`
- `earnedCredentialIds`
- `attendanceRecordIds`
- `serviceHourRecordIds`
- `staffReviewRequired`
- `staffVerified`
- `externalConsentReceived`

Rule:

**Missing Field Does Not Equal Invalid User**

## Safe Handling Guidance

Backend and frontend should:

- Treat missing lists as empty lists.
- Treat missing booleans as safe defaults.
- Avoid null-pointer failures.
- Avoid migration assumptions.

## Collections Added During v0.1

- `credentialDefinitions`
- `earnedCredentials`
- `attendanceRecords`
- `serviceHourRecords`
- `programs`
- `programEnrollments`
- `rwdActivities`
- `rwdProgress`
- `systemSettings`
- `notifications`

## Credential Definition Structure

- `credentialID`
- `credentialName`
- `description`
- `icon`
- `category`
- `active`
- `programIds`
- `requirements`
- `requirementText`
- `autoAwardEnabled`
- `requirementType`
- `requiredAttendanceCount`

Credential requirement structure:

- `requirementType`
- `requirementText`
- `requiredCount`
- `relatedProgramId`
- `relatedFormId`

Supported future requirement types:

- `attendance_count`
- `rwd_quiz_passed`
- `manual_award`
- `service_hours`
- `form_completion`

Credential handling guidance:

- Credential definitions may be associated with programs through `programIds`.
- Missing `programIds` should be treated as an empty list.
- Missing `requirements` should be treated as an empty list.
- Missing or empty `icon` should use the default credential icon behavior.
- `attendance_count` can auto-award credentials when `autoAwardEnabled` is true.
- Only `present` attendance records count toward `attendance_count`.
- Other requirement types remain future-facing and should not auto-award credentials yet.

## Earned Credential Structure

- `earnedCredentialID`
- `credentialID`
- `userUID`
- `awardedByStaffUID`
- `awardDate`

## Attendance Structure

- `attendanceRecordID`
- `userUID`
- `programID`
- `eventName`
- `eventDate`
- `attendanceStatus`
- `recordedByStaffUID`

## Service Hour Structure

- `serviceHourRecordID`
- `userUID`
- `programID`
- `serviceDate`
- `hours`
- `verificationStatus`
- `reviewedByStaffUID`
- `googleFormResponseUrl`

## Program Structure

- `programId`
- `programName`
- `description`
- `startDate`
- `endDate`
- `category`
- `programImageUrl`
- `programLeader`
- `capacity`
- `programStatus`
- `createdByStaffUID`
- `createdAt`
- `updatedAt`

Program status values:

- `active`
- `archived`

Program handling guidance:

- Staff and admin users may create or update programs through protected backend routes.
- Youth-facing program reads should expose active programs only.
- Missing `programIds` or `programParticipationIds` on older user records should be treated as empty lists.
- Enrollment records are stored separately in `programEnrollments`.

## Program Enrollment Structure

- `enrollmentId`
- `userUID`
- `programId`
- `enrollmentStatus`
- `enrolledAt`
- `updatedAt`
- `createdByUser`
- `removedByStaffUID`

Enrollment status values:

- `active`
- `removed`

Enrollment handling guidance:

- Youth self-enrollment must use the verified Firebase token UID.
- Client-supplied user IDs must not determine enrollment ownership.
- Youth users may enroll only into active programs.
- Duplicate active enrollments for the same user and program should be rejected.
- Staff and admin users may view and remove enrollments through protected backend routes.
- Program capacity enforcement is not part of v0.1 enrollment foundation.

## RWD Activity Structure

- `rwdActivityId`
- `countryName`
- `title`
- `description`
- `externalUrl`
- `active`
- `associatedCredentialId`
- `createdAt`
- `updatedAt`

RWD activity country set:

- `Bangladesh`
- `Bulgaria`
- `Indonesia`
- `Kenya`
- `Madagascar`
- `Mexico`
- `Mongolia`
- `Morocco`
- `Nepal`
- `Nigeria`
- `Peru`
- `Philippines`
- `Serbia`
- `Tanzania`
- `Timor-Leste`
- `United States`

RWD handling guidance:

- Videos and activities remain externally hosted.
- The default external source is `https://aspirationsnetwork.org/movement-map/`.
- RWD activities may optionally link to a credential through `associatedCredentialId`.
- RWD activity records do not create a finalized credential catalog.

## RWD Progress Structure

- `progressId`
- `userUID`
- `rwdActivityId`
- `completionStatus`
- `quizScore`
- `passed`
- `completedAt`
- `credentialAwarded`
- `earnedCredentialId`

RWD progress completion statuses:

- `not_started`
- `in_progress`
- `completed`

RWD progress handling guidance:

- `quizScore` must be between `0` and `100`.
- `quizScore >= 80` marks `passed = true` and `completionStatus = completed`.
- Linked credential award is allowed only when the activity has `associatedCredentialId`.
- RWD credential awarding should avoid duplicate earned credentials.
- Actual quiz questions, Google Form sync, and RWD metrics are not part of v0.1 foundation.

## System Setting Structure

- `settingKey`
- `settingValue`
- `updatedByStaffUID`
- `createdAt`
- `updatedAt`

Current system setting keys:

- `serviceHourRequestFormUrl`

System setting handling guidance:

- Staff and admin users may update system settings through protected backend routes.
- Youth users may read `serviceHourRequestFormUrl` through the dashboard response.
- Missing `serviceHourRequestFormUrl` should be treated as `null`.
- The final Google Form URL should not be hardcoded in code.
- Google Form sync is not part of v0.1 foundation.

## Notification Structure

- `notificationId`
- `userUID`
- `notificationType`
- `title`
- `message`
- `relatedCredentialId`
- `relatedEarnedCredentialId`
- `read`
- `createdAt`

Current notification types:

- `credential_earned`

Notification handling guidance:

- Credential-earned notifications are created when a new earned credential is created.
- Youth users may retrieve only their own notifications through protected youth routes.
- Youth users may mark only their own notifications as read.
- Dashboard responses may include an unread notification count.
- Email, SMS, push notifications, and notification metrics are not part of v0.1 foundation.

## Future Migration Requirement

Whenever user schema changes, update:

- Firestore Migration Notes
- Release Notes
- Build Manual
- Developer Handoff Guide
