package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ProgramDTO;
import com.AspirationsNetwork.UserData.Models.Program;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ProgramService {
    public static final String COLLECTION_NAME = "programs";
    private static final Set<String> VALID_STATUSES = Set.of("active", "archived");

    private final Firestore firestore;

    public String createProgram(ProgramDTO dto) throws Exception {
        requireText(dto.getProgramName(), "programName is required");
        requireText(dto.getCreatedByStaffUID(), "createdByStaffUID is required");

        int capacity = normalizeCapacity(dto.getCapacity());
        String status = normalizeStatus(dto.getProgramStatus());
        String programId = UUID.randomUUID().toString();
        Date now = new Date();

        Program program = new Program();
        program.setProgramId(programId);
        program.setProgramName(dto.getProgramName());
        program.setDescription(dto.getDescription());
        program.setStartDate(dto.getStartDate());
        program.setEndDate(dto.getEndDate());
        program.setCategory(dto.getCategory());
        program.setProgramImageUrl(dto.getProgramImageUrl());
        program.setProgramLeader(dto.getProgramLeader());
        program.setCapacity(capacity);
        program.setProgramStatus(status);
        program.setCreatedByStaffUID(dto.getCreatedByStaffUID());
        program.setCreatedAt(now);
        program.setUpdatedAt(now);

        firestore.collection(COLLECTION_NAME)
                .document(programId)
                .set(program)
                .get();

        return programId;
    }

    public void updateProgram(String programId, ProgramDTO dto) throws Exception {
        requireText(programId, "programId is required");

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(programId)
                .get()
                .get();

        if (!document.exists()) {
            throw new IllegalArgumentException("Program does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        addIfPresent(updates, "programName", dto.getProgramName());
        addIfPresent(updates, "description", dto.getDescription());
        addIfPresent(updates, "startDate", dto.getStartDate());
        addIfPresent(updates, "endDate", dto.getEndDate());
        addIfPresent(updates, "category", dto.getCategory());
        addIfPresent(updates, "programImageUrl", dto.getProgramImageUrl());
        addIfPresent(updates, "programLeader", dto.getProgramLeader());

        if (dto.getCapacity() != null) {
            updates.put("capacity", normalizeCapacity(dto.getCapacity()));
        }

        if (dto.getProgramStatus() != null) {
            updates.put("programStatus", normalizeStatus(dto.getProgramStatus()));
        }

        updates.put("updatedAt", new Date());

        firestore.collection(COLLECTION_NAME)
                .document(programId)
                .update(updates)
                .get();
    }

    public List<Program> getActivePrograms() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("programStatus", "active")
                .get();

        List<Program> programs = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            programs.add(document.toObject(Program.class));
        }
        return programs;
    }

    public Program getActiveProgramById(String programId) throws ExecutionException, InterruptedException {
        requireText(programId, "programId is required");

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(programId)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        Program program = document.toObject(Program.class);
        if (program == null || !"active".equals(program.getProgramStatus())) {
            return null;
        }
        return program;
    }

    private int normalizeCapacity(Integer capacity) {
        if (capacity == null) {
            return 0;
        }

        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be zero or greater");
        }

        return capacity;
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = status == null || status.isBlank()
                ? "active"
                : status.toLowerCase();

        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("programStatus must be active or archived");
        }

        return normalizedStatus;
    }

    private void addIfPresent(Map<String, Object> updates, String fieldName, Object value) {
        if (value != null) {
            updates.put(fieldName, value);
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
