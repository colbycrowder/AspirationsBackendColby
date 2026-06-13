package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.GovernmentOrganizationDTO;
import com.AspirationsNetwork.UserData.DTO.GovernmentOrganizationTotalsDTO;
import com.AspirationsNetwork.UserData.Models.GovernmentOrganization;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GovernmentOrganizationServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createGovernmentOrganizationStoresOrganizationWithDefaults() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(GovernmentOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document(any(String.class))).thenReturn(document);
        when(document.set(any(GovernmentOrganization.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        GovernmentOrganizationDTO dto = governmentDto("City of St. Louis", "municipal", "city_government");

        GovernmentOrganizationService service = new GovernmentOrganizationService(firestore);
        String organizationId = service.createGovernmentOrganization(dto);

        ArgumentCaptor<GovernmentOrganization> captor = ArgumentCaptor.forClass(GovernmentOrganization.class);
        verify(document).set(captor.capture());

        GovernmentOrganization savedOrganization = captor.getValue();
        assertEquals(organizationId, savedOrganization.getGovernmentOrganizationId());
        assertEquals("City of St. Louis", savedOrganization.getOrganizationName());
        assertEquals("municipal", savedOrganization.getGovernmentLevel());
        assertEquals("city_government", savedOrganization.getOrganizationType());
        assertTrue(savedOrganization.isActive());
        assertTrue(savedOrganization.isWorkforcePartner());
        assertFalse(savedOrganization.isCredentialPartner());
    }

    @Test
    void createGovernmentOrganizationRejectsInvalidGovernmentLevel() {
        GovernmentOrganizationDTO dto = governmentDto("City of St. Louis", "local", "city_government");

        GovernmentOrganizationService service = new GovernmentOrganizationService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createGovernmentOrganization(dto)
        );

        assertEquals("governmentLevel is invalid", exception.getMessage());
    }

    @Test
    void createGovernmentOrganizationRejectsInvalidOrganizationType() {
        GovernmentOrganizationDTO dto = governmentDto("City of St. Louis", "municipal", "municipality");

        GovernmentOrganizationService service = new GovernmentOrganizationService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createGovernmentOrganization(dto)
        );

        assertEquals("organizationType is invalid", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGovernmentOrganizationsFiltersByLevelTypeStatusPartnerFlagsAndName() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        QueryDocumentSnapshot matchingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot wrongTypeDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(GovernmentOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        stubCollection(collection, List.of(matchingDocument, inactiveDocument, wrongTypeDocument));
        when(matchingDocument.toObject(GovernmentOrganization.class))
                .thenReturn(government("government-1", true, "municipal", "city_government", true, false, "City of St. Louis"));
        when(inactiveDocument.toObject(GovernmentOrganization.class))
                .thenReturn(government("government-2", false, "municipal", "city_government", true, false, "City of St. Louis"));
        when(wrongTypeDocument.toObject(GovernmentOrganization.class))
                .thenReturn(government("government-3", true, "state", "state_agency", true, false, "State Agency"));

        GovernmentOrganizationService service = new GovernmentOrganizationService(firestore);
        List<GovernmentOrganization> organizations = service.getGovernmentOrganizations(
                "municipal",
                "city_government",
                true,
                true,
                false,
                "St. Louis"
        );

        assertEquals(1, organizations.size());
        assertEquals("government-1", organizations.get(0).getGovernmentOrganizationId());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void updateGovernmentOrganizationUpdatesAllowedFields() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(GovernmentOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("government-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(GovernmentOrganization.class))
                .thenReturn(government("government-123", true, "municipal", "city_government", true, false, "City of St. Louis"));
        when(document.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        GovernmentOrganizationDTO dto = new GovernmentOrganizationDTO();
        dto.setGovernmentLevel("state");
        dto.setOrganizationType("state_agency");
        dto.setWorkforcePartner(false);
        dto.setCredentialPartner(true);

        GovernmentOrganizationService service = new GovernmentOrganizationService(firestore);
        service.updateGovernmentOrganization("government-123", dto);

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(document).update(captor.capture());

        Map<String, Object> updates = captor.getValue();
        assertEquals("state", updates.get("governmentLevel"));
        assertEquals("state_agency", updates.get("organizationType"));
        assertEquals(false, updates.get("workforcePartner"));
        assertEquals(true, updates.get("credentialPartner"));
        assertFalse(updates.containsKey("governmentOrganizationId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void activateAndDeactivateGovernmentOrganizationToggleActiveStatus() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> activateFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> deactivateFuture = mock(ApiFuture.class);

        when(firestore.collection(GovernmentOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("government-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(GovernmentOrganization.class))
                .thenReturn(government("government-123", true, "municipal", "city_government", true, false, "City of St. Louis"));
        when(document.update(
                any(String.class), any(Boolean.class),
                any(String.class), any(Object.class)
        )).thenReturn(activateFuture, deactivateFuture);
        when(activateFuture.get()).thenReturn(mock(WriteResult.class));
        when(deactivateFuture.get()).thenReturn(mock(WriteResult.class));

        GovernmentOrganizationService service = new GovernmentOrganizationService(firestore);
        service.activateGovernmentOrganization("government-123");
        service.deactivateGovernmentOrganization("government-123");

        verify(document).update(
                eq("active"), eq(true),
                eq("updatedAt"), any(java.util.Date.class)
        );
        verify(document).update(
                eq("active"), eq(false),
                eq("updatedAt"), any(java.util.Date.class)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGovernmentOrganizationTotalsCalculatesCountsAndGroupedTotals() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        QueryDocumentSnapshot activeDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(GovernmentOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        stubCollection(collection, List.of(activeDocument, inactiveDocument));
        when(activeDocument.toObject(GovernmentOrganization.class))
                .thenReturn(government("government-1", true, "municipal", "city_government", true, false, "City of St. Louis"));
        when(inactiveDocument.toObject(GovernmentOrganization.class))
                .thenReturn(government("government-2", false, "state", "state_agency", false, true, "State Agency"));

        GovernmentOrganizationService service = new GovernmentOrganizationService(firestore);
        GovernmentOrganizationTotalsDTO totals = service.getGovernmentOrganizationTotals();

        assertEquals(2, totals.getTotalGovernmentOrganizations());
        assertEquals(1, totals.getActiveGovernmentOrganizations());
        assertEquals(1, totals.getInactiveGovernmentOrganizations());
        assertEquals(1, totals.getWorkforcePartners());
        assertEquals(1, totals.getCredentialPartners());
        assertEquals(1, totals.getOrganizationsByGovernmentLevel().get("municipal"));
        assertEquals(1, totals.getOrganizationsByGovernmentLevel().get("state"));
        assertEquals(1, totals.getOrganizationsByOrganizationType().get("city_government"));
        assertEquals(1, totals.getOrganizationsByOrganizationType().get("state_agency"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMissingGovernmentOrganizationReturnsNull() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);

        when(firestore.collection(GovernmentOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("missing-government")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(false);

        GovernmentOrganizationService service = new GovernmentOrganizationService(firestore);

        assertNull(service.getGovernmentOrganization("missing-government"));
    }

    @SuppressWarnings("unchecked")
    private void stubCollection(CollectionReference collection, List<QueryDocumentSnapshot> documents) throws Exception {
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(collection.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(documents);
    }

    private GovernmentOrganizationDTO governmentDto(String organizationName, String governmentLevel, String organizationType) {
        GovernmentOrganizationDTO dto = new GovernmentOrganizationDTO();
        dto.setOrganizationName(organizationName);
        dto.setGovernmentLevel(governmentLevel);
        dto.setOrganizationType(organizationType);
        dto.setWebsite("https://www.stlouis-mo.gov");
        dto.setPrimaryContactName("Jordan Lee");
        dto.setPrimaryContactTitle("Workforce Coordinator");
        dto.setPrimaryContactEmail("jordan@example.gov");
        dto.setPrimaryContactPhone("314-555-3434");
        dto.setWorkforcePartner(true);
        dto.setCredentialPartner(false);
        return dto;
    }

    private GovernmentOrganization government(
            String governmentOrganizationId,
            boolean active,
            String governmentLevel,
            String organizationType,
            boolean workforcePartner,
            boolean credentialPartner,
            String organizationName
    ) {
        GovernmentOrganization organization = new GovernmentOrganization();
        organization.setGovernmentOrganizationId(governmentOrganizationId);
        organization.setOrganizationName(organizationName);
        organization.setGovernmentLevel(governmentLevel);
        organization.setOrganizationType(organizationType);
        organization.setWebsite("https://www.stlouis-mo.gov");
        organization.setPrimaryContactName("Jordan Lee");
        organization.setPrimaryContactTitle("Workforce Coordinator");
        organization.setPrimaryContactEmail("jordan@example.gov");
        organization.setActive(active);
        organization.setWorkforcePartner(workforcePartner);
        organization.setCredentialPartner(credentialPartner);
        return organization;
    }
}
