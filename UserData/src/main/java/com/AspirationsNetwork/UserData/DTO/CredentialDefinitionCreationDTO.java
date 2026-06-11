package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import com.AspirationsNetwork.UserData.Models.CredentialRequirement;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CredentialDefinitionCreationDTO {
    private String credentialName;
    private String description;
    private String icon;
    private String category;
    private boolean active;
    private List<String> programIds = new ArrayList<>();
    private List<CredentialRequirement> requirements = new ArrayList<>();
    private String requirementText;
    private boolean autoAwardEnabled;
    private String requirementType;
    private Integer requiredAttendanceCount;
    private String createdByStaffUID;
}
