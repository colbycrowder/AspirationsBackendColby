package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.ProgramEnrollment;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ProgramEnrollmentService {
    public static final String COLLECTION_NAME = "programEnrollments";
    private static final Set<String> VALID_STATUSES = Set.of("active", "removed");

    private final Firestore firestore;
    private final ProgramService programService;

    public String enrollYouthInProgram(String userUID, String programId) throws Exception {
        requireText(userUID, "userUID is required");
        requireText(programId, "programId is required");

        if (programService.getActiveProgramById(programId) == null) {
            throw new IllegalArgumentException("Youth users may enroll only into active programs");
        }

        if (hasActiveEnrollment(userUID, programId)) {
            throw new IllegalArgumentException("User is already enrolled in this program");
        }

        String enrollmentId = UUID.randomUUID().toString();
        Date now = new Date();

        ProgramEnrollment enrollment = new ProgramEnrollment();
        enrollment.setEnrollmentId(enrollmentId);
        enrollment.setUserUID(userUID);
        enrollment.setProgramId(programId);
        enrollment.setEnrollmentStatus("active");
        enrollment.setEnrolledAt(now);
        enrollment.setUpdatedAt(now);
        enrollment.setCreatedByUser(true);

        firestore.collection(COLLECTION_NAME)
                .document(enrollmentId)
                .set(enrollment)
                .get();

        firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(userUID)
                .update(
                        "programIds", FieldValue.arrayUnion(programId),
                        "programParticipationIds", FieldValue.arrayUnion(enrollmentId)
                )
                .get();

        return enrollmentId;
    }

    public List<ProgramEnrollment> getAllEnrollments() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        return mapEnrollments(future.get().getDocuments());
    }

    public List<ProgramEnrollment> getEnrollmentsForProgram(String programId)
            throws ExecutionException, InterruptedException {
        requireText(programId, "programId is required");

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("programId", programId)
                .get();
        return mapEnrollments(future.get().getDocuments());
    }

    public List<ProgramEnrollment> getEnrollmentsForUser(String userUID)
            throws ExecutionException, InterruptedException {
        requireText(userUID, "userUID is required");

        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userUID", userUID)
                .get();
        return mapEnrollments(future.get().getDocuments());
    }

    public void removeEnrollment(String enrollmentId, String removedByStaffUID) throws Exception {
        requireText(enrollmentId, "enrollmentId is required");
        requireText(removedByStaffUID, "removedByStaffUID is required");

        QueryDocumentSnapshot enrollmentDocument = getEnrollmentDocument(enrollmentId);
        if (enrollmentDocument == null) {
            throw new IllegalArgumentException("Enrollment does not exist");
        }

        ProgramEnrollment enrollment = enrollmentDocument.toObject(ProgramEnrollment.class);
        String status = normalizeStatus("removed");

        firestore.collection(COLLECTION_NAME)
                .document(enrollmentId)
                .update(
                        "enrollmentStatus", status,
                        "removedByStaffUID", removedByStaffUID,
                        "updatedAt", new Date()
                )
                .get();

        firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(enrollment.getUserUID())
                .update(
                        "programIds", FieldValue.arrayRemove(enrollment.getProgramId()),
                        "programParticipationIds", FieldValue.arrayRemove(enrollmentId)
                )
                .get();
    }

    private boolean hasActiveEnrollment(String userUID, String programId)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userUID", userUID)
                .whereEqualTo("programId", programId)
                .whereEqualTo("enrollmentStatus", "active")
                .get();

        return !future.get().getDocuments().isEmpty();
    }

    private QueryDocumentSnapshot getEnrollmentDocument(String enrollmentId)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("enrollmentId", enrollmentId)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (documents.isEmpty()) {
            return null;
        }
        return documents.get(0);
    }

    private List<ProgramEnrollment> mapEnrollments(List<QueryDocumentSnapshot> documents) {
        List<ProgramEnrollment> enrollments = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            enrollments.add(document.toObject(ProgramEnrollment.class));
        }
        return enrollments;
    }

    private String normalizeStatus(String status) {
        String normalizedStatus = status == null || status.isBlank()
                ? "active"
                : status.toLowerCase();

        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("enrollmentStatus must be active or removed");
        }

        return normalizedStatus;
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
