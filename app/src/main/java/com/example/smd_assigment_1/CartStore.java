package com.example.smd_assigment_1;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stores cart items in SharedPreferences with quantity tracking.
 */
public class CartStore {

    private static final String PREFS_NAME = "cart_prefs";
    private static final String KEY_CART_JSON = "cart_json";

    private static CartStore instance;
    private final SharedPreferences prefs;

    public static CartStore getInstance(Context context) {
        if (instance == null) {
            instance = new CartStore(context.getApplicationContext());
        }
        return instance;
    }

    private CartStore(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private JSONObject getCartJson() {
        String json = prefs.getString(KEY_CART_JSON, "{}");
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private void saveCartJson(JSONObject cart) {
        prefs.edit().putString(KEY_CART_JSON, cart.toString()).apply();
    }

    /**
     * Adds a product to the cart with quantity = 1.
     * If it already exists, the quantity is kept as-is.
     */
    public void addToCart(Product product) {
        JSONObject cart = getCartJson();
        if (!cart.has(product.id)) {
            try {
                int imageResId = product.imageResId != 0 ? product.imageResId : product.getResolvedImageResId();
                JSONObject entry = new JSONObject();
                entry.put("name", product.name);
                entry.put("price", product.price);
                if (product.originalPrice != null) entry.put("originalPrice", product.originalPrice);
                entry.put("description", product.description);
                entry.put("modelOrInfo", product.modelOrInfo);
                entry.put("type", product.type);
                if (product.sellerId != null) entry.put("sellerId", product.sellerId);
                entry.put("imageResId", imageResId);
                entry.put("quantity", 1);
                cart.put(product.id, entry);
                saveCartJson(cart);
            } catch (JSONException ignored) {
            }
        }
    }

    /**
     * Removes a product from the cart.
     */
    public void removeFromCart(String productId) {
        JSONObject cart = getCartJson();
        cart.remove(productId);
        saveCartJson(cart);
    }

    /**
     * Updates the quantity of a product in the cart.
     */
    public void updateQuantity(String productId, int quantity) {
        JSONObject cart = getCartJson();
        JSONObject entry = cart.optJSONObject(productId);
        if (entry != null) {
            try {
                entry.put("quantity", quantity);
                cart.put(productId, entry);
                saveCartJson(cart);
            } catch (JSONException ignored) {
            }
        }
    }

    public boolean isInCart(String productId) {
        return getCartJson().has(productId);
    }

    public void clearCart() {
        saveCartJson(new JSONObject());
    }

    /**
     * Returns all cart items as a list of CartItem objects.
     */
    public List<CartItem> getCartItems() {
        JSONObject cart = getCartJson();
        List<CartItem> items = new ArrayList<>();
        Iterator<String> keys = cart.keys();
        while (keys.hasNext()) {
            String id = keys.next();
            JSONObject entry = cart.optJSONObject(id);
            if (entry == null) continue;

            String name = entry.optString("name", id);
            String price = entry.optString("price", "$0.00");
            String originalPrice = entry.has("originalPrice") ? entry.optString("originalPrice") : null;
            String description = entry.optString("description", "");
            String modelOrInfo = entry.optString("modelOrInfo", "");
            String type = entry.optString("type", "");
            String sellerId = entry.optString("sellerId", null);
            int imageResId = entry.optInt("imageResId", 0);
            int quantity = entry.optInt("quantity", 1);

            // Try to get image from catalog if not stored
            if (imageResId == 0) {
                Product catalogProduct = ProductCatalog.findById(id);
                if (catalogProduct != null) imageResId = catalogProduct.imageResId;
            }

            Product product = new Product(id, name, price, originalPrice, description, modelOrInfo, imageResId);
            product.setType(type);
            product.setSellerId(sellerId);
            if (product.imageResId == 0) {
                product.setImageResId(product.getResolvedImageResId());
            }
            items.add(new CartItem(product, quantity));
        }
        return items;
    }

    /**
     * Simple holder for a product + its quantity in the cart.
     */
    public static class CartItem {
        public final Product product;
        public int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
