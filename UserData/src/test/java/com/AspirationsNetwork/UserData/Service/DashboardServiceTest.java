package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.AvailableCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.EarnedCredentialDisplayDTO;
import com.AspirationsNetwork.UserData.DTO.RwdLearningCenterItemDTO;
import com.AspirationsNetwork.UserData.DTO.YouthDashboardDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import com.AspirationsNetwork.UserData.Models.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Test
    void getYouthDashboardAggregatesProfileProgramsAndRecords() throws Exception {
        UserInfoService userInfoService = mock(UserInfoService.class);
        CredentialService credentialService = mock(CredentialService.class);
        AttendanceService attendanceService = mock(AttendanceService.class);
        ServiceHourService serviceHourService = mock(ServiceHourService.class);
        ProgramEnrollmentService programEnrollmentService = mock(ProgramEnrollmentService.class);
        ProgramService programService = mock(ProgramService.class);
        RwdLearningService rwdLearningService = mock(RwdLearningService.class);
        SystemSettingsService systemSettingsService = mock(SystemSettingsService.class);
        NotificationService notificationService = mock(NotificationService.class);

        User user = new User();
        user.setUid("youth-123");
        user.setFirstName("Jordan");
        user.setLastName("Taylor");
        user.setEmail("jordan@example.com");
        user.setProfileImageUrl(null);
        user.setProfileStatus("pending_onboarding");
        user.setSchool("Central High");
        user.setGraduationYear("2027");

        ProgramEnrollment activeEnrollment = new ProgramEnrollment();
        activeEnrollment.setProgramId("program-123");
        activeEnrollment.setEnrollmentStatus("active");

        ProgramEnrollment removedEnrollment = new ProgramEnrollment();
        removedEnrollment.setProgramId("program-archived");
        removedEnrollment.setEnrollmentStatus("removed");

        Program program = new Program();
        program.setProgramId("program-123");
        program.setProgramName("September Onboarding");
        program.setDescription("ASPN youth onboarding");
        program.setCategory("onboarding");
        program.setProgramImageUrl("https://example.com/program.png");
        program.setProgramLeader("ASPN Staff");
        program.setProgramStatus("active");

        EarnedCredentialDisplayDTO credential = new EarnedCredentialDisplayDTO();
        AvailableCredentialDTO availableCredential = new AvailableCredentialDTO();
        availableCredential.setCredentialID("credential-available");
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        ServiceHourRecord serviceHourRecord = new ServiceHourRecord();
        RwdLearningCenterItemDTO rwdItem = new RwdLearningCenterItemDTO();
        rwdItem.setCountryName("Bangladesh");

        when(userInfoService.getUser("youth-123")).thenReturn(user);
        when(credentialService.getEarnedCredentialsForUser("youth-123")).thenReturn(List.of(credential));
        when(attendanceService.getAttendanceRecordsForUser("youth-123")).thenReturn(List.of(attendanceRecord));
        when(serviceHourService.getServiceHourRecordsForUser("youth-123")).thenReturn(List.of(serviceHourRecord));
        when(programEnrollmentService.getEnrollmentsForUser("youth-123"))
                .thenReturn(List.of(activeEnrollment, removedEnrollment));
        when(programService.getActiveProgramById("program-123")).thenReturn(program);
        when(credentialService.getAvailableCredentialsForPrograms("youth-123", List.of("program-123")))
                .thenReturn(List.of(availableCredential));
        when(rwdLearningService.getLearningCenterForUser("youth-123")).thenReturn(List.of(rwdItem));
        when(systemSettingsService.getServiceHourRequestFormUrl()).thenReturn("https://example.com/service-hours");
        when(notificationService.getUnreadNotificationCount("youth-123")).thenReturn(2);

        DashboardService dashboardService = new DashboardService(
                userInfoService,
                credentialService,
                attendanceService,
                serviceHourService,
                programEnrollmentService,
                programService,
                rwdLearningService,
                systemSettingsService,
                notificationService
        );

        YouthDashboardDTO dashboard = dashboardService.getYouthDashboard("youth-123");

        assertEquals("Jordan", dashboard.getProfileSummary().getFirstName());
        assertEquals("J", dashboard.getProfileSummary().getFirstInitial());
        assertTrue(dashboard.getProfileSummary().isUseFirstInitialFallback());
        assertEquals("Central High", dashboard.getProfileSummary().getSchool());
        assertEquals(1, dashboard.getPrograms().size());
        assertEquals("September Onboarding", dashboard.getPrograms().get(0).getProgramName());
        assertEquals(1, dashboard.getEarnedCredentials().size());
        assertEquals(1, dashboard.getAttendanceRecords().size());
        assertEquals(1, dashboard.getServiceHourRecords().size());
        assertEquals(1, dashboard.getAvailableCredentials().size());
        assertEquals("credential-available", dashboard.getAvailableCredentials().get(0).getCredentialID());
        assertEquals(1, dashboard.getRwdLearningCenter().size());
        assertEquals("Bangladesh", dashboard.getRwdLearningCenter().get(0).getCountryName());
        assertEquals("https://example.com/service-hours", dashboard.getServiceHourRequestFormUrl());
        assertEquals(2, dashboard.getUnreadNotificationCount());
        assertEquals("https://aspirationsnetwork.org/", dashboard.getOpportunities().get(0).getUrl());
    }
}
