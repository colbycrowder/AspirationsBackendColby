package com.AspirationsNetwork.UserData.DTO;

import com.AspirationsNetwork.UserData.Models.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserProfileWithCredentialsDTO {
    private User user;
    private List<EarnedCredentialDisplayDTO> earnedCredentials = new ArrayList<>();
}
