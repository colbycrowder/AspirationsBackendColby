package com.AspirationsNetwork.UserData.DTO;

import com.AspirationsNetwork.UserData.Models.CredentialRequirement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AvailableCredentialDTO {
    private String credentialID;
    private String credentialName;
    private String description;
    private String icon;
    private String category;
    private boolean active;
    private List<String> programIds = new ArrayList<>();
    private List<CredentialRequirement> requirements = new ArrayList<>();
    private String requirementText;
    private String status = "locked";
}
