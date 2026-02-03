package com.example.miniprojet;

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

import java.util.Date;

public class SignInActivity extends AppCompatActivity {

    private EditText etLastName, etFirstName, etEmail, etPassword, etPhone, etUniversity;
    private Button btnRegister;
    private TextView tvResult;
    private UserFirestoreHelper userHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Inscription");
        }

        userHelper = new UserFirestoreHelper();
        initViews();
        setupRegisterButton();
    }

    private void initViews() {
        etLastName = findViewById(R.id.et_full_name);
        etFirstName = findViewById(R.id.et_first_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPhone = findViewById(R.id.et_phone);
        etUniversity = findViewById(R.id.et_university);
        btnRegister = findViewById(R.id.btn_register);
        tvResult = findViewById(R.id.tv_result);
    }

    private void setupRegisterButton() {
        btnRegister.setOnClickListener(v -> {
            String lastName = etLastName.getText().toString().trim();
            String firstName = etFirstName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String university = etUniversity.getText().toString().trim();

            if (validateForm(lastName, firstName, email, password, phone, university)) {
                String hashedPassword = PasswordHasher.hashPassword(password);

                User newUser = new User(lastName, firstName, email, hashedPassword, phone, university);
                newUser.setProfileImageUrl("");
                newUser.setCreatedAt(new Date());
                newUser.setUpdatedAt(new Date());

                registerUserInFirestore(newUser);
            }
        });
    }

    private boolean validateForm(String lastName, String firstName, String email, String password, String phone, String university) {
        tvResult.setText("");
        tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));

        if (TextUtils.isEmpty(lastName)) {
            tvResult.setText("Le nom de famille est obligatoire");
            etLastName.requestFocus();
            return false;
        }
        if (lastName.length() < 2) {
            tvResult.setText("Le nom de famille doit contenir au moins 2 caractères");
            etLastName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(firstName)) {
            tvResult.setText("Le prénom est obligatoire");
            etFirstName.requestFocus();
            return false;
        }
        if (firstName.length() < 2) {
            tvResult.setText("Le prénom doit contenir au moins 2 caractères");
            etFirstName.requestFocus();
            return false;
        }

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

        String passwordError = PasswordHasher.validatePasswordStrength(password);
        if (passwordError != null) {
            tvResult.setText(passwordError);
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            tvResult.setText("Le téléphone est obligatoire");
            etPhone.requestFocus();
            return false;
        }
        if (phone.length() < 10) {
            tvResult.setText("Le numéro de téléphone doit contenir au moins 10 chiffres");
            etPhone.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(university)) {
            tvResult.setText("L'université est obligatoire");
            etUniversity.requestFocus();
            return false;
        }
        if (university.length() < 2) {
            tvResult.setText("Le nom de l'université doit contenir au moins 2 caractères");
            etUniversity.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUserInFirestore(User user) {
        tvResult.setText("Vérification en cours...");
        tvResult.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        btnRegister.setEnabled(false);

        userHelper.registerUser(user, new UserFirestoreHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                tvResult.setText("Profil créé avec succès !");
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_green_light));

                Toast.makeText(SignInActivity.this,
                        "Bienvenue " + user.getFirstName() + " !", Toast.LENGTH_LONG).show();

                btnRegister.setEnabled(true);
                clearForm();
            }

            @Override
            public void onFailure(Exception e) {
                tvResult.setText("Erreur: " + e.getMessage());
                tvResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));

                Toast.makeText(SignInActivity.this,
                        "Échec de l'inscription", Toast.LENGTH_SHORT).show();

                btnRegister.setEnabled(true);
            }
        });
    }

    private void clearForm() {
        etLastName.setText("");
        etFirstName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etPhone.setText("");
        etUniversity.setText("");
    }
}