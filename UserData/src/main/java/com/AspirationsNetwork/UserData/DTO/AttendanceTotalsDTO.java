package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceTotalsDTO {
    private int totalRecords;
    private int present;
    private int absent;
    private int excused;
    private int pending;
}
