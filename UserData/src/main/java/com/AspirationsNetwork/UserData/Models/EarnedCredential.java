package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class EarnedCredential {
    private String earnedCredentialID;
    private String credentialID;
    private String credentialId;
    private String id;
    private String credentialName;
    private String name;
    private String userUID;
    private String awardedByStaffUID;
    private String status = "pending_review";
    private Date earnedAt;
    private Date awardedAt;
    private Date updatedAt;

    public String getCredentialID() {
        return credentialID;
    }

    public void setCredentialID(String credentialID) {
        this.credentialID = credentialID;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }
}
