package com.example.freshmart;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.*;
import android.view.View;
import java.util.ArrayList;

public class MainActivity extends Activity {
    DatabaseHelper db;
    LinearLayout list;
    TextView cartButton;
    String selectedCategory = "All";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DatabaseHelper(this);
        buildScreen();
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density); }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(8)); root.setBackgroundColor(Color.rgb(247,250,246));
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("🥬 FreshMart", 25, Color.rgb(24,110,65), true);
        top.addView(brand, new LinearLayout.LayoutParams(0, -2, 1));
        cartButton = text("🛒 Cart (" + db.getCartCount() + ")", 16, Color.WHITE, true);
        cartButton.setBackgroundColor(Color.rgb(24,130,75)); cartButton.setPadding(dp(12),dp(10),dp(12),dp(10));
        cartButton.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        top.addView(cartButton); root.addView(top);

        TextView subtitle = text("Fresh groceries delivered to your door", 14, Color.DKGRAY, false);
        subtitle.setPadding(0,dp(8),0,dp(12)); root.addView(subtitle);

        EditText search = new EditText(this); search.setSingleLine(true); search.setTextSize(15);
        search.setHint("Search groceries..."); search.setPadding(dp(12),0,dp(12),0);
        root.addView(search, new LinearLayout.LayoutParams(-1, dp(48)));

        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout cats = new LinearLayout(this); cats.setPadding(0,dp(10),0,dp(10));
        String[] categories = {"All","Fruits","Vegetables","Dairy","Staples"};
        for (String cat : categories) {
            Button btn = new Button(this); btn.setText(cat); btn.setTextSize(12);
            btn.setOnClickListener(v -> { selectedCategory = cat; showProducts(search.getText().toString()); });
            cats.addView(btn);
        }
        hsv.addView(cats); root.addView(hsv);

        TextView heading = text("Popular groceries", 20, Color.BLACK, true);
        heading.setPadding(0,dp(8),0,dp(8)); root.addView(heading);

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list); root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int st,int before,int count) { showProducts(s.toString()); }
            public void afterTextChanged(android.text.Editable e) {}
        });
        showProducts("");
        setContentView(root);
    }

    private void showProducts(String query) {
        list.removeAllViews();
        ArrayList<Product> products = db.getProducts();
        for (Product p : products) {
            if (!selectedCategory.equals("All") && !p.category.equals(selectedCategory)) continue;
            if (!p.name.toLowerCase().contains(query.toLowerCase())) continue;
            LinearLayout card = new LinearLayout(this); card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(12),dp(10),dp(8),dp(10)); card.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1,dp(82)); cp.bottomMargin=dp(8);
            TextView emoji = text(p.emoji, 32, Color.BLACK, false); card.addView(emoji);
            LinearLayout info = new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(12),0,0,0);
            info.addView(text(p.name,17,Color.BLACK,true));
            info.addView(text("₹"+(int)p.price+" / unit",14,Color.DKGRAY,false));
            card.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            Button add = new Button(this); add.setText("ADD"); add.setTextSize(12);
            add.setOnClickListener(v -> { db.addToCart(p.id); cartButton.setText("🛒 Cart ("+db.getCartCount()+")"); Toast.makeText(this,p.name+" added to cart",Toast.LENGTH_SHORT).show(); });
            card.addView(add); list.addView(card,cp);
        }
        if (list.getChildCount()==0) list.addView(text("No products found.",16,Color.DKGRAY,false));
    }

    @Override protected void onResume() { super.onResume(); if (db != null && cartButton != null) cartButton.setText("🛒 Cart ("+db.getCartCount()+")"); }
}
