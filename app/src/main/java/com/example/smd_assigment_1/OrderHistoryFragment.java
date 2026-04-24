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

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private List<Order> orderList;

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
        
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        Cursor cursor = dbHelper.getAllOrders();
        
        orderList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ID));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_USER_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_DATE));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS));
                
                // Load items for this order
                List<OrderItem> items = new ArrayList<>();
                Cursor itemCursor = dbHelper.getOrderItems(id);
                if (itemCursor != null && itemCursor.moveToFirst()) {
                    do {
                        String pName = itemCursor.getString(itemCursor.getColumnIndexOrThrow(DatabaseHelper.COL_OI_PROD_NAME));
                        int qty = itemCursor.getInt(itemCursor.getColumnIndexOrThrow(DatabaseHelper.COL_OI_QTY));
                        double price = itemCursor.getDouble(itemCursor.getColumnIndexOrThrow(DatabaseHelper.COL_OI_PRICE));
                        items.add(new OrderItem(pName, qty, price));
                    } while (itemCursor.moveToNext());
                    itemCursor.close();
                }
                
                orderList.add(new Order(id, userId, date, total, items, status));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }
}
