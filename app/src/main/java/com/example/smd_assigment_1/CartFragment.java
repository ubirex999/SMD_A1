package com.example.smd_assigment_1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.OnCartChangedListener {

    private static final int SMS_PERMISSION_CODE = 101;

    private TextView tvTotalPrice;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        tvTotalPrice = root.findViewById(R.id.tvTotalPrice);
        RecyclerView recycler = root.findViewById(R.id.recyclerCart);
        Button btnCheckout = root.findViewById(R.id.btnCheckout);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(requireContext(), this);
        recycler.setAdapter(adapter);

        // Load cart items
        loadCart();

        // Checkout — send SMS with order details
        btnCheckout.setOnClickListener(v -> {
            if (adapter.getItems().isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            sendCheckoutSms();
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    private void loadCart() {
        CartStore cartStore = CartStore.getInstance(requireContext());
        List<CartStore.CartItem> items = cartStore.getCartItems();
        adapter.setItems(items);
        updateTotal();
    }

    @Override
    public void onCartChanged() {
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (CartStore.CartItem item : adapter.getItems()) {
            // Parse price string like "$108.20"
            String priceStr = item.product.price.replaceAll("[^\\d.]", "");
            try {
                double price = Double.parseDouble(priceStr);
                total += price * item.quantity;
            } catch (NumberFormatException ignored) {
            }
        }
        tvTotalPrice.setText(String.format(Locale.US, "$%.2f", total));
    }

    private void sendCheckoutSms() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
            return;
        }
        doSendSms();
    }

    private void doSendSms() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Details:\n");

        double total = 0;
        for (CartStore.CartItem item : adapter.getItems()) {
            String priceStr = item.product.price.replaceAll("[^\\d.]", "");
            double price = 0;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException ignored) {
            }
            double lineTotal = price * item.quantity;
            total += lineTotal;

            sb.append(item.product.name)
              .append(" x").append(item.quantity)
              .append(" = $").append(String.format(Locale.US, "%.2f", lineTotal))
              .append("\n");
        }
        sb.append("Total: $").append(String.format(Locale.US, "%.2f", total));

        String number = "03104695189";
        String message = sb.toString();

        try {
            // Save order to Database for History
            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
            String orderId = "ORD-" + (int)(Math.random() * 9000 + 1000);
            String date = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
            
            java.util.List<OrderItem> orderItems = new java.util.ArrayList<>();
            for (CartStore.CartItem item : adapter.getItems()) {
                double price = 0;
                try {
                    price = Double.parseDouble(item.product.price.replaceAll("[^\\d.]", ""));
                } catch (Exception ignored) {}
                orderItems.add(new OrderItem(item.product.name, item.quantity, price));
            }
            
            Order order = new Order(orderId, "user_123", date, total, orderItems, "Processing");
            dbHelper.saveOrder(order);

            SmsManager smsManager = SmsManager.getDefault();
            // Split message if it's too long
            List<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(number, null, (java.util.ArrayList<String>) parts, null, null);
            
            Toast.makeText(requireContext(), "Order placed and history updated!", Toast.LENGTH_SHORT).show();
            
            // Clear Cart
            CartStore cartStore = CartStore.getInstance(requireContext());
            cartStore.clearCart();
            loadCart();
            
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Checkout failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doSendSms();
            } else {
                Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
