package com.example.smd_assigment_1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_search) {
                replaceFragment(new Frag_Search());
                return true;
            } else if (id == R.id.nav_favourites) {
                replaceFragment(new FavouritesFragment());
                return true;
            } else if (id == R.id.nav_cart) {
                replaceFragment(new CartFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                replaceFragment(new AccountFragment());
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }






    }

    private void replaceFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }







}