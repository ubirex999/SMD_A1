package com.example.smd_assigment_1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    private TextView tvName, tvEmail, tvAddress, tvDob, tvGender, tvPhone, tvCountry;
    private MaterialButton btnLogout;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private static final String PREFS_NAME = "auth_prefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        tvName = root.findViewById(R.id.tvName_account);
        tvEmail = root.findViewById(R.id.tvEmail_account);
        tvAddress = root.findViewById(R.id.tvAddress_account);
        tvDob = root.findViewById(R.id.tvDob_account);
        tvGender = root.findViewById(R.id.tvGender_account);
        tvPhone = root.findViewById(R.id.tvPhone_account);
        tvCountry = root.findViewById(R.id.tvCountry_account);
        btnLogout = root.findViewById(R.id.btn_logout);
        View btnBack = root.findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadUserDetails();

        btnLogout.setOnClickListener(v -> logout());

        return root;
    }

    private void loadUserDetails() {
        // First, load basic info from SharedPreferences to show something immediately
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String cachedName = prefs.getString("user_name", "User");
            tvName.setText(cachedName);
            // Since email isn't in prefs by default, we'll wait for Firebase or use a placeholder
            if (mAuth.getCurrentUser() != null) {
                tvEmail.setText(mAuth.getCurrentUser().getEmail());
            }
        }

        String userId = mAuth.getUid();
        if (userId == null) return;

        // Monitor connection status
        mDatabase.child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class);
                Log.d("AccountFragment", "Firebase connection status: " + connected);
                if (!connected && isAdded()) {
                    // Toast.makeText(getContext(), "You are offline. Showing cached data.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mDatabase.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                
                if (snapshot.exists()) {
                    Log.d("AccountFragment", "User record found for: " + userId + ". Data: " + snapshot.getValue());
                    
                    // Helper to get values safely
                    tvName.setText(getStringValue(snapshot, "name", "Not Set"));
                    tvEmail.setText(getStringValue(snapshot, "email", "Not Set"));
                    tvAddress.setText(getStringValue(snapshot, "address", "Not Set"));
                    tvDob.setText(getStringValue(snapshot, "dob", "Not Set"));
                    tvGender.setText(getStringValue(snapshot, "gender", "Not Set"));
                    tvPhone.setText(getStringValue(snapshot, "phone", "Not Set"));
                    tvCountry.setText(getStringValue(snapshot, "country", "Not Set"));
                    
                    // Sync with prefs for header
                    String name = snapshot.child("name").getValue(String.class);
                    if (getContext() != null && name != null) {
                        getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            .edit().putString("user_name", name).apply();
                    }
                } else {
                    Log.d("AccountFragment", "USER RECORD NOT FOUND in Firebase at /users/" + userId);
                    // Clear the loading text so it's not stuck
                    tvAddress.setText("Not Registered");
                    tvDob.setText("Not Registered");
                    tvGender.setText("Not Registered");
                    tvPhone.setText("Not Registered");
                    tvCountry.setText("Not Registered");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AccountFragment", "Firebase DB load failed: " + error.getMessage() + " (" + error.getCode() + ")");
                if (isAdded() && getContext() != null) {
                    if (tvAddress.getText().toString().equals("Loading...")) tvAddress.setText("Error: " + error.getMessage());
                }
            }
        });
    }

    private String getStringValue(DataSnapshot snapshot, String key, String defaultValue) {
        String val = snapshot.child(key).getValue(String.class);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    private void logout() {
        String userId = mAuth.getUid();
        
        // Requirement: "Also user info is also removed from firebase real-time db"
        if (userId != null) {
            mDatabase.child("users").child(userId).removeValue();
        }

        mAuth.signOut();
        
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(getActivity(), Login_Signup_page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }
}
