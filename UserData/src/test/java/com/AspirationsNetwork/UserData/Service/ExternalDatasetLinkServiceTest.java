package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ExternalDatasetDTO;
import com.AspirationsNetwork.UserData.DTO.ParticipantExternalLinkDTO;
import com.AspirationsNetwork.UserData.Models.ExternalDataset;
import com.AspirationsNetwork.UserData.Models.ParticipantExternalLink;
import com.AspirationsNetwork.UserData.Models.User;
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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalDatasetLinkServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createExternalDatasetStoresDatasetMetadata() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference datasetsCollection = mock(CollectionReference.class);
        DocumentReference datasetDocument = mock(DocumentReference.class);
        DocumentSnapshot missingDataset = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(ExternalDatasetLinkService.EXTERNAL_DATASETS_COLLECTION))
                .thenReturn(datasetsCollection);
        when(datasetsCollection.document("fall-survey-2026")).thenReturn(datasetDocument);
        when(datasetDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(missingDataset);
        when(missingDataset.exists()).thenReturn(false);
        when(datasetDocument.set(any(ExternalDataset.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        ExternalDatasetDTO dto = new ExternalDatasetDTO();
        dto.setExternalDatasetId("fall-survey-2026");
        dto.setDatasetName("Fall Survey 2026");
        dto.setExternalSource("google_forms");
        dto.setCollectionPurpose("Pilot evaluation");
        dto.setContainsPII(true);
        dto.setCreatedByStaffUID("staff-123");

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);
        String datasetId = service.createExternalDataset(dto);

        ArgumentCaptor<ExternalDataset> datasetCaptor = ArgumentCaptor.forClass(ExternalDataset.class);
        verify(datasetDocument).set(datasetCaptor.capture());
        ExternalDataset savedDataset = datasetCaptor.getValue();
        assertEquals("fall-survey-2026", datasetId);
        assertEquals("Fall Survey 2026", savedDataset.getDatasetName());
        assertEquals("google_forms", savedDataset.getExternalSource());
        assertEquals("Pilot evaluation", savedDataset.getCollectionPurpose());
        assertTrue(savedDataset.isContainsPII());
        assertTrue(savedDataset.isActive());
        assertEquals("staff-123", savedDataset.getCreatedByStaffUID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getExternalDatasetsReturnsStoredDatasets() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference datasetsCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> queryFuture = mock(ApiFuture.class);
        QuerySnapshot querySnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot document = mock(QueryDocumentSnapshot.class);
        ExternalDataset dataset = new ExternalDataset();
        dataset.setExternalDatasetId("fall-survey-2026");

        when(firestore.collection(ExternalDatasetLinkService.EXTERNAL_DATASETS_COLLECTION))
                .thenReturn(datasetsCollection);
        when(datasetsCollection.get()).thenReturn(queryFuture);
        when(queryFuture.get()).thenReturn(querySnapshot);
        when(querySnapshot.getDocuments()).thenReturn(List.of(document));
        when(document.toObject(ExternalDataset.class)).thenReturn(dataset);

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);
        List<ExternalDataset> datasets = service.getExternalDatasets();

        assertEquals(1, datasets.size());
        assertEquals("fall-survey-2026", datasets.get(0).getExternalDatasetId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateExternalDatasetUpdatesAllowedMetadataFields() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference datasetsCollection = mock(CollectionReference.class);
        DocumentReference datasetDocument = mock(DocumentReference.class);
        DocumentSnapshot existingDataset = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(ExternalDatasetLinkService.EXTERNAL_DATASETS_COLLECTION))
                .thenReturn(datasetsCollection);
        when(datasetsCollection.document("fall-survey-2026")).thenReturn(datasetDocument);
        when(datasetDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(existingDataset);
        when(existingDataset.exists()).thenReturn(true);
        when(datasetDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        ExternalDatasetDTO dto = new ExternalDatasetDTO();
        dto.setDatasetName("Updated Survey");
        dto.setActive(false);

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);
        service.updateExternalDataset("fall-survey-2026", dto);

        ArgumentCaptor<Map> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(datasetDocument).update(updateCaptor.capture());
        assertEquals("Updated Survey", updateCaptor.getValue().get("datasetName"));
        assertEquals(false, updateCaptor.getValue().get("active"));
        assertTrue(updateCaptor.getValue().containsKey("updatedAt"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createParticipantExternalLinkStoresValidatedActiveLinkWithoutPii() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference datasetsCollection = mock(CollectionReference.class);
        CollectionReference linksCollection = mock(CollectionReference.class);
        DocumentReference datasetDocument = mock(DocumentReference.class);
        DocumentReference linkDocument = mock(DocumentReference.class);
        DocumentSnapshot datasetSnapshot = mock(DocumentSnapshot.class);
        Query participantQuery = mock(Query.class);
        Query duplicateDatasetQuery = mock(Query.class);
        Query duplicateRecordQuery = mock(Query.class);
        Query duplicateActiveQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> participantFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> duplicateFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> datasetFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        QuerySnapshot participantSnapshot = mock(QuerySnapshot.class);
        QuerySnapshot duplicateSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot participantDocument = mock(QueryDocumentSnapshot.class);
        User participant = youthParticipant();
        ExternalDataset dataset = activeDataset();

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(ExternalDatasetLinkService.EXTERNAL_DATASETS_COLLECTION))
                .thenReturn(datasetsCollection);
        when(firestore.collection(ExternalDatasetLinkService.PARTICIPANT_EXTERNAL_LINKS_COLLECTION))
                .thenReturn(linksCollection);
        when(usersCollection.whereEqualTo("aspnParticipantId", "ASPN-2026-0001"))
                .thenReturn(participantQuery);
        when(participantQuery.get()).thenReturn(participantFuture);
        when(participantFuture.get()).thenReturn(participantSnapshot);
        when(participantSnapshot.getDocuments()).thenReturn(List.of(participantDocument));
        when(participantDocument.toObject(User.class)).thenReturn(participant);
        when(datasetsCollection.document("fall-survey-2026")).thenReturn(datasetDocument);
        when(datasetDocument.get()).thenReturn(datasetFuture);
        when(datasetFuture.get()).thenReturn(datasetSnapshot);
        when(datasetSnapshot.exists()).thenReturn(true);
        when(datasetSnapshot.toObject(ExternalDataset.class)).thenReturn(dataset);
        when(linksCollection.whereEqualTo("externalDatasetId", "fall-survey-2026"))
                .thenReturn(duplicateDatasetQuery);
        when(duplicateDatasetQuery.whereEqualTo("externalRecordId", "response-123"))
                .thenReturn(duplicateRecordQuery);
        when(duplicateRecordQuery.whereEqualTo("linkStatus", "active"))
                .thenReturn(duplicateActiveQuery);
        when(duplicateActiveQuery.get()).thenReturn(duplicateFuture);
        when(duplicateFuture.get()).thenReturn(duplicateSnapshot);
        when(duplicateSnapshot.getDocuments()).thenReturn(List.of());
        when(linksCollection.document(any(String.class))).thenReturn(linkDocument);
        when(linkDocument.set(any(ParticipantExternalLink.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        ParticipantExternalLinkDTO dto = new ParticipantExternalLinkDTO();
        dto.setAspnParticipantId("ASPN-2026-0001");
        dto.setExternalDatasetId("fall-survey-2026");
        dto.setExternalRecordId("response-123");
        dto.setLinkedByStaffUID("staff-123");
        dto.setNotes("Matched during pilot intake");

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);
        String linkId = service.createParticipantExternalLink(dto);

        ArgumentCaptor<ParticipantExternalLink> linkCaptor = ArgumentCaptor.forClass(ParticipantExternalLink.class);
        verify(linkDocument).set(linkCaptor.capture());
        ParticipantExternalLink savedLink = linkCaptor.getValue();
        assertEquals(linkId, savedLink.getLinkId());
        assertEquals("ASPN-2026-0001", savedLink.getAspnParticipantId());
        assertEquals("youth-123", savedLink.getUserUID());
        assertEquals("fall-survey-2026", savedLink.getExternalDatasetId());
        assertEquals("google_forms", savedLink.getExternalSource());
        assertEquals("response-123", savedLink.getExternalRecordId());
        assertEquals("active", savedLink.getLinkStatus());
        assertEquals("staff-123", savedLink.getLinkedByStaffUID());
    }

    @Test
    void createParticipantExternalLinkRejectsMissingParticipantIdBeforeFirestoreLookup() {
        ParticipantExternalLinkDTO dto = new ParticipantExternalLinkDTO();
        dto.setExternalDatasetId("fall-survey-2026");
        dto.setExternalRecordId("response-123");
        dto.setLinkedByStaffUID("staff-123");

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createParticipantExternalLink(dto)
        );

        assertEquals("aspnParticipantId is required", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createParticipantExternalLinkRejectsMissingParticipantProfile() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        Query participantQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> participantFuture = mock(ApiFuture.class);
        QuerySnapshot participantSnapshot = mock(QuerySnapshot.class);

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.whereEqualTo("aspnParticipantId", "ASPN-2026-0001"))
                .thenReturn(participantQuery);
        when(participantQuery.get()).thenReturn(participantFuture);
        when(participantFuture.get()).thenReturn(participantSnapshot);
        when(participantSnapshot.getDocuments()).thenReturn(List.of());

        ParticipantExternalLinkDTO dto = validLinkDto();

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createParticipantExternalLink(dto)
        );

        assertEquals("Participant profile does not exist", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createParticipantExternalLinkRejectsInactiveDataset() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference datasetsCollection = mock(CollectionReference.class);
        DocumentReference datasetDocument = mock(DocumentReference.class);
        DocumentSnapshot datasetSnapshot = mock(DocumentSnapshot.class);
        Query participantQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> participantFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> datasetFuture = mock(ApiFuture.class);
        QuerySnapshot participantSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot participantDocument = mock(QueryDocumentSnapshot.class);
        ExternalDataset inactiveDataset = activeDataset();
        inactiveDataset.setActive(false);

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(ExternalDatasetLinkService.EXTERNAL_DATASETS_COLLECTION))
                .thenReturn(datasetsCollection);
        when(usersCollection.whereEqualTo("aspnParticipantId", "ASPN-2026-0001"))
                .thenReturn(participantQuery);
        when(participantQuery.get()).thenReturn(participantFuture);
        when(participantFuture.get()).thenReturn(participantSnapshot);
        when(participantSnapshot.getDocuments()).thenReturn(List.of(participantDocument));
        when(participantDocument.toObject(User.class)).thenReturn(youthParticipant());
        when(datasetsCollection.document("fall-survey-2026")).thenReturn(datasetDocument);
        when(datasetDocument.get()).thenReturn(datasetFuture);
        when(datasetFuture.get()).thenReturn(datasetSnapshot);
        when(datasetSnapshot.exists()).thenReturn(true);
        when(datasetSnapshot.toObject(ExternalDataset.class)).thenReturn(inactiveDataset);

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createParticipantExternalLink(validLinkDto())
        );

        assertEquals("External dataset does not exist or is inactive", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createParticipantExternalLinkRejectsMissingDataset() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference datasetsCollection = mock(CollectionReference.class);
        DocumentReference datasetDocument = mock(DocumentReference.class);
        DocumentSnapshot datasetSnapshot = mock(DocumentSnapshot.class);
        Query participantQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> participantFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> datasetFuture = mock(ApiFuture.class);
        QuerySnapshot participantSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot participantDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(ExternalDatasetLinkService.EXTERNAL_DATASETS_COLLECTION))
                .thenReturn(datasetsCollection);
        when(usersCollection.whereEqualTo("aspnParticipantId", "ASPN-2026-0001"))
                .thenReturn(participantQuery);
        when(participantQuery.get()).thenReturn(participantFuture);
        when(participantFuture.get()).thenReturn(participantSnapshot);
        when(participantSnapshot.getDocuments()).thenReturn(List.of(participantDocument));
        when(participantDocument.toObject(User.class)).thenReturn(youthParticipant());
        when(datasetsCollection.document("fall-survey-2026")).thenReturn(datasetDocument);
        when(datasetDocument.get()).thenReturn(datasetFuture);
        when(datasetFuture.get()).thenReturn(datasetSnapshot);
        when(datasetSnapshot.exists()).thenReturn(false);

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createParticipantExternalLink(validLinkDto())
        );

        assertEquals("External dataset does not exist or is inactive", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createParticipantExternalLinkRejectsDuplicateActiveExternalRecordLink() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference datasetsCollection = mock(CollectionReference.class);
        CollectionReference linksCollection = mock(CollectionReference.class);
        DocumentReference datasetDocument = mock(DocumentReference.class);
        DocumentSnapshot datasetSnapshot = mock(DocumentSnapshot.class);
        Query participantQuery = mock(Query.class);
        Query duplicateDatasetQuery = mock(Query.class);
        Query duplicateRecordQuery = mock(Query.class);
        Query duplicateActiveQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> participantFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> duplicateFuture = mock(ApiFuture.class);
        ApiFuture<DocumentSnapshot> datasetFuture = mock(ApiFuture.class);
        QuerySnapshot participantSnapshot = mock(QuerySnapshot.class);
        QuerySnapshot duplicateSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot participantDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot duplicateDocument = mock(QueryDocumentSnapshot.class);

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(ExternalDatasetLinkService.EXTERNAL_DATASETS_COLLECTION))
                .thenReturn(datasetsCollection);
        when(firestore.collection(ExternalDatasetLinkService.PARTICIPANT_EXTERNAL_LINKS_COLLECTION))
                .thenReturn(linksCollection);
        when(usersCollection.whereEqualTo("aspnParticipantId", "ASPN-2026-0001"))
                .thenReturn(participantQuery);
        when(participantQuery.get()).thenReturn(participantFuture);
        when(participantFuture.get()).thenReturn(participantSnapshot);
        when(participantSnapshot.getDocuments()).thenReturn(List.of(participantDocument));
        when(participantDocument.toObject(User.class)).thenReturn(youthParticipant());
        when(datasetsCollection.document("fall-survey-2026")).thenReturn(datasetDocument);
        when(datasetDocument.get()).thenReturn(datasetFuture);
        when(datasetFuture.get()).thenReturn(datasetSnapshot);
        when(datasetSnapshot.exists()).thenReturn(true);
        when(datasetSnapshot.toObject(ExternalDataset.class)).thenReturn(activeDataset());
        when(linksCollection.whereEqualTo("externalDatasetId", "fall-survey-2026"))
                .thenReturn(duplicateDatasetQuery);
        when(duplicateDatasetQuery.whereEqualTo("externalRecordId", "response-123"))
                .thenReturn(duplicateRecordQuery);
        when(duplicateRecordQuery.whereEqualTo("linkStatus", "active"))
                .thenReturn(duplicateActiveQuery);
        when(duplicateActiveQuery.get()).thenReturn(duplicateFuture);
        when(duplicateFuture.get()).thenReturn(duplicateSnapshot);
        when(duplicateSnapshot.getDocuments()).thenReturn(List.of(duplicateDocument));

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createParticipantExternalLink(validLinkDto())
        );

        assertEquals("Active external record link already exists", exception.getMessage());
        verify(linksCollection, never()).document(any(String.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void removeParticipantExternalLinkMarksRemovedWithoutDeleting() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference linksCollection = mock(CollectionReference.class);
        DocumentReference linkDocument = mock(DocumentReference.class);
        DocumentSnapshot linkSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(ExternalDatasetLinkService.PARTICIPANT_EXTERNAL_LINKS_COLLECTION))
                .thenReturn(linksCollection);
        when(linksCollection.document("link-123")).thenReturn(linkDocument);
        when(linkDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(linkSnapshot);
        when(linkSnapshot.exists()).thenReturn(true);
        when(linkDocument.update(
                eq("linkStatus"), eq("removed"),
                eq("removedAt"), any(),
                eq("removedByStaffUID"), eq("staff-123"),
                eq("updatedAt"), any()
        )).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);
        service.removeParticipantExternalLink("link-123", "staff-123");

        verify(linkDocument).update(
                eq("linkStatus"), eq("removed"),
                eq("removedAt"), any(),
                eq("removedByStaffUID"), eq("staff-123"),
                eq("updatedAt"), any()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void getLinksByParticipantAndDatasetReturnKnownLinkStatuses() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference linksCollection = mock(CollectionReference.class);
        Query participantQuery = mock(Query.class);
        Query datasetQuery = mock(Query.class);
        ApiFuture<QuerySnapshot> participantFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> datasetFuture = mock(ApiFuture.class);
        QuerySnapshot participantSnapshot = mock(QuerySnapshot.class);
        QuerySnapshot datasetSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot activeDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot removedDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot invalidDocument = mock(QueryDocumentSnapshot.class);
        ParticipantExternalLink activeLink = linkWithStatus("active");
        ParticipantExternalLink removedLink = linkWithStatus("removed");
        ParticipantExternalLink invalidLink = linkWithStatus("unexpected");

        when(firestore.collection(ExternalDatasetLinkService.PARTICIPANT_EXTERNAL_LINKS_COLLECTION))
                .thenReturn(linksCollection);
        when(linksCollection.whereEqualTo("aspnParticipantId", "ASPN-2026-0001"))
                .thenReturn(participantQuery);
        when(participantQuery.get()).thenReturn(participantFuture);
        when(participantFuture.get()).thenReturn(participantSnapshot);
        when(participantSnapshot.getDocuments()).thenReturn(List.of(activeDocument, invalidDocument));
        when(activeDocument.toObject(ParticipantExternalLink.class)).thenReturn(activeLink);
        when(invalidDocument.toObject(ParticipantExternalLink.class)).thenReturn(invalidLink);
        when(linksCollection.whereEqualTo("externalDatasetId", "fall-survey-2026"))
                .thenReturn(datasetQuery);
        when(datasetQuery.get()).thenReturn(datasetFuture);
        when(datasetFuture.get()).thenReturn(datasetSnapshot);
        when(datasetSnapshot.getDocuments()).thenReturn(List.of(removedDocument));
        when(removedDocument.toObject(ParticipantExternalLink.class)).thenReturn(removedLink);

        ExternalDatasetLinkService service = new ExternalDatasetLinkService(firestore);

        List<ParticipantExternalLink> participantLinks = service.getLinksByParticipant("ASPN-2026-0001");
        List<ParticipantExternalLink> datasetLinks = service.getLinksByDataset("fall-survey-2026");

        assertEquals(1, participantLinks.size());
        assertEquals("active", participantLinks.get(0).getLinkStatus());
        assertEquals(1, datasetLinks.size());
        assertEquals("removed", datasetLinks.get(0).getLinkStatus());
        assertFalse(participantLinks.contains(invalidLink));
    }

    private ParticipantExternalLinkDTO validLinkDto() {
        ParticipantExternalLinkDTO dto = new ParticipantExternalLinkDTO();
        dto.setAspnParticipantId("ASPN-2026-0001");
        dto.setExternalDatasetId("fall-survey-2026");
        dto.setExternalRecordId("response-123");
        dto.setLinkedByStaffUID("staff-123");
        return dto;
    }

    private User youthParticipant() {
        User participant = new User();
        participant.setUid("youth-123");
        participant.setAspnParticipantId("ASPN-2026-0001");
        participant.setYouthProfile(true);
        return participant;
    }

    private ExternalDataset activeDataset() {
        ExternalDataset dataset = new ExternalDataset();
        dataset.setExternalDatasetId("fall-survey-2026");
        dataset.setDatasetName("Fall Survey 2026");
        dataset.setExternalSource("google_forms");
        dataset.setActive(true);
        return dataset;
    }

    private ParticipantExternalLink linkWithStatus(String status) {
        ParticipantExternalLink link = new ParticipantExternalLink();
        link.setLinkId("link-" + status);
        link.setLinkStatus(status);
        return link;
    }
}
