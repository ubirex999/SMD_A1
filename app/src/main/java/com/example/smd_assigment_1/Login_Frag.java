package com.example.smd_assigment_1;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login_Frag extends Fragment {

    private static final String TAG = "Login_Frag";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private MaterialButton loginBtn;

    private static final String PREFS_NAME = "auth_prefs";
    private static final String LOGGED_IN_KEY = "logged_in";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NAME_KEY = "user_name";
    private static final String ACCOUNT_TYPE_KEY = "account_type";

    public Login_Frag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        try {
            // Explicitly set the database URL to match your Firebase project setup
            mDatabase = FirebaseDatabase.getInstance("https://smd-assigment-1-default-rtdb.firebaseio.com").getReference();
            Log.d(TAG, "Database Reference initialized with explicit URL.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase Database", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login_, container, false);

        TextInputEditText emailEt = root.findViewById(R.id.etEmail_login);
        TextInputEditText passwordEt = root.findViewById(R.id.etPassword);
        loginBtn = root.findViewById(R.id.bt_login);
        progressBar = root.findViewById(R.id.pb_login);

        loginBtn.setOnClickListener(v -> {
            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);
            Log.d(TAG, "Attempting login for: " + email);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (getActivity() == null) return;
                        
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                final boolean[] hasRedirected = {false};
                                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                                Runnable timeoutRunnable = () -> {
                                    if (!hasRedirected[0] && isAdded() && getContext() != null) {
                                        setLoading(false);
                                        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                            .setTitle("Connectivity Issue")
                                            .setMessage("Database sync is taking too long. Please select your role to proceed:")
                                            .setPositiveButton("Seller", (dialog, which) -> {
                                                hasRedirected[0] = true;
                                                saveToSharedPrefs(user.getUid(), "User", "Seller");
                                                navigateToMain();
                                            })
                                            .setNegativeButton("Buyer", (dialog, which) -> {
                                                hasRedirected[0] = true;
                                                saveToSharedPrefs(user.getUid(), "User", "Buyer");
                                                navigateToMain();
                                            })
                                            .setCancelable(false)
                                            .show();
                                    }
                                };
                                handler.postDelayed(timeoutRunnable, 4000);

                                mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (hasRedirected[0] || !isAdded()) return;
                                        
                                        String name = snapshot.child("name").getValue(String.class);
                                        String accountType = snapshot.child("accountType").getValue(String.class);
                                        
                                        if (accountType != null) {
                                            hasRedirected[0] = true;
                                            handler.removeCallbacks(timeoutRunnable);
                                            setLoading(false);
                                            saveToSharedPrefs(user.getUid(), name, accountType);
                                            navigateToMain();
                                        } else {
                                            // Fallback: Check the role_registry by email
                                            String email = user.getEmail();
                                            if (email != null) {
                                                String encodedEmail = email.replace(".", ",");
                                                mDatabase.child("role_registry").child(encodedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot roleSnapshot) {
                                                        if (hasRedirected[0] || !isAdded()) return;
                                                        hasRedirected[0] = true;
                                                        handler.removeCallbacks(timeoutRunnable);
                                                        setLoading(false);
                                                        
                                                        String type = roleSnapshot.getValue(String.class);
                                                        saveToSharedPrefs(user.getUid(), name, type != null ? type : "Buyer");
                                                        navigateToMain();
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        // If both fail, let the timeout handle it
                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        if (hasRedirected[0] || !isAdded()) return;
                                        Log.e(TAG, "DB Load error: " + error.getMessage());
                                    }
                                });
                            } else {
                                setLoading(false);
                            }
                        } else {
                            setLoading(false);
                            setLoading(false);
                            String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                            Log.e(TAG, "Login failed: " + error);
                            Toast.makeText(getContext(), "Login failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        return root;
    }

    private void setLoading(boolean isLoading) {
        if (loginBtn != null) loginBtn.setEnabled(!isLoading);
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void fetchUserInfoAndSync(String userId) {
        // This runs in the background. Even if it hangs, the user is already in MainActivity.
        if (mDatabase == null) return;
        
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String accountType = snapshot.child("accountType").getValue(String.class);
                    Log.d(TAG, "Background sync: User data found: " + name);
                    saveToSharedPrefs(userId, name, accountType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Background sync cancelled: " + error.getMessage());
            }
        });
    }

    private void navigateToMain() {
        if (getActivity() != null) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void saveToSharedPrefs(String userId, String name, String accountType) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit()
                .putString(USER_ID_KEY, userId)
                .putString(USER_NAME_KEY, name != null ? name : "User")
                .putString(ACCOUNT_TYPE_KEY, accountType != null ? accountType : "Buyer")
                .putBoolean(LOGGED_IN_KEY, true)
                .apply();
    }
}
