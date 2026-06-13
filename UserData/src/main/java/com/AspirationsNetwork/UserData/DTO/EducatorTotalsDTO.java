package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class EducatorTotalsDTO {
    private long totalEducators;
    private long activeEducators;
    private long inactiveEducators;
    private long organizationsRepresented;
    private Map<String, Long> educatorsByOrganizationType = new HashMap<>();
}
