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
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

    public List<Program> getPrograms(Boolean active, String programType) throws Exception {
        List<Program> programs = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            Program program = document.toObject(Program.class);
            if (program != null && matchesProgramFilters(program, active, programType)) {
                programs.add(program);
            }
        }
        return programs;
    }

    public ProgramDetailDTO getProgramDetail(String programId) throws Exception {
        Program program = getProgramById(programId);
        if (program == null) {
            return null;
        }

        ProgramDetailDTO detail = new ProgramDetailDTO();
        detail.setProgram(program);
        detail.setEnrollmentCount(countEnrollmentsForProgram(programId));
        detail.setCredentialCount(countCredentialsEarnedForProgram(programId));
        detail.setAttendanceCount(countAttendanceRecordsForProgram(programId));
        ServiceHourStats serviceHourStats = getServiceHourStatsForProgram(programId);
        detail.setServiceHourRecordCount(serviceHourStats.recordCount());
        detail.setServiceHourTotal(serviceHourStats.totalHours());
        return detail;
    }

    public ProgramTotalsDTO getProgramTotals() throws Exception {
        ProgramTotalsDTO totals = new ProgramTotalsDTO();
        for (Program program : getPrograms(null, null)) {
            totals.setTotalPrograms(totals.getTotalPrograms() + 1);
            String status = normalizeExistingStatus(program.getProgramStatus());
            totals.getProgramsByStatus().merge(status, 1L, Long::sum);
            if ("active".equals(status)) {
                totals.setActivePrograms(totals.getActivePrograms() + 1);
            } else if ("archived".equals(status)) {
                totals.setArchivedPrograms(totals.getArchivedPrograms() + 1);
            }
        }
        totals.setTotalEnrollments(countAllEnrollments());
        totals.setTotalCredentialsEarned(countAllEarnedCredentials());
        totals.setTotalAttendanceRecords(countAllAttendanceRecords());
        totals.setTotalServiceHours(sumAllServiceHours());
        return totals;
    }

    public void archiveProgram(String programId) throws Exception {
        setProgramStatus(programId, "archived");
    }

    public void restoreProgram(String programId) throws Exception {
        setProgramStatus(programId, "active");
    }

    private Program getProgramById(String programId) throws Exception {
        requireText(programId, "programId is required");

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(programId)
                .get()
                .get();
        if (!document.exists()) {
            return null;
        }
        return document.toObject(Program.class);
    }

    private void setProgramStatus(String programId, String status) throws Exception {
        requireText(programId, "programId is required");
        String normalizedStatus = normalizeStatus(status);
        if (getProgramById(programId) == null) {
            throw new IllegalArgumentException("Program does not exist");
        }

        firestore.collection(COLLECTION_NAME)
                .document(programId)
                .update(
                        "programStatus", normalizedStatus,
                        "updatedAt", new Date()
                )
                .get();
    }

    private boolean matchesProgramFilters(Program program, Boolean active, String programType) {
        if (active != null) {
            String expectedStatus = active ? "active" : "archived";
            if (!expectedStatus.equals(normalizeExistingStatus(program.getProgramStatus()))) {
                return false;
            }
        }
        return programType == null || programType.isBlank()
                || programType.equalsIgnoreCase(program.getCategory());
    }

    private long countEnrollmentsForProgram(String programId) throws Exception {
        long count = 0;
        ApiFuture<QuerySnapshot> future = firestore.collection(ProgramEnrollmentService.COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            ProgramEnrollment enrollment = document.toObject(ProgramEnrollment.class);
            if (enrollment != null && programId.equals(enrollment.getProgramId())) {
                count++;
            }
        }
        return count;
    }

    private long countCredentialsEarnedForProgram(String programId) throws Exception {
        Set<String> credentialIds = getCredentialDefinitionIdsForProgram(programId);
        long count = 0;
        ApiFuture<QuerySnapshot> future = firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            EarnedCredential credential = document.toObject(EarnedCredential.class);
            if (credential != null && credentialIds.contains(credential.getCredentialID())) {
                count++;
            }
        }
        return count;
    }

    private Set<String> getCredentialDefinitionIdsForProgram(String programId) throws Exception {
        Set<String> credentialIds = new HashSet<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            CredentialDefinition definition = document.toObject(CredentialDefinition.class);
            if (definition != null
                    && definition.getProgramIds() != null
                    && definition.getProgramIds().contains(programId)) {
                credentialIds.add(definition.getCredentialID());
            }
        }
        return credentialIds;
    }

    private long countAttendanceRecordsForProgram(String programId) throws Exception {
        long count = 0;
        ApiFuture<QuerySnapshot> future = firestore.collection(AttendanceService.COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            AttendanceRecord record = document.toObject(AttendanceRecord.class);
            if (record != null && programId.equals(record.getProgramID())) {
                count++;
            }
        }
        return count;
    }

    private ServiceHourStats getServiceHourStatsForProgram(String programId) throws Exception {
        long recordCount = 0;
        double totalHours = 0.0;
        ApiFuture<QuerySnapshot> future = firestore.collection(ServiceHourService.COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            ServiceHourRecord record = document.toObject(ServiceHourRecord.class);
            if (record != null && programId.equals(record.getProgramId())) {
                recordCount++;
                totalHours += record.getHours();
            }
        }
        return new ServiceHourStats(recordCount, totalHours);
    }

    private long countAllEnrollments() throws Exception {
        return firestore.collection(ProgramEnrollmentService.COLLECTION_NAME).get().get().getDocuments().size();
    }

    private long countAllEarnedCredentials() throws Exception {
        return firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION).get().get().getDocuments().size();
    }

    private long countAllAttendanceRecords() throws Exception {
        return firestore.collection(AttendanceService.COLLECTION_NAME).get().get().getDocuments().size();
    }

    private double sumAllServiceHours() throws Exception {
        double totalHours = 0.0;
        ApiFuture<QuerySnapshot> future = firestore.collection(ServiceHourService.COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            ServiceHourRecord record = document.toObject(ServiceHourRecord.class);
            if (record != null) {
                totalHours += record.getHours();
            }
        }
        return totalHours;
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

    private String normalizeExistingStatus(String status) {
        return status == null || status.isBlank() ? "active" : status.toLowerCase();
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

    private record ServiceHourStats(long recordCount, double totalHours) {
    }
}
