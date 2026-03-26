package com.example.smd_assigment_1;

import android.os.Bundle;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class Login_Signup_page extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private static final String PREFS_NAME = "auth_prefs";
    private static final String LOGGED_IN_KEY = "logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If user is already logged in, skip auth screens.
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(LOGGED_IN_KEY, false)) {
            startActivity(new android.content.Intent(this, MainActivity.class));
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_signup_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.ViewPager);
        setupViewPager();
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) return new Login_Frag();
                return new Fragment_Sign_up();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Login");
            else tab.setText("Sign up");
        }).attach();
    }

    public void showLoginTab() {
        showTab(0);
    }

    public void showSignupTab() {
        showTab(1);
    }

    public void showTab(int position) {
        if (viewPager == null) return;
        viewPager.setCurrentItem(position, true);
    }
}