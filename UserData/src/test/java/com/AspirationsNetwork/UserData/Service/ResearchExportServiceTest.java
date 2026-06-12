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
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ResearchExportServiceTest {

    @Test
    void participantExportUsesAspnParticipantIdAndOmitsFirebaseUidAndPersonalNames() {
        ResearchExportService service = new ResearchExportService(mock(Firestore.class));

        ResearchExportDTO export = service.buildParticipantsExport(List.of(youthUser(), staffUser()));

        assertEquals(ResearchExportService.PARTICIPANTS_EXPORT, export.getExportType());
        assertEquals(1, export.getRecordCount());
        Map<String, Object> row = export.getRecords().get(0);
        assertEquals("ASPN-2026-0001", row.get("aspnParticipantId"));
        assertEquals("active", row.get("profileStatus"));
        assertFalse(row.containsKey("uid"));
        assertFalse(row.containsKey("userUID"));
        assertFalse(row.containsKey("email"));
        assertFalse(row.containsKey("firstName"));
        assertFalse(row.containsKey("lastName"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void platformEventsExportUsesExistingParticipantIdAndOmitsFirebaseUid() {
        ResearchExportService service = new ResearchExportService(mock(Firestore.class));
        PlatformEvent event = new PlatformEvent();
        event.setEventId("event-123");
        event.setUserUID("firebase-youth-1");
        event.setAspnParticipantId("ASPN-2026-0001");
        event.setEventType("DASHBOARD_VIEW");
        event.setEventTimestamp(new Date());
        event.setMetadata(Map.of("source", "dashboard", "userUID", "firebase-youth-1"));

        ResearchExportDTO export = service.buildPlatformEventsExport(List.of(event));

        assertEquals(1, export.getRecordCount());
        Map<String, Object> row = export.getRecords().get(0);
        assertEquals("ASPN-2026-0001", row.get("aspnParticipantId"));
        assertEquals("DASHBOARD_VIEW", row.get("eventType"));
        assertFalse(row.containsKey("userUID"));
        Map<String, Object> metadata = (Map<String, Object>) row.get("metadata");
        assertEquals("dashboard", metadata.get("source"));
        assertFalse(metadata.containsKey("userUID"));
    }

    @Test
    void credentialsExportJoinsDefinitionDataByAspnParticipantId() {
        ResearchExportService service = new ResearchExportService(mock(Firestore.class));
        EarnedCredential earned = new EarnedCredential();
        earned.setEarnedCredentialID("earned-123");
        earned.setCredentialID("credential-123");
        earned.setUserUID("firebase-youth-1");
        earned.setAwardedByStaffUID("staff-123");
        earned.setStatus("awarded");

        CredentialDefinition definition = new CredentialDefinition();
        definition.setCredentialID("credential-123");
        definition.setCredentialName("RWD Completion");
        definition.setCategory("RWD");
        definition.setActive(true);

        ResearchExportDTO export = service.buildCredentialsExport(
                List.of(earned),
                Map.of("credential-123", definition),
                Map.of("firebase-youth-1", "ASPN-2026-0001")
        );

        Map<String, Object> row = export.getRecords().get(0);
        assertEquals("ASPN-2026-0001", row.get("aspnParticipantId"));
        assertEquals("RWD Completion", row.get("credentialName"));
        assertEquals("RWD", row.get("credentialCategory"));
        assertFalse(row.containsKey("userUID"));
        assertFalse(row.containsKey("awardedByStaffUID"));
    }

    @Test
    void programParticipationExportJoinsProgramDataByAspnParticipantId() {
        ResearchExportService service = new ResearchExportService(mock(Firestore.class));
        ProgramEnrollment enrollment = new ProgramEnrollment();
        enrollment.setEnrollmentId("enrollment-123");
        enrollment.setUserUID("firebase-youth-1");
        enrollment.setProgramId("program-123");
        enrollment.setEnrollmentStatus("active");
        enrollment.setCreatedByUser(true);

        Program program = new Program();
        program.setProgramId("program-123");
        program.setProgramName("Bell Youth Council");
        program.setCategory("Civic Leadership");
        program.setProgramStatus("active");

        ResearchExportDTO export = service.buildProgramParticipationExport(
                List.of(enrollment),
                Map.of("program-123", program),
                Map.of("firebase-youth-1", "ASPN-2026-0001")
        );

        Map<String, Object> row = export.getRecords().get(0);
        assertEquals("ASPN-2026-0001", row.get("aspnParticipantId"));
        assertEquals("Bell Youth Council", row.get("programName"));
        assertFalse(row.containsKey("userUID"));
        assertFalse(row.containsKey("removedByStaffUID"));
    }

    @Test
    void attendanceServiceHoursAndRwdExportsOmitOperationalUids() {
        ResearchExportService service = new ResearchExportService(mock(Firestore.class));
        Map<String, String> participantIdsByUserUid = Map.of("firebase-youth-1", "ASPN-2026-0001");

        AttendanceRecord attendance = new AttendanceRecord();
        attendance.setAttendanceRecordID("attendance-123");
        attendance.setUserUID("firebase-youth-1");
        attendance.setStaffRecorderUID("staff-123");
        attendance.setProgramID("program-123");
        attendance.setAttendanceStatus("present");

        ServiceHourRecord serviceHour = new ServiceHourRecord();
        serviceHour.setServiceHourRecordId("service-123");
        serviceHour.setUserUID("firebase-youth-1");
        serviceHour.setReviewedByStaffUID("staff-123");
        serviceHour.setProgramId("program-123");
        serviceHour.setHours(2.5);
        serviceHour.setGoogleFormResponseUrl("https://forms.example/response");

        RwdProgress progress = new RwdProgress();
        progress.setProgressId("progress-123");
        progress.setUserUID("firebase-youth-1");
        progress.setRwdActivityId("rwd-123");
        progress.setCompletionStatus("completed");
        progress.setPassed(true);

        RwdActivity activity = new RwdActivity();
        activity.setRwdActivityId("rwd-123");
        activity.setCountryName("Kenya");
        activity.setTitle("Kenya Movement Map");

        Map<String, Object> attendanceRow = service.buildAttendanceExport(
                List.of(attendance),
                participantIdsByUserUid
        ).getRecords().get(0);
        Map<String, Object> serviceHourRow = service.buildServiceHoursExport(
                List.of(serviceHour),
                participantIdsByUserUid
        ).getRecords().get(0);
        Map<String, Object> rwdRow = service.buildRwdProgressExport(
                List.of(progress),
                Map.of("rwd-123", activity),
                participantIdsByUserUid
        ).getRecords().get(0);

        assertEquals("ASPN-2026-0001", attendanceRow.get("aspnParticipantId"));
        assertFalse(attendanceRow.containsKey("userUID"));
        assertFalse(attendanceRow.containsKey("staffRecorderUID"));
        assertEquals(true, serviceHourRow.get("hasGoogleFormResponseUrl"));
        assertFalse(serviceHourRow.containsKey("userUID"));
        assertFalse(serviceHourRow.containsKey("reviewedByStaffUID"));
        assertFalse(serviceHourRow.containsKey("googleFormResponseUrl"));
        assertEquals("Kenya", rwdRow.get("countryName"));
        assertFalse(rwdRow.containsKey("userUID"));
    }

    @Test
    void externalLinksExportOmitsUserAndStaffUids() {
        ResearchExportService service = new ResearchExportService(mock(Firestore.class));
        ParticipantExternalLink link = new ParticipantExternalLink();
        link.setLinkId("link-123");
        link.setAspnParticipantId("ASPN-2026-0001");
        link.setUserUID("firebase-youth-1");
        link.setLinkedByStaffUID("staff-123");
        link.setRemovedByStaffUID("staff-456");
        link.setExternalDatasetId("dataset-123");
        link.setExternalDatasetName("Fall Survey");
        link.setExternalSource("google_forms");
        link.setExternalRecordId("response-123");
        link.setLinkStatus("active");
        link.setNotes("Matched by staff");

        ResearchExportDTO export = service.buildExternalLinksExport(List.of(link));

        Map<String, Object> row = export.getRecords().get(0);
        assertEquals("ASPN-2026-0001", row.get("aspnParticipantId"));
        assertEquals("response-123", row.get("externalRecordId"));
        assertFalse(row.containsKey("userUID"));
        assertFalse(row.containsKey("linkedByStaffUID"));
        assertFalse(row.containsKey("removedByStaffUID"));
        assertFalse(row.containsKey("notes"));
        assertEquals(true, row.get("hasNotes"));
    }

    @Test
    void exportsSkipOperationalRecordsWithoutKnownAspnParticipantId() {
        ResearchExportService service = new ResearchExportService(mock(Firestore.class));
        EarnedCredential earned = new EarnedCredential();
        earned.setUserUID("missing-user");
        earned.setCredentialID("credential-123");

        ResearchExportDTO export = service.buildCredentialsExport(List.of(earned), Map.of(), Map.of());

        assertEquals(0, export.getRecordCount());
        assertTrue(export.getRecords().isEmpty());
    }

    @Test
    void unsupportedExportTypeIsRejected() {
        ResearchExportService service = new ResearchExportService(mock(Firestore.class));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getResearchExport("unknown_export")
        );

        assertEquals("Unsupported research export type", exception.getMessage());
    }

    private User youthUser() {
        User user = new User();
        user.setUid("firebase-youth-1");
        user.setFirstName("Youth");
        user.setLastName("Participant");
        user.setEmail("youth@example.com");
        user.setAspnParticipantId("ASPN-2026-0001");
        user.setAspnParticipantCohortYear("2026");
        user.setYouthProfile(true);
        user.setProfileStatus("active");
        user.setSchool("Pilot High School");
        user.setGraduationYear("2027");
        user.setExternalConsentReceived(true);
        return user;
    }

    private User staffUser() {
        User user = new User();
        user.setUid("staff-1");
        user.setYouthProfile(false);
        user.setRole("staff");
        return user;
    }
}
