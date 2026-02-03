package com.example.miniprojet.firebase;

import android.util.Log;

import com.example.miniprojet.model.Advertisement;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvertisementFirestoreHelper {
    private static final String TAG = "AdFirestoreHelper";
    private static final String COLLECTION_ADS = "advertisements";
    private static final String COLLECTION_FAVORITES = "favorites";

    private FirebaseFirestore db;
    private CollectionReference adsRef;
    private CollectionReference favoritesRef;

    public AdvertisementFirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        adsRef = db.collection(COLLECTION_ADS);
        favoritesRef = db.collection(COLLECTION_FAVORITES);

        // Active la persistance (si ce n'est pas déjà fait)
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
    }

    // CREATE - Créer une annonce
    public void createAdvertisement(Advertisement ad, final AdvertisementCallback callback) {
        DocumentReference newAdRef = adsRef.document();
        ad.setId(newAdRef.getId());

        Map<String, Object> adData = new HashMap<>();
        adData.put("id", ad.getId());
        adData.put("userId", ad.getUserId());
        adData.put("userEmail", ad.getUserEmail());
        adData.put("userName", ad.getUserName());
        adData.put("title", ad.getTitle());
        adData.put("description", ad.getDescription());
        adData.put("price", ad.getPrice());
        adData.put("surface", ad.getSurface());
        adData.put("rooms", ad.getRooms());
        adData.put("address", ad.getAddress());
        adData.put("city", ad.getCity());
        adData.put("latitude", ad.getLatitude());
        adData.put("longitude", ad.getLongitude());
        adData.put("amenities", ad.getAmenities());
        adData.put("hasParking", ad.isHasParking());
        adData.put("hasElevator", ad.isHasElevator());
        adData.put("hasBalcony", ad.isHasBalcony());
        adData.put("imageUrls", ad.getImageUrls());
        adData.put("rating", ad.getRating());
        adData.put("reviewCount", ad.getReviewCount());
        adData.put("createdAt", FieldValue.serverTimestamp());
        adData.put("updatedAt", FieldValue.serverTimestamp());

        Log.d(TAG, "📤 Envoi à Firestore: " + adData);

        newAdRef.set(adData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, " Annonce créée dans Firestore: " + ad.getId());
                    callback.onSuccess(ad);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, " Erreur création annonce: ", e);
                    callback.onFailure(e);
                });
    }

    // MÉTHODE UTILITAIRE POUR SERVEUR + CACHE FALLBACK
    private Task<QuerySnapshot> getWithFallback(Query query) {
        return query.get(Source.SERVER)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return task;
                    } else {
                        Log.w(TAG, "⚠️ Serveur échoué, tentative depuis le cache...", task.getException());
                        return query.get(Source.CACHE);
                    }
                });
    }

    // READ - Récupérer toutes les annonces (AVEC FALLBACK)
    public void getAllAdvertisements(AdvertisementsCallback callback) {
        getWithFallback(adsRef)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Advertisement> advertisements = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Advertisement ad = documentToAdvertisement(document);
                        advertisements.add(ad);
                    }

                    String source = queryDocumentSnapshots.getMetadata().isFromCache()
                            ? "CACHE" : "SERVEUR";
                    Log.d(TAG, "📥 " + advertisements.size() + " annonces récupérées depuis " + source);

                    callback.onSuccess(advertisements);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Échec total serveur + cache", e);
                    callback.onFailure(e);
                });
    }

    // READ - Récupérer les annonces d'un utilisateur (AVEC FALLBACK)
    public void getUserAdvertisements(String userId, AdvertisementsCallback callback) {
        Query query = adsRef.whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        getWithFallback(query)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Advertisement> ads = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ads.add(documentToAdvertisement(doc));
                    }

                    String source = queryDocumentSnapshots.getMetadata().isFromCache()
                            ? "CACHE" : "SERVEUR";
                    Log.d(TAG, "👤 " + ads.size() + " annonces utilisateur depuis " + source);

                    callback.onSuccess(ads);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur récupération annonces utilisateur", e);
                    callback.onFailure(e);
                });
    }

    // READ - Récupérer une annonce par ID (AVEC FALLBACK)
    public void getAdvertisementById(String adId, AdvertisementCallback callback) {
        DocumentReference docRef = adsRef.document(adId);

        // Tentative serveur d'abord
        docRef.get(Source.SERVER)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "✅ Annonce " + adId + " récupérée depuis SERVEUR");
                        callback.onSuccess(documentToAdvertisement(documentSnapshot));
                    } else {
                        // Essai depuis le cache si non trouvé sur serveur
                        docRef.get(Source.CACHE)
                                .addOnSuccessListener(cacheSnapshot -> {
                                    if (cacheSnapshot.exists()) {
                                        Log.d(TAG, "✅ Annonce " + adId + " récupérée depuis CACHE");
                                        callback.onSuccess(documentToAdvertisement(cacheSnapshot));
                                    } else {
                                        callback.onFailure(new Exception("Annonce non trouvée (serveur ni cache)"));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    callback.onFailure(new Exception("Annonce non trouvée: " + e.getMessage()));
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "⚠️ Serveur échoué pour annonce " + adId + ", essai cache...", e);

                    // Fallback vers le cache
                    docRef.get(Source.CACHE)
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Log.d(TAG, "✅ Annonce " + adId + " récupérée depuis CACHE (fallback)");
                                    callback.onSuccess(documentToAdvertisement(documentSnapshot));
                                } else {
                                    callback.onFailure(new Exception("Annonce non trouvée dans le cache"));
                                }
                            })
                            .addOnFailureListener(cacheError -> {
                                callback.onFailure(new Exception("Échec serveur et cache: " + cacheError.getMessage()));
                            });
                });
    }

    // UPDATE - Mettre à jour une annonce
    public void updateAdvertisement(Advertisement ad, AdvertisementCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", ad.getTitle());
        updates.put("description", ad.getDescription());
        updates.put("price", ad.getPrice());
        updates.put("surface", ad.getSurface());
        updates.put("rooms", ad.getRooms());
        updates.put("address", ad.getAddress());
        updates.put("city", ad.getCity());
        updates.put("latitude", ad.getLatitude());
        updates.put("longitude", ad.getLongitude());
        updates.put("amenities", ad.getAmenities());
        updates.put("hasParking", ad.isHasParking());
        updates.put("hasElevator", ad.isHasElevator());
        updates.put("hasBalcony", ad.isHasBalcony());
        updates.put("imageUrls", ad.getImageUrls());
        updates.put("updatedAt", FieldValue.serverTimestamp());

        adsRef.document(ad.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Annonce " + ad.getId() + " mise à jour");
                    callback.onSuccess(ad);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Erreur mise à jour annonce " + ad.getId(), e);
                    callback.onFailure(e);
                });
    }

    // DELETE - Supprimer une annonce
    public void deleteAdvertisement(String adId, AdvertisementCallback callback) {
        adsRef.document(adId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Annonce " + adId + " supprimée");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Erreur suppression annonce " + adId, e);
                    callback.onFailure(e);
                });
    }

    // RECHERCHE - Annonces par ville (AVEC FALLBACK)
    public void searchByCity(String city, AdvertisementsCallback callback) {
        Query query = adsRef.whereEqualTo("city", city)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        getWithFallback(query)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Advertisement> ads = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ads.add(documentToAdvertisement(doc));
                    }

                    String source = queryDocumentSnapshots.getMetadata().isFromCache()
                            ? "CACHE" : "SERVEUR";
                    Log.d(TAG, "🏙️ " + ads.size() + " annonces à " + city + " depuis " + source);

                    callback.onSuccess(ads);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur recherche par ville: " + city, e);
                    callback.onFailure(e);
                });
    }

    // RECHERCHE - Annonces par prix maximum (AVEC FALLBACK)
    public void searchByMaxPrice(double maxPrice, AdvertisementsCallback callback) {
        Query query = adsRef.whereLessThanOrEqualTo("price", maxPrice)
                .orderBy("price", Query.Direction.DESCENDING);

        getWithFallback(query)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Advertisement> ads = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ads.add(documentToAdvertisement(doc));
                    }

                    String source = queryDocumentSnapshots.getMetadata().isFromCache()
                            ? "CACHE" : "SERVEUR";
                    Log.d(TAG, "💰 " + ads.size() + " annonces < " + maxPrice + " depuis " + source);

                    callback.onSuccess(ads);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur recherche par prix: " + maxPrice, e);
                    callback.onFailure(e);
                });
    }

    // FAVORIS - Ajouter aux favoris
    public void addToFavorites(String userId, String adId, AdvertisementCallback callback) {
        Map<String, Object> favoriteData = new HashMap<>();
        favoriteData.put("userId", userId);
        favoriteData.put("adId", adId);
        favoriteData.put("addedAt", FieldValue.serverTimestamp());

        favoritesRef.document(userId + "_" + adId)
                .set(favoriteData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "⭐ Favori ajouté: " + userId + " -> " + adId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Erreur ajout favori", e);
                    callback.onFailure(e);
                });
    }

    // FAVORIS - Retirer des favoris
    public void removeFromFavorites(String userId, String adId, AdvertisementCallback callback) {
        favoritesRef.document(userId + "_" + adId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "🗑️ Favori supprimé: " + userId + " -> " + adId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Erreur suppression favori", e);
                    callback.onFailure(e);
                });
    }

    // FAVORIS - Récupérer les favoris d'un utilisateur (AVEC FALLBACK)
    public void getUserFavorites(String userId, AdvertisementsCallback callback) {
        Query query = favoritesRef.whereEqualTo("userId", userId);

        getWithFallback(query)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> adIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        adIds.add(doc.getString("adId"));
                    }

                    if (!adIds.isEmpty()) {
                        getAdvertisementsByIds(adIds, callback);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur récupération favoris", e);
                    callback.onFailure(e);
                });
    }

    // Méthode utilitaire pour convertir DocumentSnapshot en Advertisement
    private Advertisement documentToAdvertisement(DocumentSnapshot document) {
        Advertisement ad = new Advertisement();
        ad.setId(document.getId());
        ad.setUserId(document.getString("userId"));
        ad.setUserEmail(document.getString("userEmail"));
        ad.setUserName(document.getString("userName"));
        ad.setTitle(document.getString("title"));
        ad.setDescription(document.getString("description"));

        Double price = document.getDouble("price");
        if (price != null) ad.setPrice(price);

        Double surface = document.getDouble("surface");
        if (surface != null) ad.setSurface(surface);

        Long rooms = document.getLong("rooms");
        if (rooms != null) ad.setRooms(rooms.intValue());

        ad.setAddress(document.getString("address"));
        ad.setCity(document.getString("city"));

        Double latitude = document.getDouble("latitude");
        Double longitude = document.getDouble("longitude");
        if (latitude != null && longitude != null) {
            ad.setLatitude(latitude);
            ad.setLongitude(longitude);
        }

        List<String> amenities = (List<String>) document.get("amenities");
        if (amenities != null) ad.setAmenities(amenities);

        Boolean hasParking = document.getBoolean("hasParking");
        if (hasParking != null) ad.setHasParking(hasParking);

        Boolean hasElevator = document.getBoolean("hasElevator");
        if (hasElevator != null) ad.setHasElevator(hasElevator);

        Boolean hasBalcony = document.getBoolean("hasBalcony");
        if (hasBalcony != null) ad.setHasBalcony(hasBalcony);

        List<String> imageUrls = (List<String>) document.get("imageUrls");
        if (imageUrls != null) ad.setImageUrls(imageUrls);

        Double rating = document.getDouble("rating");
        if (rating != null) ad.setRating(rating);

        Long reviewCount = document.getLong("reviewCount");
        if (reviewCount != null) ad.setReviewCount(reviewCount.intValue());

        var createdAt = document.getTimestamp("createdAt");
        if (createdAt != null) {
            ad.setCreatedAt(createdAt.toDate());
        }

        var updatedAt = document.getTimestamp("updatedAt");
        if (updatedAt != null) {
            ad.setUpdatedAt(updatedAt.toDate());
        }

        return ad;
    }

    // Méthode privée pour récupérer plusieurs annonces par IDs (AVEC FALLBACK)
    private void getAdvertisementsByIds(List<String> adIds, AdvertisementsCallback callback) {
        List<Advertisement> ads = new ArrayList<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (String adId : adIds) {
            DocumentReference docRef = adsRef.document(adId);

            // Créer une tâche avec fallback pour chaque document
            Task<DocumentSnapshot> task = docRef.get(Source.SERVER)
                    .continueWithTask(serverTask -> {
                        if (serverTask.isSuccessful()) {
                            return serverTask;
                        } else {
                            return docRef.get(Source.CACHE);
                        }
                    });

            tasks.add(task);

            task.addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ads.add(documentToAdvertisement(documentSnapshot));
                        }

                        // Vérifier si toutes les tâches sont terminées
                        if (ads.size() == adIds.size()) {
                            Log.d(TAG, "✅ Toutes les annonces favorites récupérées");
                            callback.onSuccess(ads);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Erreur récupération annonce " + adId, e);

                        // Continuer même si certaines échouent
                        if (ads.size() == tasks.size()) {
                            callback.onSuccess(ads);
                        }
                    });
        }

        // Cas spécial: liste vide
        if (adIds.isEmpty()) {
            callback.onSuccess(ads);
        }
    }

    // MÉTHODE AJOUTÉE : Nettoyer le cache Firebase
    public void clearFirebaseCache(final CacheClearCallback callback) {
        db.clearPersistence()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "✅ Cache Firebase nettoyé");
                        callback.onCacheCleared();
                    } else {
                        Log.e(TAG, "❌ Erreur nettoyage cache", task.getException());
                        callback.onCacheClearError(task.getException());
                    }
                });
    }

    // MÉTHODE AJOUTÉE : Tester la connexion Firestore
    public void testFirestoreConnection(final TestConnectionCallback callback) {
        // Tente de récupérer un document factice pour tester la connexion
        adsRef.limit(1).get(Source.SERVER)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "✅ Connexion Firestore SERVEUR OK");
                    callback.onConnectionTested(true, "Serveur", null);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "⚠️ Connexion serveur échouée, test cache...", e);

                    adsRef.limit(1).get(Source.CACHE)
                            .addOnSuccessListener(cacheSnapshots -> {
                                Log.d(TAG, "✅ Connexion Firestore CACHE OK");
                                callback.onConnectionTested(true, "Cache", null);
                            })
                            .addOnFailureListener(cacheError -> {
                                Log.e(TAG, "❌ Connexion Firestore totale échouée");
                                callback.onConnectionTested(false, "Aucune", cacheError);
                            });
                });
    }

    // Interfaces de callback
    public interface AdvertisementCallback {
        void onSuccess(Advertisement ad);
        void onFailure(Exception e);
    }

    public interface AdvertisementsCallback {
        void onSuccess(List<Advertisement> ads);
        void onFailure(Exception e);
    }

    public interface CacheClearCallback {
        void onCacheCleared();
        void onCacheClearError(Exception e);
    }

    public interface TestConnectionCallback {
        void onConnectionTested(boolean success, String source, Exception error);
    }
}