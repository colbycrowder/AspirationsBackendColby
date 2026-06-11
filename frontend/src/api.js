import { appConfig } from "./config.js";

export class ApiAccessError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiAccessError";
    this.status = status;
  }
}

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

export async function fetchMyProfile(user) {
  if (!user) {
    throw new Error("A signed-in Firebase user is required.");
  }

  const token = await user.getIdToken();
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/me/profile`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (response.status === 401) {
    throw new Error("Firebase sign-in is required or the token was rejected.");
  }

  if (response.status === 404) {
    return null;
  }

  if (!response.ok) {
    throw new Error("Profile data is unavailable. Confirm the backend is running and try again.");
  }

  return response.json();
}

export async function saveMyProfile(user, profile) {
  if (!user) {
    throw new Error("Sign in before saving your profile.");
  }

  const token = await user.getIdToken();
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/me/profile`, {
    body: JSON.stringify(profile),
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    method: "PATCH",
  });

  if (response.status === 401) {
    throw new Error("Firebase sign-in is required or the token was rejected.");
  }

  if (response.status === 400) {
    throw new Error("Profile could not be saved. Check the profile fields and try again.");
  }

  if (!response.ok) {
    throw new Error("Profile could not be saved. Confirm the backend is running and try again.");
  }

  return response.json();
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

export async function fetchNotifications(user) {
  if (!user) {
    throw new Error("Sign in before viewing notifications.");
  }

  const token = await user.getIdToken();
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/me/notifications`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (response.status === 401) {
    throw new Error("Firebase sign-in is required or the token was rejected.");
  }

  if (!response.ok) {
    throw new Error("Notifications are unavailable. Confirm the backend is running and try again.");
  }

  const notifications = await response.json();
  return Array.isArray(notifications) ? notifications : [];
}

export async function markNotificationRead(user, notificationId) {
  if (!user) {
    throw new Error("Sign in before updating notifications.");
  }

  if (!notificationId) {
    throw new Error("Notification selection is required.");
  }

  const token = await user.getIdToken();
  const response = await fetch(
    `${trimTrailingSlash(appConfig.apiBaseUrl)}/api/me/notifications/${notificationId}/read`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      method: "PATCH",
    }
  );

  if (response.status === 401) {
    throw new Error("Firebase sign-in is required or the token was rejected.");
  }

  if (response.status === 403) {
    throw new Error("This notification does not belong to the signed-in user.");
  }

  if (!response.ok) {
    throw new Error("Notification could not be marked as read.");
  }

  return response.text();
}

export async function fetchStaffMetrics(user) {
  if (!user) {
    throw new ApiAccessError("Sign in before opening staff/admin tools.", 401);
  }

  const response = await fetchStaffEndpoint(user, "/api/staff/metrics");
  const metrics = await response.json();
  return metrics && typeof metrics === "object" ? metrics : {};
}

export async function fetchStaffYouthUsers(user) {
  const response = await fetchStaffEndpoint(user, "/api/staff/users/youth");
  const users = await response.json();
  return Array.isArray(users) ? users : [];
}

export async function fetchStaffYouthUser(user, userUID) {
  if (!userUID) {
    throw new Error("Youth user selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/users/youth/${encodeURIComponent(userUID)}`);
  return response.json();
}

export async function updateStaffYouthUser(user, userUID, updates) {
  if (!userUID) {
    throw new Error("Youth user selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/users/youth/${encodeURIComponent(userUID)}`, {
    body: JSON.stringify(updates),
    headers: {
      "Content-Type": "application/json",
    },
    method: "PATCH",
  });

  return response.text();
}

export async function createStaffProgram(user, program) {
  const response = await fetchStaffEndpoint(user, "/api/staff/programs", {
    body: JSON.stringify(program),
    headers: {
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  return response.text();
}

export async function updateStaffProgram(user, programId, updates) {
  if (!programId) {
    throw new Error("Program selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/programs/${encodeURIComponent(programId)}`, {
    body: JSON.stringify(updates),
    headers: {
      "Content-Type": "application/json",
    },
    method: "PATCH",
  });

  return response.text();
}

export async function fetchStaffProgramEnrollments(user) {
  const response = await fetchStaffEndpoint(user, "/api/staff/program-enrollments");
  const enrollments = await response.json();
  return Array.isArray(enrollments) ? enrollments : [];
}

export async function fetchStaffProgramEnrollmentsForProgram(user, programId) {
  if (!programId) {
    throw new Error("Program selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/program-enrollments/program/${encodeURIComponent(programId)}`
  );
  const enrollments = await response.json();
  return Array.isArray(enrollments) ? enrollments : [];
}

export async function removeStaffProgramEnrollment(user, enrollmentId) {
  if (!enrollmentId) {
    throw new Error("Enrollment selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/program-enrollments/${encodeURIComponent(enrollmentId)}/remove`,
    {
      method: "PATCH",
    }
  );

  return response.text();
}

async function fetchStaffEndpoint(user, path, options = {}) {
  if (!user) {
    throw new ApiAccessError("Sign in before opening staff/admin tools.", 401);
  }

  const token = await user.getIdToken();
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}${path}`, {
    ...options,
    headers: {
      ...(options.headers || {}),
      Authorization: `Bearer ${token}`,
    },
  });

  if (response.status === 401) {
    throw new ApiAccessError("Firebase sign-in is required or the token was rejected.", 401);
  }

  if (response.status === 403) {
    throw new ApiAccessError("Staff or admin access is required for this page.", 403);
  }

  if (!response.ok) {
    throw new ApiAccessError("Staff/admin data is unavailable. Confirm the backend is running and try again.", response.status);
  }

  return response;
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
