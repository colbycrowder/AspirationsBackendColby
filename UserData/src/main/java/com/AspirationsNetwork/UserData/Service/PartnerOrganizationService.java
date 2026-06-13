package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PartnerOrganizationDTO;
import com.AspirationsNetwork.UserData.DTO.PartnerOrganizationTotalsDTO;
import com.AspirationsNetwork.UserData.Models.PartnerOrganization;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartnerOrganizationService {
    public static final String COLLECTION_NAME = "partnerOrganizations";

    private static final Set<String> ORGANIZATION_TYPES = Set.of(
            "nonprofit",
            "foundation",
            "workforce",
            "business",
            "higher_education",
            "faith_based",
            "government_affiliated",
            "community",
            "other"
    );

    private final Firestore firestore;

    public String createPartnerOrganization(PartnerOrganizationDTO dto) throws Exception {
        validateRequiredFields(dto);

        String partnerOrganizationId = UUID.randomUUID().toString();
        Date now = new Date();

        PartnerOrganization partner = new PartnerOrganization();
        partner.setPartnerOrganizationId(partnerOrganizationId);
        partner.setOrganizationName(trim(dto.getOrganizationName()));
        partner.setOrganizationType(normalizeOrganizationType(dto.getOrganizationType()));
        partner.setWebsite(trim(dto.getWebsite()));
        partner.setPrimaryContactName(trim(dto.getPrimaryContactName()));
        partner.setPrimaryContactEmail(trim(dto.getPrimaryContactEmail()));
        partner.setPrimaryContactPhone(trim(dto.getPrimaryContactPhone()));
        partner.setActive(dto.getActive() == null || dto.getActive());
        partner.setNotes(trim(dto.getNotes()));
        partner.setCreatedAt(now);
        partner.setUpdatedAt(now);

        firestore.collection(COLLECTION_NAME)
                .document(partnerOrganizationId)
                .set(partner)
                .get();

        return partnerOrganizationId;
    }

    public List<PartnerOrganization> getPartnerOrganizations(
            String organizationType,
            Boolean active,
            String organizationName
    ) throws Exception {
        List<PartnerOrganization> partners = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            PartnerOrganization partner = document.toObject(PartnerOrganization.class);
            if (partner != null && matchesFilters(partner, organizationType, active, organizationName)) {
                partners.add(partner);
            }
        }
        return partners;
    }

    public PartnerOrganization getPartnerOrganization(String partnerOrganizationId) throws Exception {
        requireText(partnerOrganizationId, "partnerOrganizationId is required");

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(partnerOrganizationId)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        return document.toObject(PartnerOrganization.class);
    }

    public void updatePartnerOrganization(String partnerOrganizationId, PartnerOrganizationDTO dto) throws Exception {
        requireText(partnerOrganizationId, "partnerOrganizationId is required");
        if (dto == null) {
            throw new IllegalArgumentException("partner organization update is required");
        }
        if (getPartnerOrganization(partnerOrganizationId) == null) {
            throw new IllegalArgumentException("Partner organization does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        addIfPresent(updates, "organizationName", dto.getOrganizationName());
        addIfPresent(updates, "website", dto.getWebsite());
        addIfPresent(updates, "primaryContactName", dto.getPrimaryContactName());
        addIfPresent(updates, "primaryContactEmail", dto.getPrimaryContactEmail());
        addIfPresent(updates, "primaryContactPhone", dto.getPrimaryContactPhone());
        addIfPresent(updates, "notes", dto.getNotes());
        if (dto.getOrganizationType() != null) {
            updates.put("organizationType", normalizeOrganizationType(dto.getOrganizationType()));
        }
        if (dto.getActive() != null) {
            updates.put("active", dto.getActive());
        }
        updates.put("updatedAt", new Date());

        firestore.collection(COLLECTION_NAME)
                .document(partnerOrganizationId)
                .update(updates)
                .get();
    }

    public void activatePartnerOrganization(String partnerOrganizationId) throws Exception {
        setActiveStatus(partnerOrganizationId, true);
    }

    public void deactivatePartnerOrganization(String partnerOrganizationId) throws Exception {
        setActiveStatus(partnerOrganizationId, false);
    }

    public PartnerOrganizationTotalsDTO getPartnerOrganizationTotals() throws Exception {
        PartnerOrganizationTotalsDTO totals = new PartnerOrganizationTotalsDTO();
        Set<String> organizationTypes = new HashSet<>();

        for (PartnerOrganization partner : getPartnerOrganizations(null, null, null)) {
            totals.setTotalPartners(totals.getTotalPartners() + 1);
            if (partner.isActive()) {
                totals.setActivePartners(totals.getActivePartners() + 1);
            } else {
                totals.setInactivePartners(totals.getInactivePartners() + 1);
            }

            String type = normalizeExistingOrganizationType(partner.getOrganizationType());
            organizationTypes.add(type);
            totals.getPartnersByOrganizationType().merge(type, 1L, Long::sum);
        }

        totals.setOrganizationTypesRepresented(organizationTypes.size());
        return totals;
    }

    private void setActiveStatus(String partnerOrganizationId, boolean active) throws Exception {
        requireText(partnerOrganizationId, "partnerOrganizationId is required");
        if (getPartnerOrganization(partnerOrganizationId) == null) {
            throw new IllegalArgumentException("Partner organization does not exist");
        }

        firestore.collection(COLLECTION_NAME)
                .document(partnerOrganizationId)
                .update(
                        "active", active,
                        "updatedAt", new Date()
                )
                .get();
    }

    private boolean matchesFilters(
            PartnerOrganization partner,
            String organizationType,
            Boolean active,
            String organizationName
    ) {
        if (active != null && partner.isActive() != active) {
            return false;
        }
        if (organizationType != null && !organizationType.isBlank()
                && !normalizeOrganizationType(organizationType).equals(normalizeExistingOrganizationType(partner.getOrganizationType()))) {
            return false;
        }
        return organizationName == null || organizationName.isBlank()
                || containsIgnoreCase(partner.getOrganizationName(), organizationName);
    }

    private void validateRequiredFields(PartnerOrganizationDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("partner organization is required");
        }
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

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
