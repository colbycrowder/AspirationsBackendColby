package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PilotReportingDTO;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.AspirationsNetwork.UserData.Models.PlatformEvent;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PilotReportingService {
    private static final long DAY_MILLIS = TimeUnit.DAYS.toMillis(1);

    private final Firestore firestore;

    public PilotReportingDTO getPilotReportingMetrics() throws Exception {
        List<User> users = getCollectionObjects(UserInfoService.COLLECTION_NAME, User.class);
        List<Program> programs = getCollectionObjects(ProgramService.COLLECTION_NAME, Program.class);
        List<ProgramEnrollment> enrollments = getCollectionObjects(
                ProgramEnrollmentService.COLLECTION_NAME,
                ProgramEnrollment.class
        );
        List<EarnedCredential> earnedCredentials = getCollectionObjects(
                CredentialService.EARNED_CREDENTIALS_COLLECTION,
                EarnedCredential.class
        );
        Map<String, CredentialDefinition> definitionsById = getCollectionObjects(
                CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION,
                CredentialDefinition.class
        ).stream()
                .filter(definition -> definition.getCredentialID() != null)
                .collect(Collectors.toMap(
                        CredentialDefinition::getCredentialID,
                        definition -> definition,
                        (existing, replacement) -> existing
                ));
        List<PlatformEvent> events = getCollectionObjects(PlatformEventService.COLLECTION_NAME, PlatformEvent.class);

        return buildReport(users, programs, enrollments, earnedCredentials, definitionsById, events, new Date());
    }

    PilotReportingDTO buildReport(
            List<User> users,
            List<Program> programs,
            List<ProgramEnrollment> enrollments,
            List<EarnedCredential> earnedCredentials,
            Map<String, CredentialDefinition> definitionsById,
            List<PlatformEvent> events,
            Date now
    ) {
        List<User> youthUsers = users.stream()
                .filter(User::isYouthProfile)
                .toList();
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

        PilotReportingDTO report = new PilotReportingDTO();
        setParticipationMetrics(report, youthUsers, activeEnrollmentUsers, credentialUsers);
        setActiveUserMetrics(report, events, now);
        setRetentionMetrics(report, events);
        setCredentialMetrics(report, earnedCredentials, definitionsById);
        report.setPrograms(buildProgramReports(programs, enrollments, earnedCredentials, definitionsById, events, now));
        return report;
    }

    private void setParticipationMetrics(
            PilotReportingDTO report,
            List<User> youthUsers,
            Set<String> activeEnrollmentUsers,
            Set<String> credentialUsers
    ) {
        int totalYouth = youthUsers.size();
        int completedProfiles = (int) youthUsers.stream()
                .filter(user -> "active".equalsIgnoreCase(user.getProfileStatus()))
                .count();

        PilotReportingDTO.ParticipationMetricsDTO participation = report.getParticipation();
        participation.setTotalRegisteredYouth(totalYouth);
        participation.setProfileCompletedYouth(completedProfiles);
        participation.setProfileCompletionPercentage(percentage(completedProfiles, totalYouth));
        participation.setProgramParticipants(activeEnrollmentUsers.size());
        participation.setProgramParticipationPercentage(percentage(activeEnrollmentUsers.size(), totalYouth));
        participation.setCredentialParticipants(credentialUsers.size());
        participation.setCredentialParticipationPercentage(percentage(credentialUsers.size(), totalYouth));
    }

    private void setActiveUserMetrics(PilotReportingDTO report, List<PlatformEvent> events, Date now) {
        PilotReportingDTO.ActiveUserMetricsDTO activeUsers = report.getActiveUsers();
        activeUsers.setActiveUsersLast30Days(countActiveParticipants(events, now, 30));
        activeUsers.setActiveUsersLast60Days(countActiveParticipants(events, now, 60));
        activeUsers.setActiveUsersLast90Days(countActiveParticipants(events, now, 90));
    }

    private void setRetentionMetrics(PilotReportingDTO report, List<PlatformEvent> events) {
        Map<String, List<PlatformEvent>> eventsByParticipant = eventsByParticipant(events);
        int eligible = eventsByParticipant.size();

        PilotReportingDTO.RetentionMetricsDTO retention = report.getRetention();
        retention.setRetentionEligibleParticipants(eligible);
        retention.setRetained30DayParticipants(countRetainedParticipants(eventsByParticipant, 30));
        retention.setRetention30DayPercentage(percentage(retention.getRetained30DayParticipants(), eligible));
        retention.setRetained60DayParticipants(countRetainedParticipants(eventsByParticipant, 60));
        retention.setRetention60DayPercentage(percentage(retention.getRetained60DayParticipants(), eligible));
        retention.setRetained90DayParticipants(countRetainedParticipants(eventsByParticipant, 90));
        retention.setRetention90DayPercentage(percentage(retention.getRetained90DayParticipants(), eligible));
    }

    private void setCredentialMetrics(
            PilotReportingDTO report,
            List<EarnedCredential> earnedCredentials,
            Map<String, CredentialDefinition> definitionsById
    ) {
        PilotReportingDTO.CredentialMetricsDTO credentialMetrics = report.getCredentials();
        credentialMetrics.setTotalCredentialsEarned(earnedCredentials.size());

        Map<String, Integer> byCategory = new HashMap<>();
        for (EarnedCredential earnedCredential : earnedCredentials) {
            CredentialDefinition definition = definitionsById.get(earnedCredential.getCredentialID());
            String category = definition == null || !hasText(definition.getCategory())
                    ? "Uncategorized"
                    : definition.getCategory();
            byCategory.merge(category, 1, Integer::sum);
        }
        credentialMetrics.setCredentialsByCategory(byCategory);
    }

    private List<PilotReportingDTO.ProgramReportingDTO> buildProgramReports(
            List<Program> programs,
            List<ProgramEnrollment> enrollments,
            List<EarnedCredential> earnedCredentials,
            Map<String, CredentialDefinition> definitionsById,
            List<PlatformEvent> events,
            Date now
    ) {
        Set<String> activeUserUids = activeUserUids(events, now, 30);
        List<PilotReportingDTO.ProgramReportingDTO> reports = new ArrayList<>();

        for (Program program : programs) {
            if (!hasText(program.getProgramId())) {
                continue;
            }

            Set<String> enrolledUsers = enrollments.stream()
                    .filter(this::isActiveEnrollment)
                    .filter(enrollment -> program.getProgramId().equals(enrollment.getProgramId()))
                    .map(ProgramEnrollment::getUserUID)
                    .filter(this::hasText)
                    .collect(Collectors.toSet());

            int credentialCompletions = (int) earnedCredentials.stream()
                    .filter(earnedCredential -> credentialAppliesToProgram(
                            earnedCredential,
                            program.getProgramId(),
                            definitionsById
                    ))
                    .count();

            PilotReportingDTO.ProgramReportingDTO programReport = new PilotReportingDTO.ProgramReportingDTO();
            programReport.setProgramId(program.getProgramId());
            programReport.setProgramName(program.getProgramName());
            programReport.setCategory(program.getCategory());
            programReport.setProgramStatus(program.getProgramStatus());
            programReport.setRegistrations(enrolledUsers.size());
            programReport.setCredentialCompletions(credentialCompletions);
            programReport.setActiveParticipants((int) enrolledUsers.stream()
                    .filter(activeUserUids::contains)
                    .count());
            reports.add(programReport);
        }

        reports.sort(Comparator.comparing(
                PilotReportingDTO.ProgramReportingDTO::getProgramName,
                Comparator.nullsLast(String::compareToIgnoreCase)
        ));
        return reports;
    }

    private int countActiveParticipants(List<PlatformEvent> events, Date now, int days) {
        return activeParticipantIds(events, now, days).size();
    }

    private Set<String> activeParticipantIds(List<PlatformEvent> events, Date now, int days) {
        long cutoff = now.getTime() - days * DAY_MILLIS;
        return events.stream()
                .filter(event -> hasText(event.getAspnParticipantId()))
                .filter(event -> event.getEventTimestamp() != null)
                .filter(event -> event.getEventTimestamp().getTime() >= cutoff)
                .map(PlatformEvent::getAspnParticipantId)
                .collect(Collectors.toSet());
    }

    private Set<String> activeUserUids(List<PlatformEvent> events, Date now, int days) {
        long cutoff = now.getTime() - days * DAY_MILLIS;
        return events.stream()
                .filter(event -> hasText(event.getUserUID()))
                .filter(event -> event.getEventTimestamp() != null)
                .filter(event -> event.getEventTimestamp().getTime() >= cutoff)
                .map(PlatformEvent::getUserUID)
                .collect(Collectors.toSet());
    }

    private Map<String, List<PlatformEvent>> eventsByParticipant(List<PlatformEvent> events) {
        return events.stream()
                .filter(event -> hasText(event.getAspnParticipantId()))
                .filter(event -> event.getEventTimestamp() != null)
                .collect(Collectors.groupingBy(PlatformEvent::getAspnParticipantId));
    }

    private int countRetainedParticipants(Map<String, List<PlatformEvent>> eventsByParticipant, int days) {
        long requiredAge = days * DAY_MILLIS;
        int retained = 0;

        for (List<PlatformEvent> participantEvents : eventsByParticipant.values()) {
            long firstEvent = participantEvents.stream()
                    .map(PlatformEvent::getEventTimestamp)
                    .mapToLong(Date::getTime)
                    .min()
                    .orElse(0L);

            boolean hasLaterEvent = participantEvents.stream()
                    .map(PlatformEvent::getEventTimestamp)
                    .mapToLong(Date::getTime)
                    .anyMatch(timestamp -> timestamp >= firstEvent + requiredAge);
            if (hasLaterEvent) {
                retained++;
            }
        }
        return retained;
    }

    private boolean credentialAppliesToProgram(
            EarnedCredential earnedCredential,
            String programId,
            Map<String, CredentialDefinition> definitionsById
    ) {
        CredentialDefinition definition = definitionsById.get(earnedCredential.getCredentialID());
        return definition != null
                && definition.getProgramIds() != null
                && definition.getProgramIds().contains(programId);
    }

    private boolean isActiveEnrollment(ProgramEnrollment enrollment) {
        return enrollment != null && "active".equalsIgnoreCase(enrollment.getEnrollmentStatus());
    }

    private double percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return Math.round((numerator * 10000.0 / denominator)) / 100.0;
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
