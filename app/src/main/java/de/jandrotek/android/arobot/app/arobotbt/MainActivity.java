package de.jandrotek.android.arobot.app.arobotbt;


import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import de.jandrotek.android.arobot.app.arobotbt.SensorRx.Callbacks;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks, SensorRx.Callbacks {
    private static final String TAG = "MainActivity";
	// from Reto ???
	// menu defines
	static final private int MENU_PREFERENCES = Menu.FIRST + 1;
	static final private int MENU_UPDATE = Menu.FIRST + 2;
	private static final int SHOW_PREFERENCES = 1;

	// defines, macros
	// poistion of navigation drawer
	private static final int POSITION_MOVEMENT_VIEW = 0;
	private static final int POSITION_BLUETOOTH_VIEW = 1;
	private final int POSITION_PREFS_ACTIVITY = 2;

	private static final byte[] BT_STOP_MESSAGE = {(byte)0xA5, 0,0,0,0,0};
	// fragment numbers
	// control obj, vars
	private int mFragmentIndexAct = -1;// on start, no fragment selected
	private int mFragmentIndexNew = -1;// on start, no fragment selected
	private final int FRAGMENT_MOVEMENT_VIEW = 0;
	private final int FRAGMENT_BLUETOOTH_VIEW = 1;

	//Model objects, vars
    private static final boolean D = true;
    Callbacks mCallbacks;
    private ArobotSettings mArobotSettings;
	private SensorRx mSensorData = null;
	private boolean mBTConnected = false;
	//private ActionBar mActionBar;
	private boolean mSavingSensorData = false;

	private SensorMovementFragment mSensorMovementFragment;
	private BluetoothFragment mBluetoothFragment;
    private BluetoothAdapter mBluetoothAdapter = null;
	private SensorManager mSensorManager = null;


	// we need service here, some other fragments can write to BT too
    private BluetoothService mBTService = null;
    private String mConnectedDeviceName = null;



	// view obj, vars
	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;
	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	       if(D) Log.e(TAG, "+++ ON CREATE +++");
		setContentView(R.layout.activity_main);

        // keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if(savedInstanceState == null){

			mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
					.findFragmentById(R.id.navigation_drawer);
			mTitle = getTitle();

			// Set up the drawer.
			mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
					(DrawerLayout) findViewById(R.id.drawer_layout));

	        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	        // If the adapter is null, then Bluetooth is not supported
	        if (mBluetoothAdapter == null) {
	            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
	            // allow to run on emulator
	            //finish();
	            //return;
	        }

			mSensorManager = (SensorManager) getSystemService("sensor");// SENSOR_SERVICE);
			mSensorData = SensorRx.INSTANCE(this);//new SensorRx(this);

			mArobotSettings = new ArobotSettings();

			//getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

		    //setContentView(R.layout.main);

		    // experiment with the ActionBar
		    //mActionBar = getActionBar();
		    //mActionBar.hide();
		}
		updateFromPreferences();
	}
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		if ((mSensorMovementFragment == null) && (mBluetoothFragment == null)) {
			if(position == POSITION_MOVEMENT_VIEW){
				mFragmentIndexAct = FRAGMENT_MOVEMENT_VIEW;
				mFragmentIndexNew = FRAGMENT_MOVEMENT_VIEW;
				mSensorMovementFragment = SensorMovementFragment.newInstance(position, this);
				fragmentManager
					.beginTransaction()
					.add(R.id.container,
							mSensorMovementFragment).commit();
			} else if(position == POSITION_BLUETOOTH_VIEW){
				mFragmentIndexAct = FRAGMENT_BLUETOOTH_VIEW;
				mFragmentIndexNew = FRAGMENT_BLUETOOTH_VIEW;
				mBluetoothFragment = BluetoothFragment.newInstance(position);
				fragmentManager
					.beginTransaction()
					.add(R.id.container,
							mBluetoothFragment).commit();
//			} else if(position == POSITION_PREFS_ACTIVITY){
//				mFragmentIndexNew = -1;
//				Intent i = new Intent(this, PreferencesActivity.class);
//				startActivityForResult(i, SHOW_PREFERENCES);
			} else {
				fragmentManager
				.beginTransaction()
				.add(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
			}
		} else {
			if(position == POSITION_MOVEMENT_VIEW){
				mFragmentIndexAct = FRAGMENT_MOVEMENT_VIEW;
				mFragmentIndexNew = FRAGMENT_MOVEMENT_VIEW;
				mSensorMovementFragment = SensorMovementFragment.newInstance(position, this);
				fragmentManager
					.beginTransaction()
					.replace(R.id.container,
							mSensorMovementFragment).commit();
			} else if(position == POSITION_BLUETOOTH_VIEW){
				mFragmentIndexAct = FRAGMENT_BLUETOOTH_VIEW;
				mFragmentIndexNew = FRAGMENT_BLUETOOTH_VIEW;
					mBluetoothFragment = BluetoothFragment.newInstance(position);
					fragmentManager
						.beginTransaction()
						.replace(R.id.container,
								mBluetoothFragment).commit();
//			} else if(position == POSITION_PREFS_ACTIVITY){
//				mFragmentIndexNew = -1;
//				Intent i = new Intent(this, PreferencesActivity.class);
//
//				startActivityForResult(i, SHOW_PREFERENCES);
			} else {
				fragmentManager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
			}
		}
	}

	public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			mTitle = getString(R.string.title_section3);
			break;
		}
	}

	public void restoreActionBar() {//afetr orefs should be -1
		ActionBar actionBar = getActionBar();
		if(mFragmentIndexAct == FRAGMENT_MOVEMENT_VIEW) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		} else if (mFragmentIndexNew == FRAGMENT_BLUETOOTH_VIEW) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		} else {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.activity_main_menu, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
        Intent serverIntent = null;
		int id = item.getItemId();
		if(id == R.id.connect_device){
			if(mBTConnected == true){
				mBTConnected = false;
//				if(mBTService.getState() == BluetoothService.STATE_CONNECTED){
				mBTService.stop();
				if (mSensorMovementFragment != null)
					mSensorMovementFragment.setMovementEnabled(false);
				mSensorMovementFragment.updateUI();

			} else {
				startBluetooth();
		           // Launch the DeviceListActivity to see devices and do scan
	            serverIntent = new Intent(this, DeviceListActivity.class);
	            startActivityForResult(serverIntent, BTDefs.REQUEST_CONNECT_DEVICE);
			}
            return true;
		} else if (id == R.id.start_stop_saving) {
			if(!mSavingSensorData){
				mSavingSensorData = true;
				mSensorMovementFragment.startSavingSensorsData();
			} else {
				mSavingSensorData = false;
				mSensorMovementFragment.stopSavingSensorsData();

			}

//		} else if (id == R.id.action_settings) {
//			Intent i = new Intent(this, PreferencesActivity.class);
//
//			startActivityForResult(i, SHOW_PREFERENCES);
//			return true;
		} else if (id == R.id.action_about) {
			showAppVersion();
			return true;
		}


		return super.onOptionsItemSelected(item);
	}

	private void startBluetooth(){
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BTDefs.REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mBTService == null)
            	createBTService(mHandler);
        }

	}

	void resumeBTConnection(){
        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
              mBTService.start();
            }
        }

	}

	private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBTService.connect(device);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BTDefs.MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    setStatus(R.string.title_connected_to);// + mConnectedDeviceName);
                    mBTConnected = true;
					if (mSensorMovementFragment != null)
						mSensorMovementFragment.updateUI();
					if(mBluetoothFragment != null)
                    	mBluetoothFragment.clearChatAdapter();
                    break;
                case BluetoothService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case BTDefs.MESSAGE_WRITE:
                if(mBluetoothFragment != null)
                	mBluetoothFragment.writeMsgFromHandler(msg);
                break;
            case BTDefs.MESSAGE_READ:
                if(mBluetoothFragment != null)
                	mBluetoothFragment.readMsgFromHandler(msg);
                break;
            case BTDefs.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(BTDefs.DEVICE_NAME);
                setBTConnected(true);

                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case BTDefs.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(BTDefs.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case SHOW_PREFERENCES:
            updateFromPreferences();
            //TODO: check what to do after coming back,
            // if one fragment was already running
            recreateFragment(mFragmentIndexAct);
        break;
    case BTDefs.REQUEST_CONNECT_DEVICE:
        // When DeviceListActivity returns with a device to connect
        if (resultCode == Activity.RESULT_OK) {
            connectDevice(data, false);
        } //TODO: if no paired devices, then show available
        break;
    case BTDefs.REQUEST_ENABLE_BT:
        // When the request to enable Bluetooth returns
        if (resultCode == Activity.RESULT_OK) {
            // Bluetooth is now enabled, so set up a chat session
        	createBTService(mHandler);//setupChat();
        } else {
            // User did not enable Bluetooth or an error occurred
            Log.d(TAG, "BT not enabled");
            Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    }

	private void recreateFragment(int fragmentID) {
		FragmentManager fragmentManager = getFragmentManager();

		if((fragmentID < 0) || (fragmentID == FRAGMENT_MOVEMENT_VIEW)){// no fragment was used before, start with movement
			mFragmentIndexAct = FRAGMENT_MOVEMENT_VIEW;
			mFragmentIndexNew = FRAGMENT_MOVEMENT_VIEW;
//check both, if any then restore
			if ((mSensorMovementFragment == null) && (mBluetoothFragment == null)) { // before another fragment was not activ
				mSensorMovementFragment = SensorMovementFragment.newInstance(fragmentID, this);
				fragmentManager
					.beginTransaction()
					.add(R.id.container,
							mSensorMovementFragment).commit();
			} else { // before BTFragment was active
				mSensorMovementFragment = SensorMovementFragment.newInstance(fragmentID, this);
				fragmentManager
					.beginTransaction()
					.replace(R.id.container,
							mSensorMovementFragment).commit();
			}
		} else if (fragmentID == FRAGMENT_BLUETOOTH_VIEW) {
			mFragmentIndexAct = FRAGMENT_BLUETOOTH_VIEW;
			mFragmentIndexNew = FRAGMENT_BLUETOOTH_VIEW;
			if ((mSensorMovementFragment == null) && (mBluetoothFragment == null)) {
				mBluetoothFragment = BluetoothFragment.newInstance(fragmentID);
				fragmentManager
					.beginTransaction()
					.add(R.id.container,
							mBluetoothFragment).commit();
			}else{
				mFragmentIndexAct = FRAGMENT_BLUETOOTH_VIEW;
				mBluetoothFragment = BluetoothFragment.newInstance(fragmentID);
				fragmentManager
					.beginTransaction()
					.replace(R.id.container,
							mBluetoothFragment).commit();
			}
		}
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			 Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	    }
	};


	// implemented callback for SensorRx
	// receive the results from SensorRx
	// pack data into BT-Frame
	// write to BT-Service
    public void onNewBTCommand(byte[] btMessage) {
    	//check if BT connected
		if(mBTService != null){
	    	if(mBTService.getState() == BluetoothService.STATE_CONNECTED) {
				if (mSensorMovementFragment.isMovementEnabled()) {
					// send message
	    			mBTService.write(btMessage);
	    		} else {
	    			mBTService.write(BT_STOP_MESSAGE);
	    		}
	    	}
		}
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
		mSensorMovementFragment.setMovementEnabled(false);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
       if (mBTService != null) mBTService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

	private void updateFromPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		mArobotSettings.loadSettings(context, prefs);

		//TODO: first active if Fragment is active
		if (mSensorMovementFragment != null) {
			if (mSensorMovementFragment.mPrefsCreated) {
				mSensorMovementFragment.updateParams();
			}
		}
		//		if(mBluetoothFragment != null){
//			mBluetoothFragment.updateParams();
//		}
	}

	public void showAppVersion(){
		String versionName;
		String packageName;
		int versionCode;
		try{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = pInfo.versionName;
			versionCode = pInfo.versionCode;
			packageName = pInfo.packageName;
		} catch (NameNotFoundException e){
			versionName = "not known";
			versionCode = -1;
			packageName = "not known";
		}

		Toast.makeText(this,
			     "PackageName = " + packageName + "\nVersionCode = "
			       + versionCode + "\nVersionName = "
			       + versionName , Toast.LENGTH_SHORT).show();
	}

	private final void setStatus(int resId) {
	    //final ActionBar actionBar = getActionBar();
	    //mActionBar.setSubtitle(resId);
	}


	// getters, setters
	public ArobotSettings getArobotSettings() {
		return mArobotSettings;
	}
	public void setArobotSettings(ArobotSettings arobotSettings) {
		mArobotSettings = arobotSettings;
	}

	public SensorRx getSensorData() {
		return mSensorData;
	}

	public boolean getBTConnected() {
		return mBTConnected;
	}
	public void setBTConnected(boolean bTConnected) {
		mBTConnected = bTConnected;
	}

	public BluetoothService createBTService(Handler handler) {
        mBTService = new BluetoothService(this, handler);

		return mBTService;
	}
	public BluetoothService getChatService() {

		return mBTService;
	}
	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}
    public SensorManager getSensorManager() {
		return mSensorManager;
	}


}
