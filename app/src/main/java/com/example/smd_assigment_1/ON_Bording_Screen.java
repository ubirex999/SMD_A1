package com.example.smd_assigment_1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;

import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ON_Bording_Screen extends AppCompatActivity {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String ONBOARDING_SHOWN_KEY = "onboarding_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_bording_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton getStarted = findViewById(R.id.bt_getstarted);
        getStarted.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean(ONBOARDING_SHOWN_KEY, true).apply();
            startActivity(new Intent(this, Login_Signup_page.class));
            finish();
        });
    }
}