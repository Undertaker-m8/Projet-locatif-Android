package com.example.miniprojet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.miniprojet.firebase.UserFirestoreHelper;
import com.example.miniprojet.model.User;
import com.example.miniprojet.utils.SessionManager;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private SessionManager sessionManager;
    private UserFirestoreHelper userFirestoreHelper;
    private User currentUser;

    private ImageView ivProfile;
    private ImageButton btnChangePhoto;
    private EditText etFirstName, etLastName, etEmail, etPhone, etUniversity;
    private Button btnSave, btnCancel;

    private Uri selectedImageUri;
    private String currentProfileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);
        userFirestoreHelper = new UserFirestoreHelper();

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Modifier mon profil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        loadCurrentUserProfile();
        setupListeners();
    }

    private void initViews() {
        ivProfile = findViewById(R.id.iv_profile);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etUniversity = findViewById(R.id.et_university);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void loadCurrentUserProfile() {
        currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            etFirstName.setText(currentUser.getFirstName());
            etLastName.setText(currentUser.getLastName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
            etUniversity.setText(currentUser.getUniversity() != null ? currentUser.getUniversity() : "");

            // Charger la photo de profil si elle existe
            currentProfileImageUrl = currentUser.getProfileImageUrl();
            if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(currentProfileImageUrl)
                        .placeholder(R.drawable.ic_person)
                        .into(ivProfile);
            }
        }
    }

    private void setupListeners() {
        btnChangePhoto.setOnClickListener(v -> openImagePicker());

        btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                updateUserProfile();
            }
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Vérifier le prénom
        if (TextUtils.isEmpty(etFirstName.getText().toString().trim())) {
            etFirstName.setError("Le prénom est obligatoire");
            etFirstName.requestFocus();
            isValid = false;
        }

        // Vérifier le nom
        if (TextUtils.isEmpty(etLastName.getText().toString().trim())) {
            etLastName.setError("Le nom est obligatoire");
            etLastName.requestFocus();
            isValid = false;
        }

        // Vérifier l'email
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("L'email est obligatoire");
            etEmail.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Format d'email invalide");
            etEmail.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void updateUserProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String university = etUniversity.getText().toString().trim();

        // Désactiver le bouton pour éviter les clics multiples
        btnSave.setEnabled(false);
        btnSave.setText("Enregistrement...");

        // Vérifier si l'email a changé
        if (!email.equals(currentUser.getEmail())) {
            // Vérifier si le nouvel email existe déjà
            userFirestoreHelper.checkEmailExists(email, new UserFirestoreHelper.UserExistsCallback() {
                @Override
                public void onCallback(boolean exists) {
                    runOnUiThread(() -> {
                        if (exists) {
                            // L'email existe déjà
                            btnSave.setEnabled(true);
                            btnSave.setText("Enregistrer les modifications");
                            etEmail.setError("Cet email est déjà utilisé");
                            etEmail.requestFocus();
                        } else {
                            // L'email n'existe pas, procéder à la mise à jour
                            proceedWithUpdate(firstName, lastName, email, phone, university);
                        }
                    });
                }
            });
        } else {
            // L'email n'a pas changé, procéder directement
            proceedWithUpdate(firstName, lastName, email, phone, university);
        }
    }

    private void proceedWithUpdate(String firstName, String lastName, String email,
                                   String phone, String university) {
        if (selectedImageUri != null) {
            // Si une nouvelle image a été sélectionnée
            uploadProfileImageAndUpdate(firstName, lastName, email, phone, university);
        } else {
            // Si aucune nouvelle image n'a été sélectionnée
            updateUserInFirestore(firstName, lastName, email, phone, university, currentProfileImageUrl);
        }
    }

    private void uploadProfileImageAndUpdate(String firstName, String lastName, String email,
                                             String phone, String university) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String imageName = "profile_" + currentUser.getId() + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference profileImageRef = storageRef.child("profile_images/" + imageName);

        UploadTask uploadTask = profileImageRef.putFile(selectedImageUri);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                updateUserInFirestore(firstName, lastName, email, phone, university, imageUrl);
            });
        }).addOnFailureListener(e -> {
            btnSave.setEnabled(true);
            btnSave.setText("Enregistrer les modifications");
            Toast.makeText(this, "Échec du téléchargement de l'image", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUserInFirestore(String firstName, String lastName, String email,
                                       String phone, String university, String profileImageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("email", email);
        updates.put("phone", phone);
        updates.put("university", university);
        updates.put("profileImageUrl", profileImageUrl);

        userFirestoreHelper.updateUser(currentUser.getId(), updates, new UserFirestoreHelper.UpdateCallback() {
            @Override
            public void onSuccess() {
                // Mettre à jour l'utilisateur localement
                User updatedUser = sessionManager.getCurrentUser();
                if (updatedUser != null) {
                    updatedUser.setFirstName(firstName);
                    updatedUser.setLastName(lastName);
                    updatedUser.setEmail(email);
                    updatedUser.setPhone(phone);
                    updatedUser.setUniversity(university);
                    updatedUser.setProfileImageUrl(profileImageUrl);
                    updatedUser.setUpdatedAt(new Date());

                    // Sauvegarder dans SessionManager
                    sessionManager.saveUser(updatedUser);
                }

                Toast.makeText(EditProfileActivity.this,
                        "Profil mis à jour avec succès",
                        Toast.LENGTH_SHORT).show();

                // Retourner au profil avec un résultat
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                btnSave.setEnabled(true);
                btnSave.setText("Enregistrer les modifications");
                Toast.makeText(EditProfileActivity.this,
                        "Erreur lors de la mise à jour: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_person)
                    .into(ivProfile);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}