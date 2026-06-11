package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CredentialRequirement {
    private String requirementType;
    private String requirementText;
    private Integer requiredCount;
    private String relatedProgramId;
    private String relatedFormId;
}
