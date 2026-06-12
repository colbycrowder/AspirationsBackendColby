package com.AspirationsNetwork.UserData.DTO;

import com.AspirationsNetwork.UserData.Models.Program;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramDetailDTO {
    private Program program;
    private long enrollmentCount;
    private long credentialCount;
    private long attendanceCount;
    private long serviceHourRecordCount;
    private double serviceHourTotal;
}
