package com.AspirationsNetwork.UserData.DTO;

import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class YouthDashboardDTO {
    private ProfileSummaryDTO profileSummary;
    private List<DashboardProgramDTO> programs = new ArrayList<>();
    private List<EarnedCredentialDisplayDTO> earnedCredentials = new ArrayList<>();
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    private List<ServiceHourRecord> serviceHourRecords = new ArrayList<>();
    private List<AvailableCredentialDTO> availableCredentials = new ArrayList<>();
    private List<RwdLearningCenterItemDTO> rwdLearningCenter = new ArrayList<>();
    private List<DashboardLinkDTO> opportunities = new ArrayList<>();
    private String serviceHourRequestFormUrl;
    private int unreadNotificationCount;

    @Getter
    @Setter
    public static class ProfileSummaryDTO {
        private String firstName;
        private String lastName;
        private String email;
        private String profileImageUrl;
        private boolean useFirstInitialFallback;
        private String firstInitial;
        private String profileStatus;
        private String school;
        private String graduationYear;
        private String aspnParticipantId;
    }

    @Getter
    @Setter
    public static class DashboardProgramDTO {
        private String programId;
        private String programName;
        private String description;
        private String category;
        private String programImageUrl;
        private String programLeader;
        private String programStatus;
    }

    @Getter
    @Setter
    public static class DashboardLinkDTO {
        private String title;
        private String url;
    }
}
