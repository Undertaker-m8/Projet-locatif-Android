package com.example.miniprojet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        initViews();
        updateWelcomeMessage();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);

        Button btnCreateAd = findViewById(R.id.btn_create_ad);
        Button btnMap = findViewById(R.id.btn_map);
        Button btnViewAds = findViewById(R.id.btn_view_ads);
        Button btnMyAds = findViewById(R.id.btn_my_ads);
        Button btnFavorites = findViewById(R.id.btn_favorites);
        Button btnProfile = findViewById(R.id.btn_profile); // AJOUTÉ
        Button btnLogout = findViewById(R.id.btn_logout);

        btnCreateAd.setOnClickListener(v -> createAd());
        btnMap.setOnClickListener(v -> openMap());
        btnViewAds.setOnClickListener(v -> viewAllAds());
        btnMyAds.setOnClickListener(v -> viewMyAds());
        btnFavorites.setOnClickListener(v -> viewFavorites());
        btnProfile.setOnClickListener(v -> openProfile()); // AJOUTÉ
        btnLogout.setOnClickListener(v -> logout());
    }

    private void updateWelcomeMessage() {
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Bonjour, " + userName + " !");
        } else {
            tvWelcome.setText("Bienvenue !");
        }
    }

    private void createAd() {
        Intent intent = new Intent(this, CreateAnnouncesActivity.class);
        startActivity(intent);
    }

    private void openMap() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void viewAllAds() {
        Intent intent = new Intent(this, AllAnnouncesActivity.class);
        startActivity(intent);
    }

    private void viewMyAds() {
        Intent intent = new Intent(this, ViewAnnouncesActivity.class);
        startActivity(intent);
    }

    private void viewFavorites() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
    }

    // AJOUTÉ : Méthode pour ouvrir le profil
    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isLoggedIn()) {
            updateWelcomeMessage();
        }
    }
}