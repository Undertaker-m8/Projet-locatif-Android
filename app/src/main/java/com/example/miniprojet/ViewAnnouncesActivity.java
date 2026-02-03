package com.example.miniprojet;

import android.content.Intent;
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

public class ViewAnnouncesActivity extends AppCompatActivity
        implements AdvertisementAdapter.OnAdvertisementClickListener {

    private RecyclerView recyclerView;
    private AdvertisementAdapter adapter;
    private List<Advertisement> advertisements = new ArrayList<>();
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_announces);

        sessionManager = new SessionManager(this);
        initViews();
        setupRecyclerView();
        loadUserAdvertisements();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdvertisementAdapter(this, advertisements, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadUserAdvertisements() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Veuillez vous connecter", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        AdvertisementFirestoreHelper firestoreHelper = new AdvertisementFirestoreHelper();
        firestoreHelper.getUserAdvertisements(userId, new AdvertisementFirestoreHelper.AdvertisementsCallback() {
            @Override
            public void onSuccess(List<Advertisement> ads) {
                advertisements.clear();
                advertisements.addAll(ads);
                adapter.updateData(advertisements);

                if (ads.isEmpty()) {
                    Toast.makeText(ViewAnnouncesActivity.this,
                            "Vous n'avez pas encore créé d'annonces", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ViewAnnouncesActivity.this,
                        "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAdvertisementClick(Advertisement advertisement) {
        // CORRECTION ICI : Ouvrir EditAdvertisementActivity avec un Intent
        Intent intent = new Intent(ViewAnnouncesActivity.this, EditAdvertisementActivity.class);
        intent.putExtra("ADVERTISEMENT_ID", advertisement.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Advertisement advertisement) {
        // Pour les annonces de l'utilisateur
        Toast.makeText(this, "C'est votre propre annonce", Toast.LENGTH_SHORT).show();
    }
}