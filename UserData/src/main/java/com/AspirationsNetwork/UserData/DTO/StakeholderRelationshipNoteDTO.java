package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StakeholderRelationshipNoteDTO {
    private String stakeholderType;
    private String stakeholderId;
    private String stakeholderName;
    private String noteText;
    private String relationshipStatus;
    private String relationshipOwnerUID;
    private Date lastContactDate;
    private Date nextFollowUpDate;
    private Boolean active;
}
