package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AttendanceRecordCreationDTO {
    private String userIdentifier;
    private String userUID;
    private String programID;
    private String eventName;
    private Date eventDate;
    private String attendanceStatus;
    private String staffRecorderUID;
}
