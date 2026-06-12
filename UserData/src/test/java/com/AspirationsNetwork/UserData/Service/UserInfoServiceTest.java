package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.UserProfileCreationDTO;
import com.AspirationsNetwork.UserData.DTO.StaffUserUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.UserTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.YouthProfileCompletionDTO;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserInfoServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createUserDetailsStoresPrivateYouthProfileDefaults() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ParticipantIdService participantIdService = mock(ParticipantIdService.class);
        Date assignedAt = new Date();

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.document("uid-123")).thenReturn(userDocument);
        when(userDocument.set(any(User.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(participantIdService.generateParticipantId()).thenReturn(new ParticipantIdService.ParticipantIdAssignment(
                "ASPN-2026-0001",
                assignedAt,
                "system",
                "2026"
        ));

        UserProfileCreationDTO dto = new UserProfileCreationDTO();
        dto.setUid("uid-123");
        dto.setFirstName("Ari");
        dto.setLastName("Student");
        dto.setEmail("ari@example.com");

        UserInfoService service = new UserInfoService(firestore, participantIdService);
        String createdUid = service.createUserDetails(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDocument).set(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("uid-123", createdUid);
        assertEquals("uid-123", savedUser.getUid());
        assertEquals("member", savedUser.getAccountType());
        assertEquals("member", savedUser.getRole());
        assertEquals("ASPN-2026-0001", savedUser.getAspnParticipantId());
        assertEquals(assignedAt, savedUser.getAspnParticipantIdAssignedAt());
        assertEquals("system", savedUser.getAspnParticipantIdAssignedBy());
        assertEquals("2026", savedUser.getAspnParticipantCohortYear());
        assertFalse(savedUser.isPublicProfile());
        assertTrue(savedUser.isYouthProfile());
        assertEquals("pending_onboarding", savedUser.getProfileStatus());
        assertNotNull(savedUser.getCollegeInterests());
        assertNotNull(savedUser.getCareerInterests());
        assertNotNull(savedUser.getDesiredCareerFields());
        assertNotNull(savedUser.getGovernmentCareerInterests());
        assertNotNull(savedUser.getWorkforceInterests());
        assertNotNull(savedUser.getCivicInterests());
        assertNotNull(savedUser.getCommunityInterests());
        assertNotNull(savedUser.getPublicServiceInterests());
        assertNotNull(savedUser.getProgramIds());
        assertNotNull(savedUser.getProgramParticipationIds());
        assertNotNull(savedUser.getCredentialIds());
        assertNotNull(savedUser.getEarnedCredentialIds());
        assertNotNull(savedUser.getAttendanceRecordIds());
        assertNotNull(savedUser.getServiceHourRecordIds());
        assertTrue(savedUser.getCollegeInterests().isEmpty());
        assertTrue(savedUser.getCareerInterests().isEmpty());
        assertTrue(savedUser.getDesiredCareerFields().isEmpty());
        assertTrue(savedUser.getGovernmentCareerInterests().isEmpty());
        assertTrue(savedUser.getWorkforceInterests().isEmpty());
        assertTrue(savedUser.getCivicInterests().isEmpty());
        assertTrue(savedUser.getCommunityInterests().isEmpty());
        assertTrue(savedUser.getPublicServiceInterests().isEmpty());
        assertTrue(savedUser.getProgramIds().isEmpty());
        assertTrue(savedUser.getProgramParticipationIds().isEmpty());
        assertTrue(savedUser.getCredentialIds().isEmpty());
        assertTrue(savedUser.getEarnedCredentialIds().isEmpty());
        assertTrue(savedUser.getAttendanceRecordIds().isEmpty());
        assertTrue(savedUser.getServiceHourRecordIds().isEmpty());
        assertTrue(savedUser.isStaffReviewRequired());
        assertFalse(savedUser.isStaffVerified());
        assertFalse(savedUser.isExternalConsentReceived());
        assertFalse(savedUser.isCredentialReviewAccess());
        assertFalse(savedUser.isAttendanceReviewAccess());
        assertFalse(savedUser.isServiceHourVerificationAccess());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateYouthUserForStaffOnlyUpdatesAllowedStaffManagedFields() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        ParticipantIdService participantIdService = mock(ParticipantIdService.class);
        User youthUser = new User();
        youthUser.setUid("uid-123");
        youthUser.setYouthProfile(true);

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.document("uid-123")).thenReturn(userDocument);
        when(userDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(youthUser);
        when(userDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        StaffUserUpdateDTO dto = new StaffUserUpdateDTO();
        dto.setProfileStatus("active");
        dto.setProgramIds(List.of("program-1"));
        dto.setStaffReviewRequired(false);
        dto.setStaffVerified(true);

        UserInfoService service = new UserInfoService(firestore, participantIdService);
        service.updateYouthUserForStaff("uid-123", dto);

        ArgumentCaptor<Map<String, Object>> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(userDocument).update(updateCaptor.capture());

        Map<String, Object> updates = updateCaptor.getValue();
        assertEquals("active", updates.get("profileStatus"));
        assertEquals(List.of("program-1"), updates.get("programIds"));
        assertEquals(List.of("program-1"), updates.get("programParticipationIds"));
        assertEquals(false, updates.get("staffReviewRequired"));
        assertEquals(true, updates.get("staffVerified"));
        assertFalse(updates.containsKey("role"));
        assertFalse(updates.containsKey("publicProfile"));
        assertFalse(updates.containsKey("aspnParticipantId"));
        verify(participantIdService, never()).generateParticipantId();
    }

    @Test
    @SuppressWarnings("unchecked")
    void completeYouthProfileCreatesNewYouthProfileWithParticipantId() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentSnapshot missingSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);
        ParticipantIdService participantIdService = mock(ParticipantIdService.class);
        Date assignedAt = new Date();

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.document("uid-new")).thenReturn(userDocument);
        when(userDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(missingSnapshot);
        when(missingSnapshot.exists()).thenReturn(false);
        when(userDocument.set(any(User.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(participantIdService.generateParticipantId()).thenReturn(new ParticipantIdService.ParticipantIdAssignment(
                "ASPN-2026-0002",
                assignedAt,
                "system",
                "2026"
        ));

        YouthProfileCompletionDTO dto = new YouthProfileCompletionDTO();
        dto.setFirstName("New");
        dto.setLastName("Student");
        dto.setEmail("new@example.com");

        UserInfoService service = new UserInfoService(firestore, participantIdService);
        User user = service.completeYouthProfile("uid-new", dto);

        assertEquals("uid-new", user.getUid());
        assertEquals("ASPN-2026-0002", user.getAspnParticipantId());
        assertEquals(assignedAt, user.getAspnParticipantIdAssignedAt());
        assertEquals("system", user.getAspnParticipantIdAssignedBy());
        assertEquals("2026", user.getAspnParticipantCohortYear());
        assertTrue(user.isYouthProfile());
        assertEquals("member", user.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDocument).set(userCaptor.capture());
        assertEquals("ASPN-2026-0002", userCaptor.getValue().getAspnParticipantId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void completeYouthProfileBackfillsMissingParticipantIdForExistingYouthProfile() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);
        ParticipantIdService participantIdService = mock(ParticipantIdService.class);
        Date assignedAt = new Date();
        User youthUser = new User();
        youthUser.setUid("uid-existing");
        youthUser.setYouthProfile(true);
        youthUser.setRole("member");

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.document("uid-existing")).thenReturn(userDocument);
        when(userDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(youthUser);
        when(userDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));
        when(participantIdService.generateParticipantId()).thenReturn(new ParticipantIdService.ParticipantIdAssignment(
                "ASPN-2026-0003",
                assignedAt,
                "system",
                "2026"
        ));

        YouthProfileCompletionDTO dto = new YouthProfileCompletionDTO();
        dto.setSchool("ASPN High");

        UserInfoService service = new UserInfoService(firestore, participantIdService);
        service.completeYouthProfile("uid-existing", dto);

        ArgumentCaptor<Map<String, Object>> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(userDocument).update(updateCaptor.capture());

        Map<String, Object> updates = updateCaptor.getValue();
        assertEquals("ASPN-2026-0003", updates.get("aspnParticipantId"));
        assertEquals(assignedAt, updates.get("aspnParticipantIdAssignedAt"));
        assertEquals("system", updates.get("aspnParticipantIdAssignedBy"));
        assertEquals("2026", updates.get("aspnParticipantCohortYear"));
        assertEquals("ASPN High", updates.get("school"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void completeYouthProfileDoesNotAssignParticipantIdToStaffProfile() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ParticipantIdService participantIdService = mock(ParticipantIdService.class);
        User staffUser = new User();
        staffUser.setUid("staff-uid");
        staffUser.setYouthProfile(false);
        staffUser.setRole("staff");

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.document("staff-uid")).thenReturn(userDocument);
        when(userDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(staffUser);

        YouthProfileCompletionDTO dto = new YouthProfileCompletionDTO();

        UserInfoService service = new UserInfoService(firestore, participantIdService);
        assertThrows(ForbiddenAccessException.class, () -> service.completeYouthProfile("staff-uid", dto));

        verify(participantIdService, never()).generateParticipantId();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUsersForStaffFiltersByRoleActiveYouthProfileAndProgram() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        QueryDocumentSnapshot matchingDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot inactiveDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot staffDocument = mock(QueryDocumentSnapshot.class);
        User matchingUser = staffUser("youth-1", "member", true, "active");
        matchingUser.setProgramIds(List.of("program-123"));
        User inactiveUser = staffUser("youth-2", "member", true, "inactive");
        inactiveUser.setProgramIds(List.of("program-123"));
        User staffUser = staffUser("staff-1", "staff", false, "active");

        stubUserCollection(usersCollection, List.of(matchingDocument, inactiveDocument, staffDocument));
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(matchingDocument.toObject(User.class)).thenReturn(matchingUser);
        when(inactiveDocument.toObject(User.class)).thenReturn(inactiveUser);
        when(staffDocument.toObject(User.class)).thenReturn(staffUser);

        UserInfoService service = new UserInfoService(firestore, mock(ParticipantIdService.class));
        List<User> users = service.getUsersForStaff("member", true, true, "program-123");

        assertEquals(1, users.size());
        assertEquals("youth-1", users.get(0).getUid());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getUserTotalsForStaffCountsStatusesAndRoles() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        QueryDocumentSnapshot youthDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot staffDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot educatorDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot partnerDocument = mock(QueryDocumentSnapshot.class);
        QueryDocumentSnapshot governmentDocument = mock(QueryDocumentSnapshot.class);

        stubUserCollection(usersCollection, List.of(
                youthDocument,
                staffDocument,
                educatorDocument,
                partnerDocument,
                governmentDocument
        ));
        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(youthDocument.toObject(User.class)).thenReturn(staffUser("youth-1", "member", true, "active"));
        when(staffDocument.toObject(User.class)).thenReturn(staffUser("staff-1", "staff", false, "active"));
        when(educatorDocument.toObject(User.class)).thenReturn(staffUser("educator-1", "educator", false, "inactive"));
        when(partnerDocument.toObject(User.class)).thenReturn(staffUser("partner-1", "partner", false, "inactive"));
        when(governmentDocument.toObject(User.class)).thenReturn(staffUser("government-1", "government", false, "active"));

        UserInfoService service = new UserInfoService(firestore, mock(ParticipantIdService.class));
        UserTotalsDTO totals = service.getUserTotalsForStaff();

        assertEquals(5, totals.getTotalUsers());
        assertEquals(3, totals.getActiveUsers());
        assertEquals(2, totals.getInactiveUsers());
        assertEquals(1, totals.getYouthUsers());
        assertEquals(1, totals.getStaffUsers());
        assertEquals(1, totals.getEducatorUsers());
        assertEquals(1, totals.getPartnerUsers());
        assertEquals(1, totals.getGovernmentUsers());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateUserForStaffUpdatesAllowedFieldsWithoutChangingUidOrRole() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> updateFuture = mock(ApiFuture.class);

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.document("uid-123")).thenReturn(userDocument);
        when(userDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(staffUser("uid-123", "member", true, "pending_onboarding"));
        when(userDocument.update(any(Map.class))).thenReturn(updateFuture);
        when(updateFuture.get()).thenReturn(mock(WriteResult.class));

        StaffUserUpdateDTO dto = new StaffUserUpdateDTO();
        dto.setProfileStatus("active");
        dto.setProgramIds(List.of("program-1"));
        dto.setStaffReviewRequired(false);
        dto.setStaffVerified(true);

        UserInfoService service = new UserInfoService(firestore, mock(ParticipantIdService.class));
        service.updateUserForStaff("uid-123", dto);

        ArgumentCaptor<Map<String, Object>> updateCaptor = ArgumentCaptor.forClass(Map.class);
        verify(userDocument).update(updateCaptor.capture());

        Map<String, Object> updates = updateCaptor.getValue();
        assertEquals("active", updates.get("profileStatus"));
        assertEquals(List.of("program-1"), updates.get("programIds"));
        assertEquals(false, updates.get("staffReviewRequired"));
        assertEquals(true, updates.get("staffVerified"));
        assertFalse(updates.containsKey("uid"));
        assertFalse(updates.containsKey("role"));
        assertFalse(updates.containsKey("aspnParticipantId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void activateAndDeactivateUserForStaffSetProfileStatus() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CollectionReference usersCollection = mock(CollectionReference.class);
        DocumentReference userDocument = mock(DocumentReference.class);
        DocumentSnapshot userSnapshot = mock(DocumentSnapshot.class);
        ApiFuture<DocumentSnapshot> readFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> activeFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> inactiveFuture = mock(ApiFuture.class);

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.document("uid-123")).thenReturn(userDocument);
        when(userDocument.get()).thenReturn(readFuture);
        when(readFuture.get()).thenReturn(userSnapshot);
        when(userSnapshot.exists()).thenReturn(true);
        when(userSnapshot.toObject(User.class)).thenReturn(staffUser("uid-123", "member", true, "pending_onboarding"));
        when(userDocument.update(eq("profileStatus"), eq("active"))).thenReturn(activeFuture);
        when(userDocument.update(eq("profileStatus"), eq("inactive"))).thenReturn(inactiveFuture);
        when(activeFuture.get()).thenReturn(mock(WriteResult.class));
        when(inactiveFuture.get()).thenReturn(mock(WriteResult.class));

        UserInfoService service = new UserInfoService(firestore, mock(ParticipantIdService.class));
        service.activateUserForStaff("uid-123");
        service.deactivateUserForStaff("uid-123");

        verify(userDocument).update(eq("profileStatus"), eq("active"));
        verify(userDocument).update(eq("profileStatus"), eq("inactive"));
    }

    @SuppressWarnings("unchecked")
    private void stubUserCollection(CollectionReference usersCollection, List<QueryDocumentSnapshot> documents)
            throws Exception {
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        when(usersCollection.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
        when(snapshot.getDocuments()).thenReturn(documents);
    }

    private User staffUser(String uid, String role, boolean youthProfile, String profileStatus) {
        User user = new User();
        user.setUid(uid);
        user.setRole(role);
        user.setYouthProfile(youthProfile);
        user.setProfileStatus(profileStatus);
        return user;
    }
}
