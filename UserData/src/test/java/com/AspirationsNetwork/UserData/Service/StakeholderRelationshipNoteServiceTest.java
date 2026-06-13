package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.StakeholderRelationshipNoteDTO;
import com.AspirationsNetwork.UserData.DTO.StakeholderRelationshipNoteTotalsDTO;
import com.AspirationsNetwork.UserData.Models.StakeholderRelationshipNote;
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

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StakeholderRelationshipNoteServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createNoteStoresNoteWithVerifiedStaffOwnerDefault() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(StakeholderRelationshipNoteService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document(any(String.class))).thenReturn(document);
        when(document.set(any(StakeholderRelationshipNote.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        StakeholderRelationshipNoteDTO dto = noteDto("educator", "educator-123", "contacted");

        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(firestore);
        String noteId = service.createNote(dto, "staff-123");

        ArgumentCaptor<StakeholderRelationshipNote> captor = ArgumentCaptor.forClass(StakeholderRelationshipNote.class);
        verify(document).set(captor.capture());

        StakeholderRelationshipNote savedNote = captor.getValue();
        assertEquals(noteId, savedNote.getStakeholderRelationshipNoteId());
        assertEquals("educator", savedNote.getStakeholderType());
        assertEquals("educator-123", savedNote.getStakeholderId());
        assertEquals("contacted", savedNote.getRelationshipStatus());
        assertEquals("staff-123", savedNote.getRelationshipOwnerUID());
        assertTrue(savedNote.isActive());
    }

    @Test
    void createNoteRejectsInvalidStakeholderType() {
        StakeholderRelationshipNoteDTO dto = noteDto("student", "youth-123", "prospect");
        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createNote(dto, "staff-123")
        );

        assertEquals("stakeholderType is invalid", exception.getMessage());
    }

    @Test
    void createNoteRejectsInvalidRelationshipStatus() {
        StakeholderRelationshipNoteDTO dto = noteDto("educator", "educator-123", "emailed");
        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createNote(dto, "staff-123")
        );

        assertEquals("relationshipStatus is invalid", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getNotesFiltersByStakeholderStatusOwnerActiveAndFollowUpWindow() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        QueryDocumentSnapshot matchingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);
        Date now = new Date();
        Date yesterday = new Date(now.getTime() - 24L * 60L * 60L * 1000L);
        Date tomorrow = new Date(now.getTime() + 24L * 60L * 60L * 1000L);

        when(firestore.collection(StakeholderRelationshipNoteService.COLLECTION_NAME)).thenReturn(collection);
        stubCollection(collection, List.of(matchingDocument, inactiveDocument));
        when(matchingDocument.toObject(StakeholderRelationshipNote.class))
                .thenReturn(note("note-1", "educator", "educator-123", "contacted", "staff-123", true, tomorrow));
        when(inactiveDocument.toObject(StakeholderRelationshipNote.class))
                .thenReturn(note("note-2", "educator", "educator-123", "contacted", "staff-123", false, yesterday));

        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(firestore);
        List<StakeholderRelationshipNote> notes = service.getNotes(
                "educator",
                "educator-123",
                "contacted",
                "staff-123",
                true,
                tomorrow,
                now
        );

        assertEquals(1, notes.size());
        assertEquals("note-1", notes.get(0).getStakeholderRelationshipNoteId());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void updateNoteUpdatesAllowedFields() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(StakeholderRelationshipNoteService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("note-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(StakeholderRelationshipNote.class))
                .thenReturn(note("note-123", "educator", "educator-123", "prospect", "staff-123", true, null));
        when(document.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        StakeholderRelationshipNoteDTO dto = new StakeholderRelationshipNoteDTO();
        dto.setRelationshipStatus("active_partner");
        dto.setNoteText("Updated note");
        dto.setActive(false);

        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(firestore);
        service.updateNote("note-123", dto, "staff-123");

        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(document).update(captor.capture());

        Map<String, Object> updates = captor.getValue();
        assertEquals("active_partner", updates.get("relationshipStatus"));
        assertEquals("Updated note", updates.get("noteText"));
        assertEquals(false, updates.get("active"));
        assertFalse(updates.containsKey("stakeholderRelationshipNoteId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateNoteRejectsBlankRequiredFields() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);

        when(firestore.collection(StakeholderRelationshipNoteService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("note-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(StakeholderRelationshipNote.class))
                .thenReturn(note("note-123", "educator", "educator-123", "prospect", "staff-123", true, null));

        StakeholderRelationshipNoteDTO dto = new StakeholderRelationshipNoteDTO();
        dto.setNoteText("   ");

        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(firestore);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateNote("note-123", dto, "staff-123")
        );

        assertEquals("noteText is required", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteNoteDeletesExistingDocument() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> deleteFuture = mock(ApiFuture.class);

        when(firestore.collection(StakeholderRelationshipNoteService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("note-123")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.toObject(StakeholderRelationshipNote.class))
                .thenReturn(note("note-123", "educator", "educator-123", "prospect", "staff-123", true, null));
        when(document.delete()).thenReturn(deleteFuture);
        when(deleteFuture.get()).thenReturn(mock(WriteResult.class));

        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(firestore);
        service.deleteNote("note-123");

        verify(document).delete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTotalsCalculatesUpcomingAndOverdueFollowUps() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        QueryDocumentSnapshot upcomingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot overdueDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);
        Date now = new Date();
        Date yesterday = new Date(now.getTime() - 24L * 60L * 60L * 1000L);
        Date tomorrow = new Date(now.getTime() + 24L * 60L * 60L * 1000L);

        when(firestore.collection(StakeholderRelationshipNoteService.COLLECTION_NAME)).thenReturn(collection);
        stubCollection(collection, List.of(upcomingDocument, overdueDocument, inactiveDocument));
        when(upcomingDocument.toObject(StakeholderRelationshipNote.class))
                .thenReturn(note("note-1", "educator", "educator-123", "contacted", "staff-123", true, tomorrow));
        when(overdueDocument.toObject(StakeholderRelationshipNote.class))
                .thenReturn(note("note-2", "partner_organization", "partner-123", "meeting_scheduled", "staff-123", true, yesterday));
        when(inactiveDocument.toObject(StakeholderRelationshipNote.class))
                .thenReturn(note("note-3", "government_organization", "government-123", "inactive_partner", "staff-123", false, yesterday));

        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(firestore);
        StakeholderRelationshipNoteTotalsDTO totals = service.getTotals();

        assertEquals(3, totals.getTotalNotes());
        assertEquals(2, totals.getActiveNotes());
        assertEquals(1, totals.getInactiveNotes());
        assertEquals(1, totals.getUpcomingFollowUps());
        assertEquals(1, totals.getOverdueFollowUps());
        assertEquals(1, totals.getNotesByStakeholderType().get("educator"));
        assertEquals(1, totals.getNotesByRelationshipStatus().get("contacted"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMissingNoteReturnsNull() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference collection = mock(CollectionReference.class);
        DocumentReference document = mock(DocumentReference.class);
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);

        when(firestore.collection(StakeholderRelationshipNoteService.COLLECTION_NAME)).thenReturn(collection);
        when(collection.document("missing-note")).thenReturn(document);
        when(document.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(snapshot);
        when(snapshot.exists()).thenReturn(false);

        StakeholderRelationshipNoteService service = new StakeholderRelationshipNoteService(firestore);

        assertNull(service.getNote("missing-note"));
    }

    @SuppressWarnings("unchecked")
    private void stubCollection(CollectionReference collection, List<QueryDocumentSnapshot> documents) throws Exception {
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);

        when(collection.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(documents);
    }

    private StakeholderRelationshipNoteDTO noteDto(String stakeholderType, String stakeholderId, String relationshipStatus) {
        StakeholderRelationshipNoteDTO dto = new StakeholderRelationshipNoteDTO();
        dto.setStakeholderType(stakeholderType);
        dto.setStakeholderId(stakeholderId);
        dto.setStakeholderName("Stakeholder Name");
        dto.setNoteText("Relationship note");
        dto.setRelationshipStatus(relationshipStatus);
        return dto;
    }

    private StakeholderRelationshipNote note(
            String noteId,
            String stakeholderType,
            String stakeholderId,
            String relationshipStatus,
            String ownerUID,
            boolean active,
            Date nextFollowUpDate
    ) {
        StakeholderRelationshipNote note = new StakeholderRelationshipNote();
        note.setStakeholderRelationshipNoteId(noteId);
        note.setStakeholderType(stakeholderType);
        note.setStakeholderId(stakeholderId);
        note.setStakeholderName("Stakeholder Name");
        note.setNoteText("Relationship note");
        note.setRelationshipStatus(relationshipStatus);
        note.setRelationshipOwnerUID(ownerUID);
        note.setActive(active);
        note.setNextFollowUpDate(nextFollowUpDate);
        return note;
    }
}
