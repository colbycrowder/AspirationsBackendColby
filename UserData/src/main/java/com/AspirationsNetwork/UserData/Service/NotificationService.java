package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.Notification;
import com.AspirationsNetwork.UserData.Models.PlatformEventType;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    public static final String COLLECTION_NAME = "notifications";
    public static final String CREDENTIAL_EARNED_TYPE = "credential_earned";

    private final Firestore firestore;
    private final PlatformEventService platformEventService;

    public String createCredentialEarnedNotification(
            String userUID,
            String credentialID,
            String earnedCredentialID
    ) throws Exception {
        requireText(userUID, "userUID is required");
        requireText(credentialID, "credentialID is required");
        requireText(earnedCredentialID, "earnedCredentialID is required");

        String notificationId = UUID.randomUUID().toString();
        CredentialDefinition definition = getCredentialDefinition(credentialID);
        String credentialName = definition == null || definition.getCredentialName() == null
                || definition.getCredentialName().isBlank()
                ? "credential"
                : definition.getCredentialName();

        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setUserUID(userUID);
        notification.setNotificationType(CREDENTIAL_EARNED_TYPE);
        notification.setTitle("Credential earned");
        notification.setMessage("You earned " + credentialName + ".");
        notification.setRelatedCredentialId(credentialID);
        notification.setRelatedEarnedCredentialId(earnedCredentialID);
        notification.setRead(false);
        notification.setCreatedAt(new Date());

        firestore.collection(COLLECTION_NAME)
                .document(notificationId)
                .set(notification)
                .get();

        return notificationId;
    }

    public List<Notification> getNotificationsForUser(String userUID) throws Exception {
        requireText(userUID, "userUID is required");

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userUID", userUID)
                .get();

        List<Notification> notifications = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            Notification notification = document.toObject(Notification.class);
            if (notification != null) {
                notifications.add(notification);
            }
        }

        notifications.sort(Comparator.comparing(
                Notification::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));
        return notifications;
    }

    public int getUnreadNotificationCount(String userUID) throws Exception {
        requireText(userUID, "userUID is required");

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userUID", userUID)
                .whereEqualTo("read", false)
                .get();

        return future.get().getDocuments().size();
    }

    public void markNotificationAsRead(String userUID, String notificationId) throws Exception {
        requireText(userUID, "userUID is required");
        requireText(notificationId, "notificationId is required");

        DocumentReference notificationRef = firestore.collection(COLLECTION_NAME).document(notificationId);
        DocumentSnapshot notificationDocument = notificationRef.get().get();
        if (!notificationDocument.exists()) {
            throw new IllegalArgumentException("Notification does not exist");
        }

        String ownerUID = notificationDocument.getString("userUID");
        if (!userUID.equals(ownerUID)) {
            throw new ForbiddenAccessException("Notification does not belong to signed-in user");
        }

        Boolean alreadyRead = notificationDocument.getBoolean("read");
        notificationRef.update("read", true).get();
        if (!Boolean.TRUE.equals(alreadyRead)) {
            platformEventService.trackEventSafely(
                    userUID,
                    PlatformEventType.NOTIFICATION_VIEWED,
                    java.util.Map.of("notificationId", notificationId)
            );
        }
    }

    private CredentialDefinition getCredentialDefinition(String credentialID) throws Exception {
        DocumentSnapshot definitionDocument = firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)
                .document(credentialID)
                .get()
                .get();

        if (!definitionDocument.exists()) {
            return null;
        }
        return definitionDocument.toObject(CredentialDefinition.class);
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
