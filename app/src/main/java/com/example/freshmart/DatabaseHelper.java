package com.example.freshmart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "freshmart.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY, name TEXT, category TEXT, price REAL, emoji TEXT)");
        db.execSQL("CREATE TABLE cart (product_id INTEGER PRIMARY KEY, quantity INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE orders (id INTEGER PRIMARY KEY AUTOINCREMENT, customer TEXT, total REAL, order_date TEXT)");
        insertProduct(db, 1, "Apple", "Fruits", 120, "🍎");
        insertProduct(db, 2, "Banana", "Fruits", 50, "🍌");
        insertProduct(db, 3, "Orange", "Fruits", 80, "🍊");
        insertProduct(db, 4, "Potato", "Vegetables", 30, "🥔");
        insertProduct(db, 5, "Tomato", "Vegetables", 40, "🍅");
        insertProduct(db, 6, "Carrot", "Vegetables", 60, "🥕");
        insertProduct(db, 7, "Milk", "Dairy", 30, "🥛");
        insertProduct(db, 8, "Eggs", "Dairy", 70, "🥚");
        insertProduct(db, 9, "Rice", "Staples", 60, "🍚");
    }

    private void insertProduct(SQLiteDatabase db, int id, String name, String category, double price, String emoji) {
        ContentValues v = new ContentValues();
        v.put("id", id); v.put("name", name); v.put("category", category);
        v.put("price", price); v.put("emoji", emoji);
        db.insert("products", null, v);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS cart");
        db.execSQL("DROP TABLE IF EXISTS products");
        db.execSQL("DROP TABLE IF EXISTS orders");
        onCreate(db);
    }

    public ArrayList<Product> getProducts() {
        ArrayList<Product> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM products ORDER BY id", null);
        try {
            while (c.moveToNext()) list.add(new Product(c.getInt(0), c.getString(1), c.getString(2), c.getDouble(3), c.getString(4)));
        } finally { c.close(); }
        return list;
    }

    public void addToCart(int productId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT quantity FROM cart WHERE product_id = ?", new String[]{String.valueOf(productId)});
        boolean exists = c.moveToFirst();
        int quantity = exists ? c.getInt(0) + 1 : 1;
        c.close();
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        if (exists) db.update("cart", values, "product_id=?", new String[]{String.valueOf(productId)});
        else { values.put("product_id", productId); db.insert("cart", null, values); }
    }

    public void changeQuantity(int productId, int change) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE cart SET quantity = quantity + ? WHERE product_id = ?", new Object[]{change, productId});
        db.execSQL("DELETE FROM cart WHERE product_id = ? AND quantity <= 0", new Object[]{productId});
    }

    public ArrayList<Product> getCartProducts() {
        ArrayList<Product> list = new ArrayList<>();
        String sql = "SELECT p.id,p.name,p.category,p.price,p.emoji,c.quantity FROM products p JOIN cart c ON p.id=c.product_id ORDER BY p.id";
        Cursor c = getReadableDatabase().rawQuery(sql, null);
        try {
            while (c.moveToNext()) {
                Product p = new Product(c.getInt(0), c.getString(1), c.getString(2), c.getDouble(3), c.getString(4));
                pQuantity.put(p.id, c.getInt(5));
                list.add(p);
            }
        } finally { c.close(); }
        return list;
    }

    private final java.util.HashMap<Integer, Integer> pQuantity = new java.util.HashMap<>();
    public int getQuantity(int productId) { Integer q = pQuantity.get(productId); return q == null ? 0 : q; }

    public double getCartTotal() {
        Cursor c = getReadableDatabase().rawQuery("SELECT SUM(p.price*c.quantity) FROM products p JOIN cart c ON p.id=c.product_id", null);
        double total = 0;
        try { if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0); }
        finally { c.close(); }
        return total;
    }

    public int getCartCount() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COALESCE(SUM(quantity),0) FROM cart", null);
        int count = 0;
        try { if (c.moveToFirst()) count = c.getInt(0); } finally { c.close(); }
        return count;
    }

    public void placeOrder(String customer) {
        ContentValues v = new ContentValues();
        v.put("customer", customer);
        v.put("total", getCartTotal());
        v.put("order_date", new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
        getWritableDatabase().insert("orders", null, v);
        getWritableDatabase().delete("cart", null, null);
    }
}
