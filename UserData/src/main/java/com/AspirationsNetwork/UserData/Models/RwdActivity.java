package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RwdActivity {
    private String rwdActivityId;
    private String countryName;
    private String title;
    private String description;
    private String externalUrl;
    private boolean active = true;
    private String associatedCredentialId;
    private Date createdAt;
    private Date updatedAt;
}
