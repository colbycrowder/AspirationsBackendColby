package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.SystemSettingDTO;
import com.AspirationsNetwork.UserData.Models.SystemSetting;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class SystemSettingsService {
    public static final String COLLECTION_NAME = "systemSettings";
    public static final String SERVICE_HOUR_REQUEST_FORM_URL_KEY = "serviceHourRequestFormUrl";

    private final Firestore firestore;

    public void setServiceHourRequestFormUrl(SystemSettingDTO dto) throws Exception {
        if (dto == null) {
            throw new IllegalArgumentException("system setting request is required");
        }
        requireText(dto.getUpdatedByStaffUID(), "updatedByStaffUID is required");

        Date now = new Date();
        DocumentReference settingRef = firestore.collection(COLLECTION_NAME)
                .document(SERVICE_HOUR_REQUEST_FORM_URL_KEY);
        DocumentSnapshot existingSetting = settingRef.get().get();

        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(SERVICE_HOUR_REQUEST_FORM_URL_KEY);
        setting.setSettingValue(dto.getSettingValue());
        setting.setUpdatedByStaffUID(dto.getUpdatedByStaffUID());
        setting.setCreatedAt(getCreatedAtOrNow(existingSetting, now));
        setting.setUpdatedAt(now);

        settingRef.set(setting).get();
    }

    public String getServiceHourRequestFormUrl() throws Exception {
        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(SERVICE_HOUR_REQUEST_FORM_URL_KEY)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        SystemSetting setting = document.toObject(SystemSetting.class);
        return setting == null ? null : setting.getSettingValue();
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private Date getCreatedAtOrNow(DocumentSnapshot existingSetting, Date now) {
        if (!existingSetting.exists()) {
            return now;
        }

        SystemSetting setting = existingSetting.toObject(SystemSetting.class);
        return setting == null || setting.getCreatedAt() == null ? now : setting.getCreatedAt();
    }
}
