package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ProgramDetailDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramTotalsDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProgramServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createProgramStoresProgramWithActiveDefaultAndStaffCreator() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference programsCollection = mock(CollectionReference.class);
        DocumentReference programDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(ProgramService.COLLECTION_NAME)).thenReturn(programsCollection);
        when(programsCollection.document(any(String.class))).thenReturn(programDocument);
        when(programDocument.set(any(Program.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        ProgramDTO dto = new ProgramDTO();
        dto.setProgramName("September Onboarding");
        dto.setDescription("Youth onboarding program");
        dto.setCategory("onboarding");
        dto.setProgramLeader("ASPN Staff");
        dto.setCapacity(25);
        dto.setCreatedByStaffUID("staff-123");

        ProgramService service = new ProgramService(firestore);
        String programId = service.createProgram(dto);

        ArgumentCaptor<Program> programCaptor = ArgumentCaptor.forClass(Program.class);
        verify(programDocument).set(programCaptor.capture());

        Program savedProgram = programCaptor.getValue();
        assertEquals(programId, savedProgram.getProgramId());
        assertEquals("September Onboarding", savedProgram.getProgramName());
        assertEquals("Youth onboarding program", savedProgram.getDescription());
        assertEquals("onboarding", savedProgram.getCategory());
        assertEquals("ASPN Staff", savedProgram.getProgramLeader());
        assertEquals(25, savedProgram.getCapacity());
        assertEquals("active", savedProgram.getProgramStatus());
        assertEquals("staff-123", savedProgram.getCreatedByStaffUID());
    }

    @Test
    void createProgramRejectsInvalidStatus() {
        ProgramDTO dto = new ProgramDTO();
        dto.setProgramName("September Onboarding");
        dto.setProgramStatus("draft");
        dto.setCreatedByStaffUID("staff-123");

        ProgramService service = new ProgramService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createProgram(dto)
        );

        assertEquals("programStatus must be active or archived", exception.getMessage());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void updateProgramUpdatesAllowedFieldsWithoutChangingCreator() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference programsCollection = mock(CollectionReference.class);
        DocumentReference programDocument = mock(DocumentReference.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        DocumentSnapshot programSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(ProgramService.COLLECTION_NAME)).thenReturn(programsCollection);
        when(programsCollection.document("program-123")).thenReturn(programDocument);
        when(programDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(programSnapshot);
        when(programSnapshot.exists()).thenReturn(true);
        when(programDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        ProgramDTO dto = new ProgramDTO();
        dto.setProgramName("Updated Program Name");
        dto.setCapacity(30);
        dto.setProgramStatus("ARCHIVED");
        dto.setCreatedByStaffUID("client-spoofed-staff");

        ProgramService service = new ProgramService(firestore);
        service.updateProgram("program-123", dto);

        ArgumentCaptor<Map> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(programDocument).update(updateCaptor.capture());

        Map<String, Object> updates = updateCaptor.getValue();
        assertEquals("Updated Program Name", updates.get("programName"));
        assertEquals(30, updates.get("capacity"));
        assertEquals("archived", updates.get("programStatus"));
        assertFalse(updates.containsKey("createdByStaffUID"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProgramsFiltersByActiveStatusAndProgramTypeCategory() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference programsCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> programsFuture = mock(ApiFuture.class);
        QuerySnapshot programsSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot activeDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot archivedDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(ProgramService.COLLECTION_NAME)).thenReturn(programsCollection);
        when(programsCollection.get()).thenReturn(programsFuture);
        when(programsFuture.get()).thenReturn(programsSnapshot);
        when(programsSnapshot.getDocuments()).thenReturn(List.of(activeDocument, archivedDocument));
        when(activeDocument.toObject(Program.class)).thenReturn(program("program-1", "active", "leadership"));
        when(archivedDocument.toObject(Program.class)).thenReturn(program("program-2", "archived", "leadership"));

        ProgramService service = new ProgramService(firestore);
        List<Program> programs = service.getPrograms(true, "Leadership");

        assertEquals(1, programs.size());
        assertEquals("program-1", programs.get(0).getProgramId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProgramDetailReturnsProgramAndRelatedCounts() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference programsCollection = mock(CollectionReference.class);
        CollectionReference enrollmentsCollection = mock(CollectionReference.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        DocumentReference programDocument = mock(DocumentReference.class);
        DocumentSnapshot programSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> programFuture = mock(ApiFuture.class);

        QueryDocumentSnapshot enrollmentDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot definitionDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot earnedDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot attendanceDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot serviceHourDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(ProgramService.COLLECTION_NAME)).thenReturn(programsCollection);
        when(firestore.collection(ProgramEnrollmentService.COLLECTION_NAME)).thenReturn(enrollmentsCollection);
        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        when(programsCollection.document("program-123")).thenReturn(programDocument);
        when(programDocument.get()).thenReturn(programFuture);
        when(programFuture.get()).thenReturn(programSnapshot);
        when(programSnapshot.exists()).thenReturn(true);
        when(programSnapshot.toObject(Program.class)).thenReturn(program("program-123", "active", "leadership"));
        stubCollection(enrollmentsCollection, List.of(enrollmentDocument));
        stubCollection(definitionsCollection, List.of(definitionDocument));
        stubCollection(earnedCollection, List.of(earnedDocument));
        stubCollection(attendanceCollection, List.of(attendanceDocument));
        stubCollection(serviceHoursCollection, List.of(serviceHourDocument));
        when(enrollmentDocument.toObject(ProgramEnrollment.class)).thenReturn(enrollment("program-123"));
        when(definitionDocument.toObject(CredentialDefinition.class)).thenReturn(credentialDefinition("credential-123", "program-123"));
        when(earnedDocument.toObject(EarnedCredential.class)).thenReturn(earnedCredential("credential-123"));
        when(attendanceDocument.toObject(AttendanceRecord.class)).thenReturn(attendanceRecord("program-123"));
        when(serviceHourDocument.toObject(ServiceHourRecord.class)).thenReturn(serviceHourRecord("program-123", 3.5));

        ProgramService service = new ProgramService(firestore);
        ProgramDetailDTO detail = service.getProgramDetail("program-123");

        assertEquals("program-123", detail.getProgram().getProgramId());
        assertEquals(1, detail.getEnrollmentCount());
        assertEquals(1, detail.getCredentialCount());
        assertEquals(1, detail.getAttendanceCount());
        assertEquals(1, detail.getServiceHourRecordCount());
        assertEquals(3.5, detail.getServiceHourTotal());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getProgramTotalsCountsProgramsAndRelatedCollections() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference programsCollection = mock(CollectionReference.class);
        CollectionReference enrollmentsCollection = mock(CollectionReference.class);
        CollectionReference earnedCollection = mock(CollectionReference.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        QueryDocumentSnapshot activeProgramDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot archivedProgramDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot enrollmentDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot earnedDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot attendanceDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot serviceHourDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(ProgramService.COLLECTION_NAME)).thenReturn(programsCollection);
        when(firestore.collection(ProgramEnrollmentService.COLLECTION_NAME)).thenReturn(enrollmentsCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION)).thenReturn(earnedCollection);
        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        stubCollection(programsCollection, List.of(activeProgramDocument, archivedProgramDocument));
        stubCollection(enrollmentsCollection, List.of(enrollmentDocument));
        stubCollection(earnedCollection, List.of(earnedDocument));
        stubCollection(attendanceCollection, List.of(attendanceDocument));
        stubCollection(serviceHoursCollection, List.of(serviceHourDocument));
        when(activeProgramDocument.toObject(Program.class)).thenReturn(program("program-1", "active", "leadership"));
        when(archivedProgramDocument.toObject(Program.class)).thenReturn(program("program-2", "archived", "leadership"));
        when(serviceHourDocument.toObject(ServiceHourRecord.class)).thenReturn(serviceHourRecord("program-1", 2.0));

        ProgramService service = new ProgramService(firestore);
        ProgramTotalsDTO totals = service.getProgramTotals();

        assertEquals(2, totals.getTotalPrograms());
        assertEquals(1, totals.getActivePrograms());
        assertEquals(1, totals.getArchivedPrograms());
        assertEquals(1, totals.getTotalEnrollments());
        assertEquals(1, totals.getTotalCredentialsEarned());
        assertEquals(1, totals.getTotalAttendanceRecords());
        assertEquals(2.0, totals.getTotalServiceHours());
        assertEquals(1, totals.getProgramsByStatus().get("active"));
        assertEquals(1, totals.getProgramsByStatus().get("archived"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void archiveAndRestoreProgramToggleProgramStatus() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference programsCollection = mock(CollectionReference.class);
        DocumentReference programDocument = mock(DocumentReference.class);
        DocumentSnapshot programSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> archiveFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> restoreFuture = mock(ApiFuture.class);

        when(firestore.collection(ProgramService.COLLECTION_NAME)).thenReturn(programsCollection);
        when(programsCollection.document("program-123")).thenReturn(programDocument);
        when(programDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(programSnapshot);
        when(programSnapshot.exists()).thenReturn(true);
        when(programSnapshot.toObject(Program.class)).thenReturn(program("program-123", "active", "leadership"));
        when(programDocument.update(
                org.mockito.ArgumentMatchers.eq("programStatus"), org.mockito.ArgumentMatchers.eq("archived"),
                org.mockito.ArgumentMatchers.eq("updatedAt"), any()
        )).thenReturn(archiveFuture);
        when(programDocument.update(
                org.mockito.ArgumentMatchers.eq("programStatus"), org.mockito.ArgumentMatchers.eq("active"),
                org.mockito.ArgumentMatchers.eq("updatedAt"), any()
        )).thenReturn(restoreFuture);
        when(archiveFuture.get()).thenReturn(mock(WriteResult.class));
        when(restoreFuture.get()).thenReturn(mock(WriteResult.class));

        ProgramService service = new ProgramService(firestore);
        service.archiveProgram("program-123");
        service.restoreProgram("program-123");

        verify(programDocument).update(
                org.mockito.ArgumentMatchers.eq("programStatus"), org.mockito.ArgumentMatchers.eq("archived"),
                org.mockito.ArgumentMatchers.eq("updatedAt"), any()
        );
        verify(programDocument).update(
                org.mockito.ArgumentMatchers.eq("programStatus"), org.mockito.ArgumentMatchers.eq("active"),
                org.mockito.ArgumentMatchers.eq("updatedAt"), any()
        );
    }

    @SuppressWarnings("unchecked")
    private void stubCollection(CollectionReference collection, List<QueryDocumentSnapshot> documents) throws Exception {
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        when(collection.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(documents);
    }

    private Program program(String programId, String programStatus, String category) {
        Program program = new Program();
        program.setProgramId(programId);
        program.setProgramStatus(programStatus);
        program.setCategory(category);
        return program;
    }

    private ProgramEnrollment enrollment(String programId) {
        ProgramEnrollment enrollment = new ProgramEnrollment();
        enrollment.setProgramId(programId);
        return enrollment;
    }

    private CredentialDefinition credentialDefinition(String credentialID, String programId) {
        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID(credentialID);
        definition.setProgramIds(List.of(programId));
        return definition;
    }

    private EarnedCredential earnedCredential(String credentialID) {
        EarnedCredential credential = new EarnedCredential();
        credential.setCredentialID(credentialID);
        return credential;
    }

    private AttendanceRecord attendanceRecord(String programId) {
        AttendanceRecord record = new AttendanceRecord();
        record.setProgramID(programId);
        return record;
    }

    private ServiceHourRecord serviceHourRecord(String programId, double hours) {
        ServiceHourRecord record = new ServiceHourRecord();
        record.setProgramId(programId);
        record.setHours(hours);
        return record;
    }
}
