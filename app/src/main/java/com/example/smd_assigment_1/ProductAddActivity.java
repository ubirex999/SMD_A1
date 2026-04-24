package com.example.smd_assigment_1;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProductAddActivity extends AppCompatActivity {

    private static final String TAG = "ProductAddActivity";
    private EditText etName, etType, etPrice, etDescription;
    private MaterialButton btnAdd;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_add);

        mAuth = FirebaseAuth.getInstance();
        // Use explicit URL for stability as confirmed in previous fixes
        mDatabase = FirebaseDatabase.getInstance("https://smd-assigment-1-default-rtdb.firebaseio.com").getReference("products");

        etName = findViewById(R.id.etProductName);
        etType = findViewById(R.id.etProductType);
        etPrice = findViewById(R.id.etProductPrice);
        etDescription = findViewById(R.id.etProductDescription);
        btnAdd = findViewById(R.id.btnAddProduct);

        btnAdd.setOnClickListener(v -> {
            Log.d(TAG, "Add Product button clicked");
            Toast.makeText(this, "Processing...", Toast.LENGTH_SHORT).show();
            addProduct();
        });
    }

    private void addProduct() {
        String name = etName.getText().toString().trim();
        String type = etType.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String sellerId = mAuth.getUid();

        Log.d(TAG, "Attempting to add product: " + name + ", Type: " + type + ", Seller: " + sellerId);

        if (name.isEmpty() || type.isEmpty() || price.isEmpty() || description.isEmpty()) {
            Log.w(TAG, "Validation failed: Some fields are empty");
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sellerId == null) {
            Log.e(TAG, "Seller ID is null! User not logged in.");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String productId = mDatabase.push().getKey();
        if (productId != null) {
            btnAdd.setEnabled(false);
            btnAdd.setText("Saving...");
            
            Log.d(TAG, "Generated Product ID: " + productId);
            Product product = new Product(productId, name, type, price, description, sellerId);
            
            // Safety Timeout: If Firebase doesn't respond in 4 seconds
            android.os.Handler timeoutHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            Runnable timeoutRunnable = () -> {
                if (!isFinishing()) {
                    Log.w(TAG, "Save operation slow. Proceeding locally.");
                    Toast.makeText(ProductAddActivity.this, "Product added locally. Will sync when online.", Toast.LENGTH_LONG).show();
                    finish(); // Go back to Home immediately, trusting persistence
                }
            };
            timeoutHandler.postDelayed(timeoutRunnable, 4000);

            mDatabase.child(productId).setValue(product)
                .addOnSuccessListener(aVoid -> {
                    if (!isFinishing()) {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        Log.d(TAG, "Firebase save successful");
                        Toast.makeText(ProductAddActivity.this, "Product synced with cloud!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isFinishing()) {
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        btnAdd.setEnabled(true);
                        btnAdd.setText("Add Product");
                        Log.e(TAG, "Firebase save failed: " + e.getMessage());
                        Toast.makeText(ProductAddActivity.this, "Failed to add product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            Log.e(TAG, "Failed to generate Product ID from Firebase");
        }
    }
}
