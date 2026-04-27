package com.example.smd_assigment_1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private boolean isSeller;

    public OrderAdapter(List<Order> orderList) {
        this(orderList, false);
    }

    public OrderAdapter(List<Order> orderList, boolean isSeller) {
        this.orderList = orderList;
        this.isSeller = isSeller;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvId.setText("#" + (order.getOrderId() != null ? order.getOrderId() : "N/A"));
        
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "PROCESSING";
        holder.tvStatus.setText(status);
        
        if (status.equals("DELIVERED")) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_delivered);
            holder.tvStatus.setTextColor(0xFF1976D2);
            holder.btnTrack.setVisibility(View.GONE);
        } else if (status.equals("IN TRANSIT")) {
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_transit);
            holder.tvStatus.setTextColor(0xFFEF6C00);
            holder.btnTrack.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.status_bg_processing);
            holder.tvStatus.setTextColor(0xFF616161);
            holder.btnTrack.setVisibility(View.GONE);
        }

        holder.tvDate.setText(order.getDate() != null ? order.getDate() : "No Date");
        holder.tvTotal.setText(String.format(Locale.US, "$%.2f", order.getTotalAmount()));

        // Show buyer name for seller view
        if (isSeller && order.getBuyerName() != null && !order.getBuyerName().isEmpty()) {
            holder.tvBuyerName.setVisibility(View.VISIBLE);
            holder.tvBuyerName.setText("Buyer: " + order.getBuyerName());
        } else {
            holder.tvBuyerName.setVisibility(View.GONE);
        }

        StringBuilder summary = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                summary.append("• ").append(item.getProductName())
                        .append(" (Qty: ").append(item.getQuantity()).append(")")
                        .append(" - $").append(String.format(Locale.US, "%.2f", item.getPrice()))
                        .append("\n");
            }
        } else {
            summary.append("No items found");
        }
        holder.tvSummary.setText(summary.toString().trim());
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvStatus, tvDate, tvSummary, tvTotal, tvBuyerName;
        View btnTrack;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvSummary = itemView.findViewById(R.id.tvOrderSummary);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
            btnTrack = itemView.findViewById(R.id.btnTrack);
            tvBuyerName = itemView.findViewById(R.id.tvBuyerName);
        }
    }
}
