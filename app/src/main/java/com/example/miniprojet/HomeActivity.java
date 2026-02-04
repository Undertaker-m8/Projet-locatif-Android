package com.example.miniprojet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

// IMPORTANT
import android.app.Activity;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        // LAYOUT TRÈS SIMPLE - sans ActionBar
        setContentView(R.layout.activity_home);

        Button btnSignIn = findViewById(R.id.btn_signIn);
        Button btnLogIn = findViewById(R.id.btn_login);

        if (btnSignIn != null) {
            btnSignIn.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogIn != null) {
            btnLogIn.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }
    }
}
