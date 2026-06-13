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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PilotReadinessServiceTest {

    @Test
    void buildReadinessReturnsReadyWhenPilotOperationsArePrepared() {
        Date now = new Date();
        PilotReadinessService service = new PilotReadinessService(mock(Firestore.class));

        PilotReadinessDTO readiness = service.buildReadiness(
                List.of(youth("youth-1", "active"), youth("youth-2", "active")),
                List.of(program("active")),
                List.of(credentialDefinition(true)),
                List.of(new AttendanceRecord()),
                List.of(new ServiceHourRecord()),
                List.of(educator(true)),
                List.of(partner(true)),
                List.of(government(true)),
                List.of(relationshipNote(true)),
                List.of(platformEvent(now)),
                now
        );

        assertEquals(100, readiness.getReadinessScore());
        assertEquals("ready", readiness.getReadinessStatus());
        assertEquals(2, readiness.getTotalYouthUsers());
        assertEquals(100.0, readiness.getProfileCompletionRate());
        assertTrue(readiness.getBlockers().isEmpty());
        assertTrue(readiness.getWarnings().isEmpty());
        assertEquals(10, readiness.getChecklistItems().size());
    }

    @Test
    void buildReadinessReturnsReadyAtEightyWithWarningsForIncompleteOperations() {
        Date now = new Date();
        PilotReadinessService service = new PilotReadinessService(mock(Firestore.class));

        PilotReadinessDTO readiness = service.buildReadiness(
                List.of(youth("youth-1", "active"), youth("youth-2", "active")),
                List.of(program("active")),
                List.of(credentialDefinition(true)),
                List.of(new AttendanceRecord()),
                List.of(new ServiceHourRecord()),
                List.of(educator(true)),
                List.of(partner(true)),
                List.of(),
                List.of(),
                List.of(platformEvent(now)),
                now
        );

        assertEquals(80, readiness.getReadinessScore());
        assertEquals("ready", readiness.getReadinessStatus());
        assertTrue(readiness.getWarnings().contains("No active government organization relationships exist."));
        assertTrue(readiness.getWarnings().contains("No active stakeholder relationship notes exist."));
    }

    @Test
    void buildReadinessReturnsCautionWhenScoreIsBetweenSixtyAndSeventyNine() {
        Date now = new Date();
        PilotReadinessService service = new PilotReadinessService(mock(Firestore.class));

        PilotReadinessDTO readiness = service.buildReadiness(
                List.of(youth("youth-1", "active"), youth("youth-2", "pending_onboarding")),
                List.of(program("active")),
                List.of(credentialDefinition(true)),
                List.of(new AttendanceRecord()),
                List.of(new ServiceHourRecord()),
                List.of(educator(true)),
                List.of(partner(true)),
                List.of(),
                List.of(),
                List.of(platformEvent(now)),
                now
        );

        assertEquals(70, readiness.getReadinessScore());
        assertEquals("caution", readiness.getReadinessStatus());
        assertTrue(readiness.getWarnings().contains("Profile completion is below the 80% pilot target."));
    }

    @Test
    void buildReadinessReturnsNotReadyAndBlockersWhenCoreSetupIsMissing() {
        PilotReadinessService service = new PilotReadinessService(mock(Firestore.class));

        PilotReadinessDTO readiness = service.buildReadiness(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new Date()
        );

        assertEquals(0, readiness.getReadinessScore());
        assertEquals("not_ready", readiness.getReadinessStatus());
        assertTrue(readiness.getBlockers().contains("No youth users exist."));
        assertTrue(readiness.getBlockers().contains("No active programs exist."));
        assertTrue(readiness.getBlockers().contains("No active credential definitions exist."));
        assertFalse(readiness.getWarnings().isEmpty());
    }

    private User youth(String uid, String profileStatus) {
        User user = new User();
        user.setUid(uid);
        user.setYouthProfile(true);
        user.setProfileStatus(profileStatus);
        return user;
    }

    private Program program(String status) {
        Program program = new Program();
        program.setProgramStatus(status);
        return program;
    }

    private CredentialDefinition credentialDefinition(boolean active) {
        CredentialDefinition credentialDefinition = new CredentialDefinition();
        credentialDefinition.setActive(active);
        return credentialDefinition;
    }

    private Educator educator(boolean active) {
        Educator educator = new Educator();
        educator.setActive(active);
        return educator;
    }

    private PartnerOrganization partner(boolean active) {
        PartnerOrganization partner = new PartnerOrganization();
        partner.setActive(active);
        return partner;
    }

    private GovernmentOrganization government(boolean active) {
        GovernmentOrganization government = new GovernmentOrganization();
        government.setActive(active);
        return government;
    }

    private StakeholderRelationshipNote relationshipNote(boolean active) {
        StakeholderRelationshipNote note = new StakeholderRelationshipNote();
        note.setActive(active);
        return note;
    }

    private PlatformEvent platformEvent(Date timestamp) {
        PlatformEvent event = new PlatformEvent();
        event.setEventTimestamp(timestamp);
        return event;
    }
}
