package com.example.smd_assigment_1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private String accountType;

    private static final String PREFS_NAME = "auth_prefs";
    private static final String LOGGED_IN_KEY = "logged_in";
    private static final String ACCOUNT_TYPE_KEY = "account_type";
    private static final String USER_NAME_KEY = "user_name";
    private static final String THEME_KEY = "theme_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate so it takes effect immediately
        SharedPreferences themePrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean(THEME_KEY, false);
        int desiredMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(desiredMode);

        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            prefs.edit().putBoolean(LOGGED_IN_KEY, false).apply();
            Intent intent = new Intent(this, Login_Signup_page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        accountType = prefs.getString(ACCOUNT_TYPE_KEY, "Buyer");
        String userName = prefs.getString(USER_NAME_KEY, "User");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        drawerLayout = findViewById(R.id.drawer_layout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationView navigationViewCustom = findViewById(R.id.nav_view_custom);

        // Setup Header Data
        View headerView = findViewById(R.id.side_menu_container);
        TextView tvHeaderName = headerView.findViewById(R.id.tvHeaderName);
        tvHeaderName.setText("Hello " + userName);

        navigationViewCustom.setNavigationItemSelectedListener(this);

        // Setup Theme Toggle in Footer
        LinearLayout llLight = findViewById(R.id.llLightTheme);
        LinearLayout llDark = findViewById(R.id.llDarkTheme);
        updateThemeSelectionUI(isDarkMode);

        llLight.setOnClickListener(v -> {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                setThemeMode(false);
            }
        });
        llDark.setOnClickListener(v -> {
            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                setThemeMode(true);
            }
        });

        // Update Drawer Header Profile
        NavigationView navView = findViewById(R.id.nav_view_custom);
        View header = navView.getHeaderView(0);
        if (header != null) {
            TextView tvName = header.findViewById(R.id.tvHeaderName);
            TextView tvEmail = header.findViewById(R.id.tvHeaderEmail);
            tvName.setText(userName);
            // Email could be from Firebase, using placeholder for now
            tvEmail.setText(userName.toLowerCase().replace(" ", "") + "@gmail.com");
        }

        if ("Seller".equalsIgnoreCase(accountType)) {
            bottomNavigationView.setVisibility(View.GONE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            if (savedInstanceState == null || getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                replaceFragment(new SellerHomeFragment());
            }
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            setupBottomNav();
            if (savedInstanceState == null || getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                replaceFragment(new HomeFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setThemeMode(boolean darkMode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(THEME_KEY, darkMode).apply();
        
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate();
    }

    private void updateThemeSelectionUI(boolean isDarkMode) {
        LinearLayout llLight = findViewById(R.id.llLightTheme);
        LinearLayout llDark = findViewById(R.id.llDarkTheme);
        
        if (isDarkMode) {
            llDark.setBackgroundResource(R.drawable.theme_option_selected);
            llLight.setBackground(null);
        } else {
            llLight.setBackgroundResource(R.drawable.theme_option_selected);
            llDark.setBackground(null);
        }
    }

    private void setupBottomNav() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment(), false);
                return true;
            } else if (id == R.id.nav_search) {
                replaceFragment(new Frag_Search(), true);
                return true;
            } else if (id == R.id.nav_favourites) {
                replaceFragment(new FavouritesFragment(), true);
                return true;
            } else if (id == R.id.nav_cart) {
                replaceFragment(new CartFragment(), true);
                return true;
            } else if (id == R.id.nav_profile) {
                replaceFragment(new AccountFragment(), true);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_seller_home) {
            replaceFragment(new SellerHomeFragment(), false);
        } else if (id == R.id.nav_order_history) {
            replaceFragment(new OrderHistoryFragment(), true);
        } else if (id == R.id.nav_seller_messages) {
            replaceFragment(new BuyerListFragment(), true);
        } else if (id == R.id.nav_seller_account) {
            replaceFragment(new AccountFragment(), true);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private void replaceFragment(androidx.fragment.app.Fragment fragment) {
        replaceFragment(fragment, false);
    }

    private void replaceFragment(androidx.fragment.app.Fragment fragment, boolean addToBackStack) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment);
        
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
