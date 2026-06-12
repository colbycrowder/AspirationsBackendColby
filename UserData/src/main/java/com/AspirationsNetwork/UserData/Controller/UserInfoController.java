package com.AspirationsNetwork.UserData.Controller;

import com.AspirationsNetwork.UserData.DTO.AttendanceRecordCreationDTO;
import com.AspirationsNetwork.UserData.DTO.AttendanceTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.AwardCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionCreationDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.ExternalDatasetDTO;
import com.AspirationsNetwork.UserData.DTO.ParticipantExternalLinkDTO;
import com.AspirationsNetwork.UserData.DTO.PlatformEventRequestDTO;
import com.AspirationsNetwork.UserData.DTO.PlatformMetricsDTO;
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
import com.AspirationsNetwork.UserData.DTO.SystemSettingDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileCreationDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileWithCredentialsDTO;
import com.AspirationsNetwork.UserData.DTO.YouthDashboardDTO;
import com.AspirationsNetwork.UserData.DTO.YouthProfileCompletionDTO;
import com.AspirationsNetwork.UserData.DTO.YouthSelfServiceProfileDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.Comment;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.DiscussionPost;
import com.AspirationsNetwork.UserData.Models.ExternalDataset;
import com.AspirationsNetwork.UserData.Models.Notification;
import com.AspirationsNetwork.UserData.Models.ParticipantExternalLink;
import com.AspirationsNetwork.UserData.Models.PlatformEventType;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "https://aspirationnetwork-a633f.web.app"})
@RequestMapping("/api")
public class UserInfoController {
    private final UserInfoService userInfoService;
    private  final DiscussionPostService discussionPostService;
    private final CredentialService credentialService;
    private final AuthService authService;
    private final AttendanceService attendanceService;
    private final ServiceHourService serviceHourService;
    private final ProgramService programService;
    private final ProgramEnrollmentService programEnrollmentService;
    private final DashboardService dashboardService;
    private final RwdLearningService rwdLearningService;
    private final SystemSettingsService systemSettingsService;
    private final NotificationService notificationService;
    private final MetricsService metricsService;
    private final PlatformEventService platformEventService;
    private final PilotReportingService pilotReportingService;
    private final ExternalDatasetLinkService externalDatasetLinkService;
    private final ResearchExportService researchExportService;
    private final StaffOperationEventService staffOperationEventService;

    @GetMapping("/getUser/{id}")
    public ResponseEntity<User> getUser(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }

    @GetMapping("/getUserWithCredentials/{id}")
    public ResponseEntity<UserProfileWithCredentialsDTO> getUserWithCredentials(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.GONE).build();
    }

    @GetMapping("/me/profile")
    public ResponseEntity<YouthSelfServiceProfileDTO> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            User user = userInfoService.getUser(userUID);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            YouthSelfServiceProfileDTO response = new YouthSelfServiceProfileDTO();
            response.setUser(user);
            response.setEarnedCredentials(credentialService.getEarnedCredentialsForUser(userUID));
            response.setAttendanceRecords(attendanceService.getAttendanceRecordsForUser(userUID));
            response.setServiceHourRecords(serviceHourService.getServiceHourRecordsForUser(userUID));
            return ResponseEntity.ok(response);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<YouthSelfServiceProfileDTO> completeMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody YouthProfileCompletionDTO dto
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            User existingUser = userInfoService.getUser(userUID);
            User user = userInfoService.completeYouthProfile(userUID, dto);
            if (existingUser == null) {
                platformEventService.trackEventSafely(
                        userUID,
                        PlatformEventType.ACCOUNT_CREATED,
                        Map.of("source", "protected_profile_flow")
                );
            }
            platformEventService.trackEventSafely(userUID, PlatformEventType.PROFILE_COMPLETED);

            YouthSelfServiceProfileDTO response = new YouthSelfServiceProfileDTO();
            response.setUser(user);
            response.setEarnedCredentials(credentialService.getEarnedCredentialsForUser(userUID));
            response.setAttendanceRecords(attendanceService.getAttendanceRecordsForUser(userUID));
            response.setServiceHourRecords(serviceHourService.getServiceHourRecordsForUser(userUID));
            return ResponseEntity.ok(response);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me/dashboard")
    public ResponseEntity<YouthDashboardDTO> getMyDashboard(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            YouthDashboardDTO dashboard = dashboardService.getYouthDashboard(userUID);
            if (dashboard == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(dashboard);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me/notifications")
    public ResponseEntity<List<Notification>> getMyNotifications(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            return ResponseEntity.ok(notificationService.getNotificationsForUser(userUID));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/me/notifications/{notificationId}/read")
    public ResponseEntity<String> markMyNotificationAsRead(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String notificationId
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            notificationService.markNotificationAsRead(userUID, notificationId);
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating notification");
        }
    }

    @PostMapping("/me/platform-events")
    public ResponseEntity<String> trackMyPlatformEvent(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody PlatformEventRequestDTO dto
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            if (dto == null || dto.getEventType() == null || dto.getEventType().isBlank()) {
                return ResponseEntity.badRequest().body("eventType is required");
            }
            PlatformEventType eventType = PlatformEventType.valueOf(dto.getEventType().trim().toUpperCase());
            if (!isClientTrackableEvent(eventType)) {
                return ResponseEntity.badRequest().body("eventType is not client-trackable");
            }
            Map<String, Object> metadata = dto.getMetadata() == null ? new HashMap<>() : dto.getMetadata();
            platformEventService.trackEventForUser(userUID, eventType, metadata);
            return ResponseEntity.ok("Tracked");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error tracking platform event");
        }
    }

    private boolean isClientTrackableEvent(PlatformEventType eventType) {
        return eventType == PlatformEventType.LOGIN
                || eventType == PlatformEventType.DASHBOARD_VIEW
                || eventType == PlatformEventType.SERVICE_HOURS_VIEWED
                || eventType == PlatformEventType.RWD_ACTIVITY_VIEWED;
    }

    private Map<String, Object> metadata(Object... keyValues) {
        Map<String, Object> metadata = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            metadata.put((String) keyValues[i], keyValues[i + 1]);
        }
        return metadata;
    }

    @GetMapping("/staff/metrics")
    public ResponseEntity<PlatformMetricsDTO> getStaffMetrics(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(metricsService.getPlatformMetrics());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/metrics/reporting")
    public ResponseEntity<PilotReportingDTO> getStaffPilotReportingMetrics(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(pilotReportingService.getPilotReportingMetrics());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/operations/reporting")
    public ResponseEntity<StaffOperationReportingDTO> getStaffOperationReporting(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(staffOperationEventService.buildOperationReport());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/staff/external-datasets")
    public ResponseEntity<String> createExternalDataset(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ExternalDatasetDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            if (dto == null) {
                return ResponseEntity.badRequest().body("external dataset request is required");
            }
            dto.setCreatedByStaffUID(staffUID);
            String externalDatasetId = externalDatasetLinkService.createExternalDataset(dto);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.EXTERNAL_DATASET_CREATED,
                    "externalDataset",
                    externalDatasetId,
                    null,
                    metadata(
                            "datasetName", dto.getDatasetName(),
                            "externalSource", dto.getExternalSource(),
                            "collectionPurpose", dto.getCollectionPurpose()
                    )
            );
            return ResponseEntity.ok(externalDatasetId);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating external dataset");
        }
    }

    @GetMapping("/staff/external-datasets")
    public ResponseEntity<List<ExternalDataset>> getExternalDatasets(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(externalDatasetLinkService.getExternalDatasets());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/staff/external-datasets/{externalDatasetId}")
    public ResponseEntity<String> updateExternalDataset(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String externalDatasetId,
            @RequestBody ExternalDatasetDTO dto
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            externalDatasetLinkService.updateExternalDataset(externalDatasetId, dto);
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating external dataset");
        }
    }

    @PostMapping("/staff/participant-external-links")
    public ResponseEntity<String> createParticipantExternalLink(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ParticipantExternalLinkDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            if (dto == null) {
                return ResponseEntity.badRequest().body("participant external link request is required");
            }
            dto.setLinkedByStaffUID(staffUID);
            String linkId = externalDatasetLinkService.createParticipantExternalLink(dto);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.PARTICIPANT_EXTERNAL_LINK_CREATED,
                    "participantExternalLink",
                    linkId,
                    null,
                    metadata(
                            "aspnParticipantId", dto.getAspnParticipantId(),
                            "externalDatasetId", dto.getExternalDatasetId(),
                            "externalRecordId", dto.getExternalRecordId()
                    )
            );
            return ResponseEntity.ok(linkId);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating participant external link");
        }
    }

    @GetMapping("/staff/participant-external-links/participant/{aspnParticipantId}")
    public ResponseEntity<List<ParticipantExternalLink>> getParticipantExternalLinksByParticipant(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String aspnParticipantId
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(externalDatasetLinkService.getLinksByParticipant(aspnParticipantId));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/participant-external-links/dataset/{externalDatasetId}")
    public ResponseEntity<List<ParticipantExternalLink>> getParticipantExternalLinksByDataset(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String externalDatasetId
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(externalDatasetLinkService.getLinksByDataset(externalDatasetId));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/staff/participant-external-links/{linkId}/remove")
    public ResponseEntity<String> removeParticipantExternalLink(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String linkId
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            externalDatasetLinkService.removeParticipantExternalLink(linkId, staffUID);
            return ResponseEntity.ok("Removed");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error removing participant external link");
        }
    }

    @GetMapping("/staff/research-exports/{exportType}")
    public ResponseEntity<ResearchExportDTO> getStaffResearchExport(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String exportType
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(researchExportService.getResearchExport(exportType));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/getCommentForPost/{postID}")
    public ResponseEntity<List<Comment>> getCommentsForPost(@PathVariable String postID) throws Exception {
        return ResponseEntity.status(HttpStatus.GONE).build();

    }
    @PostMapping("/upVote/{postID}")
    public ResponseEntity<Void> upVote(@PathVariable String postID) throws Exception {
        return ResponseEntity.status(HttpStatus.GONE).build();

    }


    @DeleteMapping("/deletePost/{postID}")
    public ResponseEntity<String> deletePost(@PathVariable String postID) throws Exception {
        return ResponseEntity.status(HttpStatus.GONE).body("Legacy discussion endpoints are disabled");

    }



    @PostMapping("/createProfile")
    public ResponseEntity<String> createProfile(@RequestBody UserProfileCreationDTO dto) {
        return ResponseEntity.status(HttpStatus.GONE).body("Legacy profile creation is disabled; use /api/me/profile");
    }

    @PostMapping({"/createPost", "/creatPost"})
    public ResponseEntity<String> createPost(@RequestBody DiscussionPost discussionPost){
        return ResponseEntity.status(HttpStatus.GONE).body("Legacy discussion endpoints are disabled");

    }

    @PostMapping("/createComment")
    public ResponseEntity<String> createComment(@RequestBody Comment comment){
        return ResponseEntity.status(HttpStatus.GONE).body("Legacy discussion endpoints are disabled");

    }



    @GetMapping("/getallpost")
    public ResponseEntity<List<DiscussionPost>> getallPost() throws Exception {
        return ResponseEntity.status(HttpStatus.GONE).build();

    }

    @GetMapping("/programs")
    public ResponseEntity<List<Program>> getActivePrograms() {
        try {
            return ResponseEntity.ok(programService.getActivePrograms());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/programs/{programId}")
    public ResponseEntity<Program> getActiveProgramById(@PathVariable String programId) {
        try {
            Program program = programService.getActiveProgramById(programId);
            if (program == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(program);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/me/program-enrollments")
    public ResponseEntity<String> enrollMeInProgram(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ProgramEnrollmentDTO dto
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            String programId = dto == null ? null : dto.getProgramId();
            String enrollmentId = programEnrollmentService.enrollYouthInProgram(userUID, programId);
            platformEventService.trackEventSafely(
                    userUID,
                    PlatformEventType.PROGRAM_ENROLLED,
                    Map.of(
                            "programId", programId,
                            "enrollmentId", enrollmentId
                    )
            );
            return ResponseEntity.ok(enrollmentId);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error enrolling in program");
        }
    }

    @GetMapping("/rwd/activities")
    public ResponseEntity<List<RwdActivity>> getActiveRwdActivities() {
        try {
            return ResponseEntity.ok(rwdLearningService.getActiveRwdActivities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/me/rwd-progress")
    public ResponseEntity<List<RwdProgress>> getMyRwdProgress(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            return ResponseEntity.ok(rwdLearningService.getProgressForUser(userUID));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/me/rwd-progress")
    public ResponseEntity<RwdProgress> saveMyRwdProgress(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody RwdProgressDTO dto
    ) {
        try {
            String userUID = authService.requireAuthenticatedUserUid(authorizationHeader);
            return ResponseEntity.ok(rwdLearningService.saveProgressForUser(userUID, dto));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/staff/credentials/definitions")
    public ResponseEntity<String> createCredentialDefinition(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody CredentialDefinitionCreationDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            dto.setCreatedByStaffUID(staffUID);
            String credentialID = credentialService.createCredentialDefinition(dto);
            return ResponseEntity.ok(credentialID);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating credential definition");
        }
    }

    @PostMapping("/staff/credentials/award")
    public ResponseEntity<String> awardCredentialToYouth(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody AwardCredentialDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            dto.setAwardedByStaffUID(staffUID);
            String earnedCredentialID = credentialService.awardCredentialToYouth(dto);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.CREDENTIAL_AWARDED,
                    "earnedCredential",
                    earnedCredentialID,
                    dto.getUserUID(),
                    metadata("credentialID", dto.getCredentialID())
            );
            return ResponseEntity.ok(earnedCredentialID);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error awarding credential");
        }
    }

    @GetMapping("/staff/credentials/definitions")
    public ResponseEntity<List<CredentialDefinition>> getCredentialDefinitionsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "programId", required = false) String programId
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(credentialService.getCredentialDefinitions(category, active, programId));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/credentials/definitions/{credentialID}")
    public ResponseEntity<CredentialDefinition> getCredentialDefinitionForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String credentialID
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            CredentialDefinition definition = credentialService.getCredentialDefinition(credentialID);
            if (definition == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(definition);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/staff/credentials/definitions/{credentialID}")
    public ResponseEntity<String> updateCredentialDefinition(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String credentialID,
            @RequestBody CredentialDefinitionUpdateDTO dto
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            credentialService.updateCredentialDefinition(credentialID, dto);
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating credential definition");
        }
    }

    @PatchMapping("/staff/credentials/definitions/{credentialID}/archive")
    public ResponseEntity<String> archiveCredentialDefinition(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String credentialID
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            credentialService.archiveCredentialDefinition(credentialID);
            return ResponseEntity.ok("Archived");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error archiving credential definition");
        }
    }

    @PatchMapping("/staff/credentials/definitions/{credentialID}/restore")
    public ResponseEntity<String> restoreCredentialDefinition(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String credentialID
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            credentialService.restoreCredentialDefinition(credentialID);
            return ResponseEntity.ok("Restored");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error restoring credential definition");
        }
    }

    @GetMapping("/staff/credentials/totals")
    public ResponseEntity<CredentialTotalsDTO> getCredentialTotalsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "programId", required = false) String programId
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(credentialService.getCredentialTotals(category, programId));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/staff/programs")
    public ResponseEntity<String> createProgram(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ProgramDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            dto.setCreatedByStaffUID(staffUID);
            String programId = programService.createProgram(dto);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.PROGRAM_CREATED,
                    "program",
                    programId,
                    null,
                    metadata(
                            "programName", dto.getProgramName(),
                            "programStatus", dto.getProgramStatus(),
                            "category", dto.getCategory()
                    )
            );
            return ResponseEntity.ok(programId);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating program");
        }
    }

    @GetMapping("/staff/programs")
    public ResponseEntity<List<Program>> getProgramsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "programType", required = false) String programType
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(programService.getPrograms(active, programType));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/programs/totals")
    public ResponseEntity<ProgramTotalsDTO> getProgramTotalsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(programService.getProgramTotals());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/programs/{programId}")
    public ResponseEntity<ProgramDetailDTO> getProgramDetailForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String programId
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            ProgramDetailDTO detail = programService.getProgramDetail(programId);
            if (detail == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(detail);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/staff/programs/{programId}")
    public ResponseEntity<String> updateProgram(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String programId,
            @RequestBody ProgramDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            programService.updateProgram(programId, dto);
            String operationType = dto != null && "archived".equalsIgnoreCase(dto.getProgramStatus())
                    ? StaffOperationEventService.PROGRAM_ARCHIVED
                    : StaffOperationEventService.PROGRAM_UPDATED;
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    operationType,
                    "program",
                    programId,
                    null,
                    metadata(
                            "programName", dto == null ? null : dto.getProgramName(),
                            "programStatus", dto == null ? null : dto.getProgramStatus(),
                            "category", dto == null ? null : dto.getCategory()
                    )
            );
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating program");
        }
    }

    @PatchMapping("/staff/programs/{programId}/archive")
    public ResponseEntity<String> archiveProgram(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String programId
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            programService.archiveProgram(programId);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.PROGRAM_ARCHIVED,
                    "program",
                    programId,
                    null,
                    metadata("programStatus", "archived")
            );
            return ResponseEntity.ok("Archived");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error archiving program");
        }
    }

    @PatchMapping("/staff/programs/{programId}/restore")
    public ResponseEntity<String> restoreProgram(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String programId
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            programService.restoreProgram(programId);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.PROGRAM_UPDATED,
                    "program",
                    programId,
                    null,
                    metadata("programStatus", "active")
            );
            return ResponseEntity.ok("Restored");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error restoring program");
        }
    }

    @PostMapping("/staff/rwd/activities")
    public ResponseEntity<String> createRwdActivity(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody RwdActivityDTO dto
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(rwdLearningService.createRwdActivity(dto));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating RWD activity");
        }
    }

    @PatchMapping("/staff/rwd/activities/{rwdActivityId}")
    public ResponseEntity<String> updateRwdActivity(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String rwdActivityId,
            @RequestBody RwdActivityDTO dto
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            rwdLearningService.updateRwdActivity(rwdActivityId, dto);
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating RWD activity");
        }
    }

    @PatchMapping("/staff/settings/service-hour-request-url")
    public ResponseEntity<String> setServiceHourRequestUrl(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ServiceHourRequestUrlDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            SystemSettingDTO settingDTO = new SystemSettingDTO();
            settingDTO.setSettingValue(dto == null ? null : dto.getServiceHourRequestFormUrl());
            settingDTO.setUpdatedByStaffUID(staffUID);
            systemSettingsService.setServiceHourRequestFormUrl(settingDTO);
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating service-hour request URL");
        }
    }

    @GetMapping("/staff/program-enrollments")
    public ResponseEntity<List<ProgramEnrollment>> getProgramEnrollmentsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(programEnrollmentService.getAllEnrollments());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/program-enrollments/program/{programId}")
    public ResponseEntity<List<ProgramEnrollment>> getProgramEnrollmentsForProgram(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String programId
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(programEnrollmentService.getEnrollmentsForProgram(programId));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/program-enrollments/user/{userUID}")
    public ResponseEntity<List<ProgramEnrollment>> getProgramEnrollmentsForUser(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String userUID
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(programEnrollmentService.getEnrollmentsForUser(userUID));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/staff/program-enrollments/{enrollmentId}/remove")
    public ResponseEntity<String> removeProgramEnrollment(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String enrollmentId
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            programEnrollmentService.removeEnrollment(enrollmentId, staffUID);
            return ResponseEntity.ok("Removed");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error removing program enrollment");
        }
    }

    @GetMapping("/staff/users/youth")
    public ResponseEntity<List<User>> getYouthUsersForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(userInfoService.getYouthUsersForStaff());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/users/youth/{id}")
    public ResponseEntity<User> getYouthUserForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String id
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            User user = userInfoService.getYouthUserForStaff(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/staff/users/youth/{id}")
    public ResponseEntity<String> updateYouthUserForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String id,
            @RequestBody StaffUserUpdateDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            userInfoService.updateYouthUserForStaff(id, dto);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.YOUTH_PROFILE_REVIEWED,
                    "youthProfile",
                    id,
                    id,
                    metadata(
                            "profileStatus", dto == null ? null : dto.getProfileStatus(),
                            "staffReviewRequired", dto == null ? null : dto.getStaffReviewRequired(),
                            "staffVerified", dto == null ? null : dto.getStaffVerified()
                    )
            );
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating youth user");
        }
    }

    @PostMapping("/staff/attendance")
    public ResponseEntity<String> createAttendanceRecord(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody AttendanceRecordCreationDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            dto.setStaffRecorderUID(staffUID);
            String attendanceRecordID = attendanceService.createAttendanceRecord(dto);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.ATTENDANCE_RECORDED,
                    "attendanceRecord",
                    attendanceRecordID,
                    dto.getUserUID(),
                    metadata(
                            "programID", dto.getProgramID(),
                            "eventName", dto.getEventName(),
                            "attendanceStatus", dto.getAttendanceStatus()
                    )
            );
            return ResponseEntity.ok(attendanceRecordID);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating attendance record");
        }
    }

    @GetMapping("/staff/attendance")
    public ResponseEntity<List<AttendanceRecord>> getAttendanceRecordsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "userUID", required = false) String userUID,
            @RequestParam(value = "programID", required = false) String programID,
            @RequestParam(value = "eventDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date eventDate
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(attendanceService.getAttendanceRecords(userUID, programID, eventDate));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/attendance/totals")
    public ResponseEntity<AttendanceTotalsDTO> getAttendanceTotalsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "userUID", required = false) String userUID,
            @RequestParam(value = "programID", required = false) String programID,
            @RequestParam(value = "eventDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date eventDate
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(attendanceService.getAttendanceTotals(userUID, programID, eventDate));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/staff/attendance/{attendanceRecordID}")
    public ResponseEntity<String> updateAttendanceRecord(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String attendanceRecordID,
            @RequestBody AttendanceRecordCreationDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            if (dto == null) {
                return ResponseEntity.badRequest().body("attendance record update request is required");
            }
            dto.setStaffRecorderUID(staffUID);
            attendanceService.updateAttendanceRecord(attendanceRecordID, dto);
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating attendance record");
        }
    }

    @DeleteMapping("/staff/attendance/{attendanceRecordID}")
    public ResponseEntity<String> deleteAttendanceRecord(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String attendanceRecordID
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            attendanceService.deleteAttendanceRecord(attendanceRecordID);
            return ResponseEntity.ok("Deleted");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting attendance record");
        }
    }

    @GetMapping("/staff/attendance/user/{userUID}")
    public ResponseEntity<List<AttendanceRecord>> getAttendanceRecordsForUser(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String userUID
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(attendanceService.getAttendanceRecordsForUser(userUID));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/staff/service-hours")
    public ResponseEntity<String> createOrReviewServiceHourRecord(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ServiceHourRecordDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            dto.setReviewedByStaffUID(staffUID);
            String serviceHourRecordId = serviceHourService.createOrReviewServiceHourRecord(dto);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.SERVICE_HOUR_REVIEWED,
                    "serviceHourRecord",
                    serviceHourRecordId,
                    dto.getUserUID(),
                    metadata(
                            "programId", dto.getProgramId(),
                            "hours", dto.getHours(),
                            "verificationStatus", dto.getVerificationStatus()
                    )
            );
            return ResponseEntity.ok(serviceHourRecordId);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating service-hour record");
        }
    }

    @GetMapping("/staff/service-hours")
    public ResponseEntity<List<ServiceHourRecord>> getServiceHourRecordsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "userUID", required = false) String userUID,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "programId", required = false) String programId,
            @RequestParam(value = "serviceDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date serviceDate
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(serviceHourService.getServiceHourRecords(
                    userUID,
                    status,
                    programId,
                    serviceDate
            ));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/staff/service-hours/totals")
    public ResponseEntity<ServiceHourTotalsDTO> getServiceHourTotalsForStaff(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "userUID", required = false) String userUID,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "programId", required = false) String programId,
            @RequestParam(value = "serviceDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date serviceDate
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(serviceHourService.getServiceHourTotals(
                    userUID,
                    status,
                    programId,
                    serviceDate
            ));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/staff/service-hours/{serviceHourRecordId}/status")
    public ResponseEntity<String> updateServiceHourRecordStatus(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String serviceHourRecordId,
            @RequestBody ServiceHourStatusUpdateDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            if (dto == null) {
                return ResponseEntity.badRequest().body("service-hour status update request is required");
            }
            serviceHourService.updateServiceHourRecordStatus(
                    serviceHourRecordId,
                    dto.getVerificationStatus(),
                    staffUID
            );
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.SERVICE_HOUR_REVIEWED,
                    "serviceHourRecord",
                    serviceHourRecordId,
                    null,
                    metadata("verificationStatus", dto.getVerificationStatus())
            );
            return ResponseEntity.ok("Updated");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating service-hour status");
        }
    }

    @PatchMapping("/staff/service-hours/{serviceHourRecordId}/approve")
    public ResponseEntity<String> approveServiceHourRecord(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String serviceHourRecordId
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            serviceHourService.approveServiceHourRecord(serviceHourRecordId, staffUID);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.SERVICE_HOUR_REVIEWED,
                    "serviceHourRecord",
                    serviceHourRecordId,
                    null,
                    metadata("verificationStatus", "verified")
            );
            return ResponseEntity.ok("Approved");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error approving service-hour record");
        }
    }

    @PatchMapping("/staff/service-hours/{serviceHourRecordId}/reject")
    public ResponseEntity<String> rejectServiceHourRecord(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String serviceHourRecordId
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            serviceHourService.rejectServiceHourRecord(serviceHourRecordId, staffUID);
            staffOperationEventService.trackOperationSafely(
                    staffUID,
                    StaffOperationEventService.SERVICE_HOUR_REVIEWED,
                    "serviceHourRecord",
                    serviceHourRecordId,
                    null,
                    metadata("verificationStatus", "rejected")
            );
            return ResponseEntity.ok("Rejected");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error rejecting service-hour record");
        }
    }

    @DeleteMapping("/staff/service-hours/{serviceHourRecordId}")
    public ResponseEntity<String> deleteServiceHourRecord(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String serviceHourRecordId
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            serviceHourService.deleteServiceHourRecord(serviceHourRecordId);
            return ResponseEntity.ok("Deleted");
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting service-hour record");
        }
    }

    @GetMapping("/staff/service-hours/user/{userUID}")
    public ResponseEntity<List<ServiceHourRecord>> getServiceHourRecordsForUser(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String userUID
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            return ResponseEntity.ok(serviceHourService.getServiceHourRecordsForUser(userUID));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (ForbiddenAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }





}
