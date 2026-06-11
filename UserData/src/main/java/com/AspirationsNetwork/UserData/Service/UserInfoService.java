package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.StaffUserUpdateDTO;
import com.AspirationsNetwork.UserData.DTO.UserProfileCreationDTO;
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
    public String createUserDetails(@NonNull UserProfileCreationDTO dto) throws ExecutionException, InterruptedException {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setUid(dto.getUid());
        user.setAccountType("member");
        user.setRole("member");
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

}
