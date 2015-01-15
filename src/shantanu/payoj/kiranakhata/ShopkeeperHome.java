package shantanu.payoj.kiranakhata;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.sql.Timestamp;

/**
 * This is the main Activity that displays the current chat session.
 */
public class ShopkeeperHome extends Activity {
    // Debugging
    private static final String TAG = "Shopkeeper Home";
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
	protected static final int REQUEST_CREDIT = 4;
	protected static final int REQUEST_RECHARGE = 5;
	
	protected static final String CREDIT_REQ_REJECTED = "creditReqRejected";
	protected static final String CREDIT_REQ_CONFIRMED = "creditReqConfirmed";
	protected static final String RECHARGE_REQ_REJECTED = "rechargeReqRejected";
	protected static final String RECHARGE_REQ_CONFIRMED = "rechargeReqConfirmed";
	
	public SimpleDateFormat sdf;
	
    // Layout Views
    private TextView mTitle;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private DataManager mDataManager = null;

    private Context shopkeeperHomeContext;
    
    private TextView customerName;
    private TextView balanceSk;
    
    public static int balance;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.skhome);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        customerName = (TextView) findViewById(R.id.customerName);
        balanceSk = (TextView) findViewById(R.id.balanceSk);
        
        
        shopkeeperHomeContext = this.getApplicationContext();
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        sdf = new SimpleDateFormat("hh:mm-MMM-d-yy");
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
              // Start the Bluetooth chat services
              mDataManager.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mDataManager = new DataManager(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
        
    }
    
    private void setBalance(int balance)	{
    	
    	balanceSk.setText(Integer.toString(balance));
    	if (balance < 0)
    		balanceSk.setBackgroundColor(Color.RED);
    	else if (balance > 0)
    		balanceSk.setBackgroundColor(Color.GREEN);
    	else
    		balanceSk.setBackgroundColor(Color.BLUE);
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
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mDataManager.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }


    // The Handler that gets information back from the BluetoothChatService
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
                    
                    customerName.setText(mConnectedDeviceName);
                    balance = 0;
                    setBalance(balance);
                    //mConversationArrayAdapter.clear();
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
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, "Raw message received: " + readMessage);
                StringTokenizer st = new StringTokenizer(readMessage," ");
                
                String event = st.nextToken();
                
                if (event.equals("BALANCE"))	{
                	int newbal = Integer.parseInt(st.nextToken());
                	if (newbal == balance)
                		setBalance(newbal);
                	else	{
                		Log.e(TAG, "balance doesn't match!");
                		Toast.makeText(getApplicationContext(), "Balance not matching!", Toast.LENGTH_SHORT).show();
                	}
                }
                
                
                else if (event.equals("CREDIT"))	{
                	
                	Log.d(TAG, "CREDIT message received");
                	Log.d(TAG, "Number of tokens received = " + st.countTokens());
                	Intent intent = new Intent(shopkeeperHomeContext, ShopkeeperTransaction.class);
                	
                    if (st.countTokens() == 2)	{
                    	intent.putExtra("args", 2);
                    	intent.putExtra("amount", st.nextToken());
                    	intent.putExtra("item", st.nextToken());
                    	
                    	Log.d(TAG, "Message received with amount AND item");
                    	
                    }
                    else if (st.countTokens() == 1)	{
                    	intent.putExtra("args", 1);
                    	intent.putExtra("amount", st.nextToken());
                    	
                    	Log.d(TAG, "Message received with amount ONLY");
                    	
                    }
                    
                    else
                    	Log.e(TAG, "Invalid number of tokens for CREDIT");
                    
                    Log.d(TAG,"starting shopkeeper CREDIT activity");
                    startActivityForResult(intent,REQUEST_CREDIT);
                }
                
                else if (event.equals("RECHARGE")){
                	
                	Log.d(TAG, "RECHARGE message received");
                	Intent intent = new Intent(shopkeeperHomeContext, ShopkeeperRecharge.class);
                	intent.putExtra("amount", st.nextToken());
                	Log.d(TAG,"starting shopkeeper RECHARGE activity");
                	startActivityForResult(intent,REQUEST_RECHARGE);
                	
                }
                
                else {
                	Log.e(TAG, "Unknown event");
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
            break;
        case REQUEST_CREDIT:
        	if (resultCode == Activity.RESULT_OK) {
                
        		
        		String timestamp = sdf.format(new Date());
        		
        		if(data.getStringExtra("item").equals(""))	{
        			sendMessage(CREDIT_REQ_CONFIRMED + " " + data.getStringExtra("amount") + " " + timestamp);
        			Log.d(TAG, "Confirming CREDIT request with amount ONLY");
        		}
        		else	{
        			sendMessage(CREDIT_REQ_CONFIRMED + " " + data.getStringExtra("amount") + " " + data.getStringExtra("item") + " " + timestamp);
        			Log.d(TAG, "Confirming CREDIT request with amount AND item");
        		}
        		
        		Toast.makeText(this, "Credit request confirmed", Toast.LENGTH_SHORT).show();
        		
        		
            } else {
                Log.d(TAG, "CREDIT Request Rejected");
                Toast.makeText(this, "Credit request rejected", Toast.LENGTH_SHORT).show();
                sendMessage(CREDIT_REQ_REJECTED);
            }
        	break;
        case REQUEST_RECHARGE:
        	
        	String timestamp = sdf.format(new Date());
        	
        	if (resultCode == Activity.RESULT_OK) {
        		//Log.d("Dangerous", "Somebody exited with REQUEST_RECHARGE RESULT.OK");
        		sendMessage(RECHARGE_REQ_CONFIRMED + " " + data.getStringExtra("amount") + " " + timestamp);
            } else {
                Log.d(TAG, "RECHARGE request rejected");
                Toast.makeText(this, "Recharge request rejected", Toast.LENGTH_SHORT).show();
                sendMessage(RECHARGE_REQ_REJECTED);
            }
        	break;
        	
        }
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

