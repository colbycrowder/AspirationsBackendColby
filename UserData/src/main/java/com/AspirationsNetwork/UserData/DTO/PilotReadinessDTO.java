package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PilotReadinessDTO {
    private int readinessScore;
    private String readinessStatus;
    private Date generatedAt;
    private int totalYouthUsers;
    private int activeYouthUsers;
    private int completedProfiles;
    private double profileCompletionRate;
    private int activePrograms;
    private int activeCredentialDefinitions;
    private int attendanceRecords;
    private int serviceHourRecords;
    private int activeEducators;
    private int activePartnerOrganizations;
    private int activeGovernmentOrganizations;
    private int activeStakeholderRelationshipNotes;
    private int platformEventsLast30Days;
    private List<String> blockers = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<ChecklistItemDTO> checklistItems = new ArrayList<>();

    @Getter
    @Setter
    public static class ChecklistItemDTO {
        private String category;
        private String label;
        private boolean complete;
        private String detail;
    }
}
