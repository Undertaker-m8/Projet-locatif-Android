package com.example.miniprojet.utils;

import android.content.Context;
import android.location.Location;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DistanceCalculator {
    
    private static final String OSRM_BASE_URL = "https://router.project-osrm.org/route/v1/driving/";
    private static final OkHttpClient client = new OkHttpClient();
    
    public interface DistanceCallback {
        void onDistanceCalculated(double distance, double duration);
        void onFailure(String error);
    }
    
    public static void calculateDistance(double startLat, double startLon,
                                        double endLat, double endLon,
                                        DistanceCallback callback) {
        String url = String.format("%s%f,%f;%f,%f?overview=false",
                OSRM_BASE_URL, startLon, startLat, endLon, endLat);
        
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                
                Response response = client.newCall(request).execute();
                
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    
                    if (json.getString("code").equals("Ok")) {
                        JSONArray routes = json.getJSONArray("routes");
                        JSONObject route = routes.getJSONObject(0);
                        
                        double distance = route.getDouble("distance") / 1000.0; // Convert to km
                        double duration = route.getDouble("duration") / 60.0; // Convert to minutes
                        
                        callback.onDistanceCalculated(distance, duration);
                    } else {
                        callback.onFailure("No route found");
                    }
                } else {
                    callback.onFailure("HTTP error: " + response.code());
                }
            } catch (Exception e) {
                callback.onFailure(e.getMessage());
            }
        }).start();
    }
    
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%d m", (int)(distanceKm * 1000));
        } else if (distanceKm < 10) {
            return String.format("%.1f km", distanceKm);
        } else {
            return String.format("%d km", (int)distanceKm);
        }
    }
    
    public static String formatDuration(double durationMinutes) {
        if (durationMinutes < 60) {
            return String.format("%d min", (int)durationMinutes);
        } else {
            int hours = (int)(durationMinutes / 60);
            int minutes = (int)(durationMinutes % 60);
            return String.format("%dh %02d", hours, minutes);
        }
    }
}