package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.SystemSettingDTO;
import com.AspirationsNetwork.UserData.Models.SystemSetting;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemSettingsServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void setServiceHourRequestFormUrlStoresSystemSetting() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference settingsCollection = mock(CollectionReference.class);
        DocumentReference settingDocument = mock(DocumentReference.class);
        DocumentSnapshot existingSettingSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(SystemSettingsService.COLLECTION_NAME)).thenReturn(settingsCollection);
        when(settingsCollection.document(SystemSettingsService.SERVICE_HOUR_REQUEST_FORM_URL_KEY))
                .thenReturn(settingDocument);
        when(settingDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(existingSettingSnapshot);
        when(existingSettingSnapshot.exists()).thenReturn(false);
        when(settingDocument.set(any(SystemSetting.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        SystemSettingDTO dto = new SystemSettingDTO();
        dto.setSettingValue("https://example.com/service-hours");
        dto.setUpdatedByStaffUID("staff-123");

        SystemSettingsService service = new SystemSettingsService(firestore);
        service.setServiceHourRequestFormUrl(dto);

        ArgumentCaptor<SystemSetting> settingCaptor = ArgumentCaptor.forClass(SystemSetting.class);
        verify(settingDocument).set(settingCaptor.capture());

        SystemSetting setting = settingCaptor.getValue();
        assertEquals(SystemSettingsService.SERVICE_HOUR_REQUEST_FORM_URL_KEY, setting.getSettingKey());
        assertEquals("https://example.com/service-hours", setting.getSettingValue());
        assertEquals("staff-123", setting.getUpdatedByStaffUID());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getServiceHourRequestFormUrlReturnsNullWhenSettingIsMissing() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference settingsCollection = mock(CollectionReference.class);
        DocumentReference settingDocument = mock(DocumentReference.class);
        DocumentSnapshot settingSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);

        when(firestore.collection(SystemSettingsService.COLLECTION_NAME)).thenReturn(settingsCollection);
        when(settingsCollection.document(SystemSettingsService.SERVICE_HOUR_REQUEST_FORM_URL_KEY))
                .thenReturn(settingDocument);
        when(settingDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(settingSnapshot);
        when(settingSnapshot.exists()).thenReturn(false);

        SystemSettingsService service = new SystemSettingsService(firestore);

        assertNull(service.getServiceHourRequestFormUrl());
    }
}
