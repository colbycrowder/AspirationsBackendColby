package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwardCredentialDTO {
    private String credentialID;
    private String credentialIdentifier;
    private String userIdentifier;
    private String userUID;
    private String awardedByStaffUID;
}
