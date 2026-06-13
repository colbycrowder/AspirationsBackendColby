package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class PilotMetricsDTO {
    private int totalRegistrations;
    private int activeUsers;
    private int profileCompletions;
    private double profileCompletionRate;
    private int activeLast30Days;
    private int activeLast60Days;
    private int activeLast90Days;

    private int totalPrograms;
    private int activePrograms;
    private int totalEnrollments;
    private int activeParticipants;
    private int attendanceRecords;
    private double attendanceRate;
    private Map<String, Integer> programParticipationCounts = new HashMap<>();

    private int credentialDefinitions;
    private int activeCredentialDefinitions;
    private int credentialsAwarded;
    private Map<String, Integer> credentialsByCategory = new HashMap<>();
    private Map<String, Integer> credentialsByProgram = new HashMap<>();

    private int serviceHourSubmissions;
    private int approvedServiceHourSubmissions;
    private double totalApprovedServiceHours;
    private Map<String, Double> serviceHoursByProgram = new HashMap<>();

    private int educators;
    private int partnerOrganizations;
    private int governmentOrganizations;
    private int relationshipNotes;
    private int activeRelationshipNotes;
    private int overdueFollowUps;
    private int upcomingFollowUps;

    private int staffOperationsLast30Days;
    private int staffOperationsLast60Days;
    private int staffOperationsLast90Days;
}
