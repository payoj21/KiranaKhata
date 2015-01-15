package shantanu.payoj.kiranakhata;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ShopkeeperRecharge extends Activity{
	
	TextView amtText;
	Button confirmRecharge;
	Button rejectRecharge;
	String amount = null;
	
	TextView oldBalance;
	TextView newBalance;
	
	String TAG = "Shopkeeper Recharge";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skrecharge);
        
        amtText = (TextView) findViewById(R.id.skRechargeAmt);
        confirmRecharge = (Button) findViewById(R.id.confirmRecharge);
        rejectRecharge = (Button) findViewById(R.id.rejectRecharge);
        
        oldBalance = (TextView) findViewById(R.id.oldBalance);
        newBalance = (TextView) findViewById(R.id.newBalance);
        
        Bundle br = getIntent().getExtras();
        
        amount = br.getString("amount");
        amtText.setText(amount);
        Log.d(TAG,"amount = " + amount);
        
        int oldbal = ShopkeeperHome.balance;
        int newbal = oldbal + Integer.parseInt(amount);
        
        
        oldBalance.setText(Integer.toString(oldbal));
        if (oldbal > 0)
        	oldBalance.setBackgroundColor(Color.GREEN);
        else if (oldbal < 0)
        	oldBalance.setBackgroundColor(Color.RED);
        else 
        	oldBalance.setBackgroundColor(Color.BLUE);
        
        newBalance.setText(Integer.toString(newbal));
        if (newbal > 0)
        	newBalance.setBackgroundColor(Color.GREEN);
        else if (newbal < 0)
        	newBalance.setBackgroundColor(Color.RED);
        else 
        	newBalance.setBackgroundColor(Color.BLUE);
        
        
        
        confirmRecharge.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("amount", amount);
                setResult(RESULT_OK,returnIntent);
                
                ShopkeeperHome.balance += Integer.parseInt(amount);
                
                //Log.d("Dangerous", "Recharge confirm button pressed");
                
                finish();
            }
        });
        
        rejectRecharge.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED,returnIntent);
                finish();
            }
        });
        
        
    }
}
