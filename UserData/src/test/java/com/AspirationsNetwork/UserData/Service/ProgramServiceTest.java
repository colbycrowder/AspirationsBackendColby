package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ProgramDTO;
import com.AspirationsNetwork.UserData.Models.Program;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
}
