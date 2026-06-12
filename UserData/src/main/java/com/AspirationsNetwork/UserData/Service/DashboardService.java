package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.YouthDashboardDTO;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.AspirationsNetwork.UserData.Models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final String ASPIRATIONS_NETWORK_URL = "https://aspirationsnetwork.org/";

    private final UserInfoService userInfoService;
    private final CredentialService credentialService;
    private final AttendanceService attendanceService;
    private final ServiceHourService serviceHourService;
    private final ProgramEnrollmentService programEnrollmentService;
    private final ProgramService programService;
    private final RwdLearningService rwdLearningService;
    private final SystemSettingsService systemSettingsService;
    private final NotificationService notificationService;

    public YouthDashboardDTO getYouthDashboard(String userUID) throws Exception {
        User user = userInfoService.getUser(userUID);
        if (user == null) {
            return null;
        }

        YouthDashboardDTO dashboard = new YouthDashboardDTO();
        dashboard.setProfileSummary(toProfileSummary(user));
        dashboard.setEarnedCredentials(credentialService.getEarnedCredentialsForUser(userUID));
        dashboard.setAttendanceRecords(attendanceService.getAttendanceRecordsForUser(userUID));
        dashboard.setServiceHourRecords(serviceHourService.getServiceHourRecordsForUser(userUID));
        List<Program> enrolledActivePrograms = getEnrolledActiveProgramModels(userUID);
        dashboard.setPrograms(toDashboardPrograms(enrolledActivePrograms));
        dashboard.setAvailableCredentials(credentialService.getAvailableCredentialsForPrograms(
                userUID,
                enrolledActivePrograms.stream().map(Program::getProgramId).toList()
        ));
        dashboard.setRwdLearningCenter(rwdLearningService.getLearningCenterForUser(userUID));
        dashboard.setServiceHourRequestFormUrl(systemSettingsService.getServiceHourRequestFormUrl());
        dashboard.setUnreadNotificationCount(notificationService.getUnreadNotificationCount(userUID));
        dashboard.getOpportunities().add(toAspnOpportunityLink());
        return dashboard;
    }

    private YouthDashboardDTO.ProfileSummaryDTO toProfileSummary(User user) {
        YouthDashboardDTO.ProfileSummaryDTO summary = new YouthDashboardDTO.ProfileSummaryDTO();
        summary.setFirstName(user.getFirstName());
        summary.setLastName(user.getLastName());
        summary.setEmail(user.getEmail());
        summary.setProfileImageUrl(user.getProfileImageUrl());
        summary.setUseFirstInitialFallback(user.getProfileImageUrl() == null || user.getProfileImageUrl().isBlank());
        summary.setFirstInitial(getFirstInitial(user.getFirstName()));
        summary.setProfileStatus(user.getProfileStatus());
        summary.setSchool(user.getSchool());
        summary.setGraduationYear(user.getGraduationYear());
        summary.setAspnParticipantId(user.getAspnParticipantId());
        return summary;
    }

    private List<Program> getEnrolledActiveProgramModels(String userUID) throws Exception {
        List<Program> programs = new ArrayList<>();
        for (ProgramEnrollment enrollment : programEnrollmentService.getEnrollmentsForUser(userUID)) {
            if (!"active".equals(enrollment.getEnrollmentStatus())) {
                continue;
            }

            Program program = getActiveProgram(enrollment.getProgramId());
            if (program != null) {
                programs.add(program);
            }
        }
        return programs;
    }

    private List<YouthDashboardDTO.DashboardProgramDTO> toDashboardPrograms(List<Program> programs) {
        return programs.stream()
                .map(this::toDashboardProgram)
                .toList();
    }

    private Program getActiveProgram(String programId) {
        try {
            return programService.getActiveProgramById(programId);
        } catch (Exception e) {
            return null;
        }
    }

    private YouthDashboardDTO.DashboardProgramDTO toDashboardProgram(Program program) {
        YouthDashboardDTO.DashboardProgramDTO dto = new YouthDashboardDTO.DashboardProgramDTO();
        dto.setProgramId(program.getProgramId());
        dto.setProgramName(program.getProgramName());
        dto.setDescription(program.getDescription());
        dto.setCategory(program.getCategory());
        dto.setProgramImageUrl(program.getProgramImageUrl());
        dto.setProgramLeader(program.getProgramLeader());
        dto.setProgramStatus(program.getProgramStatus());
        return dto;
    }

    private YouthDashboardDTO.DashboardLinkDTO toAspnOpportunityLink() {
        YouthDashboardDTO.DashboardLinkDTO link = new YouthDashboardDTO.DashboardLinkDTO();
        link.setTitle("Aspirations Network");
        link.setUrl(ASPIRATIONS_NETWORK_URL);
        return link;
    }

    private String getFirstInitial(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            return "";
        }
        return firstName.substring(0, 1).toUpperCase();
    }
}
