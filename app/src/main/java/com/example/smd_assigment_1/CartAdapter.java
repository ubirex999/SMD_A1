package com.example.smd_assigment_1;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {

    public interface OnCartChangedListener {
        void onCartChanged();
    }

    private final Context context;
    private final DatabaseHelper dbHelper;
    private final List<CartStore.CartItem> items = new ArrayList<>();
    private final OnCartChangedListener listener;

    public CartAdapter(Context context, OnCartChangedListener listener) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.listener = listener;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productModel;
        TextView tvQuantity;
        TextView btnPlus;
        TextView btnMinus;
        ImageView btnRemove;

        VH(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.cartProductImage);
            productName = itemView.findViewById(R.id.cartProductName);
            productPrice = itemView.findViewById(R.id.cartProductPrice);
            productModel = itemView.findViewById(R.id.cartProductModel);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }

    public void setItems(List<CartStore.CartItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public List<CartStore.CartItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CartStore.CartItem item = items.get(position);

        holder.productImage.setImageResource(item.product.imageResId != 0 ? item.product.imageResId : R.drawable.headphones);
        holder.productName.setText(item.product.name);
        holder.productPrice.setText(item.product.price);
        holder.tvQuantity.setText(String.valueOf(item.quantity));

        holder.btnPlus.setOnClickListener(v -> {
            item.quantity++;
            // Note: This logic assumes CartStore or DB update is handled or not required here.
            // Since CartFragment uses CartStore, we should ideally update it there.
            // For now, updating local state for UI.
            holder.tvQuantity.setText(String.valueOf(item.quantity));
            if (listener != null) listener.onCartChanged();
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (item.quantity > 1) {
                item.quantity--;
                holder.tvQuantity.setText(String.valueOf(item.quantity));
                if (listener != null) listener.onCartChanged();
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                items.remove(pos);
                notifyItemRemoved(pos);
                if (listener != null) listener.onCartChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
