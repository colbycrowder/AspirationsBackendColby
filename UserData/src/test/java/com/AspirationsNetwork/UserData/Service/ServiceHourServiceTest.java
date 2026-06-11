package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ServiceHourRecordDTO;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;

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
}
