package com.example.freshmart;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.util.ArrayList;

public class CartActivity extends Activity {
    DatabaseHelper db;
    LinearLayout items;
    TextView totalText;
    int dp(int v) { return (int)(v*getResources().getDisplayMetrics().density); }

    @Override protected void onCreate(Bundle b) { super.onCreate(b); db=new DatabaseHelper(this); build(); }
    private TextView txt(String s,int size,boolean bold) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.BLACK);
        if(bold)t.setTypeface(null,android.graphics.Typeface.BOLD); return t;
    }
    private void build() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(16),dp(16),dp(12));
        root.setBackgroundColor(Color.rgb(247,250,246));
        TextView title=txt("🛒 Your Cart",24,true); root.addView(title);
        ScrollView scroll=new ScrollView(this); items=new LinearLayout(this); items.setOrientation(LinearLayout.VERTICAL); scroll.addView(items);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        totalText=txt("Total: ₹0",20,true); totalText.setPadding(0,dp(12),0,dp(12)); root.addView(totalText);
        Button checkout=new Button(this); checkout.setText("Proceed to Checkout");
        checkout.setOnClickListener(v -> {
            if(db.getCartCount()==0) Toast.makeText(this,"Your cart is empty",Toast.LENGTH_SHORT).show();
            else startActivity(new Intent(this,CheckoutActivity.class));
        });
        root.addView(checkout);
        Button back=new Button(this); back.setText("Continue Shopping"); back.setOnClickListener(v->finish()); root.addView(back);
        setContentView(root); refresh();
    }
    private void refresh() {
        items.removeAllViews();
        ArrayList<Product> products=db.getCartProducts();
        if(products.isEmpty()) items.addView(txt("Your cart is empty. Add some groceries!",16,false));
        for(Product p:products) {
            int q=db.getQuantity(p.id);
            LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(12),0,dp(12));
            LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL);
            info.addView(txt(p.emoji+"  "+p.name,17,true));
            info.addView(txt("₹"+(int)p.price+" × "+q+" = ₹"+(int)(p.price*q),14,false));
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            Button minus=new Button(this); minus.setText("−"); minus.setOnClickListener(v->{db.changeQuantity(p.id,-1);refresh();});
            Button plus=new Button(this); plus.setText("+"); plus.setOnClickListener(v->{db.changeQuantity(p.id,1);refresh();});
            row.addView(minus); row.addView(txt(" "+q+" ",16,true)); row.addView(plus); items.addView(row);
        }
        totalText.setText("Total: ₹"+(int)db.getCartTotal());
    }
    @Override protected void onResume(){super.onResume();if(items!=null)refresh();}
}
