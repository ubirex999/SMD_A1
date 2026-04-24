package com.example.smd_assigment_1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShopLocal.db";
    private static final int DATABASE_VERSION = 2;

    // Favourites Table
    public static final String TABLE_FAVOURITES = "favourites";
    public static final String COL_FAV_ID = "id";
    public static final String COL_FAV_NAME = "name";
    public static final String COL_FAV_PRICE = "price";
    public static final String COL_FAV_TYPE = "type";
    public static final String COL_FAV_DESC = "description";

    // Cart Table
    public static final String TABLE_CART = "cart";
    public static final String COL_CART_ID = "id";
    public static final String COL_CART_NAME = "name";
    public static final String COL_CART_PRICE = "price";
    public static final String COL_CART_QTY = "quantity";

    // Orders Table
    public static final String TABLE_ORDERS = "orders";
    public static final String COL_ORDER_ID = "order_id";
    public static final String COL_ORDER_USER_ID = "user_id";
    public static final String COL_ORDER_DATE = "date";
    public static final String COL_ORDER_TOTAL = "total_amount";
    public static final String COL_ORDER_STATUS = "status";

    // Order Items Table
    public static final String TABLE_ORDER_ITEMS = "order_items";
    public static final String COL_OI_ID = "oi_id";
    public static final String COL_OI_ORDER_ID = "order_id";
    public static final String COL_OI_PROD_NAME = "product_name";
    public static final String COL_OI_QTY = "quantity";
    public static final String COL_OI_PRICE = "price";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createFavTable = "CREATE TABLE " + TABLE_FAVOURITES + " (" +
                COL_FAV_ID + " TEXT PRIMARY KEY, " +
                COL_FAV_NAME + " TEXT, " +
                COL_FAV_PRICE + " TEXT, " +
                COL_FAV_TYPE + " TEXT, " +
                COL_FAV_DESC + " TEXT)";

        String createCartTable = "CREATE TABLE " + TABLE_CART + " (" +
                COL_CART_ID + " TEXT PRIMARY KEY, " +
                COL_CART_NAME + " TEXT, " +
                COL_CART_PRICE + " TEXT, " +
                COL_CART_QTY + " INTEGER)";

        String createOrderTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COL_ORDER_ID + " TEXT PRIMARY KEY, " +
                COL_ORDER_USER_ID + " TEXT, " +
                COL_ORDER_DATE + " TEXT, " +
                COL_ORDER_TOTAL + " REAL, " +
                COL_ORDER_STATUS + " TEXT)";

        String createOrderItemsTable = "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                COL_OI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OI_ORDER_ID + " TEXT, " +
                COL_OI_PROD_NAME + " TEXT, " +
                COL_OI_QTY + " INTEGER, " +
                COL_OI_PRICE + " REAL)";

        db.execSQL(createFavTable);
        db.execSQL(createCartTable);
        db.execSQL(createOrderTable);
        db.execSQL(createOrderItemsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        onCreate(db);
    }

    // ... (Favourites and Cart operations remain same)

    // Orders Operations
    public void saveOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ORDER_ID, order.getOrderId());
        values.put(COL_ORDER_USER_ID, order.getUserId());
        values.put(COL_ORDER_DATE, order.getDate());
        values.put(COL_ORDER_TOTAL, order.getTotalAmount());
        values.put(COL_ORDER_STATUS, order.getStatus());

        db.insert(TABLE_ORDERS, null, values);

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                ContentValues iValues = new ContentValues();
                iValues.put(COL_OI_ORDER_ID, order.getOrderId());
                iValues.put(COL_OI_PROD_NAME, item.getProductName());
                iValues.put(COL_OI_QTY, item.getQuantity());
                iValues.put(COL_OI_PRICE, item.getPrice());
                db.insert(TABLE_ORDER_ITEMS, null, iValues);
            }
        }
    }

    public Cursor getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ORDERS + " ORDER BY " + COL_ORDER_DATE + " DESC", null);
    }

    public Cursor getOrderItems(String orderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ORDER_ITEMS + " WHERE " + COL_OI_ORDER_ID + "=?", new String[]{orderId});
    }

    // Favourites Operations
    public boolean addFavourite(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAV_ID, product.getId());
        values.put(COL_FAV_NAME, product.getName());
        values.put(COL_FAV_PRICE, product.getPrice());
        values.put(COL_FAV_TYPE, product.getType());
        values.put(COL_FAV_DESC, product.getDescription());

        long result = db.insertWithOnConflict(TABLE_FAVOURITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public void removeFavourite(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVOURITES, COL_FAV_ID + "=?", new String[]{productId});
    }

    public Cursor getFavourites() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_FAVOURITES, null);
    }

    // Cart Operations
    public boolean addToCart(Product product, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CART_ID, product.getId());
        values.put(COL_CART_NAME, product.getName());
        values.put(COL_CART_PRICE, product.getPrice());
        values.put(COL_CART_QTY, qty);

        long result = db.insertWithOnConflict(TABLE_CART, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public void updateCartQty(String productId, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CART_QTY, qty);
        db.update(TABLE_CART, values, COL_CART_ID + "=?", new String[]{productId});
    }

    public void removeFromCart(String productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, COL_CART_ID + "=?", new String[]{productId});
    }

    public void clearCart() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, null, null);
    }

    public Cursor getCartItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CART, null);
    }
}
