package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProgramEnrollmentServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void enrollYouthInProgramStoresEnrollmentAndLinksUserWhenProgramIsActive() throws Exception {
        Firestore firestore = mock(Firestore.class);
        ProgramService programService = mock(ProgramService.class);
        CollectionReference enrollmentCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        Query userQuery = mock(Query.class);
        Query programQuery = mock(Query.class);
        Query activeEnrollmentQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> duplicateFuture = mock(ApiFuture.class);
        QuerySnapshot duplicateSnapshot = mock(QuerySnapshot.class);
        DocumentReference enrollmentDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(programService.getActiveProgramById("program-123")).thenReturn(new Program());
        when(firestore.collection(ProgramEnrollmentService.COLLECTION_NAME)).thenReturn(enrollmentCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(enrollmentCollection.whereEqualTo("userUID", "youth-123")).thenReturn(userQuery);
        when(userQuery.whereEqualTo("programId", "program-123")).thenReturn(programQuery);
        when(programQuery.whereEqualTo("enrollmentStatus", "active")).thenReturn(activeEnrollmentQuery);
        when(activeEnrollmentQuery.get()).thenReturn(duplicateFuture);
        when(duplicateFuture.get()).thenReturn(duplicateSnapshot);
        when(duplicateSnapshot.getDocuments()).thenReturn(List.of());
        when(enrollmentCollection.document(any(String.class))).thenReturn(enrollmentDocument);
        when(enrollmentDocument.set(any(ProgramEnrollment.class))).thenReturn(writeFuture);
        when(usersCollection.document("youth-123")).thenReturn(userDocument);
        when(userDocument.update(
                eq("programIds"),
                any(),
                eq("programParticipationIds"),
                any()
        )).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        ProgramEnrollmentService service = new ProgramEnrollmentService(firestore, programService);
        String enrollmentId = service.enrollYouthInProgram("youth-123", "program-123");

        ArgumentCaptor<ProgramEnrollment> enrollmentCaptor = ArgumentCaptor.forClass(ProgramEnrollment.class);
        verify(enrollmentDocument).set(enrollmentCaptor.capture());
        verify(userDocument).update(eq("programIds"), any(), eq("programParticipationIds"), any());

        ProgramEnrollment savedEnrollment = enrollmentCaptor.getValue();
        assertEquals(enrollmentId, savedEnrollment.getEnrollmentId());
        assertEquals("youth-123", savedEnrollment.getUserUID());
        assertEquals("program-123", savedEnrollment.getProgramId());
        assertEquals("active", savedEnrollment.getEnrollmentStatus());
        assertTrue(savedEnrollment.isCreatedByUser());
    }

    @Test
    void enrollYouthInProgramRejectsArchivedOrMissingProgram() throws Exception {
        ProgramService programService = mock(ProgramService.class);
        when(programService.getActiveProgramById("archived-program")).thenReturn(null);

        ProgramEnrollmentService service = new ProgramEnrollmentService(mock(Firestore.class), programService);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enrollYouthInProgram("youth-123", "archived-program")
        );

        assertEquals("Youth users may enroll only into active programs", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void enrollYouthInProgramRejectsDuplicateActiveEnrollment() throws Exception {
        Firestore firestore = mock(Firestore.class);
        ProgramService programService = mock(ProgramService.class);
        CollectionReference enrollmentCollection = mock(CollectionReference.class);
        Query userQuery = mock(Query.class);
        Query programQuery = mock(Query.class);
        Query activeEnrollmentQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> duplicateFuture = mock(ApiFuture.class);
        QuerySnapshot duplicateSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot duplicateDocument = mock(QueryDocumentSnapshot.class);

        when(programService.getActiveProgramById("program-123")).thenReturn(new Program());
        when(firestore.collection(ProgramEnrollmentService.COLLECTION_NAME)).thenReturn(enrollmentCollection);
        when(enrollmentCollection.whereEqualTo("userUID", "youth-123")).thenReturn(userQuery);
        when(userQuery.whereEqualTo("programId", "program-123")).thenReturn(programQuery);
        when(programQuery.whereEqualTo("enrollmentStatus", "active")).thenReturn(activeEnrollmentQuery);
        when(activeEnrollmentQuery.get()).thenReturn(duplicateFuture);
        when(duplicateFuture.get()).thenReturn(duplicateSnapshot);
        when(duplicateSnapshot.getDocuments()).thenReturn(List.of(duplicateDocument));

        ProgramEnrollmentService service = new ProgramEnrollmentService(firestore, programService);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.enrollYouthInProgram("youth-123", "program-123")
        );

        assertEquals("User is already enrolled in this program", exception.getMessage());
    }
}
