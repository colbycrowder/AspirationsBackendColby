package com.AspirationsNetwork.UserData.DTO;

import com.AspirationsNetwork.UserData.Models.CredentialRequirement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CredentialDefinitionUpdateDTO {
    private String credentialName;
    private String description;
    private String icon;
    private String category;
    private Boolean active;
    private List<String> programIds;
    private List<CredentialRequirement> requirements;
    private String requirementText;
    private Boolean autoAwardEnabled;
    private String requirementType;
    private Integer requiredAttendanceCount;
}
