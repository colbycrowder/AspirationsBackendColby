# ASPN Platform Frontend

This is the frontend scaffold for the ASPN Platform September MVP.

## Framework

- Vite
- React
- JavaScript

## Setup

Install dependencies:

```bash
cd frontend
npm install
```

Create a local environment file:

```bash
cp .env.example .env
```

Fill `.env` with the Firebase web app values from the Firebase console.
Do not commit real Firebase values or secrets.

## Run Locally

```bash
npm run dev
```

Expected local URL:

```text
http://localhost:5173
```

The backend currently allows this origin through CORS.

## Backend for Dashboard Data

The youth dashboard calls:

```text
GET /api/me/dashboard
```

using:

```text
Authorization: Bearer <Firebase ID token>
```

Run the backend separately when testing dashboard data:

```bash
cd ../UserData
./gradlew bootRun
```

`VITE_API_BASE_URL` should point to the backend, usually:

```text
http://localhost:8080
```

## Current Scope

This scaffold includes placeholder pages for September MVP youth and staff/admin screens.
It includes Firebase Authentication login, account creation, logout, auth state,
and protected route shell behavior.

The youth dashboard connects to the backend dashboard endpoint.
The youth Profile page uses protected self-service profile endpoints to create
or update basic private profile fields.
The youth Programs page connects to active-program listing and youth
self-enrollment endpoints.
The youth Credentials page uses the dashboard response to show earned and
available credentials.
The youth RWD Learning Center page uses the dashboard response to show active
RWD items and uses the youth progress endpoint for simple status updates.
The youth Notifications page uses youth notification endpoints to show unread
counts and mark notifications as read.
The youth Service Hours page uses the dashboard response to show records,
status totals, and the configured request form URL.

The staff dashboard and metrics page now use the protected backend metrics
endpoint as the staff/admin authorization check.
Other staff/admin pages remain placeholders for later Staff/Admin MVP
checkpoints.

## Environment Variables

- `VITE_API_BASE_URL`
- `VITE_FIREBASE_API_KEY`
- `VITE_FIREBASE_AUTH_DOMAIN`
- `VITE_FIREBASE_PROJECT_ID`
- `VITE_FIREBASE_STORAGE_BUCKET`
- `VITE_FIREBASE_MESSAGING_SENDER_ID`
- `VITE_FIREBASE_APP_ID`

## Firebase Auth

Enable Email/Password sign-in in the Firebase project before testing login or
account creation.

Protected youth and staff/admin placeholder pages require a signed-in Firebase
user. Staff/admin data access is verified by the backend using Firestore role
values of `staff` or `admin`.

## Local Auth Test

1. Start the app with `npm run dev`.
2. Open `http://localhost:5173`.
3. Go to `Create Account`.
4. Create a test Firebase user with email and password.
5. Confirm the app redirects to `Youth Dashboard`.
6. Click `Logout`.
7. Go to `Login` and sign in with the same account.

## Local Dashboard API Test

1. Start the backend with `./gradlew bootRun` from `../UserData`.
2. Start the frontend with `npm run dev`.
3. Sign in with Firebase.
4. Open `Youth Dashboard`.
5. Confirm dashboard sections load from the backend.

If the backend is not running, or the signed-in Firebase user does not have a
matching Firestore profile document, the dashboard shows a clear unavailable
message instead of failing silently.

## Local Staff Metrics Test

The Staff Dashboard and Metrics page call:

```text
GET /api/staff/metrics
Authorization: Bearer <Firebase ID token>
```

To test locally:

1. Start the backend with `./gradlew bootRun` from `../UserData`.
2. Start the frontend with `npm run dev`.
3. Sign in with a Firebase user whose Firestore profile has `role = staff` or `role = admin`.
4. Open `Staff Dashboard` or `Metrics`.
5. Confirm platform metrics render.

Expected access behavior:

- Signed-out users see a sign-in requirement.
- Signed-in youth/member users see an access denied message.
- If the backend is unavailable, the page shows a backend unavailable message.

## Local Profile Completion Test

The Profile page reads:

```text
GET /api/me/profile
Authorization: Bearer <Firebase ID token>
```

It saves basic youth-owned profile fields with:

```text
PATCH /api/me/profile
Authorization: Bearer <Firebase ID token>
Content-Type: application/json
```

The backend uses the verified Firebase UID. The frontend does not send UID,
role, staff/admin status, verification flags, or public profile settings.

To test locally:

1. Start the backend with `./gradlew bootRun` from `../UserData`.
2. Start the frontend with `npm run dev`.
3. Sign in with Firebase.
4. Open `Profile`.
5. Fill first name, last name, school, graduation year, and optional interests.
6. Save the profile.
7. Open `Youth Dashboard` and confirm school/graduation year appear in the profile summary.

## Local Credentials Test

The Credentials page uses:

```text
GET /api/me/dashboard
Authorization: Bearer <Firebase ID token>
```

To test locally:

1. Start the backend with `./gradlew bootRun` from `../UserData`.
2. Start the frontend with `npm run dev`.
3. Sign in with Firebase.
4. Confirm the signed-in Firebase UID has a matching Firestore profile document.
5. Open `Credentials`.
6. Confirm earned credentials and available credentials render from the dashboard response.

If the user has no earned credentials or no available credentials for enrolled
programs, the page shows safe empty states.

## Local Service Hours Test

The Service Hours page uses:

```text
GET /api/me/dashboard
Authorization: Bearer <Firebase ID token>
```

To test locally:

1. Start the backend with `./gradlew bootRun` from `../UserData`.
2. Start the frontend with `npm run dev`.
3. Sign in with Firebase.
4. Confirm the signed-in Firebase UID has a matching Firestore profile document.
5. Open `Service Hours`.
6. Confirm submitted service-hour records, status totals, and the request form link render from the dashboard response.

Youth service-hour submission is not currently exposed as a backend youth route.
The page links to the configured request form URL when staff/admin has set one.

## Local RWD Learning Center Test

The RWD Learning Center page reads:

```text
GET /api/me/dashboard
Authorization: Bearer <Firebase ID token>
```

Progress updates call:

```text
POST /api/me/rwd-progress
Authorization: Bearer <Firebase ID token>
Content-Type: application/json

{
  "rwdActivityId": "RWD_ACTIVITY_ID",
  "completionStatus": "in_progress"
}
```

To test locally:

1. Start the backend with `./gradlew bootRun` from `../UserData`.
2. Start the frontend with `npm run dev`.
3. Sign in with Firebase.
4. Confirm the signed-in Firebase UID has a matching Firestore profile document.
5. Confirm Firestore has active documents in the `rwdActivities` collection.
6. Open `RWD Learning Center`.
7. Open an activity link or mark an item in progress/completed.
8. Confirm the page refreshes progress from the dashboard response.

The frontend does not create quiz questions. Quiz score and credential-award
logic remain backend-supported workflows for a later UI checkpoint.

## Local Notifications Test

The Notifications page reads:

```text
GET /api/me/notifications
Authorization: Bearer <Firebase ID token>
```

It marks notifications read with:

```text
PATCH /api/me/notifications/{notificationId}/read
Authorization: Bearer <Firebase ID token>
```

To test locally:

1. Start the backend with `./gradlew bootRun` from `../UserData`.
2. Start the frontend with `npm run dev`.
3. Sign in with Firebase.
4. Confirm the signed-in Firebase UID has a matching Firestore profile document.
5. Confirm the user has notification documents in the `notifications` collection, or award a credential to generate one.
6. Open `Notifications`.
7. Confirm unread/read status appears.
8. Click `Mark as Read` on an unread notification.
9. Confirm the notification updates and the unread count decreases.

## Local Programs and Enrollment Test

The Programs page calls:

```text
GET /api/programs
```

Enrollment calls:

```text
POST /api/me/program-enrollments
Authorization: Bearer <Firebase ID token>
Content-Type: application/json

{
  "programId": "PROGRAM_ID"
}
```

To test locally:

1. Start the backend with `./gradlew bootRun` from `../UserData`.
2. Start the frontend with `npm run dev`.
3. Sign in with Firebase.
4. Confirm the signed-in Firebase UID has a matching Firestore profile document.
5. Confirm Firestore has at least one active document in the `programs` collection.
6. Open `Programs`.
7. Click `Enroll` on an active program.
8. Return to `Youth Dashboard` and confirm the enrolled active program appears.

Duplicate enrollment attempts should show a friendly already-enrolled message.
Archived programs are not returned by `GET /api/programs`; if a stale archived
program ID is submitted, the backend rejects it and the frontend shows an
unavailable-program message.
