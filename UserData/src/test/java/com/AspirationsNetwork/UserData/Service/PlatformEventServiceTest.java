package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.PlatformEvent;
import com.AspirationsNetwork.UserData.Models.PlatformEventType;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformEventServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void trackEventPersistsParticipantEventWithTimestampAndMetadata() throws Exception {
        Firestore firestore = mock(Firestore.class);
        UserInfoService userInfoService = mock(UserInfoService.class);
        CollectionReference eventsCollection = mock(CollectionReference.class);
        DocumentReference eventDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        User user = new User();
        user.setUid("youth-123");
        user.setAspnParticipantId("ASPN-2026-0001");

        when(userInfoService.getUser("youth-123")).thenReturn(user);
        when(firestore.collection(PlatformEventService.COLLECTION_NAME)).thenReturn(eventsCollection);
        when(eventsCollection.document(any(String.class))).thenReturn(eventDocument);
        when(eventDocument.set(any(PlatformEvent.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        PlatformEventService service = new PlatformEventService(firestore, userInfoService);
        String eventId = service.trackEventForUser(
                "youth-123",
                PlatformEventType.PROGRAM_ENROLLED,
                Map.of("programId", "program-123")
        );

        ArgumentCaptor<PlatformEvent> eventCaptor = ArgumentCaptor.forClass(PlatformEvent.class);
        verify(eventDocument).set(eventCaptor.capture());

        PlatformEvent event = eventCaptor.getValue();
        assertEquals(eventId, event.getEventId());
        assertEquals("youth-123", event.getUserUID());
        assertEquals("ASPN-2026-0001", event.getAspnParticipantId());
        assertEquals("PROGRAM_ENROLLED", event.getEventType());
        assertEquals("program-123", event.getMetadata().get("programId"));
        assertNotNull(event.getEventTimestamp());
    }

    @Test
    void trackEventRequiresParticipantId() throws Exception {
        Firestore firestore = mock(Firestore.class);
        UserInfoService userInfoService = mock(UserInfoService.class);
        User user = new User();
        user.setUid("youth-123");
        when(userInfoService.getUser("youth-123")).thenReturn(user);

        PlatformEventService service = new PlatformEventService(firestore, userInfoService);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.trackEventForUser("youth-123", PlatformEventType.LOGIN)
        );
    }
}
