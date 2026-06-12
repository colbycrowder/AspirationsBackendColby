package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ServiceHourTotalsDTO {
    private int totalRecords;
    private double totalHours;
    private double pendingHours;
    private double verifiedHours;
    private double rejectedHours;
    private Map<String, Integer> recordsByStatus = new HashMap<>();
}
