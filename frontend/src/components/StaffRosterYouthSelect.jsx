import { getRecordId } from "./staffUi.jsx";

export function StaffRosterYouthSelect({
  disabled = false,
  formProgramId,
  loadingRoster = false,
  onYouthSelect,
  programRoster,
  value,
}) {
  return (
    <label>
      Youth
      <select
        disabled={disabled || !formProgramId || loadingRoster}
        required
        value={value}
        onChange={(event) => onYouthSelect(event.target.value)}
      >
        <option value="">
          {loadingRoster ? "Loading program roster..." : formProgramId ? "Select enrolled youth" : "Select a program first"}
        </option>
        {programRoster.map((entry) => (
          <option key={entry.userUID || entry.userIdentifier} value={entry.userIdentifier}>
            {getRosterOptionLabel(entry)}
          </option>
        ))}
      </select>
      <small>Roster comes from the selected active program.</small>
    </label>
  );
}

export function buildProgramRosterOptions(enrollments, youthUsers) {
  return enrollments
    .map((enrollment) => {
      const sourceIdentifier = enrollment.userUID || enrollment.userIdentifier || enrollment.email || "";
      const youth = findYouthByIdentifier(youthUsers, sourceIdentifier);
      const resolvedUserUID = youth ? getUserUid(youth) : sourceIdentifier;

      return {
        enrollmentId: enrollment.enrollmentId,
        sourceIdentifier,
        userIdentifier: resolvedUserUID,
        userUID: resolvedUserUID,
        youthName: youth ? formatYouthName(youth) : sourceIdentifier || "Unknown youth",
        youthEmail: youth?.email || enrollment.email || "",
        aspnParticipantId: youth?.aspnParticipantId || enrollment.aspnParticipantId || "",
      };
    })
    .sort((left, right) => left.youthName.localeCompare(right.youthName));
}

export function findYouthByIdentifier(youthUsers, identifier) {
  if (!identifier) {
    return null;
  }

  const normalizedIdentifier = identifier.toLowerCase();
  return youthUsers.find((youth) => {
    const uid = getUserUid(youth);
    return (
      uid === identifier ||
      youth?.aspnParticipantId?.toLowerCase() === normalizedIdentifier ||
      youth?.email?.toLowerCase() === normalizedIdentifier
    );
  }) || null;
}

export function formatYouthName(youth) {
  const name = [youth?.firstName, youth?.lastName].filter(Boolean).join(" ").trim();
  return name || youth?.displayName || youth?.email || getUserUid(youth) || "Youth";
}

export function getRosterOptionLabel(entry) {
  return [
    entry.youthName || "Unnamed youth",
    entry.youthEmail || "Email not listed",
    entry.aspnParticipantId || "No ASPN ID",
  ].join(" — ");
}

export function getUserUid(youth) {
  return getRecordId(youth, ["uid", "userUID", "userUid"]);
}
