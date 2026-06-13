package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PartnerOrganizationDTO;
import com.AspirationsNetwork.UserData.DTO.PartnerOrganizationTotalsDTO;
import com.AspirationsNetwork.UserData.Models.PartnerOrganization;
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

class PartnerOrganizationServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createPartnerOrganizationStoresPartnerWithActiveDefault() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(PartnerOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document(any(String.class))).thenReturn(document);
        when(document.set(any(PartnerOrganization.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        PartnerOrganizationDTO dto = partnerDto("ASPN Partner", "nonprofit");

        PartnerOrganizationService service = new PartnerOrganizationService(firestore);
        String partnerId = service.createPartnerOrganization(dto);

        ArgumentCaptor<PartnerOrganization> captor = ArgumentCaptor.forClass(PartnerOrganization.class);
        verify(document).set(captor.capture());

        PartnerOrganization savedPartner = captor.getValue();
        assertEquals(partnerId, savedPartner.getPartnerOrganizationId());
        assertEquals("ASPN Partner", savedPartner.getOrganizationName());
        assertEquals("nonprofit", savedPartner.getOrganizationType());
        assertEquals("https://example.org", savedPartner.getWebsite());
        assertEquals("Jordan Lee", savedPartner.getPrimaryContactName());
        assertTrue(savedPartner.isActive());
    }

    @Test
    void createPartnerOrganizationRejectsInvalidOrganizationType() {
        PartnerOrganizationDTO dto = partnerDto("ASPN Partner", "school");

        PartnerOrganizationService service = new PartnerOrganizationService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createPartnerOrganization(dto)
        );

        assertEquals("organizationType is invalid", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPartnerOrganizationsFiltersByOrganizationTypeActiveAndOrganizationName() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        QueryDocumentSnapshot matchingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot wrongTypeDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(PartnerOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        stubCollection(collection, List.of(matchingDocument, inactiveDocument, wrongTypeDocument));
        when(matchingDocument.toObject(PartnerOrganization.class)).thenReturn(partner("partner-1", true, "nonprofit", "ASPN Partner"));
        when(inactiveDocument.toObject(PartnerOrganization.class)).thenReturn(partner("partner-2", false, "nonprofit", "ASPN Partner"));
        when(wrongTypeDocument.toObject(PartnerOrganization.class)).thenReturn(partner("partner-3", true, "foundation", "Regional Foundation"));

        PartnerOrganizationService service = new PartnerOrganizationService(firestore);
        List<PartnerOrganization> partners = service.getPartnerOrganizations("nonprofit", true, "ASPN");

        assertEquals(1, partners.size());
        assertEquals("partner-1", partners.get(0).getPartnerOrganizationId());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void updatePartnerOrganizationUpdatesAllowedFields() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(PartnerOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("partner-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(PartnerOrganization.class)).thenReturn(partner("partner-123", true, "nonprofit", "ASPN Partner"));
        when(document.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        PartnerOrganizationDTO dto = new PartnerOrganizationDTO();
        dto.setWebsite("https://updated.example.org");
        dto.setOrganizationType("foundation");
        dto.setActive(false);

        PartnerOrganizationService service = new PartnerOrganizationService(firestore);
        service.updatePartnerOrganization("partner-123", dto);

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(document).update(captor.capture());

        Map<String, Object> updates = captor.getValue();
        assertEquals("https://updated.example.org", updates.get("website"));
        assertEquals("foundation", updates.get("organizationType"));
        assertEquals(false, updates.get("active"));
        assertFalse(updates.containsKey("partnerOrganizationId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void activateAndDeactivatePartnerOrganizationToggleActiveStatus() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> activateFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> deactivateFuture = mock(ApiFuture.class);

        when(firestore.collection(PartnerOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("partner-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(PartnerOrganization.class)).thenReturn(partner("partner-123", true, "nonprofit", "ASPN Partner"));
        when(document.update(
                any(String.class), any(Boolean.class),
                any(String.class), any(Object.class)
        )).thenReturn(activateFuture, deactivateFuture);
        when(activateFuture.get()).thenReturn(mock(WriteResult.class));
        when(deactivateFuture.get()).thenReturn(mock(WriteResult.class));

        PartnerOrganizationService service = new PartnerOrganizationService(firestore);
        service.activatePartnerOrganization("partner-123");
        service.deactivatePartnerOrganization("partner-123");

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
    void getPartnerOrganizationTotalsCalculatesCountsAndOrganizationTypes() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        QueryDocumentSnapshot activeDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(PartnerOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        stubCollection(collection, List.of(activeDocument, inactiveDocument));
        when(activeDocument.toObject(PartnerOrganization.class)).thenReturn(partner("partner-1", true, "nonprofit", "ASPN Partner"));
        when(inactiveDocument.toObject(PartnerOrganization.class)).thenReturn(partner("partner-2", false, "foundation", "Regional Foundation"));

        PartnerOrganizationService service = new PartnerOrganizationService(firestore);
        PartnerOrganizationTotalsDTO totals = service.getPartnerOrganizationTotals();

        assertEquals(2, totals.getTotalPartners());
        assertEquals(1, totals.getActivePartners());
        assertEquals(1, totals.getInactivePartners());
        assertEquals(2, totals.getOrganizationTypesRepresented());
        assertEquals(1, totals.getPartnersByOrganizationType().get("nonprofit"));
        assertEquals(1, totals.getPartnersByOrganizationType().get("foundation"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMissingPartnerOrganizationReturnsNull() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);

        when(firestore.collection(PartnerOrganizationService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("missing-partner")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(false);

        PartnerOrganizationService service = new PartnerOrganizationService(firestore);

        assertNull(service.getPartnerOrganization("missing-partner"));
    }

    @SuppressWarnings("unchecked")
    private void stubCollection(CollectionReference collection, List<QueryDocumentSnapshot> documents) throws Exception {
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(collection.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(documents);
    }

    private PartnerOrganizationDTO partnerDto(String organizationName, String organizationType) {
        PartnerOrganizationDTO dto = new PartnerOrganizationDTO();
        dto.setOrganizationName(organizationName);
        dto.setOrganizationType(organizationType);
        dto.setWebsite("https://example.org");
        dto.setPrimaryContactName("Jordan Lee");
        dto.setPrimaryContactEmail("jordan@example.org");
        dto.setPrimaryContactPhone("314-555-1212");
        return dto;
    }

    private PartnerOrganization partner(String partnerOrganizationId, boolean active, String organizationType, String organizationName) {
        PartnerOrganization partner = new PartnerOrganization();
        partner.setPartnerOrganizationId(partnerOrganizationId);
        partner.setOrganizationName(organizationName);
        partner.setOrganizationType(organizationType);
        partner.setWebsite("https://example.org");
        partner.setPrimaryContactName("Jordan Lee");
        partner.setPrimaryContactEmail("jordan@example.org");
        partner.setActive(active);
        return partner;
    }
}
