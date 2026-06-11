package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ServiceHourRecord {
    private String serviceHourRecordId;
    private String userUID;
    private String programId;
    private Date serviceDate;
    private double hours;
    private String description;
    private String verificationStatus = "pending";
    private String verificationSource;
    private String googleFormResponseUrl;
    private String reviewedByStaffUID;
    private Date submittedAt;
    private Date reviewedAt;
    private Date createdAt;
    private Date updatedAt;
}
