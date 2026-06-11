package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RwdActivityDTO {
    private String countryName;
    private String title;
    private String description;
    private String externalUrl;
    private Boolean active;
    private String associatedCredentialId;
}
