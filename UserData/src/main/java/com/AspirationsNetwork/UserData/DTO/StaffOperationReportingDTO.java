package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class StaffOperationReportingDTO {
    private int totalOperations;
    private int operationsLast30Days;
    private int operationsLast60Days;
    private int operationsLast90Days;
    private Map<String, Integer> operationsByType = new HashMap<>();
    private Map<String, Integer> operationsByStaffUser = new HashMap<>();
    private Map<String, Integer> operationsByTargetType = new HashMap<>();
}
