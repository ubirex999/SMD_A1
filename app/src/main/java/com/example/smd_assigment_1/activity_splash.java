package com.example.smd_assigment_1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class activity_splash extends AppCompatActivity {
    ImageView truck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        truck.post(new Runnable() {
            @Override
            public void run() {
                float screenWidth = getResources().getDisplayMetrics().widthPixels;

                truck.setTranslationX(-truck.getWidth());
                truck.animate()
                        .translationX(screenWidth)
                        .setDuration(4000)
                        .start();
            }
        });
        new Handler().postDelayed(()->{
            startActivity(new Intent(activity_splash.this , MainActivity.class));
            finish();

        },4000 );

    }


    private void init()
    {
        truck = findViewById(R.id.truck);

    }

}