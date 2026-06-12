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

export async function fetchActiveRwdActivities() {
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/rwd/activities`);

  if (!response.ok) {
    throw new Error("RWD activities are unavailable. Confirm the backend is running and Firebase staging is configured.");
  }

  const activities = await response.json();
  return Array.isArray(activities) ? activities : [];
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

export async function trackPlatformEvent(user, eventType, metadata = {}) {
  if (!user || !eventType) {
    return null;
  }

  const token = await user.getIdToken();
  const response = await fetch(`${trimTrailingSlash(appConfig.apiBaseUrl)}/api/me/platform-events`, {
    body: JSON.stringify({ eventType, metadata }),
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  if (!response.ok) {
    return null;
  }

  return response.text();
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

export async function fetchStaffOperationsReporting(user) {
  const response = await fetchStaffEndpoint(user, "/api/staff/operations/reporting");
  const report = await response.json();
  return report && typeof report === "object" ? report : {};
}

export async function fetchStaffYouthUsers(user) {
  const response = await fetchStaffEndpoint(user, "/api/staff/users/youth");
  const users = await response.json();
  return Array.isArray(users) ? users : [];
}

export async function fetchStaffUsers(user, filters = {}) {
  const params = new URLSearchParams();
  if (filters.role) {
    params.set("role", filters.role);
  }
  if (filters.active !== undefined && filters.active !== "") {
    params.set("active", String(filters.active));
  }
  if (filters.youthProfile !== undefined && filters.youthProfile !== "") {
    params.set("youthProfile", String(filters.youthProfile));
  }
  if (filters.programId) {
    params.set("programId", filters.programId);
  }

  const query = params.toString();
  const response = await fetchStaffEndpoint(user, `/api/staff/users${query ? `?${query}` : ""}`);
  const users = await response.json();
  return Array.isArray(users) ? users : [];
}

export async function fetchStaffUser(user, userUID) {
  if (!userUID) {
    throw new Error("User selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/users/${encodeURIComponent(userUID)}`);
  return response.json();
}

export async function fetchStaffUserTotals(user) {
  const response = await fetchStaffEndpoint(user, "/api/staff/users/totals");
  const totals = await response.json();
  return totals && typeof totals === "object" ? totals : {};
}

export async function updateStaffUser(user, userUID, updates) {
  if (!userUID) {
    throw new Error("User selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/users/${encodeURIComponent(userUID)}`, {
    body: JSON.stringify(updates),
    headers: {
      "Content-Type": "application/json",
    },
    method: "PATCH",
  });

  return response.text();
}

export async function activateStaffUser(user, userUID) {
  if (!userUID) {
    throw new Error("User selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/users/${encodeURIComponent(userUID)}/activate`, {
    method: "PATCH",
  });

  return response.text();
}

export async function deactivateStaffUser(user, userUID) {
  if (!userUID) {
    throw new Error("User selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/users/${encodeURIComponent(userUID)}/deactivate`, {
    method: "PATCH",
  });

  return response.text();
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

export async function fetchStaffPrograms(user, filters = {}) {
  const params = new URLSearchParams();
  if (filters.active !== undefined && filters.active !== "") {
    params.set("active", String(filters.active));
  }
  if (filters.programType) {
    params.set("programType", filters.programType);
  }

  const query = params.toString();
  const response = await fetchStaffEndpoint(user, `/api/staff/programs${query ? `?${query}` : ""}`);
  const programs = await response.json();
  return Array.isArray(programs) ? programs : [];
}

export async function fetchStaffProgramDetail(user, programId) {
  if (!programId) {
    throw new Error("Program selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/programs/${encodeURIComponent(programId)}`);
  return response.json();
}

export async function fetchStaffProgramTotals(user) {
  const response = await fetchStaffEndpoint(user, "/api/staff/programs/totals");
  const totals = await response.json();
  return totals && typeof totals === "object" ? totals : {};
}

export async function archiveStaffProgram(user, programId) {
  if (!programId) {
    throw new Error("Program selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/programs/${encodeURIComponent(programId)}/archive`, {
    method: "PATCH",
  });

  return response.text();
}

export async function restoreStaffProgram(user, programId) {
  if (!programId) {
    throw new Error("Program selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/programs/${encodeURIComponent(programId)}/restore`, {
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

export async function createStaffCredentialDefinition(user, credentialDefinition) {
  const response = await fetchStaffEndpoint(user, "/api/staff/credentials/definitions", {
    body: JSON.stringify(credentialDefinition),
    headers: {
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  return response.text();
}

export async function fetchStaffCredentialDefinitions(user, filters = {}) {
  const params = new URLSearchParams();
  if (filters.category) {
    params.set("category", filters.category);
  }
  if (filters.active !== undefined && filters.active !== "") {
    params.set("active", String(filters.active));
  }
  if (filters.programId) {
    params.set("programId", filters.programId);
  }

  const query = params.toString();
  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/credentials/definitions${query ? `?${query}` : ""}`
  );
  const definitions = await response.json();
  return Array.isArray(definitions) ? definitions : [];
}

export async function fetchStaffCredentialTotals(user, filters = {}) {
  const params = new URLSearchParams();
  if (filters.category) {
    params.set("category", filters.category);
  }
  if (filters.programId) {
    params.set("programId", filters.programId);
  }

  const query = params.toString();
  const response = await fetchStaffEndpoint(user, `/api/staff/credentials/totals${query ? `?${query}` : ""}`);
  const totals = await response.json();
  return totals && typeof totals === "object" ? totals : {};
}

export async function updateStaffCredentialDefinition(user, credentialId, updates) {
  if (!credentialId) {
    throw new Error("Credential selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/credentials/definitions/${encodeURIComponent(credentialId)}`,
    {
      body: JSON.stringify(updates),
      headers: {
        "Content-Type": "application/json",
      },
      method: "PATCH",
    }
  );

  return response.text();
}

export async function archiveStaffCredentialDefinition(user, credentialId) {
  if (!credentialId) {
    throw new Error("Credential selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/credentials/definitions/${encodeURIComponent(credentialId)}/archive`,
    { method: "PATCH" }
  );

  return response.text();
}

export async function restoreStaffCredentialDefinition(user, credentialId) {
  if (!credentialId) {
    throw new Error("Credential selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/credentials/definitions/${encodeURIComponent(credentialId)}/restore`,
    { method: "PATCH" }
  );

  return response.text();
}

export async function awardStaffCredential(user, award) {
  const response = await fetchStaffEndpoint(user, "/api/staff/credentials/award", {
    body: JSON.stringify(award),
    headers: {
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  return response.text();
}

export async function createStaffAttendanceRecord(user, attendanceRecord) {
  const response = await fetchStaffEndpoint(user, "/api/staff/attendance", {
    body: JSON.stringify(attendanceRecord),
    headers: {
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  return response.text();
}

export async function fetchStaffAttendanceRecords(user, filters = {}) {
  const params = new URLSearchParams();
  if (filters.userUID) {
    params.set("userUID", filters.userUID);
  }
  if (filters.programID) {
    params.set("programID", filters.programID);
  }
  if (filters.eventDate) {
    params.set("eventDate", filters.eventDate);
  }

  const query = params.toString();
  const response = await fetchStaffEndpoint(user, `/api/staff/attendance${query ? `?${query}` : ""}`);
  const records = await response.json();
  return Array.isArray(records) ? records : [];
}

export async function fetchStaffAttendanceTotals(user, filters = {}) {
  const params = new URLSearchParams();
  if (filters.userUID) {
    params.set("userUID", filters.userUID);
  }
  if (filters.programID) {
    params.set("programID", filters.programID);
  }
  if (filters.eventDate) {
    params.set("eventDate", filters.eventDate);
  }

  const query = params.toString();
  const response = await fetchStaffEndpoint(user, `/api/staff/attendance/totals${query ? `?${query}` : ""}`);
  const totals = await response.json();
  return totals && typeof totals === "object" ? totals : {};
}

export async function updateStaffAttendanceRecord(user, attendanceRecordId, updates) {
  if (!attendanceRecordId) {
    throw new Error("Attendance record selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/attendance/${encodeURIComponent(attendanceRecordId)}`, {
    body: JSON.stringify(updates),
    headers: {
      "Content-Type": "application/json",
    },
    method: "PATCH",
  });

  return response.text();
}

export async function deleteStaffAttendanceRecord(user, attendanceRecordId) {
  if (!attendanceRecordId) {
    throw new Error("Attendance record selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/attendance/${encodeURIComponent(attendanceRecordId)}`, {
    method: "DELETE",
  });

  return response.text();
}

export async function createOrReviewStaffServiceHourRecord(user, serviceHourRecord) {
  const response = await fetchStaffEndpoint(user, "/api/staff/service-hours", {
    body: JSON.stringify(serviceHourRecord),
    headers: {
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  return response.text();
}

export async function fetchStaffServiceHours(user, filters = {}) {
  const params = new URLSearchParams();
  if (filters.userUID) {
    params.set("userUID", filters.userUID);
  }
  if (filters.status) {
    params.set("status", filters.status);
  }
  if (filters.programId) {
    params.set("programId", filters.programId);
  }
  if (filters.serviceDate) {
    params.set("serviceDate", filters.serviceDate);
  }

  const query = params.toString();
  const response = await fetchStaffEndpoint(user, `/api/staff/service-hours${query ? `?${query}` : ""}`);
  const serviceHours = await response.json();
  return Array.isArray(serviceHours) ? serviceHours : [];
}

export async function fetchStaffServiceHourTotals(user, filters = {}) {
  const params = new URLSearchParams();
  if (filters.userUID) {
    params.set("userUID", filters.userUID);
  }
  if (filters.status) {
    params.set("status", filters.status);
  }
  if (filters.programId) {
    params.set("programId", filters.programId);
  }
  if (filters.serviceDate) {
    params.set("serviceDate", filters.serviceDate);
  }

  const query = params.toString();
  const response = await fetchStaffEndpoint(user, `/api/staff/service-hours/totals${query ? `?${query}` : ""}`);
  const totals = await response.json();
  return totals && typeof totals === "object" ? totals : {};
}

export async function fetchStaffServiceHoursForUser(user, userUID) {
  if (!userUID) {
    throw new Error("Youth UID is required before loading service-hour records.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/service-hours/user/${encodeURIComponent(userUID)}`);
  const serviceHours = await response.json();
  return Array.isArray(serviceHours) ? serviceHours : [];
}

export async function updateStaffServiceHourStatus(user, serviceHourRecordId, verificationStatus) {
  if (!serviceHourRecordId) {
    throw new Error("Service-hour record selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/service-hours/${encodeURIComponent(serviceHourRecordId)}/status`,
    {
      body: JSON.stringify({ verificationStatus }),
      headers: {
        "Content-Type": "application/json",
      },
      method: "PATCH",
    }
  );

  return response.text();
}

export async function approveStaffServiceHour(user, serviceHourRecordId) {
  if (!serviceHourRecordId) {
    throw new Error("Service-hour record selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/service-hours/${encodeURIComponent(serviceHourRecordId)}/approve`,
    { method: "PATCH" }
  );

  return response.text();
}

export async function rejectStaffServiceHour(user, serviceHourRecordId) {
  if (!serviceHourRecordId) {
    throw new Error("Service-hour record selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/service-hours/${encodeURIComponent(serviceHourRecordId)}/reject`,
    { method: "PATCH" }
  );

  return response.text();
}

export async function deleteStaffServiceHour(user, serviceHourRecordId) {
  if (!serviceHourRecordId) {
    throw new Error("Service-hour record selection is required.");
  }

  const response = await fetchStaffEndpoint(
    user,
    `/api/staff/service-hours/${encodeURIComponent(serviceHourRecordId)}`,
    { method: "DELETE" }
  );

  return response.text();
}

export async function updateStaffServiceHourRequestUrl(user, serviceHourRequestFormUrl) {
  const response = await fetchStaffEndpoint(user, "/api/staff/settings/service-hour-request-url", {
    body: JSON.stringify({ serviceHourRequestFormUrl }),
    headers: {
      "Content-Type": "application/json",
    },
    method: "PATCH",
  });

  return response.text();
}

export async function createStaffRwdActivity(user, activity) {
  const response = await fetchStaffEndpoint(user, "/api/staff/rwd/activities", {
    body: JSON.stringify(activity),
    headers: {
      "Content-Type": "application/json",
    },
    method: "POST",
  });

  return response.text();
}

export async function updateStaffRwdActivity(user, rwdActivityId, updates) {
  if (!rwdActivityId) {
    throw new Error("RWD activity selection is required.");
  }

  const response = await fetchStaffEndpoint(user, `/api/staff/rwd/activities/${encodeURIComponent(rwdActivityId)}`, {
    body: JSON.stringify(updates),
    headers: {
      "Content-Type": "application/json",
    },
    method: "PATCH",
  });

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
    const message = await readResponseMessage(response);
    throw new ApiAccessError(
      message || "Staff/admin data is unavailable. Confirm the backend is running and try again.",
      response.status
    );
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
