package com.example.miniprojet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.firebase.AdvertisementFirestoreHelper;
import com.example.miniprojet.model.Advertisement;
import com.example.miniprojet.utils.SessionManager;

public class CreateAnnouncesActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etPrice, etSurface, etRooms, etAddress, etCity;
    private CheckBox cbParking, cbElevator, cbBalcony;
    private Button btnCreate;
    private ProgressBar progressBar;

    private AdvertisementFirestoreHelper adFirestoreHelper;
    private SessionManager sessionManager;
    private boolean isCreating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_createannounces);

        sessionManager = new SessionManager(this);
        adFirestoreHelper = new AdvertisementFirestoreHelper();

        initViews();
        setupButtonListener();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etSurface = findViewById(R.id.et_surface);
        etRooms = findViewById(R.id.et_rooms);
        etAddress = findViewById(R.id.et_address);
        etCity = findViewById(R.id.et_city);

        cbParking = findViewById(R.id.cb_parking);
        cbElevator = findViewById(R.id.cb_elevator);
        cbBalcony = findViewById(R.id.cb_balcony);

        btnCreate = findViewById(R.id.btn_create);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupButtonListener() {
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isCreating) {
                    createAdvertisement();
                }
            }
        });
    }

    private void createAdvertisement() {
        // 1. Valider le formulaire
        if (!validateForm()) {
            return;
        }

        // 2. Désactiver le bouton pour éviter double clic
        isCreating = true;
        btnCreate.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // 3. Créer l'objet Advertisement
        Advertisement ad = new Advertisement();
        ad.setTitle(etTitle.getText().toString().trim());
        ad.setDescription(etDescription.getText().toString().trim());
        ad.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
        ad.setSurface(Double.parseDouble(etSurface.getText().toString().trim()));
        ad.setRooms(Integer.parseInt(etRooms.getText().toString().trim()));
        ad.setAddress(etAddress.getText().toString().trim());
        ad.setCity(etCity.getText().toString().trim());

        // User info
        ad.setUserId(sessionManager.getUserId());
        ad.setUserEmail(sessionManager.getUserEmail());
        ad.setUserName(sessionManager.getUserName());

        // Équipements
        ad.setHasParking(cbParking.isChecked());
        ad.setHasElevator(cbElevator.isChecked());
        ad.setHasBalcony(cbBalcony.isChecked());

        // Valeurs par défaut (important pour éviter null)
        ad.setLatitude(0.0);
        ad.setLongitude(0.0);
        ad.setRating(0.0);
        ad.setReviewCount(0);

        // 4. Envoyer à Firestore
        adFirestoreHelper.createAdvertisement(ad, new AdvertisementFirestoreHelper.AdvertisementCallback() {
            @Override
            public void onSuccess(Advertisement advertisement) {
                // SUCCÈS
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        isCreating = false;
                        btnCreate.setEnabled(true);

                        Toast.makeText(CreateAnnouncesActivity.this,
                                "✅ Annonce créée avec succès !", Toast.LENGTH_SHORT).show();

                        // Rediriger vers la liste des annonces
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // ÉCHEC
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        isCreating = false;
                        btnCreate.setEnabled(true);

                        Toast.makeText(CreateAnnouncesActivity.this,
                                "❌ Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        // Nettoyer le cache si échec
                        clearCacheAfterFailure();
                    }
                });
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Titre
        if (TextUtils.isEmpty(etTitle.getText().toString().trim())) {
            etTitle.setError("Le titre est obligatoire");
            etTitle.requestFocus();
            isValid = false;
        }

        // Description
        if (TextUtils.isEmpty(etDescription.getText().toString().trim())) {
            etDescription.setError("La description est obligatoire");
            etDescription.requestFocus();
            isValid = false;
        }

        // Prix
        try {
            double price = Double.parseDouble(etPrice.getText().toString().trim());
            if (price <= 0) {
                etPrice.setError("Le prix doit être > 0");
                etPrice.requestFocus();
                isValid = false;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Prix invalide");
            etPrice.requestFocus();
            isValid = false;
        }

        // Surface
        try {
            double surface = Double.parseDouble(etSurface.getText().toString().trim());
            if (surface <= 0) {
                etSurface.setError("La surface doit être > 0");
                etSurface.requestFocus();
                isValid = false;
            }
        } catch (NumberFormatException e) {
            etSurface.setError("Surface invalide");
            etSurface.requestFocus();
            isValid = false;
        }

        // Pièces
        try {
            int rooms = Integer.parseInt(etRooms.getText().toString().trim());
            if (rooms <= 0) {
                etRooms.setError("Le nombre de pièces doit être > 0");
                etRooms.requestFocus();
                isValid = false;
            }
        } catch (NumberFormatException e) {
            etRooms.setError("Nombre de pièces invalide");
            etRooms.requestFocus();
            isValid = false;
        }

        // Adresse
        if (TextUtils.isEmpty(etAddress.getText().toString().trim())) {
            etAddress.setError("L'adresse est obligatoire");
            etAddress.requestFocus();
            isValid = false;
        }

        // Ville
        if (TextUtils.isEmpty(etCity.getText().toString().trim())) {
            etCity.setError("La ville est obligatoire");
            etCity.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void clearCacheAfterFailure() {
        // Nettoyer le cache Firebase si création échouée
        adFirestoreHelper.clearFirebaseCache(new AdvertisementFirestoreHelper.CacheClearCallback() {
            @Override
            public void onCacheCleared() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CreateAnnouncesActivity.this,
                                "Cache nettoyé après échec", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCacheClearError(Exception e) {
                // Ignorer l'erreur de nettoyage
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!isCreating) {
            super.onBackPressed();
        }
        // Si création en cours, on n'autorise pas le retour
    }
}