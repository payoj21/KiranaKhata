package shantanu.payoj.kiranakhata;

import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity on the customer's screen
 */
@SuppressLint("HandlerLeak")
public class CustomerTransaction extends Activity {
    // Debugging
    private static final String TAG = "Customer Transaction";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    private TextView mTitle;
    private TextView shopName;
    
    private EditText itemText;
    private EditText amountText;
    private Button sendButton;

    private TextView balanceCustomer;
    private int balance;
    
    private EditText rechargeAmt;
    private Button rechargeButton;
    
    private ListView recentsList;
    
    private String[] recents;
    int recentsPos;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private DataManager mDataManager = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.transaction);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        shopName = (TextView) findViewById(R.id.shopName);
        
        //Views
        itemText = (EditText) findViewById(R.id.itemText);
        amountText = (EditText) findViewById(R.id.amountText);
        sendButton = (Button) findViewById(R.id.sendButton);
        
        rechargeAmt = (EditText) findViewById(R.id.rechargeAmt);
        rechargeButton = (Button) findViewById(R.id.rechargeButton);
        
        balanceCustomer = (TextView) findViewById(R.id.balanceCustomer);
        
        recentsList = (ListView) findViewById(R.id.recentsList);
        recents = new String[5];
        for (int i=0; i<5; i++)
        	recents[i] = "No data";
        
        ArrayAdapter<String> recentsArray = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recents);
        
        recentsList.setAdapter(recentsArray);
        
        
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mDataManager == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mDataManager != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mDataManager.getState() == DataManager.STATE_NONE) {
              // Start the Bluetooth data manager
              mDataManager.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the DataManager to perform bluetooth connections
        mDataManager = new DataManager(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        
        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String amount = amountText.getText().toString();
                String item = itemText.getText().toString();
                if(item == null || item.length() == 0)	{
                	sendMessage("CREDIT" + " " + amount);
                	Log.d(TAG, "CREDIT request sent: amount =" + amount);
                }
                	
                else	{
                	sendMessage("CREDIT" + " " + amount + " " + item);
                	Log.d(TAG, "CREDIT request sent with amount = " + amount + " item = " + item);
                }
                	
            }
        });
        
        rechargeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String rechargeAmount = rechargeAmt.getText().toString().trim();
                if(rechargeAmount != null && rechargeAmount.length()>0)	{
                	sendMessage("RECHARGE " + rechargeAmount);
                	Log.d(TAG, "RECHARGE request sent with amount " + rechargeAmount);
                }
                
            }
        });
        
        recentsPos = 0;
       
        balance = 0;
        balanceCustomer.setText(Integer.toString(balance));
        
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mDataManager != null) mDataManager.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }
    
    
    private void setBalance(String amount, boolean credit)	{
    	if (credit)
    		balance -= Integer.parseInt(amount);
    	else
    		balance += Integer.parseInt(amount);
    	
    	balanceCustomer.setText(Integer.toString(balance));
    	if (balance < 0)
    		balanceCustomer.setBackgroundColor(Color.RED);
    	else if (balance > 0)
    		balanceCustomer.setBackgroundColor(Color.GREEN);
    	else
    		balanceCustomer.setBackgroundColor(Color.BLUE);
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mDataManager.getState() != DataManager.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the DataManager to write
            byte[] send = message.getBytes();
            mDataManager.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    // The Handler that gets information back from the DataManager
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case DataManager.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    shopName.setText(mConnectedDeviceName);
                    setBalance("0",true);
                    CustomerTransaction.this.sendMessage("BALANCE " + balance);
                    break;
                case DataManager.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case DataManager.STATE_LISTEN:
                case DataManager.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:

                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);

                Log.d(TAG, "Raw Message Received: " + readMessage);
                
                StringTokenizer st = new StringTokenizer(readMessage, " ");
                String event = st.nextToken();
                
                String listEntry = null;
                if(event.equals(ShopkeeperHome.CREDIT_REQ_CONFIRMED))	{
                	
                	Log.d(TAG, "CREDIT Request Confirmation Received");
                	Log.d(TAG, "Tokens Received: " + st.countTokens());
                	
                	String amount = null;
                	if (st.countTokens() == 2)	{
                		amount = st.nextToken();
                		String timestamp = st.nextToken();
                		listEntry = "C | " + amount + " | " + timestamp;
                		
                		Log.d(TAG, "The list entry for CREDIT is \"" + listEntry + "\" and will be entered position " + recentsPos);
                    	
                    	recents[recentsPos] = listEntry;
                    	recentsPos = (recentsPos + 1)%5;
                		
                    	setBalance(amount, true);
                    	CustomerTransaction.this.sendMessage("BALANCE " + balance);
                	}
                	
                	else if (st.countTokens() == 3){
                		amount = st.nextToken();
                		String item = st.nextToken();
                		String timestamp = st.nextToken();
                		listEntry = "C | " + amount + " | " + item + " | " + timestamp;
                		
                		
                		Log.d(TAG, "The list entry for CREDIT is \"" + listEntry + "\" and will be entered position " + recentsPos);
                    	
                    	recents[recentsPos] = listEntry;
                    	recentsPos = (recentsPos + 1)%5;
                		
                    	setBalance(amount, true);
                    	CustomerTransaction.this.sendMessage("BALANCE " + balance);
                    	
                	}	else	{
                		Log.e(TAG, "Incorrect no. of tokens in CREDIT request confirmation");
                		return;
                	}
                	
                }
                
                else if (event.equals(ShopkeeperHome.CREDIT_REQ_REJECTED))	{
                	Toast.makeText(getApplicationContext(), "Credit request rejected", Toast.LENGTH_SHORT).show();
                	Log.d(TAG, "CREDIT request rejected");
                }
                	
                
                else if (event.equals(ShopkeeperHome.RECHARGE_REQ_CONFIRMED))	{
                	Log.d(TAG, "RECHARGE request confirmation received");

                	if(st.countTokens() == 2)	{
                		String amount = st.nextToken();
                		String timestamp = st.nextToken();
	                	listEntry = "R | " + amount + " | " + timestamp;
	                	Log.d(TAG, "The list entry for RECHARGE is " + listEntry + " and will be entered position " + recentsPos);
	                	recents[recentsPos] = listEntry;
	                	recentsPos = (recentsPos + 1)%5;
	                	
	                	setBalance(amount, false);
	                	CustomerTransaction.this.sendMessage("BALANCE " + balance);
	                	
                	} else	{
                		Log.e(TAG, "Incorrect number of tokens");
                	}
                	
                }
                else if (event.equals(ShopkeeperHome.RECHARGE_REQ_REJECTED))	{
                	Toast.makeText(getApplicationContext(), "Recharge request rejected", Toast.LENGTH_SHORT).show();
                	Log.d(TAG, "RECHARGE request rejected");
                }
                
                else	{
                	Log.e(TAG, "Unkown message received");
                }
                
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    protected void setBalance(int i, boolean credit) {
		// TODO Auto-generated method stub
		
	}

	private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mDataManager.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, ListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, ListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

}
