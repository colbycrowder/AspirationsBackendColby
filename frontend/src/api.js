import { appConfig } from "./config.js";

export async function fetchActivePrograms() {
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/programs`);

  if (!response.ok) {
    throw new Error("Programs are unavailable. Confirm the backend is running and Firebase staging is configured.");
  }

  const programs = await response.json();
  return Array.isArray(programs) ? programs : [];
}

export async function fetchYouthDashboard(user) {
  if (!user) {
    throw new Error("A signed-in Firebase user is required.");
  }

  const token = await user.getIdToken();
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/me/dashboard`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (response.status === 401) {
    throw new Error("Firebase sign-in is required or the token was rejected.");
  }

  if (response.status === 404) {
    throw new Error("Dashboard data is unavailable because this Firebase user does not have a matching Firestore profile yet.");
  }

  if (!response.ok) {
    throw new Error("Dashboard data is unavailable. Confirm the backend is running and Firebase staging is configured.");
  }

  const dashboard = await response.json();
  if (!dashboard || Object.keys(dashboard).length === 0) {
    throw new Error("Dashboard data is unavailable because the backend returned an empty dashboard response.");
  }

  return dashboard;
}

export async function enrollInProgram(user, programId) {
  if (!user) {
    throw new Error("Sign in before enrolling in a program.");
  }

  if (!programId) {
    throw new Error("Program selection is required before enrolling.");
  }

  const token = await user.getIdToken();
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/me/program-enrollments`, {
    body: JSON.stringify({ programId }),
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  if (response.status === 401) {
    throw new Error("Firebase sign-in is required or the token was rejected.");
  }

  if (response.status === 400) {
    const message = await readResponseMessage(response);
    if (message.toLowerCase().includes("already enrolled")) {
      throw new Error("You are already enrolled in this program.");
    }

    if (message.toLowerCase().includes("active program")) {
      throw new Error("This program is archived or unavailable for enrollment.");
    }

    throw new Error(message || "Enrollment could not be completed for this program.");
  }

  if (!response.ok) {
    throw new Error("Enrollment failed. Confirm the backend is running and try again.");
  }

  return response.text();
}

export async function saveRwdProgress(user, progress) {
  if (!user) {
    throw new Error("Sign in before updating RWD progress.");
  }

  if (!progress?.rwdActivityId) {
    throw new Error("RWD activity selection is required before updating progress.");
  }

  const token = await user.getIdToken();
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/me/rwd-progress`, {
    body: JSON.stringify(progress),
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  if (response.status === 401) {
    throw new Error("Firebase sign-in is required or the token was rejected.");
  }

  if (response.status === 400) {
    throw new Error("RWD progress could not be saved for this activity.");
  }

  if (!response.ok) {
    throw new Error("RWD progress update failed. Confirm the backend is running and try again.");
  }

  return response.json();
}

async function readResponseMessage(response) {
  try {
    return await response.text();
  } catch {
    return "";
  }
}

function trimTrailingSlash(value) {
  return value.endsWith("/") ? value.slice(0, -1) : value;
}
