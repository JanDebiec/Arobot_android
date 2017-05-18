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

import org.rajawali3d.view.ISurface;

import java.text.DecimalFormat;
import java.util.Timer;

import de.jandrotek.android.arobot.core.ArobotDefines;
import de.jandrotek.android.arobot.libbluetooth.BluetoothDefines;
import de.jandrotek.android.arobot.libbluetooth.BluetoothService;

public class SensorMovementFragment extends Fragment
//        implements SensorRx.Callbacks {
         {

    private static final String TAG = "SensorMovementFragment";
    private static final String CSV_BASE_HEADER = "sensor, time, X Axis,Y Axis,Z Axis";
//    DecimalFormat d = ArobotDefines.d;

    /// Model's members
    private SensorService mSensorService = null;

    private ArobotSettings mArobotSettings; //TODO move to Activity


    /// Control's members
    private Context mContext;
    public float[] mSensorReceivedData;
    public int mSelectedSensorDelay;
    private int mRollOffsset;
    public boolean mPrefsCreated = false;
    private int mTimerPeriod;
    private boolean mMovementEnabled = false;
    private Timer mBlinkTimer;

    private float mOutputFilteredL;
    private float mOutputFilteredR;
    private String mStrLeft;
    private String mStrRight;

    public boolean isMovementEnabled() {
        return mMovementEnabled;
    }


//    public void setBTService(BluetoothService BTService) {
//        mBTService = BTService;
//    }
//
//    // BT control
//    private BluetoothService mBTService = null;

    /// View's members
     private RajawaliLoadModelRenderer mRenderer;
//     private TiltRenderer mRenderer;
     private ISurface mRajawaliSurface;

    private OnNavigationListener mOnNavigationListener;

    private SensorMovementFragment(){
    }

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


        // Find the TextureView
        mRajawaliSurface = (ISurface) v.findViewById(R.id.rajwali_surface);
        mRenderer = new RajawaliLoadModelRenderer(getActivity());

        mRajawaliSurface.setSurfaceRenderer(mRenderer);
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
        if(mRenderer != null){
            mRenderer.setRotateValues(
                    -mSensorReceivedData[5],//ok
                    0,
                    mSensorReceivedData[6] //ok
                    );
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
        if (mSensorService != null) {
            mSensorService.setUpdateUi(true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) Log.i(TAG, "onResume");
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
            mSensorService.unregisterSensors();
        }
        if (BuildConfig.DEBUG) Log.i(TAG, "onPause");
        mRenderer.setSceneInitialized(false);
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

}
