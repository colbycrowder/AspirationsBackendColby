package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.StaffUserUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileCreationDTO;
import com.AspirationsNetwork.UserData.DTO.UserTotalsDTO;
import com.AspirationsNetwork.UserData.DTO.YouthProfileCompletionDTO;
import com.AspirationsNetwork.UserData.Models.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private  final Firestore firestore;
    private final ParticipantIdService participantIdService;
    public static final String COLLECTION_NAME = "aspirationnetworkusers";

    public User getUser(String id) throws ExecutionException, InterruptedException {
        System.out.println("Searching Firestore for UID: " + id);

        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.toObject(User.class);
        }
        return null;
    }
    public String createUserDetails(@NonNull UserProfileCreationDTO dto) throws Exception {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setUid(dto.getUid());
        user.setAccountType("member");
        user.setRole("member");
        applyParticipantIdAssignment(user, participantIdService.generateParticipantId());
        user.setProfileImageUrl(null);
        user.setPublicProfile(false);
        user.setYouthProfile(true);
        user.setProfileStatus("pending_onboarding");
        user.setSchool(null);
        user.setGraduationYear(null);
        user.setDesiredMajor(null);
        user.setProgramIds(new ArrayList<>());
        user.setProgramParticipationIds(new ArrayList<>());
        user.setCredentialIds(new ArrayList<>());
        user.setEarnedCredentialIds(new ArrayList<>());
        user.setAttendanceRecordIds(new ArrayList<>());
        user.setServiceHourRecordIds(new ArrayList<>());
        user.setCollegeInterests(new ArrayList<>());
        user.setCareerInterests(new ArrayList<>());
        user.setDesiredCareerFields(new ArrayList<>());
        user.setGovernmentCareerInterests(new ArrayList<>());
        user.setWorkforceInterests(new ArrayList<>());
        user.setCivicInterests(new ArrayList<>());
        user.setCommunityInterests(new ArrayList<>());
        user.setPublicServiceInterests(new ArrayList<>());
        user.setStaffReviewRequired(true);
        user.setStaffVerified(false);
        user.setExternalConsentReceived(false);
        user.setCredentialReviewAccess(false);
        user.setAttendanceReviewAccess(false);
        user.setServiceHourVerificationAccess(false);

        firestore.collection(COLLECTION_NAME).document(dto.getUid()).set(user).get();

        return dto.getUid();
    }

    public List<User> getYouthUsersForStaff() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("youthProfile", true)
                .get();

        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            users.add(document.toObject(User.class));
        }
        return users;
    }

    public User getYouthUserForStaff(String uid) throws ExecutionException, InterruptedException {
        User user = getUser(uid);
        if (user == null || !user.isYouthProfile()) {
            return null;
        }
        return user;
    }

    public List<User> getUsersForStaff(String role, Boolean active, Boolean youthProfile, String programId)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();

        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            User user = document.toObject(User.class);
            if (user != null && matchesStaffUserFilters(user, role, active, youthProfile, programId)) {
                users.add(user);
            }
        }
        return users;
    }

    public User getUserForStaff(String uid) throws ExecutionException, InterruptedException {
        requireText(uid, "uid is required");
        return getUser(uid);
    }

    public UserTotalsDTO getUserTotalsForStaff() throws ExecutionException, InterruptedException {
        UserTotalsDTO totals = new UserTotalsDTO();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();

        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            User user = document.toObject(User.class);
            if (user == null) {
                continue;
            }

            totals.setTotalUsers(totals.getTotalUsers() + 1);
            if ("active".equalsIgnoreCase(user.getProfileStatus())) {
                totals.setActiveUsers(totals.getActiveUsers() + 1);
            }
            if ("inactive".equalsIgnoreCase(user.getProfileStatus())) {
                totals.setInactiveUsers(totals.getInactiveUsers() + 1);
            }
            if (user.isYouthProfile()) {
                totals.setYouthUsers(totals.getYouthUsers() + 1);
            }

            String role = user.getRole() == null ? "" : user.getRole().toLowerCase();
            if ("staff".equals(role) || "admin".equals(role)) {
                totals.setStaffUsers(totals.getStaffUsers() + 1);
            } else if ("educator".equals(role)) {
                totals.setEducatorUsers(totals.getEducatorUsers() + 1);
            } else if ("partner".equals(role)) {
                totals.setPartnerUsers(totals.getPartnerUsers() + 1);
            } else if ("government".equals(role)) {
                totals.setGovernmentUsers(totals.getGovernmentUsers() + 1);
            }
        }

        return totals;
    }

    public void updateYouthUserForStaff(String uid, StaffUserUpdateDTO dto) throws ExecutionException, InterruptedException {
        User user = getYouthUserForStaff(uid);
        if (user == null) {
            throw new IllegalArgumentException("Youth user profile does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        if (dto.getProfileStatus() != null) {
            updates.put("profileStatus", dto.getProfileStatus());
        }
        if (dto.getProgramIds() != null) {
            updates.put("programIds", dto.getProgramIds());
            updates.put("programParticipationIds", dto.getProgramIds());
        }
        if (dto.getStaffReviewRequired() != null) {
            updates.put("staffReviewRequired", dto.getStaffReviewRequired());
        }
        if (dto.getStaffVerified() != null) {
            updates.put("staffVerified", dto.getStaffVerified());
        }

        if (!updates.isEmpty()) {
            firestore.collection(COLLECTION_NAME).document(uid).update(updates).get();
        }
    }

    public void updateUserForStaff(String uid, StaffUserUpdateDTO dto) throws ExecutionException, InterruptedException {
        requireText(uid, "uid is required");
        if (dto == null) {
            throw new IllegalArgumentException("user update request is required");
        }

        User user = getUserForStaff(uid);
        if (user == null) {
            throw new IllegalArgumentException("User profile does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        if (dto.getProfileStatus() != null) {
            updates.put("profileStatus", dto.getProfileStatus());
        }
        if (dto.getProgramIds() != null) {
            updates.put("programIds", dto.getProgramIds());
        }
        if (dto.getStaffReviewRequired() != null) {
            updates.put("staffReviewRequired", dto.getStaffReviewRequired());
        }
        if (dto.getStaffVerified() != null) {
            updates.put("staffVerified", dto.getStaffVerified());
        }

        if (!updates.isEmpty()) {
            firestore.collection(COLLECTION_NAME).document(uid).update(updates).get();
        }
    }

    public void activateUserForStaff(String uid) throws ExecutionException, InterruptedException {
        setProfileStatusForStaff(uid, "active");
    }

    public void deactivateUserForStaff(String uid) throws ExecutionException, InterruptedException {
        setProfileStatusForStaff(uid, "inactive");
    }

    private void setProfileStatusForStaff(String uid, String profileStatus)
            throws ExecutionException, InterruptedException {
        requireText(uid, "uid is required");
        if (getUserForStaff(uid) == null) {
            throw new IllegalArgumentException("User profile does not exist");
        }
        firestore.collection(COLLECTION_NAME).document(uid).update("profileStatus", profileStatus).get();
    }

    private boolean matchesStaffUserFilters(
            User user,
            String role,
            Boolean active,
            Boolean youthProfile,
            String programId
    ) {
        if (role != null && !role.isBlank() && !role.equalsIgnoreCase(user.getRole())) {
            return false;
        }
        if (active != null) {
            String expectedStatus = active ? "active" : "inactive";
            if (!expectedStatus.equalsIgnoreCase(user.getProfileStatus())) {
                return false;
            }
        }
        if (youthProfile != null && youthProfile != user.isYouthProfile()) {
            return false;
        }
        return programId == null || programId.isBlank()
                || (user.getProgramIds() != null && user.getProgramIds().contains(programId))
                || (user.getProgramParticipationIds() != null
                && user.getProgramParticipationIds().contains(programId));
    }

    public User completeYouthProfile(String uid, YouthProfileCompletionDTO dto) throws Exception {
        requireText(uid, "uid is required");
        if (dto == null) {
            throw new IllegalArgumentException("profile completion request is required");
        }

        User existingUser = getUser(uid);
        if (existingUser == null) {
            User user = new User();
            user.setUid(uid);
            user.setAccountType("member");
            user.setRole("member");
            applyParticipantIdAssignment(user, participantIdService.generateParticipantId());
            user.setPublicProfile(false);
            user.setYouthProfile(true);
            user.setProfileStatus("pending_onboarding");
            user.setStaffReviewRequired(true);
            user.setStaffVerified(false);
            user.setExternalConsentReceived(false);
            user.setCredentialReviewAccess(false);
            user.setAttendanceReviewAccess(false);
            user.setServiceHourVerificationAccess(false);
            applyYouthProfileFields(user, dto);
            firestore.collection(COLLECTION_NAME).document(uid).set(user).get();
            return user;
        }

        if (!existingUser.isYouthProfile() || isStaffOrAdmin(existingUser.getRole())) {
            throw new ForbiddenAccessException("Only youth profiles can use self-service profile completion");
        }

        Map<String, Object> updates = new HashMap<>();
        addParticipantIdIfMissing(existingUser, updates);
        addStringIfPresent(updates, "firstName", dto.getFirstName());
        addStringIfPresent(updates, "lastName", dto.getLastName());
        addStringIfPresent(updates, "email", dto.getEmail());
        addStringIfPresent(updates, "school", dto.getSchool());
        addStringIfPresent(updates, "graduationYear", dto.getGraduationYear());
        addListIfPresent(updates, "collegeInterests", dto.getCollegeInterests());
        addListIfPresent(updates, "careerInterests", dto.getCareerInterests());
        addListIfPresent(updates, "civicInterests", dto.getCivicInterests());
        addListIfPresent(updates, "communityInterests", dto.getCommunityInterests());
        addListIfPresent(updates, "publicServiceInterests", dto.getPublicServiceInterests());
        updates.put("publicProfile", false);
        updates.put("youthProfile", true);

        if (existingUser.getProfileStatus() == null || existingUser.getProfileStatus().isBlank()) {
            updates.put("profileStatus", "pending_onboarding");
        }

        firestore.collection(COLLECTION_NAME).document(uid).update(updates).get();
        return getUser(uid);
    }

    private void addParticipantIdIfMissing(User user, Map<String, Object> updates) throws Exception {
        if (user.getAspnParticipantId() != null && !user.getAspnParticipantId().isBlank()) {
            return;
        }

        ParticipantIdService.ParticipantIdAssignment assignment = participantIdService.generateParticipantId();
        updates.put("aspnParticipantId", assignment.getAspnParticipantId());
        updates.put("aspnParticipantIdAssignedAt", assignment.getAssignedAt());
        updates.put("aspnParticipantIdAssignedBy", assignment.getAssignedBy());
        updates.put("aspnParticipantCohortYear", assignment.getCohortYear());
    }

    private void applyParticipantIdAssignment(
            User user,
            ParticipantIdService.ParticipantIdAssignment assignment
    ) {
        user.setAspnParticipantId(assignment.getAspnParticipantId());
        user.setAspnParticipantIdAssignedAt(assignment.getAssignedAt());
        user.setAspnParticipantIdAssignedBy(assignment.getAssignedBy());
        user.setAspnParticipantCohortYear(assignment.getCohortYear());
    }

    private boolean isStaffOrAdmin(String role) {
        return "staff".equals(role) || "admin".equals(role);
    }

    private void applyYouthProfileFields(User user, YouthProfileCompletionDTO dto) {
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setSchool(dto.getSchool());
        user.setGraduationYear(dto.getGraduationYear());
        user.setCollegeInterests(listOrEmpty(dto.getCollegeInterests()));
        user.setCareerInterests(listOrEmpty(dto.getCareerInterests()));
        user.setCivicInterests(listOrEmpty(dto.getCivicInterests()));
        user.setCommunityInterests(listOrEmpty(dto.getCommunityInterests()));
        user.setPublicServiceInterests(listOrEmpty(dto.getPublicServiceInterests()));
    }

    private List<String> listOrEmpty(List<String> values) {
        return values == null ? new ArrayList<>() : values;
    }

    private void addStringIfPresent(Map<String, Object> updates, String fieldName, String value) {
        if (value != null) {
            updates.put(fieldName, value);
        }
    }

    private void addListIfPresent(Map<String, Object> updates, String fieldName, List<String> values) {
        if (values != null) {
            updates.put(fieldName, values);
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

}
