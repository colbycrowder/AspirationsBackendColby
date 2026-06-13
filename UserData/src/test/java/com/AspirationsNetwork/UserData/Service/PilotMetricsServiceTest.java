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
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PilotMetricsServiceTest {
    private static final Date NOW = new Date(TimeUnit.DAYS.toMillis(120));

    @Test
    void buildMetricsAggregatesPilotDataAcrossCollections() {
        PilotMetricsService service = new PilotMetricsService(mock(Firestore.class));

        PilotMetricsDTO metrics = service.buildMetrics(
                List.of(youth("youth-1", "active"), youth("youth-2", "pending_onboarding"), staffUser()),
                List.of(program("program-a", "active"), program("program-b", "archived")),
                List.of(
                        enrollment("youth-1", "program-a", "active"),
                        enrollment("youth-2", "program-a", "active"),
                        enrollment("youth-3", "program-b", "removed")
                ),
                List.of(
                        credentialDefinition("credential-a", "Attendance", true, List.of("program-a")),
                        credentialDefinition("credential-b", "RWD", false, List.of("program-b"))
                ),
                List.of(
                        earnedCredential("credential-a"),
                        earnedCredential("credential-b")
                ),
                List.of(
                        attendance("present"),
                        attendance("absent")
                ),
                List.of(
                        serviceHour("program-a", "verified", 3.5),
                        serviceHour("program-a", "pending", 2.0)
                ),
                List.of(educator(), educator()),
                List.of(partner()),
                List.of(government()),
                List.of(
                        relationshipNote(true, 1),
                        relationshipNote(true, -1),
                        relationshipNote(false, -2)
                ),
                List.of(
                        platformEvent("ASPN-2026-0001", 115),
                        platformEvent("ASPN-2026-0002", 70),
                        platformEvent("ASPN-2026-0003", 40)
                ),
                List.of(
                        staffOperation(115),
                        staffOperation(75),
                        staffOperation(40)
                ),
                NOW
        );

        assertEquals(2, metrics.getTotalRegistrations());
        assertEquals(1, metrics.getActiveUsers());
        assertEquals(1, metrics.getProfileCompletions());
        assertEquals(50.0, metrics.getProfileCompletionRate());
        assertEquals(1, metrics.getActiveLast30Days());
        assertEquals(2, metrics.getActiveLast60Days());
        assertEquals(3, metrics.getActiveLast90Days());

        assertEquals(2, metrics.getTotalPrograms());
        assertEquals(1, metrics.getActivePrograms());
        assertEquals(3, metrics.getTotalEnrollments());
        assertEquals(2, metrics.getActiveParticipants());
        assertEquals(2, metrics.getAttendanceRecords());
        assertEquals(50.0, metrics.getAttendanceRate());
        assertEquals(2, metrics.getProgramParticipationCounts().get("program-a"));

        assertEquals(2, metrics.getCredentialDefinitions());
        assertEquals(1, metrics.getActiveCredentialDefinitions());
        assertEquals(2, metrics.getCredentialsAwarded());
        assertEquals(1, metrics.getCredentialsByCategory().get("Attendance"));
        assertEquals(1, metrics.getCredentialsByCategory().get("RWD"));
        assertEquals(1, metrics.getCredentialsByProgram().get("program-a"));
        assertEquals(1, metrics.getCredentialsByProgram().get("program-b"));

        assertEquals(2, metrics.getServiceHourSubmissions());
        assertEquals(1, metrics.getApprovedServiceHourSubmissions());
        assertEquals(3.5, metrics.getTotalApprovedServiceHours());
        assertEquals(3.5, metrics.getServiceHoursByProgram().get("program-a"));

        assertEquals(2, metrics.getEducators());
        assertEquals(1, metrics.getPartnerOrganizations());
        assertEquals(1, metrics.getGovernmentOrganizations());
        assertEquals(3, metrics.getRelationshipNotes());
        assertEquals(2, metrics.getActiveRelationshipNotes());
        assertEquals(1, metrics.getUpcomingFollowUps());
        assertEquals(1, metrics.getOverdueFollowUps());

        assertEquals(1, metrics.getStaffOperationsLast30Days());
        assertEquals(2, metrics.getStaffOperationsLast60Days());
        assertEquals(3, metrics.getStaffOperationsLast90Days());
    }

    @Test
    void buildMetricsReturnsZeroValuesForEmptyCollections() {
        PilotMetricsService service = new PilotMetricsService(mock(Firestore.class));

        PilotMetricsDTO metrics = service.buildMetrics(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                NOW
        );

        assertEquals(0, metrics.getTotalRegistrations());
        assertEquals(0.0, metrics.getProfileCompletionRate());
        assertEquals(0.0, metrics.getAttendanceRate());
        assertEquals(0, metrics.getStaffOperationsLast90Days());
    }

    private User youth(String uid, String profileStatus) {
        User user = new User();
        user.setUid(uid);
        user.setYouthProfile(true);
        user.setProfileStatus(profileStatus);
        return user;
    }

    private User staffUser() {
        User user = new User();
        user.setUid("staff-1");
        user.setYouthProfile(false);
        user.setRole("staff");
        return user;
    }

    private Program program(String programId, String status) {
        Program program = new Program();
        program.setProgramId(programId);
        program.setProgramStatus(status);
        return program;
    }

    private ProgramEnrollment enrollment(String userUID, String programId, String status) {
        ProgramEnrollment enrollment = new ProgramEnrollment();
        enrollment.setUserUID(userUID);
        enrollment.setProgramId(programId);
        enrollment.setEnrollmentStatus(status);
        return enrollment;
    }

    private CredentialDefinition credentialDefinition(String credentialId, String category, boolean active, List<String> programIds) {
        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID(credentialId);
        definition.setCategory(category);
        definition.setActive(active);
        definition.setProgramIds(programIds);
        return definition;
    }

    private EarnedCredential earnedCredential(String credentialId) {
        EarnedCredential credential = new EarnedCredential();
        credential.setCredentialID(credentialId);
        return credential;
    }

    private AttendanceRecord attendance(String status) {
        AttendanceRecord attendance = new AttendanceRecord();
        attendance.setAttendanceStatus(status);
        return attendance;
    }

    private ServiceHourRecord serviceHour(String programId, String verificationStatus, double hours) {
        ServiceHourRecord record = new ServiceHourRecord();
        record.setProgramId(programId);
        record.setVerificationStatus(verificationStatus);
        record.setHours(hours);
        return record;
    }

    private Educator educator() {
        return new Educator();
    }

    private PartnerOrganization partner() {
        return new PartnerOrganization();
    }

    private GovernmentOrganization government() {
        return new GovernmentOrganization();
    }

    private StakeholderRelationshipNote relationshipNote(boolean active, int daysFromNow) {
        StakeholderRelationshipNote note = new StakeholderRelationshipNote();
        note.setActive(active);
        note.setNextFollowUpDate(new Date(NOW.getTime() + TimeUnit.DAYS.toMillis(daysFromNow)));
        return note;
    }

    private PlatformEvent platformEvent(String aspnParticipantId, int day) {
        PlatformEvent event = new PlatformEvent();
        event.setAspnParticipantId(aspnParticipantId);
        event.setEventTimestamp(new Date(TimeUnit.DAYS.toMillis(day)));
        return event;
    }

    private StaffOperationEvent staffOperation(int day) {
        StaffOperationEvent event = new StaffOperationEvent();
        event.setCreatedAt(new Date(TimeUnit.DAYS.toMillis(day)));
        return event;
    }
}
