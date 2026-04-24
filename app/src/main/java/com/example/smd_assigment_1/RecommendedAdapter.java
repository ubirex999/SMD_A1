package com.example.smd_assigment_1;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.VH> {

    private final Context context;
    private final List<Product> products;
    private final DatabaseHelper dbHelper;

    public RecommendedAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.dbHelper = new DatabaseHelper(context);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productModel;
        TextView heartButton;
        MaterialButton btnBuy;

        VH(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productModel = itemView.findViewById(R.id.productModel);
            heartButton = itemView.findViewById(R.id.heartButton);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }

    private boolean isProductInFavourites(String productId) {
        Cursor cursor = dbHelper.getFavourites();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FAV_ID));
                if (id.equals(productId)) {
                    cursor.close();
                    return true;
                }
            }
            cursor.close();
        }
        return false;
    }

    private void setHeartUi(TextView heartButton, boolean isFavourite) {
        if (isFavourite) {
            heartButton.setText("♥");
            heartButton.setTextColor(context.getResources().getColor(R.color.magenta));
        } else {
            heartButton.setText("♡");
            heartButton.setTextColor(context.getResources().getColor(R.color.purple));
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
        
        // Handle images - use resource ID if available, otherwise default
        if (p.imageResId != 0) {
            holder.productImage.setImageResource(p.imageResId);
        } else {
            holder.productImage.setImageResource(R.drawable.headphones);
        }

        holder.productName.setText(p.name);
        holder.productPrice.setText(p.price);
        
        String info = p.modelOrInfo != null ? p.modelOrInfo : (p.type != null ? p.type : "");
        holder.productModel.setText(info);

        boolean fav = isProductInFavourites(p.id);
        setHeartUi(holder.heartButton, fav);

        holder.heartButton.setOnClickListener(v -> {
            if (isProductInFavourites(p.id)) {
                dbHelper.removeFavourite(p.id);
            } else {
                dbHelper.addFavourite(p);
            }
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, detail_buy.class);
            intent.putExtra("image", p.imageResId != 0 ? p.imageResId : R.drawable.headphones);
            intent.putExtra("price", p.price);
            intent.putExtra("name", p.name);
            intent.putExtra("model", info);
            intent.putExtra("detail", p.description != null ? p.description : "");
            intent.putExtra("productId", p.id);
            context.startActivity(intent);
        });

        holder.btnBuy.setOnClickListener(v -> {
            CartStore.getInstance(context).addToCart(p);
            Toast.makeText(context, p.name + " added to cart", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}
