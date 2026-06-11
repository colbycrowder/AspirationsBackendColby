package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.AttendanceRecordCreationDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    public static final String COLLECTION_NAME = "attendanceRecords";
    private static final Set<String> VALID_STATUSES = Set.of("present", "absent", "excused", "pending");

    private final Firestore firestore;
    private final CredentialService credentialService;

    public String createAttendanceRecord(AttendanceRecordCreationDTO dto) throws Exception {
        requireText(dto.getUserUID(), "userUID is required");
        requireText(dto.getProgramID(), "programID is required");
        requireText(dto.getEventName(), "eventName is required");
        requireText(dto.getStaffRecorderUID(), "staffRecorderUID is required");

        String status = normalizeStatus(dto.getAttendanceStatus());
        String attendanceRecordID = UUID.randomUUID().toString();
        Date now = new Date();

        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceRecordID(attendanceRecordID);
        record.setUserUID(dto.getUserUID());
        record.setProgramID(dto.getProgramID());
        record.setEventName(dto.getEventName());
        record.setEventDate(dto.getEventDate());
        record.setAttendanceStatus(status);
        record.setStaffRecorderUID(dto.getStaffRecorderUID());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        firestore.collection(COLLECTION_NAME)
                .document(attendanceRecordID)
                .set(record)
                .get();

        firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(dto.getUserUID())
                .update("attendanceRecordIds", FieldValue.arrayUnion(attendanceRecordID))
                .get();

        if ("present".equals(status)) {
            evaluateAttendanceAutoAwards(dto);
        }

        return attendanceRecordID;
    }

    public List<AttendanceRecord> getAttendanceRecordsForUser(String userUID) throws ExecutionException, InterruptedException {
        requireText(userUID, "userUID is required");

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userUID", userUID)
                .get();

        List<AttendanceRecord> records = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            records.add(document.toObject(AttendanceRecord.class));
        }
        return records;
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = status == null || status.isBlank()
                ? "pending"
                : status.toLowerCase();

        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("attendanceStatus must be present, absent, excused, or pending");
        }

        return normalizedStatus;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void evaluateAttendanceAutoAwards(AttendanceRecordCreationDTO dto) {
        try {
            credentialService.evaluateAttendanceAutoAwards(
                    dto.getUserUID(),
                    dto.getProgramID(),
                    dto.getStaffRecorderUID()
            );
        } catch (Exception e) {
            System.err.println("Attendance auto-award evaluation failed: " + e.getMessage());
        }
    }
}
