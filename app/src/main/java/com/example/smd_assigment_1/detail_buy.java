package com.example.smd_assigment_1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class detail_buy extends AppCompatActivity {

    Button buy;
    ImageView image;
    TextView name, price, detail, model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_buy);

        image = findViewById(R.id.detailImage);
        price = findViewById(R.id.detailPrice);
        name = findViewById(R.id.detailName);
        model = findViewById(R.id.detailModel);
        detail = findViewById(R.id.detaildetail);
        buy = findViewById(R.id.buy);

        Intent intent = getIntent();

        int img = intent.getIntExtra("image", 0);
        String productPrice = intent.getStringExtra("price");
        String productName = intent.getStringExtra("name");
        String productModel = intent.getStringExtra("model");
        String productDetail = intent.getStringExtra("detail");
        String productId = intent.getStringExtra("productId");

        image.setImageResource(img);
        name.setText(productName);
        price.setText(productPrice);
        model.setText(productModel);
        detail.setText(productDetail);

        buy.setOnClickListener(v -> {
            // Show AlertDialog confirmation (no SMS)
            new AlertDialog.Builder(detail_buy.this)
                    .setTitle("Buy Now")
                    .setMessage("Are you sure you want to buy this product?")
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        // Add product to cart with quantity = 1
                        Product product = new Product(
                                productId != null ? productId : "unknown",
                                productName != null ? productName : "",
                                productPrice != null ? productPrice : "$0.00",
                                null,
                                productDetail != null ? productDetail : "",
                                productModel != null ? productModel : "",
                                img
                        );
                        CartStore cartStore = new CartStore(detail_buy.this);
                        cartStore.addToCart(product);
                        Toast.makeText(detail_buy.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}