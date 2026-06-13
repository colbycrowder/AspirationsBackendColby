package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class GovernmentOrganization {
    private String governmentOrganizationId;
    private String organizationName;
    private String governmentLevel;
    private String organizationType;
    private String website;
    private String primaryContactName;
    private String primaryContactTitle;
    private String primaryContactEmail;
    private String primaryContactPhone;
    private boolean active = true;
    private boolean workforcePartner;
    private boolean credentialPartner;
    private String notes;
    private Date createdAt;
    private Date updatedAt;
}
