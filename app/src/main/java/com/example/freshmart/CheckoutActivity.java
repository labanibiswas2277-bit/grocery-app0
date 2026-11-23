package com.example.freshmart;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class CheckoutActivity extends Activity {
    DatabaseHelper db;
    int dp(int v){return (int)(v*getResources().getDisplayMetrics().density);}
    @Override protected void onCreate(Bundle b){
        super.onCreate(b); db=new DatabaseHelper(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(24),dp(20),dp(20));root.setBackgroundColor(Color.rgb(247,250,246));
        TextView title=new TextView(this);title.setText("Checkout");title.setTextSize(26);title.setTextColor(Color.rgb(24,110,65));title.setTypeface(null,android.graphics.Typeface.BOLD);root.addView(title);
        TextView amount=new TextView(this);amount.setText("Order total: ₹"+(int)db.getCartTotal());amount.setTextSize(20);amount.setTextColor(Color.BLACK);amount.setPadding(0,dp(20),0,dp(20));root.addView(amount);
        EditText name=new EditText(this);name.setHint("Customer name");root.addView(name);
        EditText phone=new EditText(this);phone.setHint("Phone number");phone.setInputType(3);root.addView(phone);
        EditText address=new EditText(this);address.setHint("Delivery address");address.setMinLines(2);address.setGravity(Gravity.TOP);root.addView(address);
        TextView note=new TextView(this);note.setText("Payment method: Cash on Delivery");note.setTextSize(15);note.setTextColor(Color.DKGRAY);note.setPadding(0,dp(18),0,dp(18));root.addView(note);
        Button place=new Button(this);place.setText("Place Order");
        place.setOnClickListener(v->{
            if(name.getText().toString().trim().isEmpty()||phone.getText().toString().trim().isEmpty()||address.getText().toString().trim().isEmpty()){
                Toast.makeText(this,"Please fill in all details",Toast.LENGTH_SHORT).show();return;
            }
            if(phone.getText().toString().trim().length()<10){Toast.makeText(this,"Enter a valid phone number",Toast.LENGTH_SHORT).show();return;}
            db.placeOrder(name.getText().toString().trim());
            new android.app.AlertDialog.Builder(this).setTitle("Order placed!")
                .setMessage("Thank you, "+name.getText().toString().trim()+"! Your demo order has been placed successfully.")
                .setCancelable(false).setPositiveButton("Continue Shopping",(d,w)->{finishAffinity();startActivity(new android.content.Intent(this,MainActivity.class));}).show();
        });
        root.addView(place);setContentView(root);
    }
}
