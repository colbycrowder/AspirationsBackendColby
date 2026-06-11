package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PlatformMetricsDTO;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final Firestore firestore;

    public PlatformMetricsDTO getPlatformMetrics() throws Exception {
        PlatformMetricsDTO metrics = new PlatformMetricsDTO();
        metrics.setTotalYouthUsers(countByField(UserInfoService.COLLECTION_NAME, "youthProfile", true));
        metrics.setActiveYouthUsers(countActiveYouthUsers());
        metrics.setActivePrograms(countByField(ProgramService.COLLECTION_NAME, "programStatus", "active"));
        metrics.setEnrollments(countCollection(ProgramEnrollmentService.COLLECTION_NAME));
        metrics.setEarnedCredentials(countCollection(CredentialService.EARNED_CREDENTIALS_COLLECTION));
        metrics.setAttendanceRecords(countCollection(AttendanceService.COLLECTION_NAME));
        metrics.setServiceHourRecords(countCollection(ServiceHourService.COLLECTION_NAME));
        metrics.setCompletedRwdActivities(countByField(
                RwdLearningService.PROGRESS_COLLECTION,
                "completionStatus",
                "completed"
        ));
        metrics.setUnreadNotifications(countByField(NotificationService.COLLECTION_NAME, "read", false));
        return metrics;
    }

    private int countCollection(String collectionName) throws Exception {
        return firestore.collection(collectionName).get().get().getDocuments().size();
    }

    private int countByField(String collectionName, String fieldName, Object value) throws Exception {
        return firestore.collection(collectionName)
                .whereEqualTo(fieldName, value)
                .get()
                .get()
                .getDocuments()
                .size();
    }

    private int countActiveYouthUsers() throws Exception {
        Query activeYouthQuery = firestore.collection(UserInfoService.COLLECTION_NAME)
                .whereEqualTo("youthProfile", true)
                .whereEqualTo("profileStatus", "active");

        return activeYouthQuery.get().get().getDocuments().size();
    }
}
