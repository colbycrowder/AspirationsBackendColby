package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class GovernmentOrganizationTotalsDTO {
    private long totalGovernmentOrganizations;
    private long activeGovernmentOrganizations;
    private long inactiveGovernmentOrganizations;
    private long workforcePartners;
    private long credentialPartners;
    private Map<String, Long> organizationsByGovernmentLevel = new HashMap<>();
    private Map<String, Long> organizationsByOrganizationType = new HashMap<>();
}
