package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PilotMetricsDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.AspirationsNetwork.UserData.Models.Educator;
import com.AspirationsNetwork.UserData.Models.GovernmentOrganization;
import com.AspirationsNetwork.UserData.Models.PartnerOrganization;
import com.AspirationsNetwork.UserData.Models.PlatformEvent;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import com.AspirationsNetwork.UserData.Models.StaffOperationEvent;
import com.AspirationsNetwork.UserData.Models.StakeholderRelationshipNote;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PilotMetricsService {
    private static final long DAY_MILLIS = TimeUnit.DAYS.toMillis(1);

    private final Firestore firestore;

    public PilotMetricsDTO getPilotMetrics() throws Exception {
        return buildMetrics(
                getCollectionObjects(UserInfoService.COLLECTION_NAME, User.class),
                getCollectionObjects(ProgramService.COLLECTION_NAME, Program.class),
                getCollectionObjects(ProgramEnrollmentService.COLLECTION_NAME, ProgramEnrollment.class),
                getCollectionObjects(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION, CredentialDefinition.class),
                getCollectionObjects(CredentialService.EARNED_CREDENTIALS_COLLECTION, EarnedCredential.class),
                getCollectionObjects(AttendanceService.COLLECTION_NAME, AttendanceRecord.class),
                getCollectionObjects(ServiceHourService.COLLECTION_NAME, ServiceHourRecord.class),
                getCollectionObjects(EducatorService.COLLECTION_NAME, Educator.class),
                getCollectionObjects(PartnerOrganizationService.COLLECTION_NAME, PartnerOrganization.class),
                getCollectionObjects(GovernmentOrganizationService.COLLECTION_NAME, GovernmentOrganization.class),
                getCollectionObjects(StakeholderRelationshipNoteService.COLLECTION_NAME, StakeholderRelationshipNote.class),
                getCollectionObjects(PlatformEventService.COLLECTION_NAME, PlatformEvent.class),
                getCollectionObjects(StaffOperationEventService.COLLECTION_NAME, StaffOperationEvent.class),
                new Date()
        );
    }

    PilotMetricsDTO buildMetrics(
            List<User> users,
            List<Program> programs,
            List<ProgramEnrollment> enrollments,
            List<CredentialDefinition> credentialDefinitions,
            List<EarnedCredential> earnedCredentials,
            List<AttendanceRecord> attendanceRecords,
            List<ServiceHourRecord> serviceHourRecords,
            List<Educator> educators,
            List<PartnerOrganization> partnerOrganizations,
            List<GovernmentOrganization> governmentOrganizations,
            List<StakeholderRelationshipNote> relationshipNotes,
            List<PlatformEvent> platformEvents,
            List<StaffOperationEvent> staffOperationEvents,
            Date now
    ) {
        PilotMetricsDTO metrics = new PilotMetricsDTO();
        setUserFunnel(metrics, users, platformEvents, now);
        setProgramEngagement(metrics, programs, enrollments, attendanceRecords);
        setCredentialEngagement(metrics, credentialDefinitions, earnedCredentials);
        setServiceEngagement(metrics, serviceHourRecords);
        setStakeholderEngagement(metrics, educators, partnerOrganizations, governmentOrganizations, relationshipNotes, now);
        setOperationalMetrics(metrics, staffOperationEvents, now);
        return metrics;
    }

    private void setUserFunnel(PilotMetricsDTO metrics, List<User> users, List<PlatformEvent> platformEvents, Date now) {
        List<User> youthUsers = users.stream().filter(User::isYouthProfile).toList();
        int activeUsers = (int) youthUsers.stream()
                .filter(user -> "active".equalsIgnoreCase(user.getProfileStatus()))
                .count();

        metrics.setTotalRegistrations(youthUsers.size());
        metrics.setActiveUsers(activeUsers);
        metrics.setProfileCompletions(activeUsers);
        metrics.setProfileCompletionRate(percentage(activeUsers, youthUsers.size()));
        metrics.setActiveLast30Days(countActiveParticipants(platformEvents, now, 30));
        metrics.setActiveLast60Days(countActiveParticipants(platformEvents, now, 60));
        metrics.setActiveLast90Days(countActiveParticipants(platformEvents, now, 90));
    }

    private void setProgramEngagement(
            PilotMetricsDTO metrics,
            List<Program> programs,
            List<ProgramEnrollment> enrollments,
            List<AttendanceRecord> attendanceRecords
    ) {
        List<ProgramEnrollment> activeEnrollments = enrollments.stream()
                .filter(this::isActiveEnrollment)
                .toList();
        Set<String> activeParticipantUids = activeEnrollments.stream()
                .map(ProgramEnrollment::getUserUID)
                .filter(this::hasText)
                .collect(Collectors.toSet());

        metrics.setTotalPrograms(programs.size());
        metrics.setActivePrograms((int) programs.stream().filter(this::isActiveProgram).count());
        metrics.setTotalEnrollments(enrollments.size());
        metrics.setActiveParticipants(activeParticipantUids.size());
        metrics.setAttendanceRecords(attendanceRecords.size());
        metrics.setAttendanceRate(percentage(
                (int) attendanceRecords.stream().filter(this::isPresentAttendance).count(),
                attendanceRecords.size()
        ));
        for (ProgramEnrollment enrollment : activeEnrollments) {
            if (hasText(enrollment.getProgramId())) {
                metrics.getProgramParticipationCounts().merge(enrollment.getProgramId(), 1, Integer::sum);
            }
        }
    }

    private void setCredentialEngagement(
            PilotMetricsDTO metrics,
            List<CredentialDefinition> credentialDefinitions,
            List<EarnedCredential> earnedCredentials
    ) {
        Map<String, CredentialDefinition> definitionsById = credentialDefinitions.stream()
                .filter(definition -> hasText(definition.getCredentialID()))
                .collect(Collectors.toMap(
                        CredentialDefinition::getCredentialID,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        metrics.setCredentialDefinitions(credentialDefinitions.size());
        metrics.setActiveCredentialDefinitions((int) credentialDefinitions.stream().filter(CredentialDefinition::isActive).count());
        metrics.setCredentialsAwarded(earnedCredentials.size());

        for (EarnedCredential earnedCredential : earnedCredentials) {
            CredentialDefinition definition = definitionsById.get(earnedCredential.getCredentialID());
            String category = definition == null || !hasText(definition.getCategory())
                    ? "Uncategorized"
                    : definition.getCategory();
            metrics.getCredentialsByCategory().merge(category, 1, Integer::sum);

            if (definition != null && definition.getProgramIds() != null) {
                for (String programId : definition.getProgramIds()) {
                    if (hasText(programId)) {
                        metrics.getCredentialsByProgram().merge(programId, 1, Integer::sum);
                    }
                }
            }
        }
    }

    private void setServiceEngagement(PilotMetricsDTO metrics, List<ServiceHourRecord> serviceHourRecords) {
        metrics.setServiceHourSubmissions(serviceHourRecords.size());

        for (ServiceHourRecord record : serviceHourRecords) {
            if (!"verified".equalsIgnoreCase(record.getVerificationStatus())) {
                continue;
            }
            metrics.setApprovedServiceHourSubmissions(metrics.getApprovedServiceHourSubmissions() + 1);
            metrics.setTotalApprovedServiceHours(metrics.getTotalApprovedServiceHours() + record.getHours());
            if (hasText(record.getProgramId())) {
                metrics.getServiceHoursByProgram().merge(record.getProgramId(), record.getHours(), Double::sum);
            }
        }
    }

    private void setStakeholderEngagement(
            PilotMetricsDTO metrics,
            List<Educator> educators,
            List<PartnerOrganization> partnerOrganizations,
            List<GovernmentOrganization> governmentOrganizations,
            List<StakeholderRelationshipNote> relationshipNotes,
            Date now
    ) {
        metrics.setEducators(educators.size());
        metrics.setPartnerOrganizations(partnerOrganizations.size());
        metrics.setGovernmentOrganizations(governmentOrganizations.size());
        metrics.setRelationshipNotes(relationshipNotes.size());

        for (StakeholderRelationshipNote note : relationshipNotes) {
            if (note.isActive()) {
                metrics.setActiveRelationshipNotes(metrics.getActiveRelationshipNotes() + 1);
            }
            Date followUp = note.getNextFollowUpDate();
            if (note.isActive() && followUp != null) {
                if (followUp.before(now)) {
                    metrics.setOverdueFollowUps(metrics.getOverdueFollowUps() + 1);
                } else {
                    metrics.setUpcomingFollowUps(metrics.getUpcomingFollowUps() + 1);
                }
            }
        }
    }

    private void setOperationalMetrics(PilotMetricsDTO metrics, List<StaffOperationEvent> staffOperationEvents, Date now) {
        metrics.setStaffOperationsLast30Days(countStaffOperations(staffOperationEvents, now, 30));
        metrics.setStaffOperationsLast60Days(countStaffOperations(staffOperationEvents, now, 60));
        metrics.setStaffOperationsLast90Days(countStaffOperations(staffOperationEvents, now, 90));
    }

    private int countActiveParticipants(List<PlatformEvent> events, Date now, int days) {
        long cutoff = now.getTime() - days * DAY_MILLIS;
        return events.stream()
                .filter(event -> hasText(event.getAspnParticipantId()))
                .filter(event -> event.getEventTimestamp() != null)
                .filter(event -> event.getEventTimestamp().getTime() >= cutoff)
                .map(PlatformEvent::getAspnParticipantId)
                .collect(Collectors.toSet())
                .size();
    }

    private int countStaffOperations(List<StaffOperationEvent> events, Date now, int days) {
        long cutoff = now.getTime() - days * DAY_MILLIS;
        return (int) events.stream()
                .filter(event -> event.getCreatedAt() != null)
                .filter(event -> event.getCreatedAt().getTime() >= cutoff)
                .count();
    }

    private boolean isActiveEnrollment(ProgramEnrollment enrollment) {
        return "active".equalsIgnoreCase(enrollment.getEnrollmentStatus());
    }

    private boolean isActiveProgram(Program program) {
        return "active".equalsIgnoreCase(program.getProgramStatus());
    }

    private boolean isPresentAttendance(AttendanceRecord attendanceRecord) {
        return "present".equalsIgnoreCase(attendanceRecord.getAttendanceStatus());
    }

    private double percentage(int numerator, int denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return Math.round((numerator * 10000.0) / denominator) / 100.0;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> List<T> getCollectionObjects(String collectionName, Class<T> clazz) throws Exception {
        return firestore.collection(collectionName)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(document -> document.toObject(clazz))
                .filter(item -> item != null)
                .toList();
    }
}
