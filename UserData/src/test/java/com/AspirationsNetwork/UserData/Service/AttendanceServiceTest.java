package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.AttendanceRecordCreationDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttendanceServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createAttendanceRecordStoresRecordAndLinksToUserProfile() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference attendanceDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        CredentialService credentialService = mock(CredentialService.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        Date eventDate = new Date();

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(attendanceCollection.document(any(String.class))).thenReturn(attendanceDocument);
        when(usersCollection.document("youth-123")).thenReturn(userDocument);
        when(attendanceDocument.set(any(AttendanceRecord.class))).thenReturn(writeFuture);
        when(userDocument.update(eq("attendanceRecordIds"), any())).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setUserUID("youth-123");
        dto.setProgramID("program-123");
        dto.setEventName("September Onboarding");
        dto.setEventDate(eventDate);
        dto.setAttendanceStatus("PRESENT");
        dto.setStaffRecorderUID("staff-123");

        AttendanceService service = new AttendanceService(firestore, credentialService);
        String attendanceRecordID = service.createAttendanceRecord(dto);

        ArgumentCaptor<AttendanceRecord> recordCaptor = ArgumentCaptor.forClass(AttendanceRecord.class);
        verify(attendanceDocument).set(recordCaptor.capture());
        verify(userDocument).update(eq("attendanceRecordIds"), any());

        AttendanceRecord savedRecord = recordCaptor.getValue();
        assertEquals(attendanceRecordID, savedRecord.getAttendanceRecordID());
        assertEquals("youth-123", savedRecord.getUserUID());
        assertEquals("program-123", savedRecord.getProgramID());
        assertEquals("September Onboarding", savedRecord.getEventName());
        assertEquals(eventDate, savedRecord.getEventDate());
        assertEquals("present", savedRecord.getAttendanceStatus());
        assertEquals("staff-123", savedRecord.getStaffRecorderUID());
        verify(credentialService).evaluateAttendanceAutoAwards("youth-123", "program-123", "staff-123");
    }

    @Test
    @SuppressWarnings("unchecked")
    void createAttendanceRecordDoesNotEvaluateAutoAwardsForNonPresentStatus() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference attendanceDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        CredentialService credentialService = mock(CredentialService.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(attendanceCollection.document(any(String.class))).thenReturn(attendanceDocument);
        when(usersCollection.document("youth-123")).thenReturn(userDocument);
        when(attendanceDocument.set(any(AttendanceRecord.class))).thenReturn(writeFuture);
        when(userDocument.update(eq("attendanceRecordIds"), any())).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setUserUID("youth-123");
        dto.setProgramID("program-123");
        dto.setEventName("September Onboarding");
        dto.setAttendanceStatus("ABSENT");
        dto.setStaffRecorderUID("staff-123");

        AttendanceService service = new AttendanceService(firestore, credentialService);
        service.createAttendanceRecord(dto);

        verify(credentialService, never()).evaluateAttendanceAutoAwards(any(), any(), any());
    }

    @Test
    void createAttendanceRecordRejectsInvalidStatus() {
        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setUserUID("youth-123");
        dto.setProgramID("program-123");
        dto.setEventName("September Onboarding");
        dto.setAttendanceStatus("late");
        dto.setStaffRecorderUID("staff-123");

        AttendanceService service = new AttendanceService(mock(Firestore.class), mock(CredentialService.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createAttendanceRecord(dto)
        );

        assertEquals("attendanceStatus must be present, absent, excused, or pending", exception.getMessage());
    }
}
