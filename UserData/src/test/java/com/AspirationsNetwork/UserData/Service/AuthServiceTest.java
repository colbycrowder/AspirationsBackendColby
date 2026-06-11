package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void requireAuthenticatedUserUidReturnsVerifiedTokenUid() throws Exception {
        FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
        FirebaseToken firebaseToken = mock(FirebaseToken.class);
        AuthService authService = new AuthService(firebaseAuth, mock(UserInfoService.class));

        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn("youth-123");

        assertEquals("youth-123", authService.requireAuthenticatedUserUid("Bearer valid-token"));
    }

    @Test
    void requireStaffReturnsUidForVerifiedStaffUser() throws Exception {
        FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
        FirebaseToken firebaseToken = mock(FirebaseToken.class);
        UserInfoService userInfoService = mock(UserInfoService.class);
        User staffUser = new User();
        staffUser.setUid("staff-123");
        staffUser.setRole("staff");

        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn("staff-123");
        when(userInfoService.getUser("staff-123")).thenReturn(staffUser);

        AuthService authService = new AuthService(firebaseAuth, userInfoService);

        assertEquals("staff-123", authService.requireStaff("Bearer valid-token"));
    }

    @Test
    void requireStaffRejectsNonStaffUser() throws Exception {
        FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
        FirebaseToken firebaseToken = mock(FirebaseToken.class);
        UserInfoService userInfoService = mock(UserInfoService.class);
        User youthUser = new User();
        youthUser.setUid("youth-123");
        youthUser.setRole("member");

        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(firebaseToken);
        when(firebaseToken.getUid()).thenReturn("youth-123");
        when(userInfoService.getUser("youth-123")).thenReturn(youthUser);

        AuthService authService = new AuthService(firebaseAuth, userInfoService);

        ForbiddenAccessException exception = assertThrows(
                ForbiddenAccessException.class,
                () -> authService.requireStaff("Bearer valid-token")
        );

        assertEquals("Staff role is required", exception.getMessage());
    }

    @Test
    void requireStaffRejectsMissingBearerToken() {
        AuthService authService = new AuthService(mock(FirebaseAuth.class), mock(UserInfoService.class));

        UnauthorizedAccessException exception = assertThrows(
                UnauthorizedAccessException.class,
                () -> authService.requireStaff(null)
        );

        assertEquals("Authorization bearer token is required", exception.getMessage());
    }
}
