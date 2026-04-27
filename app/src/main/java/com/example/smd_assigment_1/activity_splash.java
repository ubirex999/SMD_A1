package com.example.smd_assigment_1;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class activity_splash extends AppCompatActivity {
    ImageView truck;
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = () -> {
        Intent next;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean(LOGGED_IN_KEY, false);
        boolean firebaseLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        boolean onboardingShown = prefs.getBoolean(ONBOARDING_SHOWN_KEY, false);

        if (loggedIn && firebaseLoggedIn) {
            next = new Intent(activity_splash.this, MainActivity.class);
        } else {
            prefs.edit().putBoolean(LOGGED_IN_KEY, false).apply();
            if (onboardingShown) {
                next = new Intent(activity_splash.this, Login_Signup_page.class);
            } else {
                next = new Intent(activity_splash.this, ON_Bording_Screen.class);
            }
        }
        startActivity(next);
        finish();
    };

    private static final String PREFS_NAME = "auth_prefs";
    private static final String LOGGED_IN_KEY = "logged_in";
    private static final String ONBOARDING_SHOWN_KEY = "onboarding_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
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
        splashHandler.postDelayed(navigateRunnable, 4000);

    }


    private void init()
    {
        truck = findViewById(R.id.truck);

    }

    @Override
    protected void onDestroy() {
        splashHandler.removeCallbacks(navigateRunnable);
        super.onDestroy();
    }

}