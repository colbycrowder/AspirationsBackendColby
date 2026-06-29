package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class AttendanceBatchCreationDTO {
    private String programID;
    private String eventName;
    private Date eventDate;
    private String staffRecorderUID;
    private List<AttendanceRecordCreationDTO> records = new ArrayList<>();
}
