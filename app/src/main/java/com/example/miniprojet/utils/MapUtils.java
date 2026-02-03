package com.example.miniprojet.utils;

import android.content.Context;
import org.osmdroid.config.Configuration;

public class MapUtils {

    public static void initializeOSM(Context context) {
        // Configuration OSM de base
        Configuration.getInstance().load(context,
                context.getSharedPreferences("osm", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        // Configuration du cache
        Configuration.getInstance().setOsmdroidBasePath(context.getFilesDir());
        Configuration.getInstance().setOsmdroidTileCache(context.getCacheDir());
        Configuration.getInstance().setCacheMapTileCount((short) 100);
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(1024L * 1024L * 100L); // 100 MB

        // Configuration du téléchargement
        Configuration.getInstance().setTileDownloadThreads((short) 2);
        Configuration.getInstance().setTileDownloadMaxQueueSize((short) 10);

        // Configuration de débogage (optionnel)
        Configuration.getInstance().setDebugMapView(false);
        Configuration.getInstance().setDebugTileProviders(false);
        Configuration.getInstance().setDebugMode(false);
    }
}