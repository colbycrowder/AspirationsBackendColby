package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EarnedCredential {
    private String earnedCredentialID;
    private String credentialID;
    private String userUID;
    private String awardedByStaffUID;
    private String status = "pending_review";
    private Date earnedAt;
    private Date awardedAt;
    private Date updatedAt;
}
