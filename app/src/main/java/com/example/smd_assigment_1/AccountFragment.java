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
    private ValueEventListener userListener;
    private ValueEventListener connectedListener;

    private static final String PREFS_NAME = "auth_prefs";
    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String LOGGED_IN_KEY = "logged_in";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NAME_KEY = "user_name";
    private static final String ACCOUNT_TYPE_KEY = "account_type";

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
        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference();

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
        if (connectedListener != null) {
            mDatabase.child(".info/connected").removeEventListener(connectedListener);
        }
        connectedListener = new ValueEventListener() {
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
        };
        mDatabase.child(".info/connected").addValueEventListener(connectedListener);

        if (userListener != null) {
            mDatabase.child("users").child(userId).removeEventListener(userListener);
        }
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                
                if (snapshot.exists()) {
                    Log.d("AccountFragment", "User record found for: " + userId + ". Data: " + snapshot.getValue());
                    
                    // Helper to get values safely
                    tvName.setText(getStringValue(snapshot, "name", "Not Set"));
                    String emailFromDb = getStringValue(snapshot, "email", null);
                    if (emailFromDb != null) {
                        tvEmail.setText(emailFromDb);
                    } else if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                        tvEmail.setText(mAuth.getCurrentUser().getEmail());
                    } else {
                        tvEmail.setText("Not Set");
                    }
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
                    tvAddress.setText("Error: " + error.getMessage());
                    tvDob.setText("Error");
                    tvGender.setText("Error");
                    tvPhone.setText("Error");
                    tvCountry.setText("Error");
                }
            }
        };
        mDatabase.child("users").child(userId).addValueEventListener(userListener);
    }

    private String getStringValue(DataSnapshot snapshot, String key, String defaultValue) {
        String val = snapshot.child(key).getValue(String.class);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    private void logout() {
        // Clean up Firebase listeners BEFORE signing out
        // (after signOut, mAuth.getUid() returns null and listeners can't be removed)
        String userId = mAuth != null ? mAuth.getUid() : null;
        if (mDatabase != null) {
            if (connectedListener != null) {
                mDatabase.child(".info/connected").removeEventListener(connectedListener);
                connectedListener = null;
            }
            if (userListener != null && userId != null) {
                mDatabase.child("users").child(userId).removeEventListener(userListener);
                userListener = null;
            }
        }

        mAuth.signOut();

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(LOGGED_IN_KEY, false)
                .remove(USER_ID_KEY)
                .remove(USER_NAME_KEY)
                .remove(ACCOUNT_TYPE_KEY)
                .apply();

        Intent intent = new Intent(getActivity(), Login_Signup_page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        String userId = mAuth != null ? mAuth.getUid() : null;
        if (mDatabase != null) {
            if (connectedListener != null) {
                mDatabase.child(".info/connected").removeEventListener(connectedListener);
                connectedListener = null;
            }
            if (userListener != null && userId != null) {
                mDatabase.child("users").child(userId).removeEventListener(userListener);
                userListener = null;
            }
        }
    }
}
