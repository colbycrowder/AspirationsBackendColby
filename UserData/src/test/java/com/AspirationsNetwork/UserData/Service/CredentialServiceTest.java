package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.AwardCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.AvailableCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionCreationDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.EarnedCredentialDisplayDTO;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.CredentialRequirement;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CredentialServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createCredentialDefinitionStoresStaffProvidedDefinitionWithoutCatalogContent() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(definitionsCollection.document(any(String.class))).thenReturn(definitionDocument);
        when(definitionDocument.set(any(CredentialDefinition.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        CredentialDefinitionCreationDTO dto = new CredentialDefinitionCreationDTO();
        dto.setCredentialName("Staff provided name");
        dto.setDescription("Staff provided description");
        dto.setIcon("");
        dto.setCategory("staff-provided-category");
        dto.setActive(true);
        dto.setProgramIds(List.of("program-123"));
        CredentialRequirement requirement = new CredentialRequirement();
        requirement.setRequirementType("manual_award");
        requirement.setRequirementText("Staff review required");
        dto.setRequirements(List.of(requirement));
        dto.setRequirementText("Complete staff review.");
        dto.setAutoAwardEnabled(true);
        dto.setRequirementType("attendance_count");
        dto.setRequiredAttendanceCount(3);
        dto.setCreatedByStaffUID("staff-123");

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        String credentialID = service.createCredentialDefinition(dto);

        ArgumentCaptor<CredentialDefinition> definitionCaptor = ArgumentCaptor.forClass(CredentialDefinition.class);
        verify(definitionDocument).set(definitionCaptor.capture());

        CredentialDefinition savedDefinition = definitionCaptor.getValue();
        assertEquals(credentialID, savedDefinition.getCredentialID());
        assertEquals("Staff provided name", savedDefinition.getCredentialName());
        assertEquals("Staff provided description", savedDefinition.getDescription());
        assertEquals("default-credential", savedDefinition.getIcon());
        assertEquals("staff-provided-category", savedDefinition.getCategory());
        assertEquals(List.of("program-123"), savedDefinition.getProgramIds());
        assertEquals("manual_award", savedDefinition.getRequirements().get(0).getRequirementType());
        assertEquals("Complete staff review.", savedDefinition.getRequirementText());
        assertTrue(savedDefinition.isAutoAwardEnabled());
        assertEquals("attendance_count", savedDefinition.getRequirementType());
        assertEquals(3, savedDefinition.getRequiredAttendanceCount());
        assertEquals("staff-123", savedDefinition.getCreatedByStaffUID());
        assertTrue(savedDefinition.isActive());
    }

    @Test
    void createCredentialDefinitionRequiresStaffUid() {
        CredentialService service = new CredentialService(mock(Firestore.class), mock(NotificationService.class), mock(PlatformEventService.class));
        CredentialDefinitionCreationDTO dto = new CredentialDefinitionCreationDTO();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createCredentialDefinition(dto)
        );

        assertEquals("createdByStaffUID is required", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCredentialDefinitionsFiltersByCategoryActiveStatusAndProgram() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> definitionsFuture = mock(ApiFuture.class);
        QuerySnapshot definitionsSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot matchingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);
        CredentialDefinition matchingDefinition = credentialDefinition(
                "credential-rwd",
                "RWD",
                true,
                List.of("program-123")
        );
        CredentialDefinition inactiveDefinition = credentialDefinition(
                "credential-rwd-inactive",
                "RWD",
                false,
                List.of("program-123")
        );

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION))
                .thenReturn(definitionsCollection);
        when(definitionsCollection.get()).thenReturn(definitionsFuture);
        when(definitionsFuture.get()).thenReturn(definitionsSnapshot);
        when(definitionsSnapshot.getDocuments()).thenReturn(List.of(matchingDocument, inactiveDocument));
        when(matchingDocument.toObject(CredentialDefinition.class)).thenReturn(matchingDefinition);
        when(inactiveDocument.toObject(CredentialDefinition.class)).thenReturn(inactiveDefinition);

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        List<CredentialDefinition> definitions = service.getCredentialDefinitions("rwd", true, "program-123");

        assertEquals(1, definitions.size());
        assertEquals("credential-rwd", definitions.get(0).getCredentialID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCredentialDefinitionReturnsDefinitionDetails() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        CredentialDefinition definition = credentialDefinition("credential-123", "RWD", true, List.of("program-123"));

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION))
                .thenReturn(definitionsCollection);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionSnapshot.toObject(CredentialDefinition.class)).thenReturn(definition);

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        CredentialDefinition result = service.getCredentialDefinition("credential-123");

        assertEquals("credential-123", result.getCredentialID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateCredentialDefinitionUpdatesNullableCatalogFields() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION))
                .thenReturn(definitionsCollection);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        CredentialDefinitionUpdateDTO dto = new CredentialDefinitionUpdateDTO();
        dto.setCredentialName("Updated credential");
        dto.setIcon("");
        dto.setActive(false);
        dto.setProgramIds(List.of("program-123"));
        dto.setAutoAwardEnabled(true);

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        service.updateCredentialDefinition("credential-123", dto);

        ArgumentCaptor<Map> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(definitionDocument).update(updateCaptor.capture());
        assertEquals("Updated credential", updateCaptor.getValue().get("credentialName"));
        assertEquals("default-credential", updateCaptor.getValue().get("icon"));
        assertEquals(false, updateCaptor.getValue().get("active"));
        assertEquals(List.of("program-123"), updateCaptor.getValue().get("programIds"));
        assertEquals(true, updateCaptor.getValue().get("autoAwardEnabled"));
        assertTrue(updateCaptor.getValue().containsKey("updatedAt"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void archiveAndRestoreCredentialDefinitionsToggleActiveStatus() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> archiveFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> restoreFuture = mock(ApiFuture.class);

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION))
                .thenReturn(definitionsCollection);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionDocument.update(
                eq("active"), eq(false),
                eq("updatedAt"), any()
        )).thenReturn(archiveFuture);
        when(definitionDocument.update(
                eq("active"), eq(true),
                eq("updatedAt"), any()
        )).thenReturn(restoreFuture);
        when(archiveFuture.get()).thenReturn(mock(WriteResult.class));
        when(restoreFuture.get()).thenReturn(mock(WriteResult.class));

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        service.archiveCredentialDefinition("credential-123");
        service.restoreCredentialDefinition("credential-123");

        verify(definitionDocument).update(eq("active"), eq(false), eq("updatedAt"), any());
        verify(definitionDocument).update(eq("active"), eq(true), eq("updatedAt"), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCredentialTotalsCountsDefinitionsAndEarnedCredentialsByCategory() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> definitionsFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> earnedFuture = mock(ApiFuture.class);
        QuerySnapshot definitionsSnapshot = mock(QuerySnapshot.class);
        QuerySnapshot earnedSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot rwdDefinitionDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot archivedDefinitionDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot earnedDocument = mock(QueryDocumentSnapshot.class);
        CredentialDefinition rwdDefinition = credentialDefinition("credential-rwd", "RWD", true, List.of("program-123"));
        CredentialDefinition archivedDefinition = credentialDefinition("credential-old", "RWD", false, List.of("program-123"));
        EarnedCredential earnedCredential = new EarnedCredential();
        earnedCredential.setCredentialID("credential-rwd");

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION))
                .thenReturn(definitionsCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION))
                .thenReturn(earnedCollection);
        when(definitionsCollection.get()).thenReturn(definitionsFuture);
        when(definitionsFuture.get()).thenReturn(definitionsSnapshot);
        when(definitionsSnapshot.getDocuments()).thenReturn(List.of(rwdDefinitionDocument, archivedDefinitionDocument));
        when(rwdDefinitionDocument.toObject(CredentialDefinition.class)).thenReturn(rwdDefinition);
        when(archivedDefinitionDocument.toObject(CredentialDefinition.class)).thenReturn(archivedDefinition);
        when(earnedCollection.get()).thenReturn(earnedFuture);
        when(earnedFuture.get()).thenReturn(earnedSnapshot);
        when(earnedSnapshot.getDocuments()).thenReturn(List.of(earnedDocument));
        when(earnedDocument.toObject(EarnedCredential.class)).thenReturn(earnedCredential);

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        CredentialTotalsDTO totals = service.getCredentialTotals("RWD", "program-123");

        assertEquals(2, totals.getTotalDefinitions());
        assertEquals(1, totals.getActiveDefinitions());
        assertEquals(1, totals.getArchivedDefinitions());
        assertEquals(1, totals.getTotalEarnedCredentials());
        assertEquals(2, totals.getDefinitionsByCategory().get("RWD"));
        assertEquals(1, totals.getEarnedCredentialsByCategory().get("RWD"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void awardCredentialStoresEarnedCredentialAndAssociatesItWithUserProfile() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentReference earnedDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        Query earnedUserQuery = mock(Query.class);
        QuerySnapshot earnedLookupSnapshot = mock(QuerySnapshot.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> earnedLookupFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        NotificationService notificationService = mock(NotificationService.class);
        CredentialDefinition definition = credentialDefinition("credential-123", "Core Credential", true, List.of());

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(usersCollection.document("user-123")).thenReturn(userDocument);
        when(earnedCollection.document(any(String.class))).thenReturn(earnedDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(userDocument.get()).thenReturn(userFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(userFuture.get()).thenReturn(userSnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionSnapshot.getBoolean("active")).thenReturn(true);
        when(definitionSnapshot.toObject(CredentialDefinition.class)).thenReturn(definition);
        when(userSnapshot.exists()).thenReturn(true);
        when(earnedCollection.whereEqualTo("userUID", "user-123")).thenReturn(earnedUserQuery);
        when(earnedUserQuery.get()).thenReturn(earnedLookupFuture);
        when(earnedLookupFuture.get()).thenReturn(earnedLookupSnapshot);
        when(earnedLookupSnapshot.getDocuments()).thenReturn(List.of());
        when(earnedDocument.set(any(EarnedCredential.class))).thenReturn(writeFuture);
        when(userDocument.update(eq("earnedCredentialIds"), any())).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AwardCredentialDTO dto = new AwardCredentialDTO();
        dto.setCredentialID("credential-123");
        dto.setUserUID("user-123");
        dto.setAwardedByStaffUID("staff-123");

        CredentialService service = new CredentialService(firestore, notificationService, mock(PlatformEventService.class));
        String earnedCredentialID = service.awardCredentialToYouth(dto);

        ArgumentCaptor<EarnedCredential> earnedCaptor = ArgumentCaptor.forClass(EarnedCredential.class);
        verify(earnedDocument).set(earnedCaptor.capture());
        verify(userDocument).update(eq("earnedCredentialIds"), any());
        verify(notificationService).createCredentialEarnedNotification(
                "user-123",
                "credential-123",
                earnedCredentialID
        );

        EarnedCredential savedEarnedCredential = earnedCaptor.getValue();
        assertEquals(earnedCredentialID, savedEarnedCredential.getEarnedCredentialID());
        assertEquals("credential-123", savedEarnedCredential.getCredentialID());
        assertEquals("user-123", savedEarnedCredential.getUserUID());
        assertEquals("staff-123", savedEarnedCredential.getAwardedByStaffUID());
        assertEquals("awarded", savedEarnedCredential.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void awardCredentialResolvesAspnParticipantIdBeforeAwarding() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentReference participantLookupDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentReference earnedDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot participantLookupSnapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        Query participantQuery = mock(Query.class);
        Query earnedUserQuery = mock(Query.class);
        QuerySnapshot participantQuerySnapshot = mock(QuerySnapshot.class);
        QuerySnapshot earnedLookupSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot matchingUserDocument = mock(QueryDocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> participantLookupFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> participantQueryFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> earnedLookupFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        NotificationService notificationService = mock(NotificationService.class);
        CredentialDefinition definition = credentialDefinition("credential-123", "Core Credential", true, List.of());

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(usersCollection.document("ASPN-2026-0001")).thenReturn(participantLookupDocument);
        when(usersCollection.document("user-123")).thenReturn(userDocument);
        when(usersCollection.whereEqualTo("aspnParticipantId", "ASPN-2026-0001")).thenReturn(participantQuery);
        when(earnedCollection.document(any(String.class))).thenReturn(earnedDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(participantLookupDocument.get()).thenReturn(participantLookupFuture);
        when(userDocument.get()).thenReturn(userFuture);
        when(participantQuery.get()).thenReturn(participantQueryFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(participantLookupFuture.get()).thenReturn(participantLookupSnapshot);
        when(userFuture.get()).thenReturn(userSnapshot);
        when(participantQueryFuture.get()).thenReturn(participantQuerySnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionSnapshot.getBoolean("active")).thenReturn(true);
        when(definitionSnapshot.toObject(CredentialDefinition.class)).thenReturn(definition);
        when(participantLookupSnapshot.exists()).thenReturn(false);
        when(participantQuerySnapshot.getDocuments()).thenReturn(List.of(matchingUserDocument));
        when(matchingUserDocument.getString("uid")).thenReturn("user-123");
        when(userSnapshot.exists()).thenReturn(true);
        when(earnedCollection.whereEqualTo("userUID", "user-123")).thenReturn(earnedUserQuery);
        when(earnedUserQuery.get()).thenReturn(earnedLookupFuture);
        when(earnedLookupFuture.get()).thenReturn(earnedLookupSnapshot);
        when(earnedLookupSnapshot.getDocuments()).thenReturn(List.of());
        when(earnedDocument.set(any(EarnedCredential.class))).thenReturn(writeFuture);
        when(userDocument.update(eq("earnedCredentialIds"), any())).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AwardCredentialDTO dto = new AwardCredentialDTO();
        dto.setCredentialID("credential-123");
        dto.setUserIdentifier("ASPN-2026-0001");
        dto.setAwardedByStaffUID("staff-123");

        CredentialService service = new CredentialService(firestore, notificationService, mock(PlatformEventService.class));
        String earnedCredentialID = service.awardCredentialToYouth(dto);

        ArgumentCaptor<EarnedCredential> earnedCaptor = ArgumentCaptor.forClass(EarnedCredential.class);
        verify(earnedDocument).set(earnedCaptor.capture());
        verify(userDocument).update(eq("earnedCredentialIds"), any());
        verify(notificationService).createCredentialEarnedNotification(
                "user-123",
                "credential-123",
                earnedCredentialID
        );

        assertEquals("user-123", dto.getUserUID());
        assertEquals("user-123", earnedCaptor.getValue().getUserUID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void awardCredentialResolvesCredentialNameBeforeAwarding() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        DocumentReference credentialNameLookupDocument = mock(DocumentReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentReference earnedDocument = mock(DocumentReference.class);
        DocumentSnapshot credentialNameLookupSnapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        Query credentialNameQuery = mock(Query.class);
        Query earnedUserQuery = mock(Query.class);
        QuerySnapshot credentialNameQuerySnapshot = mock(QuerySnapshot.class);
        QuerySnapshot earnedLookupSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot matchingCredentialDocument = mock(QueryDocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> credentialNameLookupFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> credentialNameQueryFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> earnedLookupFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        NotificationService notificationService = mock(NotificationService.class);
        CredentialDefinition definition = credentialDefinition("credential-123", "Core Credential", true, List.of());
        definition.setCredentialName("Civic Research");

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(definitionsCollection.document("Civic Research")).thenReturn(credentialNameLookupDocument);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(definitionsCollection.whereEqualTo("credentialName", "Civic Research")).thenReturn(credentialNameQuery);
        when(usersCollection.document("user-123")).thenReturn(userDocument);
        when(earnedCollection.document(any(String.class))).thenReturn(earnedDocument);
        when(credentialNameLookupDocument.get()).thenReturn(credentialNameLookupFuture);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(userDocument.get()).thenReturn(userFuture);
        when(credentialNameQuery.get()).thenReturn(credentialNameQueryFuture);
        when(credentialNameLookupFuture.get()).thenReturn(credentialNameLookupSnapshot);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(userFuture.get()).thenReturn(userSnapshot);
        when(credentialNameQueryFuture.get()).thenReturn(credentialNameQuerySnapshot);
        when(credentialNameLookupSnapshot.exists()).thenReturn(false);
        when(credentialNameQuerySnapshot.getDocuments()).thenReturn(List.of(matchingCredentialDocument));
        when(matchingCredentialDocument.getString("credentialID")).thenReturn("credential-123");
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionSnapshot.getBoolean("active")).thenReturn(true);
        when(definitionSnapshot.toObject(CredentialDefinition.class)).thenReturn(definition);
        when(userSnapshot.exists()).thenReturn(true);
        when(earnedCollection.whereEqualTo("userUID", "user-123")).thenReturn(earnedUserQuery);
        when(earnedUserQuery.get()).thenReturn(earnedLookupFuture);
        when(earnedLookupFuture.get()).thenReturn(earnedLookupSnapshot);
        when(earnedLookupSnapshot.getDocuments()).thenReturn(List.of());
        when(earnedDocument.set(any(EarnedCredential.class))).thenReturn(writeFuture);
        when(userDocument.update(eq("earnedCredentialIds"), any())).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AwardCredentialDTO dto = new AwardCredentialDTO();
        dto.setCredentialIdentifier("Civic Research");
        dto.setUserUID("user-123");
        dto.setAwardedByStaffUID("staff-123");

        CredentialService service = new CredentialService(firestore, notificationService, mock(PlatformEventService.class));
        service.awardCredentialToYouth(dto);

        ArgumentCaptor<EarnedCredential> earnedCaptor = ArgumentCaptor.forClass(EarnedCredential.class);
        verify(earnedDocument).set(earnedCaptor.capture());

        assertEquals("credential-123", dto.getCredentialID());
        assertEquals("credential-123", earnedCaptor.getValue().getCredentialID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void awardCredentialBlocksDuplicateAwardForSameYouthAndCredential() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentReference earnedWriteDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        Query earnedUserQuery = mock(Query.class);
        QuerySnapshot earnedLookupSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot existingEarnedDocument = mock(QueryDocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> userFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> earnedLookupFuture = mock(ApiFuture.class);
        Date existingAwardDate = localDate(2026, 6, 24);

        CredentialDefinition definition = credentialDefinition("credential-123", "Core Credential", true, List.of());
        definition.setCredentialName("Civic Research");

        EarnedCredential existingEarnedCredential = new EarnedCredential();
        existingEarnedCredential.setEarnedCredentialID("earned-existing");
        existingEarnedCredential.setCredentialID("credential-123");
        existingEarnedCredential.setUserUID("user-123");
        existingEarnedCredential.setAwardedAt(existingAwardDate);

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(usersCollection.document("user-123")).thenReturn(userDocument);
        when(earnedCollection.document(any(String.class))).thenReturn(earnedWriteDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(userDocument.get()).thenReturn(userFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(userFuture.get()).thenReturn(userSnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionSnapshot.getBoolean("active")).thenReturn(true);
        when(definitionSnapshot.toObject(CredentialDefinition.class)).thenReturn(definition);
        when(userSnapshot.exists()).thenReturn(true);
        when(earnedCollection.whereEqualTo("userUID", "user-123")).thenReturn(earnedUserQuery);
        when(earnedUserQuery.get()).thenReturn(earnedLookupFuture);
        when(earnedLookupFuture.get()).thenReturn(earnedLookupSnapshot);
        when(earnedLookupSnapshot.getDocuments()).thenReturn(List.of(existingEarnedDocument));
        when(existingEarnedDocument.toObject(EarnedCredential.class)).thenReturn(existingEarnedCredential);

        AwardCredentialDTO dto = new AwardCredentialDTO();
        dto.setCredentialID("credential-123");
        dto.setUserUID("user-123");
        dto.setAwardedByStaffUID("staff-123");

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        DuplicateCredentialAwardException exception = assertThrows(
                DuplicateCredentialAwardException.class,
                () -> service.awardCredentialToYouth(dto)
        );

        assertEquals("This youth has already earned Civic Research on Jun 24, 2026.", exception.getMessage());
        verify(earnedWriteDocument, never()).set(any(EarnedCredential.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEarnedCredentialsForUserIncludesCredentialDefinitionDisplayDetails() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        QueryDocumentSnapshot earnedDocument = mock(QueryDocumentSnapshot.class);
        QuerySnapshot earnedSnapshot = mock(QuerySnapshot.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<QuerySnapshot> earnedFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        Date awardedAt = new Date();

        EarnedCredential earnedCredential = new EarnedCredential();
        earnedCredential.setEarnedCredentialID("earned-123");
        earnedCredential.setCredentialID("credential-123");
        earnedCredential.setUserUID("user-123");
        earnedCredential.setStatus("awarded");
        earnedCredential.setAwardedAt(awardedAt);

        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID("credential-123");
        definition.setCredentialName("Staff configured name");
        definition.setDescription("Staff configured description");
        definition.setIcon("staff-icon");
        definition.setCategory("staff-category");

        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(earnedCollection.whereEqualTo("userUID", "user-123")).thenReturn(earnedCollection);
        when(earnedCollection.get()).thenReturn(earnedFuture);
        when(earnedFuture.get()).thenReturn(earnedSnapshot);
        when(earnedSnapshot.getDocuments()).thenReturn(List.of(earnedDocument));
        when(earnedDocument.toObject(EarnedCredential.class)).thenReturn(earnedCredential);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionSnapshot.toObject(CredentialDefinition.class)).thenReturn(definition);

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        List<EarnedCredentialDisplayDTO> credentials = service.getEarnedCredentialsForUser("user-123");

        assertEquals(1, credentials.size());
        EarnedCredentialDisplayDTO credential = credentials.get(0);
        assertEquals("earned-123", credential.getEarnedCredentialID());
        assertEquals("credential-123", credential.getCredentialID());
        assertEquals("Staff configured name", credential.getCredentialName());
        assertEquals("Staff configured description", credential.getDescription());
        assertEquals("staff-icon", credential.getIcon());
        assertEquals("staff-category", credential.getCategory());
        assertEquals("awarded", credential.getStatus());
        assertEquals(awardedAt, credential.getAwardedAt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEarnedCredentialsForUserDeduplicatesHistoricalDuplicatesUsingEarliestAwardDate() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        QueryDocumentSnapshot firstEarnedDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot duplicateEarnedDocument = mock(QueryDocumentSnapshot.class);
        QuerySnapshot earnedSnapshot = mock(QuerySnapshot.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<QuerySnapshot> earnedFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        Date laterAwardDate = localDate(2026, 6, 29);
        Date earlierAwardDate = localDate(2026, 6, 24);

        EarnedCredential laterEarnedCredential = new EarnedCredential();
        laterEarnedCredential.setEarnedCredentialID("earned-later");
        laterEarnedCredential.setCredentialID("credential-123");
        laterEarnedCredential.setUserUID("user-123");
        laterEarnedCredential.setStatus("awarded");
        laterEarnedCredential.setAwardedAt(laterAwardDate);

        EarnedCredential earlierEarnedCredential = new EarnedCredential();
        earlierEarnedCredential.setEarnedCredentialID("earned-earlier");
        earlierEarnedCredential.setCredentialID("credential-123");
        earlierEarnedCredential.setUserUID("user-123");
        earlierEarnedCredential.setStatus("awarded");
        earlierEarnedCredential.setAwardedAt(earlierAwardDate);

        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID("credential-123");
        definition.setCredentialName("Civic Research");
        definition.setDescription("Staff configured description");
        definition.setIcon("staff-icon");
        definition.setCategory("Core Credential");

        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(earnedCollection.whereEqualTo("userUID", "user-123")).thenReturn(earnedCollection);
        when(earnedCollection.get()).thenReturn(earnedFuture);
        when(earnedFuture.get()).thenReturn(earnedSnapshot);
        when(earnedSnapshot.getDocuments()).thenReturn(List.of(firstEarnedDocument, duplicateEarnedDocument));
        when(firstEarnedDocument.toObject(EarnedCredential.class)).thenReturn(laterEarnedCredential);
        when(duplicateEarnedDocument.toObject(EarnedCredential.class)).thenReturn(earlierEarnedCredential);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionSnapshot.toObject(CredentialDefinition.class)).thenReturn(definition);

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        List<EarnedCredentialDisplayDTO> credentials = service.getEarnedCredentialsForUser("user-123");

        assertEquals(1, credentials.size());
        assertEquals("earned-earlier", credentials.get(0).getEarnedCredentialID());
        assertEquals("credential-123", credentials.get(0).getCredentialID());
        assertEquals("Civic Research", credentials.get(0).getCredentialName());
        assertEquals(earlierAwardDate, credentials.get(0).getAwardedAt());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAvailableCredentialsForProgramsReturnsRelevantNotYetEarnedCredentials() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        Query activeDefinitionsQuery = mock(Query.class);
        Query programDefinitionsQuery = mock(Query.class);
        QueryDocumentSnapshot earnedDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot availableDefinitionDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot earnedDefinitionDocument = mock(QueryDocumentSnapshot.class);
        QuerySnapshot earnedSnapshot = mock(QuerySnapshot.class);
        QuerySnapshot definitionSnapshot = mock(QuerySnapshot.class);
        ApiFuture<QuerySnapshot> earnedFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> definitionFuture = mock(ApiFuture.class);

        EarnedCredential earnedCredential = new EarnedCredential();
        earnedCredential.setCredentialID("credential-earned");

        CredentialDefinition availableDefinition = new CredentialDefinition();
        availableDefinition.setCredentialID("credential-available");
        availableDefinition.setCredentialName("Available Credential");
        availableDefinition.setDescription("Visible to enrolled youth");
        availableDefinition.setIcon("");
        availableDefinition.setCategory("program");
        availableDefinition.setActive(true);
        availableDefinition.setProgramIds(List.of("program-123"));
        availableDefinition.setRequirementText("Attend one session.");

        CredentialDefinition earnedDefinition = new CredentialDefinition();
        earnedDefinition.setCredentialID("credential-earned");

        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(earnedCollection.whereEqualTo("userUID", "youth-123")).thenReturn(earnedCollection);
        when(earnedCollection.get()).thenReturn(earnedFuture);
        when(earnedFuture.get()).thenReturn(earnedSnapshot);
        when(earnedSnapshot.getDocuments()).thenReturn(List.of(earnedDocument));
        when(earnedDocument.toObject(EarnedCredential.class)).thenReturn(earnedCredential);
        when(definitionsCollection.whereEqualTo("active", true)).thenReturn(activeDefinitionsQuery);
        when(activeDefinitionsQuery.whereArrayContainsAny("programIds", List.of("program-123")))
                .thenReturn(programDefinitionsQuery);
        when(programDefinitionsQuery.get()).thenReturn(definitionFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(definitionSnapshot.getDocuments()).thenReturn(List.of(availableDefinitionDocument, earnedDefinitionDocument));
        when(availableDefinitionDocument.toObject(CredentialDefinition.class)).thenReturn(availableDefinition);
        when(earnedDefinitionDocument.toObject(CredentialDefinition.class)).thenReturn(earnedDefinition);

        CredentialService service = new CredentialService(firestore, mock(NotificationService.class), mock(PlatformEventService.class));
        List<AvailableCredentialDTO> credentials = service.getAvailableCredentialsForPrograms(
                "youth-123",
                List.of("program-123")
        );

        assertEquals(1, credentials.size());
        AvailableCredentialDTO credential = credentials.get(0);
        assertEquals("credential-available", credential.getCredentialID());
        assertEquals("Available Credential", credential.getCredentialName());
        assertEquals("default-credential", credential.getIcon());
        assertEquals("Attend one session.", credential.getRequirementText());
        assertEquals("locked", credential.getStatus());
    }

    @Test
    @SuppressWarnings("unchecked")
    void evaluateAttendanceAutoAwardsCreatesEarnedCredentialWhenAttendanceCountIsMet() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        Query activeDefinitionQuery = mock(Query.class);
        Query autoAwardDefinitionQuery = mock(Query.class);
        Query requirementTypeDefinitionQuery = mock(Query.class);
        Query programDefinitionQuery = mock(Query.class);
        Query attendanceUserQuery = mock(Query.class);
        Query attendanceProgramQuery = mock(Query.class);
        Query attendancePresentQuery = mock(Query.class);
        Query earnedUserQuery = mock(Query.class);
        Query earnedCredentialQuery = mock(Query.class);
        QuerySnapshot definitionSnapshot = mock(QuerySnapshot.class);
        QuerySnapshot attendanceSnapshot = mock(QuerySnapshot.class);
        QuerySnapshot earnedLookupSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot definitionDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot attendanceDocumentOne = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot attendanceDocumentTwo = mock(QueryDocumentSnapshot.class);
        DocumentReference earnedDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        ApiFuture<QuerySnapshot> definitionFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> attendanceFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> earnedLookupFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> earnedWriteFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> userUpdateFuture = mock(ApiFuture.class);
        NotificationService notificationService = mock(NotificationService.class);

        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID("credential-123");
        definition.setActive(true);
        definition.setAutoAwardEnabled(true);
        definition.setRequirementType("attendance_count");
        definition.setRequiredAttendanceCount(2);
        definition.setProgramIds(List.of("program-123"));

        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(definitionsCollection.whereEqualTo("active", true)).thenReturn(activeDefinitionQuery);
        when(activeDefinitionQuery.whereEqualTo("autoAwardEnabled", true)).thenReturn(autoAwardDefinitionQuery);
        when(autoAwardDefinitionQuery.whereEqualTo("requirementType", "attendance_count"))
                .thenReturn(requirementTypeDefinitionQuery);
        when(requirementTypeDefinitionQuery.whereArrayContains("programIds", "program-123"))
                .thenReturn(programDefinitionQuery);
        when(programDefinitionQuery.get()).thenReturn(definitionFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(definitionSnapshot.getDocuments()).thenReturn(List.of(definitionDocument));
        when(definitionDocument.toObject(CredentialDefinition.class)).thenReturn(definition);
        when(attendanceCollection.whereEqualTo("userUID", "youth-123")).thenReturn(attendanceUserQuery);
        when(attendanceUserQuery.whereEqualTo("programID", "program-123")).thenReturn(attendanceProgramQuery);
        when(attendanceProgramQuery.whereEqualTo("attendanceStatus", "present")).thenReturn(attendancePresentQuery);
        when(attendancePresentQuery.get()).thenReturn(attendanceFuture);
        when(attendanceFuture.get()).thenReturn(attendanceSnapshot);
        when(attendanceSnapshot.getDocuments()).thenReturn(List.of(attendanceDocumentOne, attendanceDocumentTwo));
        when(earnedCollection.whereEqualTo("userUID", "youth-123")).thenReturn(earnedUserQuery);
        when(earnedUserQuery.whereEqualTo("credentialID", "credential-123")).thenReturn(earnedCredentialQuery);
        when(earnedCredentialQuery.get()).thenReturn(earnedLookupFuture);
        when(earnedLookupFuture.get()).thenReturn(earnedLookupSnapshot);
        when(earnedLookupSnapshot.getDocuments()).thenReturn(List.of());
        when(earnedCollection.document(any(String.class))).thenReturn(earnedDocument);
        when(usersCollection.document("youth-123")).thenReturn(userDocument);
        when(earnedDocument.set(any(EarnedCredential.class))).thenReturn(earnedWriteFuture);
        when(userDocument.update(eq("earnedCredentialIds"), any())).thenReturn(userUpdateFuture);
        when(earnedWriteFuture.get()).thenReturn(mock(WriteResult.class));
        when(userUpdateFuture.get()).thenReturn(mock(WriteResult.class));

        CredentialService service = new CredentialService(firestore, notificationService, mock(PlatformEventService.class));
        List<String> awardedCredentialIds = service.evaluateAttendanceAutoAwards(
                "youth-123",
                "program-123",
                "staff-123"
        );

        ArgumentCaptor<EarnedCredential> earnedCaptor = ArgumentCaptor.forClass(EarnedCredential.class);
        verify(earnedDocument).set(earnedCaptor.capture());
        verify(userDocument).update(eq("earnedCredentialIds"), any());

        assertEquals(1, awardedCredentialIds.size());
        EarnedCredential earnedCredential = earnedCaptor.getValue();
        assertEquals("credential-123", earnedCredential.getCredentialID());
        assertEquals("youth-123", earnedCredential.getUserUID());
        assertEquals("staff-123", earnedCredential.getAwardedByStaffUID());
        assertEquals("awarded", earnedCredential.getStatus());
        verify(notificationService).createCredentialEarnedNotification(
                "youth-123",
                "credential-123",
                awardedCredentialIds.get(0)
        );
    }

    private CredentialDefinition credentialDefinition(
            String credentialID,
            String category,
            boolean active,
            List<String> programIds
    ) {
        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID(credentialID);
        definition.setCredentialName("Credential " + credentialID);
        definition.setCategory(category);
        definition.setActive(active);
        definition.setProgramIds(programIds);
        return definition;
    }

    private Date localDate(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant());
    }
}
