package com.example.miniprojet;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.miniprojet.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Rediriger selon l'état de connexion
        if (sessionManager.isLoggedIn()) {
            // Connecté -> Dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        } else {
            // Non connecté -> Login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        finish(); // Fermer MainActivity
    }
}