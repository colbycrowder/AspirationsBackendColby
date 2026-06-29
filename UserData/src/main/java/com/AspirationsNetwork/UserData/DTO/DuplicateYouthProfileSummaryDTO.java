package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DuplicateYouthProfileSummaryDTO {
    private String uid;
    private String name;
    private String email;
    private String aspnParticipantId;
    private String profileStatus;
    private boolean staffVerified;
    private boolean dashboardRecordsAvailable;
}
