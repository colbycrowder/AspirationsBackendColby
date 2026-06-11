package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.PlatformMetricsDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricsServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void getPlatformMetricsCountsExistingPlatformData() throws Exception {
        Firestore firestore = mock(Firestore.class);

        CollectionReference usersCollection = mock(CollectionReference.class);
        CollectionReference programsCollection = mock(CollectionReference.class);
        CollectionReference enrollmentsCollection = mock(CollectionReference.class);
        CollectionReference earnedCredentialsCollection = mock(CollectionReference.class);
        CollectionReference attendanceCollection = mock(CollectionReference.class);
        CollectionReference serviceHoursCollection = mock(CollectionReference.class);
        CollectionReference rwdProgressCollection = mock(CollectionReference.class);
        CollectionReference notificationsCollection = mock(CollectionReference.class);

        Query totalYouthQuery = mock(Query.class);
        Query activeYouthProfileQuery = mock(Query.class);
        Query activeYouthStatusQuery = mock(Query.class);
        Query activeProgramsQuery = mock(Query.class);
        Query completedRwdQuery = mock(Query.class);
        Query unreadNotificationsQuery = mock(Query.class);

        when(firestore.collection(UserInfoService.COLLECTION_NAME)).thenReturn(usersCollection);
        when(firestore.collection(ProgramService.COLLECTION_NAME)).thenReturn(programsCollection);
        when(firestore.collection(ProgramEnrollmentService.COLLECTION_NAME)).thenReturn(enrollmentsCollection);
        when(firestore.collection(CredentialService.EARNED_CREDENTIALS_COLLECTION))
                .thenReturn(earnedCredentialsCollection);
        when(firestore.collection(AttendanceService.COLLECTION_NAME)).thenReturn(attendanceCollection);
        when(firestore.collection(ServiceHourService.COLLECTION_NAME)).thenReturn(serviceHoursCollection);
        when(firestore.collection(RwdLearningService.PROGRESS_COLLECTION)).thenReturn(rwdProgressCollection);
        when(firestore.collection(NotificationService.COLLECTION_NAME)).thenReturn(notificationsCollection);

        when(usersCollection.whereEqualTo("youthProfile", true)).thenReturn(totalYouthQuery, activeYouthProfileQuery);
        when(activeYouthProfileQuery.whereEqualTo("profileStatus", "active")).thenReturn(activeYouthStatusQuery);
        when(programsCollection.whereEqualTo("programStatus", "active")).thenReturn(activeProgramsQuery);
        when(rwdProgressCollection.whereEqualTo("completionStatus", "completed")).thenReturn(completedRwdQuery);
        when(notificationsCollection.whereEqualTo("read", false)).thenReturn(unreadNotificationsQuery);

        stubQueryCount(totalYouthQuery, 12);
        stubQueryCount(activeYouthStatusQuery, 7);
        stubQueryCount(activeProgramsQuery, 3);
        stubCollectionCount(enrollmentsCollection, 20);
        stubCollectionCount(earnedCredentialsCollection, 15);
        stubCollectionCount(attendanceCollection, 30);
        stubCollectionCount(serviceHoursCollection, 4);
        stubQueryCount(completedRwdQuery, 9);
        stubQueryCount(unreadNotificationsQuery, 5);

        MetricsService service = new MetricsService(firestore);
        PlatformMetricsDTO metrics = service.getPlatformMetrics();

        assertEquals(12, metrics.getTotalYouthUsers());
        assertEquals(7, metrics.getActiveYouthUsers());
        assertEquals(3, metrics.getActivePrograms());
        assertEquals(20, metrics.getEnrollments());
        assertEquals(15, metrics.getEarnedCredentials());
        assertEquals(30, metrics.getAttendanceRecords());
        assertEquals(4, metrics.getServiceHourRecords());
        assertEquals(9, metrics.getCompletedRwdActivities());
        assertEquals(5, metrics.getUnreadNotifications());
    }

    @SuppressWarnings("unchecked")
    private void stubCollectionCount(CollectionReference collectionReference, int count) throws Exception {
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = querySnapshotWithCount(count);
        when(collectionReference.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
    }

    @SuppressWarnings("unchecked")
    private void stubQueryCount(Query query, int count) throws Exception {
        ApiFuture<QuerySnapshot> future = mock(ApiFuture.class);
        QuerySnapshot snapshot = querySnapshotWithCount(count);
        when(query.get()).thenReturn(future);
        when(future.get()).thenReturn(snapshot);
    }

    private QuerySnapshot querySnapshotWithCount(int count) {
        QuerySnapshot snapshot = mock(QuerySnapshot.class);
        List<QueryDocumentSnapshot> documents = count == 0
                ? Collections.emptyList()
                : Collections.nCopies(count, mock(QueryDocumentSnapshot.class));
        when(snapshot.getDocuments()).thenReturn(documents);
        return snapshot;
    }
}
