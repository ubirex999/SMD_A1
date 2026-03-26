package com.example.smd_assigment_1;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stores favourite products in SharedPreferences.
 */
public class FavouritesStore {

    private static final String PREFS_NAME = "favourites_prefs";
    private static final String KEY_FAVOURITES_JSON = "favourites_json";

    // Extra shared pref (not used, required by assignment)
    private final SharedPreferences favListFavourites;

    private final SharedPreferences prefs;

    public FavouritesStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.favListFavourites = context.getApplicationContext().getSharedPreferences("favList.favourites", Context.MODE_PRIVATE);
    }

    private JSONObject getFavouritesJson() {
        String json = prefs.getString(KEY_FAVOURITES_JSON, "{}");
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }

    private void saveFavouritesJson(JSONObject favourites) {
        prefs.edit().putString(KEY_FAVOURITES_JSON, favourites.toString()).apply();
    }

    public boolean isFavourite(String productId) {
        JSONObject favourites = getFavouritesJson();
        return favourites.has(productId);
    }

    /**
     * Toggle favourite status and persist metadata.
     */
    public void toggleFavourite(Product product) {
        JSONObject favourites = getFavouritesJson();
        try {
            if (favourites.has(product.id)) {
                favourites.remove(product.id);
            } else {
                JSONObject entry = new JSONObject();
                entry.put("name", product.name);
                entry.put("price", product.price);
                if (product.originalPrice != null) entry.put("originalPrice", product.originalPrice);
                entry.put("description", product.description);
                entry.put("modelOrInfo", product.modelOrInfo);
                favourites.put(product.id, entry);
            }
            saveFavouritesJson(favourites);
        } catch (JSONException ignored) {
        }
    }

    public List<Product> getFavouriteProducts() {
        JSONObject favourites = getFavouritesJson();
        List<Product> result = new ArrayList<>();
        Iterator<String> keys = favourites.keys();
        while (keys.hasNext()) {
            String id = keys.next();
            JSONObject entry = favourites.optJSONObject(id);
            if (entry == null) continue;

            Product catalogProduct = ProductCatalog.findById(id);
            int imageRes = catalogProduct != null ? catalogProduct.imageResId : 0;

            String name = entry.optString("name", id);
            String price = entry.optString("price", "");
            String originalPrice = entry.has("originalPrice") ? entry.optString("originalPrice", null) : null;
            String description = entry.optString("description", "");
            String modelOrInfo = entry.optString("modelOrInfo", "");

            result.add(new Product(id, name, price, originalPrice, description, modelOrInfo, imageRes));
        }
        return result;
    }
}

