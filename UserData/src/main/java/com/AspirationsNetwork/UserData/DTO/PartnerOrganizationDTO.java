package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartnerOrganizationDTO {
    private String organizationName;
    private String organizationType;
    private String website;
    private String primaryContactName;
    private String primaryContactEmail;
    private String primaryContactPhone;
    private Boolean active;
    private String notes;
}
