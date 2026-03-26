package com.example.smd_assigment_1;

/**
 * Simple product model used for the Home screen and favourites storage.
 */
public class Product {
    public final String id;
    public final String name;
    public final String price;
    public final String originalPrice; // optional (deals)
    public final String description;   // deals short description
    public final String modelOrInfo;   // recommended model/description
    public final int imageResId;

    public Product(
            String id,
            String name,
            String price,
            String originalPrice,
            String description,
            String modelOrInfo,
            int imageResId
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.description = description;
        this.modelOrInfo = modelOrInfo;
        this.imageResId = imageResId;
    }
}

