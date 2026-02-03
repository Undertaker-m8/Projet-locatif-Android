package com.example.miniprojet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.miniprojet.firebase.AdvertisementFirestoreHelper;
import com.example.miniprojet.firebase.UserFirestoreHelper;
import com.example.miniprojet.model.User;
import com.example.miniprojet.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private static final int EDIT_PROFILE_REQUEST = 1001;

    private SessionManager sessionManager;
    private UserFirestoreHelper userFirestoreHelper;
    private AdvertisementFirestoreHelper adFirestoreHelper;

    private ImageView ivProfile;
    private TextView tvName, tvEmail, tvPhone, tvUniversity, tvJoinDate;
    private TextView tvAdsCount, tvFavoritesCount, tvRating;
    private Button btnEditProfile, btnChangePassword, btnMyAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        userFirestoreHelper = new UserFirestoreHelper();
        adFirestoreHelper = new AdvertisementFirestoreHelper();

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mon profil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        loadUserProfile();
        setupListeners();
    }

    private void initViews() {
        ivProfile = findViewById(R.id.iv_profile);
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvUniversity = findViewById(R.id.tv_university);
        tvJoinDate = findViewById(R.id.tv_join_date);
        tvAdsCount = findViewById(R.id.tv_ads_count);
        tvFavoritesCount = findViewById(R.id.tv_favorites_count);
        tvRating = findViewById(R.id.tv_rating);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnMyAds = findViewById(R.id.btn_my_ads);
    }

    private void loadUserProfile() {
        User user = sessionManager.getCurrentUser();
        if (user != null) {
            // Charger la photo de profil
            String profileImageUrl = user.getProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_person)
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_person);
            }

            // Mettre à jour les informations textuelles
            tvName.setText(user.getFirstName() + " " + user.getLastName());
            tvEmail.setText(user.getEmail());

            // Gérer les champs optionnels
            String phone = user.getPhone();
            tvPhone.setText(phone != null && !phone.trim().isEmpty() ? phone : "Non renseigné");

            String university = user.getUniversity();
            tvUniversity.setText(university != null && !university.trim().isEmpty() ? university : "Non renseigné");

            // Formater la date d'inscription
            if (user.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH);
                tvJoinDate.setText("Membre depuis " + sdf.format(user.getCreatedAt()));
            } else {
                tvJoinDate.setText("Membre depuis date inconnue");
            }

            // Mettre à jour les statistiques RÉELLES
            updateRealStatistics();
        } else {
            // Si aucun utilisateur n'est trouvé, rediriger vers le login
            redirectToLogin();
        }
    }

    private void updateRealStatistics() {
        String userId = sessionManager.getUserId();

        // 1. Compter les annonces de l'utilisateur
        adFirestoreHelper.getUserAdvertisements(userId, new AdvertisementFirestoreHelper.AdvertisementsCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.miniprojet.model.Advertisement> ads) {
                tvAdsCount.setText(String.valueOf(ads.size()));
            }

            @Override
            public void onFailure(Exception e) {
                tvAdsCount.setText("0");
            }
        });

        // 2. Compter les favoris de l'utilisateur
        adFirestoreHelper.getUserFavorites(userId, new AdvertisementFirestoreHelper.AdvertisementsCallback() {
            @Override
            public void onSuccess(java.util.List<com.example.miniprojet.model.Advertisement> favorites) {
                tvFavoritesCount.setText(String.valueOf(favorites.size()));
            }

            @Override
            public void onFailure(Exception e) {
                tvFavoritesCount.setText("0");
            }
        });

        // 3. Pour la note moyenne (à implémenter plus tard)
        tvRating.setText("5.0");
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnMyAds.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ViewAnnouncesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            // Rafraîchir les données du profil
            loadUserProfile();
            Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rafraîchir les données à chaque retour sur l'écran
        loadUserProfile();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}