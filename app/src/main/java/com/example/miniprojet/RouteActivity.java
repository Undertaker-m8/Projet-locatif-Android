package com.example.miniprojet;

import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.utils.DistanceCalculator;
import com.example.miniprojet.utils.LocationHelper;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView tvDistance, tvDuration, tvInstructions;
    private Button btnStartNavigation;

    private double startLat, startLon, endLat, endLon;
    private String destinationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));

        initViews();
        getIntentData();
        setupMap();
        calculateRoute();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapView);
        tvDistance = findViewById(R.id.tv_distance);
        tvDuration = findViewById(R.id.tv_duration);
        tvInstructions = findViewById(R.id.tv_instructions);
        btnStartNavigation = findViewById(R.id.btn_start_navigation);

        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
    }

    // Dans RouteActivity.java, modifiez getIntentData() :
    private void getIntentData() {
        startLat = getIntent().getDoubleExtra("START_LAT", 0);
        startLon = getIntent().getDoubleExtra("START_LON", 0);
        endLat = getIntent().getDoubleExtra("END_LAT", 0);
        endLon = getIntent().getDoubleExtra("END_LON", 0);
        destinationName = getIntent().getStringExtra("DESTINATION_NAME");

        // Use current location if start not provided
        if (startLat == 0 && startLon == 0) {
            LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback() {
                @Override
                public void onLocationReceived(Location location) {
                    startLat = location.getLatitude();
                    startLon = location.getLongitude();
                    calculateRoute();
                }

                @Override
                public void onLocationError(String error) {
                    Toast.makeText(RouteActivity.this, error, Toast.LENGTH_SHORT).show();
                    // Utiliser une position par défaut
                    startLat = 48.8566; // Paris
                    startLon = 2.3522;
                    calculateRoute();
                }
            });
        } else {
            calculateRoute();
        }
    }
    private void setupMap() {
        // Center map between start and end
        double centerLat = (startLat + endLat) / 2;
        double centerLon = (startLon + endLon) / 2;

        GeoPoint centerPoint = new GeoPoint(centerLat, centerLon);
        mapView.getController().setCenter(centerPoint);
        mapView.getController().setZoom(14.0);

        // Add markers
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(new GeoPoint(startLat, startLon));
        startMarker.setTitle("Départ");
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        Marker endMarker = new Marker(mapView);
        endMarker.setPosition(new GeoPoint(endLat, endLon));
        endMarker.setTitle(destinationName != null ? destinationName : "Destination");
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        mapView.getOverlays().add(startMarker);
        mapView.getOverlays().add(endMarker);
    }

    private void calculateRoute() {
        DistanceCalculator.calculateDistance(startLat, startLon, endLat, endLon,
                new DistanceCalculator.DistanceCallback() {
                    @Override
                    public void onDistanceCalculated(double distance, double duration) {
                        runOnUiThread(() -> {
                            tvDistance.setText(DistanceCalculator.formatDistance(distance));
                            tvDuration.setText(DistanceCalculator.formatDuration(duration));

                            // Draw route on map
                            drawRouteOnMap();

                            // Generate instructions
                            generateRouteInstructions(distance, duration);
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(RouteActivity.this,
                                    "Erreur: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void drawRouteOnMap() {
        // For OSRM route drawing, you would need to parse the route geometry
        // This is a simplified version using a straight line
        List<GeoPoint> points = new ArrayList<>();
        points.add(new GeoPoint(startLat, startLon));
        points.add(new GeoPoint(endLat, endLon));

        Polyline line = new Polyline();
        line.setPoints(points);
        line.setColor(0xFF2196F3);
        line.setWidth(10.0f);

        mapView.getOverlays().add(line);
        mapView.invalidate();
    }

    private void generateRouteInstructions(double distance, double duration) {
        StringBuilder instructions = new StringBuilder();
        instructions.append("Itinéraire vers ").append(destinationName).append("\n\n");
        instructions.append("Distance: ").append(DistanceCalculator.formatDistance(distance)).append("\n");
        instructions.append("Durée estimée: ").append(DistanceCalculator.formatDuration(duration)).append("\n\n");

        if (distance < 2) {
            instructions.append(" Vous êtes très près de la destination\n");
            instructions.append("Marchez environ ").append((int)(distance * 1000)).append(" mètres");
        } else if (distance < 10) {
            instructions.append(" Prenez votre voiture ou les transports en commun\n");
            instructions.append("Temps estimé: ").append((int)duration).append(" minutes");
        } else {
            instructions.append(" Long trajet en voiture recommandé\n");
            instructions.append("Prévoyez environ ").append((int)duration).append(" minutes de route");
        }

        tvInstructions.setText(instructions.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
