package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ServiceHourRecordDTO;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
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
