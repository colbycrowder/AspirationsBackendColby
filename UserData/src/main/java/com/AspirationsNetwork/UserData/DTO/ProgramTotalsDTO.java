package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ProgramTotalsDTO {
    private long totalPrograms;
    private long activePrograms;
    private long archivedPrograms;
    private long totalEnrollments;
    private long totalCredentialsEarned;
    private long totalAttendanceRecords;
    private double totalServiceHours;
    private Map<String, Long> programsByStatus = new HashMap<>();
}
