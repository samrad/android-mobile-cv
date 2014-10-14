/**
 * 
 */
package de.rwth.aachen.comsys.androidcv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.rwth.aachen.comsys.androidcv.connectivity.BluetoothService;
import de.rwth.aachen.comsys.androidcv.ui.CustomArrayAdapter;
import de.rwth.aachen.comsys.androidcv.ui.Item;
import de.rwth.aachen.comsys.androidcv.ui.ListViewHeader;
import de.rwth.aachen.comsys.androidcv.ui.ListViewItem;
import de.rwth.aachen.comsys.androidcv.ui.CustomArrayAdapter.RowType;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentBluetooth extends Fragment {
	
	// Debugging
    private static final String TAG = "FragmentBluetooth";
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
	
	// Layout views
	private ListView mListView;
	private ProgressBar mProgressBar;
	private List<Item> items;
	
	// Array adapter to populate the list of BT devices (paired and available)
	private CustomArrayAdapter mArrayAdapter;
	
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

	// Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Name of the connected device
    private String mConnectedDeviceName = null;
	// Member object for Bluetooth Service
    private BluetoothService mBluetoothService = null;
    
    private long t;
    private int i = 0; 

	public FragmentBluetooth() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);
		
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            // Return to previous fragment
            getFragmentManager().popBackStackImmediate();
        }
        
        // List of paired and device items
        items = new ArrayList<Item>();
//        items.add(new ListViewHeader("Paired Devices"));
        
//        // Get a set of currently paired devices
//        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        
//        // If there are paired devices, add each one to the ArrayAdapter
//        if (pairedDevices.size() > 0) {
//            for (BluetoothDevice device : pairedDevices) {
//                items.add(new ListViewItem(device.getName(), device.getAddress()));
//            }
//        } else {
//        	items.add(new ListViewItem("Empty", "No paired device"));
//        }
//        
        // Header for available devices
//        items.add(new ListViewHeader("Available Devices"));
        
		
		// Sample data
//        items.add(new ListViewItem("Text 1", "Rabble rabble"));
//        items.add(new ListViewItem("Text 2", "Rabble rabble"));
//        items.add(new ListViewItem("Text 3", "Rabble rabble"));
//        items.add(new ListViewItem("Text 4", "Rabble rabble"));
//        items.add(new ListViewHeader("Available Devices"));
//        items.add(new ListViewItem("Text 5", "Rabble rabble"));
//        items.add(new ListViewItem("Text 6", "Rabble rabble"));
//        items.add(new ListViewItem("Text 7", "Rabble rabble"));
//        items.add(new ListViewItem("Text 8", "Rabble rabble"));
		
		View view = inflater.inflate(R.layout.fragment_layout_bluetooth, 
				container,	false);
		
		// Find the ListView and set it up with ArrayAdapter and ItemClickListener
		mListView = (ListView) view.findViewById(R.id.bluetooth_list);
		mArrayAdapter = new CustomArrayAdapter(getActivity(), items);
		mListView.setAdapter(mArrayAdapter);
		mListView.setOnItemClickListener(mDeviceClickListener);
		
		// Reference to ProgressBar
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar1);
		
		// Tell the ActionaBar to use Fragment-Specific menu options
		setHasOptionsMenu(true);
		
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mBluetoothService == null) initialize();
		}
	}
	
	

	@Override
	public synchronized void onResume() {
		super.onResume();
		
		// Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
            	mBluetoothService.start();
            }
        }
	}

	private void initialize() {
		Log.d(TAG, "setupChat()");
		
        items.add(new ListViewHeader("Paired Devices"));
		
		// Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                items.add(new ListViewItem(device.getName(), device.getAddress()));
            }
        } else {
        	items.add(new ListViewItem("Empty", "No paired device"));
        }
		
        items.add(new ListViewHeader("Available Devices"));
        // Update the view
     	mArrayAdapter.notifyDataSetChanged();

		// Initialize the BluetoothChatService to perform bluetooth connections
		mBluetoothService = new BluetoothService(getActivity(), mHandler);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.bluetooth_menu, menu);
	}
	
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
		Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
//				connectDevice(data, true);
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
//				connectDevice(data, false);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				initialize();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				// Return to previous fragment
	            getFragmentManager().popBackStackImmediate();
			}
		}
	}
     
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// handle item selection
		switch (item.getItemId()) {
		case R.id.bluetooth_search_action:
//			startDiscovery();
			startDiscoveryTest();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// Disable Bluetooth adapter
		mBluetoothAdapter.disable();
	}
	
	

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) {
        	mBluetoothAdapter.cancelDiscovery();
        }
        
        if (mBluetoothService != null) { 
        	mBluetoothService.stop();
        }

        // Unregister broadcast listeners
        getActivity().unregisterReceiver(mReceiver);
	}

	/**
	 * 
	 */
	private void startDiscovery() {
		
		// Set progress bar to visible to indicate scanning
		mProgressBar.setVisibility(View.VISIBLE);
//        setTitle(R.string.scanning);

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
        	mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
		
	}
	
	private void startDiscoveryTest() {
		
//		for (int i = 0; i < 200; i++) {
			
//			Log.d(TAG, String.valueOf(i));

			t = System.nanoTime();
			// Request discover from BluetoothAdapter
			mBluetoothAdapter.startDiscovery();
//		}
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> av, View v, int position, long id) {
			
			// Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();
            
            Item item = (Item) av.getItemAtPosition(position);
            
            // Skip if a "header" is clicked
            if (item.getViewType() == RowType.LIST_ITEM.ordinal()) {
            	Log.d(TAG, "ItemClicked");
            	
            	// Get the view's text
                LinearLayout ll = (LinearLayout) v;
                TextView tv = (TextView) ll.findViewById(R.id.tv_device_address);
                final String deviceAddress = tv.getText().toString();
                Log.d(TAG, deviceAddress);
                
                // Connect to the device selected from the list
                connectDevice(deviceAddress, true);
            }
            
            
			
		}
		
	};
	
	private void connectDevice(String macAddress, boolean secure) {

		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
		// Attempt to connect to the device
		mBluetoothService.connect(device, secure);
	}
	
    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			Log.d(TAG, "onReceive");

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					
					Log.d(TAG, "BT Device Found");
					mArrayAdapter.add(new ListViewItem(device.getName(), device.getAddress()));
					
					// Just for test -START
					if (device.getName().equals("SAM-PC")) {
						i++;
						t = System.nanoTime() - t;
						double e = t / 1000000.0;
						Log.d(TAG, String.valueOf(i) + ": Sam found in " + String.valueOf(e) + " ms");
						mBluetoothAdapter.cancelDiscovery();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						
						if (i < 200) {
							startDiscoveryTest();
						}
					}
					// Just for test -END
					
					
//					items.add(new ListViewItem(device.getName(), device.getAddress()));
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mProgressBar.setVisibility(View.GONE);

			}
			
			// Update the view
			mArrayAdapter.notifyDataSetChanged();
		}
	};
	
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
//                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
//                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothService.STATE_CONNECTING:
//                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
//                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
//                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
//                Log.d(TAG, Arrays.toString(readBuf));
//                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getActivity(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getActivity(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
	
}