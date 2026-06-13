package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.EducatorDTO;
import com.AspirationsNetwork.UserData.DTO.EducatorTotalsDTO;
import com.AspirationsNetwork.UserData.Models.Educator;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EducatorServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createEducatorStoresEducatorWithActiveDefault() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(EducatorService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document(any(String.class))).thenReturn(document);
        when(document.set(any(Educator.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        EducatorDTO dto = educatorDto("Jane", "Smith", "jane@school.org", "Hazelwood East", "high_school");

        EducatorService service = new EducatorService(firestore);
        String educatorId = service.createEducator(dto);

        ArgumentCaptor<Educator> captor = ArgumentCaptor.forClass(Educator.class);
        verify(document).set(captor.capture());

        Educator savedEducator = captor.getValue();
        assertEquals(educatorId, savedEducator.getEducatorId());
        assertEquals("Jane", savedEducator.getFirstName());
        assertEquals("Smith", savedEducator.getLastName());
        assertEquals("jane@school.org", savedEducator.getEmail());
        assertEquals("Hazelwood East", savedEducator.getOrganizationName());
        assertEquals("high_school", savedEducator.getOrganizationType());
        assertTrue(savedEducator.isActive());
    }

    @Test
    void createEducatorRejectsInvalidOrganizationType() {
        EducatorDTO dto = educatorDto("Jane", "Smith", "jane@school.org", "Hazelwood East", "school");

        EducatorService service = new EducatorService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createEducator(dto)
        );

        assertEquals("organizationType is invalid", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getEducatorsFiltersByOrganizationTypeActiveOrganizationAndSearch() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        QueryDocumentSnapshot matchingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot wrongTypeDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(EducatorService.COLLECTION_NAME)).thenReturn(collection);
        stubCollection(collection, List.of(matchingDocument, inactiveDocument, wrongTypeDocument));
        when(matchingDocument.toObject(Educator.class)).thenReturn(educator("educator-1", true, "high_school", "Hazelwood East", "Jane"));
        when(inactiveDocument.toObject(Educator.class)).thenReturn(educator("educator-2", false, "high_school", "Hazelwood East", "Pat"));
        when(wrongTypeDocument.toObject(Educator.class)).thenReturn(educator("educator-3", true, "college", "UMSL", "Alex"));

        EducatorService service = new EducatorService(firestore);
        List<Educator> educators = service.getEducators("high_school", true, "Hazelwood", "Jane");

        assertEquals(1, educators.size());
        assertEquals("educator-1", educators.get(0).getEducatorId());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void updateEducatorUpdatesAllowedFields() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(EducatorService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("educator-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(Educator.class)).thenReturn(educator("educator-123", true, "high_school", "Hazelwood East", "Jane"));
        when(document.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        EducatorDTO dto = new EducatorDTO();
        dto.setTitle("Counselor");
        dto.setOrganizationType("middle_school");
        dto.setActive(false);

        EducatorService service = new EducatorService(firestore);
        service.updateEducator("educator-123", dto);

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(document).update(captor.capture());

        Map<String, Object> updates = captor.getValue();
        assertEquals("Counselor", updates.get("title"));
        assertEquals("middle_school", updates.get("organizationType"));
        assertEquals(false, updates.get("active"));
        assertFalse(updates.containsKey("educatorId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void activateAndDeactivateEducatorToggleActiveStatus() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> activateFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> deactivateFuture = mock(ApiFuture.class);

        when(firestore.collection(EducatorService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("educator-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(Educator.class)).thenReturn(educator("educator-123", true, "high_school", "Hazelwood East", "Jane"));
        when(document.update(
                any(String.class), any(Boolean.class),
                any(String.class), any(Object.class)
        )).thenReturn(activateFuture, deactivateFuture);
        when(activateFuture.get()).thenReturn(mock(WriteResult.class));
        when(deactivateFuture.get()).thenReturn(mock(WriteResult.class));

        EducatorService service = new EducatorService(firestore);
        service.activateEducator("educator-123");
        service.deactivateEducator("educator-123");

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
    void getEducatorTotalsCalculatesCountsAndOrganizationTypes() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        QueryDocumentSnapshot activeDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(EducatorService.COLLECTION_NAME)).thenReturn(collection);
        stubCollection(collection, List.of(activeDocument, inactiveDocument));
        when(activeDocument.toObject(Educator.class)).thenReturn(educator("educator-1", true, "high_school", "Hazelwood East", "Jane"));
        when(inactiveDocument.toObject(Educator.class)).thenReturn(educator("educator-2", false, "college", "UMSL", "Alex"));

        EducatorService service = new EducatorService(firestore);
        EducatorTotalsDTO totals = service.getEducatorTotals();

        assertEquals(2, totals.getTotalEducators());
        assertEquals(1, totals.getActiveEducators());
        assertEquals(1, totals.getInactiveEducators());
        assertEquals(2, totals.getOrganizationsRepresented());
        assertEquals(1, totals.getEducatorsByOrganizationType().get("high_school"));
        assertEquals(1, totals.getEducatorsByOrganizationType().get("college"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMissingEducatorReturnsNull() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);

        when(firestore.collection(EducatorService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("missing-educator")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(false);

        EducatorService service = new EducatorService(firestore);

        assertEquals(null, service.getEducator("missing-educator"));
    }

    @SuppressWarnings("unchecked")
    private void stubCollection(CollectionReference collection, List<QueryDocumentSnapshot> documents) throws Exception {
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(collection.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(documents);
    }

    private EducatorDTO educatorDto(String firstName, String lastName, String email, String organizationName, String organizationType) {
        EducatorDTO dto = new EducatorDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setOrganizationName(organizationName);
        dto.setOrganizationType(organizationType);
        return dto;
    }

    private Educator educator(String educatorId, boolean active, String organizationType, String organizationName, String firstName) {
        Educator educator = new Educator();
        educator.setEducatorId(educatorId);
        educator.setFirstName(firstName);
        educator.setLastName("Smith");
        educator.setEmail(firstName.toLowerCase() + "@example.org");
        educator.setOrganizationName(organizationName);
        educator.setOrganizationType(organizationType);
        educator.setActive(active);
        return educator;
    }
}
