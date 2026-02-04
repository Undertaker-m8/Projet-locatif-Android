package com.example.miniprojet.firebase;

import android.util.Log;

import com.example.miniprojet.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserFirestoreHelper {
    private static final String TAG = "UserFirestoreHelper";
    private static final String COLLECTION_USERS = "users";

    private FirebaseFirestore db;
    private CollectionReference usersRef;

    public UserFirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection(COLLECTION_USERS);
    }

    // CREATE - Ajouter un utilisateur
    public void registerUser(User user, final UserCallback callback) {
        // Vérifier si l'email existe déjà
        usersRef.whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // Email n'existe pas, on peut créer l'utilisateur
                            createNewUser(user, callback);
                        } else {
                            // Email existe déjà
                            callback.onFailure(new Exception("Cet email est déjà utilisé"));
                        }
                    } else {
                        Log.e(TAG, "Erreur vérification email: ", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    private void createNewUser(User user, UserCallback callback) {
        DocumentReference newUserRef = usersRef.document();
        user.setId(newUserRef.getId());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("lastName", user.getLastName());
        userData.put("firstName", user.getFirstName());
        userData.put("email", user.getEmail());
        userData.put("password", user.getPassword());
        userData.put("phone", user.getPhone());
        userData.put("university", user.getUniversity());
        userData.put("profileImageUrl", user.getProfileImageUrl());
        userData.put("createdAt", FieldValue.serverTimestamp());
        userData.put("updatedAt", FieldValue.serverTimestamp());

        newUserRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Utilisateur créé: " + user.getEmail());
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur création utilisateur: ", e);
                    callback.onFailure(e);
                });
    }

    // READ - Récupérer un utilisateur par email (POUR LA CONNEXION)
    public void getUserByEmail(String email, UserCallback callback) {
        Log.d(TAG, " Recherche utilisateur avec email: " + email);

        usersRef.whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    Log.d(TAG, " Task successful: " + task.isSuccessful());

                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        Log.d(TAG, " Documents trouvés: " + querySnapshot.size());

                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            Log.d(TAG, " Document ID: " + document.getId());
                            Log.d(TAG, " Document data: " + document.getData());

                            User user = documentToUser(document);
                            callback.onSuccess(user);
                        } else {
                            Log.w(TAG, " Aucun document trouvé pour email: " + email);
                            callback.onFailure(new Exception("Aucun compte trouvé avec cet email"));
                        }
                    } else {
                        Log.e(TAG, " Erreur Firestore: ", task.getException());
                        callback.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Erreur récupération utilisateur: ", e);
                    callback.onFailure(e);
                });
    }

    // =========================================================================
    // NOUVELLES MÉTHODES POUR LA MODIFICATION DU PROFIL
    // =========================================================================

    // MÉTHODE POUR VÉRIFIER SI UN EMAIL EXISTE (pour éviter les doublons)
    public void checkEmailExists(String email, UserExistsCallback callback) {
        usersRef.whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean exists = !task.getResult().isEmpty();
                        callback.onCallback(exists);
                    } else {
                        Log.e(TAG, "Erreur vérification email: ", task.getException());
                        callback.onCallback(false);
                    }
                });
    }

    // MÉTHODE POUR METTRE À JOUR UN UTILISATEUR
    public void updateUser(String userId, Map<String, Object> updates, UpdateCallback callback) {
        // Ajouter la date de mise à jour
        updates.put("updatedAt", FieldValue.serverTimestamp());

        usersRef.document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Utilisateur mis à jour avec succès: " + userId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la mise à jour utilisateur: ", e);
                    callback.onFailure(e.getMessage());
                });
    }

    // MÉTHODE POUR RÉCUPÉRER LES STATISTIQUES (optionnel)
    public void getUserStatistics(String userId, StatisticsCallback callback) {
        // Pour l'instant, retournons des valeurs par défaut
        // Vous pourrez implémenter la logique réelle plus tard
        callback.onStatisticsLoaded(0, 0, 5.0);
    }

    // =========================================================================
    // MÉTHODE UTILITAIRE
    // =========================================================================

    // Méthode utilitaire pour convertir DocumentSnapshot en User
    private User documentToUser(DocumentSnapshot document) {
        User user = new User();

        // IMPORTANT: Utiliser document.getId() pour l'ID du document
        user.setId(document.getId());

        user.setLastName(document.getString("lastName"));
        user.setFirstName(document.getString("firstName"));
        user.setEmail(document.getString("email"));
        user.setPassword(document.getString("password"));
        user.setPhone(document.getString("phone"));
        user.setUniversity(document.getString("university"));
        user.setProfileImageUrl(document.getString("profileImageUrl"));

        // Gestion des timestamps
        var createdAt = document.getTimestamp("createdAt");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toDate());
        } else {
            user.setCreatedAt(new Date());
        }

        var updatedAt = document.getTimestamp("updatedAt");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toDate());
        } else {
            user.setUpdatedAt(new Date());
        }

        // Log pour débogage
        Log.d(TAG, "🔄 DocumentToUser - Email: " + user.getEmail() +
                ", ID: " + user.getId() +
                ", Password hash: " + (user.getPassword() != null ? "OUI" : "NON"));

        return user;
    }

    // =========================================================================
    // INTERFACES DE CALLBACK
    // =========================================================================

    // Interface pour les opérations utilisateur (EXISTANT)
    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    // Interface pour vérifier si un email existe (NOUVELLE)
    public interface UserExistsCallback {
        void onCallback(boolean exists);
    }



    // Interface pour la mise à jour
    public interface UpdateCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Interface pour les statistiques
    public interface StatisticsCallback {
        void onStatisticsLoaded(int adsCount, int favoritesCount, double rating);
        void onError(String error);
    }
}
