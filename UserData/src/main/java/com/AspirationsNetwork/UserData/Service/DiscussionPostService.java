package com.AspirationsNetwork.UserData.Service;

import com.AspirationsNetwork.UserData.Models.Comment;
import com.AspirationsNetwork.UserData.Models.DiscussionPost;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class DiscussionPostService {
    private  final Firestore firestore;
    private static final String COLLECTION_NAME = "aspirationnetworkposts";


    public List<DiscussionPost> getAllPosts() throws ExecutionException, InterruptedException {
        // 1. Get the future
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();


        // 2. Get the snapshots from the future
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        // 3. Map the documents to your DiscussionPost class
        List<DiscussionPost> posts = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            posts.add(document.toObject(DiscussionPost.class));
        }

        return posts;
    }

    public String createDiscussionPost(DiscussionPost post) throws Exception {
        // Look up the creator in the same profile collection used by UserInfoService.
        DocumentSnapshot userDoc = firestore.collection(UserInfoService.COLLECTION_NAME)
                .document(post.getCreatorUID())
                .get().get();

        if (userDoc.exists()) {

            String fullName = userDoc.getString("firstName") + " " + userDoc.getString("lastName");
            post.setCreatorName(fullName);
        }


        post.setPostID(UUID.randomUUID().toString());
        post.setCreationTime(new Date());
        firestore.collection(COLLECTION_NAME).document(post.getPostID()).set(post);

        return post.getPostID();
    }



    public String createComment(Comment comment) throws Exception {
        String commentID = UUID.randomUUID().toString();
        comment.setCommentID(commentID);
        comment.setCreationTime(new Date());

        firestore.collection("comments")
                .document(commentID)
                .set(comment)
                .get();

        return commentID;
    }
    public List<Comment> getCommentsForPost(String postID) throws Exception {
        // This query filters the comments collection for only those linked to this post
        ApiFuture<QuerySnapshot> future = firestore.collection("comments")
                .whereEqualTo("postID", postID)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Comment> comments = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            comments.add(doc.toObject(Comment.class));
        }

        // Sort them by time so the conversation flows correctly
        comments.sort(Comparator.comparing(Comment::getCreationTime));

        return comments;
    }


    public void upvotePost(String postID){
        firestore.collection(COLLECTION_NAME)
                .document(postID)
                .update("upvotes", FieldValue.increment(1));
    }


    public String deletePost(String postID) throws Exception{

        DocumentReference postRef = firestore.collection(COLLECTION_NAME).document(postID);

        ApiFuture<QuerySnapshot> future = firestore.collection("comments")
                .whereEqualTo("postID", postID)
                .get();

        WriteBatch batch = firestore.batch();
        batch.delete(postRef);

        List<QueryDocumentSnapshot> comments = future.get().getDocuments();
        for (QueryDocumentSnapshot comment : comments) {
            batch.delete(comment.getReference());
        }
        batch.commit().get();

        return " All gone";
    }
}
