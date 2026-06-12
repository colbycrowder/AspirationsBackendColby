package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.StaffOperationReportingDTO;
import com.AspirationsNetwork.UserData.Models.StaffOperationEvent;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffOperationEventService {
    public static final String COLLECTION_NAME = "staffOperationEvents";

    public static final String ATTENDANCE_RECORDED = "attendance_recorded";
    public static final String CREDENTIAL_AWARDED = "credential_awarded";
    public static final String SERVICE_HOUR_REVIEWED = "service_hour_reviewed";
    public static final String PROGRAM_CREATED = "program_created";
    public static final String PROGRAM_UPDATED = "program_updated";
    public static final String PROGRAM_ARCHIVED = "program_archived";
    public static final String EXTERNAL_DATASET_CREATED = "external_dataset_created";
    public static final String PARTICIPANT_EXTERNAL_LINK_CREATED = "participant_external_link_created";
    public static final String YOUTH_PROFILE_REVIEWED = "youth_profile_reviewed";

    private final Firestore firestore;

    public String trackOperation(
            String staffUID,
            String operationType,
            String targetType,
            String targetId,
            String targetUserUID,
            Map<String, Object> metadata
    ) throws Exception {
        requireText(staffUID, "staffUID is required");
        requireText(operationType, "operationType is required");

        String eventId = UUID.randomUUID().toString();
        StaffOperationEvent event = new StaffOperationEvent();
        event.setStaffOperationEventId(eventId);
        event.setStaffUID(staffUID);
        event.setOperationType(operationType);
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setTargetUserUID(targetUserUID);
        event.setCreatedAt(new Date());
        event.setMetadata(metadata == null ? new HashMap<>() : new HashMap<>(metadata));

        firestore.collection(COLLECTION_NAME)
                .document(eventId)
                .set(event)
                .get();

        return eventId;
    }

    public void trackOperationSafely(
            String staffUID,
            String operationType,
            String targetType,
            String targetId,
            String targetUserUID,
            Map<String, Object> metadata
    ) {
        try {
            trackOperation(staffUID, operationType, targetType, targetId, targetUserUID, metadata);
        } catch (Exception e) {
            System.err.println("Staff operation tracking failed: " + e.getMessage());
        }
    }

    public StaffOperationReportingDTO buildOperationReport() throws Exception {
        StaffOperationReportingDTO report = new StaffOperationReportingDTO();
        Date now = new Date();
        Date thirtyDaysAgo = daysAgo(now, 30);
        Date sixtyDaysAgo = daysAgo(now, 60);
        Date ninetyDaysAgo = daysAgo(now, 90);

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            StaffOperationEvent event = document.toObject(StaffOperationEvent.class);
            if (event == null) {
                continue;
            }

            report.setTotalOperations(report.getTotalOperations() + 1);
            increment(report.getOperationsByType(), normalizeGroupValue(event.getOperationType()));
            increment(report.getOperationsByStaffUser(), normalizeGroupValue(event.getStaffUID()));
            increment(report.getOperationsByTargetType(), normalizeGroupValue(event.getTargetType()));

            Date createdAt = event.getCreatedAt();
            if (createdAt == null) {
                continue;
            }
            if (!createdAt.before(thirtyDaysAgo)) {
                report.setOperationsLast30Days(report.getOperationsLast30Days() + 1);
            }
            if (!createdAt.before(sixtyDaysAgo)) {
                report.setOperationsLast60Days(report.getOperationsLast60Days() + 1);
            }
            if (!createdAt.before(ninetyDaysAgo)) {
                report.setOperationsLast90Days(report.getOperationsLast90Days() + 1);
            }
        }

        return report;
    }

    private void increment(Map<String, Integer> counts, String key) {
        counts.merge(key, 1, Integer::sum);
    }

    private Date daysAgo(Date now, int days) {
        long millisPerDay = 24L * 60L * 60L * 1000L;
        return new Date(now.getTime() - days * millisPerDay);
    }

    private String normalizeGroupValue(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
