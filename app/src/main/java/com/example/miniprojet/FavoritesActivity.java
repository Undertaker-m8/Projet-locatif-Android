package com.example.miniprojet;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniprojet.firebase.AdvertisementFirestoreHelper;
import com.example.miniprojet.model.Advertisement;
import com.example.miniprojet.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity
        implements AdvertisementAdapter.OnAdvertisementClickListener {

    private RecyclerView recyclerView;
    private AdvertisementAdapter adapter;
    private List<Advertisement> favoriteAdvertisements = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        sessionManager = new SessionManager(this);
        initViews();
        setupRecyclerView();
        loadFavorites();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // CORRECTION ICI : Constructeur avec 3 paramètres
        adapter = new AdvertisementAdapter(this, favoriteAdvertisements, this);

        // CORRECTION ICI : Utilisez setFavoriteMode au lieu du constructeur
        adapter.setFavoriteMode(true);

        recyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AdvertisementFirestoreHelper firestoreHelper = new AdvertisementFirestoreHelper();
        firestoreHelper.getUserFavorites(userId, new AdvertisementFirestoreHelper.AdvertisementsCallback() {
            @Override
            public void onSuccess(List<Advertisement> ads) {
                favoriteAdvertisements.clear();
                favoriteAdvertisements.addAll(ads);
                adapter.updateData(favoriteAdvertisements);

                if (ads.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this,
                            "Aucun favori pour le moment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(FavoritesActivity.this,
                        "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAdvertisementClick(Advertisement advertisement) {
        // Ouvrir les détails de l'annonce
        AdvertisementDetailActivity.start(this, advertisement.getId());
    }

    @Override
    public void onFavoriteClick(Advertisement advertisement) {
        // Retirer des favoris
        String userId = sessionManager.getUserId();
        AdvertisementFirestoreHelper firestoreHelper = new AdvertisementFirestoreHelper();

        firestoreHelper.removeFromFavorites(userId, advertisement.getId(),
                new AdvertisementFirestoreHelper.AdvertisementCallback() {
                    @Override
                    public void onSuccess(Advertisement ad) {
                        Toast.makeText(FavoritesActivity.this,
                                "Retiré des favoris", Toast.LENGTH_SHORT).show();
                        // Recharger les favoris
                        loadFavorites();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(FavoritesActivity.this,
                                "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}