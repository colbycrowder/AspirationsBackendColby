package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.StaffOperationEvent;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StaffOperationEventServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void trackOperationStoresStaffOperationEvent() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference eventsCollection = mock(CollectionReference.class);
        DocumentReference eventDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(StaffOperationEventService.COLLECTION_NAME)).thenReturn(eventsCollection);
        when(eventsCollection.document(any(String.class))).thenReturn(eventDocument);
        when(eventDocument.set(any(StaffOperationEvent.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        StaffOperationEventService service = new StaffOperationEventService(firestore);
        String eventId = service.trackOperation(
                "staff-123",
                StaffOperationEventService.CREDENTIAL_AWARDED,
                "earnedCredential",
                "earned-123",
                "youth-123",
                Map.of("credentialID", "credential-123")
        );

        ArgumentCaptor<StaffOperationEvent> eventCaptor = ArgumentCaptor.forClass(StaffOperationEvent.class);
        verify(eventDocument).set(eventCaptor.capture());
        StaffOperationEvent savedEvent = eventCaptor.getValue();
        assertEquals(eventId, savedEvent.getStaffOperationEventId());
        assertEquals("staff-123", savedEvent.getStaffUID());
        assertEquals(StaffOperationEventService.CREDENTIAL_AWARDED, savedEvent.getOperationType());
        assertEquals("earnedCredential", savedEvent.getTargetType());
        assertEquals("earned-123", savedEvent.getTargetId());
        assertEquals("youth-123", savedEvent.getTargetUserUID());
        assertEquals("credential-123", savedEvent.getMetadata().get("credentialID"));
        assertNotNull(savedEvent.getCreatedAt());
    }

    @Test
    void trackOperationRejectsMissingStaffUid() {
        StaffOperationEventService service = new StaffOperationEventService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.trackOperation(
                        "",
                        StaffOperationEventService.ATTENDANCE_RECORDED,
                        "attendanceRecord",
                        "attendance-123",
                        "youth-123",
                        Map.of()
                )
        );

        assertEquals("staffUID is required", exception.getMessage());
    }

    @Test
    void trackOperationSafelyDoesNotThrowWhenFirestoreWriteFails() throws Exception {
        Firestore firestore = mock(Firestore.class);
        when(firestore.collection(StaffOperationEventService.COLLECTION_NAME))
                .thenThrow(new RuntimeException("Firestore unavailable"));

        StaffOperationEventService service = new StaffOperationEventService(firestore);

        service.trackOperationSafely(
                "staff-123",
                StaffOperationEventService.PROGRAM_UPDATED,
                "program",
                "program-123",
                null,
                Map.of("programStatus", "active")
        );

        verify(firestore).collection(StaffOperationEventService.COLLECTION_NAME);
    }
}
