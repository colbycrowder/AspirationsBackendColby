package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Notification {
    private String notificationId;
    private String userUID;
    private String notificationType;
    private String title;
    private String message;
    private String relatedCredentialId;
    private String relatedEarnedCredentialId;
    private boolean read = false;
    private Date createdAt;
}
