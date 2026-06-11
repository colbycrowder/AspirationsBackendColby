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
The youth Programs page connects to active-program listing and youth
self-enrollment endpoints.

Staff/admin pages do not connect to backend APIs yet, and the app does not
perform role-based staff/admin authorization yet.

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
user. Staff/admin role checks are intentionally deferred to a later checkpoint.

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
