package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.EducatorDTO;
import com.AspirationsNetwork.UserData.DTO.EducatorTotalsDTO;
import com.AspirationsNetwork.UserData.Models.Educator;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EducatorService {
    public static final String COLLECTION_NAME = "educators";

    private static final Set<String> ORGANIZATION_TYPES = Set.of(
            "high_school",
            "middle_school",
            "college",
            "nonprofit",
            "government",
            "other"
    );

    private final Firestore firestore;

    public String createEducator(EducatorDTO dto) throws Exception {
        validateRequiredFields(dto);

        String educatorId = UUID.randomUUID().toString();
        Date now = new Date();

        Educator educator = new Educator();
        educator.setEducatorId(educatorId);
        educator.setFirstName(trim(dto.getFirstName()));
        educator.setLastName(trim(dto.getLastName()));
        educator.setEmail(trim(dto.getEmail()));
        educator.setPhone(trim(dto.getPhone()));
        educator.setTitle(trim(dto.getTitle()));
        educator.setOrganizationName(trim(dto.getOrganizationName()));
        educator.setOrganizationType(normalizeOrganizationType(dto.getOrganizationType()));
        educator.setActive(dto.getActive() == null || dto.getActive());
        educator.setNotes(trim(dto.getNotes()));
        educator.setCreatedAt(now);
        educator.setUpdatedAt(now);

        firestore.collection(COLLECTION_NAME)
                .document(educatorId)
                .set(educator)
                .get();

        return educatorId;
    }

    public List<Educator> getEducators(
            String organizationType,
            Boolean active,
            String organizationName,
            String search
    ) throws Exception {
        List<Educator> educators = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            Educator educator = document.toObject(Educator.class);
            if (educator != null && matchesFilters(educator, organizationType, active, organizationName, search)) {
                educators.add(educator);
            }
        }
        return educators;
    }

    public Educator getEducator(String educatorId) throws Exception {
        requireText(educatorId, "educatorId is required");

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(educatorId)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        return document.toObject(Educator.class);
    }

    public void updateEducator(String educatorId, EducatorDTO dto) throws Exception {
        requireText(educatorId, "educatorId is required");
        if (dto == null) {
            throw new IllegalArgumentException("educator update is required");
        }
        if (getEducator(educatorId) == null) {
            throw new IllegalArgumentException("Educator does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        addIfPresent(updates, "firstName", dto.getFirstName());
        addIfPresent(updates, "lastName", dto.getLastName());
        addIfPresent(updates, "email", dto.getEmail());
        addIfPresent(updates, "phone", dto.getPhone());
        addIfPresent(updates, "title", dto.getTitle());
        addIfPresent(updates, "organizationName", dto.getOrganizationName());
        addIfPresent(updates, "notes", dto.getNotes());
        if (dto.getOrganizationType() != null) {
            updates.put("organizationType", normalizeOrganizationType(dto.getOrganizationType()));
        }
        if (dto.getActive() != null) {
            updates.put("active", dto.getActive());
        }
        updates.put("updatedAt", new Date());

        firestore.collection(COLLECTION_NAME)
                .document(educatorId)
                .update(updates)
                .get();
    }

    public void activateEducator(String educatorId) throws Exception {
        setActiveStatus(educatorId, true);
    }

    public void deactivateEducator(String educatorId) throws Exception {
        setActiveStatus(educatorId, false);
    }

    public EducatorTotalsDTO getEducatorTotals() throws Exception {
        EducatorTotalsDTO totals = new EducatorTotalsDTO();
        Set<String> organizations = new HashSet<>();

        for (Educator educator : getEducators(null, null, null, null)) {
            totals.setTotalEducators(totals.getTotalEducators() + 1);
            if (educator.isActive()) {
                totals.setActiveEducators(totals.getActiveEducators() + 1);
            } else {
                totals.setInactiveEducators(totals.getInactiveEducators() + 1);
            }

            if (educator.getOrganizationName() != null && !educator.getOrganizationName().isBlank()) {
                organizations.add(educator.getOrganizationName().trim().toLowerCase());
            }

            String type = normalizeExistingOrganizationType(educator.getOrganizationType());
            totals.getEducatorsByOrganizationType().merge(type, 1L, Long::sum);
        }

        totals.setOrganizationsRepresented(organizations.size());
        return totals;
    }

    private void setActiveStatus(String educatorId, boolean active) throws Exception {
        requireText(educatorId, "educatorId is required");
        if (getEducator(educatorId) == null) {
            throw new IllegalArgumentException("Educator does not exist");
        }

        firestore.collection(COLLECTION_NAME)
                .document(educatorId)
                .update(
                        "active", active,
                        "updatedAt", new Date()
                )
                .get();
    }

    private boolean matchesFilters(
            Educator educator,
            String organizationType,
            Boolean active,
            String organizationName,
            String search
    ) {
        if (active != null && educator.isActive() != active) {
            return false;
        }
        if (organizationType != null && !organizationType.isBlank()
                && !normalizeOrganizationType(organizationType).equals(normalizeExistingOrganizationType(educator.getOrganizationType()))) {
            return false;
        }
        if (organizationName != null && !organizationName.isBlank()
                && !containsIgnoreCase(educator.getOrganizationName(), organizationName)) {
            return false;
        }
        if (search != null && !search.isBlank()) {
            String haystack = String.join(" ",
                    valueOrBlank(educator.getFirstName()),
                    valueOrBlank(educator.getLastName()),
                    valueOrBlank(educator.getEmail()),
                    valueOrBlank(educator.getTitle()),
                    valueOrBlank(educator.getOrganizationName())
            );
            return containsIgnoreCase(haystack, search);
        }
        return true;
    }

    private void validateRequiredFields(EducatorDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("educator is required");
        }
        requireText(dto.getFirstName(), "firstName is required");
        requireText(dto.getLastName(), "lastName is required");
        requireText(dto.getEmail(), "email is required");
        requireText(dto.getOrganizationName(), "organizationName is required");
        normalizeOrganizationType(dto.getOrganizationType());
    }

    private void addIfPresent(Map<String, Object> updates, String field, String value) {
        if (value != null) {
            updates.put(field, trim(value));
        }
    }

    private String normalizeOrganizationType(String value) {
        requireText(value, "organizationType is required");
        String normalized = value.trim().toLowerCase();
        if (!ORGANIZATION_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("organizationType is invalid");
        }
        return normalized;
    }

    private String normalizeExistingOrganizationType(String value) {
        if (value == null || value.isBlank()) {
            return "other";
        }
        String normalized = value.trim().toLowerCase();
        return ORGANIZATION_TYPES.contains(normalized) ? normalized : "other";
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query.trim().toLowerCase());
    }

    private String valueOrBlank(String value) {
        return value == null ? "" : value;
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
