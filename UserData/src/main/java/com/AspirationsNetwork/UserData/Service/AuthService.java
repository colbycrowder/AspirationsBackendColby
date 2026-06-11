package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Set<String> STAFF_ROLES = Set.of("staff", "admin");

    private final FirebaseAuth firebaseAuth;
    private final UserInfoService userInfoService;

    public String requireAuthenticatedUserUid(String authorizationHeader) {
        return verifyFirebaseToken(authorizationHeader).getUid();
    }

    public String requireStaff(String authorizationHeader) throws Exception {
        FirebaseToken token = verifyFirebaseToken(authorizationHeader);
        User user = userInfoService.getUser(token.getUid());

        if (user == null) {
            throw new ForbiddenAccessException("User profile is required for staff access");
        }

        String role = user.getRole();
        if (role == null || !STAFF_ROLES.contains(role.toLowerCase())) {
            throw new ForbiddenAccessException("Staff role is required");
        }

        return token.getUid();
    }

    private FirebaseToken verifyFirebaseToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedAccessException("Authorization bearer token is required");
        }

        String idToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (idToken.isEmpty()) {
            throw new UnauthorizedAccessException("Authorization bearer token is required");
        }

        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new UnauthorizedAccessException("Invalid Firebase ID token");
        }
    }
}
