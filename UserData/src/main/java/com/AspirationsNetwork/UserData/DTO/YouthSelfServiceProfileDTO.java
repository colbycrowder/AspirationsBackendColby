package com.AspirationsNetwork.UserData.DTO;

import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import com.AspirationsNetwork.UserData.Models.User;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class YouthSelfServiceProfileDTO {
    private User user;
    private List<EarnedCredentialDisplayDTO> earnedCredentials = new ArrayList<>();
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private List<ServiceHourRecord> serviceHourRecords = new ArrayList<>();
}
