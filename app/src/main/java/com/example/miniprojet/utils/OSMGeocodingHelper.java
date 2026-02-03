package com.example.miniprojet.utils;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OSMGeocodingHelper {
    private static final String TAG = "OSMGeocodingHelper";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public static double[] getCoordinatesFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            Log.w(TAG, " Adresse vide");
            return new double[]{0.0, 0.0};
        }

        try {
            // Nettoyer et formater l'adresse pour la France
            String cleanAddress = address.trim();

            // Log pour déboguer
            Log.d(TAG, "Adresse originale: " + cleanAddress);

            // IMPORTANT: Ne pas ajouter "France" si déjà présent
            if (!cleanAddress.toLowerCase().contains("france")) {
                cleanAddress += ", France";
            }

            // Encodage URL
            String encodedAddress = URLEncoder.encode(cleanAddress, "UTF-8");

            // URL avec paramètres optimisés pour la France
            String urlString = NOMINATIM_URL + "?q=" + encodedAddress +
                    "&format=json&limit=1&addressdetails=1" +
                    "&countrycodes=fr" +  // Recherche limitée à la France
                    "&accept-language=fr"; // Langue française

            Log.d(TAG, " Requête OSM: " + urlString);

            // Configuration de la requête
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "StudentHousingApp/1.0 (contact@example.com)");
            connection.setRequestProperty("Accept-Language", "fr-FR,fr;q=0.9");
            connection.setConnectTimeout(15000); // 15 secondes
            connection.setReadTimeout(15000);

            // Désactiver le cache
            connection.setUseCaches(false);

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Code réponse OSM: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d(TAG, "Réponse OSM: " + response.toString());

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject firstResult = jsonArray.getJSONObject(0);
                    double lat = firstResult.getDouble("lat");
                    double lon = firstResult.getDouble("lon");
                    String displayName = firstResult.getString("display_name");

                    Log.d(TAG, " OSM trouvé: " + lat + ", " + lon);
                    Log.d(TAG, " Lieu: " + displayName);

                    return new double[]{lat, lon};
                } else {
                    Log.w(TAG, " Aucun résultat OSM pour: " + cleanAddress);

                    // Tentative alternative : recherche plus large
                    return tryAlternativeSearch(cleanAddress);
                }
            } else {
                Log.e(TAG, "❌ Erreur HTTP OSM: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur OSM: " + e.getMessage());
            e.printStackTrace();
        }

        return new double[]{0.0, 0.0};
    }

    // Méthode alternative si la première recherche échoue
    private static double[] tryAlternativeSearch(String address) {
        try {
            // Tentative 1 : Recherche sans "France"
            String cleanAddress = address.replace(", France", "").trim();
            String encodedAddress = URLEncoder.encode(cleanAddress, "UTF-8");
            String urlString = NOMINATIM_URL + "?q=" + encodedAddress +
                    "&format=json&limit=1&countrycodes=fr";

            Log.d(TAG, "🔍 Tentative alternative: " + cleanAddress);

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "StudentHousingApp/1.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject firstResult = jsonArray.getJSONObject(0);
                    double lat = firstResult.getDouble("lat");
                    double lon = firstResult.getDouble("lon");

                    Log.d(TAG, "✅ Alternative trouvée: " + lat + ", " + lon);
                    return new double[]{lat, lon};
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erreur recherche alternative: " + e.getMessage());
        }

        return new double[]{0.0, 0.0};
    }
}