package com.AspirationsNetwork.UserData.Controller;

import com.AspirationsNetwork.UserData.DTO.AttendanceRecordCreationDTO;
import com.AspirationsNetwork.UserData.DTO.AttendanceTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.AwardCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionCreationDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.EarnedCredentialDisplayDTO;
import com.AspirationsNetwork.UserData.DTO.EducatorDTO;
import com.AspirationsNetwork.UserData.DTO.EducatorTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.ExternalDatasetDTO;
import com.AspirationsNetwork.UserData.DTO.ParticipantExternalLinkDTO;
import com.AspirationsNetwork.UserData.DTO.PlatformMetricsDTO;
import com.AspirationsNetwork.UserData.DTO.PlatformEventRequestDTO;
import com.AspirationsNetwork.UserData.DTO.PilotReportingDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramDetailDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramEnrollmentDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.ResearchExportDTO;
import com.AspirationsNetwork.UserData.DTO.RwdActivityDTO;
import com.AspirationsNetwork.UserData.DTO.RwdProgressDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourRecordDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourRequestUrlDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourStatusUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.StaffOperationReportingDTO;
import com.AspirationsNetwork.UserData.DTO.StaffUserUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileCreationDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileWithCredentialsDTO;
import com.AspirationsNetwork.UserData.DTO.UserTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.YouthDashboardDTO;
import com.AspirationsNetwork.UserData.DTO.YouthSelfServiceProfileDTO;
import com.AspirationsNetwork.UserData.Models.PlatformEventType;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.Comment;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.DiscussionPost;
import com.AspirationsNetwork.UserData.Models.Educator;
import com.AspirationsNetwork.UserData.Models.ExternalDataset;
import com.AspirationsNetwork.UserData.Models.Notification;
import com.AspirationsNetwork.UserData.Models.ParticipantExternalLink;
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
import com.AspirationsNetwork.UserData.Service.EducatorService;
import com.AspirationsNetwork.UserData.Service.ExternalDatasetLinkService;
import com.AspirationsNetwork.UserData.Service.ForbiddenAccessException;
import com.AspirationsNetwork.UserData.Service.MetricsService;
import com.AspirationsNetwork.UserData.Service.NotificationService;
import com.AspirationsNetwork.UserData.Service.PilotReportingService;
import com.AspirationsNetwork.UserData.Service.PlatformEventService;
import com.AspirationsNetwork.UserData.Service.ProgramEnrollmentService;
import com.AspirationsNetwork.UserData.Service.ProgramService;
import com.AspirationsNetwork.UserData.Service.ResearchExportService;
import com.AspirationsNetwork.UserData.Service.RwdLearningService;
import com.AspirationsNetwork.UserData.Service.ServiceHourService;
import com.AspirationsNetwork.UserData.Service.StaffOperationEventService;
import com.AspirationsNetwork.UserData.Service.SystemSettingsService;
import com.AspirationsNetwork.UserData.Service.UnauthorizedAccessException;
import com.AspirationsNetwork.UserData.Service.UserInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
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
    private final PlatformEventService platformEventService = mock(PlatformEventService.class);
    private final PilotReportingService pilotReportingService = mock(PilotReportingService.class);
    private final ExternalDatasetLinkService externalDatasetLinkService = mock(ExternalDatasetLinkService.class);
    private final ResearchExportService researchExportService = mock(ResearchExportService.class);
    private final StaffOperationEventService staffOperationEventService = mock(StaffOperationEventService.class);
    private final EducatorService educatorService = mock(EducatorService.class);
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
            metricsService,
            platformEventService,
            pilotReportingService,
            externalDatasetLinkService,
            researchExportService,
            staffOperationEventService,
            educatorService
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
    void getStaffPilotReportingMetricsReturnsReportForStaffToken() throws Exception {
        PilotReportingDTO reporting = new PilotReportingDTO();
        reporting.getParticipation().setTotalRegisteredYouth(75);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");
        when(pilotReportingService.getPilotReportingMetrics()).thenReturn(reporting);

        ResponseEntity<PilotReportingDTO> response = controller.getStaffPilotReportingMetrics("Bearer staff-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(75, response.getBody().getParticipation().getTotalRegisteredYouth());
    }

    @Test
    void getStaffPilotReportingMetricsRejectsYouthToken() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<PilotReportingDTO> response = controller.getStaffPilotReportingMetrics("Bearer youth-token");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(pilotReportingService);
    }

    @Test
    void getStaffOperationReportingReturnsReportForStaffToken() throws Exception {
        StaffOperationReportingDTO reporting = new StaffOperationReportingDTO();
        reporting.setTotalOperations(8);
        reporting.setOperationsLast30Days(4);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");
        when(staffOperationEventService.buildOperationReport()).thenReturn(reporting);

        ResponseEntity<StaffOperationReportingDTO> response = controller.getStaffOperationReporting(
                "Bearer staff-token"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(8, response.getBody().getTotalOperations());
        assertEquals(4, response.getBody().getOperationsLast30Days());
    }

    @Test
    void getStaffOperationReportingRejectsYouthToken() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<StaffOperationReportingDTO> response = controller.getStaffOperationReporting(
                "Bearer youth-token"
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(staffOperationEventService);
    }

    @Test
    void createExternalDatasetUsesVerifiedStaffUid() throws Exception {
        ExternalDatasetDTO dto = new ExternalDatasetDTO();
        dto.setExternalDatasetId("fall-survey-2026");
        dto.setDatasetName("Fall Survey 2026");
        dto.setExternalSource("google_forms");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");
        when(externalDatasetLinkService.createExternalDataset(dto)).thenReturn("fall-survey-2026");

        ResponseEntity<String> response = controller.createExternalDataset("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("fall-survey-2026", response.getBody());
        verify(externalDatasetLinkService).createExternalDataset(argThat(request ->
                "staff-123".equals(request.getCreatedByStaffUID())
        ));
        verify(staffOperationEventService).trackOperationSafely(
                eq("staff-123"),
                eq(StaffOperationEventService.EXTERNAL_DATASET_CREATED),
                eq("externalDataset"),
                eq("fall-survey-2026"),
                isNull(),
                argThat(metadata ->
                        "Fall Survey 2026".equals(metadata.get("datasetName"))
                                && "google_forms".equals(metadata.get("externalSource"))
                                && metadata.containsKey("collectionPurpose")
                )
        );
    }

    @Test
    void createExternalDatasetRejectsYouthToken() throws Exception {
        ExternalDatasetDTO dto = new ExternalDatasetDTO();
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<String> response = controller.createExternalDataset("Bearer youth-token", dto);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Staff role is required", response.getBody());
        verifyNoInteractions(externalDatasetLinkService);
    }

    @Test
    void getExternalDatasetsReturnsStaffDatasetList() throws Exception {
        ExternalDataset dataset = new ExternalDataset();
        dataset.setExternalDatasetId("fall-survey-2026");
        dataset.setDatasetName("Fall Survey 2026");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");
        when(externalDatasetLinkService.getExternalDatasets()).thenReturn(List.of(dataset));

        ResponseEntity<List<ExternalDataset>> response = controller.getExternalDatasets("Bearer staff-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("fall-survey-2026", response.getBody().get(0).getExternalDatasetId());
    }

    @Test
    void createParticipantExternalLinkUsesVerifiedStaffUid() throws Exception {
        ParticipantExternalLinkDTO dto = new ParticipantExternalLinkDTO();
        dto.setAspnParticipantId("ASPN-2026-0001");
        dto.setExternalDatasetId("fall-survey-2026");
        dto.setExternalRecordId("response-123");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");
        when(externalDatasetLinkService.createParticipantExternalLink(dto)).thenReturn("link-123");

        ResponseEntity<String> response = controller.createParticipantExternalLink("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("link-123", response.getBody());
        verify(externalDatasetLinkService).createParticipantExternalLink(argThat(request ->
                "staff-123".equals(request.getLinkedByStaffUID())
        ));
        verify(staffOperationEventService).trackOperationSafely(
                eq("staff-123"),
                eq(StaffOperationEventService.PARTICIPANT_EXTERNAL_LINK_CREATED),
                eq("participantExternalLink"),
                eq("link-123"),
                isNull(),
                argThat(metadata ->
                        "ASPN-2026-0001".equals(metadata.get("aspnParticipantId"))
                                && "fall-survey-2026".equals(metadata.get("externalDatasetId"))
                                && "response-123".equals(metadata.get("externalRecordId"))
                )
        );
    }

    @Test
    void getParticipantExternalLinksByParticipantRequiresStaffRole() throws Exception {
        ParticipantExternalLink link = new ParticipantExternalLink();
        link.setLinkId("link-123");
        link.setAspnParticipantId("ASPN-2026-0001");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");
        when(externalDatasetLinkService.getLinksByParticipant("ASPN-2026-0001"))
                .thenReturn(List.of(link));

        ResponseEntity<List<ParticipantExternalLink>> response =
                controller.getParticipantExternalLinksByParticipant("Bearer staff-token", "ASPN-2026-0001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("link-123", response.getBody().get(0).getLinkId());
    }

    @Test
    void removeParticipantExternalLinkSoftRemovesForStaff() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");

        ResponseEntity<String> response =
                controller.removeParticipantExternalLink("Bearer staff-token", "link-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Removed", response.getBody());
        verify(externalDatasetLinkService).removeParticipantExternalLink("link-123", "staff-123");
    }

    @Test
    void getStaffResearchExportReturnsExportForStaffToken() throws Exception {
        ResearchExportDTO export = new ResearchExportDTO();
        export.setExportType("participants_export");
        export.setRecords(List.of(Map.of("aspnParticipantId", "ASPN-2026-0001")));
        export.setRecordCount(1);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("staff-123");
        when(researchExportService.getResearchExport("participants_export")).thenReturn(export);

        ResponseEntity<ResearchExportDTO> response =
                controller.getStaffResearchExport("Bearer staff-token", "participants_export");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("participants_export", response.getBody().getExportType());
        assertEquals(1, response.getBody().getRecordCount());
    }

    @Test
    void getStaffResearchExportRejectsYouthToken() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<ResearchExportDTO> response =
                controller.getStaffResearchExport("Bearer youth-token", "participants_export");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(researchExportService);
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
    void trackMyPlatformEventUsesVerifiedUserUidForClientTrackableEvent() throws Exception {
        PlatformEventRequestDTO dto = new PlatformEventRequestDTO();
        dto.setEventType("DASHBOARD_VIEW");
        dto.setMetadata(Map.of("source", "dashboard"));
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("verified-youth-123");
        when(platformEventService.trackEventForUser(
                "verified-youth-123",
                PlatformEventType.DASHBOARD_VIEW,
                dto.getMetadata()
        )).thenReturn("event-123");

        ResponseEntity<String> response = controller.trackMyPlatformEvent("Bearer youth-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tracked", response.getBody());
        verify(platformEventService).trackEventForUser(
                "verified-youth-123",
                PlatformEventType.DASHBOARD_VIEW,
                dto.getMetadata()
        );
    }

    @Test
    void trackMyPlatformEventRejectsServerOwnedEventTypesFromClient() {
        PlatformEventRequestDTO dto = new PlatformEventRequestDTO();
        dto.setEventType("CREDENTIAL_EARNED");
        when(authService.requireAuthenticatedUserUid("Bearer youth-token")).thenReturn("verified-youth-123");

        ResponseEntity<String> response = controller.trackMyPlatformEvent("Bearer youth-token", dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("eventType is not client-trackable", response.getBody());
        verifyNoInteractions(platformEventService);
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
        dto.setCredentialID("credential-123");
        dto.setUserUID("youth-123");
        dto.setAwardedByStaffUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer token")).thenReturn("verified-staff-123");
        when(credentialService.awardCredentialToYouth(dto)).thenReturn("earned-credential-123");

        ResponseEntity<String> response = controller.awardCredentialToYouth("Bearer token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("earned-credential-123", response.getBody());
        assertEquals("verified-staff-123", dto.getAwardedByStaffUID());
        verify(staffOperationEventService).trackOperationSafely(
                eq("verified-staff-123"),
                eq(StaffOperationEventService.CREDENTIAL_AWARDED),
                eq("earnedCredential"),
                eq("earned-credential-123"),
                eq("youth-123"),
                argThat(metadata -> "credential-123".equals(metadata.get("credentialID")))
        );
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
    void getCredentialDefinitionsForStaffReturnsFilteredDefinitions() throws Exception {
        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID("credential-123");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(credentialService.getCredentialDefinitions("RWD", true, "program-123"))
                .thenReturn(List.of(definition));

        ResponseEntity<List<CredentialDefinition>> response = controller.getCredentialDefinitionsForStaff(
                "Bearer staff-token",
                "RWD",
                true,
                "program-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("credential-123", response.getBody().get(0).getCredentialID());
    }

    @Test
    void getCredentialDefinitionForStaffReturnsDetails() throws Exception {
        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID("credential-123");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(credentialService.getCredentialDefinition("credential-123")).thenReturn(definition);

        ResponseEntity<CredentialDefinition> response = controller.getCredentialDefinitionForStaff(
                "Bearer staff-token",
                "credential-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("credential-123", response.getBody().getCredentialID());
    }

    @Test
    void updateCredentialDefinitionRequiresStaffAndDelegatesToService() throws Exception {
        CredentialDefinitionUpdateDTO dto = new CredentialDefinitionUpdateDTO();
        dto.setCredentialName("Updated credential");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.updateCredentialDefinition(
                "Bearer staff-token",
                "credential-123",
                dto
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(credentialService).updateCredentialDefinition("credential-123", dto);
    }

    @Test
    void archiveAndRestoreCredentialDefinitionRequireStaff() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> archiveResponse = controller.archiveCredentialDefinition(
                "Bearer staff-token",
                "credential-123"
        );
        ResponseEntity<String> restoreResponse = controller.restoreCredentialDefinition(
                "Bearer staff-token",
                "credential-123"
        );

        assertEquals(HttpStatus.OK, archiveResponse.getStatusCode());
        assertEquals("Archived", archiveResponse.getBody());
        assertEquals(HttpStatus.OK, restoreResponse.getStatusCode());
        assertEquals("Restored", restoreResponse.getBody());
        verify(credentialService).archiveCredentialDefinition("credential-123");
        verify(credentialService).restoreCredentialDefinition("credential-123");
    }

    @Test
    void getCredentialTotalsForStaffReturnsTotals() throws Exception {
        CredentialTotalsDTO totals = new CredentialTotalsDTO();
        totals.setTotalDefinitions(3);
        totals.setActiveDefinitions(2);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(credentialService.getCredentialTotals("RWD", "program-123")).thenReturn(totals);

        ResponseEntity<CredentialTotalsDTO> response = controller.getCredentialTotalsForStaff(
                "Bearer staff-token",
                "RWD",
                "program-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().getTotalDefinitions());
        assertEquals(2, response.getBody().getActiveDefinitions());
    }

    @Test
    void getCredentialDefinitionsForStaffRejectsYouthToken() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<List<CredentialDefinition>> response = controller.getCredentialDefinitionsForStaff(
                "Bearer youth-token",
                null,
                null,
                null
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createProgramReturnsProgramIdAndUsesVerifiedStaffUid() throws Exception {
        ProgramDTO dto = new ProgramDTO();
        dto.setProgramName("Youth2Lead");
        dto.setProgramStatus("active");
        dto.setCreatedByStaffUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(programService.createProgram(dto)).thenReturn("program-123");

        ResponseEntity<String> response = controller.createProgram("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("program-123", response.getBody());
        assertEquals("verified-staff-123", dto.getCreatedByStaffUID());
        verify(staffOperationEventService).trackOperationSafely(
                eq("verified-staff-123"),
                eq(StaffOperationEventService.PROGRAM_CREATED),
                eq("program"),
                eq("program-123"),
                isNull(),
                argThat(metadata ->
                        "Youth2Lead".equals(metadata.get("programName"))
                                && "active".equals(metadata.get("programStatus"))
                )
        );
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
    void updateProgramReturnsUpdatedForStaffToken() throws Exception {
        ProgramDTO dto = new ProgramDTO();
        dto.setProgramStatus("active");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.updateProgram("Bearer staff-token", "program-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(staffOperationEventService).trackOperationSafely(
                eq("verified-staff-123"),
                eq(StaffOperationEventService.PROGRAM_UPDATED),
                eq("program"),
                eq("program-123"),
                isNull(),
                argThat(metadata -> "active".equals(metadata.get("programStatus")))
        );
    }

    @Test
    void updateProgramTracksArchiveWhenProgramStatusIsArchived() throws Exception {
        ProgramDTO dto = new ProgramDTO();
        dto.setProgramStatus("archived");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.updateProgram("Bearer staff-token", "program-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(staffOperationEventService).trackOperationSafely(
                eq("verified-staff-123"),
                eq(StaffOperationEventService.PROGRAM_ARCHIVED),
                eq("program"),
                eq("program-123"),
                isNull(),
                argThat(metadata -> "archived".equals(metadata.get("programStatus")))
        );
    }

    @Test
    void getProgramsForStaffReturnsFilteredPrograms() throws Exception {
        Program program = new Program();
        program.setProgramId("program-123");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(programService.getPrograms(true, "leadership")).thenReturn(List.of(program));

        ResponseEntity<List<Program>> response = controller.getProgramsForStaff(
                "Bearer staff-token",
                true,
                "leadership"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("program-123", response.getBody().get(0).getProgramId());
    }

    @Test
    void getProgramDetailForStaffReturnsDetail() throws Exception {
        ProgramDetailDTO detail = new ProgramDetailDTO();
        Program program = new Program();
        program.setProgramId("program-123");
        detail.setProgram(program);
        detail.setEnrollmentCount(2);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(programService.getProgramDetail("program-123")).thenReturn(detail);

        ResponseEntity<ProgramDetailDTO> response = controller.getProgramDetailForStaff(
                "Bearer staff-token",
                "program-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("program-123", response.getBody().getProgram().getProgramId());
        assertEquals(2, response.getBody().getEnrollmentCount());
    }

    @Test
    void getProgramDetailForStaffReturnsNotFoundWhenProgramMissing() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(programService.getProgramDetail("missing-program")).thenReturn(null);

        ResponseEntity<ProgramDetailDTO> response = controller.getProgramDetailForStaff(
                "Bearer staff-token",
                "missing-program"
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getProgramTotalsForStaffReturnsTotals() throws Exception {
        ProgramTotalsDTO totals = new ProgramTotalsDTO();
        totals.setTotalPrograms(3);
        totals.setActivePrograms(2);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(programService.getProgramTotals()).thenReturn(totals);

        ResponseEntity<ProgramTotalsDTO> response = controller.getProgramTotalsForStaff(
                "Bearer staff-token"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().getTotalPrograms());
        assertEquals(2, response.getBody().getActivePrograms());
    }

    @Test
    void archiveAndRestoreProgramRequireStaffToken() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> archiveResponse = controller.archiveProgram(
                "Bearer staff-token",
                "program-123"
        );
        ResponseEntity<String> restoreResponse = controller.restoreProgram(
                "Bearer staff-token",
                "program-123"
        );

        assertEquals(HttpStatus.OK, archiveResponse.getStatusCode());
        assertEquals("Archived", archiveResponse.getBody());
        assertEquals(HttpStatus.OK, restoreResponse.getStatusCode());
        assertEquals("Restored", restoreResponse.getBody());
        verify(programService).archiveProgram("program-123");
        verify(programService).restoreProgram("program-123");
    }

    @Test
    void getProgramsForStaffRejectsYouthToken() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<List<Program>> response = controller.getProgramsForStaff(
                "Bearer youth-token",
                null,
                null
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
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
    void getUsersForStaffReturnsFilteredUsersForStaffToken() throws Exception {
        User user = new User();
        user.setUid("user-123");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(userInfoService.getUsersForStaff("member", true, true, "program-123"))
                .thenReturn(List.of(user));

        ResponseEntity<List<User>> response = controller.getUsersForStaff(
                "Bearer staff-token",
                "member",
                true,
                true,
                "program-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user-123", response.getBody().get(0).getUid());
    }

    @Test
    void getUserForStaffReturnsUserDetail() throws Exception {
        User user = new User();
        user.setUid("user-123");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(userInfoService.getUserForStaff("user-123")).thenReturn(user);

        ResponseEntity<User> response = controller.getUserForStaff("Bearer staff-token", "user-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("user-123", response.getBody().getUid());
    }

    @Test
    void getUserTotalsForStaffReturnsTotals() throws Exception {
        UserTotalsDTO totals = new UserTotalsDTO();
        totals.setTotalUsers(10);
        totals.setActiveUsers(7);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(userInfoService.getUserTotalsForStaff()).thenReturn(totals);

        ResponseEntity<UserTotalsDTO> response = controller.getUserTotalsForStaff("Bearer staff-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10, response.getBody().getTotalUsers());
        assertEquals(7, response.getBody().getActiveUsers());
    }

    @Test
    void updateUserForStaffReturnsUpdatedForStaffToken() throws Exception {
        StaffUserUpdateDTO dto = new StaffUserUpdateDTO();
        dto.setProfileStatus("active");
        dto.setStaffVerified(true);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.updateUserForStaff("Bearer staff-token", "user-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(userInfoService).updateUserForStaff("user-123", dto);
    }

    @Test
    void activateAndDeactivateUserForStaffUseProfileStatusWorkflow() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> activateResponse = controller.activateUserForStaff("Bearer staff-token", "user-123");
        ResponseEntity<String> deactivateResponse = controller.deactivateUserForStaff("Bearer staff-token", "user-123");

        assertEquals(HttpStatus.OK, activateResponse.getStatusCode());
        assertEquals("Activated", activateResponse.getBody());
        assertEquals(HttpStatus.OK, deactivateResponse.getStatusCode());
        assertEquals("Deactivated", deactivateResponse.getBody());
        verify(userInfoService).activateUserForStaff("user-123");
        verify(userInfoService).deactivateUserForStaff("user-123");
    }

    @Test
    void getUsersForStaffReturnsForbiddenForNonStaffUser() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<List<User>> response = controller.getUsersForStaff(
                "Bearer youth-token",
                null,
                null,
                null,
                null
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getYouthUserForStaffReturnsNotFoundWhenProfileIsMissingOrNotYouth() throws Exception {
        when(userInfoService.getYouthUserForStaff("missing-user")).thenReturn(null);

        ResponseEntity<User> response = controller.getYouthUserForStaff("Bearer staff-token", "missing-user");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateYouthUserForStaffReturnsUpdatedForStaffToken() throws Exception {
        StaffUserUpdateDTO dto = new StaffUserUpdateDTO();
        dto.setProfileStatus("active");
        dto.setStaffVerified(true);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.updateYouthUserForStaff("Bearer staff-token", "youth-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(staffOperationEventService).trackOperationSafely(
                eq("verified-staff-123"),
                eq(StaffOperationEventService.YOUTH_PROFILE_REVIEWED),
                eq("youthProfile"),
                eq("youth-123"),
                eq("youth-123"),
                argThat(metadata ->
                        "active".equals(metadata.get("profileStatus"))
                                && Boolean.TRUE.equals(metadata.get("staffVerified"))
                )
        );
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
        dto.setUserUID("youth-123");
        dto.setProgramID("program-123");
        dto.setEventName("Weekly meeting");
        dto.setAttendanceStatus("present");
        dto.setStaffRecorderUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(attendanceService.createAttendanceRecord(dto)).thenReturn("attendance-123");

        ResponseEntity<String> response = controller.createAttendanceRecord("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attendance-123", response.getBody());
        assertEquals("verified-staff-123", dto.getStaffRecorderUID());
        verify(staffOperationEventService).trackOperationSafely(
                eq("verified-staff-123"),
                eq(StaffOperationEventService.ATTENDANCE_RECORDED),
                eq("attendanceRecord"),
                eq("attendance-123"),
                eq("youth-123"),
                argThat(metadata ->
                        "program-123".equals(metadata.get("programID"))
                                && "present".equals(metadata.get("attendanceStatus"))
                )
        );
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
    void getAttendanceRecordsForStaffReturnsFilteredRecords() throws Exception {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceRecordID("attendance-123");
        Date eventDate = new Date();
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(attendanceService.getAttendanceRecords("youth-123", "program-123", eventDate))
                .thenReturn(List.of(record));

        ResponseEntity<List<AttendanceRecord>> response = controller.getAttendanceRecordsForStaff(
                "Bearer staff-token",
                "youth-123",
                "program-123",
                eventDate
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attendance-123", response.getBody().get(0).getAttendanceRecordID());
    }

    @Test
    void getAttendanceTotalsForStaffReturnsTotals() throws Exception {
        AttendanceTotalsDTO totals = new AttendanceTotalsDTO();
        totals.setTotalRecords(3);
        totals.setPresent(2);
        Date eventDate = new Date();
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(attendanceService.getAttendanceTotals("youth-123", "program-123", eventDate))
                .thenReturn(totals);

        ResponseEntity<AttendanceTotalsDTO> response = controller.getAttendanceTotalsForStaff(
                "Bearer staff-token",
                "youth-123",
                "program-123",
                eventDate
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().getTotalRecords());
        assertEquals(2, response.getBody().getPresent());
    }

    @Test
    void updateAttendanceRecordUsesVerifiedStaffUid() throws Exception {
        AttendanceRecordCreationDTO dto = new AttendanceRecordCreationDTO();
        dto.setStaffRecorderUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.updateAttendanceRecord(
                "Bearer staff-token",
                "attendance-123",
                dto
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        assertEquals("verified-staff-123", dto.getStaffRecorderUID());
        verify(attendanceService).updateAttendanceRecord("attendance-123", dto);
    }

    @Test
    void deleteAttendanceRecordRequiresStaffToken() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.deleteAttendanceRecord(
                "Bearer staff-token",
                "attendance-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted", response.getBody());
        verify(attendanceService).deleteAttendanceRecord("attendance-123");
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
        dto.setUserUID("youth-123");
        dto.setProgramId("program-123");
        dto.setHours(2.5);
        dto.setVerificationStatus("verified");
        dto.setReviewedByStaffUID("client-spoofed-staff");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(serviceHourService.createOrReviewServiceHourRecord(dto)).thenReturn("service-hours-123");

        ResponseEntity<String> response = controller.createOrReviewServiceHourRecord("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("service-hours-123", response.getBody());
        assertEquals("verified-staff-123", dto.getReviewedByStaffUID());
        verify(staffOperationEventService).trackOperationSafely(
                eq("verified-staff-123"),
                eq(StaffOperationEventService.SERVICE_HOUR_REVIEWED),
                eq("serviceHourRecord"),
                eq("service-hours-123"),
                eq("youth-123"),
                argThat(metadata ->
                        "program-123".equals(metadata.get("programId"))
                                && "verified".equals(metadata.get("verificationStatus"))
                                && Double.valueOf(2.5).equals(metadata.get("hours"))
                )
        );
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

    @Test
    void getServiceHourRecordsForStaffReturnsFilteredRecords() throws Exception {
        ServiceHourRecord record = new ServiceHourRecord();
        record.setServiceHourRecordId("service-hours-123");
        Date serviceDate = new Date();
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(serviceHourService.getServiceHourRecords("youth-123", "verified", "program-123", serviceDate))
                .thenReturn(List.of(record));

        ResponseEntity<List<ServiceHourRecord>> response = controller.getServiceHourRecordsForStaff(
                "Bearer staff-token",
                "youth-123",
                "verified",
                "program-123",
                serviceDate
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("service-hours-123", response.getBody().get(0).getServiceHourRecordId());
    }

    @Test
    void getServiceHourTotalsForStaffReturnsTotals() throws Exception {
        ServiceHourTotalsDTO totals = new ServiceHourTotalsDTO();
        totals.setTotalRecords(2);
        totals.setVerifiedHours(4.5);
        Date serviceDate = new Date();
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(serviceHourService.getServiceHourTotals("youth-123", "verified", "program-123", serviceDate))
                .thenReturn(totals);

        ResponseEntity<ServiceHourTotalsDTO> response = controller.getServiceHourTotalsForStaff(
                "Bearer staff-token",
                "youth-123",
                "verified",
                "program-123",
                serviceDate
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalRecords());
        assertEquals(4.5, response.getBody().getVerifiedHours());
    }

    @Test
    void updateServiceHourRecordStatusUsesVerifiedStaffUid() throws Exception {
        ServiceHourStatusUpdateDTO dto = new ServiceHourStatusUpdateDTO();
        dto.setVerificationStatus("verified");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.updateServiceHourRecordStatus(
                "Bearer staff-token",
                "service-hours-123",
                dto
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(serviceHourService).updateServiceHourRecordStatus(
                "service-hours-123",
                "verified",
                "verified-staff-123"
        );
        verify(staffOperationEventService).trackOperationSafely(
                eq("verified-staff-123"),
                eq(StaffOperationEventService.SERVICE_HOUR_REVIEWED),
                eq("serviceHourRecord"),
                eq("service-hours-123"),
                isNull(),
                argThat(metadata -> "verified".equals(metadata.get("verificationStatus")))
        );
    }

    @Test
    void approveAndRejectServiceHourRecordRequireStaffToken() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> approveResponse = controller.approveServiceHourRecord(
                "Bearer staff-token",
                "service-hours-123"
        );
        ResponseEntity<String> rejectResponse = controller.rejectServiceHourRecord(
                "Bearer staff-token",
                "service-hours-123"
        );

        assertEquals(HttpStatus.OK, approveResponse.getStatusCode());
        assertEquals("Approved", approveResponse.getBody());
        assertEquals(HttpStatus.OK, rejectResponse.getStatusCode());
        assertEquals("Rejected", rejectResponse.getBody());
        verify(serviceHourService).approveServiceHourRecord("service-hours-123", "verified-staff-123");
        verify(serviceHourService).rejectServiceHourRecord("service-hours-123", "verified-staff-123");
    }

    @Test
    void deleteServiceHourRecordRequiresStaffToken() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.deleteServiceHourRecord(
                "Bearer staff-token",
                "service-hours-123"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted", response.getBody());
        verify(serviceHourService).deleteServiceHourRecord("service-hours-123");
    }

    @Test
    void getServiceHourRecordsForStaffRejectsYouthToken() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<List<ServiceHourRecord>> response = controller.getServiceHourRecordsForStaff(
                "Bearer youth-token",
                null,
                null,
                null,
                null
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createEducatorReturnsEducatorIdForStaffToken() throws Exception {
        EducatorDTO dto = educatorDto();
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(educatorService.createEducator(dto)).thenReturn("educator-123");

        ResponseEntity<String> response = controller.createEducatorForStaff("Bearer staff-token", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("educator-123", response.getBody());
        verify(educatorService).createEducator(dto);
    }

    @Test
    void getEducatorsForStaffPassesFiltersToService() throws Exception {
        Educator educator = educator("educator-123");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(educatorService.getEducators("high_school", true, "Hazelwood", "Jane"))
                .thenReturn(List.of(educator));

        ResponseEntity<List<Educator>> response = controller.getEducatorsForStaff(
                "Bearer staff-token",
                "high_school",
                true,
                "Hazelwood",
                "Jane"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("educator-123", response.getBody().get(0).getEducatorId());
    }

    @Test
    void getEducatorForStaffReturnsDetail() throws Exception {
        Educator educator = educator("educator-123");
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(educatorService.getEducator("educator-123")).thenReturn(educator);

        ResponseEntity<Educator> response = controller.getEducatorForStaff("Bearer staff-token", "educator-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("educator-123", response.getBody().getEducatorId());
    }

    @Test
    void getMissingEducatorForStaffReturnsNotFound() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(educatorService.getEducator("missing-educator")).thenReturn(null);

        ResponseEntity<Educator> response = controller.getEducatorForStaff("Bearer staff-token", "missing-educator");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateEducatorForStaffReturnsUpdated() throws Exception {
        EducatorDTO dto = educatorDto();
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> response = controller.updateEducatorForStaff("Bearer staff-token", "educator-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(educatorService).updateEducator("educator-123", dto);
    }

    @Test
    void activateAndDeactivateEducatorForStaffRequireStaffToken() throws Exception {
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");

        ResponseEntity<String> activateResponse = controller.activateEducatorForStaff("Bearer staff-token", "educator-123");
        ResponseEntity<String> deactivateResponse = controller.deactivateEducatorForStaff("Bearer staff-token", "educator-123");

        assertEquals(HttpStatus.OK, activateResponse.getStatusCode());
        assertEquals("Activated", activateResponse.getBody());
        assertEquals(HttpStatus.OK, deactivateResponse.getStatusCode());
        assertEquals("Deactivated", deactivateResponse.getBody());
        verify(educatorService).activateEducator("educator-123");
        verify(educatorService).deactivateEducator("educator-123");
    }

    @Test
    void getEducatorTotalsForStaffReturnsTotals() throws Exception {
        EducatorTotalsDTO totals = new EducatorTotalsDTO();
        totals.setTotalEducators(2);
        totals.setActiveEducators(1);
        totals.setInactiveEducators(1);
        totals.setOrganizationsRepresented(2);
        when(authService.requireStaff("Bearer staff-token")).thenReturn("verified-staff-123");
        when(educatorService.getEducatorTotals()).thenReturn(totals);

        ResponseEntity<EducatorTotalsDTO> response = controller.getEducatorTotalsForStaff("Bearer staff-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTotalEducators());
        assertEquals(1, response.getBody().getActiveEducators());
        assertEquals(1, response.getBody().getInactiveEducators());
        assertEquals(2, response.getBody().getOrganizationsRepresented());
    }

    @Test
    void getEducatorsForStaffRejectsYouthToken() throws Exception {
        doThrow(new ForbiddenAccessException("Staff role is required"))
                .when(authService)
                .requireStaff("Bearer youth-token");

        ResponseEntity<List<Educator>> response = controller.getEducatorsForStaff(
                "Bearer youth-token",
                null,
                null,
                null,
                null
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
    }

    private EducatorDTO educatorDto() {
        EducatorDTO dto = new EducatorDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane@school.org");
        dto.setPhone("314-555-1234");
        dto.setTitle("Counselor");
        dto.setOrganizationName("Hazelwood East High School");
        dto.setOrganizationType("high_school");
        dto.setActive(true);
        dto.setNotes("Primary counselor contact");
        return dto;
    }

    private Educator educator(String educatorId) {
        Educator educator = new Educator();
        educator.setEducatorId(educatorId);
        educator.setFirstName("Jane");
        educator.setLastName("Smith");
        educator.setEmail("jane@school.org");
        educator.setOrganizationName("Hazelwood East High School");
        educator.setOrganizationType("high_school");
        educator.setActive(true);
        return educator;
    }
}
