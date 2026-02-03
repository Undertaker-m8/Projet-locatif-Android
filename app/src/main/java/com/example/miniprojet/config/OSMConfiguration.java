package com.example.miniprojet.config;

import android.content.Context;
import org.osmdroid.config.Configuration;

public class OSMConfiguration {
    
    public static void initialize(Context context) {
        // Configuration OSM
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(context.getFilesDir());
        Configuration.getInstance().setOsmdroidTileCache(context.getCacheDir());
        
        // Définir le cache (optionnel)
        Configuration.getInstance().setCacheMapTileCount((short) 12);
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(1024 * 1024 * 50L); // 50 MB
    }
}