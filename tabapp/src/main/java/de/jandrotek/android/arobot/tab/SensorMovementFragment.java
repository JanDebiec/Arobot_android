package de.jandrotek.android.arobot.tab;

import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.text.DecimalFormat;
import java.util.Timer;

import de.jandrotek.android.arobot.libbluetooth.BluetoothDefines;
import de.jandrotek.android.arobot.libbluetooth.BluetoothService;

public class SensorMovementFragment extends Fragment
//        implements SensorRx.Callbacks {
         {

    private static final String TAG = "SensorMovementFragment";
    private static final String CSV_BASE_HEADER = "sensor, time, X Axis,Y Axis,Z Axis";
    DecimalFormat d = ArobotDefines.d;
    // selecting child fragments
    //private static final int SELECTED_LEFTRIGHT_CHILD = 2;
//    private static final int SELECTED_ASCIIDATA_CHILD = 1;
//    private static final int SELECTED_TILTVIEW_CHILD = 0;


    /// Model's members
    private SensorService mSensorService = null;

//    public void setSensorMoveController(SensorMovementController mSensorMoveController) {
//     this.mSensorMoveController = mSensorMoveController;
//    }
//
//    private SensorMovementController mSensorMoveController;
    private ArobotSettings mArobotSettings; //TODO move to Activity


    /// Control's members
    private Context mContext;
//    private boolean mSavingSensorData = false;
    public float[] mSensorReceivedData;
//    private PrintWriter mPrintWriter;
    public int mSelectedSensorDelay;
    private int mRollOffsset;
//    private float mFilterFactor;
//    public int mSelectedSavingContent;
//    public int mSelectedDisplayContent = 2;
//    private int mSelectedChildFragmentID;
    public boolean mPrefsCreated = false;
    private int mTimerPeriod;
    private boolean mMovementEnabled = false;
    private Timer mBlinkTimer;



    public boolean isMovementEnabled() {
        return mMovementEnabled;
    }


    public void setBTService(BluetoothService BTService) {
        mBTService = BTService;
    }

    // BT control
    private BluetoothService mBTService = null;

    /// View's members
    private TiltView mTilter;
    private OnNavigationListener mOnNavigationListener;

    public static SensorMovementFragment newInstance(int sectionNumber, Context context) {
        SensorMovementFragment fragment = new SensorMovementFragment();
        Bundle args = new Bundle();
        fragment.setContext(context);
        fragment.setArguments(args);
        return fragment;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) Log.i(TAG, "onCreate");
            setRetainInstance(true);// onDestroy will not be called

            mArobotSettings = ((MovementActivity) getActivity()).getArobotSettings();
            mBlinkTimer = new Timer();

            updateParams();

            mSensorReceivedData = new float[7];
            mSensorReceivedData[0] = (float) 0.95;
            mSensorReceivedData[1] = (float) 0.95;
            mSensorReceivedData[2] = (float) 0.95;
            mPrefsCreated = true;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_sensor_movement, parent, false);
        mTilter = (TiltView)v.findViewById(R.id.tiltView);

        return v;

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.fragment_bt_option_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }


    public void updateOrientationDisplay() {
        // moved to activity
//        mLeftCmdView.setText(d.format(mSensorReceivedData[3]));
//        mRightCmdView.setText(d.format(mSensorReceivedData[4]));
        if (mTilter != null) {
            mTilter.setTilt(mSensorReceivedData[6],
                    mSensorReceivedData[1]);
            }
    }

    private Runnable updateOrientationDisplayTask = new Runnable() {
        public void run() {
            updateOrientationDisplay();
        }
    };
    private int mPWMMin;
    private float mAmplification;

    @Override
    public void onStart() {
        super.onStart();
        if (BuildConfig.DEBUG) Log.i(TAG, "onStart");
//        mSensorMoveController.init();
        if (mSensorService != null) {
            mSensorService.setUpdateUi(true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) Log.i(TAG, "onResume");
//        mSensorMoveController.startSensors();
        if (mSensorService != null) {
            mSensorService.startFuseCalc();
            mSensorService.setUpdateUi(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSensorService != null) {
            mSensorService.setUpdateUi(false);
        }
//        mSensorMoveController.cleanSensors();
//        setMovementEnabled(false);
        if (BuildConfig.DEBUG) Log.i(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSensorService != null) {
            mSensorService.setUpdateUi(false);
        }
        if (BuildConfig.DEBUG) Log.i(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Log.i(TAG, "onDestroy");
    }


    public void restartSensors() {
        mSensorService.initListeners(mSelectedSensorDelay);

    }

    public void updateParams() {
        mSelectedSensorDelay = mArobotSettings.getPrefsSensorDelay();// = SensorManager.SENSOR_DELAY_FASTEST;
//        mFilterFactor = mArobotSettings.getFilterFactor();
//        mSelectedSavingContent = mArobotSettings.getPrefsSavingContent();
        mTimerPeriod = mArobotSettings.getPrefsTimerPeriod();
//        mRollOffsset = mArobotSettings.getPrefsRollOffset();
//        mPWMMin = mArobotSettings.getPrefsPwmMinimal();
//        mAmplification = mArobotSettings.getPrefsAmplification();
//        mSensorService.setFilterCoeff(mFilterFactor);
//        mSensorService.setTimerPeriod(mTimerPeriod);
//        mMovCalculator.setRollOffset(mRollOffsset);
//        mMovCalculator.setPWMMin(mPWMMin);
//        mMovCalculator.setScaleCorrection(mAmplification);

    }

    // implemented callback for SensorRx
    // receive the results from SensorRx
    // pack data into BT-Frame
    // write to BT-Service
    public void onNewBTCommand(byte[] btMessage) {
        //check if BT connected
        if (mBTService != null) {
            if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
                if (isMovementEnabled()) {
                    // send message
                    mBTService.write(btMessage);
                } else {
                    mBTService.write(BluetoothDefines.BT_STOP_MESSAGE);
                }
            }
        }
    }

    // handling messages from HandlerThread from SensorService
//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//            }
//        }
//    }


}
