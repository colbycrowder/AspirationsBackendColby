package com.AspirationsNetwork.UserData.Controller;

import com.AspirationsNetwork.UserData.DTO.AttendanceRecordCreationDTO;
import com.AspirationsNetwork.UserData.DTO.AwardCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionCreationDTO;
import com.AspirationsNetwork.UserData.DTO.EarnedCredentialDisplayDTO;
import com.AspirationsNetwork.UserData.DTO.PlatformMetricsDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramEnrollmentDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramDTO;
import com.AspirationsNetwork.UserData.DTO.RwdActivityDTO;
import com.AspirationsNetwork.UserData.DTO.RwdProgressDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourRecordDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourRequestUrlDTO;
import com.AspirationsNetwork.UserData.DTO.StaffUserUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileCreationDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileWithCredentialsDTO;
import com.AspirationsNetwork.UserData.DTO.YouthDashboardDTO;
import com.AspirationsNetwork.UserData.DTO.YouthSelfServiceProfileDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.Comment;
import com.AspirationsNetwork.UserData.Models.DiscussionPost;
import com.AspirationsNetwork.UserData.Models.Notification;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.AspirationsNetwork.UserData.Models.RwdActivity;
import com.AspirationsNetwork.UserData.Models.RwdProgress;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import com.AspirationsNetwork.UserData.Models.User;
import com.AspirationsNetwork.UserData.Service.AttendanceService;
import com.AspirationsNetwork.UserData.Service.AuthService;
import com.AspirationsNetwork.UserData.Service.CredentialService;
import com.AspirationsNetwork.UserData.Service.DashboardService;
import com.AspirationsNetwork.UserData.Service.DiscussionPostService;
import com.AspirationsNetwork.UserData.Service.ForbiddenAccessException;
import com.AspirationsNetwork.UserData.Service.MetricsService;
import com.AspirationsNetwork.UserData.Service.NotificationService;
import com.AspirationsNetwork.UserData.Service.ProgramEnrollmentService;
import com.AspirationsNetwork.UserData.Service.ProgramService;
import com.AspirationsNetwork.UserData.Service.RwdLearningService;
import com.AspirationsNetwork.UserData.Service.ServiceHourService;
import com.AspirationsNetwork.UserData.Service.SystemSettingsService;
import com.AspirationsNetwork.UserData.Service.UnauthorizedAccessException;
import com.AspirationsNetwork.UserData.Service.UserInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserInfoControllerTest {

    private final UserInfoService userInfoService = mock(UserInfoService.class);
    private final DiscussionPostService discussionPostService = mock(DiscussionPostService.class);
    private final CredentialService credentialService = mock(CredentialService.class);
    private final AuthService authService = mock(AuthService.class);
    private final AttendanceService attendanceService = mock(AttendanceService.class);
    private final ServiceHourService serviceHourService = mock(ServiceHourService.class);
    private final ProgramService programService = mock(ProgramService.class);
    private final ProgramEnrollmentService programEnrollmentService = mock(ProgramEnrollmentService.class);
    private final DashboardService dashboardService = mock(DashboardService.class);
    private final RwdLearningService rwdLearningService = mock(RwdLearningService.class);
    private final SystemSettingsService systemSettingsService = mock(SystemSettingsService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final MetricsService metricsService = mock(MetricsService.class);
    private final UserInfoController controller = new UserInfoController(
            userInfoService,
            discussionPostService,
            credentialService,
            authService,
            attendanceService,
            serviceHourService,
            programService,
            programEnrollmentService,
            dashboardService,
            rwdLearningService,
            systemSettingsService,
            notificationService,
            metricsService
    );

    @Test
    void createPostReturnsGoneBecauseLegacyDiscussionEndpointsAreDisabled() throws Exception {
        DiscussionPost post = new DiscussionPost();

        ResponseEntity<String> response = controller.createPost(post);

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("Legacy discussion endpoints are disabled", response.getBody());
        verifyNoInteractions(discussionPostService);
    }

    @Test
    void getCommentsForPostReturnsGoneBecauseLegacyDiscussionEndpointsAreDisabled() throws Exception {
        ResponseEntity<List<Comment>> response = controller.getCommentsForPost("post-123");

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(discussionPostService);
    }

    @Test
    void getAllPostsReturnsGoneBecauseLegacyDiscussionEndpointsAreDisabled() throws Exception {
        ResponseEntity<List<DiscussionPost>> response = controller.getallPost();

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(discussionPostService);
    }

    @Test
    void createCommentReturnsGoneBecauseLegacyDiscussionEndpointsAreDisabled() throws Exception {
        Comment comment = new Comment();

        ResponseEntity<String> response = controller.createComment(comment);

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("Legacy discussion endpoints are disabled", response.getBody());
        verifyNoInteractions(discussionPostService);
    }

    @Test
    void upVoteReturnsGoneBecauseLegacyDiscussionEndpointsAreDisabled() throws Exception {
        ResponseEntity<Void> response = controller.upVote("post-123");

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(discussionPostService);
    }

    @Test
    void deletePostReturnsGoneBecauseLegacyDiscussionEndpointsAreDisabled() throws Exception {
        ResponseEntity<String> response = controller.deletePost("post-123");

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("Legacy discussion endpoints are disabled", response.getBody());
        verifyNoInteractions(discussionPostService);
    }

    @Test
    void createProfileReturnsGoneBecauseLegacyProfileCreationIsDisabled() {
        UserProfileCreationDTO dto = new UserProfileCreationDTO();

        ResponseEntity<String> response = controller.createProfile(dto);

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("Legacy profile creation is disabled; use /api/me/profile", response.getBody());
        verifyNoInteractions(userInfoService);
    }

    @Test
    void getUserReturnsGoneBecauseLegacyPublicProfileLookupIsDisabled() {
        ResponseEntity<User> response = controller.getUser("user-123");

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(userInfoService);
    }

    @Test
    void getActiveProgramsReturnsProgramsVisibleToYouthUsers() throws Exception {
        Program program = new Program();
        program.setProgramId("program-123");
        program.setProgramStatus("active");
        when(programService.getActivePrograms()).thenReturn(List.of(program));

        ResponseEntity<List<Program>> response = controller.getActivePrograms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("program-123", response.getBody().get(0).getProgramId());
    }

    @Test
    void getActiveProgramByIdReturnsNotFoundWhenProgramIsMissingOrArchived() throws Exception {
        when(programService.getActiveProgramById("program-archived")).thenReturn(null);

        ResponseEntity<Program> response = controller.getActiveProgramById("program-archived");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void enrollMeInProgramUsesVerifiedUserUid() throws Exception {
        ProgramEnrollmentDTO dto = new ProgramEnrollmentDTO();
        dto.setProgramId("program-123");
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("verified-youth-123");
        when(programEnrollmentService.enrollYouthInProgram("verified-youth-123", "program-123"))
                .thenReturn("enrollment-123");

        ResponseEntity<String> response = controller.enrollMeInProgram("Bearer youth-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("enrollment-123", response.getBody());
    }

    @Test
    void enrollMeInProgramReturnsUnauthorizedWithoutFirebaseToken() {
        ProgramEnrollmentDTO dto = new ProgramEnrollmentDTO();
        dto.setProgramId("program-123");
        doThrow(new UnauthorizedAccessException("Authorization bearer token is required"))
                .when(authService)
                .requireAuthenticatedUserUid(null);

        ResponseEntity<String> response = controller.enrollMeInProgram(null, dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authorization bearer token is required", response.getBody());
    }

    @Test
    void getActiveRwdActivitiesReturnsActivitiesVisibleToYouthUsers() throws Exception {
        RwdActivity activity = new RwdActivity();
        activity.setRwdActivityId("rwd-123");
        activity.setCountryName("Bangladesh");
        when(rwdLearningService.getActiveRwdActivities()).thenReturn(List.of(activity));

        ResponseEntity<List<RwdActivity>> response = controller.getActiveRwdActivities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Bangladesh", response.getBody().get(0).getCountryName());
    }

    @Test
    void saveMyRwdProgressUsesVerifiedUserUid() throws Exception {
        RwdProgressDTO dto = new RwdProgressDTO();
        dto.setRwdActivityId("rwd-123");
        dto.setQuizScore(80);
        RwdProgress progress = new RwdProgress();
        progress.setProgressId("progress-123");
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("verified-youth-123");
        when(rwdLearningService.saveProgressForUser("verified-youth-123", dto)).thenReturn(progress);

        ResponseEntity<RwdProgress> response = controller.saveMyRwdProgress("Bearer youth-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("progress-123", response.getBody().getProgressId());
    }

    @Test
    void setServiceHourRequestUrlUsesVerifiedStaffUid() throws Exception {
        ServiceHourRequestUrlDTO dto = new ServiceHourRequestUrlDTO();
        dto.setServiceHourRequestFormUrl("https://example.com/service-hours");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.setServiceHourRequestUrl("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(systemSettingsService).setServiceHourRequestFormUrl(argThat(setting ->
                "https://example.com/service-hours".equals(setting.getSettingValue())
                        && "verified-staff-123".equals(setting.getUpdatedByStaffUID())
        ));
    }

    @Test
    void getUserWithCredentialsReturnsGoneBecauseLegacyPublicCredentialLookupIsDisabled() throws Exception {
        ResponseEntity<UserProfileWithCredentialsDTO> response = controller.getUserWithCredentials("user-123");

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(userInfoService, credentialService);
    }

    @Test
    void getUserWithCredentialsReturnsGoneEvenWhenUserDoesNotExist() throws Exception {
        ResponseEntity<UserProfileWithCredentialsDTO> response = controller.getUserWithCredentials("missing-user");

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(userInfoService, credentialService);
    }

    @Test
    void getMyProfileReturnsSignedInUsersPrivateProfileAndRecords() throws Exception {
        User user = new User();
        user.setUid("token-user-123");
        EarnedCredentialDisplayDTO credential = new EarnedCredentialDisplayDTO();
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        ServiceHourRecord serviceHourRecord = new ServiceHourRecord();
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("token-user-123");
        when(userInfoService.getUser("token-user-123")).thenReturn(user);
        when(credentialService.getEarnedCredentialsForUser("token-user-123")).thenReturn(List.of(credential));
        when(attendanceService.getAttendanceRecordsForUser("token-user-123")).thenReturn(List.of(attendanceRecord));
        when(serviceHourService.getServiceHourRecordsForUser("token-user-123")).thenReturn(List.of(serviceHourRecord));

        ResponseEntity<YouthSelfServiceProfileDTO> response = controller.getMyProfile("Bearer youth-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token-user-123", response.getBody().getUser().getUid());
        assertEquals(1, response.getBody().getEarnedCredentials().size());
        assertEquals(1, response.getBody().getAttendanceRecords().size());
        assertEquals(1, response.getBody().getServiceHourRecords().size());
    }

    @Test
    void getMyProfileReturnsUnauthorizedWithoutFirebaseToken() {
        doThrow(new UnauthorizedAccessException("Authorization bearer token is required"))
                .when(authService)
                .requireAuthenticatedUserUid(null);

        ResponseEntity<YouthSelfServiceProfileDTO> response = controller.getMyProfile(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getMyDashboardReturnsSignedInUsersDashboard() throws Exception {
        YouthDashboardDTO dashboard = new YouthDashboardDTO();
        YouthDashboardDTO.ProfileSummaryDTO profileSummary = new YouthDashboardDTO.ProfileSummaryDTO();
        profileSummary.setFirstName("Youth");
        dashboard.setProfileSummary(profileSummary);
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("token-user-123");
        when(dashboardService.getYouthDashboard("token-user-123")).thenReturn(dashboard);

        ResponseEntity<YouthDashboardDTO> response = controller.getMyDashboard("Bearer youth-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Youth", response.getBody().getProfileSummary().getFirstName());
    }

    @Test
    void getMyDashboardReturnsUnauthorizedWithoutFirebaseToken() {
        doThrow(new UnauthorizedAccessException("Authorization bearer token is required"))
                .when(authService)
                .requireAuthenticatedUserUid(null);

        ResponseEntity<YouthDashboardDTO> response = controller.getMyDashboard(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getMyNotificationsReturnsSignedInUsersNotifications() throws Exception {
        Notification notification = new Notification();
        notification.setNotificationId("notification-123");
        notification.setUserUID("token-user-123");
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("token-user-123");
        when(notificationService.getNotificationsForUser("token-user-123")).thenReturn(List.of(notification));

        ResponseEntity<List<Notification>> response = controller.getMyNotifications("Bearer youth-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("notification-123", response.getBody().get(0).getNotificationId());
    }

    @Test
    void getMyNotificationsReturnsUnauthorizedWithoutFirebaseToken() throws Exception {
        doThrow(new UnauthorizedAccessException("Authorization bearer token is required"))
                .when(authService)
                .requireAuthenticatedUserUid(null);

        ResponseEntity<List<Notification>> response = controller.getMyNotifications(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void markMyNotificationAsReadUsesVerifiedUserUid() throws Exception {
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("token-user-123");

        ResponseEntity<String> response = controller.markMyNotificationAsRead(
                "Bearer youth-token",
                "notification-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(notificationService).markNotificationAsRead("token-user-123", "notification-123");
    }

    @Test
    void markMyNotificationAsReadReturnsForbiddenForAnotherUsersNotification() throws Exception {
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("token-user-123");
        doThrow(new ForbiddenAccessException("Notification does not belong to signed-in user"))
                .when(notificationService)
                .markNotificationAsRead("token-user-123", "notification-123");

        ResponseEntity<String> response = controller.markMyNotificationAsRead(
                "Bearer youth-token",
                "notification-123"
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Notification does not belong to signed-in user", response.getBody());
    }

    @Test
    void getStaffMetricsReturnsMetricsForStaffUser() throws Exception {
        PlatformMetricsDTO metrics = new PlatformMetricsDTO();
        metrics.setTotalYouthUsers(10);
        metrics.setActivePrograms(3);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");
        when(metricsService.getPlatformMetrics()).thenReturn(metrics);

        ResponseEntity<PlatformMetricsDTO> response = controller.getStaffMetrics("Bearer staff-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10, response.getBody().getTotalYouthUsers());
        assertEquals(3, response.getBody().getActivePrograms());
    }

    @Test
    void getStaffMetricsReturnsUnauthorizedWithoutFirebaseToken() throws Exception {
        doThrow(new UnauthorizedAccessException("Authorization bearer token is required"))
                .when(authService)
                .requireStaff(null);

        ResponseEntity<PlatformMetricsDTO> response = controller.getStaffMetrics(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getStaffMetricsReturnsForbiddenForNonStaffUser() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<PlatformMetricsDTO> response = controller.getStaffMetrics("Bearer youth-token");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createCredentialDefinitionReturnsCreatedCredentialId() throws Exception {
        CredentialDefinitionCreationDTO dto = new CredentialDefinitionCreationDTO();
        dto.setCreatedByStaffUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer token")).thenReturn("verified-staff-123");
        when(credentialService.createCredentialDefinition(dto)).thenReturn("credential-definition-123");

        ResponseEntity<String> response = controller.createCredentialDefinition("Bearer token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("credential-definition-123", response.getBody());
        assertEquals("verified-staff-123", dto.getCreatedByStaffUID());
    }

    @Test
    void awardCredentialReturnsEarnedCredentialId() throws Exception {
        AwardCredentialDTO dto = new AwardCredentialDTO();
        dto.setAwardedByStaffUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer token")).thenReturn("verified-staff-123");
        when(credentialService.awardCredentialToYouth(dto)).thenReturn("earned-credential-123");

        ResponseEntity<String> response = controller.awardCredentialToYouth("Bearer token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("earned-credential-123", response.getBody());
        assertEquals("verified-staff-123", dto.getAwardedByStaffUID());
    }

    @Test
    void awardCredentialReturnsBadRequestForUnsafeRequest() throws Exception {
        AwardCredentialDTO dto = new AwardCredentialDTO();
        when(credentialService.awardCredentialToYouth(dto)).thenThrow(new IllegalArgumentException("credentialID is required"));

        ResponseEntity<String> response = controller.awardCredentialToYouth("Bearer token", dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("credentialID is required", response.getBody());
    }

    @Test
    void createCredentialDefinitionReturnsUnauthorizedWithoutFirebaseToken() throws Exception {
        CredentialDefinitionCreationDTO dto = new CredentialDefinitionCreationDTO();
        doThrow(new UnauthorizedAccessException("Authorization bearer token is required"))
                .when(authService)
                .requireStaff(null);

        ResponseEntity<String> response = controller.createCredentialDefinition(null, dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authorization bearer token is required", response.getBody());
    }

    @Test
    void awardCredentialReturnsForbiddenForNonStaffUser() throws Exception {
        AwardCredentialDTO dto = new AwardCredentialDTO();
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<String> response = controller.awardCredentialToYouth("Bearer youth-token", dto);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Staff role is required", response.getBody());
    }

    @Test
    void createProgramReturnsProgramIdAndUsesVerifiedStaffUid() throws Exception {
        ProgramDTO dto = new ProgramDTO();
        dto.setCreatedByStaffUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(programService.createProgram(dto)).thenReturn("program-123");

        ResponseEntity<String> response = controller.createProgram("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("program-123", response.getBody());
        assertEquals("verified-staff-123", dto.getCreatedByStaffUID());
    }

    @Test
    void createProgramReturnsForbiddenForNonStaffUser() throws Exception {
        ProgramDTO dto = new ProgramDTO();
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<String> response = controller.createProgram("Bearer youth-token", dto);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Staff role is required", response.getBody());
    }

    @Test
    void updateProgramReturnsUpdatedForStaffToken() {
        ProgramDTO dto = new ProgramDTO();

        ResponseEntity<String> response = controller.updateProgram("Bearer staff-token", "program-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
    }

    @Test
    void getProgramEnrollmentsForStaffReturnsEnrollmentsForStaffToken() throws Exception {
        ProgramEnrollment enrollment = new ProgramEnrollment();
        enrollment.setEnrollmentId("enrollment-123");
        when(programEnrollmentService.getAllEnrollments()).thenReturn(List.of(enrollment));

        ResponseEntity<List<ProgramEnrollment>> response = controller.getProgramEnrollmentsForStaff("Bearer staff-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("enrollment-123", response.getBody().get(0).getEnrollmentId());
    }

    @Test
    void getProgramEnrollmentsForStaffReturnsForbiddenForNonStaffUser() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<List<ProgramEnrollment>> response = controller.getProgramEnrollmentsForStaff("Bearer youth-token");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void removeProgramEnrollmentUsesVerifiedStaffUid() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.removeProgramEnrollment("Bearer staff-token", "enrollment-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Removed", response.getBody());
        verify(programEnrollmentService).removeEnrollment("enrollment-123", "verified-staff-123");
    }

    @Test
    void getYouthUsersForStaffReturnsYouthUsersForStaffToken() throws Exception {
        User youthUser = new User();
        youthUser.setUid("youth-123");
        youthUser.setYouthProfile(true);
        when(userInfoService.getYouthUsersForStaff()).thenReturn(List.of(youthUser));

        ResponseEntity<List<User>> response = controller.getYouthUsersForStaff("Bearer staff-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("youth-123", response.getBody().get(0).getUid());
    }

    @Test
    void getYouthUserForStaffReturnsNotFoundWhenProfileIsMissingOrNotYouth() throws Exception {
        when(userInfoService.getYouthUserForStaff("missing-user")).thenReturn(null);

        ResponseEntity<User> response = controller.getYouthUserForStaff("Bearer staff-token", "missing-user");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateYouthUserForStaffReturnsUpdatedForStaffToken() {
        StaffUserUpdateDTO dto = new StaffUserUpdateDTO();

        ResponseEntity<String> response = controller.updateYouthUserForStaff("Bearer staff-token", "youth-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
    }

    @Test
    void getYouthUsersForStaffReturnsUnauthorizedWithoutToken() throws Exception {
        doThrow(new UnauthorizedAccessException("Authorization bearer token is required"))
                .when(authService)
                .requireStaff(null);

        ResponseEntity<List<User>> response = controller.getYouthUsersForStaff(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createAttendanceRecordReturnsCreatedRecordIdAndUsesVerifiedStaffUid() throws Exception {
        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setStaffRecorderUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(attendanceService.createAttendanceRecord(dto)).thenReturn("attendance-123");

        ResponseEntity<String> response = controller.createAttendanceRecord("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attendance-123", response.getBody());
        assertEquals("verified-staff-123", dto.getStaffRecorderUID());
    }

    @Test
    void createAttendanceRecordReturnsForbiddenForNonStaffUser() throws Exception {
        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<String> response = controller.createAttendanceRecord("Bearer youth-token", dto);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Staff role is required", response.getBody());
    }

    @Test
    void getAttendanceRecordsForUserReturnsRecordsForStaffToken() throws Exception {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceRecordID("attendance-123");
        when(attendanceService.getAttendanceRecordsForUser("youth-123")).thenReturn(List.of(record));

        ResponseEntity<List<AttendanceRecord>> response = controller.getAttendanceRecordsForUser(
                "Bearer staff-token",
                "youth-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("attendance-123", response.getBody().get(0).getAttendanceRecordID());
    }

    @Test
    void createOrReviewServiceHourRecordReturnsRecordIdAndUsesVerifiedStaffUid() throws Exception {
        ServiceHourRecordDTO dto = new ServiceHourRecordDTO();
        dto.setReviewedByStaffUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(serviceHourService.createOrReviewServiceHourRecord(dto)).thenReturn("service-hours-123");

        ResponseEntity<String> response = controller.createOrReviewServiceHourRecord("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("service-hours-123", response.getBody());
        assertEquals("verified-staff-123", dto.getReviewedByStaffUID());
    }

    @Test
    void createOrReviewServiceHourRecordReturnsForbiddenForNonStaffUser() throws Exception {
        ServiceHourRecordDTO dto = new ServiceHourRecordDTO();
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<String> response = controller.createOrReviewServiceHourRecord("Bearer youth-token", dto);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Staff role is required", response.getBody());
    }

    @Test
    void getServiceHourRecordsForUserReturnsRecordsForStaffToken() throws Exception {
        ServiceHourRecord record = new ServiceHourRecord();
        record.setServiceHourRecordId("service-hours-123");
        when(serviceHourService.getServiceHourRecordsForUser("youth-123")).thenReturn(List.of(record));

        ResponseEntity<List<ServiceHourRecord>> response = controller.getServiceHourRecordsForUser(
                "Bearer staff-token",
                "youth-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("service-hours-123", response.getBody().get(0).getServiceHourRecordId());
    }
}
