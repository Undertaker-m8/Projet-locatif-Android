package com.example.miniprojet.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class RatingSystem {
    
    private FirebaseFirestore db;
    
    public RatingSystem() {
        db = FirebaseFirestore.getInstance();
    }
    
    public interface RatingCallback {
        void onSuccess(double newAverage);
        void onFailure(Exception e);
    }
    
    public void addRating(String advertisementId, String userId, float rating, String comment, RatingCallback callback) {
        DocumentReference adRef = db.collection("advertisements").document(advertisementId);
        DocumentReference ratingRef = db.collection("ratings").document(advertisementId + "_" + userId);
        
        // Check if user already rated
        ratingRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Update existing rating
                    updateRating(ratingRef, adRef, advertisementId, userId, rating, comment, callback);
                } else {
                    // Add new rating
                    addNewRating(ratingRef, adRef, advertisementId, userId, rating, comment, callback);
                }
            } else {
                callback.onFailure(task.getException());
            }
        });
    }
    
    private void addNewRating(DocumentReference ratingRef, DocumentReference adRef, 
                             String adId, String userId, float rating, String comment, RatingCallback callback) {
        // Create rating document
        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("advertisementId", adId);
        ratingData.put("userId", userId);
        ratingData.put("rating", rating);
        ratingData.put("comment", comment);
        ratingData.put("createdAt", FieldValue.serverTimestamp());
        
        ratingRef.set(ratingData).addOnSuccessListener(aVoid -> {
            // Update advertisement's average rating
            updateAdvertisementRating(adRef, rating, true, callback);
        }).addOnFailureListener(callback::onFailure);
    }
    
    private void updateAdvertisementRating(DocumentReference adRef, float rating, 
                                          boolean isNewRating, RatingCallback callback) {
        adRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                double currentAverage = documentSnapshot.getDouble("averageRating");
                long ratingCount = documentSnapshot.getLong("ratingCount");
                
                if (isNewRating) {
                    // New rating
                    double newAverage = ((currentAverage * ratingCount) + rating) / (ratingCount + 1);
                    updateAdFields(adRef, newAverage, ratingCount + 1, callback);
                } else {
                    // Update existing rating
                    double newAverage = ((currentAverage * ratingCount) - currentAverage + rating) / ratingCount;
                    updateAdFields(adRef, newAverage, ratingCount, callback);
                }
            }
        }).addOnFailureListener(callback::onFailure);
    }
    
    private void updateAdFields(DocumentReference adRef, double newAverage, long newCount, RatingCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("averageRating", newAverage);
        updates.put("ratingCount", newCount);
        updates.put("updatedAt", FieldValue.serverTimestamp());
        
        adRef.update(updates).addOnSuccessListener(aVoid -> {
            callback.onSuccess(newAverage);
        }).addOnFailureListener(callback::onFailure);
    }
    
    public void getAdvertisementRatings(String advertisementId, RatingsListCallback callback) {
        db.collection("ratings")
                .whereEqualTo("advertisementId", advertisementId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots);
                })
                .addOnFailureListener(callback::onFailure);
    }
    
    public interface RatingsListCallback {
        void onSuccess(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots);
        void onFailure(Exception e);
    }
    // Dans RatingSystem.java, ajoutez cette méthode :
    private void updateRating(DocumentReference ratingRef, DocumentReference adRef,
                              String adId, String userId, float rating,
                              String comment, RatingCallback callback) {
        // Mettre à jour le rating existant
        Map<String, Object> updates = new HashMap<>();
        updates.put("rating", rating);
        updates.put("comment", comment);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        ratingRef.update(updates).addOnSuccessListener(aVoid -> {
            // Mettre à jour la moyenne de l'annonce
            updateAdvertisementRating(adRef, rating, false, callback);
        }).addOnFailureListener(callback::onFailure);
    }
}