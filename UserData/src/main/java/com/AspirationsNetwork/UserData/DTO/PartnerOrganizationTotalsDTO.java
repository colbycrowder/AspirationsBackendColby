package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PartnerOrganizationTotalsDTO {
    private long totalPartners;
    private long activePartners;
    private long inactivePartners;
    private long organizationTypesRepresented;
    private Map<String, Long> partnersByOrganizationType = new HashMap<>();
}
