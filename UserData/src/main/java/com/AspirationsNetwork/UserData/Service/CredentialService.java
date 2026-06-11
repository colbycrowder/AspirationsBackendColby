package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.AwardCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.AvailableCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionCreationDTO;
import com.AspirationsNetwork.UserData.DTO.EarnedCredentialDisplayDTO;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CredentialService {
    public static final String CREDENTIAL_DEFINITIONS_COLLECTION = "credentialDefinitions";
    public static final String EARNED_CREDENTIALS_COLLECTION = "earnedCredentials";
    private static final String ATTENDANCE_COUNT_REQUIREMENT = "attendance_count";

    private final Firestore firestore;
    private final NotificationService notificationService;

    public String createCredentialDefinition(CredentialDefinitionCreationDTO dto) throws Exception {
        requireText(dto.getCreatedByStaffUID(), "createdByStaffUID is required");

        String credentialID = UUID.randomUUID().toString();
        Date now = new Date();

        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID(credentialID);
        definition.setCredentialName(dto.getCredentialName());
        definition.setDescription(dto.getDescription());
        definition.setIcon(normalizeIcon(dto.getIcon()));
        definition.setCategory(dto.getCategory());
        definition.setActive(dto.isActive());
        definition.setProgramIds(dto.getProgramIds() == null ? new ArrayList<>() : dto.getProgramIds());
        definition.setRequirements(dto.getRequirements() == null ? new ArrayList<>() : dto.getRequirements());
        definition.setRequirementText(dto.getRequirementText());
        definition.setAutoAwardEnabled(dto.isAutoAwardEnabled());
        definition.setRequirementType(dto.getRequirementType());
        definition.setRequiredAttendanceCount(dto.getRequiredAttendanceCount());
        definition.setCreatedByStaffUID(dto.getCreatedByStaffUID());
        definition.setCreatedAt(now);
        definition.setUpdatedAt(now);

        firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                .document(credentialID)
                .set(definition)
                .get();

        return credentialID;
    }

    public String awardCredentialToYouth(AwardCredentialDTO dto) throws Exception {
        requireText(dto.getAwardedByStaffUID(), "awardedByStaffUID is required");
        requireText(dto.getCredentialID(), "credentialID is required");
        requireText(dto.getUserUID(), "userUID is required");

        DocumentSnapshot credentialDoc = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                .document(dto.getCredentialID())
                .get()
                .get();

        if (!credentialDoc.exists()) {
            throw new IllegalArgumentException("Credential definition does not exist");
        }

        Boolean active = credentialDoc.getBoolean("active");
        if (!Boolean.TRUE.equals(active)) {
            throw new IllegalArgumentException("Credential definition is not active");
        }

        DocumentReference userRef = firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(dto.getUserUID());
        DocumentSnapshot userDoc = userRef.get().get();

        if (!userDoc.exists()) {
            throw new IllegalArgumentException("User profile does not exist");
        }

        String earnedCredentialID = UUID.randomUUID().toString();
        Date now = new Date();

        EarnedCredential earnedCredential = new EarnedCredential();
        earnedCredential.setEarnedCredentialID(earnedCredentialID);
        earnedCredential.setCredentialID(dto.getCredentialID());
        earnedCredential.setUserUID(dto.getUserUID());
        earnedCredential.setAwardedByStaffUID(dto.getAwardedByStaffUID());
        earnedCredential.setStatus("awarded");
        earnedCredential.setEarnedAt(now);
        earnedCredential.setAwardedAt(now);
        earnedCredential.setUpdatedAt(now);

        firestore.collection(EARNED_CREDENTIALS_COLLECTION)
                .document(earnedCredentialID)
                .set(earnedCredential)
                .get();

        userRef.update("earnedCredentialIds", FieldValue.arrayUnion(earnedCredentialID)).get();
        createCredentialEarnedNotificationSafely(dto.getUserUID(), dto.getCredentialID(), earnedCredentialID);

        return earnedCredentialID;
    }

    public List<String> evaluateAttendanceAutoAwards(
            String userUID,
            String programID,
            String awardedByStaffUID
    ) throws Exception {
        requireText(userUID, "userUID is required");
        requireText(programID, "programID is required");
        requireText(awardedByStaffUID, "awardedByStaffUID is required");

        int presentAttendanceCount = getPresentAttendanceCount(userUID, programID);
        List<String> awardedCredentialIds = new ArrayList<>();

        for (CredentialDefinition definition : getAttendanceAutoAwardDefinitions(programID)) {
            Integer requiredAttendanceCount = definition.getRequiredAttendanceCount();
            if (requiredAttendanceCount == null || requiredAttendanceCount <= 0) {
                continue;
            }

            if (presentAttendanceCount < requiredAttendanceCount) {
                continue;
            }

            if (hasEarnedCredential(userUID, definition.getCredentialID())) {
                continue;
            }

            awardedCredentialIds.add(createEarnedCredentialRecord(
                    userUID,
                    definition.getCredentialID(),
                    awardedByStaffUID
            ));
        }

        return awardedCredentialIds;
    }

    public String awardLinkedCredentialIfNotEarned(
            String userUID,
            String credentialID,
            String awardedByUID
    ) throws Exception {
        requireText(userUID, "userUID is required");
        requireText(credentialID, "credentialID is required");
        requireText(awardedByUID, "awardedByUID is required");

        if (hasEarnedCredential(userUID, credentialID)) {
            return null;
        }

        return createEarnedCredentialRecord(userUID, credentialID, awardedByUID);
    }

    public List<EarnedCredentialDisplayDTO> getEarnedCredentialsForUser(String userUID) throws Exception {
        requireText(userUID, "userUID is required");

        ApiFuture<QuerySnapshot> future = firestore.collection(EARNED_CREDENTIALS_COLLECTION)
                .whereEqualTo("userUID", userUID)
                .get();

        List<QueryDocumentSnapshot> earnedCredentialDocuments = future.get().getDocuments();
        List<EarnedCredentialDisplayDTO> earnedCredentials = new ArrayList<>();

        for (QueryDocumentSnapshot earnedCredentialDocument : earnedCredentialDocuments) {
            EarnedCredential earnedCredential = earnedCredentialDocument.toObject(EarnedCredential.class);
            DocumentSnapshot definitionDocument = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                    .document(earnedCredential.getCredentialID())
                    .get()
                    .get();

            if (!definitionDocument.exists()) {
                continue;
            }

            CredentialDefinition definition = definitionDocument.toObject(CredentialDefinition.class);
            if (definition == null) {
                continue;
            }

            earnedCredentials.add(toDisplayDTO(earnedCredential, definition));
        }

        return earnedCredentials;
    }

    public List<AvailableCredentialDTO> getAvailableCredentialsForPrograms(
            String userUID,
            List<String> enrolledProgramIds
    ) throws Exception {
        requireText(userUID, "userUID is required");

        if (enrolledProgramIds == null || enrolledProgramIds.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> earnedCredentialIds = getEarnedCredentialDefinitionIds(userUID);
        ApiFuture<QuerySnapshot> future = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                .whereEqualTo("active", true)
                .whereArrayContainsAny("programIds", limitToFirestoreArrayQuerySize(enrolledProgramIds))
                .get();

        List<AvailableCredentialDTO> availableCredentials = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            CredentialDefinition definition = document.toObject(CredentialDefinition.class);
            if (definition == null || earnedCredentialIds.contains(definition.getCredentialID())) {
                continue;
            }

            availableCredentials.add(toAvailableDTO(definition));
        }

        return availableCredentials;
    }

    private EarnedCredentialDisplayDTO toDisplayDTO(
            EarnedCredential earnedCredential,
            CredentialDefinition definition
    ) {
        EarnedCredentialDisplayDTO dto = new EarnedCredentialDisplayDTO();
        dto.setEarnedCredentialID(earnedCredential.getEarnedCredentialID());
        dto.setCredentialID(earnedCredential.getCredentialID());
        dto.setCredentialName(definition.getCredentialName());
        dto.setDescription(definition.getDescription());
        dto.setIcon(normalizeIcon(definition.getIcon()));
        dto.setCategory(definition.getCategory());
        dto.setStatus(earnedCredential.getStatus());
        dto.setEarnedAt(earnedCredential.getEarnedAt());
        dto.setAwardedAt(earnedCredential.getAwardedAt());
        return dto;
    }

    private Set<String> getEarnedCredentialDefinitionIds(String userUID) throws Exception {
        ApiFuture<QuerySnapshot> future = firestore.collection(EARNED_CREDENTIALS_COLLECTION)
                .whereEqualTo("userUID", userUID)
                .get();

        Set<String> earnedCredentialIds = new HashSet<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            EarnedCredential earnedCredential = document.toObject(EarnedCredential.class);
            if (earnedCredential != null && earnedCredential.getCredentialID() != null) {
                earnedCredentialIds.add(earnedCredential.getCredentialID());
            }
        }
        return earnedCredentialIds;
    }

    private List<CredentialDefinition> getAttendanceAutoAwardDefinitions(String programID) throws Exception {
        Query query = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                .whereEqualTo("active", true)
                .whereEqualTo("autoAwardEnabled", true)
                .whereEqualTo("requirementType", ATTENDANCE_COUNT_REQUIREMENT)
                .whereArrayContains("programIds", programID);

        ApiFuture<QuerySnapshot> future = query.get();
        List<CredentialDefinition> definitions = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            CredentialDefinition definition = document.toObject(CredentialDefinition.class);
            if (definition != null) {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    private int getPresentAttendanceCount(String userUID, String programID) throws Exception {
        Query query = firestore.collection(AttendanceService.COLLECTION_NAME)
                .whereEqualTo("userUID", userUID)
                .whereEqualTo("programID", programID)
                .whereEqualTo("attendanceStatus", "present");

        return query.get().get().getDocuments().size();
    }

    private boolean hasEarnedCredential(String userUID, String credentialID) throws Exception {
        Query query = firestore.collection(EARNED_CREDENTIALS_COLLECTION)
                .whereEqualTo("userUID", userUID)
                .whereEqualTo("credentialID", credentialID);

        return !query.get().get().getDocuments().isEmpty();
    }

    private String createEarnedCredentialRecord(
            String userUID,
            String credentialID,
            String awardedByStaffUID
    ) throws Exception {
        String earnedCredentialID = UUID.randomUUID().toString();
        Date now = new Date();

        EarnedCredential earnedCredential = new EarnedCredential();
        earnedCredential.setEarnedCredentialID(earnedCredentialID);
        earnedCredential.setCredentialID(credentialID);
        earnedCredential.setUserUID(userUID);
        earnedCredential.setAwardedByStaffUID(awardedByStaffUID);
        earnedCredential.setStatus("awarded");
        earnedCredential.setEarnedAt(now);
        earnedCredential.setAwardedAt(now);
        earnedCredential.setUpdatedAt(now);

        firestore.collection(EARNED_CREDENTIALS_COLLECTION)
                .document(earnedCredentialID)
                .set(earnedCredential)
                .get();

        firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(userUID)
                .update("earnedCredentialIds", FieldValue.arrayUnion(earnedCredentialID))
                .get();

        createCredentialEarnedNotificationSafely(userUID, credentialID, earnedCredentialID);

        return earnedCredentialID;
    }

    private void createCredentialEarnedNotificationSafely(
            String userUID,
            String credentialID,
            String earnedCredentialID
    ) {
        try {
            notificationService.createCredentialEarnedNotification(userUID, credentialID, earnedCredentialID);
        } catch (Exception e) {
            System.err.println("Credential earned notification failed: " + e.getMessage());
        }
    }

    private AvailableCredentialDTO toAvailableDTO(CredentialDefinition definition) {
        AvailableCredentialDTO dto = new AvailableCredentialDTO();
        dto.setCredentialID(definition.getCredentialID());
        dto.setCredentialName(definition.getCredentialName());
        dto.setDescription(definition.getDescription());
        dto.setIcon(normalizeIcon(definition.getIcon()));
        dto.setCategory(definition.getCategory());
        dto.setActive(definition.isActive());
        dto.setProgramIds(definition.getProgramIds() == null ? new ArrayList<>() : definition.getProgramIds());
        dto.setRequirements(definition.getRequirements() == null ? new ArrayList<>() : definition.getRequirements());
        dto.setRequirementText(normalizeRequirementText(definition.getRequirementText()));
        dto.setStatus("locked");
        return dto;
    }

    private List<String> limitToFirestoreArrayQuerySize(List<String> values) {
        return values.size() <= 10 ? values : values.subList(0, 10);
    }

    private String normalizeIcon(String icon) {
        if (icon == null || icon.isBlank()) {
            return "default-credential";
        }
        return icon;
    }

    private String normalizeRequirementText(String requirementText) {
        if (requirementText == null || requirementText.isBlank()) {
            return "Complete the listed requirements to earn this credential.";
        }
        return requirementText;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
