package de.jandrotek.android.arobot.tab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.appindexing.Action;

import de.jandrotek.android.arobot.core.ArobotDefines;
import de.jandrotek.android.arobot.core.SensorCalc;
//import de.jandrotek.android.arobot.libbluetooth;
import de.jandrotek.android.arobot.libbluetooth.BTDefs;
import de.jandrotek.android.arobot.libbluetooth.BluetoothDefines;
import de.jandrotek.android.arobot.libbluetooth.BluetoothFragment;
import de.jandrotek.android.arobot.libbluetooth.BluetoothInterface;
import de.jandrotek.android.arobot.libbluetooth.BluetoothService;
import de.jandrotek.android.arobot.libbluetooth.DeviceListActivity;
import de.jandrotek.android.arobot.libbluetooth.TxBTMessage;
import de.jandrotek.android.arobot.libwifi.WlanDefines;

import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;
import static android.R.drawable.ic_menu_help;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static de.jandrotek.android.arobot.core.ArobotDefines.eBtVelCmdScaleFactor;
import static de.jandrotek.android.arobot.tab.AppStateController.eColorIdle;
import static de.jandrotek.android.arobot.tab.AppStateController.eColorMoving;
import static de.jandrotek.android.arobot.tab.AppStateController.eColorReadyToMove;

public class MovementActivity extends AppCompatActivity {
    private static final String TAG = "MovementActivity";

    private ArobotSettings mArobotSettings;
//    public static DecimalFormat cmdFormat = new DecimalFormat(" ####.0; -####.0");

    private AppStateController mStateController;

    //fragment control vars
    private SensorMovementFragment mSensorMovementFragment;
    private ManualMovementFragment mManualMovementFragment;
    private BluetoothFragment mBluetoothFragment;
    private SensorService mSensorService;
    private SensorCalc mMovCalculator; // for use in RxSensor
    private SensorManager mSensorManager = null;
    private RajawaliLoadModelRenderer mRenderer = null;


    // BT control vars, all moved to BTInterface
    private BluetoothInterface mBTInterface = null;
    private static boolean mBTConnected = false;
    private static String mConnectedDeviceName = null;


    // own widgets
    // will be moved to ActBar, or removed and only FAB will be used
//    private ToggleButton mToggleButtonMove;
    // will be removed
    // connect status will be shown in ActBar with icons
//    private TextView mBTConnectStatus;

//    private TextView mMovingStatus;
    // moving status will be shown double: by ToggleButton and FAB


    private TextView mFragmentName;

    // will be not used
    //    private boolean mAppBarExpanded = true;
//    private int mVisibility = View.VISIBLE;
    private AppBarLayout mAppBarLayout;
    private FloatingActionButton mFab;

    // to set the color of FAB
    // mFab.setBackgroundTintList(ColorStateList.valueOf(your color in int));

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
    private int mFragmentIndexAct = -1;// on start, no fragment selected
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private Display mDisplay;
    private PowerManager.WakeLock mWakeLock;
    private TextView mtvTiltLeft;
    private TextView mtvTiltRight;
    private String mStrLeft;
    private String mStrRight;

    private int mExternalConn = ArobotDefines.EXT_CONN_BT;
    private TxBTMessage mBTMessCreator;
    private float[] mLeftRightCmd;
    private byte[] mBTMessage;

    private int mExtInterfaceOld = ArobotDefines.EXT_CONN_UNKNOWN;
    private int mExtInterfaceNew = ArobotDefines.EXT_CONN_UNKNOWN;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check orientation
        int screenOrientation = getResources().getConfiguration().orientation;
        if(screenOrientation == ORIENTATION_PORTRAIT){
            // show screen portrait
            return;
        }
        mLeftRightCmd = new float[2];
        mBTMessCreator = new TxBTMessage();
        mMovCalculator = new SensorCalc.Builder().build();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorService = new SensorService(this, mSensorManager);
        mSensorService.setCalculator(mMovCalculator);

        mRenderer = new RajawaliLoadModelRenderer(this);


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
                handleFABPress();
            }
        });
        mFab.setImageResource(ic_menu_help);
        mFab.setBackgroundTintList(ColorStateList.valueOf(eColorIdle));

        //        prepareBTInterface();
        mBTInterface = new BluetoothInterface(this, mHandler);
//        mBluetoothFragment = BluetoothFragment.getInstance();

        mFragmentName = (TextView) findViewById(R.id.tvFragmentName);

        mArobotSettings = new ArobotSettings();
        updateFromPreferences();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Create a bright wake lock
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass()
                .getName());

        mtvTiltLeft = (TextView)findViewById(R.id.tvTiltLeft);
        mtvTiltRight = (TextView)findViewById(R.id.tvTiltRight);

    }

    private void handleFABPress() {
        // get state form StateCOntroller
        int state = mStateController.getAppState();
        // depending on state, call the proper commands
        if(state == AppStateController.eNotConnected){
            // try to connect
        } else if (state == AppStateController.eConnected){

        } else if (state == AppStateController.eReadyToMove){
            if(mFragmentIndexAct == ArobotDefines.POSITION_SENSOR_MOVEMENT) {
                if (mSensorService != null) {
                    startMoveInSensFrag();
                }
            }
            mFab.setImageResource(ic_media_pause);
            mFab.setBackgroundTintList(ColorStateList.valueOf(eColorMoving));
            mStateController.setAppState(AppStateController.eMoving);

        } else if (state == AppStateController.eMoving){
            if(mFragmentIndexAct == ArobotDefines.POSITION_SENSOR_MOVEMENT) {
                stopMoveInSensFrag();
            }
            mFab.setImageResource(ic_media_play);
            mFab.setBackgroundTintList(ColorStateList.valueOf(eColorReadyToMove));
            mStateController.setAppState(AppStateController.eReadyToMove);

        } else { // unknown

        }
    }

    private void stopMoveInSensFrag() {
        mSensorService.setRunFuseTask(false);
        mSensorService.unregisterSensors();
    }

    private void startMoveInSensFrag() {
        mSensorService.setFragment(mSensorMovementFragment);
        mSensorService.registerSensors();
        mSensorService.setRunFuseTask(true);
        mSensorService.startFuseCalc();
        mSensorService.setUpdateUi(true);
    }

    private void allowMovement(boolean flag) {
        if(mExternalConn == ArobotDefines.EXT_CONN_BT) {
            mBTInterface.setMovementAllowed(flag);
        } else if(mExternalConn == ArobotDefines.EXT_CONN_WLAN){

        }
    }

    private void showProperFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if ((mSensorMovementFragment == null) && (mManualMovementFragment == null) && (mBluetoothFragment == null)) {
            if (position == ArobotDefines.POSITION_SENSOR_MOVEMENT) {
                mFragmentIndexAct = ArobotDefines.POSITION_SENSOR_MOVEMENT;
                mSensorMovementFragment = SensorMovementFragment.newInstance(position, this, mRenderer);
                fragmentManager
                        .beginTransaction()
                        .add(R.id.container,
                                mSensorMovementFragment).commitAllowingStateLoss();
            } else if (position == ArobotDefines.POSITION_MANUAL_MOVEMENT) {
                mFragmentIndexAct = ArobotDefines.POSITION_MANUAL_MOVEMENT;
                mManualMovementFragment = ManualMovementFragment.getInstance(mRenderer);
                fragmentManager
                        .beginTransaction()
                        .add(R.id.container,
                                mManualMovementFragment).commit();
            } else if (position == ArobotDefines.POSITION_BLUETOOTH_CHAT) {
                mFragmentIndexAct = ArobotDefines.POSITION_BLUETOOTH_CHAT;
                mBluetoothFragment = BluetoothFragment.getInstance();
                fragmentManager
                        .beginTransaction()
                        .add(R.id.container,
                                mBluetoothFragment).commit();
            }
        } else {
            if (position == ArobotDefines.POSITION_SENSOR_MOVEMENT) {
                mFragmentIndexAct = ArobotDefines.POSITION_SENSOR_MOVEMENT;
                mSensorMovementFragment = SensorMovementFragment.newInstance(position, this, mRenderer);
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container,
                                mSensorMovementFragment).commit();
            } else if (position == ArobotDefines.POSITION_MANUAL_MOVEMENT) {
                mFragmentIndexAct = ArobotDefines.POSITION_MANUAL_MOVEMENT;
                mManualMovementFragment = ManualMovementFragment.getInstance(mRenderer);
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.container,
                                mManualMovementFragment).commit();
            } else if (position == ArobotDefines.POSITION_BLUETOOTH_CHAT) {
                mFragmentIndexAct = ArobotDefines.POSITION_BLUETOOTH_CHAT;
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
            //TODO check which interface
            if(mExtInterfaceNew == ArobotDefines.EXT_CONN_BT) {
                if (mBTInterface.isBTConnected() == true) {
                    mBTInterface.stopBtService();
                    updateUI();
                } else {
                    mBTInterface.startBluetooth();
                    // Launch the DeviceListActivity to see devices and do scan
                    serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, BluetoothDefines.REQUEST_CONNECT_DEVICE);
                }
            } else if (mExtInterfaceNew == ArobotDefines.EXT_CONN_WLAN){

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
    public void onResume(){
        super.onResume();
        mWakeLock.acquire();
        // query state
        // set the FAB button, only the color and shape,
        // without calling internal functions, depending the state
    }

    @Override
    public void onPause(){
    super.onPause();
        mWakeLock.release();
    }

    @Override
    public void onStop() {
        super.onStop();
        // check if connections active, if yes stop

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

        //TODO check if ext-interface changed, if yes, then react
        mExtInterfaceNew = mArobotSettings.getPrefsExtInterface();
        if(mExtInterfaceNew != mExtInterfaceOld){

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
                    mBTInterface.connectBTDevice(data, false);
                } //TODO: if no paired devices, then show available
                break;
            case BTDefs.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    mBTInterface.createBTService();//setupChat();
//                    mBTInterface.createBTService(mHandler);//setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public ArobotSettings getArobotSettings() {
        return mArobotSettings;
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
    // TODO handler should han;de the messages from wlan preparing too
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
                    mBTInterface.setBTConnected(true);

                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothDefines.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothDefines.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
//WLAN handling
                case WlanDefines.MESSAGE_STATE_CHANGE:
                    break;
                case WlanDefines.MESSAGE_READ:
                    break;
                case WlanDefines.MESSAGE_WRITE:
                    break;
                case WlanDefines.MESSAGE_DEVICE_NAME:
                    break;
                case WlanDefines.MESSAGE_TOAST:
                    break;
            }
        }
    };

    private final void setStatus(int resId) {
        //final ActionBar actionBar = getActionBar();
        //mActionBar.setSubtitle(resId);
    }

//    private void ensureDiscoverable() {
//        if (BuildConfig.DEBUG) Log.d(TAG, "ensure discoverable");
//        if (mBluetoothAdapter.getScanMode() !=
//                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            startActivity(discoverableIntent);
//        }
//    }

    public void updateUI() {
        if (mBTInterface.isBTConnected()) {
//            mBTConnectStatus.setBackgroundColor(Color.GREEN);
//            mBTConnectStatus.setText(R.string.connected_status_on);
//            mToggleButtonMove.setEnabled(true);

        } else {
//            mBTConnectStatus.setBackgroundColor(Color.LTGRAY);
//            mBTConnectStatus.setText(R.string.connected_status_off);
//            mToggleButtonMove.setEnabled(false);
        }
        if (mMovementEnabled == false) {
//            mMovingStatus.setBackgroundColor(Color.LTGRAY);
//            mMovingStatus.setText(R.string.moving_status_stop);
//            mToggleButtonMove.setText(R.string.move_button_off);
//            mToggleButtonMove.setBackgroundColor(Color.LTGRAY);
        } else {
//            mMovingStatus.setBackgroundColor(Color.GREEN);
//            mMovingStatus.setText(R.string.moving_status_move);
//            mToggleButtonMove.setText(R.string.move_button_on);
//            mToggleButtonMove.setBackgroundColor(Color.RED);
        }
    }

    public void handleVelCmd(float cmdLeft, float cmdRight){
        updateCmdTxt(cmdLeft, cmdRight);
        txVelCmd(cmdLeft, cmdRight);
    }

    public void updateCmdTxt(float cmdLeft, float cmdRight){
        mStrLeft =  ArobotDefines.cmdFormat.format(cmdLeft);
        mStrRight =  ArobotDefines.cmdFormat.format(cmdRight);
        mtvTiltLeft.setText(mStrLeft);
        mtvTiltRight.setText(mStrRight);
    }

    public void txVelCmd(float cmdLeft, float cmdRight){
        if(mExternalConn == ArobotDefines.EXT_CONN_BT){
            mLeftRightCmd[0] = cmdLeft * eBtVelCmdScaleFactor;
            mLeftRightCmd[1] = cmdRight * eBtVelCmdScaleFactor;
            mBTMessage = mBTMessCreator.prepareTxMessage(mLeftRightCmd);

            mBTInterface.txNewBTCommand(mBTMessage);
        } else if (mExternalConn == ArobotDefines.EXT_CONN_WLAN){
            //TODO implement Wlan Interface
        }
    }
}
