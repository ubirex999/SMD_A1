package com.example.smd_assigment_1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.VH> {

    private final Context context;
    private final List<Product> products;
    private final FavouritesStore favouritesStore;

    public RecommendedAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.favouritesStore = new FavouritesStore(context);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productModel;
        TextView heartButton;
        TextView productHidden;

        VH(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productModel = itemView.findViewById(R.id.productModel);
            heartButton = itemView.findViewById(R.id.heartButton);
            productHidden = itemView.findViewById(R.id.product);
        }
    }

    private void setHeartUi(TextView heartButton, boolean isFavourite) {
        if (isFavourite) {
            heartButton.setText("♥");
            heartButton.setTextColor(context.getResources().getColor(R.color.magenta, context.getTheme()));
        } else {
            heartButton.setText("♡");
            heartButton.setTextColor(context.getResources().getColor(R.color.purple, context.getTheme()));
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = products.get(position);
        holder.productImage.setImageResource(p.imageResId);
        holder.productName.setText(p.name);
        holder.productPrice.setText(p.price);
        holder.productModel.setText(p.modelOrInfo);
        holder.productHidden.setText(p.id);

        boolean fav = favouritesStore.isFavourite(p.id);
        setHeartUi(holder.heartButton, fav);

        holder.heartButton.setOnClickListener(v -> {
            favouritesStore.toggleFavourite(p);
            boolean nowFav = favouritesStore.isFavourite(p.id);
            setHeartUi(holder.heartButton, nowFav);
            // Keep views consistent for reused holders.
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, detail_buy.class);
            intent.putExtra("image", p.imageResId);
            intent.putExtra("price", p.price);
            intent.putExtra("name", p.name);
            intent.putExtra("model", p.modelOrInfo != null ? p.modelOrInfo : "");
            intent.putExtra("detail", p.description != null ? p.description : "");
            intent.putExtra("productId", p.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}

