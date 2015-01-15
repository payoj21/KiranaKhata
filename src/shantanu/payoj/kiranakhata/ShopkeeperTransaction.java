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


public class ShopkeeperTransaction extends Activity{

	TextView amtText;
	TextView itemText;
	Button confirmCredit;
	Button rejectCredit;
	
	TextView oldBalanceT;
	TextView newBalanceT;
	
	String amount = null;
	String item = null;
	
	String TAG = "Shopkeeper Transaction";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.skconfirm);
        
        amtText = (TextView) findViewById(R.id.amtText);
        itemText = (TextView) findViewById(R.id.itemText);
        confirmCredit = (Button) findViewById(R.id.confirmCredit);
        rejectCredit = (Button) findViewById(R.id.rejectCredit);
        
        oldBalanceT = (TextView) findViewById(R.id.oldBalanceT);
        newBalanceT = (TextView) findViewById(R.id.newBalanceT);
        
        Bundle br = getIntent().getExtras();
        
        Log.d(TAG, "Args received = " + br.getInt("args"));

        if(br.getInt("args") == 2)	{
        	amount = br.getString("amount");
            item = (String) br.getString("item");
        }
        else	{
        	amount = br.getString("amount");
        	item = "";
        }
        
        amtText.setText(amount);
        itemText.setText(item);
        Log.d(TAG,"amount = " + amount + " item = " + item + ".");
        
        int oldbal = ShopkeeperHome.balance;
        int newbal = oldbal - Integer.parseInt(amount);
        
        oldBalanceT.setText(Integer.toString(oldbal));
        if (oldbal > 0)
        	oldBalanceT.setBackgroundColor(Color.GREEN);
        else if (oldbal < 0)
        	oldBalanceT.setBackgroundColor(Color.RED);
        else 
        	oldBalanceT.setBackgroundColor(Color.BLUE);
        
        newBalanceT.setText(Integer.toString(newbal));
        if (newbal > 0)
        	newBalanceT.setBackgroundColor(Color.GREEN);
        else if (newbal < 0)
        	newBalanceT.setBackgroundColor(Color.RED);
        else 
        	newBalanceT.setBackgroundColor(Color.BLUE);
        
        
        confirmCredit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("amount",amount);
                returnIntent.putExtra("item", item);
                setResult(RESULT_OK,returnIntent);
                
                ShopkeeperHome.balance -= Integer.parseInt(amount);
                
                finish();
            }
        });
        
        rejectCredit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED,returnIntent);
                finish();
            }
        });
        
        
    }
	
}
