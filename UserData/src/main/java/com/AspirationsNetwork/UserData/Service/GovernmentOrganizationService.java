package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.GovernmentOrganizationDTO;
import com.AspirationsNetwork.UserData.DTO.GovernmentOrganizationTotalsDTO;
import com.AspirationsNetwork.UserData.Models.GovernmentOrganization;
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
public class GovernmentOrganizationService {
    public static final String COLLECTION_NAME = "governmentOrganizations";

    private static final Set<String> GOVERNMENT_LEVELS = Set.of(
            "municipal",
            "county",
            "regional",
            "state",
            "federal",
            "international"
    );

    private static final Set<String> ORGANIZATION_TYPES = Set.of(
            "city_government",
            "county_government",
            "state_agency",
            "school_district",
            "workforce_board",
            "public_authority",
            "public_university",
            "other"
    );

    private final Firestore firestore;

    public String createGovernmentOrganization(GovernmentOrganizationDTO dto) throws Exception {
        validateRequiredFields(dto);

        String governmentOrganizationId = UUID.randomUUID().toString();
        Date now = new Date();

        GovernmentOrganization organization = new GovernmentOrganization();
        organization.setGovernmentOrganizationId(governmentOrganizationId);
        organization.setOrganizationName(trim(dto.getOrganizationName()));
        organization.setGovernmentLevel(normalizeGovernmentLevel(dto.getGovernmentLevel()));
        organization.setOrganizationType(normalizeOrganizationType(dto.getOrganizationType()));
        organization.setWebsite(trim(dto.getWebsite()));
        organization.setPrimaryContactName(trim(dto.getPrimaryContactName()));
        organization.setPrimaryContactTitle(trim(dto.getPrimaryContactTitle()));
        organization.setPrimaryContactEmail(trim(dto.getPrimaryContactEmail()));
        organization.setPrimaryContactPhone(trim(dto.getPrimaryContactPhone()));
        organization.setActive(dto.getActive() == null || dto.getActive());
        organization.setWorkforcePartner(Boolean.TRUE.equals(dto.getWorkforcePartner()));
        organization.setCredentialPartner(Boolean.TRUE.equals(dto.getCredentialPartner()));
        organization.setNotes(trim(dto.getNotes()));
        organization.setCreatedAt(now);
        organization.setUpdatedAt(now);

        firestore.collection(COLLECTION_NAME)
                .document(governmentOrganizationId)
                .set(organization)
                .get();

        return governmentOrganizationId;
    }

    public List<GovernmentOrganization> getGovernmentOrganizations(
            String governmentLevel,
            String organizationType,
            Boolean active,
            Boolean workforcePartner,
            Boolean credentialPartner,
            String organizationName
    ) throws Exception {
        List<GovernmentOrganization> organizations = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            GovernmentOrganization organization = document.toObject(GovernmentOrganization.class);
            if (organization != null
                    && matchesFilters(organization, governmentLevel, organizationType, active, workforcePartner, credentialPartner, organizationName)) {
                organizations.add(organization);
            }
        }
        return organizations;
    }

    public GovernmentOrganization getGovernmentOrganization(String governmentOrganizationId) throws Exception {
        requireText(governmentOrganizationId, "governmentOrganizationId is required");

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(governmentOrganizationId)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        return document.toObject(GovernmentOrganization.class);
    }

    public void updateGovernmentOrganization(String governmentOrganizationId, GovernmentOrganizationDTO dto) throws Exception {
        requireText(governmentOrganizationId, "governmentOrganizationId is required");
        if (dto == null) {
            throw new IllegalArgumentException("government organization update is required");
        }
        if (getGovernmentOrganization(governmentOrganizationId) == null) {
            throw new IllegalArgumentException("Government organization does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        addIfPresent(updates, "organizationName", dto.getOrganizationName());
        addIfPresent(updates, "website", dto.getWebsite());
        addIfPresent(updates, "primaryContactName", dto.getPrimaryContactName());
        addIfPresent(updates, "primaryContactTitle", dto.getPrimaryContactTitle());
        addIfPresent(updates, "primaryContactEmail", dto.getPrimaryContactEmail());
        addIfPresent(updates, "primaryContactPhone", dto.getPrimaryContactPhone());
        addIfPresent(updates, "notes", dto.getNotes());
        if (dto.getGovernmentLevel() != null) {
            updates.put("governmentLevel", normalizeGovernmentLevel(dto.getGovernmentLevel()));
        }
        if (dto.getOrganizationType() != null) {
            updates.put("organizationType", normalizeOrganizationType(dto.getOrganizationType()));
        }
        if (dto.getActive() != null) {
            updates.put("active", dto.getActive());
        }
        if (dto.getWorkforcePartner() != null) {
            updates.put("workforcePartner", dto.getWorkforcePartner());
        }
        if (dto.getCredentialPartner() != null) {
            updates.put("credentialPartner", dto.getCredentialPartner());
        }
        updates.put("updatedAt", new Date());

        firestore.collection(COLLECTION_NAME)
                .document(governmentOrganizationId)
                .update(updates)
                .get();
    }

    public void activateGovernmentOrganization(String governmentOrganizationId) throws Exception {
        setActiveStatus(governmentOrganizationId, true);
    }

    public void deactivateGovernmentOrganization(String governmentOrganizationId) throws Exception {
        setActiveStatus(governmentOrganizationId, false);
    }

    public GovernmentOrganizationTotalsDTO getGovernmentOrganizationTotals() throws Exception {
        GovernmentOrganizationTotalsDTO totals = new GovernmentOrganizationTotalsDTO();

        for (GovernmentOrganization organization : getGovernmentOrganizations(null, null, null, null, null, null)) {
            totals.setTotalGovernmentOrganizations(totals.getTotalGovernmentOrganizations() + 1);
            if (organization.isActive()) {
                totals.setActiveGovernmentOrganizations(totals.getActiveGovernmentOrganizations() + 1);
            } else {
                totals.setInactiveGovernmentOrganizations(totals.getInactiveGovernmentOrganizations() + 1);
            }
            if (organization.isWorkforcePartner()) {
                totals.setWorkforcePartners(totals.getWorkforcePartners() + 1);
            }
            if (organization.isCredentialPartner()) {
                totals.setCredentialPartners(totals.getCredentialPartners() + 1);
            }

            totals.getOrganizationsByGovernmentLevel()
                    .merge(normalizeExistingGovernmentLevel(organization.getGovernmentLevel()), 1L, Long::sum);
            totals.getOrganizationsByOrganizationType()
                    .merge(normalizeExistingOrganizationType(organization.getOrganizationType()), 1L, Long::sum);
        }

        return totals;
    }

    private void setActiveStatus(String governmentOrganizationId, boolean active) throws Exception {
        requireText(governmentOrganizationId, "governmentOrganizationId is required");
        if (getGovernmentOrganization(governmentOrganizationId) == null) {
            throw new IllegalArgumentException("Government organization does not exist");
        }

        firestore.collection(COLLECTION_NAME)
                .document(governmentOrganizationId)
                .update(
                        "active", active,
                        "updatedAt", new Date()
                )
                .get();
    }

    private boolean matchesFilters(
            GovernmentOrganization organization,
            String governmentLevel,
            String organizationType,
            Boolean active,
            Boolean workforcePartner,
            Boolean credentialPartner,
            String organizationName
    ) {
        if (active != null && organization.isActive() != active) {
            return false;
        }
        if (workforcePartner != null && organization.isWorkforcePartner() != workforcePartner) {
            return false;
        }
        if (credentialPartner != null && organization.isCredentialPartner() != credentialPartner) {
            return false;
        }
        if (governmentLevel != null && !governmentLevel.isBlank()
                && !normalizeGovernmentLevel(governmentLevel).equals(normalizeExistingGovernmentLevel(organization.getGovernmentLevel()))) {
            return false;
        }
        if (organizationType != null && !organizationType.isBlank()
                && !normalizeOrganizationType(organizationType).equals(normalizeExistingOrganizationType(organization.getOrganizationType()))) {
            return false;
        }
        return organizationName == null || organizationName.isBlank()
                || containsIgnoreCase(organization.getOrganizationName(), organizationName);
    }

    private void validateRequiredFields(GovernmentOrganizationDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("government organization is required");
        }
        requireText(dto.getOrganizationName(), "organizationName is required");
        normalizeGovernmentLevel(dto.getGovernmentLevel());
        normalizeOrganizationType(dto.getOrganizationType());
    }

    private void addIfPresent(Map<String, Object> updates, String field, String value) {
        if (value != null) {
            updates.put(field, trim(value));
        }
    }

    private String normalizeGovernmentLevel(String value) {
        requireText(value, "governmentLevel is required");
        String normalized = value.trim().toLowerCase();
        if (!GOVERNMENT_LEVELS.contains(normalized)) {
            throw new IllegalArgumentException("governmentLevel is invalid");
        }
        return normalized;
    }

    private String normalizeExistingGovernmentLevel(String value) {
        if (value == null || value.isBlank()) {
            return "municipal";
        }
        String normalized = value.trim().toLowerCase();
        return GOVERNMENT_LEVELS.contains(normalized) ? normalized : "municipal";
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
