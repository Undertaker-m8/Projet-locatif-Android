package com.example.miniprojet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.miniprojet.firebase.AdvertisementFirestoreHelper;
import com.example.miniprojet.model.Advertisement;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private AdvertisementFirestoreHelper adFirestoreHelper;
    private MyLocationNewOverlay myLocationOverlay;
    private List<Marker> advertisementMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        // CONFIGURATION OSM CRITIQUE
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        initViews();
        setupMap();
        handleIntent();
        loadAdvertisements();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        adFirestoreHelper = new AdvertisementFirestoreHelper();
    }

    private void setupMap() {
        // Configuration de base
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setMinZoomLevel(3.0);
        mapView.setMaxZoomLevel(19.0);

        // Position par défaut (Paris)
        GeoPoint defaultCenter = new GeoPoint(48.8566, 2.3522);
        mapView.getController().setCenter(defaultCenter);
        mapView.getController().setZoom(12.0);

        // Ajouter la boussole
        CompassOverlay compassOverlay = new CompassOverlay(this,
                new InternalCompassOrientationProvider(this), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // Activer le suivi de localisation
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mapView.getOverlays().add(myLocationOverlay);

        // Configurer le bouton "Ma position"
        findViewById(R.id.fab_my_location).setOnClickListener(v -> {
            if (myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                Toast.makeText(this, "Centré sur votre position", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Position GPS non disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAdvertisements() {
        adFirestoreHelper.getAllAdvertisements(new AdvertisementFirestoreHelper.AdvertisementsCallback() {
            @Override
            public void onSuccess(List<Advertisement> advertisements) {
                runOnUiThread(() -> {
                    displayAdvertisementsOnMap(advertisements);
                    Toast.makeText(MapActivity.this,
                            advertisements.size() + " annonces chargées sur la carte",
                            Toast.LENGTH_SHORT).show();

                    // Ajuster la vue pour montrer tous les marqueurs si > 1
                    if (advertisementMarkers.size() > 1) {
                        zoomToShowAllMarkers();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MapActivity.this,
                            "Erreur chargement annonces: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void displayAdvertisementsOnMap(List<Advertisement> advertisements) {
        // Nettoyer les anciens marqueurs
        for (Marker marker : advertisementMarkers) {
            mapView.getOverlays().remove(marker);
        }
        advertisementMarkers.clear();

        // Ajouter les nouveaux marqueurs
        int validLocations = 0;
        for (Advertisement ad : advertisements) {
            if (ad.getLatitude() != 0.0 && ad.getLongitude() != 0.0) {
                GeoPoint point = new GeoPoint(ad.getLatitude(), ad.getLongitude());
                Marker marker = new Marker(mapView);

                marker.setPosition(point);
                marker.setTitle(ad.getTitle());

                // CORRECTION : Gestion des valeurs avec vérifications
                String surfaceText = (ad.getSurface() > 0) ?
                        String.format("%.0f m²", ad.getSurface()) : "Surface N/A";
                String roomsText = (ad.getRooms() > 0) ?
                        String.format("%d pièces", ad.getRooms()) : "Pièces N/A";

                String snippet = String.format("%s\n%s • %s • %s",
                        ad.getFormattedPrice() != null ? ad.getFormattedPrice() : "Prix N/A",
                        ad.getCity() != null ? ad.getCity() : "Ville N/A",
                        surfaceText,
                        roomsText);

                marker.setSnippet(snippet);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                // Personnaliser l'icône (optionnel)
                // marker.setIcon(getResources().getDrawable(R.drawable.ic_home));

                // Ajouter un OnClickListener
                marker.setOnMarkerClickListener((marker1, mapView) -> {
                    Toast.makeText(MapActivity.this,
                            marker1.getTitle(),
                            Toast.LENGTH_SHORT).show();
                    return true;
                });

                mapView.getOverlays().add(marker);
                advertisementMarkers.add(marker);
                validLocations++;
            }
        }

        mapView.invalidate();

        // Si aucune localisation valide, centrer sur la position par défaut
        if (validLocations == 0 && !advertisements.isEmpty()) {
            Toast.makeText(this,
                    "Aucune localisation précise trouvée dans les annonces",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void zoomToShowAllMarkers() {
        if (advertisementMarkers.isEmpty()) return;

        // Calculer les bornes
        double minLat = 90.0, maxLat = -90.0;
        double minLon = 180.0, maxLon = -180.0;

        for (Marker marker : advertisementMarkers) {
            GeoPoint point = (GeoPoint) marker.getPosition();
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }

        // Ajouter une marge
        double margin = 0.1;
        minLat -= margin;
        maxLat += margin;
        minLon -= margin;
        maxLon += margin;

        // Ajuster la vue
        mapView.getController().zoomToSpan(maxLat - minLat, maxLon - minLon);
        mapView.getController().setCenter(new GeoPoint(
                (minLat + maxLat) / 2,
                (minLon + maxLon) / 2
        ));
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("LATITUDE") && intent.hasExtra("LONGITUDE")) {
            double latitude = intent.getDoubleExtra("LATITUDE", 0.0);
            double longitude = intent.getDoubleExtra("LONGITUDE", 0.0);
            String title = intent.getStringExtra("TITLE");

            if (latitude != 0.0 && longitude != 0.0) {
                GeoPoint point = new GeoPoint(latitude, longitude);
                mapView.getController().setCenter(point);
                mapView.getController().setZoom(15.0);

                // Ajouter un marqueur spécifique
                Marker marker = new Marker(mapView);
                marker.setPosition(point);
                if (title != null) {
                    marker.setTitle(title);
                    marker.setSnippet("Localisation précise");
                }
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
                advertisementMarkers.add(marker);

                Toast.makeText(this, "Annonce localisée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
            myLocationOverlay.disableFollowLocation();
        }
    }
}