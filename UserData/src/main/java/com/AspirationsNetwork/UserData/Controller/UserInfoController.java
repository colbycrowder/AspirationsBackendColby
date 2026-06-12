package com.AspirationsNetwork.UserData.Controller;

import com.AspirationsNetwork.UserData.DTO.AttendanceRecordCreationDTO;
import com.AspirationsNetwork.UserData.DTO.AwardCredentialDTO;
import com.AspirationsNetwork.UserData.DTO.CredentialDefinitionCreationDTO;
import com.AspirationsNetwork.UserData.DTO.ExternalDatasetDTO;
import com.AspirationsNetwork.UserData.DTO.ParticipantExternalLinkDTO;
import com.AspirationsNetwork.UserData.DTO.PlatformEventRequestDTO;
import com.AspirationsNetwork.UserData.DTO.PlatformMetricsDTO;
import com.AspirationsNetwork.UserData.DTO.PilotReportingDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramEnrollmentDTO;
import com.AspirationsNetwork.UserData.DTO.ProgramDTO;
import com.AspirationsNetwork.UserData.DTO.ResearchExportDTO;
import com.AspirationsNetwork.UserData.DTO.RwdActivityDTO;
import com.AspirationsNetwork.UserData.DTO.RwdProgressDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourRecordDTO;
import com.AspirationsNetwork.UserData.DTO.ServiceHourRequestUrlDTO;
import com.AspirationsNetwork.UserData.DTO.StaffUserUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.SystemSettingDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileCreationDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileWithCredentialsDTO;
import com.AspirationsNetwork.UserData.DTO.YouthDashboardDTO;
import com.AspirationsNetwork.UserData.DTO.YouthProfileCompletionDTO;
import com.AspirationsNetwork.UserData.DTO.YouthSelfServiceProfileDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.Comment;
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
import com.AspirationsNetwork.UserData.Service.SystemSettingsService;
import com.AspirationsNetwork.UserData.Service.UnauthorizedAccessException;
import com.AspirationsNetwork.UserData.Service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
            return ResponseEntity.ok(externalDatasetLinkService.createExternalDataset(dto));
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
            return ResponseEntity.ok(externalDatasetLinkService.createParticipantExternalLink(dto));
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

    @PostMapping("/staff/programs")
    public ResponseEntity<String> createProgram(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody ProgramDTO dto
    ) {
        try {
            String staffUID = authService.requireStaff(authorizationHeader);
            dto.setCreatedByStaffUID(staffUID);
            String programId = programService.createProgram(dto);
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

    @PatchMapping("/staff/programs/{programId}")
    public ResponseEntity<String> updateProgram(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String programId,
            @RequestBody ProgramDTO dto
    ) {
        try {
            authService.requireStaff(authorizationHeader);
            programService.updateProgram(programId, dto);
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
            authService.requireStaff(authorizationHeader);
            userInfoService.updateYouthUserForStaff(id, dto);
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
