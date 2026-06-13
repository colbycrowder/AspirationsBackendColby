package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PartnerOrganization {
    private String partnerOrganizationId;
    private String organizationName;
    private String organizationType;
    private String website;
    private String primaryContactName;
    private String primaryContactEmail;
    private String primaryContactPhone;
    private boolean active = true;
    private String notes;
    private Date createdAt;
    private Date updatedAt;
}
