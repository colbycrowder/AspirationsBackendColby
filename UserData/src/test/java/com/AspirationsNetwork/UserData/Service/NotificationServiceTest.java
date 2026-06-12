package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.Notification;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createCredentialEarnedNotificationStoresUnreadNotification() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference notificationsCollection = mock(CollectionReference.class);
        CollectionReference definitionsCollection = mock(CollectionReference.class);
        DocumentReference notificationDocument = mock(DocumentReference.class);
        DocumentReference definitionDocument = mock(DocumentReference.class);
        DocumentSnapshot definitionSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> definitionFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialName("Civic Leadership");

        when(firestore.collection(NotificationService.COLLECTION_NAME)).thenReturn(notificationsCollection);
        when(firestore.collection(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION)).thenReturn(definitionsCollection);
        when(notificationsCollection.document(any(String.class))).thenReturn(notificationDocument);
        when(definitionsCollection.document("credential-123")).thenReturn(definitionDocument);
        when(definitionDocument.get()).thenReturn(definitionFuture);
        when(definitionFuture.get()).thenReturn(definitionSnapshot);
        when(definitionSnapshot.exists()).thenReturn(true);
        when(definitionSnapshot.toObject(CredentialDefinition.class)).thenReturn(definition);
        when(notificationDocument.set(any(Notification.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        NotificationService service = new NotificationService(firestore, mock(PlatformEventService.class));
        String notificationId = service.createCredentialEarnedNotification(
                "youth-123",
                "credential-123",
                "earned-123"
        );

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDocument).set(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertEquals(notificationId, notification.getNotificationId());
        assertEquals("youth-123", notification.getUserUID());
        assertEquals(NotificationService.CREDENTIAL_EARNED_TYPE, notification.getNotificationType());
        assertEquals("Credential earned", notification.getTitle());
        assertEquals("You earned Civic Leadership.", notification.getMessage());
        assertEquals("credential-123", notification.getRelatedCredentialId());
        assertEquals("earned-123", notification.getRelatedEarnedCredentialId());
        assertEquals(false, notification.isRead());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUnreadNotificationCountCountsOnlyUnreadUserNotifications() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference notificationsCollection = mock(CollectionReference.class);
        Query userQuery = mock(Query.class);
        Query unreadQuery = mock(Query.class);
        QuerySnapshot unreadSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot notificationDocumentOne = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot notificationDocumentTwo = mock(QueryDocumentSnapshot.class);
        ApiFuture<QuerySnapshot> unreadFuture = mock(ApiFuture.class);

        when(firestore.collection(NotificationService.COLLECTION_NAME)).thenReturn(notificationsCollection);
        when(notificationsCollection.whereEqualTo("userUID", "youth-123")).thenReturn(userQuery);
        when(userQuery.whereEqualTo("read", false)).thenReturn(unreadQuery);
        when(unreadQuery.get()).thenReturn(unreadFuture);
        when(unreadFuture.get()).thenReturn(unreadSnapshot);
        when(unreadSnapshot.getDocuments()).thenReturn(List.of(notificationDocumentOne, notificationDocumentTwo));

        NotificationService service = new NotificationService(firestore, mock(PlatformEventService.class));

        assertEquals(2, service.getUnreadNotificationCount("youth-123"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getNotificationsForUserReturnsNewestFirst() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference notificationsCollection = mock(CollectionReference.class);
        Query userQuery = mock(Query.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot oldDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot newDocument = mock(QueryDocumentSnapshot.class);
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);

        Notification oldNotification = new Notification();
        oldNotification.setNotificationId("old");
        oldNotification.setCreatedAt(new Date(1000));
        Notification newNotification = new Notification();
        newNotification.setNotificationId("new");
        newNotification.setCreatedAt(new Date(2000));

        when(firestore.collection(NotificationService.COLLECTION_NAME)).thenReturn(notificationsCollection);
        when(notificationsCollection.whereEqualTo("userUID", "youth-123")).thenReturn(userQuery);
        when(userQuery.get()).thenReturn(future);
        when(future.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(List.of(oldDocument, newDocument));
        when(oldDocument.toObject(Notification.class)).thenReturn(oldNotification);
        when(newDocument.toObject(Notification.class)).thenReturn(newNotification);

        NotificationService service = new NotificationService(firestore, mock(PlatformEventService.class));
        List<Notification> notifications = service.getNotificationsForUser("youth-123");

        assertEquals("new", notifications.get(0).getNotificationId());
        assertEquals("old", notifications.get(1).getNotificationId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void markNotificationAsReadRequiresNotificationOwnership() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference notificationsCollection = mock(CollectionReference.class);
        DocumentReference notificationDocument = mock(DocumentReference.class);
        DocumentSnapshot notificationSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> notificationFuture = mock(ApiFuture.class);

        when(firestore.collection(NotificationService.COLLECTION_NAME)).thenReturn(notificationsCollection);
        when(notificationsCollection.document("notification-123")).thenReturn(notificationDocument);
        when(notificationDocument.get()).thenReturn(notificationFuture);
        when(notificationFuture.get()).thenReturn(notificationSnapshot);
        when(notificationSnapshot.exists()).thenReturn(true);
        when(notificationSnapshot.getString("userUID")).thenReturn("other-user");

        NotificationService service = new NotificationService(firestore, mock(PlatformEventService.class));

        assertThrows(
                ForbiddenAccessException.class,
                () -> service.markNotificationAsRead("youth-123", "notification-123")
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void markNotificationAsReadUpdatesOwnedNotification() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference notificationsCollection = mock(CollectionReference.class);
        DocumentReference notificationDocument = mock(DocumentReference.class);
        DocumentSnapshot notificationSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> notificationFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(NotificationService.COLLECTION_NAME)).thenReturn(notificationsCollection);
        when(notificationsCollection.document("notification-123")).thenReturn(notificationDocument);
        when(notificationDocument.get()).thenReturn(notificationFuture);
        when(notificationFuture.get()).thenReturn(notificationSnapshot);
        when(notificationSnapshot.exists()).thenReturn(true);
        when(notificationSnapshot.getString("userUID")).thenReturn("youth-123");
        when(notificationDocument.update(eq("read"), eq(true))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        NotificationService service = new NotificationService(firestore, mock(PlatformEventService.class));
        service.markNotificationAsRead("youth-123", "notification-123");

        verify(notificationDocument).update("read", true);
    }
}
