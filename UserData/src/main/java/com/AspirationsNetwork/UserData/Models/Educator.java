package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Educator {
    private String educatorId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String title;
    private String organizationName;
    private String organizationType;
    private boolean active = true;
    private String notes;
    private Date createdAt;
    private Date updatedAt;
}
