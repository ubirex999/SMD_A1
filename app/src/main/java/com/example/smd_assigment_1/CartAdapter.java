package com.example.smd_assigment_1;

import android.content.Context;
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
    private final CartStore cartStore;
    private final List<CartStore.CartItem> items = new ArrayList<>();
    private final OnCartChangedListener listener;

    public CartAdapter(Context context, OnCartChangedListener listener) {
        this.context = context;
        this.cartStore = new CartStore(context);
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
        if (newItems != null) items.addAll(newItems);
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
        CartStore.CartItem cartItem = items.get(position);
        Product p = cartItem.product;

        holder.productImage.setImageResource(p.imageResId);
        holder.productName.setText(p.name);
        holder.productPrice.setText(p.price);
        holder.productModel.setText(p.modelOrInfo);
        holder.tvQuantity.setText(String.valueOf(cartItem.quantity));

        // Plus button — increase quantity
        holder.btnPlus.setOnClickListener(v -> {
            cartItem.quantity++;
            cartStore.updateQuantity(p.id, cartItem.quantity);
            holder.tvQuantity.setText(String.valueOf(cartItem.quantity));
            if (listener != null) listener.onCartChanged();
        });

        // Minus button — decrease quantity (minimum 1)
        holder.btnMinus.setOnClickListener(v -> {
            if (cartItem.quantity > 1) {
                cartItem.quantity--;
                cartStore.updateQuantity(p.id, cartItem.quantity);
                holder.tvQuantity.setText(String.valueOf(cartItem.quantity));
                if (listener != null) listener.onCartChanged();
            }
        });

        // Three-dot icon — immediately remove from cart (no dialog)
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                cartStore.removeFromCart(items.get(pos).product.id);
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
