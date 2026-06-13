package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PilotReadinessDTO;
import com.AspirationsNetwork.UserData.Models.AttendanceRecord;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.Educator;
import com.AspirationsNetwork.UserData.Models.GovernmentOrganization;
import com.AspirationsNetwork.UserData.Models.PartnerOrganization;
import com.AspirationsNetwork.UserData.Models.PlatformEvent;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ServiceHourRecord;
import com.AspirationsNetwork.UserData.Models.StakeholderRelationshipNote;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PilotReadinessService {
    private static final double PROFILE_COMPLETION_TARGET = 80.0;
    private static final long THIRTY_DAYS_MILLIS = TimeUnit.DAYS.toMillis(30);

    private final Firestore firestore;

    public PilotReadinessDTO getPilotReadiness() throws Exception {
        Date now = new Date();
        return buildReadiness(
                getCollectionObjects(UserInfoService.COLLECTION_NAME, User.class),
                getCollectionObjects(ProgramService.COLLECTION_NAME, Program.class),
                getCollectionObjects(CredentialService.CREDENTIAL_DEFINITIONS_COLLECTION, CredentialDefinition.class),
                getCollectionObjects(AttendanceService.COLLECTION_NAME, AttendanceRecord.class),
                getCollectionObjects(ServiceHourService.COLLECTION_NAME, ServiceHourRecord.class),
                getCollectionObjects(EducatorService.COLLECTION_NAME, Educator.class),
                getCollectionObjects(PartnerOrganizationService.COLLECTION_NAME, PartnerOrganization.class),
                getCollectionObjects(GovernmentOrganizationService.COLLECTION_NAME, GovernmentOrganization.class),
                getCollectionObjects(StakeholderRelationshipNoteService.COLLECTION_NAME, StakeholderRelationshipNote.class),
                getCollectionObjects(PlatformEventService.COLLECTION_NAME, PlatformEvent.class),
                now
        );
    }

    PilotReadinessDTO buildReadiness(
            List<User> users,
            List<Program> programs,
            List<CredentialDefinition> credentialDefinitions,
            List<AttendanceRecord> attendanceRecords,
            List<ServiceHourRecord> serviceHourRecords,
            List<Educator> educators,
            List<PartnerOrganization> partnerOrganizations,
            List<GovernmentOrganization> governmentOrganizations,
            List<StakeholderRelationshipNote> stakeholderRelationshipNotes,
            List<PlatformEvent> platformEvents,
            Date now
    ) {
        PilotReadinessDTO readiness = new PilotReadinessDTO();
        readiness.setGeneratedAt(now);

        int totalYouthUsers = (int) users.stream().filter(User::isYouthProfile).count();
        int activeYouthUsers = (int) users.stream()
                .filter(User::isYouthProfile)
                .filter(user -> "active".equalsIgnoreCase(user.getProfileStatus()))
                .count();
        int completedProfiles = activeYouthUsers;
        double profileCompletionRate = percentage(completedProfiles, totalYouthUsers);
        int activePrograms = (int) programs.stream().filter(this::isActiveProgram).count();
        int activeCredentialDefinitions = (int) credentialDefinitions.stream().filter(CredentialDefinition::isActive).count();
        int activeEducators = (int) educators.stream().filter(Educator::isActive).count();
        int activePartnerOrganizations = (int) partnerOrganizations.stream().filter(PartnerOrganization::isActive).count();
        int activeGovernmentOrganizations = (int) governmentOrganizations.stream().filter(GovernmentOrganization::isActive).count();
        int activeStakeholderRelationshipNotes = (int) stakeholderRelationshipNotes.stream()
                .filter(StakeholderRelationshipNote::isActive)
                .count();
        int platformEventsLast30Days = countPlatformEventsLast30Days(platformEvents, now);

        readiness.setTotalYouthUsers(totalYouthUsers);
        readiness.setActiveYouthUsers(activeYouthUsers);
        readiness.setCompletedProfiles(completedProfiles);
        readiness.setProfileCompletionRate(profileCompletionRate);
        readiness.setActivePrograms(activePrograms);
        readiness.setActiveCredentialDefinitions(activeCredentialDefinitions);
        readiness.setAttendanceRecords(attendanceRecords.size());
        readiness.setServiceHourRecords(serviceHourRecords.size());
        readiness.setActiveEducators(activeEducators);
        readiness.setActivePartnerOrganizations(activePartnerOrganizations);
        readiness.setActiveGovernmentOrganizations(activeGovernmentOrganizations);
        readiness.setActiveStakeholderRelationshipNotes(activeStakeholderRelationshipNotes);
        readiness.setPlatformEventsLast30Days(platformEventsLast30Days);

        addChecklistItem(readiness, "youth onboarding", "Youth profiles exist and profile completion is on target",
                totalYouthUsers > 0 && profileCompletionRate >= PROFILE_COMPLETION_TARGET,
                completedProfiles + " of " + totalYouthUsers + " youth profiles completed");
        addChecklistItem(readiness, "programs", "At least one active program is available",
                activePrograms > 0,
                activePrograms + " active program(s)");
        addChecklistItem(readiness, "credentials", "At least one active credential definition is available",
                activeCredentialDefinitions > 0,
                activeCredentialDefinitions + " active credential definition(s)");
        addChecklistItem(readiness, "attendance", "Attendance records exist for pilot operations",
                !attendanceRecords.isEmpty(),
                attendanceRecords.size() + " attendance record(s)");
        addChecklistItem(readiness, "service hours", "Service-hour records exist for pilot operations",
                !serviceHourRecords.isEmpty(),
                serviceHourRecords.size() + " service-hour record(s)");
        addChecklistItem(readiness, "educators", "Active educator relationships exist",
                activeEducators > 0,
                activeEducators + " active educator relationship(s)");
        addChecklistItem(readiness, "partners", "Active partner organization relationships exist",
                activePartnerOrganizations > 0,
                activePartnerOrganizations + " active partner organization(s)");
        addChecklistItem(readiness, "government organizations", "Active government organization relationships exist",
                activeGovernmentOrganizations > 0,
                activeGovernmentOrganizations + " active government organization(s)");
        addChecklistItem(readiness, "stakeholder relationships", "Active stakeholder relationship notes exist",
                activeStakeholderRelationshipNotes > 0,
                activeStakeholderRelationshipNotes + " active relationship note(s)");
        addChecklistItem(readiness, "analytics/activity", "Recent platform activity is being tracked",
                platformEventsLast30Days > 0,
                platformEventsLast30Days + " platform event(s) in the last 30 days");

        addBlockersAndWarnings(readiness);
        int score = calculateScore(readiness);
        readiness.setReadinessScore(score);
        readiness.setReadinessStatus(statusForScore(score));
        return readiness;
    }

    private void addBlockersAndWarnings(PilotReadinessDTO readiness) {
        if (readiness.getTotalYouthUsers() == 0) {
            readiness.getBlockers().add("No youth users exist.");
        }
        if (readiness.getActivePrograms() == 0) {
            readiness.getBlockers().add("No active programs exist.");
        }
        if (readiness.getActiveCredentialDefinitions() == 0) {
            readiness.getBlockers().add("No active credential definitions exist.");
        }

        if (readiness.getProfileCompletionRate() < PROFILE_COMPLETION_TARGET) {
            readiness.getWarnings().add("Profile completion is below the 80% pilot target.");
        }
        if (readiness.getAttendanceRecords() == 0) {
            readiness.getWarnings().add("No attendance records exist yet.");
        }
        if (readiness.getServiceHourRecords() == 0) {
            readiness.getWarnings().add("No service-hour records exist yet.");
        }
        if (readiness.getPlatformEventsLast30Days() == 0) {
            readiness.getWarnings().add("No platform events were recorded in the last 30 days.");
        }
        if (readiness.getActiveEducators() == 0) {
            readiness.getWarnings().add("No active educator relationships exist.");
        }
        if (readiness.getActivePartnerOrganizations() == 0) {
            readiness.getWarnings().add("No active partner organization relationships exist.");
        }
        if (readiness.getActiveGovernmentOrganizations() == 0) {
            readiness.getWarnings().add("No active government organization relationships exist.");
        }
        if (readiness.getActiveStakeholderRelationshipNotes() == 0) {
            readiness.getWarnings().add("No active stakeholder relationship notes exist.");
        }
    }

    private int calculateScore(PilotReadinessDTO readiness) {
        long completeItems = readiness.getChecklistItems().stream()
                .filter(PilotReadinessDTO.ChecklistItemDTO::isComplete)
                .count();
        if (readiness.getChecklistItems().isEmpty()) {
            return 0;
        }
        return (int) Math.round((completeItems * 100.0) / readiness.getChecklistItems().size());
    }

    private String statusForScore(int score) {
        if (score >= 80) {
            return "ready";
        }
        if (score >= 60) {
            return "caution";
        }
        return "not_ready";
    }

    private void addChecklistItem(
            PilotReadinessDTO readiness,
            String category,
            String label,
            boolean complete,
            String detail
    ) {
        PilotReadinessDTO.ChecklistItemDTO item = new PilotReadinessDTO.ChecklistItemDTO();
        item.setCategory(category);
        item.setLabel(label);
        item.setComplete(complete);
        item.setDetail(detail);
        readiness.getChecklistItems().add(item);
    }

    private int countPlatformEventsLast30Days(List<PlatformEvent> events, Date now) {
        long cutoff = now.getTime() - THIRTY_DAYS_MILLIS;
        return (int) events.stream()
                .filter(event -> event.getEventTimestamp() != null)
                .filter(event -> event.getEventTimestamp().getTime() >= cutoff)
                .count();
    }

    private boolean isActiveProgram(Program program) {
        return "active".equalsIgnoreCase(program.getProgramStatus());
    }

    private double percentage(int numerator, int denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return Math.round((numerator * 10000.0) / denominator) / 100.0;
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
