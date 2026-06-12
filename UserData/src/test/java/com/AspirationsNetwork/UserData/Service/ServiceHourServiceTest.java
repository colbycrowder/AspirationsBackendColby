package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ServiceHourRecordDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourTotalsDTO;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceHourServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createOrReviewServiceHourRecordStoresRecordAndLinksToUserProfile() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference serviceHourDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        Date serviceDate = new Date();

        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(serviceHoursCollection.document(any(String.class))).thenReturn(serviceHourDocument);
        when(usersCollection.document("youth-123")).thenReturn(userDocument);
        when(serviceHourDocument.set(any(ServiceHourRecord.class))).thenReturn(writeFuture);
        when(userDocument.update(eq("serviceHourRecordIds"), any())).thenReturn(updateFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        ServiceHourRecordDTO dto = new ServiceHourRecordDTO();
        dto.setUserUID("youth-123");
        dto.setProgramId("program-123");
        dto.setServiceDate(serviceDate);
        dto.setHours(2.5);
        dto.setDescription("Community event support");
        dto.setVerificationStatus("VERIFIED");
        dto.setVerificationSource("staff_review");
        dto.setGoogleFormResponseUrl("future-form-response-placeholder");
        dto.setReviewedByStaffUID("staff-123");

        ServiceHourService service = new ServiceHourService(firestore);
        String serviceHourRecordId = service.createOrReviewServiceHourRecord(dto);

        ArgumentCaptor<ServiceHourRecord> recordCaptor = ArgumentCaptor.forClass(ServiceHourRecord.class);
        verify(serviceHourDocument).set(recordCaptor.capture());
        verify(userDocument).update(eq("serviceHourRecordIds"), any());

        ServiceHourRecord savedRecord = recordCaptor.getValue();
        assertEquals(serviceHourRecordId, savedRecord.getServiceHourRecordId());
        assertEquals("youth-123", savedRecord.getUserUID());
        assertEquals("program-123", savedRecord.getProgramId());
        assertEquals(serviceDate, savedRecord.getServiceDate());
        assertEquals(2.5, savedRecord.getHours());
        assertEquals("Community event support", savedRecord.getDescription());
        assertEquals("verified", savedRecord.getVerificationStatus());
        assertEquals("staff_review", savedRecord.getVerificationSource());
        assertEquals("future-form-response-placeholder", savedRecord.getGoogleFormResponseUrl());
        assertEquals("staff-123", savedRecord.getReviewedByStaffUID());
    }

    @Test
    void createOrReviewServiceHourRecordRejectsInvalidStatus() {
        ServiceHourRecordDTO dto = new ServiceHourRecordDTO();
        dto.setUserUID("youth-123");
        dto.setProgramId("program-123");
        dto.setHours(1.0);
        dto.setVerificationStatus("approved");
        dto.setReviewedByStaffUID("staff-123");

        ServiceHourService service = new ServiceHourService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.createOrReviewServiceHourRecord(dto)
        );

        assertEquals("verificationStatus must be pending, verified, or rejected", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getServiceHourRecordsFiltersByUserStatusProgramAndDate() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> serviceHoursFuture = mock(ApiFuture.class);
        QuerySnapshot serviceHoursSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot matchingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot otherStatusDocument = mock(QueryDocumentSnapshot.class);
        Date serviceDate = new Date();

        ServiceHourRecord matchingRecord = serviceHourRecord(
                "service-1",
                "youth-123",
                "program-123",
                "verified",
                serviceDate,
                2.5
        );
        ServiceHourRecord otherStatusRecord = serviceHourRecord(
                "service-2",
                "youth-123",
                "program-123",
                "pending",
                serviceDate,
                1.0
        );

        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        when(serviceHoursCollection.get()).thenReturn(serviceHoursFuture);
        when(serviceHoursFuture.get()).thenReturn(serviceHoursSnapshot);
        when(serviceHoursSnapshot.getDocuments()).thenReturn(List.of(matchingDocument, otherStatusDocument));
        when(matchingDocument.toObject(ServiceHourRecord.class)).thenReturn(matchingRecord);
        when(otherStatusDocument.toObject(ServiceHourRecord.class)).thenReturn(otherStatusRecord);

        ServiceHourService service = new ServiceHourService(firestore);
        List<ServiceHourRecord> records = service.getServiceHourRecords(
                "youth-123",
                "VERIFIED",
                "program-123",
                serviceDate
        );

        assertEquals(1, records.size());
        assertEquals("service-1", records.get(0).getServiceHourRecordId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getServiceHourTotalsCountsRecordsAndHoursByStatus() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        ApiFuture<QuerySnapshot> serviceHoursFuture = mock(ApiFuture.class);
        QuerySnapshot serviceHoursSnapshot = mock(QuerySnapshot.class);
        QueryDocumentSnapshot verifiedDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot pendingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot rejectedDocument = mock(QueryDocumentSnapshot.class);
        Date serviceDate = new Date();

        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        when(serviceHoursCollection.get()).thenReturn(serviceHoursFuture);
        when(serviceHoursFuture.get()).thenReturn(serviceHoursSnapshot);
        when(serviceHoursSnapshot.getDocuments()).thenReturn(List.of(
                verifiedDocument,
                pendingDocument,
                rejectedDocument
        ));
        when(verifiedDocument.toObject(ServiceHourRecord.class)).thenReturn(serviceHourRecord(
                "service-1",
                "youth-123",
                "program-123",
                "verified",
                serviceDate,
                2.5
        ));
        when(pendingDocument.toObject(ServiceHourRecord.class)).thenReturn(serviceHourRecord(
                "service-2",
                "youth-123",
                "program-123",
                "pending",
                serviceDate,
                1.5
        ));
        when(rejectedDocument.toObject(ServiceHourRecord.class)).thenReturn(serviceHourRecord(
                "service-3",
                "youth-123",
                "program-123",
                "rejected",
                serviceDate,
                1.0
        ));

        ServiceHourService service = new ServiceHourService(firestore);
        ServiceHourTotalsDTO totals = service.getServiceHourTotals("youth-123", null, "program-123", null);

        assertEquals(3, totals.getTotalRecords());
        assertEquals(5.0, totals.getTotalHours());
        assertEquals(2.5, totals.getVerifiedHours());
        assertEquals(1.5, totals.getPendingHours());
        assertEquals(1.0, totals.getRejectedHours());
        assertEquals(1, totals.getRecordsByStatus().get("verified"));
        assertEquals(1, totals.getRecordsByStatus().get("pending"));
        assertEquals(1, totals.getRecordsByStatus().get("rejected"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateServiceHourRecordStatusStoresVerifiedStaffReview() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        DocumentReference serviceHourDocument = mock(DocumentReference.class);
        DocumentSnapshot serviceHourSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        when(serviceHoursCollection.document("service-123")).thenReturn(serviceHourDocument);
        when(serviceHourDocument.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(serviceHourSnapshot);
        when(serviceHourSnapshot.exists()).thenReturn(true);
        when(serviceHourDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        ServiceHourService service = new ServiceHourService(firestore);
        service.updateServiceHourRecordStatus("service-123", "VERIFIED", "staff-123");

        ArgumentCaptor<Map> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(serviceHourDocument).update(updateCaptor.capture());
        assertEquals("verified", updateCaptor.getValue().get("verificationStatus"));
        assertEquals("staff-123", updateCaptor.getValue().get("reviewedByStaffUID"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void approveAndRejectServiceHourRecordUseExpectedStatuses() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        DocumentReference serviceHourDocument = mock(DocumentReference.class);
        DocumentSnapshot serviceHourSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        when(serviceHoursCollection.document("service-123")).thenReturn(serviceHourDocument);
        when(serviceHourDocument.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(serviceHourSnapshot);
        when(serviceHourSnapshot.exists()).thenReturn(true);
        when(serviceHourDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        ServiceHourService service = new ServiceHourService(firestore);
        service.approveServiceHourRecord("service-123", "staff-123");
        service.rejectServiceHourRecord("service-123", "staff-123");

        ArgumentCaptor<Map> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(serviceHourDocument, org.mockito.Mockito.times(2)).update(updateCaptor.capture());
        assertEquals("verified", updateCaptor.getAllValues().get(0).get("verificationStatus"));
        assertEquals("rejected", updateCaptor.getAllValues().get(1).get("verificationStatus"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteServiceHourRecordRemovesRecordAndUnlinksUserProfile() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference serviceHourDocument = mock(DocumentReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentSnapshot serviceHourSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> getFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> deleteFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> unlinkFuture = mock(ApiFuture.class);
        ServiceHourRecord record = new ServiceHourRecord();
        record.setUserUID("youth-123");

        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(serviceHoursCollection.document("service-123")).thenReturn(serviceHourDocument);
        when(usersCollection.document("youth-123")).thenReturn(userDocument);
        when(serviceHourDocument.get()).thenReturn(getFuture);
        when(getFuture.get()).thenReturn(serviceHourSnapshot);
        when(serviceHourSnapshot.exists()).thenReturn(true);
        when(serviceHourSnapshot.toObject(ServiceHourRecord.class)).thenReturn(record);
        when(serviceHourDocument.delete()).thenReturn(deleteFuture);
        when(deleteFuture.get()).thenReturn(mock(WriteResult.class));
        when(userDocument.update(eq("serviceHourRecordIds"), any())).thenReturn(unlinkFuture);
        when(unlinkFuture.get()).thenReturn(mock(WriteResult.class));

        ServiceHourService service = new ServiceHourService(firestore);
        service.deleteServiceHourRecord("service-123");

        verify(serviceHourDocument).delete();
        verify(userDocument).update(eq("serviceHourRecordIds"), any());
    }

    private ServiceHourRecord serviceHourRecord(
            String serviceHourRecordId,
            String userUID,
            String programId,
            String verificationStatus,
            Date serviceDate,
            double hours
    ) {
        ServiceHourRecord record = new ServiceHourRecord();
        record.setServiceHourRecordId(serviceHourRecordId);
        record.setUserUID(userUID);
        record.setProgramId(programId);
        record.setVerificationStatus(verificationStatus);
        record.setServiceDate(serviceDate);
        record.setHours(hours);
        return record;
    }
}
