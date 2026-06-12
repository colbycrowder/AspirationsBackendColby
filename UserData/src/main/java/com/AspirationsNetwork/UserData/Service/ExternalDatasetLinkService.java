package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ExternalDatasetDTO;
import com.AspirationsNetwork.UserData.DTO.ParticipantExternalLinkDTO;
import com.AspirationsNetwork.UserData.Models.ExternalDataset;
import com.AspirationsNetwork.UserData.Models.ParticipantExternalLink;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
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
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ExternalDatasetLinkService {
    public static final String EXTERNAL_DATASETS_COLLECTION = "externalDatasets";
    public static final String PARTICIPANT_EXTERNAL_LINKS_COLLECTION = "participantExternalLinks";
    private static final Set<String> VALID_LINK_STATUSES = Set.of("active", "removed", "needs_review");

    private final Firestore firestore;

    public String createExternalDataset(ExternalDatasetDTO dto) throws Exception {
        if (dto == null) {
            throw new IllegalArgumentException("external dataset request is required");
        }
        requireText(dto.getDatasetName(), "datasetName is required");
        requireText(dto.getExternalSource(), "externalSource is required");
        requireText(dto.getCreatedByStaffUID(), "createdByStaffUID is required");

        String externalDatasetId = hasText(dto.getExternalDatasetId())
                ? dto.getExternalDatasetId().trim()
                : UUID.randomUUID().toString();
        DocumentReference datasetRef = firestore.collection(EXTERNAL_DATASETS_COLLECTION).document(externalDatasetId);
        if (datasetRef.get().get().exists()) {
            throw new IllegalArgumentException("External dataset already exists");
        }

        Date now = new Date();
        ExternalDataset dataset = new ExternalDataset();
        dataset.setExternalDatasetId(externalDatasetId);
        dataset.setDatasetName(dto.getDatasetName());
        dataset.setExternalSource(dto.getExternalSource());
        dataset.setDescription(dto.getDescription());
        dataset.setCollectionPurpose(dto.getCollectionPurpose());
        dataset.setContainsPII(Boolean.TRUE.equals(dto.getContainsPII()));
        dataset.setActive(dto.getActive() == null || dto.getActive());
        dataset.setCreatedByStaffUID(dto.getCreatedByStaffUID());
        dataset.setCreatedAt(now);
        dataset.setUpdatedAt(now);

        datasetRef.set(dataset).get();
        return externalDatasetId;
    }

    public List<ExternalDataset> getExternalDatasets() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(EXTERNAL_DATASETS_COLLECTION).get();
        List<ExternalDataset> datasets = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            ExternalDataset dataset = document.toObject(ExternalDataset.class);
            if (dataset != null) {
                datasets.add(dataset);
            }
        }
        return datasets;
    }

    public void updateExternalDataset(String externalDatasetId, ExternalDatasetDTO dto) throws Exception {
        requireText(externalDatasetId, "externalDatasetId is required");
        if (dto == null) {
            throw new IllegalArgumentException("external dataset request is required");
        }

        DocumentReference datasetRef = firestore.collection(EXTERNAL_DATASETS_COLLECTION).document(externalDatasetId);
        if (!datasetRef.get().get().exists()) {
            throw new IllegalArgumentException("External dataset does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        addIfPresent(updates, "datasetName", dto.getDatasetName());
        addIfPresent(updates, "externalSource", dto.getExternalSource());
        addIfPresent(updates, "description", dto.getDescription());
        addIfPresent(updates, "collectionPurpose", dto.getCollectionPurpose());
        if (dto.getContainsPII() != null) {
            updates.put("containsPII", dto.getContainsPII());
        }
        if (dto.getActive() != null) {
            updates.put("active", dto.getActive());
        }
        updates.put("updatedAt", new Date());

        datasetRef.update(updates).get();
    }

    public String createParticipantExternalLink(ParticipantExternalLinkDTO dto) throws Exception {
        if (dto == null) {
            throw new IllegalArgumentException("participant external link request is required");
        }
        requireText(dto.getAspnParticipantId(), "aspnParticipantId is required");
        requireText(dto.getExternalDatasetId(), "externalDatasetId is required");
        requireText(dto.getExternalRecordId(), "externalRecordId is required");
        requireText(dto.getLinkedByStaffUID(), "linkedByStaffUID is required");

        User participant = getParticipantByAspnParticipantId(dto.getAspnParticipantId());
        if (participant == null) {
            throw new IllegalArgumentException("Participant profile does not exist");
        }

        ExternalDataset dataset = getActiveExternalDataset(dto.getExternalDatasetId());
        if (dataset == null) {
            throw new IllegalArgumentException("External dataset does not exist or is inactive");
        }

        if (hasActiveLinkForExternalRecord(dto.getExternalDatasetId(), dto.getExternalRecordId())) {
            throw new IllegalArgumentException("Active external record link already exists");
        }

        String linkId = UUID.randomUUID().toString();
        Date now = new Date();
        ParticipantExternalLink link = new ParticipantExternalLink();
        link.setLinkId(linkId);
        link.setAspnParticipantId(participant.getAspnParticipantId());
        link.setUserUID(participant.getUid());
        link.setExternalDatasetId(dataset.getExternalDatasetId());
        link.setExternalSource(dataset.getExternalSource());
        link.setExternalRecordId(dto.getExternalRecordId());
        link.setExternalDatasetName(dataset.getDatasetName());
        link.setLinkStatus("active");
        link.setLinkedAt(now);
        link.setLinkedByStaffUID(dto.getLinkedByStaffUID());
        link.setUpdatedAt(now);
        link.setNotes(dto.getNotes());

        firestore.collection(PARTICIPANT_EXTERNAL_LINKS_COLLECTION)
                .document(linkId)
                .set(link)
                .get();

        return linkId;
    }

    public List<ParticipantExternalLink> getLinksByParticipant(String aspnParticipantId)
            throws ExecutionException, InterruptedException {
        requireText(aspnParticipantId, "aspnParticipantId is required");
        ApiFuture<QuerySnapshot> future = firestore.collection(PARTICIPANT_EXTERNAL_LINKS_COLLECTION)
                .whereEqualTo("aspnParticipantId", aspnParticipantId)
                .get();
        return mapLinks(future.get().getDocuments());
    }

    public List<ParticipantExternalLink> getLinksByDataset(String externalDatasetId)
            throws ExecutionException, InterruptedException {
        requireText(externalDatasetId, "externalDatasetId is required");
        ApiFuture<QuerySnapshot> future = firestore.collection(PARTICIPANT_EXTERNAL_LINKS_COLLECTION)
                .whereEqualTo("externalDatasetId", externalDatasetId)
                .get();
        return mapLinks(future.get().getDocuments());
    }

    public void removeParticipantExternalLink(String linkId, String removedByStaffUID) throws Exception {
        requireText(linkId, "linkId is required");
        requireText(removedByStaffUID, "removedByStaffUID is required");

        DocumentReference linkRef = firestore.collection(PARTICIPANT_EXTERNAL_LINKS_COLLECTION).document(linkId);
        if (!linkRef.get().get().exists()) {
            throw new IllegalArgumentException("Participant external link does not exist");
        }

        Date now = new Date();
        linkRef.update(
                "linkStatus", "removed",
                "removedAt", now,
                "removedByStaffUID", removedByStaffUID,
                "updatedAt", now
        ).get();
    }

    private User getParticipantByAspnParticipantId(String aspnParticipantId)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(UserInfoService.COLLECTION_NAME)
                .whereEqualTo("aspnParticipantId", aspnParticipantId)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }

        User user = documents.get(0).toObject(User.class);
        if (user == null || !user.isYouthProfile()) {
            return null;
        }
        return user;
    }

    private ExternalDataset getActiveExternalDataset(String externalDatasetId) throws Exception {
        DocumentSnapshot document = firestore.collection(EXTERNAL_DATASETS_COLLECTION)
                .document(externalDatasetId)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        ExternalDataset dataset = document.toObject(ExternalDataset.class);
        if (dataset == null || !dataset.isActive()) {
            return null;
        }
        return dataset;
    }

    private boolean hasActiveLinkForExternalRecord(String externalDatasetId, String externalRecordId)
            throws ExecutionException, InterruptedException {
        Query query = firestore.collection(PARTICIPANT_EXTERNAL_LINKS_COLLECTION)
                .whereEqualTo("externalDatasetId", externalDatasetId)
                .whereEqualTo("externalRecordId", externalRecordId)
                .whereEqualTo("linkStatus", "active");

        return !query.get().get().getDocuments().isEmpty();
    }

    private List<ParticipantExternalLink> mapLinks(List<QueryDocumentSnapshot> documents) {
        List<ParticipantExternalLink> links = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            ParticipantExternalLink link = document.toObject(ParticipantExternalLink.class);
            if (link != null && VALID_LINK_STATUSES.contains(normalizeStatus(link.getLinkStatus()))) {
                links.add(link);
            }
        }
        return links;
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "active" : status.toLowerCase();
    }

    private void addIfPresent(Map<String, Object> updates, String fieldName, String value) {
        if (value != null) {
            updates.put(fieldName, value);
        }
    }

    private void requireText(String value, String message) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
