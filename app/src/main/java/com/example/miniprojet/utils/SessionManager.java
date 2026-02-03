package com.example.miniprojet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.miniprojet.model.User;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    // Clés pour les champs utilisateur
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_LAST_NAME = "lastName";
    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_UNIVERSITY = "university";
    private static final String KEY_PROFILE_IMAGE = "profileImageUrl";
    private static final String KEY_CREATED_AT = "createdAt";
    private static final String KEY_UPDATED_AT = "updatedAt";
    private static final String KEY_AD_COUNT = "advertisementCount";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private SimpleDateFormat dateFormat;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
    }

    // Créer une session de connexion
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        saveUserData(user);
        editor.apply(); // Utiliser apply() pour asynchrone
    }

    // Sauvegarder toutes les données utilisateur
    private void saveUserData(User user) {
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_LAST_NAME, user.getLastName());
        editor.putString(KEY_FIRST_NAME, user.getFirstName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.putString(KEY_PHONE, user.getPhone() != null ? user.getPhone() : "");
        editor.putString(KEY_UNIVERSITY, user.getUniversity() != null ? user.getUniversity() : "");
        editor.putString(KEY_PROFILE_IMAGE, user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
        editor.putInt(KEY_AD_COUNT, user.getAdvertisementCount());

        // Sauvegarder les dates
        if (user.getCreatedAt() != null) {
            editor.putString(KEY_CREATED_AT, dateFormat.format(user.getCreatedAt()));
        } else {
            editor.putString(KEY_CREATED_AT, dateFormat.format(new Date()));
        }

        if (user.getUpdatedAt() != null) {
            editor.putString(KEY_UPDATED_AT, dateFormat.format(user.getUpdatedAt()));
        } else {
            editor.putString(KEY_UPDATED_AT, dateFormat.format(new Date()));
        }
    }

    // Récupérer l'utilisateur courant
    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();
        user.setId(pref.getString(KEY_USER_ID, ""));
        user.setLastName(pref.getString(KEY_LAST_NAME, ""));
        user.setFirstName(pref.getString(KEY_FIRST_NAME, ""));
        user.setEmail(pref.getString(KEY_EMAIL, ""));
        user.setPassword(pref.getString(KEY_PASSWORD, ""));
        user.setPhone(pref.getString(KEY_PHONE, ""));
        user.setUniversity(pref.getString(KEY_UNIVERSITY, ""));
        user.setProfileImageUrl(pref.getString(KEY_PROFILE_IMAGE, ""));
        user.setAdvertisementCount(pref.getInt(KEY_AD_COUNT, 0));

        // Récupérer les dates
        try {
            String createdAtStr = pref.getString(KEY_CREATED_AT, "");
            if (!createdAtStr.isEmpty()) {
                user.setCreatedAt(dateFormat.parse(createdAtStr));
            } else {
                user.setCreatedAt(new Date());
            }

            String updatedAtStr = pref.getString(KEY_UPDATED_AT, "");
            if (!updatedAtStr.isEmpty()) {
                user.setUpdatedAt(dateFormat.parse(updatedAtStr));
            } else {
                user.setUpdatedAt(new Date());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // Dates par défaut en cas d'erreur
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());
        }

        return user;
    }

    // Sauvegarder un utilisateur (pour les mises à jour)
    public void saveUser(User user) {
        saveUserData(user);
        editor.apply();
    }
    public void logout() {
        logoutUser(); // Appelle la méthode existante
    }
    // Mettre à jour seulement certaines informations
    public void updateUserInfo(String firstName, String lastName, String email,
                               String phone, String university, String profileImageUrl) {
        User user = getCurrentUser();
        if (user != null) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setUniversity(university);
            user.setProfileImageUrl(profileImageUrl);
            user.setUpdatedAt(new Date());

            saveUser(user);
        }
    }

    // Vérifier si l'utilisateur est connecté
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Déconnexion
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    // Méthodes utilitaires
    public String getUserFullName() {
        User user = getCurrentUser();
        return user != null ? user.getFullName() : "";
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getUserFirstName() {
        return pref.getString(KEY_FIRST_NAME, "");
    }

    public String getUserLastName() {
        return pref.getString(KEY_LAST_NAME, "");
    }

    public String getProfileImageUrl() {
        return pref.getString(KEY_PROFILE_IMAGE, "");
    }

    public Date getUserCreatedAt() {
        try {
            String createdAtStr = pref.getString(KEY_CREATED_AT, "");
            if (!createdAtStr.isEmpty()) {
                return dateFormat.parse(createdAtStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    // Pour la compatibilité avec l'ancien code
    public String getUserName() {
        return getUserFullName();
    }
}