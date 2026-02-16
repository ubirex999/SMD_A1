package com.example.smd_assigment_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class detail_buy extends AppCompatActivity {

    Button buy;
    ImageView image;
    TextView name, price , detail , model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_buy);

        image = findViewById(R.id.detailImage);
        price = findViewById(R.id.detailPrice);
        name = findViewById(R.id.detailName);
        model = findViewById(R.id.detailModel);
        detail = findViewById(R.id.detaildetail);


        Intent intent = getIntent();

        int img = intent.getIntExtra("image", 0);
        String productPrice = intent.getStringExtra("price");
        String productName = intent.getStringExtra("name");
        String productmodel = intent.getStringExtra("model");
        String productdetail = intent.getStringExtra("detail");


        image.setImageResource(img);
        name.setText(productName);
        price.setText(productPrice);
        model.setText(productmodel);
        detail.setText(productdetail);

        init();

        buy.setOnClickListener((v)->
        {
            Intent i =  new Intent(detail_buy.this , buy_page.class);
            startActivity(i);
            finish();

        });
    }

    private void init()
    {
        buy = findViewById(R.id.buy);
    }


}