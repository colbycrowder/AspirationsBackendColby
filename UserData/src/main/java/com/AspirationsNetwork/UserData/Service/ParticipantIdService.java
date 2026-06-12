package com.AspirationsNetwork.UserData.Service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParticipantIdService {
    public static final String COHORT_YEAR = "2026";
    public static final String COUNTER_DOCUMENT_ID = "aspnParticipantIdCounter_2026";
    private static final String PREFIX = "ASPN-" + COHORT_YEAR;
    private static final String SYSTEM_ASSIGNED_BY = "system";
    private static final long INITIAL_SEQUENCE = 1L;

    private final Firestore firestore;

    public ParticipantIdAssignment generateParticipantId() throws Exception {
        return firestore.runTransaction(transaction -> {
            Date now = new Date();
            DocumentReference counterRef = firestore.collection(SystemSettingsService.COLLECTION_NAME)
                    .document(COUNTER_DOCUMENT_ID);
            DocumentSnapshot counterSnapshot = transaction.get(counterRef).get();
            long nextSequence = getNextSequence(counterSnapshot);
            String participantId = formatParticipantId(nextSequence);

            Map<String, Object> updates = new HashMap<>();
            updates.put("nextSequence", nextSequence + 1);
            updates.put("prefix", PREFIX);
            updates.put("updatedAt", now);
            if (!counterSnapshot.exists()) {
                updates.put("createdAt", now);
            }

            transaction.set(counterRef, updates);
            return new ParticipantIdAssignment(participantId, now, SYSTEM_ASSIGNED_BY, COHORT_YEAR);
        }).get();
    }

    String formatParticipantId(long sequence) {
        if (sequence < INITIAL_SEQUENCE) {
            throw new IllegalArgumentException("participant ID sequence must be positive");
        }
        return PREFIX + "-" + String.format("%04d", sequence);
    }

    private long getNextSequence(DocumentSnapshot counterSnapshot) {
        if (!counterSnapshot.exists()) {
            return INITIAL_SEQUENCE;
        }

        Long nextSequence = counterSnapshot.getLong("nextSequence");
        if (nextSequence == null || nextSequence < INITIAL_SEQUENCE) {
            return INITIAL_SEQUENCE;
        }
        return nextSequence;
    }

    @Getter
    public static class ParticipantIdAssignment {
        private final String aspnParticipantId;
        private final Date assignedAt;
        private final String assignedBy;
        private final String cohortYear;

        public ParticipantIdAssignment(
                String aspnParticipantId,
                Date assignedAt,
                String assignedBy,
                String cohortYear
        ) {
            this.aspnParticipantId = aspnParticipantId;
            this.assignedAt = assignedAt;
            this.assignedBy = assignedBy;
            this.cohortYear = cohortYear;
        }
    }
}
