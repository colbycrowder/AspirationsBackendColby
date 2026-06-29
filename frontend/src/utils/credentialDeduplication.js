export function getUniqueEarnedCredentials(credentials) {
  const uniqueCredentials = new Map();

  for (const credential of asArray(credentials)) {
    const key = getCredentialKey(credential);
    if (!key) {
      continue;
    }

    const existing = uniqueCredentials.get(key);
    if (!existing || isEarlierAward(credential, existing)) {
      uniqueCredentials.set(key, credential);
    }
  }

  return [...uniqueCredentials.values()];
}

export function getCredentialKey(credential) {
  if (!credential) {
    return "";
  }

  const credentialId = credential.credentialID || credential.credentialId || credential.id;
  if (credentialId) {
    return String(credentialId).trim().toLowerCase();
  }

  const credentialName = credential.credentialName || credential.name;
  return credentialName ? String(credentialName).trim().toLowerCase() : "";
}

function isEarlierAward(incoming, existing) {
  const incomingDate = parseCredentialDate(incoming?.awardedAt || incoming?.earnedAt);
  const existingDate = parseCredentialDate(existing?.awardedAt || existing?.earnedAt);

  if (!incomingDate) {
    return false;
  }

  return !existingDate || incomingDate.getTime() < existingDate.getTime();
}

function parseCredentialDate(value) {
  if (!value) {
    return null;
  }

  const normalizedValue = typeof value === "object" && value.seconds
    ? value.seconds * 1000
    : value;
  const date = new Date(normalizedValue);
  return Number.isNaN(date.getTime()) ? null : date;
}

function asArray(value) {
  return Array.isArray(value) ? value : [];
}
