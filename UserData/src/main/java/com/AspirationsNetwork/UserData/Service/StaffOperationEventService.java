package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.StaffOperationEvent;
import com.google.cloud.firestore.Firestore;
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

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
