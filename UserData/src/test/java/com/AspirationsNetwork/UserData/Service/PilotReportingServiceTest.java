package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PilotReportingDTO;
import com.AspirationsNetwork.UserData.Models.CredentialDefinition;
import com.AspirationsNetwork.UserData.Models.EarnedCredential;
import com.AspirationsNetwork.UserData.Models.PlatformEvent;
import com.AspirationsNetwork.UserData.Models.Program;
import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PilotReportingServiceTest {
    private static final Date NOW = new Date(TimeUnit.DAYS.toMillis(120));

    @Test
    void buildReportCalculatesParticipationActiveUsersRetentionCredentialsAndPrograms() {
        User activeYouth = youth("uid-1", "ASPN-2026-0001", "active");
        User pendingYouth = youth("uid-2", "ASPN-2026-0002", "pending_onboarding");
        User staffUser = new User();
        staffUser.setUid("staff-1");
        staffUser.setYouthProfile(false);
        staffUser.setRole("staff");

        Program bell = program("program-bell", "Bell Youth Council", "Civic Leadership", "active");
        Program umsl = program("program-umsl", "UMSL", "College", "active");
        ProgramEnrollment enrollment = enrollment("uid-1", "program-bell", "active");
        ProgramEnrollment removedEnrollment = enrollment("uid-2", "program-umsl", "removed");

        CredentialDefinition attendanceDefinition = credentialDefinition(
                "credential-attendance",
                "Attendance",
                List.of("program-bell")
        );
        CredentialDefinition rwdDefinition = credentialDefinition(
                "credential-rwd",
                "RWD",
                List.of("program-umsl")
        );
        EarnedCredential earnedAttendance = earnedCredential("uid-1", "credential-attendance");
        EarnedCredential earnedRwd = earnedCredential("uid-2", "credential-rwd");

        List<PlatformEvent> events = List.of(
                event("uid-1", "ASPN-2026-0001", 0),
                event("uid-1", "ASPN-2026-0001", 35),
                event("uid-1", "ASPN-2026-0001", 70),
                event("uid-1", "ASPN-2026-0001", 100),
                event("uid-2", "ASPN-2026-0002", 115)
        );

        PilotReportingService service = new PilotReportingService(mock(Firestore.class));
        PilotReportingDTO report = service.buildReport(
                List.of(activeYouth, pendingYouth, staffUser),
                List.of(bell, umsl),
                List.of(enrollment, removedEnrollment),
                List.of(earnedAttendance, earnedRwd),
                Map.of(
                        "credential-attendance", attendanceDefinition,
                        "credential-rwd", rwdDefinition
                ),
                events,
                NOW
        );

        assertEquals(2, report.getParticipation().getTotalRegisteredYouth());
        assertEquals(1, report.getParticipation().getProfileCompletedYouth());
        assertEquals(50.0, report.getParticipation().getProfileCompletionPercentage());
        assertEquals(1, report.getParticipation().getProgramParticipants());
        assertEquals(50.0, report.getParticipation().getProgramParticipationPercentage());
        assertEquals(2, report.getParticipation().getCredentialParticipants());
        assertEquals(100.0, report.getParticipation().getCredentialParticipationPercentage());

        assertEquals(2, report.getActiveUsers().getActiveUsersLast30Days());
        assertEquals(2, report.getActiveUsers().getActiveUsersLast60Days());
        assertEquals(2, report.getActiveUsers().getActiveUsersLast90Days());

        assertEquals(2, report.getRetention().getRetentionEligibleParticipants());
        assertEquals(1, report.getRetention().getRetained30DayParticipants());
        assertEquals(50.0, report.getRetention().getRetention30DayPercentage());
        assertEquals(1, report.getRetention().getRetained60DayParticipants());
        assertEquals(50.0, report.getRetention().getRetention60DayPercentage());
        assertEquals(1, report.getRetention().getRetained90DayParticipants());
        assertEquals(50.0, report.getRetention().getRetention90DayPercentage());

        assertEquals(2, report.getCredentials().getTotalCredentialsEarned());
        assertEquals(1, report.getCredentials().getCredentialsByCategory().get("Attendance"));
        assertEquals(1, report.getCredentials().getCredentialsByCategory().get("RWD"));

        PilotReportingDTO.ProgramReportingDTO bellReport = report.getPrograms().stream()
                .filter(program -> "program-bell".equals(program.getProgramId()))
                .findFirst()
                .orElseThrow();
        assertEquals(1, bellReport.getRegistrations());
        assertEquals(1, bellReport.getCredentialCompletions());
        assertEquals(1, bellReport.getActiveParticipants());

        PilotReportingDTO.ProgramReportingDTO umslReport = report.getPrograms().stream()
                .filter(program -> "program-umsl".equals(program.getProgramId()))
                .findFirst()
                .orElseThrow();
        assertEquals(0, umslReport.getRegistrations());
        assertEquals(1, umslReport.getCredentialCompletions());
        assertEquals(0, umslReport.getActiveParticipants());
    }

    @Test
    void buildReportReturnsZeroPercentagesWhenThereAreNoYouthOrEvents() {
        PilotReportingService service = new PilotReportingService(mock(Firestore.class));

        PilotReportingDTO report = service.buildReport(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Map.of(),
                List.of(),
                NOW
        );

        assertEquals(0, report.getParticipation().getTotalRegisteredYouth());
        assertEquals(0.0, report.getParticipation().getProfileCompletionPercentage());
        assertEquals(0, report.getRetention().getRetentionEligibleParticipants());
        assertEquals(0.0, report.getRetention().getRetention30DayPercentage());
    }

    private User youth(String uid, String aspnParticipantId, String profileStatus) {
        User user = new User();
        user.setUid(uid);
        user.setAspnParticipantId(aspnParticipantId);
        user.setYouthProfile(true);
        user.setRole("member");
        user.setProfileStatus(profileStatus);
        return user;
    }

    private Program program(String programId, String name, String category, String status) {
        Program program = new Program();
        program.setProgramId(programId);
        program.setProgramName(name);
        program.setCategory(category);
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

    private CredentialDefinition credentialDefinition(String credentialID, String category, List<String> programIds) {
        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID(credentialID);
        definition.setCategory(category);
        definition.setProgramIds(programIds);
        return definition;
    }

    private EarnedCredential earnedCredential(String userUID, String credentialID) {
        EarnedCredential credential = new EarnedCredential();
        credential.setUserUID(userUID);
        credential.setCredentialID(credentialID);
        return credential;
    }

    private PlatformEvent event(String userUID, String aspnParticipantId, int day) {
        PlatformEvent event = new PlatformEvent();
        event.setUserUID(userUID);
        event.setAspnParticipantId(aspnParticipantId);
        event.setEventTimestamp(new Date(TimeUnit.DAYS.toMillis(day)));
        return event;
    }
}
