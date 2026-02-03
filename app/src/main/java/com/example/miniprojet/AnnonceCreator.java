package com.example.miniprojet;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class AnnonceCreator {
    
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Activity activity;
    
    public AnnonceCreator(Activity activity) {
        this.activity = activity;
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }
    
    public void addAnnonceToFirestore(String title, String description, double price, 
                                     double surface, int rooms, String address, 
                                     String city, String mainAmenity, 
                                     boolean isMeuble, boolean hasParking,
                                     boolean hasAscenseur, boolean hasBalcon,
                                     Uri imageUri) {
        
        // Si une image est sélectionnée, uploader d'abord
        if (imageUri != null) {
            uploadImageAndSaveAnnonce(imageUri, title, description, price, surface, 
                                     rooms, address, city, mainAmenity, 
                                     isMeuble, hasParking, hasAscenseur, hasBalcon);
        } else {
            saveAnnonceToFirestore("", title, description, price, surface, rooms, 
                                 address, city, mainAmenity, 
                                 isMeuble, hasParking, hasAscenseur, hasBalcon);
        }
    }
    
    private void uploadImageAndSaveAnnonce(Uri imageUri, String title, String description, 
                                          double price, double surface, int rooms, 
                                          String address, String city, String mainAmenity,
                                          boolean isMeuble, boolean hasParking,
                                          boolean hasAscenseur, boolean hasBalcon) {
        
        StorageReference storageRef = storage.getReference();
        String fileName = "annonces/" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);
        
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        saveAnnonceToFirestore(imageUrl, title, description, price, 
                                             surface, rooms, address, city, mainAmenity,
                                             isMeuble, hasParking, hasAscenseur, hasBalcon);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "Erreur upload image: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }
    
    private void saveAnnonceToFirestore(String imageUrl, String title, String description, 
                                       double price, double surface, int rooms, 
                                       String address, String city, String mainAmenity,
                                       boolean isMeuble, boolean hasParking,
                                       boolean hasAscenseur, boolean hasBalcon) {
        
        Map<String, Object> annonce = new HashMap<>();
        annonce.put("title", title);
        annonce.put("description", description);
        annonce.put("price", price);
        annonce.put("surface", surface);
        annonce.put("rooms", rooms);
        annonce.put("address", address);
        annonce.put("city", city);
        annonce.put("fullAddress", address + ", " + city);
        annonce.put("mainAmenity", mainAmenity);
        annonce.put("isMeuble", isMeuble);
        annonce.put("hasParking", hasParking);
        annonce.put("hasAscenseur", hasAscenseur);
        annonce.put("hasBalcon", hasBalcon);
        annonce.put("datePublishing", Timestamp.now());
        annonce.put("imageUrl", imageUrl);
        
        db.collection("annonces")
                .add(annonce)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(activity, "✅ Annonce créée avec succès!", 
                                 Toast.LENGTH_SHORT).show();
                    activity.finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "❌ Erreur: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
    }
}