package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ServiceHourRecordDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourTotalsDTO;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
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
public class ServiceHourService {
    public static final String COLLECTION_NAME = "serviceHourRecords";
    private static final Set<String> VALID_STATUSES = Set.of("pending", "verified", "rejected");

    private final Firestore firestore;

    public String createOrReviewServiceHourRecord(ServiceHourRecordDTO dto) throws Exception {
        requireText(dto.getUserUID(), "userUID is required");
        requireText(dto.getProgramId(), "programId is required");
        requireText(dto.getReviewedByStaffUID(), "reviewedByStaffUID is required");

        if (dto.getHours() < 0) {
            throw new IllegalArgumentException("hours must be zero or greater");
        }

        String status = normalizeStatus(dto.getVerificationStatus());
        String serviceHourRecordId = UUID.randomUUID().toString();
        Date now = new Date();

        ServiceHourRecord record = new ServiceHourRecord();
        record.setServiceHourRecordId(serviceHourRecordId);
        record.setUserUID(dto.getUserUID());
        record.setProgramId(dto.getProgramId());
        record.setServiceDate(dto.getServiceDate());
        record.setHours(dto.getHours());
        record.setDescription(dto.getDescription());
        record.setVerificationStatus(status);
        record.setVerificationSource(dto.getVerificationSource());
        record.setGoogleFormResponseUrl(dto.getGoogleFormResponseUrl());
        record.setReviewedByStaffUID(dto.getReviewedByStaffUID());
        record.setSubmittedAt(dto.getSubmittedAt() == null ? now : dto.getSubmittedAt());
        record.setReviewedAt(dto.getReviewedAt());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        firestore.collection(COLLECTION_NAME)
                .document(serviceHourRecordId)
                .set(record)
                .get();

        firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(dto.getUserUID())
                .update("serviceHourRecordIds", FieldValue.arrayUnion(serviceHourRecordId))
                .get();

        return serviceHourRecordId;
    }

    public List<ServiceHourRecord> getServiceHourRecordsForUser(String userUID)
            throws ExecutionException, InterruptedException {
        requireText(userUID, "userUID is required");

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userUID", userUID)
                .get();

        List<ServiceHourRecord> records = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            records.add(document.toObject(ServiceHourRecord.class));
        }
        return records;
    }

    public List<ServiceHourRecord> getServiceHourRecords(
            String userUID,
            String verificationStatus,
            String programId,
            Date serviceDate
    ) throws Exception {
        List<ServiceHourRecord> records = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        String normalizedStatus = verificationStatus == null || verificationStatus.isBlank()
                ? null
                : normalizeStatus(verificationStatus);

        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            ServiceHourRecord record = document.toObject(ServiceHourRecord.class);
            if (record != null && matchesFilters(record, userUID, normalizedStatus, programId, serviceDate)) {
                records.add(record);
            }
        }
        return records;
    }

    public ServiceHourTotalsDTO getServiceHourTotals(
            String userUID,
            String verificationStatus,
            String programId,
            Date serviceDate
    ) throws Exception {
        ServiceHourTotalsDTO totals = new ServiceHourTotalsDTO();
        for (ServiceHourRecord record : getServiceHourRecords(userUID, verificationStatus, programId, serviceDate)) {
            String status = record.getVerificationStatus() == null || record.getVerificationStatus().isBlank()
                    ? "pending"
                    : record.getVerificationStatus();
            totals.setTotalRecords(totals.getTotalRecords() + 1);
            totals.setTotalHours(totals.getTotalHours() + record.getHours());
            totals.getRecordsByStatus().merge(status, 1, Integer::sum);
            if ("verified".equals(status)) {
                totals.setVerifiedHours(totals.getVerifiedHours() + record.getHours());
            } else if ("rejected".equals(status)) {
                totals.setRejectedHours(totals.getRejectedHours() + record.getHours());
            } else {
                totals.setPendingHours(totals.getPendingHours() + record.getHours());
            }
        }
        return totals;
    }

    public void updateServiceHourRecordStatus(
            String serviceHourRecordId,
            String verificationStatus,
            String reviewedByStaffUID
    ) throws Exception {
        requireText(serviceHourRecordId, "serviceHourRecordId is required");
        requireText(reviewedByStaffUID, "reviewedByStaffUID is required");

        String status = normalizeStatus(verificationStatus);
        DocumentReference recordRef = firestore.collection(COLLECTION_NAME).document(serviceHourRecordId);
        if (!recordRef.get().get().exists()) {
            throw new IllegalArgumentException("Service-hour record does not exist");
        }

        Date now = new Date();
        Map<String, Object> updates = new HashMap<>();
        updates.put("verificationStatus", status);
        updates.put("reviewedByStaffUID", reviewedByStaffUID);
        updates.put("reviewedAt", now);
        updates.put("updatedAt", now);
        recordRef.update(updates).get();
    }

    public void approveServiceHourRecord(String serviceHourRecordId, String reviewedByStaffUID) throws Exception {
        updateServiceHourRecordStatus(serviceHourRecordId, "verified", reviewedByStaffUID);
    }

    public void rejectServiceHourRecord(String serviceHourRecordId, String reviewedByStaffUID) throws Exception {
        updateServiceHourRecordStatus(serviceHourRecordId, "rejected", reviewedByStaffUID);
    }

    public void deleteServiceHourRecord(String serviceHourRecordId) throws Exception {
        requireText(serviceHourRecordId, "serviceHourRecordId is required");

        DocumentReference recordRef = firestore.collection(COLLECTION_NAME).document(serviceHourRecordId);
        DocumentSnapshot recordSnapshot = recordRef.get().get();
        if (!recordSnapshot.exists()) {
            throw new IllegalArgumentException("Service-hour record does not exist");
        }

        ServiceHourRecord record = recordSnapshot.toObject(ServiceHourRecord.class);
        recordRef.delete().get();
        if (record != null && record.getUserUID() != null && !record.getUserUID().isBlank()) {
            firestore.collection(UserInfoService.COLLECTION_NAME)
                    .document(record.getUserUID())
                    .update("serviceHourRecordIds", FieldValue.arrayRemove(serviceHourRecordId))
                    .get();
        }
    }

    private boolean matchesFilters(
            ServiceHourRecord record,
            String userUID,
            String verificationStatus,
            String programId,
            Date serviceDate
    ) {
        if (userUID != null && !userUID.isBlank() && !userUID.equals(record.getUserUID())) {
            return false;
        }
        if (verificationStatus != null && !verificationStatus.equals(record.getVerificationStatus())) {
            return false;
        }
        if (programId != null && !programId.isBlank() && !programId.equals(record.getProgramId())) {
            return false;
        }
        return serviceDate == null || isSameDay(serviceDate, record.getServiceDate());
    }

    private boolean isSameDay(Date left, Date right) {
        if (left == null || right == null) {
            return false;
        }

        Calendar leftCalendar = Calendar.getInstance();
        leftCalendar.setTime(left);
        Calendar rightCalendar = Calendar.getInstance();
        rightCalendar.setTime(right);
        return leftCalendar.get(Calendar.YEAR) == rightCalendar.get(Calendar.YEAR)
                && leftCalendar.get(Calendar.DAY_OF_YEAR) == rightCalendar.get(Calendar.DAY_OF_YEAR);
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = status == null || status.isBlank()
                ? "pending"
                : status.toLowerCase();

        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("verificationStatus must be pending, verified, or rejected");
        }

        return normalizedStatus;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
