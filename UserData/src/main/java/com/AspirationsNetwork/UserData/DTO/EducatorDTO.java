package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EducatorDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String title;
    private String organizationName;
    private String organizationType;
    private Boolean active;
    private String notes;
}
