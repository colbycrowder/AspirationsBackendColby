package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YouthProfileCompletionDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String school;
    private String graduationYear;
    private List<String> collegeInterests;
    private List<String> careerInterests;
    private List<String> civicInterests;
    private List<String> communityInterests;
    private List<String> publicServiceInterests;
}
