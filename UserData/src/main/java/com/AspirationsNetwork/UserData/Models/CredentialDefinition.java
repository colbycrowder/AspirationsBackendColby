package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CredentialDefinition {
    private String credentialID;
    private String credentialName;
    private String description;
    private String icon;
    private String category;
    private boolean active = false;
    private List<String> programIds = new ArrayList<>();
    private List<CredentialRequirement> requirements = new ArrayList<>();
    private String requirementText;
    private boolean autoAwardEnabled = false;
    private String requirementType;
    private Integer requiredAttendanceCount;
    private String createdByStaffUID;
    private Date createdAt;
    private Date updatedAt;
}
