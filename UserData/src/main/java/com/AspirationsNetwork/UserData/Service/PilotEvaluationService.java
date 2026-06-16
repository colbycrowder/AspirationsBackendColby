package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PilotEvaluationDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.AspirationsNetwork.UserData.Models.Educator;
import com.AspirationsNetwork.UserData.Models.GovernmentOrganization;
import com.AspirationsNetwork.UserData.Models.PartnerOrganization;
import com.AspirationsNetwork.UserData.Models.PlatformEvent;
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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PilotEvaluationService {
    private static final int REGISTRATION_TARGET = 75;
    private static final long DAY_MILLIS = TimeUnit.DAYS.toMillis(1);

    private final Firestore firestore;

    public PilotEvaluationDTO getPilotEvaluation() throws Exception {
        return buildEvaluation(
                getCollectionObjects(UserInfoService.COLLECTION_NAME, User.class),
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

    PilotEvaluationDTO buildEvaluation(
            List<User> users,
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
        PilotEvaluationDTO evaluation = new PilotEvaluationDTO();
        evaluation.setGeneratedAt(now);

        List<User> youthUsers = users.stream().filter(User::isYouthProfile).toList();
        Set<String> registeredYouthUids = youthUsers.stream()
                .map(User::getUid)
                .filter(this::hasText)
                .collect(Collectors.toSet());
        Set<String> activeEnrollmentUsers = enrollments.stream()
                .filter(this::isActiveEnrollment)
                .map(ProgramEnrollment::getUserUID)
                .filter(registeredYouthUids::contains)
                .collect(Collectors.toSet());
        Set<String> credentialUsers = earnedCredentials.stream()
                .map(EarnedCredential::getUserUID)
                .filter(registeredYouthUids::contains)
                .collect(Collectors.toSet());
        Set<String> serviceHourUsers = serviceHourRecords.stream()
                .filter(this::isApprovedServiceHour)
                .map(ServiceHourRecord::getUserUID)
                .filter(registeredYouthUids::contains)
                .collect(Collectors.toSet());

        int registrations = youthUsers.size();
        int activeUsers = (int) youthUsers.stream()
                .filter(user -> "active".equalsIgnoreCase(user.getProfileStatus()))
                .count();
        int active30DayUsers = countActiveParticipants(platformEvents, now, 30);
        int active90DayUsers = countActiveParticipants(platformEvents, now, 90);
        int activeEnrollments = (int) enrollments.stream().filter(this::isActiveEnrollment).count();
        int presentAttendanceRecords = (int) attendanceRecords.stream().filter(this::isPresentAttendance).count();
        int credentialsAwarded = earnedCredentials.size();
        double approvedServiceHours = serviceHourRecords.stream()
                .filter(this::isApprovedServiceHour)
                .mapToDouble(ServiceHourRecord::getHours)
                .sum();
        int staffActionsLast30Days = countStaffOperations(staffOperationEvents, now, 30);
        int platformEventsLast30Days = countPlatformEvents(platformEvents, now, 30);
        int activeRelationshipNotes = (int) relationshipNotes.stream().filter(StakeholderRelationshipNote::isActive).count();
        int upcomingFollowUps = countFollowUps(relationshipNotes, now, false);
        int overdueFollowUps = countFollowUps(relationshipNotes, now, true);

        double profileCompletionRate = percentage(activeUsers, registrations);
        double retentionRate = percentage(active90DayUsers, registrations);
        double participationRate = percentage(activeEnrollmentUsers.size(), registrations);
        double attendanceRate = percentage(presentAttendanceRecords, attendanceRecords.size());
        double credentialParticipationRate = percentage(credentialUsers.size(), registrations);
        double credentialsPerParticipant = registrations == 0 ? 0.0 : round(credentialsAwarded / (double) registrations);
        double averageHoursPerParticipant = serviceHourUsers.isEmpty() ? 0.0 : round(approvedServiceHours / serviceHourUsers.size());
        double relationshipFollowUpCompletionRate = percentage(upcomingFollowUps, upcomingFollowUps + overdueFollowUps);

        evaluation.setRegistrations(registrations);
        evaluation.setActiveUsers(activeUsers);
        evaluation.setProfileCompletionRate(profileCompletionRate);
        evaluation.setActive30DayUsers(active30DayUsers);
        evaluation.setRetentionRate(retentionRate);
        evaluation.setEnrollments(enrollments.size());
        evaluation.setActiveEnrollments(activeEnrollments);
        evaluation.setAttendanceRate(attendanceRate);
        evaluation.setParticipationRate(participationRate);
        evaluation.setCredentialsAwarded(credentialsAwarded);
        evaluation.setCredentialsPerParticipant(credentialsPerParticipant);
        evaluation.setCredentialParticipationRate(credentialParticipationRate);
        evaluation.setApprovedServiceHours(round(approvedServiceHours));
        evaluation.setServiceHourParticipants(serviceHourUsers.size());
        evaluation.setAverageHoursPerParticipant(averageHoursPerParticipant);
        evaluation.setEducatorCount(educators.size());
        evaluation.setPartnerCount(partnerOrganizations.size());
        evaluation.setGovernmentOrganizationCount(governmentOrganizations.size());
        evaluation.setRelationshipFollowUpCompletionRate(relationshipFollowUpCompletionRate);
        evaluation.setStaffActionsLast30Days(staffActionsLast30Days);
        evaluation.setPlatformEventsLast30Days(platformEventsLast30Days);
        evaluation.setRelationshipNoteActivity(activeRelationshipNotes);

        evaluation.setYouthOutcomeScore(average(
                cappedTargetScore(registrations, REGISTRATION_TARGET),
                scoreFromPercentage(profileCompletionRate),
                scoreFromPercentage(percentage(active30DayUsers, registrations)),
                scoreFromPercentage(retentionRate)
        ));
        evaluation.setProgramOutcomeScore(average(
                scoreFromPercentage(participationRate),
                scoreFromPercentage(attendanceRate),
                activeEnrollments > 0 ? 100 : 0
        ));
        evaluation.setCredentialOutcomeScore(average(
                scoreFromPercentage(credentialParticipationRate),
                cappedTargetScore(credentialsAwarded, registrations),
                credentialDefinitions.stream().anyMatch(CredentialDefinition::isActive) ? 100 : 0
        ));
        evaluation.setServiceOutcomeScore(average(
                scoreFromPercentage(percentage(serviceHourUsers.size(), registrations)),
                cappedTargetScore(approvedServiceHours, Math.max(1, registrations)),
                serviceHourUsers.isEmpty() ? 0 : 100
        ));
        evaluation.setStakeholderOutcomeScore(average(
                educators.isEmpty() ? 0 : 100,
                partnerOrganizations.isEmpty() ? 0 : 100,
                governmentOrganizations.isEmpty() ? 0 : 100,
                scoreFromPercentage(relationshipFollowUpCompletionRate)
        ));
        evaluation.setOperationsOutcomeScore(average(
                staffActionsLast30Days > 0 ? 100 : 0,
                platformEventsLast30Days > 0 ? 100 : 0,
                activeRelationshipNotes > 0 ? 100 : 0
        ));
        evaluation.setOverallScore(average(
                evaluation.getYouthOutcomeScore(),
                evaluation.getProgramOutcomeScore(),
                evaluation.getCredentialOutcomeScore(),
                evaluation.getServiceOutcomeScore(),
                evaluation.getStakeholderOutcomeScore(),
                evaluation.getOperationsOutcomeScore()
        ));
        evaluation.setOverallStatus(statusForScore(evaluation.getOverallScore()));
        addNarrative(evaluation);
        return evaluation;
    }

    private void addNarrative(PilotEvaluationDTO evaluation) {
        addCategoryNarrative(evaluation, "Youth outcomes", evaluation.getYouthOutcomeScore(),
                "Youth onboarding and activity are meeting pilot expectations.",
                "Youth onboarding, activity, or retention are below pilot expectations.",
                "Review youth onboarding, profile completion, and re-engagement steps.");
        addCategoryNarrative(evaluation, "Program outcomes", evaluation.getProgramOutcomeScore(),
                "Program participation and attendance indicators are strong.",
                "Program participation or attendance indicators need attention.",
                "Review active enrollments and attendance recording practices.");
        addCategoryNarrative(evaluation, "Credential outcomes", evaluation.getCredentialOutcomeScore(),
                "Credential participation is supporting the pilot learning model.",
                "Credential participation is below the desired pilot level.",
                "Review credential definitions, program alignment, and award workflows.");
        addCategoryNarrative(evaluation, "Service outcomes", evaluation.getServiceOutcomeScore(),
                "Service-hour participation is producing measurable approved activity.",
                "Service-hour activity is limited or not yet approved.",
                "Encourage service-hour submission and staff review follow-through.");
        addCategoryNarrative(evaluation, "Stakeholder outcomes", evaluation.getStakeholderOutcomeScore(),
                "External stakeholder relationships are supporting pilot operations.",
                "Stakeholder coverage or follow-up completion needs attention.",
                "Review educator, partner, government, and relationship follow-up records.");
        addCategoryNarrative(evaluation, "Operations outcomes", evaluation.getOperationsOutcomeScore(),
                "Staff and platform activity are visible in operational records.",
                "Operational activity data is limited.",
                "Confirm staff actions and platform events are being tracked consistently.");
    }

    private void addCategoryNarrative(
            PilotEvaluationDTO evaluation,
            String category,
            int score,
            String strength,
            String concern,
            String action
    ) {
        if (score >= 80) {
            evaluation.getStrengths().add(strength);
        } else if (score < 60) {
            evaluation.getConcerns().add(concern);
            evaluation.getRecommendedActions().add(action);
        } else {
            evaluation.getConcerns().add(category + " are in the yellow range and should be monitored.");
            evaluation.getRecommendedActions().add(action);
        }
    }

    private int scoreFromPercentage(double value) {
        if (value >= 80.0) {
            return 100;
        }
        if (value >= 60.0) {
            return 70;
        }
        return (int) Math.round(Math.max(0.0, value));
    }

    private int cappedTargetScore(double value, double target) {
        if (target <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round((value * 100.0) / target));
    }

    private int average(int... values) {
        if (values.length == 0) {
            return 0;
        }
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return Math.round(total / (float) values.length);
    }

    private String statusForScore(int score) {
        if (score >= 80) {
            return "green";
        }
        if (score >= 60) {
            return "yellow";
        }
        return "red";
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

    private int countPlatformEvents(List<PlatformEvent> events, Date now, int days) {
        long cutoff = now.getTime() - days * DAY_MILLIS;
        return (int) events.stream()
                .filter(event -> event.getEventTimestamp() != null)
                .filter(event -> event.getEventTimestamp().getTime() >= cutoff)
                .count();
    }

    private int countStaffOperations(List<StaffOperationEvent> events, Date now, int days) {
        long cutoff = now.getTime() - days * DAY_MILLIS;
        return (int) events.stream()
                .filter(event -> event.getCreatedAt() != null)
                .filter(event -> event.getCreatedAt().getTime() >= cutoff)
                .count();
    }

    private int countFollowUps(List<StakeholderRelationshipNote> notes, Date now, boolean overdue) {
        return (int) notes.stream()
                .filter(StakeholderRelationshipNote::isActive)
                .filter(note -> note.getNextFollowUpDate() != null)
                .filter(note -> overdue ? note.getNextFollowUpDate().before(now) : !note.getNextFollowUpDate().before(now))
                .count();
    }

    private boolean isActiveEnrollment(ProgramEnrollment enrollment) {
        return "active".equalsIgnoreCase(enrollment.getEnrollmentStatus());
    }

    private boolean isPresentAttendance(AttendanceRecord attendanceRecord) {
        return "present".equalsIgnoreCase(attendanceRecord.getAttendanceStatus());
    }

    private boolean isApprovedServiceHour(ServiceHourRecord serviceHourRecord) {
        return "verified".equalsIgnoreCase(serviceHourRecord.getVerificationStatus());
    }

    private double percentage(int numerator, int denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return round((numerator * 100.0) / denominator);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
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
