package com.example.smd_assigment_1;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.VH> {

    private final Context context;
    private final DatabaseHelper dbHelper;
    private final CartStore cartStore;
    private final List<Product> products = new ArrayList<>();

    public FavouritesAdapter(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.cartStore = CartStore.getInstance(context);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productModel;
        TextView productHidden;
        ImageView btnCart;
        ImageView btnMoreOptions;

        VH(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productModel = itemView.findViewById(R.id.productModel);
            productHidden = itemView.findViewById(R.id.product);
            btnCart = itemView.findViewById(R.id.btnCart);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }
    }

    public void setProducts(List<Product> newProducts) {
        products.clear();
        if (newProducts != null) products.addAll(newProducts);
        notifyDataSetChanged();
    }

    public void reloadFromDb() {
        products.clear();
        Cursor cursor = dbHelper.getFavourites();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FAV_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FAV_NAME));
                String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FAV_PRICE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FAV_TYPE));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FAV_DESC));

                Product p = new Product();
                p.setId(id);
                p.setName(name);
                p.setPrice(price);
                p.setType(type);
                p.setDescription(desc);
                p.setImageResId(p.getResolvedImageResId());
                p.setModelOrInfo(type);
                products.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        } else if (cursor != null) {
            cursor.close();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favourite, parent, false);
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

        // Cart icon — add product to cart (quantity = 1), stays in favourites
        holder.btnCart.setOnClickListener(v -> {
            if (p.imageResId == 0) {
                p.setImageResId(p.getResolvedImageResId());
            }
            cartStore.addToCart(p);
            Toast.makeText(context, p.name + " added to cart", Toast.LENGTH_SHORT).show();
        });

        // Triple-dot icon — show delete confirmation dialog
        holder.btnMoreOptions.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Remove Favourite")
                    .setMessage("Do you want to delete this product from favourites?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.removeFavourite(p.id);
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            products.remove(pos);
                            notifyItemRemoved(pos);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }
}
