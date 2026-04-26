package com.example.smd_assigment_1;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private List<Order> orderList;
    private static final String DB_URL = "https://smd-assigment-1-default-rtdb.asia-southeast1.firebasedatabase.app";
    private DatabaseReference ordersRef;
    private ValueEventListener ordersListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_order_history, container, false);

        rvOrders = root.findViewById(R.id.rvOrderHistory);
        View btnBack = root.findViewById(R.id.btnBack);
        
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    // Check account type to return to correct home
                    android.content.SharedPreferences prefs = requireContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE);
                    String type = prefs.getString("account_type", "Buyer");
                    if ("Seller".equalsIgnoreCase(type)) {
                        // For seller, just close and show the seller home which is already there?
                        // Actually replace with SellerHomeFragment to be sure
                        ((MainActivity) getActivity()).onBackPressed(); 
                    } else {
                        ((MainActivity) getActivity()).onBackPressed();
                    }
                }
            });
        }
        
        orderList = new ArrayList<>();

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(orderList);
        rvOrders.setAdapter(adapter);

        loadOrderHistory();

        return root;
    }

    private void loadOrderHistory() {
        if (getContext() == null) return;

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE);
        String type = prefs.getString("account_type", "Buyer");

        String node = "Seller".equalsIgnoreCase(type) ? ("seller_orders/" + uid) : ("user_orders/" + uid);
        ordersRef = FirebaseDatabase.getInstance(DB_URL).getReference(node);

        if (ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                orderList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Order order = data.getValue(Order.class);
                    if (order != null) {
                        orderList.add(order);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load orders: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        ordersRef.addValueEventListener(ordersListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
            ordersListener = null;
        }
    }
}
