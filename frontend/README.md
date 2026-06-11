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
