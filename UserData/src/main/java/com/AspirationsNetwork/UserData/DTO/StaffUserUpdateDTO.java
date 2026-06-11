package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StaffUserUpdateDTO {
    private String profileStatus;
    private List<String> programIds;
    private Boolean staffReviewRequired;
    private Boolean staffVerified;
}
