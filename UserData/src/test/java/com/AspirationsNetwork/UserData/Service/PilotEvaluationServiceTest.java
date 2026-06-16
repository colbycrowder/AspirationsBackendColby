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
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PilotEvaluationServiceTest {
    private static final Date NOW = new Date(TimeUnit.DAYS.toMillis(120));

    @Test
    void buildEvaluationReturnsGreenForStrongPilotPerformance() {
        PilotEvaluationService service = new PilotEvaluationService(mock(Firestore.class));

        PilotEvaluationDTO evaluation = service.buildEvaluation(
                youthUsers(80, 72),
                enrollments(64),
                List.of(activeCredentialDefinition()),
                earnedCredentials(52),
                attendanceRecords(80, 70),
                serviceHourRecords(50, 100.0),
                List.of(new Educator()),
                List.of(new PartnerOrganization()),
                List.of(new GovernmentOrganization()),
                List.of(relationshipNote(1), relationshipNote(2)),
                platformEvents(64, 115),
                List.of(staffOperation(115)),
                NOW
        );

        assertEquals("green", evaluation.getOverallStatus());
        assertTrue(evaluation.getOverallScore() >= 80);
        assertEquals(80, evaluation.getRegistrations());
        assertEquals(90.0, evaluation.getProfileCompletionRate());
        assertEquals(80.0, evaluation.getParticipationRate());
        assertTrue(evaluation.getStrengths().contains("Youth onboarding and activity are meeting pilot expectations."));
        assertFalse(evaluation.getRecommendedActions().isEmpty());
    }

    @Test
    void buildEvaluationReturnsYellowForMixedPilotPerformance() {
        PilotEvaluationService service = new PilotEvaluationService(mock(Firestore.class));

        PilotEvaluationDTO evaluation = service.buildEvaluation(
                youthUsers(75, 60),
                enrollments(55),
                List.of(activeCredentialDefinition()),
                earnedCredentials(45),
                attendanceRecords(70, 48),
                serviceHourRecords(40, 35.0),
                List.of(new Educator()),
                List.of(),
                List.of(),
                List.of(relationshipNote(1), relationshipNote(-1)),
                platformEvents(45, 115),
                List.of(),
                NOW
        );

        assertEquals("yellow", evaluation.getOverallStatus());
        assertTrue(evaluation.getOverallScore() >= 60);
        assertTrue(evaluation.getOverallScore() < 80);
        assertFalse(evaluation.getConcerns().isEmpty());
        assertFalse(evaluation.getRecommendedActions().isEmpty());
    }

    @Test
    void buildEvaluationReturnsRedForLimitedPilotPerformance() {
        PilotEvaluationService service = new PilotEvaluationService(mock(Firestore.class));

        PilotEvaluationDTO evaluation = service.buildEvaluation(
                youthUsers(20, 5),
                enrollments(2),
                List.of(),
                earnedCredentials(1),
                attendanceRecords(5, 1),
                serviceHourRecords(0, 0.0),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                NOW
        );

        assertEquals("red", evaluation.getOverallStatus());
        assertTrue(evaluation.getOverallScore() < 60);
        assertTrue(evaluation.getConcerns().contains("Youth onboarding, activity, or retention are below pilot expectations."));
        assertTrue(evaluation.getRecommendedActions().contains("Review youth onboarding, profile completion, and re-engagement steps."));
    }

    private List<User> youthUsers(int count, int activeCount) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> {
                    User user = new User();
                    user.setUid("youth-" + index);
                    user.setYouthProfile(true);
                    user.setProfileStatus(index < activeCount ? "active" : "pending_onboarding");
                    return user;
                })
                .toList();
    }

    private List<ProgramEnrollment> enrollments(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> {
                    ProgramEnrollment enrollment = new ProgramEnrollment();
                    enrollment.setUserUID("youth-" + index);
                    enrollment.setProgramId("program-1");
                    enrollment.setEnrollmentStatus("active");
                    return enrollment;
                })
                .toList();
    }

    private CredentialDefinition activeCredentialDefinition() {
        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID("credential-1");
        definition.setActive(true);
        return definition;
    }

    private List<EarnedCredential> earnedCredentials(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> {
                    EarnedCredential credential = new EarnedCredential();
                    credential.setUserUID("youth-" + index);
                    credential.setCredentialID("credential-1");
                    return credential;
                })
                .toList();
    }

    private List<AttendanceRecord> attendanceRecords(int count, int presentCount) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setAttendanceStatus(index < presentCount ? "present" : "absent");
                    return record;
                })
                .toList();
    }

    private List<ServiceHourRecord> serviceHourRecords(int participants, double totalHours) {
        double hoursPerParticipant = participants == 0 ? 0.0 : totalHours / participants;
        return java.util.stream.IntStream.range(0, participants)
                .mapToObj(index -> {
                    ServiceHourRecord record = new ServiceHourRecord();
                    record.setUserUID("youth-" + index);
                    record.setVerificationStatus("verified");
                    record.setHours(hoursPerParticipant);
                    return record;
                })
                .toList();
    }

    private StakeholderRelationshipNote relationshipNote(int daysFromNow) {
        StakeholderRelationshipNote note = new StakeholderRelationshipNote();
        note.setActive(true);
        note.setNextFollowUpDate(new Date(NOW.getTime() + TimeUnit.DAYS.toMillis(daysFromNow)));
        return note;
    }

    private List<PlatformEvent> platformEvents(int participants, int day) {
        return java.util.stream.IntStream.range(0, participants)
                .mapToObj(index -> {
                    PlatformEvent event = new PlatformEvent();
                    event.setAspnParticipantId("ASPN-2026-" + String.format("%04d", index));
                    event.setEventTimestamp(new Date(TimeUnit.DAYS.toMillis(day)));
                    return event;
                })
                .toList();
    }

    private StaffOperationEvent staffOperation(int day) {
        StaffOperationEvent event = new StaffOperationEvent();
        event.setCreatedAt(new Date(TimeUnit.DAYS.toMillis(day)));
        return event;
    }
}
