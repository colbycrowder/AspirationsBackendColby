package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.PlatformEvent;
import com.AspirationsNetwork.UserData.Models.PlatformEventType;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlatformEventService {
    public static final String COLLECTION_NAME = "platformEvents";

    private final Firestore firestore;
    private final UserInfoService userInfoService;

    public String trackEventForUser(String userUID, PlatformEventType eventType) throws Exception {
        return trackEventForUser(userUID, eventType, new HashMap<>());
    }

    public String trackEventForUser(
            String userUID,
            PlatformEventType eventType,
            Map<String, Object> metadata
    ) throws Exception {
        requireText(userUID, "userUID is required");
        if (eventType == null) {
            throw new IllegalArgumentException("eventType is required");
        }

        User user = userInfoService.getUser(userUID);
        if (user == null) {
            throw new IllegalArgumentException("User profile is required before platform events can be tracked");
        }
        requireText(user.getAspnParticipantId(), "aspnParticipantId is required before platform events can be tracked");

        String eventId = UUID.randomUUID().toString();
        PlatformEvent event = new PlatformEvent();
        event.setEventId(eventId);
        event.setUserUID(userUID);
        event.setAspnParticipantId(user.getAspnParticipantId());
        event.setEventType(eventType.name());
        event.setEventTimestamp(new Date());
        event.setMetadata(metadata == null ? new HashMap<>() : metadata);

        firestore.collection(COLLECTION_NAME)
                .document(eventId)
                .set(event)
                .get();

        return eventId;
    }

    public void trackEventSafely(String userUID, PlatformEventType eventType) {
        trackEventSafely(userUID, eventType, new HashMap<>());
    }

    public void trackEventSafely(
            String userUID,
            PlatformEventType eventType,
            Map<String, Object> metadata
    ) {
        try {
            trackEventForUser(userUID, eventType, metadata);
        } catch (Exception e) {
            System.err.println("Platform event tracking failed: " + e.getMessage());
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
