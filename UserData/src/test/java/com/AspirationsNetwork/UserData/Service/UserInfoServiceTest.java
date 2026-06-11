package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.UserProfileCreationDTO;
import com.AspirationsNetwork.UserData.DTO.StaffUserUpdateDTO;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(usersCollection.document("uid-123")).thenReturn(userDocument);
        when(userDocument.set(any(User.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        UserProfileCreationDTO dto = new UserProfileCreationDTO();
        dto.setUid("uid-123");
        dto.setFirstName("Ari");
        dto.setLastName("Student");
        dto.setEmail("ari@example.com");

        UserInfoService service = new UserInfoService(firestore);
        String createdUid = service.createUserDetails(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDocument).set(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("uid-123", createdUid);
        assertEquals("uid-123", savedUser.getUid());
        assertEquals("member", savedUser.getAccountType());
        assertEquals("member", savedUser.getRole());
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

        UserInfoService service = new UserInfoService(firestore);
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
    }
}
