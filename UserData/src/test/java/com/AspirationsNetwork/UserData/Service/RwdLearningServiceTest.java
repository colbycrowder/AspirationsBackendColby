package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.DTO.RwdActivityDTO;
import com.AspirationsNetwork.UserData.DTO.RwdProgressDTO;
import com.AspirationsNetwork.UserData.Models.RwdActivity;
import com.AspirationsNetwork.UserData.Models.RwdProgress;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RwdLearningServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void createRwdActivityStoresExternallyHostedActivity() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CredentialService credentialService = mock(CredentialService.class);
        CollectionReference activitiesCollection = mock(CollectionReference.class);
        DocumentReference activityDocument = mock(DocumentReference.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        when(firestore.collection(RwdLearningService.ACTIVITIES_COLLECTION)).thenReturn(activitiesCollection);
        when(activitiesCollection.document(any(String.class))).thenReturn(activityDocument);
        when(activityDocument.set(any(RwdActivity.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));

        RwdActivityDTO dto = new RwdActivityDTO();
        dto.setCountryName("Bangladesh");
        dto.setTitle("Bangladesh RWD Activity");
        dto.setDescription("Externally hosted movement map activity");

        RwdLearningService service = new RwdLearningService(firestore, credentialService);
        String rwdActivityId = service.createRwdActivity(dto);

        ArgumentCaptor<RwdActivity> activityCaptor = ArgumentCaptor.forClass(RwdActivity.class);
        verify(activityDocument).set(activityCaptor.capture());

        RwdActivity savedActivity = activityCaptor.getValue();
        assertEquals(rwdActivityId, savedActivity.getRwdActivityId());
        assertEquals("Bangladesh", savedActivity.getCountryName());
        assertEquals(RwdLearningService.MOVEMENT_MAP_URL, savedActivity.getExternalUrl());
        assertTrue(savedActivity.isActive());
    }

    @Test
    @SuppressWarnings("unchecked")
    void saveProgressPassesAtEightyPercentAndAwardsLinkedCredential() throws Exception {
        Firestore firestore = mock(Firestore.class);
        CredentialService credentialService = mock(CredentialService.class);
        CollectionReference activitiesCollection = mock(CollectionReference.class);
        CollectionReference progressCollection = mock(CollectionReference.class);
        DocumentReference activityDocument = mock(DocumentReference.class);
        DocumentReference progressDocument = mock(DocumentReference.class);
        DocumentSnapshot activitySnapshot = mock(DocumentSnapshot.class);
        Query userProgressQuery = mock(Query.class);
        Query activityProgressQuery = mock(Query.class);
        QuerySnapshot progressSnapshot = mock(QuerySnapshot.class);
        ApiFuture<DocumentSnapshot> activityFuture = mock(ApiFuture.class);
        ApiFuture<QuerySnapshot> progressFuture = mock(ApiFuture.class);
        ApiFuture<WriteResult> writeFuture = mock(ApiFuture.class);

        RwdActivity activity = new RwdActivity();
        activity.setRwdActivityId("rwd-123");
        activity.setActive(true);
        activity.setAssociatedCredentialId("credential-123");

        when(firestore.collection(RwdLearningService.ACTIVITIES_COLLECTION)).thenReturn(activitiesCollection);
        when(firestore.collection(RwdLearningService.PROGRESS_COLLECTION)).thenReturn(progressCollection);
        when(activitiesCollection.document("rwd-123")).thenReturn(activityDocument);
        when(activityDocument.get()).thenReturn(activityFuture);
        when(activityFuture.get()).thenReturn(activitySnapshot);
        when(activitySnapshot.exists()).thenReturn(true);
        when(activitySnapshot.toObject(RwdActivity.class)).thenReturn(activity);
        when(progressCollection.whereEqualTo("userUID", "youth-123")).thenReturn(userProgressQuery);
        when(userProgressQuery.whereEqualTo("rwdActivityId", "rwd-123")).thenReturn(activityProgressQuery);
        when(activityProgressQuery.get()).thenReturn(progressFuture);
        when(progressFuture.get()).thenReturn(progressSnapshot);
        when(progressSnapshot.getDocuments()).thenReturn(List.of());
        when(progressCollection.document(any(String.class))).thenReturn(progressDocument);
        when(progressDocument.set(any(RwdProgress.class))).thenReturn(writeFuture);
        when(writeFuture.get()).thenReturn(mock(WriteResult.class));
        when(credentialService.awardLinkedCredentialIfNotEarned("youth-123", "credential-123", "system_rwd"))
                .thenReturn("earned-123");

        RwdProgressDTO dto = new RwdProgressDTO();
        dto.setRwdActivityId("rwd-123");
        dto.setQuizScore(80);

        RwdLearningService service = new RwdLearningService(firestore, credentialService);
        RwdProgress progress = service.saveProgressForUser("youth-123", dto);

        assertEquals("completed", progress.getCompletionStatus());
        assertTrue(progress.isPassed());
        assertTrue(progress.isCredentialAwarded());
        assertEquals("earned-123", progress.getEarnedCredentialId());
        verify(credentialService).awardLinkedCredentialIfNotEarned("youth-123", "credential-123", "system_rwd");
    }
}
