package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ProgramEnrollment {
    private String enrollmentId;
    private String userUID;
    private String programId;
    private String enrollmentStatus = "active";
    private Date enrolledAt;
    private Date updatedAt;
    private boolean createdByUser = true;
    private String removedByStaffUID;
}
