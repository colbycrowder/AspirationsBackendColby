package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RwdProgress {
    private String progressId;
    private String userUID;
    private String rwdActivityId;
    private String completionStatus = "not_started";
    private Integer quizScore;
    private boolean passed = false;
    private Date completedAt;
    private boolean credentialAwarded = false;
    private String earnedCredentialId;
}
