package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StakeholderRelationshipNote {
    private String stakeholderRelationshipNoteId;
    private String stakeholderType;
    private String stakeholderId;
    private String stakeholderName;
    private String noteText;
    private String relationshipStatus;
    private String relationshipOwnerUID;
    private Date lastContactDate;
    private Date nextFollowUpDate;
    private boolean active = true;
    private Date createdAt;
    private Date updatedAt;
}
