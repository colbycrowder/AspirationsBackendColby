import { useState } from "react";

export function CredentialIcon({ credential, name, size = "default" }) {
  const [imageFailed, setImageFailed] = useState(false);
  const displayName = name || credential?.name || "ASPN Credential";

  if (!credential?.icon || imageFailed) {
    return (
      <span className={`credential-badge-initials credential-icon-${size}`} aria-hidden="true">
        {getCredentialInitials(displayName)}
      </span>
    );
  }

  return (
    <span className={`credential-icon-art credential-icon-${size}`} aria-hidden="true">
      <img
        alt=""
        src={credential.icon}
        onError={() => setImageFailed(true)}
      />
    </span>
  );
}

function getCredentialInitials(name) {
  const words = String(name || "ASPN")
    .replaceAll("&", " ")
    .split(/[^A-Za-z0-9]+/)
    .filter(Boolean);
  return words.slice(0, 2).map((word) => word.charAt(0).toUpperCase()).join("") || "A";
}
