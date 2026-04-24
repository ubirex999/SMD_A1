package com.example.smd_assigment_1;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Fragment_Sign_up extends Fragment {

    private static final String TAG = "Fragment_Sign_up";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private MaterialButton signupBtn;

    private static final String PREFS_NAME = "auth_prefs";
    private static final String LOGGED_IN_KEY = "logged_in";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NAME_KEY = "user_name";
    private static final String ACCOUNT_TYPE_KEY = "account_type";

    public Fragment_Sign_up() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        try {
            mDatabase = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            Log.e(TAG, "Database init failed", e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment__sign_up, container, false);

        EditText nameEt = root.findViewById(R.id.etName_signup);
        EditText emailEt = root.findViewById(R.id.etEmail_signup);
        EditText passwordEt = root.findViewById(R.id.etPassword_signup);
        EditText confirmEt = root.findViewById(R.id.etPassword_verify);
        EditText addressEt = root.findViewById(R.id.etAddress_signup);
        EditText phoneEt = root.findViewById(R.id.etPhone_signup);
        EditText countryEt = root.findViewById(R.id.etCountry_signup);
        EditText dobEt = root.findViewById(R.id.etDob_signup);
        RadioGroup genderRg = root.findViewById(R.id.rgGender_signup);
        RadioGroup accountTypeRg = root.findViewById(R.id.rgAccountType_signup);
        signupBtn = root.findViewById(R.id.bt_signup);
        
        progressBar = root.findViewById(R.id.pb_signup);
        if (progressBar != null) progressBar.setVisibility(View.GONE);

        signupBtn.setOnClickListener(v -> {
            String name = nameEt.getText().toString().trim();
            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();
            String confirmPassword = confirmEt.getText().toString().trim();
            String address = addressEt.getText().toString().trim();
            String phone = phoneEt.getText().toString().trim();
            String country = countryEt.getText().toString().trim();
            String dob = dobEt.getText().toString().trim();

            int selectedGenderId = genderRg.getCheckedRadioButtonId();
            String gender = "";
            if (selectedGenderId != -1) {
                RadioButton rb = root.findViewById(selectedGenderId);
                gender = rb.getText().toString();
            }

            int selectedAccountTypeId = accountTypeRg.getCheckedRadioButtonId();
            String accountType = "";
            if (selectedAccountTypeId != -1) {
                RadioButton rb = root.findViewById(selectedAccountTypeId);
                accountType = rb.getText().toString();
            }

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || 
                phone.isEmpty() || country.isEmpty() || dob.isEmpty() || gender.isEmpty() || accountType.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);
            
            String finalGender = gender;
            String finalAccountType = accountType;
            
            Log.d(TAG, "Creating user: " + email);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Signup Auth Successful");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                
                                // Save to database in background
                                saveUserToDatabaseInBackground(userId, name, email, address, phone, country, dob, finalGender, finalAccountType);
                                
                                // Save to SharedPrefs and transition immediately
                                saveToSharedPrefs(userId, name, finalAccountType);
                                
                                Intent intent = new Intent(getContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                if (getActivity() != null) getActivity().finish();
                            }
                        } else {
                            setLoading(false);
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getContext(), "This email is already registered.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Signup failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        return root;
    }

    private void setLoading(boolean isLoading) {
        if (signupBtn != null) signupBtn.setEnabled(!isLoading);
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void saveUserToDatabaseInBackground(String userId, String name, String email, String address, String phone, String country, String dob, String gender, String accountType) {
        if (mDatabase == null) return;
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("address", address);
        userMap.put("phone", phone);
        userMap.put("country", country);
        userMap.put("dob", dob);
        userMap.put("gender", gender);
        userMap.put("accountType", accountType);

        mDatabase.child("users").child(userId).setValue(userMap)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Database save successful"))
                .addOnFailureListener(e -> Log.e(TAG, "Database save failed", e));
        
        // Also save to a role registry for easier lookup
        String encodedEmail = email.replace(".", ",");
        mDatabase.child("role_registry").child(encodedEmail).setValue(accountType);
    }

    private void saveToSharedPrefs(String userId, String name, String accountType) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit()
                .putString(USER_ID_KEY, userId)
                .putString(USER_NAME_KEY, name)
                .putString(ACCOUNT_TYPE_KEY, accountType)
                .putBoolean(LOGGED_IN_KEY, true)
                .apply();
    }
}
