import { appConfig } from "./config.js";

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

function trimTrailingSlash(value) {
  return value.endsWith("/") ? value.slice(0, -1) : value;
}
