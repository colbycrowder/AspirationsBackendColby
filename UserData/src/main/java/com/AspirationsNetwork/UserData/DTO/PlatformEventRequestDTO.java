package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PlatformEventRequestDTO {
    private String eventType;
    private Map<String, Object> metadata = new HashMap<>();
}
