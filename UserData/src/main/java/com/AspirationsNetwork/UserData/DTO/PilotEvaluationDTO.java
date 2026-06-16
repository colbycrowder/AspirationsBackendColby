package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PilotEvaluationDTO {
    private int overallScore;
    private String overallStatus;
    private Date generatedAt;

    private int youthOutcomeScore;
    private int programOutcomeScore;
    private int credentialOutcomeScore;
    private int serviceOutcomeScore;
    private int stakeholderOutcomeScore;
    private int operationsOutcomeScore;

    private int registrations;
    private int activeUsers;
    private double profileCompletionRate;
    private int active30DayUsers;
    private double retentionRate;

    private int enrollments;
    private int activeEnrollments;
    private double attendanceRate;
    private double participationRate;

    private int credentialsAwarded;
    private double credentialsPerParticipant;
    private double credentialParticipationRate;

    private double approvedServiceHours;
    private int serviceHourParticipants;
    private double averageHoursPerParticipant;

    private int educatorCount;
    private int partnerCount;
    private int governmentOrganizationCount;
    private double relationshipFollowUpCompletionRate;

    private int staffActionsLast30Days;
    private int platformEventsLast30Days;
    private int relationshipNoteActivity;

    private List<String> strengths = new ArrayList<>();
    private List<String> concerns = new ArrayList<>();
    private List<String> recommendedActions = new ArrayList<>();
}
