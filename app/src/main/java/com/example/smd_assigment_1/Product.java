package com.example.smd_assigment_1;

import androidx.annotation.Keep;

@Keep
public class Product {
    public String id;
    public String name;
    public String type;
    public String price;
    public String description;
    public String sellerId;
    public String modelOrInfo;
    public int imageResId;
    public String originalPrice;

    public Product() {
        // Required for Firebase
    }

    // Comprehensive constructor for Catalog and Cart
    public Product(String id, String name, String price, String originalPrice, String description, String modelOrInfo, int imageResId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.description = description;
        this.modelOrInfo = modelOrInfo;
        this.imageResId = imageResId;
    }

    // Constructor for adding products (Firebase)
    public Product(String id, String name, String type, String price, String description, String sellerId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.description = description;
        this.sellerId = sellerId;
    }

    // Getters and Setters needed for Firebase synchronization
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getModelOrInfo() { return modelOrInfo; }
    public void setModelOrInfo(String modelOrInfo) { this.modelOrInfo = modelOrInfo; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }

    /**
     * Resolves the image resource ID based on the keyword in 'type'
     * if the stored imageResId is missing or default.
     */
    public int getResolvedImageResId() {
        // If we have a specific image assigned (not 0 and not default headphones), use it
        if (imageResId != 0 && imageResId != R.drawable.headphones) {
            return imageResId;
        }

        // Otherwise, look up by keyword in 'type'
        if (type == null) return R.drawable.headphones;
        
        String t = type.toLowerCase();
        if (t.contains("mobile") || t.contains("phone")) return R.drawable.mobile;
        if (t.contains("tv") || t.contains("television")) return R.drawable.television;
        if (t.contains("keyboard") || t.contains("keybord")) return R.drawable.keybord;
        if (t.contains("mouse")) return R.drawable.mouse;
        if (t.contains("mic") || t.contains("microphone")) return R.drawable.mic;
        if (t.contains("laptop") || t.contains("computer")) return R.drawable.laptop;
        
        return R.drawable.headphones;
    }
}
