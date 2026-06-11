package com.AspirationsNetwork.UserData.Models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AttendanceRecord {
    private String attendanceRecordID;
    private String userUID;
    private String programID;
    private String eventName;
    private Date eventDate;
    private String attendanceStatus = "pending";
    private String staffRecorderUID;
    private Date createdAt;
    private Date updatedAt;
}
