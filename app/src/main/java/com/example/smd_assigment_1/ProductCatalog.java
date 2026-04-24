package com.example.smd_assigment_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hard-coded catalog for the assignment.
 */
public class ProductCatalog {

    private static final List<Product> DEALS = new ArrayList<>();
    private static final List<Product> RECOMMENDED = new ArrayList<>();
    private static final Map<String, Product> ALL_BY_ID = new HashMap<>();

    static {
        // Exactly 3 deals of the day (required).
        DEALS.add(new Product(
                "deal_1",
                "Sony Premium Dynamic",
                "$108.20",
                "$199.99",
                "Dynamic microphone speaker for studio-ready sound.",
                "Model: WH-1000XM5",
                R.drawable.headphones
        ));
        DEALS.add(new Product(
                "deal_2",
                "Razor Wireless Headphones",
                "$59.99",
                "$119.99",
                "Ultra-comfort wireless headphones with deep bass.",
                "Model: WH-1000XM4",
                R.drawable.headphones
        ));
        DEALS.add(new Product(
                "deal_3",
                "Sony Premium Wireless",
                "$39.99",
                "$79.99",
                "Lightweight wireless earbuds for everyday listening.",
                "Model: WH-2000XM2",
                R.drawable.keybord
        ));


        RECOMMENDED.add(new Product("p_1", "Premium Wireless Headphones", "$349.99", null, "Noise canceling and studio-quality audio.", "Model: WH-1000XM5", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_2", "Sony Premium Wireless", "$299.99", null, "Comfort fit with long battery life.", "Model: WH-1000XM4", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_3", "Razor Mechanical Keyboard", "$39.99", null, "Ergonomic keys with hot-swap support.", "Model: AR-8750XR7", R.drawable.keybord));
        RECOMMENDED.add(new Product("p_4", "Razor Lightfeather Mouse", "$29.99", null, "Precision tracking with smooth scroll.", "Model: MX-900", R.drawable.mouse));
        RECOMMENDED.add(new Product("p_5", "Studio Dynamic Microphone", "$89.99", null, "Clear vocals with cardioid pickup.", "Model: DM-100", R.drawable.mic));
        RECOMMENDED.add(new Product("p_6", "Wireless Earbuds Pro", "$79.99", null, "Compact earbuds with punchy bass.", "Model: EB-Pro", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_7", "Noise Cancelling Headphones", "$229.99", null, "Adaptive noise canceling features.", "Model: NC-2", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_8", "Gaming Keyboard RGB", "$69.99", null, "Programmable keys and smooth response.", "Model: GK-RGB", R.drawable.keybord));
        RECOMMENDED.add(new Product("p_9", "Ergonomic Mouse", "$25.99", null, "Designed for comfortable long sessions.", "Model: EM-1", R.drawable.mouse));
        RECOMMENDED.add(new Product("p_10", "Budget Wireless Headphones", "$49.99", null, "Everyday listening with deep sound.", "Model: BWH-5", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_11", "Pro Keyboard Mechanical", "$59.99", null, "Durable switches with satisfying feedback.", "Model: PK-MK", R.drawable.keybord));
        RECOMMENDED.add(new Product("p_12", "Premium Gaming Mouse", "$39.99", null, "Fast tracking with adjustable DPI.", "Model: G-MX", R.drawable.mouse));
        RECOMMENDED.add(new Product("p_13", "Microphone for Streaming", "$109.99", null, "Crisp voice clarity for content creation.", "Model: ST-MIC", R.drawable.mic));
        RECOMMENDED.add(new Product("p_14", "Wireless Headphones Mini", "$89.99", null, "Lightweight design for daily travel.", "Model: Mini-1", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_15", "Keyboard Compact 98 keys", "$49.99", null, "Reliable typing with hot-swap layout.", "Model: C98-KB", R.drawable.keybord));
        RECOMMENDED.add(new Product("p_16", "Mouse Silent Click", "$19.99", null, "Reduced click noise for shared spaces.", "Model: SC-MX", R.drawable.mouse));
        RECOMMENDED.add(new Product("p_17", "Studio Headphones Classic", "$129.99", null, "Balanced sound tuned for mixing.", "Model: SH-CL", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_18", "Wireless Earbuds Sport", "$59.99", null, "Secure fit for workouts.", "Model: EB-Sport", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_19", "Mechanical Keyboard Pro", "$89.99", null, "Enhanced stability and smoother keystrokes.", "Model: MK-Pro", R.drawable.keybord));
        RECOMMENDED.add(new Product("p_20", "Mouse Precision 12000 DPI", "$44.99", null, "Sharper aim and consistent tracking.", "Model: 12K-DPI", R.drawable.mouse));
        RECOMMENDED.add(new Product("p_21", "Dynamic Mic Podcast", "$69.99", null, "Broadcast-ready voice for podcasts.", "Model: PD-MIC", R.drawable.mic));
        RECOMMENDED.add(new Product("p_22", "Wireless Headphones Comfort", "$159.99", null, "Soft pads and long-lasting comfort.", "Model: WC-10", R.drawable.headphones));
        RECOMMENDED.add(new Product("p_23", "Keyboard Wireless Combo", "$79.99", null, "A complete keyboard + pad set.", "Model: KB-WL", R.drawable.keybord));
        RECOMMENDED.add(new Product("p_24", "Mouse Travel Edition", "$24.99", null, "Portable mouse for laptops.", "Model: TR-MX", R.drawable.mouse));
        RECOMMENDED.add(new Product("p_25", "Desktop Boom Arm", "$34.99", null, "Flexible arm for studio microphones.", "Model: BA-300", R.drawable.mic));
        RECOMMENDED.add(new Product("p_26", "Ultra-wide Gaming Mat", "$15.99", null, "Smooth surface for precise mouse control.", "Model: GM-XL", R.drawable.mouse));
        RECOMMENDED.add(new Product("p_27", "USB-C Audio Interface", "$149.99", null, "Professional audio conversion for PC.", "Model: AI-USB", R.drawable.mic));
        RECOMMENDED.add(new Product("p_28", "Custom PBT Keycaps", "$29.99", null, "Double-shot keycaps for customization.", "Model: KC-PBT", R.drawable.keybord));
        RECOMMENDED.add(new Product("p_29", "External SSD 1TB", "$99.99", null, "High-speed storage for your data.", "Model: SSD-1T", R.drawable.mouse));
        RECOMMENDED.add(new Product("p_30", "Wireless Trackpad", "$54.99", null, "Multi-touch support for efficient workflow.", "Model: TP-WL", R.drawable.mouse));

        for (Product p : DEALS) {
            ALL_BY_ID.put(p.id, p);
        }
        for (Product p : RECOMMENDED) {
            ALL_BY_ID.put(p.id, p);
        }
    }

    public static List<Product> getDeals() {
        return DEALS;
    }

    public static List<Product> getRecommended() {
        return RECOMMENDED;
    }

    public static Product findById(String productId) {
        return ALL_BY_ID.get(productId);
    }
}

