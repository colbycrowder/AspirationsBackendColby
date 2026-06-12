package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.RwdActivityDTO;
import com.AspirationsNetwork.UserData.DTO.RwdLearningCenterItemDTO;
import com.AspirationsNetwork.UserData.DTO.RwdProgressDTO;
import com.AspirationsNetwork.UserData.Models.RwdActivity;
import com.AspirationsNetwork.UserData.Models.RwdProgress;
import com.AspirationsNetwork.UserData.Models.PlatformEventType;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class RwdLearningService {
    public static final String ACTIVITIES_COLLECTION = "rwdActivities";
    public static final String PROGRESS_COLLECTION = "rwdProgress";
    public static final int PASSING_SCORE = 80;
    public static final String MOVEMENT_MAP_URL = "https://aspirationsnetwork.org/movement-map/";
    public static final List<String> RWD_COUNTRIES = List.of(
            "Bangladesh",
            "Bulgaria",
            "Indonesia",
            "Kenya",
            "Madagascar",
            "Mexico",
            "Mongolia",
            "Morocco",
            "Nepal",
            "Nigeria",
            "Peru",
            "Philippines",
            "Serbia",
            "Tanzania",
            "Timor-Leste",
            "United States"
    );
    private static final Set<String> VALID_COMPLETION_STATUSES = Set.of("not_started", "in_progress", "completed");
    private static final String SYSTEM_RWD_AWARDER = "system_rwd";

    private final Firestore firestore;
    private final CredentialService credentialService;
    private final PlatformEventService platformEventService;

    public String createRwdActivity(RwdActivityDTO dto) throws Exception {
        requireText(dto.getCountryName(), "countryName is required");
        requireText(dto.getTitle(), "title is required");

        String rwdActivityId = UUID.randomUUID().toString();
        Date now = new Date();

        RwdActivity activity = new RwdActivity();
        activity.setRwdActivityId(rwdActivityId);
        activity.setCountryName(dto.getCountryName());
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setExternalUrl(defaultExternalUrl(dto.getExternalUrl()));
        activity.setActive(dto.getActive() == null || dto.getActive());
        activity.setAssociatedCredentialId(dto.getAssociatedCredentialId());
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);

        firestore.collection(ACTIVITIES_COLLECTION)
                .document(rwdActivityId)
                .set(activity)
                .get();

        return rwdActivityId;
    }

    public void updateRwdActivity(String rwdActivityId, RwdActivityDTO dto) throws Exception {
        requireText(rwdActivityId, "rwdActivityId is required");

        DocumentSnapshot document = firestore.collection(ACTIVITIES_COLLECTION)
                .document(rwdActivityId)
                .get()
                .get();

        if (!document.exists()) {
            throw new IllegalArgumentException("RWD activity does not exist");
        }

        Map<String, Object> updates = new HashMap<>();
        addIfPresent(updates, "countryName", dto.getCountryName());
        addIfPresent(updates, "title", dto.getTitle());
        addIfPresent(updates, "description", dto.getDescription());
        if (dto.getExternalUrl() != null) {
            updates.put("externalUrl", defaultExternalUrl(dto.getExternalUrl()));
        }
        if (dto.getActive() != null) {
            updates.put("active", dto.getActive());
        }
        addIfPresent(updates, "associatedCredentialId", dto.getAssociatedCredentialId());
        updates.put("updatedAt", new Date());

        firestore.collection(ACTIVITIES_COLLECTION)
                .document(rwdActivityId)
                .update(updates)
                .get();
    }

    public List<RwdActivity> getActiveRwdActivities() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(ACTIVITIES_COLLECTION)
                .whereEqualTo("active", true)
                .get();

        List<RwdActivity> activities = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            activities.add(document.toObject(RwdActivity.class));
        }
        return activities;
    }

    public List<RwdProgress> getProgressForUser(String userUID) throws ExecutionException, InterruptedException {
        requireText(userUID, "userUID is required");

        ApiFuture<QuerySnapshot> future = firestore.collection(PROGRESS_COLLECTION)
                .whereEqualTo("userUID", userUID)
                .get();

        List<RwdProgress> progressRecords = new ArrayList<>();
        for (QueryDocumentSnapshot document : future.get().getDocuments()) {
            progressRecords.add(document.toObject(RwdProgress.class));
        }
        return progressRecords;
    }

    public RwdProgress saveProgressForUser(String userUID, RwdProgressDTO dto) throws Exception {
        requireText(userUID, "userUID is required");
        if (dto == null) {
            throw new IllegalArgumentException("RWD progress request is required");
        }
        requireText(dto.getRwdActivityId(), "rwdActivityId is required");

        RwdActivity activity = getActiveActivity(dto.getRwdActivityId());
        if (activity == null) {
            throw new IllegalArgumentException("RWD activity does not exist or is inactive");
        }

        RwdProgress progress = getExistingProgress(userUID, dto.getRwdActivityId());
        if (progress == null) {
            progress = new RwdProgress();
            progress.setProgressId(UUID.randomUUID().toString());
            progress.setUserUID(userUID);
            progress.setRwdActivityId(dto.getRwdActivityId());
        }
        String previousCompletionStatus = progress.getCompletionStatus();

        Integer quizScore = dto.getQuizScore();
        if (quizScore != null) {
            validateQuizScore(quizScore);
            progress.setQuizScore(quizScore);
            if (quizScore >= PASSING_SCORE) {
                progress.setPassed(true);
                progress.setCompletionStatus("completed");
                if (progress.getCompletedAt() == null) {
                    progress.setCompletedAt(new Date());
                }
                awardLinkedCredentialIfEligible(progress, activity);
            } else {
                progress.setPassed(false);
                progress.setCompletionStatus(normalizeCompletionStatus(dto.getCompletionStatus(), "in_progress"));
            }
        } else {
            progress.setCompletionStatus(normalizeCompletionStatus(dto.getCompletionStatus(), progress.getCompletionStatus()));
            if ("completed".equals(progress.getCompletionStatus())) {
                progress.setCompletedAt(new Date());
            }
        }

        firestore.collection(PROGRESS_COLLECTION)
                .document(progress.getProgressId())
                .set(progress)
                .get();

        if ("completed".equals(progress.getCompletionStatus())
                && !"completed".equals(previousCompletionStatus)) {
            platformEventService.trackEventSafely(
                    userUID,
                    PlatformEventType.RWD_ACTIVITY_COMPLETED,
                    Map.of("activityId", dto.getRwdActivityId())
            );
        }

        return progress;
    }

    public List<RwdLearningCenterItemDTO> getLearningCenterForUser(String userUID)
            throws ExecutionException, InterruptedException {
        Map<String, RwdProgress> progressByActivityId = new HashMap<>();
        for (RwdProgress progress : getProgressForUser(userUID)) {
            progressByActivityId.put(progress.getRwdActivityId(), progress);
        }

        List<RwdLearningCenterItemDTO> items = new ArrayList<>();
        for (RwdActivity activity : getActiveRwdActivities()) {
            RwdLearningCenterItemDTO item = new RwdLearningCenterItemDTO();
            item.setRwdActivityId(activity.getRwdActivityId());
            item.setCountryName(activity.getCountryName());
            item.setTitle(activity.getTitle());
            item.setDescription(activity.getDescription());
            item.setExternalUrl(activity.getExternalUrl());
            item.setActive(activity.isActive());
            item.setAssociatedCredentialId(activity.getAssociatedCredentialId());
            item.setProgress(progressByActivityId.get(activity.getRwdActivityId()));
            items.add(item);
        }
        return items;
    }

    private RwdActivity getActiveActivity(String rwdActivityId) throws Exception {
        DocumentSnapshot document = firestore.collection(ACTIVITIES_COLLECTION)
                .document(rwdActivityId)
                .get()
                .get();

        if (!document.exists()) {
            return null;
        }

        RwdActivity activity = document.toObject(RwdActivity.class);
        if (activity == null || !activity.isActive()) {
            return null;
        }
        return activity;
    }

    private RwdProgress getExistingProgress(String userUID, String rwdActivityId)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(PROGRESS_COLLECTION)
                .whereEqualTo("userUID", userUID)
                .whereEqualTo("rwdActivityId", rwdActivityId)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }
        return documents.get(0).toObject(RwdProgress.class);
    }

    private void awardLinkedCredentialIfEligible(RwdProgress progress, RwdActivity activity) throws Exception {
        String associatedCredentialId = activity.getAssociatedCredentialId();
        if (associatedCredentialId == null || associatedCredentialId.isBlank() || progress.isCredentialAwarded()) {
            return;
        }

        String earnedCredentialId = credentialService.awardLinkedCredentialIfNotEarned(
                progress.getUserUID(),
                associatedCredentialId,
                SYSTEM_RWD_AWARDER
        );

        if (earnedCredentialId != null) {
            progress.setCredentialAwarded(true);
            progress.setEarnedCredentialId(earnedCredentialId);
        }
    }

    private void validateQuizScore(Integer quizScore) {
        if (quizScore < 0 || quizScore > 100) {
            throw new IllegalArgumentException("quizScore must be between 0 and 100");
        }
    }

    private String normalizeCompletionStatus(String status, String defaultStatus) {
        String normalizedStatus = status == null || status.isBlank()
                ? defaultStatus
                : status.toLowerCase();

        if (!VALID_COMPLETION_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("completionStatus must be not_started, in_progress, or completed");
        }

        return normalizedStatus;
    }

    private String defaultExternalUrl(String externalUrl) {
        if (externalUrl == null || externalUrl.isBlank()) {
            return MOVEMENT_MAP_URL;
        }
        return externalUrl;
    }

    private void addIfPresent(Map<String, Object> updates, String fieldName, Object value) {
        if (value != null) {
            updates.put(fieldName, value);
        }
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
