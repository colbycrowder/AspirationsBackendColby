package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PlatformEvent {
    private String eventId;
    private String userUID;
    private String aspnParticipantId;
    private String eventType;
    private Date eventTimestamp;
    private Map<String, Object> metadata = new HashMap<>();
}
