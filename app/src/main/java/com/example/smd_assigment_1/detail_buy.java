package com.example.smd_assigment_1;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class detail_buy extends AppCompatActivity {

    Button buy;
    ImageView image;
    TextView name, price, detail, model;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_buy);

        dbHelper = new DatabaseHelper(this);

        image = findViewById(R.id.detailImage);
        price = findViewById(R.id.detailPrice);
        name = findViewById(R.id.detailName);
        model = findViewById(R.id.detailModel);
        detail = findViewById(R.id.detaildetail);
        buy = findViewById(R.id.buy);

        SharedPreferences prefs = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE);
        String accountType = prefs.getString("account_type", "Buyer");
        if ("Seller".equalsIgnoreCase(accountType)) {
            buy.setVisibility(View.GONE);
        }

        Intent intent = getIntent();

        int img = intent.getIntExtra("image", 0);
        String productPrice = intent.getStringExtra("price");
        String productName = intent.getStringExtra("name");
        String productModel = intent.getStringExtra("model");
        String productDetail = intent.getStringExtra("detail");
        String productId = intent.getStringExtra("productId");
        String sellerId = intent.getStringExtra("sellerId");
        String productType = intent.getStringExtra("type");

        image.setImageResource(img);
        name.setText(productName);
        price.setText(productPrice);
        model.setText(productModel);
        detail.setText(productDetail);

        buy.setOnClickListener(v -> {
            new AlertDialog.Builder(detail_buy.this)
                    .setTitle("Add to Cart")
                    .setMessage("Do you want to add this product to your cart?")
                    .setPositiveButton("Add", (dialog, which) -> {
                        Product product = new Product();
                        product.setId(productId != null ? productId : "unknown");
                        product.setName(productName != null ? productName : "");
                        product.setType(productType != null ? productType : "");
                        product.setPrice(productPrice != null ? productPrice : "$0.00");
                        product.setDescription(productDetail != null ? productDetail : "");
                        product.setSellerId(sellerId != null ? sellerId : "");
                        product.setImageResId(img);
                        product.setModelOrInfo(productModel != null ? productModel : "");
                        
                        CartStore cartStore = CartStore.getInstance(detail_buy.this);
                        cartStore.addToCart(product);
                        Toast.makeText(detail_buy.this, "Added to cart!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
