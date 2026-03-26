package com.example.smd_assigment_1;

import android.graphics.Paint;
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

public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.VH> {

    private final Context context;
    private final List<Product> deals;
    private final FavouritesStore favouritesStore;

    public DealsAdapter(Context context, List<Product> deals) {
        this.context = context;
        this.deals = deals;
        this.favouritesStore = new FavouritesStore(context);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle;
        TextView productPrice;
        TextView originalPrice;
        TextView productDescription;
        TextView heartButton;
        TextView productHidden;

        VH(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productPrice);
            originalPrice = itemView.findViewById(R.id.originalPrice);
            productDescription = itemView.findViewById(R.id.productDescription);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deal, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = deals.get(position);

        holder.productImage.setImageResource(p.imageResId);
        holder.productTitle.setText(p.name);
        holder.productPrice.setText(p.price);
        holder.originalPrice.setText(p.originalPrice != null ? p.originalPrice : "");
        holder.originalPrice.setVisibility(p.originalPrice != null ? View.VISIBLE : View.GONE);
        // Apply strikethrough to original price.
        holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.productDescription.setText(p.description);

        holder.productHidden.setText(p.id);
        holder.productHidden.setVisibility(View.GONE);

        boolean fav = favouritesStore.isFavourite(p.id);
        setHeartUi(holder.heartButton, fav);

        holder.heartButton.setOnClickListener(v -> {
            favouritesStore.toggleFavourite(p);
            boolean nowFav = favouritesStore.isFavourite(p.id);
            setHeartUi(holder.heartButton, nowFav);
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
        return deals.size();
    }
}
