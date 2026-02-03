package com.example.miniprojet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.firebase.UserFirestoreHelper;
import com.example.miniprojet.model.User;
import com.example.miniprojet.utils.PasswordHasher;
import com.example.miniprojet.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink, tvResult;
    private UserFirestoreHelper userHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Vérifier si déjà connecté
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            redirectToDashboard();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Connexion");
        }

        userHelper = new UserFirestoreHelper();
        initViews();
        setupLoginButton();
        setupSignupLink();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignupLink = findViewById(R.id.tv_signup_link);
        tvResult = findViewById(R.id.tv_result);
    }

    private void setupLoginButton() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateForm(email, password)) {
                loginUser(email, password);
            }
        });
    }

    private void setupSignupLink() {
        tvSignupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateForm(String email, String password) {
        tvResult.setText("");
        tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));

        if (TextUtils.isEmpty(email)) {
            tvResult.setText("L'email est obligatoire");
            etEmail.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvResult.setText("Format d'email invalide");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            tvResult.setText("Le mot de passe est obligatoire");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void loginUser(String email, String password) {
        tvResult.setText("Connexion en cours...");
        tvResult.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        btnLogin.setEnabled(false);

        userHelper.getUserByEmail(email, new UserFirestoreHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getPassword() != null) {
                    boolean passwordCorrect = PasswordHasher.verifyPassword(password, user.getPassword());

                    if (passwordCorrect) {
                        // Sauvegarder la session
                        sessionManager.createLoginSession(user);

                        tvResult.setText("Connexion réussie !");
                        tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_light));

                        Toast.makeText(LoginActivity.this,
                                "Bienvenue " + user.getFirstName() + " !",
                                Toast.LENGTH_LONG).show();

                        redirectToDashboard();
                    } else {
                        tvResult.setText("Mot de passe incorrect");
                        tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                        btnLogin.setEnabled(true);
                    }
                } else {
                    tvResult.setText("Problème avec votre compte");
                    tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    btnLogin.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Exception e) {
                tvResult.setText("Aucun compte trouvé avec cet email");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                btnLogin.setEnabled(true);
            }
        });
    }

    private void redirectToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Empêcher de retourner en arrière si non connecté
        if (!sessionManager.isLoggedIn()) {
            finishAffinity(); // Ferme toutes les activités
        } else {
            super.onBackPressed();
        }
    }
}