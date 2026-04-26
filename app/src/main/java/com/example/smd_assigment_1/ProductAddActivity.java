package com.example.smd_assigment_1;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductAddActivity extends AppCompatActivity {

    private static final String TAG = "ProductAddActivity";
    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";
    private EditText etName, etType, etPrice, etDescription;
    private MaterialButton btnAdd;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_add);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("products");

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
        String priceInput = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String sellerId = mAuth.getUid();

        Log.d(TAG, "Attempting to add product: " + name + ", Type: " + type + ", Seller: " + sellerId);

        if (name.isEmpty() || type.isEmpty() || priceInput.isEmpty() || description.isEmpty()) {
            Log.w(TAG, "Validation failed: Some fields are empty");
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Auto-format price: user types number, we store with "$"
        String numeric = priceInput.replaceAll("[^\\d.]", "");
        if (numeric.isEmpty()) {
            Toast.makeText(this, "Enter a valid price number", Toast.LENGTH_SHORT).show();
            return;
        }
        String price = "$" + numeric;

        if (sellerId == null) {
            Log.e(TAG, "Seller ID is null! User not logged in.");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAdd.setEnabled(false);
        btnAdd.setText("Saving...");

        DatabaseReference connectedRef = FirebaseDatabase.getInstance(DB_URL).getReference(".info/connected");
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected == null || !connected) {
                    Log.w(TAG, "Not connected to Firebase RTDB (.info/connected = false)");
                    Toast.makeText(ProductAddActivity.this, "No connection to database. Check internet and try again.", Toast.LENGTH_LONG).show();
                    btnAdd.setEnabled(true);
                    btnAdd.setText("Add Product");
                    return;
                }

                String productId = mDatabase.push().getKey();
                if (productId == null) {
                    Log.e(TAG, "Failed to generate Product ID from Firebase");
                    Toast.makeText(ProductAddActivity.this, "Failed to create product id. Try again.", Toast.LENGTH_SHORT).show();
                    btnAdd.setEnabled(true);
                    btnAdd.setText("Add Product");
                    return;
                }

                Log.d(TAG, "Generated Product ID: " + productId);
                Product product = new Product(productId, name, type, price, description, sellerId);
                // Persist an image choice as well (based on type keyword) so all clients render consistently
                product.setImageResId(product.getResolvedImageResId());

                mDatabase.child(productId)
                        .setValue(product)
                        .addOnSuccessListener(aVoid -> {
                            if (!isFinishing()) {
                                Log.d(TAG, "Firebase save successful");
                                Toast.makeText(ProductAddActivity.this, "Product added!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (!isFinishing()) {
                                btnAdd.setEnabled(true);
                                btnAdd.setText("Add Product");
                                Log.e(TAG, "Firebase save failed", e);
                                Toast.makeText(ProductAddActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Connectivity check cancelled: " + error.getMessage());
                Toast.makeText(ProductAddActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                btnAdd.setEnabled(true);
                btnAdd.setText("Add Product");
            }
        });
    }
}
