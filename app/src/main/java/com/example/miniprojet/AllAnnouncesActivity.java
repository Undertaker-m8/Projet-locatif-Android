package com.example.miniprojet;

import android.os.Bundle;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // IMPORT CORRECT
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miniprojet.firebase.AdvertisementFirestoreHelper;
import com.example.miniprojet.model.Advertisement;

import java.util.ArrayList;
import java.util.List;

public class AllAnnouncesActivity extends AppCompatActivity
        implements AdvertisementAdapter.OnAdvertisementClickListener {

    private RecyclerView recyclerView;
    private AdvertisementAdapter adapter;
    private List<Advertisement> advertisements = new ArrayList<>();
    private SearchView searchView; // CORRECT : androidx.appcompat.widget.SearchView
    private Spinner spinnerCityFilter, spinnerPriceFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_announces);

        initViews();
        setupRecyclerView();
        loadAdvertisements();
        setupFilters();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        spinnerCityFilter = findViewById(R.id.spinner_city_filter);
        spinnerPriceFilter = findViewById(R.id.spinner_price_filter);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdvertisementAdapter(this, advertisements, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadAdvertisements() {
        AdvertisementFirestoreHelper firestoreHelper = new AdvertisementFirestoreHelper();
        firestoreHelper.getAllAdvertisements(new AdvertisementFirestoreHelper.AdvertisementsCallback() {
            @Override
            public void onSuccess(List<Advertisement> ads) {
                advertisements.clear();
                advertisements.addAll(ads);
                adapter.updateData(advertisements);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AllAnnouncesActivity.this,
                        "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilters() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterAdvertisements(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAdvertisements(newText);
                return false;
            }
        });
    }

    private void filterAdvertisements(String query) {
        if (query.isEmpty()) {
            adapter.updateData(advertisements);
            return;
        }

        List<Advertisement> filtered = new ArrayList<>();
        for (Advertisement ad : advertisements) {
            if (ad.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    ad.getCity().toLowerCase().contains(query.toLowerCase()) ||
                    ad.getAddress().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(ad);
            }
        }
        adapter.updateData(filtered);
    }

    @Override
    public void onAdvertisementClick(Advertisement advertisement) {
        // Ouvrir les détails de l'annonce
        AdvertisementDetailActivity.start(this, advertisement.getId());
    }

    @Override
    public void onFavoriteClick(Advertisement advertisement) {
        // Ajouter/retirer des favoris
        Toast.makeText(this, "Fonctionnalité favoris à venir", Toast.LENGTH_SHORT).show();
    }
}