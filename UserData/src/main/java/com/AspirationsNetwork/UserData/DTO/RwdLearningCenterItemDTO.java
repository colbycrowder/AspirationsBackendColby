package com.AspirationsNetwork.UserData.DTO;

import com.AspirationsNetwork.UserData.Models.RwdProgress;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RwdLearningCenterItemDTO {
    private String rwdActivityId;
    private String countryName;
    private String title;
    private String description;
    private String externalUrl;
    private boolean active;
    private String associatedCredentialId;
    private RwdProgress progress;
}
