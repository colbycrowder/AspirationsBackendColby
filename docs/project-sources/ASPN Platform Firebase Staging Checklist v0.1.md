# ASPN Platform Firebase Staging Checklist v0.1

## Purpose

This document defines the procedures required to safely validate ASPN Platform
v0.1 in a Firebase staging environment prior to production deployment.

## Status

Ready For Staging Validation

## Version

Firebase Staging Checklist v0.1

## 1. Firebase Project Setup

- Use a dedicated staging Firebase project.
- Never test against production.
- Enable Firebase Authentication.
- Enable Firestore.
- Enable Hosting if applicable.

## 2. Backend Environment Validation

- Verify Firebase Admin credentials.
- Verify FirebaseApp initialization.
- Verify Firestore initialization.
- Verify FirebaseAuth initialization.

## 3. Firestore Collection Validation

Verify existence of:

- `aspirationnetworkusers`
- `aspirationnetworkposts`
- `comments`
- `credentialDefinitions`
- `earnedCredentials`
- `attendanceRecords`
- `serviceHourRecords`

## 4. Test Account Creation

Youth test account:

- `role = member`
- `youthProfile = true`
- `publicProfile = false`
- `profileStatus = pending_onboarding`

Staff test account:

- `role = staff`

Admin test account:

- `role = admin`

## 5. Authentication Testing

Verify:

- Valid token accepted.
- Invalid token rejected.
- Missing token rejected.

Verify:

- `GET /api/me/profile` returns `200` for authenticated users.
- `GET /api/me/profile` returns `401` for unauthenticated users.

## 6. Staff Authorization Testing

Verify members cannot:

- `POST /api/staff/credentials/definitions`
- `POST /api/staff/credentials/award`
- `POST /api/staff/attendance`
- `POST /api/staff/service-hours`

Expected result:

- `403 Forbidden`

Verify staff/admin users can successfully perform these actions.

## 7. Credential Workflow Testing

- Create credential definition.
- Award credential.

Verify:

- `credentialDefinitions` collection updated.
- `earnedCredentials` collection updated.
- `GET /api/me/profile` displays earned credential.

## 8. Attendance Workflow Testing

- Create attendance record.

Verify:

- `attendanceRecords` updated.
- `attendanceRecordIds` updated.
- Attendance appears in profile retrieval.

## 9. Service Hour Workflow Testing

- Create service-hour record.

Verify:

- `serviceHourRecords` updated.
- `serviceHourRecordIds` updated.
- Service hours appear in profile retrieval.

## 10. Frontend Integration Testing

Verify frontend sends:

```http
Authorization: Bearer <Firebase Token>
```

Verify:

- Login.
- Profile retrieval.
- Credential display.
- Attendance display.
- Service-hour display.

## 11. Security Validation

Verify youth users cannot:

- Award credentials.
- Create attendance.
- Create service hours.
- Access staff routes.

Verify invalid tokens are rejected.

## 12. Staging Exit Criteria

All tests must pass before production consideration.

If any fail:

**DO NOT DEPLOY TO PRODUCTION**
