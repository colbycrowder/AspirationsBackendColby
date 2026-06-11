package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SystemSetting {
    private String settingKey;
    private String settingValue;
    private String updatedByStaffUID;
    private Date createdAt;
    private Date updatedAt;
}
