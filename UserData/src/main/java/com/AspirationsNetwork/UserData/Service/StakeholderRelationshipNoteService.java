package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.StakeholderRelationshipNoteDTO;
import com.AspirationsNetwork.UserData.DTO.StakeholderRelationshipNoteTotalsDTO;
import com.AspirationsNetwork.UserData.Models.StakeholderRelationshipNote;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StakeholderRelationshipNoteService {
    public static final String COLLECTION_NAME = "stakeholderRelationshipNotes";

    private static final Set<String> STAKEHOLDER_TYPES = Set.of(
            "educator",
            "partner_organization",
            "government_organization"
    );
    private static final Set<String> RELATIONSHIP_STATUSES = Set.of(
            "prospect",
            "contacted",
            "meeting_scheduled",
            "active_partner",
            "inactive_partner",
            "declined"
    );

    private final Firestore firestore;

    public String createNote(StakeholderRelationshipNoteDTO dto, String verifiedStaffUID) throws Exception {
        validateRequiredFields(dto);
        requireText(verifiedStaffUID, "verifiedStaffUID is required");

        String noteId = UUID.randomUUID().toString();
        Date now = new Date();

        StakeholderRelationshipNote note = new StakeholderRelationshipNote();
        note.setStakeholderRelationshipNoteId(noteId);
        note.setStakeholderType(normalizeStakeholderType(dto.getStakeholderType()));
        note.setStakeholderId(trim(dto.getStakeholderId()));
        note.setStakeholderName(trim(dto.getStakeholderName()));
        note.setNoteText(trim(dto.getNoteText()));
        note.setRelationshipStatus(normalizeRelationshipStatusOrDefault(dto.getRelationshipStatus()));
        note.setRelationshipOwnerUID(resolveOwner(dto.getRelationshipOwnerUID(), verifiedStaffUID));
        note.setLastContactDate(dto.getLastContactDate());
        note.setNextFollowUpDate(dto.getNextFollowUpDate());
        note.setActive(dto.getActive() == null || dto.getActive());
        note.setCreatedAt(now);
        note.setUpdatedAt(now);

        firestore.collection(COLLECTION_NAME)
                .document(noteId)
                .set(note)
                .get();

        return noteId;
    }

    public List<StakeholderRelationshipNote> getNotes(
            String stakeholderType,
            String stakeholderId,
            String relationshipStatus,
            String relationshipOwnerUID,
            Boolean active,
            Date nextFollowUpBefore,
            Date nextFollowUpAfter
    ) throws Exception {
        List<StakeholderRelationshipNote> notes = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            StakeholderRelationshipNote note = document.toObject(StakeholderRelationshipNote.class);
            if (note != null && matchesFilters(note, stakeholderType, stakeholderId, relationshipStatus, relationshipOwnerUID, active, nextFollowUpBefore, nextFollowUpAfter)) {
                notes.add(note);
            }
        }
        return notes;
    }

    public StakeholderRelationshipNote getNote(String noteId) throws Exception {
        requireText(noteId, "stakeholderRelationshipNoteId is required");

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(noteId)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }
        return document.toObject(StakeholderRelationshipNote.class);
    }

    public void updateNote(String noteId, StakeholderRelationshipNoteDTO dto, String verifiedStaffUID) throws Exception {
        requireText(noteId, "stakeholderRelationshipNoteId is required");
        requireText(verifiedStaffUID, "verifiedStaffUID is required");
        if (dto == null) {
            throw new IllegalArgumentException("relationship note update is required");
        }
        if (getNote(noteId) == null) {
            throw new IllegalArgumentException("Relationship note does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        addRequiredIfPresent(updates, "stakeholderId", dto.getStakeholderId(), "stakeholderId is required");
        addIfPresent(updates, "stakeholderName", dto.getStakeholderName());
        addRequiredIfPresent(updates, "noteText", dto.getNoteText(), "noteText is required");
        if (dto.getStakeholderType() != null) {
            updates.put("stakeholderType", normalizeStakeholderType(dto.getStakeholderType()));
        }
        if (dto.getRelationshipStatus() != null) {
            updates.put("relationshipStatus", normalizeRelationshipStatus(dto.getRelationshipStatus()));
        }
        if (dto.getRelationshipOwnerUID() != null) {
            updates.put("relationshipOwnerUID", resolveOwner(dto.getRelationshipOwnerUID(), verifiedStaffUID));
        }
        if (dto.getLastContactDate() != null) {
            updates.put("lastContactDate", dto.getLastContactDate());
        }
        if (dto.getNextFollowUpDate() != null) {
            updates.put("nextFollowUpDate", dto.getNextFollowUpDate());
        }
        if (dto.getActive() != null) {
            updates.put("active", dto.getActive());
        }
        updates.put("updatedAt", new Date());

        firestore.collection(COLLECTION_NAME)
                .document(noteId)
                .update(updates)
                .get();
    }

    public void deleteNote(String noteId) throws Exception {
        requireText(noteId, "stakeholderRelationshipNoteId is required");
        if (getNote(noteId) == null) {
            throw new IllegalArgumentException("Relationship note does not exist");
        }

        firestore.collection(COLLECTION_NAME)
                .document(noteId)
                .delete()
                .get();
    }

    public StakeholderRelationshipNoteTotalsDTO getTotals() throws Exception {
        StakeholderRelationshipNoteTotalsDTO totals = new StakeholderRelationshipNoteTotalsDTO();
        Date now = new Date();

        for (StakeholderRelationshipNote note : getNotes(null, null, null, null, null, null, null)) {
            totals.setTotalNotes(totals.getTotalNotes() + 1);
            String stakeholderType = normalizeExistingStakeholderType(note.getStakeholderType());
            if (note.isActive()) {
                totals.setActiveNotes(totals.getActiveNotes() + 1);
            } else {
                totals.setInactiveNotes(totals.getInactiveNotes() + 1);
            }

            totals.getNotesByStakeholderType()
                    .merge(stakeholderType, 1L, Long::sum);
            totals.getNotesByRelationshipStatus()
                    .merge(normalizeExistingRelationshipStatus(note.getRelationshipStatus()), 1L, Long::sum);
            totals.getNotesByRelationshipOwnerUID()
                    .merge(normalizeOwnerUID(note.getRelationshipOwnerUID()), 1L, Long::sum);

            Date followUp = note.getNextFollowUpDate();
            if (note.isActive() && followUp != null) {
                if (followUp.before(now)) {
                    totals.setOverdueFollowUps(totals.getOverdueFollowUps() + 1);
                    totals.getOverdueFollowUpsByStakeholderType().merge(stakeholderType, 1L, Long::sum);
                } else {
                    totals.setUpcomingFollowUps(totals.getUpcomingFollowUps() + 1);
                    totals.getUpcomingFollowUpsByStakeholderType().merge(stakeholderType, 1L, Long::sum);
                }
            }
        }
        return totals;
    }

    private boolean matchesFilters(
            StakeholderRelationshipNote note,
            String stakeholderType,
            String stakeholderId,
            String relationshipStatus,
            String relationshipOwnerUID,
            Boolean active,
            Date nextFollowUpBefore,
            Date nextFollowUpAfter
    ) {
        if (active != null && note.isActive() != active) {
            return false;
        }
        if (stakeholderType != null && !stakeholderType.isBlank()
                && !normalizeStakeholderType(stakeholderType).equals(normalizeExistingStakeholderType(note.getStakeholderType()))) {
            return false;
        }
        if (stakeholderId != null && !stakeholderId.isBlank() && !stakeholderId.equals(note.getStakeholderId())) {
            return false;
        }
        if (relationshipStatus != null && !relationshipStatus.isBlank()
                && !normalizeRelationshipStatus(relationshipStatus).equals(normalizeExistingRelationshipStatus(note.getRelationshipStatus()))) {
            return false;
        }
        if (relationshipOwnerUID != null && !relationshipOwnerUID.isBlank()
                && !relationshipOwnerUID.equals(note.getRelationshipOwnerUID())) {
            return false;
        }
        Date followUp = note.getNextFollowUpDate();
        if (nextFollowUpBefore != null && (followUp == null || followUp.after(nextFollowUpBefore))) {
            return false;
        }
        return nextFollowUpAfter == null || (followUp != null && !followUp.before(nextFollowUpAfter));
    }

    private void validateRequiredFields(StakeholderRelationshipNoteDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("relationship note is required");
        }
        normalizeStakeholderType(dto.getStakeholderType());
        requireText(dto.getStakeholderId(), "stakeholderId is required");
        requireText(dto.getNoteText(), "noteText is required");
        if (dto.getRelationshipStatus() != null) {
            normalizeRelationshipStatus(dto.getRelationshipStatus());
        }
    }

    private void addIfPresent(Map<String, Object> updates, String field, String value) {
        if (value != null) {
            updates.put(field, trim(value));
        }
    }

    private void addRequiredIfPresent(Map<String, Object> updates, String field, String value, String message) {
        if (value != null) {
            requireText(value, message);
            updates.put(field, trim(value));
        }
    }

    private String resolveOwner(String requestedOwnerUID, String verifiedStaffUID) {
        return requestedOwnerUID == null || requestedOwnerUID.isBlank() ? verifiedStaffUID : requestedOwnerUID.trim();
    }

    private String normalizeStakeholderType(String value) {
        requireText(value, "stakeholderType is required");
        String normalized = value.trim().toLowerCase();
        if (!STAKEHOLDER_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("stakeholderType is invalid");
        }
        return normalized;
    }

    private String normalizeExistingStakeholderType(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String normalized = value.trim().toLowerCase();
        return STAKEHOLDER_TYPES.contains(normalized) ? normalized : "unknown";
    }

    private String normalizeRelationshipStatusOrDefault(String value) {
        return value == null || value.isBlank() ? "prospect" : normalizeRelationshipStatus(value);
    }

    private String normalizeRelationshipStatus(String value) {
        requireText(value, "relationshipStatus is required");
        String normalized = value.trim().toLowerCase();
        if (!RELATIONSHIP_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("relationshipStatus is invalid");
        }
        return normalized;
    }

    private String normalizeExistingRelationshipStatus(String value) {
        if (value == null || value.isBlank()) {
            return "prospect";
        }
        String normalized = value.trim().toLowerCase();
        return RELATIONSHIP_STATUSES.contains(normalized) ? normalized : "prospect";
    }

    private String normalizeOwnerUID(String value) {
        return value == null || value.isBlank() ? "unassigned" : value.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
