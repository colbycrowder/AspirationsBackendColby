package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.ResearchExportDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.AspirationsNetwork.UserData.Models.ParticipantExternalLink;
import com.AspirationsNetwork.UserData.Models.PlatformEvent;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.AspirationsNetwork.UserData.Models.RwdActivity;
import com.AspirationsNetwork.UserData.Models.RwdProgress;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResearchExportService {
    public static final String PARTICIPANTS_EXPORT = "participants_export";
    public static final String PLATFORM_EVENTS_EXPORT = "platform_events_export";
    public static final String CREDENTIALS_EXPORT = "credentials_export";
    public static final String PROGRAM_PARTICIPATION_EXPORT = "program_participation_export";
    public static final String ATTENDANCE_EXPORT = "attendance_export";
    public static final String SERVICE_HOURS_EXPORT = "service_hours_export";
    public static final String RWD_PROGRESS_EXPORT = "rwd_progress_export";
    public static final String EXTERNAL_LINKS_EXPORT = "external_links_export";

    public static final Set<String> SUPPORTED_EXPORT_TYPES = Set.of(
            PARTICIPANTS_EXPORT,
            PLATFORM_EVENTS_EXPORT,
            CREDENTIALS_EXPORT,
            PROGRAM_PARTICIPATION_EXPORT,
            ATTENDANCE_EXPORT,
            SERVICE_HOURS_EXPORT,
            RWD_PROGRESS_EXPORT,
            EXTERNAL_LINKS_EXPORT
    );

    private final Firestore firestore;

    public ResearchExportDTO getResearchExport(String exportType) throws Exception {
        requireSupportedExportType(exportType);

        List<User> users = getCollectionObjects(UserInfoService.COLLECTION_NAME, User.class);
        Map<String, String> participantIdsByUserUid = participantIdsByUserUid(users);

        return switch (exportType) {
            case PARTICIPANTS_EXPORT -> buildParticipantsExport(users);
            case PLATFORM_EVENTS_EXPORT -> buildPlatformEventsExport(
                    getCollectionObjects(PlatformEventService.COLLECTION_NAME, PlatformEvent.class)
            );
            case CREDENTIALS_EXPORT -> buildCredentialsExport(
                    getCollectionObjects(CredentialService.EARNED_CREDENTIALS_COLLECTION, EarnedCredential.class),
                    credentialDefinitionsById(),
                    participantIdsByUserUid
            );
            case PROGRAM_PARTICIPATION_EXPORT -> buildProgramParticipationExport(
                    getCollectionObjects(ProgramEnrollmentService.COLLECTION_NAME, ProgramEnrollment.class),
                    programsById(),
                    participantIdsByUserUid
            );
            case ATTENDANCE_EXPORT -> buildAttendanceExport(
                    getCollectionObjects(AttendanceService.COLLECTION_NAME, AttendanceRecord.class),
                    participantIdsByUserUid
            );
            case SERVICE_HOURS_EXPORT -> buildServiceHoursExport(
                    getCollectionObjects(ServiceHourService.COLLECTION_NAME, ServiceHourRecord.class),
                    participantIdsByUserUid
            );
            case RWD_PROGRESS_EXPORT -> buildRwdProgressExport(
                    getCollectionObjects(RwdLearningService.PROGRESS_COLLECTION, RwdProgress.class),
                    rwdActivitiesById(),
                    participantIdsByUserUid
            );
            case EXTERNAL_LINKS_EXPORT -> buildExternalLinksExport(
                    getCollectionObjects(
                            ExternalDatasetLinkService.PARTICIPANT_EXTERNAL_LINKS_COLLECTION,
                            ParticipantExternalLink.class
                    )
            );
            default -> throw new IllegalArgumentException("Unsupported export type");
        };
    }

    ResearchExportDTO buildParticipantsExport(List<User> users) {
        List<Map<String, Object>> rows = users.stream()
                .filter(User::isYouthProfile)
                .filter(user -> hasText(user.getAspnParticipantId()))
                .map(user -> row(
                        "aspnParticipantId", user.getAspnParticipantId(),
                        "aspnParticipantCohortYear", user.getAspnParticipantCohortYear(),
                        "accountType", user.getAccountType(),
                        "profileStatus", user.getProfileStatus(),
                        "school", user.getSchool(),
                        "graduationYear", user.getGraduationYear(),
                        "programIds", user.getProgramIds(),
                        "externalConsentReceived", user.isExternalConsentReceived(),
                        "staffVerified", user.isStaffVerified(),
                        "createdProfileType", user.isYouthProfile() ? "youth" : "non_youth"
                ))
                .toList();
        return export(PARTICIPANTS_EXPORT, rows);
    }

    ResearchExportDTO buildPlatformEventsExport(List<PlatformEvent> events) {
        List<Map<String, Object>> rows = events.stream()
                .filter(event -> hasText(event.getAspnParticipantId()))
                .map(event -> row(
                        "aspnParticipantId", event.getAspnParticipantId(),
                        "eventId", event.getEventId(),
                        "eventType", event.getEventType(),
                        "eventTimestamp", event.getEventTimestamp(),
                        "metadata", sanitizedMetadata(event.getMetadata())
                ))
                .toList();
        return export(PLATFORM_EVENTS_EXPORT, rows);
    }

    ResearchExportDTO buildCredentialsExport(
            List<EarnedCredential> earnedCredentials,
            Map<String, CredentialDefinition> definitionsById,
            Map<String, String> participantIdsByUserUid
    ) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (EarnedCredential credential : earnedCredentials) {
            String aspnParticipantId = participantIdsByUserUid.get(credential.getUserUID());
            if (!hasText(aspnParticipantId)) {
                continue;
            }

            CredentialDefinition definition = definitionsById.get(credential.getCredentialID());
            rows.add(row(
                    "aspnParticipantId", aspnParticipantId,
                    "earnedCredentialID", credential.getEarnedCredentialID(),
                    "credentialID", credential.getCredentialID(),
                    "credentialName", definition == null ? null : definition.getCredentialName(),
                    "credentialCategory", definition == null ? null : definition.getCategory(),
                    "credentialActive", definition == null ? null : definition.isActive(),
                    "status", credential.getStatus(),
                    "earnedAt", credential.getEarnedAt(),
                    "awardedAt", credential.getAwardedAt(),
                    "updatedAt", credential.getUpdatedAt()
            ));
        }
        return export(CREDENTIALS_EXPORT, rows);
    }

    ResearchExportDTO buildProgramParticipationExport(
            List<ProgramEnrollment> enrollments,
            Map<String, Program> programsById,
            Map<String, String> participantIdsByUserUid
    ) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ProgramEnrollment enrollment : enrollments) {
            String aspnParticipantId = participantIdsByUserUid.get(enrollment.getUserUID());
            if (!hasText(aspnParticipantId)) {
                continue;
            }

            Program program = programsById.get(enrollment.getProgramId());
            rows.add(row(
                    "aspnParticipantId", aspnParticipantId,
                    "enrollmentId", enrollment.getEnrollmentId(),
                    "programId", enrollment.getProgramId(),
                    "programName", program == null ? null : program.getProgramName(),
                    "programCategory", program == null ? null : program.getCategory(),
                    "programStatus", program == null ? null : program.getProgramStatus(),
                    "enrollmentStatus", enrollment.getEnrollmentStatus(),
                    "enrolledAt", enrollment.getEnrolledAt(),
                    "updatedAt", enrollment.getUpdatedAt(),
                    "createdByUser", enrollment.isCreatedByUser()
            ));
        }
        return export(PROGRAM_PARTICIPATION_EXPORT, rows);
    }

    ResearchExportDTO buildAttendanceExport(
            List<AttendanceRecord> attendanceRecords,
            Map<String, String> participantIdsByUserUid
    ) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AttendanceRecord attendanceRecord : attendanceRecords) {
            String aspnParticipantId = participantIdsByUserUid.get(attendanceRecord.getUserUID());
            if (!hasText(aspnParticipantId)) {
                continue;
            }

            rows.add(row(
                    "aspnParticipantId", aspnParticipantId,
                    "attendanceRecordID", attendanceRecord.getAttendanceRecordID(),
                    "programID", attendanceRecord.getProgramID(),
                    "eventName", attendanceRecord.getEventName(),
                    "eventDate", attendanceRecord.getEventDate(),
                    "attendanceStatus", attendanceRecord.getAttendanceStatus(),
                    "createdAt", attendanceRecord.getCreatedAt(),
                    "updatedAt", attendanceRecord.getUpdatedAt()
            ));
        }
        return export(ATTENDANCE_EXPORT, rows);
    }

    ResearchExportDTO buildServiceHoursExport(
            List<ServiceHourRecord> serviceHourRecords,
            Map<String, String> participantIdsByUserUid
    ) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ServiceHourRecord serviceHourRecord : serviceHourRecords) {
            String aspnParticipantId = participantIdsByUserUid.get(serviceHourRecord.getUserUID());
            if (!hasText(aspnParticipantId)) {
                continue;
            }

            rows.add(row(
                    "aspnParticipantId", aspnParticipantId,
                    "serviceHourRecordId", serviceHourRecord.getServiceHourRecordId(),
                    "programId", serviceHourRecord.getProgramId(),
                    "serviceDate", serviceHourRecord.getServiceDate(),
                    "hours", serviceHourRecord.getHours(),
                    "description", serviceHourRecord.getDescription(),
                    "verificationStatus", serviceHourRecord.getVerificationStatus(),
                    "verificationSource", serviceHourRecord.getVerificationSource(),
                    "hasGoogleFormResponseUrl", hasText(serviceHourRecord.getGoogleFormResponseUrl()),
                    "submittedAt", serviceHourRecord.getSubmittedAt(),
                    "reviewedAt", serviceHourRecord.getReviewedAt(),
                    "createdAt", serviceHourRecord.getCreatedAt(),
                    "updatedAt", serviceHourRecord.getUpdatedAt()
            ));
        }
        return export(SERVICE_HOURS_EXPORT, rows);
    }

    ResearchExportDTO buildRwdProgressExport(
            List<RwdProgress> progressRecords,
            Map<String, RwdActivity> activitiesById,
            Map<String, String> participantIdsByUserUid
    ) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (RwdProgress progress : progressRecords) {
            String aspnParticipantId = participantIdsByUserUid.get(progress.getUserUID());
            if (!hasText(aspnParticipantId)) {
                continue;
            }

            RwdActivity activity = activitiesById.get(progress.getRwdActivityId());
            rows.add(row(
                    "aspnParticipantId", aspnParticipantId,
                    "progressId", progress.getProgressId(),
                    "rwdActivityId", progress.getRwdActivityId(),
                    "countryName", activity == null ? null : activity.getCountryName(),
                    "activityTitle", activity == null ? null : activity.getTitle(),
                    "associatedCredentialId", activity == null ? null : activity.getAssociatedCredentialId(),
                    "completionStatus", progress.getCompletionStatus(),
                    "quizScore", progress.getQuizScore(),
                    "passed", progress.isPassed(),
                    "completedAt", progress.getCompletedAt(),
                    "credentialAwarded", progress.isCredentialAwarded(),
                    "earnedCredentialId", progress.getEarnedCredentialId()
            ));
        }
        return export(RWD_PROGRESS_EXPORT, rows);
    }

    ResearchExportDTO buildExternalLinksExport(List<ParticipantExternalLink> links) {
        List<Map<String, Object>> rows = links.stream()
                .filter(link -> hasText(link.getAspnParticipantId()))
                .map(link -> row(
                        "aspnParticipantId", link.getAspnParticipantId(),
                        "linkId", link.getLinkId(),
                        "externalDatasetId", link.getExternalDatasetId(),
                        "externalDatasetName", link.getExternalDatasetName(),
                        "externalSource", link.getExternalSource(),
                        "externalRecordId", link.getExternalRecordId(),
                        "linkStatus", link.getLinkStatus(),
                        "linkedAt", link.getLinkedAt(),
                        "updatedAt", link.getUpdatedAt(),
                        "removedAt", link.getRemovedAt(),
                        "hasNotes", hasText(link.getNotes())
                ))
                .toList();
        return export(EXTERNAL_LINKS_EXPORT, rows);
    }

    private Map<String, String> participantIdsByUserUid(List<User> users) {
        return users.stream()
                .filter(User::isYouthProfile)
                .filter(user -> hasText(user.getUid()))
                .filter(user -> hasText(user.getAspnParticipantId()))
                .collect(Collectors.toMap(
                        User::getUid,
                        User::getAspnParticipantId,
                        (existing, replacement) -> existing
                ));
    }

    private Map<String, CredentialDefinition> credentialDefinitionsById() throws Exception {
        return getCollectionObjects(
                CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION,
                CredentialDefinition.class
        ).stream()
                .filter(definition -> hasText(definition.getCredentialID()))
                .collect(Collectors.toMap(
                        CredentialDefinition::getCredentialID,
                        definition -> definition,
                        (existing, replacement) -> existing
                ));
    }

    private Map<String, Program> programsById() throws Exception {
        return getCollectionObjects(ProgramService.COLLECTION_NAME, Program.class).stream()
                .filter(program -> hasText(program.getProgramId()))
                .collect(Collectors.toMap(
                        Program::getProgramId,
                        program -> program,
                        (existing, replacement) -> existing
                ));
    }

    private Map<String, RwdActivity> rwdActivitiesById() throws Exception {
        return getCollectionObjects(RwdLearningService.ACTIVITIES_COLLECTION, RwdActivity.class).stream()
                .filter(activity -> hasText(activity.getRwdActivityId()))
                .collect(Collectors.toMap(
                        RwdActivity::getRwdActivityId,
                        activity -> activity,
                        (existing, replacement) -> existing
                ));
    }

    private ResearchExportDTO export(String exportType, List<Map<String, Object>> rows) {
        ResearchExportDTO dto = new ResearchExportDTO();
        dto.setExportType(exportType);
        dto.setGeneratedAt(new Date());
        dto.setRecords(rows);
        dto.setRecordCount(rows.size());
        return dto;
    }

    private Map<String, Object> row(Object... keyValues) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            row.put((String) keyValues[i], keyValues[i + 1]);
        }
        return row;
    }

    private Map<String, Object> sanitizedMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (isUidLikeKey(entry.getKey())) {
                continue;
            }
            sanitized.put(entry.getKey(), entry.getValue());
        }
        return sanitized;
    }

    private boolean isUidLikeKey(String key) {
        return key != null && key.toLowerCase().contains("uid");
    }

    private <T> List<T> getCollectionObjects(String collectionName, Class<T> type) throws Exception {
        List<T> results = new ArrayList<>();
        for (QueryDocumentSnapshot document : firestore.collection(collectionName).get().get().getDocuments()) {
            T item = document.toObject(type);
            if (item != null) {
                results.add(item);
            }
        }
        return results;
    }

    private void requireSupportedExportType(String exportType) {
        if (!SUPPORTED_EXPORT_TYPES.contains(exportType)) {
            throw new IllegalArgumentException("Unsupported research export type");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
