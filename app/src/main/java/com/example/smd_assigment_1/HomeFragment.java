package com.example.smd_assigment_1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String PREFS_NAME = "auth_prefs";
    private static final String LOGGED_IN_KEY = "logged_in";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_NAME_KEY = "user_name";
    private static final String ACCOUNT_TYPE_KEY = "account_type";
    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView recommendedRv;
    private RecommendedAdapter adapter;
    private List<Product> productList;
    private DatabaseReference mDatabase;
    private ValueEventListener productsListener;
    private FloatingActionButton fabChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvHello = root.findViewById(R.id.tvHello);
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString(USER_NAME_KEY, "User");
        tvHello.setText("Hello " + name);

        Button btnLogout = root.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logout());

        fabChat = root.findViewById(R.id.fabChat);
        fabChat.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            startActivity(intent);
        });

        // Deals section — horizontal scrolling RecyclerView
        RecyclerView dealsRv = root.findViewById(R.id.recyclerDeals);
        dealsRv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        dealsRv.setAdapter(new DealsAdapter(requireContext(), ProductCatalog.getDeals()));

        // Recommended section — Dynamic sync with Firebase
        recommendedRv = root.findViewById(R.id.recyclerRecommended);
        recommendedRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        productList = new ArrayList<>();
        
        adapter = new RecommendedAdapter(requireContext(), productList);
        recommendedRv.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("products");
        loadDynamicProducts();

        return root;
    }

    private void loadDynamicProducts() {
        // Use ValueEventListener for real-time data synchronization
        if (productsListener != null) {
            mDatabase.removeEventListener(productsListener);
        }
        productsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                // Show products from Firebase in realtime
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Real-time sync failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mDatabase.addValueEventListener(productsListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDatabase != null && productsListener != null) {
            mDatabase.removeEventListener(productsListener);
            productsListener = null;
        }
    }

    private void logout() {
        // Clean up Firebase listener BEFORE signing out
        if (mDatabase != null && productsListener != null) {
            mDatabase.removeEventListener(productsListener);
            productsListener = null;
        }

        FirebaseAuth.getInstance().signOut();
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
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
