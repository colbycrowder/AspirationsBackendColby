package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.AttendanceRecordCreationDTO;
import com.AspirationsNetwork.UserData.DTO.AttendanceTotalsDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<AttendanceRecord> getAttendanceRecords(String userUID, String programID, Date eventDate)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<AttendanceRecord> records = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            AttendanceRecord record = document.toObject(AttendanceRecord.class);
            if (record != null && matchesFilters(record, userUID, programID, eventDate)) {
                records.add(record);
            }
        }
        return records;
    }

    public AttendanceTotalsDTO getAttendanceTotals(String userUID, String programID, Date eventDate)
            throws ExecutionException, InterruptedException {
        AttendanceTotalsDTO totals = new AttendanceTotalsDTO();
        for (AttendanceRecord record : getAttendanceRecords(userUID, programID, eventDate)) {
            totals.setTotalRecords(totals.getTotalRecords() + 1);
            switch (normalizeStatus(record.getAttendanceStatus())) {
                case "present" -> totals.setPresent(totals.getPresent() + 1);
                case "absent" -> totals.setAbsent(totals.getAbsent() + 1);
                case "excused" -> totals.setExcused(totals.getExcused() + 1);
                case "pending" -> totals.setPending(totals.getPending() + 1);
                default -> {
                }
            }
        }
        return totals;
    }

    public void updateAttendanceRecord(String attendanceRecordID, AttendanceRecordCreationDTO dto) throws Exception {
        requireText(attendanceRecordID, "attendanceRecordID is required");
        if (dto == null) {
            throw new IllegalArgumentException("attendance record update request is required");
        }
        requireText(dto.getStaffRecorderUID(), "staffRecorderUID is required");

        DocumentReference recordRef = firestore.collection(COLLECTION_NAME).document(attendanceRecordID);
        DocumentSnapshot document = recordRef.get().get();
        if (!document.exists()) {
            throw new IllegalArgumentException("Attendance record does not exist");
        }

        AttendanceRecord existingRecord = document.toObject(AttendanceRecord.class);
        if (existingRecord == null) {
            throw new IllegalArgumentException("Attendance record does not exist");
        }
        if (hasText(dto.getUserUID()) && !dto.getUserUID().equals(existingRecord.getUserUID())) {
            throw new IllegalArgumentException("userUID cannot be changed for an attendance record");
        }

        Map<String, Object> updates = new HashMap<>();
        addIfPresent(updates, "programID", dto.getProgramID());
        addIfPresent(updates, "eventName", dto.getEventName());
        if (dto.getEventDate() != null) {
            updates.put("eventDate", dto.getEventDate());
        }
        if (hasText(dto.getAttendanceStatus())) {
            updates.put("attendanceStatus", normalizeStatus(dto.getAttendanceStatus()));
        }
        updates.put("staffRecorderUID", dto.getStaffRecorderUID());
        updates.put("updatedAt", new Date());

        recordRef.update(updates).get();

        String updatedStatus = (String) updates.get("attendanceStatus");
        String programID = hasText(dto.getProgramID()) ? dto.getProgramID() : existingRecord.getProgramID();
        if ("present".equals(updatedStatus)) {
            evaluateAttendanceAutoAwards(existingRecord.getUserUID(), programID, dto.getStaffRecorderUID());
        }
    }

    public void deleteAttendanceRecord(String attendanceRecordID) throws Exception {
        requireText(attendanceRecordID, "attendanceRecordID is required");

        DocumentReference recordRef = firestore.collection(COLLECTION_NAME).document(attendanceRecordID);
        DocumentSnapshot document = recordRef.get().get();
        if (!document.exists()) {
            throw new IllegalArgumentException("Attendance record does not exist");
        }

        AttendanceRecord existingRecord = document.toObject(AttendanceRecord.class);
        if (existingRecord == null) {
            throw new IllegalArgumentException("Attendance record does not exist");
        }

        recordRef.delete().get();

        firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(existingRecord.getUserUID())
                .update("attendanceRecordIds", FieldValue.arrayRemove(attendanceRecordID))
                .get();
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

    private void addIfPresent(Map<String, Object> updates, String fieldName, String value) {
        if (hasText(value)) {
            updates.put(fieldName, value);
        }
    }

    private boolean matchesFilters(AttendanceRecord record, String userUID, String programID, Date eventDate) {
        if (hasText(userUID) && !userUID.equals(record.getUserUID())) {
            return false;
        }
        if (hasText(programID) && !programID.equals(record.getProgramID())) {
            return false;
        }
        return eventDate == null || sameDay(eventDate, record.getEventDate());
    }

    private boolean sameDay(Date expected, Date actual) {
        if (actual == null) {
            return false;
        }
        Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.setTime(expected);
        Calendar actualCalendar = Calendar.getInstance();
        actualCalendar.setTime(actual);
        return expectedCalendar.get(Calendar.YEAR) == actualCalendar.get(Calendar.YEAR)
                && expectedCalendar.get(Calendar.DAY_OF_YEAR) == actualCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void evaluateAttendanceAutoAwards(AttendanceRecordCreationDTO dto) {
        evaluateAttendanceAutoAwards(dto.getUserUID(), dto.getProgramID(), dto.getStaffRecorderUID());
    }

    private void evaluateAttendanceAutoAwards(String userUID, String programID, String staffRecorderUID) {
        try {
            credentialService.evaluateAttendanceAutoAwards(
                    userUID,
                    programID,
                    staffRecorderUID
            );
        } catch (Exception e) {
            System.err.println("Attendance auto-award evaluation failed: " + e.getMessage());
        }
    }
}
