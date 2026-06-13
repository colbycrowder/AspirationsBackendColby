package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GovernmentOrganizationDTO {
    private String organizationName;
    private String governmentLevel;
    private String organizationType;
    private String website;
    private String primaryContactName;
    private String primaryContactTitle;
    private String primaryContactEmail;
    private String primaryContactPhone;
    private Boolean active;
    private Boolean workforcePartner;
    private Boolean credentialPartner;
    private String notes;
}
