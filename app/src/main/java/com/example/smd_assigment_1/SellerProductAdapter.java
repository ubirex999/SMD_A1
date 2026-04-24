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

public class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public SellerProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_seller, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice().startsWith("$") ? product.getPrice() : "$" + product.getPrice());
        holder.tvType.setText(product.getType());
        
        // Use the new helper method in Product.java to get the keyword-based image
        int imageResId = product.getResolvedImageResId();
        holder.ivImage.setImageResource(imageResId);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, detail_buy.class);
            intent.putExtra("image", imageResId);
            intent.putExtra("price", product.getPrice());
            intent.putExtra("name", product.getName());
            intent.putExtra("model", product.getModelOrInfo() != null ? product.getModelOrInfo() : product.getType());
            intent.putExtra("detail", product.getDescription() != null ? product.getDescription() : "");
            intent.putExtra("productId", product.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvType;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivProductSeller);
            tvName = itemView.findViewById(R.id.tvProductNameSeller);
            tvPrice = itemView.findViewById(R.id.tvProductPriceSeller);
            tvType = itemView.findViewById(R.id.tvProductTypeSeller);
        }
    }
}
