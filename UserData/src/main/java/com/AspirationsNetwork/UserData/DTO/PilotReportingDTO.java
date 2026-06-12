package com.AspirationsNetwork.UserData.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PilotReportingDTO {
    private ParticipationMetricsDTO participation = new ParticipationMetricsDTO();
    private ActiveUserMetricsDTO activeUsers = new ActiveUserMetricsDTO();
    private RetentionMetricsDTO retention = new RetentionMetricsDTO();
    private CredentialMetricsDTO credentials = new CredentialMetricsDTO();
    private List<ProgramReportingDTO> programs = new ArrayList<>();

    @Getter
    @Setter
    public static class ParticipationMetricsDTO {
        private int totalRegisteredYouth;
        private int profileCompletedYouth;
        private double profileCompletionPercentage;
        private int programParticipants;
        private double programParticipationPercentage;
        private int credentialParticipants;
        private double credentialParticipationPercentage;
    }

    @Getter
    @Setter
    public static class ActiveUserMetricsDTO {
        private int activeUsersLast30Days;
        private int activeUsersLast60Days;
        private int activeUsersLast90Days;
    }

    @Getter
    @Setter
    public static class RetentionMetricsDTO {
        private int retentionEligibleParticipants;
        private int retained30DayParticipants;
        private double retention30DayPercentage;
        private int retained60DayParticipants;
        private double retention60DayPercentage;
        private int retained90DayParticipants;
        private double retention90DayPercentage;
    }

    @Getter
    @Setter
    public static class CredentialMetricsDTO {
        private int totalCredentialsEarned;
        private Map<String, Integer> credentialsByCategory = new HashMap<>();
    }

    @Getter
    @Setter
    public static class ProgramReportingDTO {
        private String programId;
        private String programName;
        private String category;
        private String programStatus;
        private int registrations;
        private int credentialCompletions;
        private int activeParticipants;
    }
}
