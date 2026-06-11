package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EarnedCredentialDisplayDTO {
    private String earnedCredentialID;
    private String credentialID;
    private String credentialName;
    private String description;
    private String icon;
    private String category;
    private String status;
    private Date earnedAt;
    private Date awardedAt;
}
