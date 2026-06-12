package com.AspirationsNetwork.UserData.Service;

import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ParticipantIdServiceTest {

    @Test
    void formatParticipantIdUsesRequiredCohortSequenceFormat() {
        ParticipantIdService service = new ParticipantIdService(mock(Firestore.class));

        assertEquals("ASPN-2026-0001", service.formatParticipantId(1));
        assertEquals("ASPN-2026-0012", service.formatParticipantId(12));
        assertEquals("ASPN-2026-1234", service.formatParticipantId(1234));
    }

    @Test
    void formatParticipantIdRejectsInvalidSequence() {
        ParticipantIdService service = new ParticipantIdService(mock(Firestore.class));

        assertThrows(IllegalArgumentException.class, () -> service.formatParticipantId(0));
    }
}
