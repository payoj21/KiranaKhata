package shantanu.payoj.kiranakhata;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class StartActivity extends Activity {

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startscreen);
	}
	
	public void onStart()	{
		super.onStart();
		
		Button customer = (Button) findViewById(R.id.customer);
		
		  customer.setOnClickListener(new OnClickListener() {
		      public void onClick(View v) {
		          
		          Intent i = new Intent(StartActivity.this, CustomerTransaction.class);
		          startActivity(i);
		      }
		  });
		  
		  Button shopkeeper = (Button) findViewById(R.id.shopkeeper);
		  
		  shopkeeper.setOnClickListener(new OnClickListener() {
		      public void onClick(View v) {
		          
		          Intent i = new Intent(StartActivity.this, ShopkeeperHome.class);
		          startActivity(i);
		      }
		  });
	}
	
	
}
