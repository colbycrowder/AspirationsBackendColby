package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.AwardCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.AvailableCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionCreationDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.EarnedCredentialDisplayDTO;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.AspirationsNetwork.UserData.Models.PlatformEventType;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final PlatformEventService platformEventService;

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

    public List<CredentialDefinition> getCredentialDefinitions(String category, Boolean active, String programId)
            throws Exception {
        List<CredentialDefinition> definitions = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            CredentialDefinition definition = document.toObject(CredentialDefinition.class);
            if (definition != null && matchesDefinitionFilters(definition, category, active, programId)) {
                definitions.add(definition);
            }
        }
        return definitions;
    }

    public CredentialDefinition getCredentialDefinition(String credentialID) throws Exception {
        requireText(credentialID, "credentialID is required");

        DocumentSnapshot document = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                .document(credentialID)
                .get()
                .get();
        if (!document.exists()) {
            return null;
        }
        return document.toObject(CredentialDefinition.class);
    }

    public void updateCredentialDefinition(String credentialID, CredentialDefinitionUpdateDTO dto) throws Exception {
        requireText(credentialID, "credentialID is required");
        if (dto == null) {
            throw new IllegalArgumentException("credential definition update request is required");
        }

        DocumentReference definitionRef = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION).document(credentialID);
        if (!definitionRef.get().get().exists()) {
            throw new IllegalArgumentException("Credential definition does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        addIfPresent(updates, "credentialName", dto.getCredentialName());
        addIfPresent(updates, "description", dto.getDescription());
        if (dto.getIcon() != null) {
            updates.put("icon", normalizeIcon(dto.getIcon()));
        }
        addIfPresent(updates, "category", dto.getCategory());
        if (dto.getActive() != null) {
            updates.put("active", dto.getActive());
        }
        if (dto.getProgramIds() != null) {
            updates.put("programIds", dto.getProgramIds());
        }
        if (dto.getRequirements() != null) {
            updates.put("requirements", dto.getRequirements());
        }
        addIfPresent(updates, "requirementText", dto.getRequirementText());
        if (dto.getAutoAwardEnabled() != null) {
            updates.put("autoAwardEnabled", dto.getAutoAwardEnabled());
        }
        addIfPresent(updates, "requirementType", dto.getRequirementType());
        if (dto.getRequiredAttendanceCount() != null) {
            updates.put("requiredAttendanceCount", dto.getRequiredAttendanceCount());
        }
        updates.put("updatedAt", new Date());

        definitionRef.update(updates).get();
    }

    public void archiveCredentialDefinition(String credentialID) throws Exception {
        setCredentialDefinitionActive(credentialID, false);
    }

    public void restoreCredentialDefinition(String credentialID) throws Exception {
        setCredentialDefinitionActive(credentialID, true);
    }

    public CredentialTotalsDTO getCredentialTotals(String category, String programId) throws Exception {
        List<CredentialDefinition> definitions = getCredentialDefinitions(category, null, programId);
        Map<String, CredentialDefinition> definitionsById = new HashMap<>();
        CredentialTotalsDTO totals = new CredentialTotalsDTO();

        for (CredentialDefinition definition : definitions) {
            totals.setTotalDefinitions(totals.getTotalDefinitions() + 1);
            if (definition.isActive()) {
                totals.setActiveDefinitions(totals.getActiveDefinitions() + 1);
            } else {
                totals.setArchivedDefinitions(totals.getArchivedDefinitions() + 1);
            }
            definitionsById.put(definition.getCredentialID(), definition);
            String categoryName = hasText(definition.getCategory()) ? definition.getCategory() : "Uncategorized";
            totals.getDefinitionsByCategory().merge(categoryName, 1, Integer::sum);
        }

        ApiFuture<QuerySnapshot> future = firestore.collection(EARNED_CREDENTIALS_COLLECTION).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            EarnedCredential earnedCredential = document.toObject(EarnedCredential.class);
            if (earnedCredential == null) {
                continue;
            }
            CredentialDefinition definition = definitionsById.get(earnedCredential.getCredentialID());
            if (definition == null) {
                continue;
            }
            totals.setTotalEarnedCredentials(totals.getTotalEarnedCredentials() + 1);
            String categoryName = hasText(definition.getCategory()) ? definition.getCategory() : "Uncategorized";
            totals.getEarnedCredentialsByCategory().merge(categoryName, 1, Integer::sum);
        }

        return totals;
    }

    public String awardCredentialToYouth(AwardCredentialDTO dto) throws Exception {
        requireText(dto.getAwardedByStaffUID(), "awardedByStaffUID is required");
        String resolvedCredentialID = resolveCredentialID(dto);
        dto.setCredentialID(resolvedCredentialID);
        String resolvedUserUID = resolveYouthUserUID(dto);
        dto.setUserUID(resolvedUserUID);

        DocumentSnapshot credentialDoc = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                .document(resolvedCredentialID)
                .get()
                .get();

        if (!credentialDoc.exists()) {
            throw new IllegalArgumentException("Credential definition does not exist");
        }

        CredentialDefinition credentialDefinition = credentialDoc.toObject(CredentialDefinition.class);
        if (credentialDefinition == null) {
            throw new IllegalArgumentException("Credential definition does not exist");
        }

        Boolean active = credentialDoc.getBoolean("active");
        if (!Boolean.TRUE.equals(active)) {
            throw new IllegalArgumentException("Credential definition is not active");
        }

        DocumentReference userRef = firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(resolvedUserUID);
        DocumentSnapshot userDoc = userRef.get().get();

        if (!userDoc.exists()) {
            throw new IllegalArgumentException("User profile does not exist");
        }

        EarnedCredential existingCredential = findExistingCredentialAward(
                resolvedUserUID,
                resolvedCredentialID,
                credentialDefinition.getCredentialName()
        );
        if (existingCredential != null) {
            String credentialName = hasText(credentialDefinition.getCredentialName())
                    ? credentialDefinition.getCredentialName()
                    : "this credential";
            throw new DuplicateCredentialAwardException(
                    "This youth has already earned "
                            + credentialName
                            + " on "
                            + formatAwardDate(existingCredential)
                            + "."
            );
        }

        String earnedCredentialID = UUID.randomUUID().toString();
        Date now = new Date();

        EarnedCredential earnedCredential = new EarnedCredential();
        earnedCredential.setEarnedCredentialID(earnedCredentialID);
        earnedCredential.setCredentialID(resolvedCredentialID);
        earnedCredential.setUserUID(resolvedUserUID);
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
        createCredentialEarnedNotificationSafely(resolvedUserUID, resolvedCredentialID, earnedCredentialID);
        trackCredentialEarnedEventSafely(resolvedUserUID, resolvedCredentialID, earnedCredentialID, "manual_award");

        return earnedCredentialID;
    }

    private String resolveCredentialID(AwardCredentialDTO dto) throws Exception {
        String identifier = firstText(dto.getCredentialID(), dto.getCredentialIdentifier());
        requireText(identifier, "credential identifier is required");

        CollectionReference definitionsCollection = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION);
        DocumentSnapshot directCredentialDocument = definitionsCollection.document(identifier).get().get();
        if (directCredentialDocument.exists()) {
            return identifier;
        }

        QuerySnapshot nameQuery = definitionsCollection
                .whereEqualTo("credentialName", identifier)
                .get()
                .get();
        List<QueryDocumentSnapshot> matchingDefinitions = nameQuery.getDocuments();

        if (matchingDefinitions.isEmpty()) {
            throw new IllegalArgumentException("Credential definition does not exist");
        }

        if (matchingDefinitions.size() > 1) {
            throw new IllegalArgumentException("Multiple credential definitions match the provided credential name");
        }

        QueryDocumentSnapshot matchingDefinition = matchingDefinitions.get(0);
        String resolvedCredentialID = matchingDefinition.getString("credentialID");
        return hasText(resolvedCredentialID) ? resolvedCredentialID : matchingDefinition.getId();
    }

    private String resolveYouthUserUID(AwardCredentialDTO dto) throws Exception {
        String identifier = firstText(dto.getUserIdentifier(), dto.getUserUID());
        requireText(identifier, "youth identifier is required");

        CollectionReference usersCollection = firestore.collection(UserInfoService.COLLECTION_NAME);
        DocumentSnapshot directUserDocument = usersCollection.document(identifier).get().get();
        if (directUserDocument.exists()) {
            return identifier;
        }

        QuerySnapshot participantQuery = usersCollection
                .whereEqualTo("aspnParticipantId", identifier)
                .get()
                .get();
        List<QueryDocumentSnapshot> matchingUsers = participantQuery.getDocuments();

        if (matchingUsers.isEmpty()) {
            throw new IllegalArgumentException("Youth profile does not exist for the provided identifier");
        }

        if (matchingUsers.size() > 1) {
            throw new IllegalArgumentException("Multiple youth profiles match the provided ASPN Participant ID");
        }

        QueryDocumentSnapshot matchingUser = matchingUsers.get(0);
        String resolvedUserUID = matchingUser.getString("uid");
        return hasText(resolvedUserUID) ? resolvedUserUID : matchingUser.getId();
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
            String credentialDefinitionId = getEarnedCredentialDefinitionId(earnedCredential);
            if (!hasText(credentialDefinitionId)) {
                continue;
            }

            DocumentSnapshot definitionDocument = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                    .document(credentialDefinitionId)
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

        return deduplicateEarnedCredentials(earnedCredentials);
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
        dto.setCredentialID(getEarnedCredentialDefinitionId(earnedCredential));
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
            String credentialDefinitionId = getEarnedCredentialDefinitionId(earnedCredential);
            if (hasText(credentialDefinitionId)) {
                earnedCredentialIds.add(credentialDefinitionId);
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

    private EarnedCredential findExistingCredentialAward(
            String userUID,
            String credentialID,
            String credentialName
    ) throws Exception {
        ApiFuture<QuerySnapshot> future = firestore.collection(EARNED_CREDENTIALS_COLLECTION)
                .whereEqualTo("userUID", userUID)
                .get();

        EarnedCredential earliestMatch = null;
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            EarnedCredential earnedCredential = document.toObject(EarnedCredential.class);
            if (earnedCredential == null) {
                continue;
            }

            if (!matchesCredentialAward(earnedCredential, credentialID, credentialName)) {
                continue;
            }

            if (earliestMatch == null || isEarlierCredentialDate(earnedCredential, earliestMatch)) {
                earliestMatch = earnedCredential;
            }
        }

        return earliestMatch;
    }

    private boolean matchesCredentialAward(
            EarnedCredential earnedCredential,
            String credentialID,
            String credentialName
    ) throws Exception {
        String earnedCredentialDefinitionId = getEarnedCredentialDefinitionId(earnedCredential);
        if (hasText(earnedCredentialDefinitionId) && earnedCredentialDefinitionId.equals(credentialID)) {
            return true;
        }

        String earnedCredentialName = firstText(earnedCredential.getCredentialName(), earnedCredential.getName());
        if (hasText(credentialName) && hasText(earnedCredentialName)
                && normalizeCredentialKey(earnedCredentialName).equals(normalizeCredentialKey(credentialName))) {
            return true;
        }

        if (!hasText(credentialName) || !hasText(earnedCredentialDefinitionId)) {
            return false;
        }

        DocumentSnapshot existingDefinitionDocument = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION)
                .document(earnedCredentialDefinitionId)
                .get()
                .get();
        if (!existingDefinitionDocument.exists()) {
            return false;
        }

        CredentialDefinition existingDefinition = existingDefinitionDocument.toObject(CredentialDefinition.class);
        return existingDefinition != null
                && normalizeCredentialKey(existingDefinition.getCredentialName()).equals(normalizeCredentialKey(credentialName));
    }

    private List<EarnedCredentialDisplayDTO> deduplicateEarnedCredentials(
            List<EarnedCredentialDisplayDTO> earnedCredentials
    ) {
        Map<String, EarnedCredentialDisplayDTO> uniqueCredentials = new LinkedHashMap<>();
        for (EarnedCredentialDisplayDTO credential : earnedCredentials) {
            String key = getCredentialDisplayKey(credential);
            if (!hasText(key)) {
                key = credential.getEarnedCredentialID();
            }

            EarnedCredentialDisplayDTO current = uniqueCredentials.get(key);
            if (current == null || isEarlierCredentialDate(credential, current)) {
                uniqueCredentials.put(key, credential);
            }
        }

        return new ArrayList<>(uniqueCredentials.values());
    }

    private String getCredentialDisplayKey(EarnedCredentialDisplayDTO credential) {
        if (credential == null) {
            return "";
        }
        if (hasText(credential.getCredentialID())) {
            return credential.getCredentialID();
        }
        return normalizeCredentialKey(credential.getCredentialName());
    }

    private boolean isEarlierCredentialDate(EarnedCredential incoming, EarnedCredential current) {
        Date incomingDate = firstDate(incoming.getAwardedAt(), incoming.getEarnedAt());
        Date currentDate = firstDate(current.getAwardedAt(), current.getEarnedAt());
        return isEarlierDate(incomingDate, currentDate);
    }

    private boolean isEarlierCredentialDate(EarnedCredentialDisplayDTO incoming, EarnedCredentialDisplayDTO current) {
        Date incomingDate = firstDate(incoming.getAwardedAt(), incoming.getEarnedAt());
        Date currentDate = firstDate(current.getAwardedAt(), current.getEarnedAt());
        return isEarlierDate(incomingDate, currentDate);
    }

    private Date firstDate(Date primary, Date fallback) {
        return primary != null ? primary : fallback;
    }

    private boolean isEarlierDate(Date incomingDate, Date currentDate) {
        if (incomingDate == null) {
            return false;
        }
        return currentDate == null || incomingDate.before(currentDate);
    }

    private String formatAwardDate(EarnedCredential earnedCredential) {
        Date awardDate = firstDate(earnedCredential.getAwardedAt(), earnedCredential.getEarnedAt());
        if (awardDate == null) {
            return "an earlier date";
        }
        return new SimpleDateFormat("MMM d, yyyy", Locale.US).format(awardDate);
    }

    private String normalizeCredentialKey(String value) {
        return hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String getEarnedCredentialDefinitionId(EarnedCredential earnedCredential) {
        if (earnedCredential == null) {
            return "";
        }
        return firstText(
                earnedCredential.getCredentialID(),
                firstText(earnedCredential.getCredentialId(), earnedCredential.getId())
        );
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
        trackCredentialEarnedEventSafely(userUID, credentialID, earnedCredentialID, "system_award");

        return earnedCredentialID;
    }

    private void trackCredentialEarnedEventSafely(
            String userUID,
            String credentialID,
            String earnedCredentialID,
            String credentialType
    ) {
        platformEventService.trackEventSafely(
                userUID,
                PlatformEventType.CREDENTIAL_EARNED,
                Map.of(
                        "credentialId", credentialID,
                        "earnedCredentialId", earnedCredentialID,
                        "credentialType", credentialType
                )
        );
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

    private boolean matchesDefinitionFilters(
            CredentialDefinition definition,
            String category,
            Boolean active,
            String programId
    ) {
        if (hasText(category) && !category.equalsIgnoreCase(definition.getCategory())) {
            return false;
        }
        if (active != null && active != definition.isActive()) {
            return false;
        }
        return !hasText(programId)
                || (definition.getProgramIds() != null && definition.getProgramIds().contains(programId));
    }

    private void setCredentialDefinitionActive(String credentialID, boolean active) throws Exception {
        requireText(credentialID, "credentialID is required");

        DocumentReference definitionRef = firestore.collection(CREDENTIAL_DEFINITIONS_COLLECTION).document(credentialID);
        if (!definitionRef.get().get().exists()) {
            throw new IllegalArgumentException("Credential definition does not exist");
        }

        definitionRef.update(
                "active", active,
                "updatedAt", new Date()
        ).get();
    }

    private void addIfPresent(Map<String, Object> updates, String fieldName, String value) {
        if (value != null) {
            updates.put(fieldName, value);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String firstText(String primary, String fallback) {
        return hasText(primary) ? primary.trim() : fallback == null ? "" : fallback.trim();
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
        if (!hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }
}
