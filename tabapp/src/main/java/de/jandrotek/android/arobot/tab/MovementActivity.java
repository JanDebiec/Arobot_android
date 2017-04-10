package de.jandrotek.android.arobot.tab;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//import com.google.android.gms.appindexing.Action;

import de.jandrotek.android.arobot.core.SensorCalc;
//import de.jandrotek.android.arobot.libbluetooth;
import de.jandrotek.android.arobot.libbluetooth.BTDefs;
import de.jandrotek.android.arobot.libbluetooth.BluetoothDefines;
import de.jandrotek.android.arobot.libbluetooth.BluetoothFragment;
import de.jandrotek.android.arobot.libbluetooth.BluetoothService;

import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

// moving RxSensor to service with own thread
public class MovementActivity extends AppCompatActivity {
    private static final String TAG = "MovementActivity";

    private ArobotSettings mArobotSettings;

    //fragment control vars
    private SensorMovementFragment mSensorMovementFragment;
    private ManualMovementFragment mManualMovementFragment;
    private BluetoothFragment mBluetoothFragment;
//    private SensorMovementController mSensorMovementController;
    private SensorService mSensorService;
    private SensorCalc mMovCalculator; // for use in RxSensor

    private int mFragmentIndexAct = -1;// on start, no fragment selected
    private int mFragmentIndexNew = -1;// on start, no fragment selected

    // BT control vars
    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean mBTConnected = false;
    // we need service here, some other fragments can write to BT too
    private BluetoothService mBTService = null;
    private String mConnectedDeviceName = null;
    private SensorManager mSensorManager = null;

    // own widgets
    private ToggleButton mToggleButtonMove;
    private TextView mBTConnectStatus;
    //private Button mBTConnectBtn;
    private TextView mMovingStatus;
    private TextView mLeftCmdView;
    private TextView mRightCmdView;
    private TextView mFragmentName;
    private boolean mAppBarExpanded = true;
    private AppBarLayout mAppBarLayout;
    private FloatingActionButton mFab;

    private static final int SHOW_PREFERENCES = 1;
    private boolean mMovementEnabled = false;
    private boolean mMovementThreadRun = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient mClient;
            // calculator settings
    private int mPWMMin;
    private float mAmplification;
    private int mRollOffsset;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check orientation
        int screenOrientation = getResources().getConfiguration().orientation;
        if(screenOrientation == ORIENTATION_PORTRAIT){
            // show screen portrait
            return;
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorService = new SensorService(mSensorManager);
//        mSensorMovementController = new MovementController(mSensorManager);
        mMovCalculator = new SensorCalc();
//        mSensorMovementController.setCalculator(mMovCalculator);
        setContentView(R.layout.activity_movement);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mAppBarLayout = (AppBarLayout)findViewById((R.id.appbar));

        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(new MyAdapter(
                toolbar.getContext(),
                ArobotDefines.fragmentNames
        ));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.
                showProperFragment(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO start/stop calculations (threads)
                if(mAppBarExpanded) { //run
                    if(mFragmentIndexAct == ArobotDefines.POSITION_SENSOR_MOVEMENT) {
//                        mSensorMovementController.activateMovementThread(true);
                        mSensorService.setLoopActive(true);
                    }
                    mAppBarExpanded = false;
                    mFab.setImageResource(ic_media_pause);
                } else { // pause
                    if(mFragmentIndexAct == ArobotDefines.POSITION_SENSOR_MOVEMENT) {
//                        mSensorMovementController.activateMovementThread(false);
                        mSensorService.setLoopActive(false);
                    }
                    mAppBarExpanded = true;
                    mFab.setImageResource(ic_media_play);
                }

                mAppBarLayout.setExpanded(mAppBarExpanded);


//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        // prepare bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            // allow to run on emulator
            //finish();
            //return;
        }

        // own widgets
        mBTConnectStatus = (TextView) findViewById(R.id.tVConnected);
        if (getBTConnected()) {
            mBTConnectStatus.setBackgroundColor(ArobotDefines.COLOR_GREEN);
        } else {
            mBTConnectStatus.setBackgroundColor(ArobotDefines.COLOR_GREY);
        }

        //mBTConnectBtn = (Button)v.findViewById(R.id.btnBtConnect);
        //mBTConnectBtn.setEnabled(false);

        mMovingStatus = (TextView) findViewById(R.id.tVMoving);
        mLeftCmdView = (TextView) findViewById(R.id.tvTiltLeft);
        mRightCmdView = (TextView) findViewById(R.id.tvTiltRight);
        mFragmentName = (TextView) findViewById(R.id.tvFragmentName);

        mToggleButtonMove = (ToggleButton) findViewById(R.id.toggleButtonMove);
        mToggleButtonMove.setEnabled(false);
        mToggleButtonMove.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMovementEnabled == false) {
                    mMovementEnabled = true;
                    mMovingStatus.setBackgroundColor(Color.GREEN);
                    mMovingStatus.setText(R.string.moving_status_move);
                    mToggleButtonMove.setText(R.string.move_button_on);
                    mToggleButtonMove.setBackgroundColor(Color.RED);
                } else {
                    mMovementEnabled = false;
                    mMovingStatus.setBackgroundColor(Color.LTGRAY);
                    mMovingStatus.setText(R.string.moving_status_stop);
                    mToggleButtonMove.setText(R.string.move_button_off);
                    mToggleButtonMove.setBackgroundColor(Color.LTGRAY);
                }

            }
        });



        mArobotSettings = new ArobotSettings();
        updateFromPreferences();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void showProperFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if ((mSensorMovementFragment == null) && (mManualMovementFragment == null) && (mBluetoothFragment == null)) {
            if (position == ArobotDefines.POSITION_SENSOR_MOVEMENT) {
                mFragmentIndexAct = ArobotDefines.POSITION_SENSOR_MOVEMENT;
                mFragmentIndexNew = ArobotDefines.POSITION_SENSOR_MOVEMENT;
                mSensorMovementFragment = SensorMovementFragment.newInstance(position, this);
//                mSensorMovementFragment.setSensorMoveController(mSensorMovementController);
//                mSensorMovementController.setSensorMovementFragment(mSensorMovementFragment);
                fragmentManager
                        .beginTransaction()
                        .add(R.id.container,
                                mSensorMovementFragment).commitAllowingStateLoss();
            } else if (position == ArobotDefines.POSITION_MANUAL_MOVEMENT) {
                mFragmentIndexAct = ArobotDefines.POSITION_MANUAL_MOVEMENT;
                mFragmentIndexNew = ArobotDefines.POSITION_MANUAL_MOVEMENT;
                mManualMovementFragment = ManualMovementFragment.getInstance();
                fragmentManager
                        .beginTransaction()
                        .add(R.id.container,
                                mManualMovementFragment).commit();
            } else if (position == ArobotDefines.POSITION_BLUETOOTH_CHAT) {
                mFragmentIndexAct = ArobotDefines.POSITION_BLUETOOTH_CHAT;
                mFragmentIndexNew = ArobotDefines.POSITION_BLUETOOTH_CHAT;
                mBluetoothFragment = BluetoothFragment.getInstance();
                fragmentManager
                        .beginTransaction()
                        .add(R.id.container,
                                mBluetoothFragment).commit();
            }
        } else {
            if (position == ArobotDefines.POSITION_SENSOR_MOVEMENT) {
                mFragmentIndexAct = ArobotDefines.POSITION_SENSOR_MOVEMENT;
                mFragmentIndexNew = ArobotDefines.POSITION_SENSOR_MOVEMENT;
                mSensorMovementFragment = SensorMovementFragment.newInstance(position, this);
//                mSensorMovementFragment.setSensorMoveController(mSensorMovementController);
//                mSensorMovementController.setSensorMovementFragment(mSensorMovementFragment);
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container,
                                mSensorMovementFragment).commit();
            } else if (position == ArobotDefines.POSITION_MANUAL_MOVEMENT) {
                mFragmentIndexAct = ArobotDefines.POSITION_MANUAL_MOVEMENT;
                mFragmentIndexNew = ArobotDefines.POSITION_MANUAL_MOVEMENT;
                mManualMovementFragment = ManualMovementFragment.getInstance();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container,
                                mManualMovementFragment).commit();
            } else if (position == ArobotDefines.POSITION_BLUETOOTH_CHAT) {
                mFragmentIndexAct = ArobotDefines.POSITION_BLUETOOTH_CHAT;
                mFragmentIndexNew = ArobotDefines.POSITION_BLUETOOTH_CHAT;
                mBluetoothFragment = BluetoothFragment.getInstance();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container,
                                mBluetoothFragment).commit();
            }
        }
        mFragmentName.setText(ArobotDefines.fragmentNames[position]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movement, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent serverIntent = null;
        int id = item.getItemId();
        if (id == R.id.connect_device) {
            if (mBTConnected == true) {
                mBTConnected = false;
//				if(mBTService.getState() == BluetoothService.STATE_CONNECTED){
                mBTService.stop();
//                if (mSensorMovementFragment != null)
//                    mSensorMovementFragment.setMovementEnabled(false);
                updateUI();

            } else {
                startBluetooth();
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, BluetoothDefines.REQUEST_CONNECT_DEVICE);
            }
            return true;
        } else if (id == R.id.action_settings) {
            Intent i = new Intent(this, PreferencesActivity.class);

            startActivityForResult(i, SHOW_PREFERENCES);
            return true;
        } else if (id == R.id.action_about) {
            showAppVersion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        mClient.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Movement Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://de.jandrotek.android.arobot.tab/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(mClient, viewAction);
    }

    @Override
    public void onPause(){
    super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "Movement Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://de.jandrotek.android.arobot.tab/http/host/path")
//        );
////        AppIndex.AppIndexApi.end(mClient, viewAction);
//        mClient.disconnect();
    }


    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
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
        mPWMMin = mArobotSettings.getPrefsPwmMinimal();
        mRollOffsset = mArobotSettings.getPrefsRollOffset();
        mAmplification = mArobotSettings.getPrefsAmplification();
        mMovCalculator.setRollOffset(mRollOffsset);
        mMovCalculator.setPWMMin(mPWMMin);
        mMovCalculator.setScaleCorrection(mAmplification);
        //		if(mBluetoothFragment != null){
//			mBluetoothFragment.updateParams();
//		}
    }

    private void startBluetooth() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothDefines.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mBTService == null)
                mBTService = createBTService(mHandler);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SHOW_PREFERENCES:
                updateFromPreferences();
                //TODO: check what to do after coming back,
                // if one fragment was already running
//                recreateFragment(mFragmentIndexAct);
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

    void resumeBTConnection() {
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

    public ArobotSettings getArobotSettings() {
        return mArobotSettings;
    }

    // implemented callback for SensorRx
    // receive the results from SensorRx
    // pack data into BT-Frame
    // write to BT-Service
    public void onNewBTCommand(byte[] btMessage) {
        //check if BT connecgted
        if (mBTService != null) {
            if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
                if (mSensorMovementFragment.isMovementEnabled()) {
                    // send message
                    mBTService.write(btMessage);
                } else {
                    mBTService.write(BluetoothDefines.BT_STOP_MESSAGE);
                }
            }
        }
    }

    public void showAppVersion() {
        String versionName;
        String packageName;
        int versionCode;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
            packageName = pInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "not known";
            versionCode = -1;
            packageName = "not known";
        }

        Toast.makeText(this,
                "PackageName = " + packageName + "\nVersionCode = "
                        + versionCode + "\nVersionName = "
                        + versionName, Toast.LENGTH_SHORT).show();
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothDefines.MESSAGE_STATE_CHANGE:
                    if (BuildConfig.DEBUG) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(R.string.title_connected_to);// + mConnectedDeviceName);
                            mBTConnected = true;
                            updateUI();
                            if (mBluetoothFragment != null)
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
                case BluetoothDefines.MESSAGE_WRITE:
                    if (mBluetoothFragment != null)
                        mBluetoothFragment.writeMsgFromHandler(msg);
                    break;
                case BluetoothDefines.MESSAGE_READ:
                    if (mBluetoothFragment != null)
                        mBluetoothFragment.readMsgFromHandler(msg);
                    break;
                case BluetoothDefines.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothDefines.DEVICE_NAME);
                    setBTConnected(true);

                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothDefines.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothDefines.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final void setStatus(int resId) {
        //final ActionBar actionBar = getActionBar();
        //mActionBar.setSubtitle(resId);
    }

    private void ensureDiscoverable() {
        if (BuildConfig.DEBUG) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void updateUI() {
        if (getBTConnected()) {
            mBTConnectStatus.setBackgroundColor(Color.GREEN);
            mBTConnectStatus.setText(R.string.connected_status_on);
            mToggleButtonMove.setEnabled(true);

        } else {
            mBTConnectStatus.setBackgroundColor(Color.LTGRAY);
            mBTConnectStatus.setText(R.string.connected_status_off);
            mToggleButtonMove.setEnabled(false);
        }
        if (mMovementEnabled == false) {
            mMovingStatus.setBackgroundColor(Color.LTGRAY);
            mMovingStatus.setText(R.string.moving_status_stop);
            mToggleButtonMove.setText(R.string.move_button_off);
            mToggleButtonMove.setBackgroundColor(Color.LTGRAY);
        } else {
            mMovingStatus.setBackgroundColor(Color.GREEN);
            mMovingStatus.setText(R.string.moving_status_move);
            mToggleButtonMove.setText(R.string.move_button_on);
            mToggleButtonMove.setBackgroundColor(Color.RED);
        }

    }

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public PlaceholderFragment() {
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_sensor_movement, container, false);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }

}
