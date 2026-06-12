package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.StaffOperationReportingDTO;
import com.AspirationsNetwork.UserData.Models.StaffOperationEvent;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
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

    @Test
    @SuppressWarnings("unchecked")
    void buildOperationReportCountsTotalsWindowsAndGroupings() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference eventsCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> eventsFuture = mock(ApiFuture.class);
        QuerySnapshot eventsSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot recentCredentialDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot sixtyDayProgramDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot olderAttendanceDocument = mock(QueryDocumentSnapshot.class);

        StaffOperationEvent recentCredential = operationEvent(
                "staff-1",
                StaffOperationEventService.CREDENTIAL_AWARDED,
                "earnedCredential",
                daysAgo(10)
        );
        StaffOperationEvent sixtyDayProgram = operationEvent(
                "staff-1",
                StaffOperationEventService.PROGRAM_UPDATED,
                "program",
                daysAgo(45)
        );
        StaffOperationEvent olderAttendance = operationEvent(
                "staff-2",
                StaffOperationEventService.ATTENDANCE_RECORDED,
                "attendanceRecord",
                daysAgo(100)
        );

        when(firestore.collection(StaffOperationEventService.COLLECTION_NAME)).thenReturn(eventsCollection);
        when(eventsCollection.get()).thenReturn(eventsFuture);
        when(eventsFuture.get()).thenReturn(eventsSnapshot);
        when(eventsSnapshot.getDocuments()).thenReturn(List.of(
                recentCredentialDocument,
                sixtyDayProgramDocument,
                olderAttendanceDocument
        ));
        when(recentCredentialDocument.toObject(StaffOperationEvent.class)).thenReturn(recentCredential);
        when(sixtyDayProgramDocument.toObject(StaffOperationEvent.class)).thenReturn(sixtyDayProgram);
        when(olderAttendanceDocument.toObject(StaffOperationEvent.class)).thenReturn(olderAttendance);

        StaffOperationEventService service = new StaffOperationEventService(firestore);
        StaffOperationReportingDTO report = service.buildOperationReport();

        assertEquals(3, report.getTotalOperations());
        assertEquals(1, report.getOperationsLast30Days());
        assertEquals(2, report.getOperationsLast60Days());
        assertEquals(2, report.getOperationsLast90Days());
        assertEquals(1, report.getOperationsByType().get(StaffOperationEventService.CREDENTIAL_AWARDED));
        assertEquals(1, report.getOperationsByType().get(StaffOperationEventService.PROGRAM_UPDATED));
        assertEquals(1, report.getOperationsByType().get(StaffOperationEventService.ATTENDANCE_RECORDED));
        assertEquals(2, report.getOperationsByStaffUser().get("staff-1"));
        assertEquals(1, report.getOperationsByStaffUser().get("staff-2"));
        assertEquals(1, report.getOperationsByTargetType().get("earnedCredential"));
        assertEquals(1, report.getOperationsByTargetType().get("program"));
        assertEquals(1, report.getOperationsByTargetType().get("attendanceRecord"));
    }

    private StaffOperationEvent operationEvent(
            String staffUID,
            String operationType,
            String targetType,
            Date createdAt
    ) {
        StaffOperationEvent event = new StaffOperationEvent();
        event.setStaffUID(staffUID);
        event.setOperationType(operationType);
        event.setTargetType(targetType);
        event.setCreatedAt(createdAt);
        return event;
    }

    private Date daysAgo(int days) {
        long millisPerDay = 24L * 60L * 60L * 1000L;
        return new Date(new Date().getTime() - days * millisPerDay);
    }
}
