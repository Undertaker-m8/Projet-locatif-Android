package com.example.miniprojet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.miniprojet.firebase.AdvertisementFirestoreHelper;
import com.example.miniprojet.model.Advertisement;
import com.example.miniprojet.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditAdvertisementActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etPrice, etSurface, etRooms, etAddress;
    private AutoCompleteTextView autoCompleteCity;
    private CheckBox cbParking, cbElevator, cbBalcony;
    private Button btnUpdate, btnDelete;
    private ProgressBar progressBar;

    private AdvertisementFirestoreHelper adFirestoreHelper;
    private SessionManager sessionManager;
    private String advertisementId;
    private String selectedCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);  // Ligne importante
        setContentView(R.layout.activity_edit_advertisement);

        sessionManager = new SessionManager(this);
        adFirestoreHelper = new AdvertisementFirestoreHelper();

        advertisementId = getIntent().getStringExtra("ADVERTISEMENT_ID");
        if (advertisementId == null) {
            Toast.makeText(this, "ID d'annonce manquant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupCityAutoComplete();
        setupButtonListeners();
        loadAdvertisementDetails();
    }
    public static void start(Context context, String advertisementId) {
        Intent intent = new Intent(context, EditAdvertisementActivity.class);
        intent.putExtra("ADVERTISEMENT_ID", advertisementId);
        context.startActivity(intent);
    }
    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etPrice = findViewById(R.id.et_price);
        etSurface = findViewById(R.id.et_surface);
        etRooms = findViewById(R.id.et_rooms);
        etAddress = findViewById(R.id.et_address);

        // CHANGEMENT ICI : AutoCompleteTextView au lieu de Spinner
        autoCompleteCity = findViewById(R.id.spinner_city);

        cbParking = findViewById(R.id.cb_parking);
        cbElevator = findViewById(R.id.cb_elevator);
        cbBalcony = findViewById(R.id.cb_balcony);

        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);

        progressBar = findViewById(R.id.progressBar);
    }

    private void setupCityAutoComplete() {
        List<String> cities = new ArrayList<>();
        cities.add("Paris");
        cities.add("Lyon");
        cities.add("Marseille");
        cities.add("Toulouse");
        cities.add("Nice");
        cities.add("Nantes");
        cities.add("Strasbourg");
        cities.add("Montpellier");
        cities.add("Bordeaux");
        cities.add("Lille");
        cities.add("Saint-Étienne-du-Rouvray");
        cities.add("Rennes");
        cities.add("Rouen");
        cities.add("Grenoble");

        // Trier par ordre alphabétique
        Collections.sort(cities);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                cities
        );

        autoCompleteCity.setAdapter(adapter);
        autoCompleteCity.setThreshold(1); // Montre les suggestions dès le 1er caractère

        autoCompleteCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = parent.getItemAtPosition(position).toString();
            }
        });

        // Récupérer le texte si l'utilisateur tape manuellement
        autoCompleteCity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String text = autoCompleteCity.getText().toString().trim();
                    if (!text.isEmpty() && cities.contains(text)) {
                        selectedCity = text;
                    }
                }
            }
        });
    }

    private void setupButtonListeners() {
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAdvertisement();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAdvertisement();
            }
        });
    }

    private void loadAdvertisementDetails() {
        progressBar.setVisibility(View.VISIBLE);

        adFirestoreHelper.getAdvertisementById(advertisementId, new AdvertisementFirestoreHelper.AdvertisementCallback() {
            @Override
            public void onSuccess(Advertisement ad) {
                progressBar.setVisibility(View.GONE);
                populateForm(ad);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditAdvertisementActivity.this,
                        "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateForm(Advertisement ad) {
        etTitle.setText(ad.getTitle());
        etDescription.setText(ad.getDescription());
        etPrice.setText(String.valueOf(ad.getPrice()));
        etSurface.setText(String.valueOf(ad.getSurface()));
        etRooms.setText(String.valueOf(ad.getRooms()));
        etAddress.setText(ad.getAddress());

        // Set city in AutoCompleteTextView
        if (ad.getCity() != null && !ad.getCity().isEmpty()) {
            autoCompleteCity.setText(ad.getCity());
            selectedCity = ad.getCity();
        }

        cbParking.setChecked(ad.isHasParking());
        cbElevator.setChecked(ad.isHasElevator());
        cbBalcony.setChecked(ad.isHasBalcony());
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (TextUtils.isEmpty(etTitle.getText().toString().trim())) {
            etTitle.setError("Le titre est obligatoire");
            etTitle.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(etDescription.getText().toString().trim())) {
            etDescription.setError("La description est obligatoire");
            etDescription.requestFocus();
            isValid = false;
        }

        // Validate price
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

        // Validate surface
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

        // Validate rooms
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

        if (TextUtils.isEmpty(etAddress.getText().toString().trim())) {
            etAddress.setError("L'adresse est obligatoire");
            etAddress.requestFocus();
            isValid = false;
        }

        // Validate city
        String city = autoCompleteCity.getText().toString().trim();
        if (TextUtils.isEmpty(city)) {
            autoCompleteCity.setError("La ville est obligatoire");
            autoCompleteCity.requestFocus();
            isValid = false;
        } else {
            selectedCity = city;
        }

        return isValid;
    }

    private void updateAdvertisement() {
        if (!validateForm()) return;

        progressBar.setVisibility(View.VISIBLE);

        // Récupérer d'abord l'annonce existante
        adFirestoreHelper.getAdvertisementById(advertisementId, new AdvertisementFirestoreHelper.AdvertisementCallback() {
            @Override
            public void onSuccess(Advertisement existingAd) {
                // Mettre à jour seulement les champs modifiables
                existingAd.setTitle(etTitle.getText().toString().trim());
                existingAd.setDescription(etDescription.getText().toString().trim());
                existingAd.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
                existingAd.setSurface(Double.parseDouble(etSurface.getText().toString().trim()));
                existingAd.setRooms(Integer.parseInt(etRooms.getText().toString().trim()));
                existingAd.setAddress(etAddress.getText().toString().trim());
                existingAd.setCity(selectedCity);
                existingAd.setHasParking(cbParking.isChecked());
                existingAd.setHasElevator(cbElevator.isChecked());
                existingAd.setHasBalcony(cbBalcony.isChecked());

                // Mettre à jour dans Firestore
                adFirestoreHelper.updateAdvertisement(existingAd, new AdvertisementFirestoreHelper.AdvertisementCallback() {
                    @Override
                    public void onSuccess(Advertisement advertisement) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditAdvertisementActivity.this,
                                "Annonce mise à jour avec succès", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditAdvertisementActivity.this,
                                "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditAdvertisementActivity.this,
                        "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAdvertisement() {
        progressBar.setVisibility(View.VISIBLE);

        adFirestoreHelper.deleteAdvertisement(advertisementId, new AdvertisementFirestoreHelper.AdvertisementCallback() {
            @Override
            public void onSuccess(Advertisement advertisement) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditAdvertisementActivity.this,
                        "Annonce supprimée avec succès", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditAdvertisementActivity.this,
                        "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}