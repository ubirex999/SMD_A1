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
import androidx.recyclerview.widget.GridLayoutManager;
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

public class SellerHomeFragment extends Fragment {

    private static final String TAG = "SellerHomeFragment";
    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";
    private TextView tvHello;
    private RecyclerView rvProducts;
    private FloatingActionButton fabAdd;
    private View btnLogout;
    private DatabaseReference mDatabase;
    private SellerProductAdapter adapter;
    private List<Product> productList;
    private ValueEventListener productsListener;

    private static final String PREFS_NAME = "auth_prefs";
    private static final String LOGGED_IN_KEY = "logged_in";
    private static final String USER_NAME_KEY = "user_name";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_seller_home, container, false);

        tvHello = root.findViewById(R.id.tvHelloSeller);
        rvProducts = root.findViewById(R.id.rvSellerProducts);
        fabAdd = root.findViewById(R.id.fabAddProduct);
        btnLogout = root.findViewById(R.id.btnLogoutSeller);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString(USER_NAME_KEY, "Seller");
        View btnMenu = root.findViewById(R.id.btnMenu);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openDrawer();
                }
            });
        }

        if (tvHello != null) {
            tvHello.setText("Hello " + name);
        }

        mDatabase = FirebaseDatabase.getInstance(DB_URL).getReference("products");
        productList = new ArrayList<>();
        
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new SellerProductAdapter(getContext(), productList);
        rvProducts.setAdapter(adapter);

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ProductAddActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }

        loadSellerProducts();

        return root;
    }

    private void loadSellerProducts() {
        String sellerId = FirebaseAuth.getInstance().getUid();
        if (sellerId == null) return;

        if (productsListener != null) {
            mDatabase.removeEventListener(productsListener);
        }
        productsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                productList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null && sellerId.equals(product.getSellerId())) {
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                .remove("user_id")
                .remove("user_name")
                .remove("account_type")
                .apply();

        Intent intent = new Intent(getActivity(), Login_Signup_page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
