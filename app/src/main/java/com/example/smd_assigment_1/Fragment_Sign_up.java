package com.example.smd_assigment_1;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Sign_up#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Sign_up extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String PREFS_NAME = "auth_prefs";
    private static final String USERS_KEY = "users_json";
    private static final String PASSWORD_SALT = "smd_assigment_1_salt";
    private static final String LOGGED_IN_KEY = "logged_in";
    private static final String LOGGED_IN_EMAIL_KEY = "logged_in_email";

    public Fragment_Sign_up() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Sign_up.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Sign_up newInstance(String param1, String param2) {
        Fragment_Sign_up fragment = new Fragment_Sign_up();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment__sign_up, container, false);

        TextInputEditText emailEt = root.findViewById(R.id.etEmail_signup);
        TextInputEditText passwordEt = root.findViewById(R.id.etPassword_signup);
        TextInputEditText confirmEt = root.findViewById(R.id.etPassword_verify);
        MaterialButton signupBtn = root.findViewById(R.id.bt_signup);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        signupBtn.setOnClickListener(v -> {
            String email = emailEt.getText() != null ? emailEt.getText().toString().trim() : "";
            String password = passwordEt.getText() != null ? passwordEt.getText().toString().trim() : "";
            String confirmPassword = confirmEt.getText() != null ? confirmEt.getText().toString().trim() : "";

            emailEt.setError(null);
            passwordEt.setError(null);
            confirmEt.setError(null);

            if (email.isEmpty()) {
                emailEt.setError("Email is required");
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidEmail(email)) {
                emailEt.setError("Invalid email format");
                Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                passwordEt.setError("Password is required");
                Toast.makeText(requireContext(), "Please enter a password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidPassword(password)) {
                passwordEt.setError("Password must be 6+ chars and include letters and numbers");
                Toast.makeText(requireContext(), "Password does not meet requirements", Toast.LENGTH_SHORT).show();
                return;
            }
            if (confirmPassword.isEmpty()) {
                confirmEt.setError("Confirm password is required");
                Toast.makeText(requireContext(), "Please confirm your password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                confirmEt.setError("Passwords do not match");
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean created = registerWithPrefs(prefs, email, password);
            if (!created) {
                Toast.makeText(requireContext(), "Email already registered", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(requireContext(), "Signup successful. Please log in.", Toast.LENGTH_SHORT).show();
            clearLoggedIn(prefs);
            if (getActivity() instanceof Login_Signup_page) {
                ((Login_Signup_page) getActivity()).showLoginTab();
            }
        });

        return root;
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPassword(String password) {
        if (password == null) return false;
        String p = password.trim();
        if (p.isEmpty()) return false;
        if (p.length() < 6) return false;
        boolean hasLetter = p.matches(".*[A-Za-z].*");
        boolean hasDigit = p.matches(".*[0-9].*");
        if (!hasLetter || !hasDigit) return false;
        return !p.contains(" ");
    }

    private boolean registerWithPrefs(SharedPreferences prefs, String email, String password) {
        email = normalizeEmail(email);
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) return false;

        JSONObject users = getUsersJson(prefs);
        if (users.has(email)) return false;

        try {
            users.put(email, hashPassword(password));
            prefs.edit().putString(USERS_KEY, users.toString()).apply();
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private void clearLoggedIn(SharedPreferences prefs) {
        prefs.edit()
                .putBoolean(LOGGED_IN_KEY, false)
                .remove(LOGGED_IN_EMAIL_KEY)
                .apply();
    }

    private JSONObject getUsersJson(SharedPreferences prefs) {
        String json = prefs.getString(USERS_KEY, null);
        if (json == null || json.trim().isEmpty()) return new JSONObject();

        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((PASSWORD_SALT + password).getBytes(StandardCharsets.UTF_8));
            return toHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}