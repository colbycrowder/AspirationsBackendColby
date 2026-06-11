package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ServiceHourRecordDTO {
    private String userUID;
    private String programId;
    private Date serviceDate;
    private double hours;
    private String description;
    private String verificationStatus;
    private String verificationSource;
    private String googleFormResponseUrl;
    private String reviewedByStaffUID;
    private Date submittedAt;
    private Date reviewedAt;
}
