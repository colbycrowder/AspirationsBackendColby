package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.AttendanceRecordCreationDTO;
import com.AspirationsNetwork.UserData.DTO.AttendanceTotalsDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    void createAttendanceRecordResolvesAspnParticipantIdBeforeStoringRecord() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        Query participantQuery = mock(Query.class);
        DocumentReference attendanceDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        QueryDocumentSnapshot youthDocument = mock(QueryDocumentSnapshot.class);
        QuerySnapshot participantSnapshot = mock(QuerySnapshot.class);
        CredentialService credentialService = mock(CredentialService.class);
        ApiFuture<QuerySnapshot> participantFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("aspnParticipantId", "ASPN-2026-0001")).thenReturn(participantQuery);
        when(participantQuery.get()).thenReturn(participantFuture);
        when(participantFuture.get()).thenReturn(participantSnapshot);
        when(participantSnapshot.isEmpty()).thenReturn(false);
        when(participantSnapshot.getDocuments()).thenReturn(List.of(youthDocument));
        when(youthDocument.getId()).thenReturn("firebase-youth-123");
        when(attendanceCollection.document(any(String.class))).thenReturn(attendanceDocument);
        when(usersCollection.document("firebase-youth-123")).thenReturn(userDocument);
        when(attendanceDocument.set(any(AttendanceRecord.class))).thenReturn(writeFuture);
        when(userDocument.update(eq("attendanceRecordIds"), any())).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setUserIdentifier("ASPN-2026-0001");
        dto.setProgramID("program-123");
        dto.setEventName("September Onboarding");
        dto.setAttendanceStatus("present");
        dto.setStaffRecorderUID("staff-123");

        AttendanceService service = new AttendanceService(firestore, credentialService);
        service.createAttendanceRecord(dto);

        ArgumentCaptor<AttendanceRecord> recordCaptor = ArgumentCaptor.forClass(AttendanceRecord.class);
        verify(attendanceDocument).set(recordCaptor.capture());

        assertEquals("firebase-youth-123", recordCaptor.getValue().getUserUID());
        verify(userDocument).update(eq("attendanceRecordIds"), any());
        verify(credentialService).evaluateAttendanceAutoAwards("firebase-youth-123", "program-123", "staff-123");
    }

    @Test
    @SuppressWarnings("unchecked")
    void createAttendanceRecordResolvesEmailBeforeStoringRecord() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        Query emailQuery = mock(Query.class);
        DocumentReference attendanceDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        QueryDocumentSnapshot youthDocument = mock(QueryDocumentSnapshot.class);
        QuerySnapshot emailSnapshot = mock(QuerySnapshot.class);
        CredentialService credentialService = mock(CredentialService.class);
        ApiFuture<QuerySnapshot> emailFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("email", "colbycrowderc@gmail.com")).thenReturn(emailQuery);
        when(emailQuery.get()).thenReturn(emailFuture);
        when(emailFuture.get()).thenReturn(emailSnapshot);
        when(emailSnapshot.isEmpty()).thenReturn(false);
        when(emailSnapshot.getDocuments()).thenReturn(List.of(youthDocument));
        when(youthDocument.getId()).thenReturn("firebase-youth-123");
        when(attendanceCollection.document(any(String.class))).thenReturn(attendanceDocument);
        when(usersCollection.document("firebase-youth-123")).thenReturn(userDocument);
        when(attendanceDocument.set(any(AttendanceRecord.class))).thenReturn(writeFuture);
        when(userDocument.update(eq("attendanceRecordIds"), any())).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setUserIdentifier("colbycrowderc@gmail.com");
        dto.setProgramID("program-123");
        dto.setEventName("YAB Meeting Test 3");
        dto.setAttendanceStatus("present");
        dto.setStaffRecorderUID("staff-123");

        AttendanceService service = new AttendanceService(firestore, credentialService);
        service.createAttendanceRecord(dto);

        ArgumentCaptor<AttendanceRecord> recordCaptor = ArgumentCaptor.forClass(AttendanceRecord.class);
        verify(attendanceDocument).set(recordCaptor.capture());

        assertEquals("firebase-youth-123", recordCaptor.getValue().getUserUID());
        verify(userDocument).update(eq("attendanceRecordIds"), any());
        verify(credentialService).evaluateAttendanceAutoAwards("firebase-youth-123", "program-123", "staff-123");
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

    @Test
    @SuppressWarnings("unchecked")
    void getAttendanceRecordsFiltersByUserProgramAndDate() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> queryFuture = mock(ApiFuture.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot matchingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot otherProgramDocument = mock(QueryDocumentSnapshot.class);
        Date eventDate = date(2026, Calendar.SEPTEMBER, 15);
        AttendanceRecord matchingRecord = attendanceRecord(
                "attendance-123",
                "youth-123",
                "program-123",
                eventDate,
                "present"
        );
        AttendanceRecord otherProgramRecord = attendanceRecord(
                "attendance-456",
                "youth-123",
                "program-456",
                eventDate,
                "absent"
        );

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(attendanceCollection.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(List.of(matchingDocument, otherProgramDocument));
        when(matchingDocument.toObject(AttendanceRecord.class)).thenReturn(matchingRecord);
        when(otherProgramDocument.toObject(AttendanceRecord.class)).thenReturn(otherProgramRecord);

        AttendanceService service = new AttendanceService(firestore, mock(CredentialService.class));
        List<AttendanceRecord> records = service.getAttendanceRecords("youth-123", "program-123", eventDate);

        assertEquals(1, records.size());
        assertEquals("attendance-123", records.get(0).getAttendanceRecordID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAttendanceTotalsCountsStatuses() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> queryFuture = mock(ApiFuture.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot presentDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot absentDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot pendingDocument = mock(QueryDocumentSnapshot.class);
        Date eventDate = new Date();

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(attendanceCollection.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(List.of(presentDocument, absentDocument, pendingDocument));
        when(presentDocument.toObject(AttendanceRecord.class))
                .thenReturn(attendanceRecord("attendance-1", "youth-123", "program-123", eventDate, "present"));
        when(absentDocument.toObject(AttendanceRecord.class))
                .thenReturn(attendanceRecord("attendance-2", "youth-123", "program-123", eventDate, "absent"));
        when(pendingDocument.toObject(AttendanceRecord.class))
                .thenReturn(attendanceRecord("attendance-3", "youth-123", "program-123", eventDate, "pending"));

        AttendanceService service = new AttendanceService(firestore, mock(CredentialService.class));
        AttendanceTotalsDTO totals = service.getAttendanceTotals("youth-123", "program-123", null);

        assertEquals(3, totals.getTotalRecords());
        assertEquals(1, totals.getPresent());
        assertEquals(1, totals.getAbsent());
        assertEquals(1, totals.getPending());
        assertEquals(0, totals.getExcused());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateAttendanceRecordUpdatesMutableFieldsAndEvaluatesAutoAwardWhenPresent() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        DocumentReference attendanceDocument = mock(DocumentReference.class);
        DocumentSnapshot attendanceSnapshot = mock(DocumentSnapshot.class);
        CredentialService credentialService = mock(CredentialService.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        AttendanceRecord existingRecord = attendanceRecord(
                "attendance-123",
                "youth-123",
                "program-123",
                new Date(),
                "pending"
        );

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(attendanceCollection.document("attendance-123")).thenReturn(attendanceDocument);
        when(attendanceDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(attendanceSnapshot);
        when(attendanceSnapshot.exists()).thenReturn(true);
        when(attendanceSnapshot.toObject(AttendanceRecord.class)).thenReturn(existingRecord);
        when(attendanceDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setUserUID("youth-123");
        dto.setProgramID("program-456");
        dto.setEventName("Updated event");
        dto.setAttendanceStatus("PRESENT");
        dto.setStaffRecorderUID("staff-123");

        AttendanceService service = new AttendanceService(firestore, credentialService);
        service.updateAttendanceRecord("attendance-123", dto);

        ArgumentCaptor<Map> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(attendanceDocument).update(updateCaptor.capture());
        assertEquals("program-456", updateCaptor.getValue().get("programID"));
        assertEquals("Updated event", updateCaptor.getValue().get("eventName"));
        assertEquals("present", updateCaptor.getValue().get("attendanceStatus"));
        assertEquals("staff-123", updateCaptor.getValue().get("staffRecorderUID"));
        verify(credentialService).evaluateAttendanceAutoAwards("youth-123", "program-456", "staff-123");
    }

    @Test
    void updateAttendanceRecordRejectsChangingUserUid() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        DocumentReference attendanceDocument = mock(DocumentReference.class);
        DocumentSnapshot attendanceSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        AttendanceRecord existingRecord = attendanceRecord(
                "attendance-123",
                "youth-123",
                "program-123",
                new Date(),
                "pending"
        );

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(attendanceCollection.document("attendance-123")).thenReturn(attendanceDocument);
        when(attendanceDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(attendanceSnapshot);
        when(attendanceSnapshot.exists()).thenReturn(true);
        when(attendanceSnapshot.toObject(AttendanceRecord.class)).thenReturn(existingRecord);

        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setUserUID("other-youth");
        dto.setStaffRecorderUID("staff-123");

        AttendanceService service = new AttendanceService(firestore, mock(CredentialService.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateAttendanceRecord("attendance-123", dto)
        );

        assertEquals("userUID cannot be changed for an attendance record", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteAttendanceRecordDeletesRecordAndUnlinksUserProfile() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference attendanceDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentSnapshot attendanceSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> deleteFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        AttendanceRecord existingRecord = attendanceRecord(
                "attendance-123",
                "youth-123",
                "program-123",
                new Date(),
                "present"
        );

        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(attendanceCollection.document("attendance-123")).thenReturn(attendanceDocument);
        when(usersCollection.document("youth-123")).thenReturn(userDocument);
        when(attendanceDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(attendanceSnapshot);
        when(attendanceSnapshot.exists()).thenReturn(true);
        when(attendanceSnapshot.toObject(AttendanceRecord.class)).thenReturn(existingRecord);
        when(attendanceDocument.delete()).thenReturn(deleteFuture);
        when(userDocument.update(eq("attendanceRecordIds"), any())).thenReturn(updateFuture);
        when(deleteFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        AttendanceService service = new AttendanceService(firestore, mock(CredentialService.class));
        service.deleteAttendanceRecord("attendance-123");

        verify(attendanceDocument).delete();
        verify(userDocument).update(eq("attendanceRecordIds"), any());
    }

    private AttendanceRecord attendanceRecord(
            String attendanceRecordID,
            String userUID,
            String programID,
            Date eventDate,
            String attendanceStatus
    ) {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceRecordID(attendanceRecordID);
        record.setUserUID(userUID);
        record.setProgramID(programID);
        record.setEventName("Test event");
        record.setEventDate(eventDate);
        record.setAttendanceStatus(attendanceStatus);
        record.setStaffRecorderUID("staff-123");
        return record;
    }

    private Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
