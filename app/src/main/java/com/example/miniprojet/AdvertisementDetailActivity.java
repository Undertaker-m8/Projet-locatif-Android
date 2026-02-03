package com.example.miniprojet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.firebase.AdvertisementFirestoreHelper;
import com.example.miniprojet.model.Advertisement;
import com.example.miniprojet.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AdvertisementDetailActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivBack;
    private ImageButton btnFavorite, btnShare;
    private TextView tvTitle, tvPrice, tvSurface, tvRooms, tvCity, tvAddress;
    private TextView tvDescription, tvUserName, tvUserEmail, tvCreatedAt;
    private ChipGroup chipAmenities;
    private LinearLayout layoutParking, layoutElevator, layoutBalcony;
    private Button btnContact, btnViewOnMap;

    // Data
    private Advertisement advertisement;
    private AdvertisementFirestoreHelper adFirestoreHelper;
    private SessionManager sessionManager;

    // Favorites
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "FavoritesPrefs";
    private static final String KEY_FAVORITES = "favorite_ids";
    private Set<String> favoriteIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertisement_detail);

        // Initialize
        adFirestoreHelper = new AdvertisementFirestoreHelper();
        sessionManager = new SessionManager(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load favorites
        loadFavoriteIds();

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Détails de l'annonce");
        }

        initViews();
        loadAdvertisementDetails();
    }

    private void initViews() {
        // Back button
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> onBackPressed());

        // Favorite button
        btnFavorite = findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Share button
        btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(v -> shareAdvertisement());

        // TextViews
        tvTitle = findViewById(R.id.tv_title);
        tvPrice = findViewById(R.id.tv_price);
        tvSurface = findViewById(R.id.tv_surface);
        tvRooms = findViewById(R.id.tv_rooms);
        tvCity = findViewById(R.id.tv_city);
        tvAddress = findViewById(R.id.tv_address);
        tvDescription = findViewById(R.id.tv_description);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvCreatedAt = findViewById(R.id.tv_created_at);

        // Amenities
        chipAmenities = findViewById(R.id.chip_amenities);

        // Features layouts
        layoutParking = findViewById(R.id.layout_parking);
        layoutElevator = findViewById(R.id.layout_elevator);
        layoutBalcony = findViewById(R.id.layout_balcony);

        // Buttons
        btnContact = findViewById(R.id.btn_contact);
        btnContact.setOnClickListener(v -> contactOwner());

        btnViewOnMap = findViewById(R.id.btn_view_map);
        btnViewOnMap.setOnClickListener(v -> viewOnMap());
    }

    private void loadAdvertisementDetails() {
        String adId = getIntent().getStringExtra("ADVERTISEMENT_ID");
        if (adId == null || adId.isEmpty()) {
            Toast.makeText(this, "Annonce non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adFirestoreHelper.getAdvertisementById(adId, new AdvertisementFirestoreHelper.AdvertisementCallback() {
            @Override
            public void onSuccess(Advertisement ad) {
                advertisement = ad;
                displayAdvertisementDetails(ad);
                updateFavoriteButton();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdvertisementDetailActivity.this,
                        "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayAdvertisementDetails(Advertisement ad) {
        // Basic info
        tvTitle.setText(ad.getTitle());

        // CORRECTION : Utilisation de méthodes de formatage locales
        tvPrice.setText(formatPrice(ad.getPrice()));
        tvSurface.setText(formatSurface(ad.getSurface()));

        tvRooms.setText(formatRooms(ad.getRooms()));
        tvCity.setText(ad.getCity());
        tvAddress.setText(ad.getAddress());
        tvDescription.setText(ad.getDescription());

        // User info
        tvUserName.setText(ad.getUserName());
        tvUserEmail.setText(ad.getUserEmail());

        // Date
        if (ad.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm", Locale.FRANCE);
            tvCreatedAt.setText("Publié le " + sdf.format(ad.getCreatedAt()));
        }

        // Amenities
        chipAmenities.removeAllViews();
        if (ad.getAmenities() != null) {
            for (String amenity : ad.getAmenities()) {
                Chip chip = new Chip(this);
                chip.setText(amenity);
                chip.setChipBackgroundColorResource(R.color.chip_background);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                chipAmenities.addView(chip);
            }
        }

        // Features visibility
        layoutParking.setVisibility(ad.isHasParking() ? View.VISIBLE : View.GONE);
        layoutElevator.setVisibility(ad.isHasElevator() ? View.VISIBLE : View.GONE);
        layoutBalcony.setVisibility(ad.isHasBalcony() ? View.VISIBLE : View.GONE);
    }

    // CORRECTION : Méthodes de formatage locales
    private String formatPrice(double price) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(0);
        return format.format(price) + " €";
    }

    private String formatSurface(double surface) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(0);
        return format.format(surface) + " m²";
    }

    private String formatRooms(int rooms) {
        if (rooms == 1) {
            return rooms + " pièce";
        } else {
            return rooms + " pièces";
        }
    }

    private void loadFavoriteIds() {
        String userId = sessionManager.getUserId();
        String userFavoritesKey = KEY_FAVORITES + "_" + userId;

        String json = sharedPreferences.getString(userFavoritesKey, "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<Set<String>>(){}.getType();
        favoriteIds = gson.fromJson(json, type);

        if (favoriteIds == null) {
            favoriteIds = new HashSet<>();
        }
    }

    private void saveFavoriteIds() {
        String userId = sessionManager.getUserId();
        String userFavoritesKey = KEY_FAVORITES + "_" + userId;

        Gson gson = new Gson();
        String json = gson.toJson(favoriteIds);

        sharedPreferences.edit()
                .putString(userFavoritesKey, json)
                .apply();
    }

    private void toggleFavorite() {
        if (advertisement == null) return;

        if (favoriteIds.contains(advertisement.getId())) {
            removeFromFavorites();
        } else {
            addToFavorites();
        }

        updateFavoriteButton();
    }

    private void addToFavorites() {
        if (advertisement == null) return;

        favoriteIds.add(advertisement.getId());
        saveFavoriteIds();

        // Sync with Firebase
        String userId = sessionManager.getUserId();
        adFirestoreHelper.addToFavorites(userId, advertisement.getId(),
                new AdvertisementFirestoreHelper.AdvertisementCallback() {
                    @Override
                    public void onSuccess(Advertisement ad) {
                        runOnUiThread(() -> {
                            Toast.makeText(AdvertisementDetailActivity.this,
                                    "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Still saved locally
                        runOnUiThread(() -> {
                            Toast.makeText(AdvertisementDetailActivity.this,
                                    "Ajouté aux favoris (hors ligne)", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void removeFromFavorites() {
        if (advertisement == null) return;

        favoriteIds.remove(advertisement.getId());
        saveFavoriteIds();

        // Sync with Firebase
        String userId = sessionManager.getUserId();
        adFirestoreHelper.removeFromFavorites(userId, advertisement.getId(),
                new AdvertisementFirestoreHelper.AdvertisementCallback() {
                    @Override
                    public void onSuccess(Advertisement ad) {
                        runOnUiThread(() -> {
                            Toast.makeText(AdvertisementDetailActivity.this,
                                    "Retiré des favoris", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Still removed locally
                        runOnUiThread(() -> {
                            Toast.makeText(AdvertisementDetailActivity.this,
                                    "Retiré des favoris (hors ligne)", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void updateFavoriteButton() {
        if (advertisement == null) return;

        boolean isFavorite = favoriteIds.contains(advertisement.getId());
        // CORRECTION : Utilisation de vos propres drawables
        btnFavorite.setImageResource(isFavorite ?
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        btnFavorite.setContentDescription(isFavorite ?
                "Retirer des favoris" : "Ajouter aux favoris");
    }

    private void shareAdvertisement() {
        if (advertisement == null) return;

        String shareText = advertisement.getTitle() + "\n" +
                formatPrice(advertisement.getPrice()) + "\n" +
                formatSurface(advertisement.getSurface()) + " • " +
                formatRooms(advertisement.getRooms()) + "\n" +
                advertisement.getAddress() + ", " + advertisement.getCity() + "\n\n" +
                "Trouvé sur Student Housing App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, advertisement.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Partager l'annonce"));
    }

    private void contactOwner() {
        if (advertisement == null) return;

        String email = advertisement.getUserEmail();
        String subject = "Demande d'information: " + advertisement.getTitle();
        String body = "Bonjour " + advertisement.getUserName() + ",\n\n" +
                "Je suis intéressé(e) par votre annonce : " + advertisement.getTitle() + "\n" +
                "📍 " + advertisement.getAddress() + ", " + advertisement.getCity() + "\n" +
                "💰 " + formatPrice(advertisement.getPrice()) + "\n" +
                "🏠 " + formatSurface(advertisement.getSurface()) + " • " +
                formatRooms(advertisement.getRooms()) + "\n\n" +
                "Pouvez-vous me donner plus d'informations ?\n\n" +
                "Cordialement,";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + email));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(emailIntent, "Envoyer un email"));
        } catch (Exception e) {
            Toast.makeText(this, "Aucune application email trouvée", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewOnMap() {
        if (advertisement == null ||
                advertisement.getLatitude() == 0.0 ||
                advertisement.getLongitude() == 0.0) {
            Toast.makeText(this, "Coordonnées non disponibles", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass coordinates to MapActivity
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("LATITUDE", advertisement.getLatitude());
        intent.putExtra("LONGITUDE", advertisement.getLongitude());
        intent.putExtra("TITLE", advertisement.getTitle());
        startActivity(intent);
    }

    public boolean isFavorite(String adId) {
        return favoriteIds.contains(adId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Méthode statique pour démarrer l'activité
    public static void start(AppCompatActivity context, String advertisementId) {
        Intent intent = new Intent(context, AdvertisementDetailActivity.class);
        intent.putExtra("ADVERTISEMENT_ID", advertisementId);
        context.startActivity(intent);
    }
}