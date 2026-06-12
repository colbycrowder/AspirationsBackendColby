package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class StaffOperationEvent {
    private String staffOperationEventId;
    private String staffUID;
    private String operationType;
    private String targetType;
    private String targetId;
    private String targetUserUID;
    private Date createdAt;
    private Map<String, Object> metadata = new HashMap<>();
}
