package de.jandrotek.android.arobot.app.arobotbt;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import de.jandrotek.android.arobot.core.MoveCmdCalculator;

//import android.content.SharedPreferences;

public class SensorMovementFragment extends Fragment {
	/// constants
    private static final boolean D = true;

	private static final int COLOR_GREEN = 0x008000;
	private static final int COLOR_GREY = 0x0f0f0f;

	private static final String TAG = "SensorMovementFragment";
	private static final String CSV_BASE_HEADER = "sensor, time, X Axis,Y Axis,Z Axis";
	DecimalFormat d = SensorRx.d;
	// selecting child fragments
	//private static final int SELECTED_LEFTRIGHT_CHILD = 2;
	private static final int SELECTED_ASCIIDATA_CHILD = 1;
	private static final int SELECTED_TILTVIEW_CHILD = 0;


/// Model's members
	private SensorRx mSensorData = null;
    public MoveCmdCalculator mController; // for use in RxSensor
    private ArobotSettings mArobotSettings; //TODO move to Activity


/// Control's members
	private Context mContext;
	private SensorManager mSensorManager = null;
	private boolean mSavingSensorData = false;
	public float[] mSensorReceivedData;
	private PrintWriter mPrintWriter;
	public int mSelectedSensorDelay;
	private int mRollOffsset;
	private float mFilterFactor;
	public int mSelectedSavingContent;
	public int mSelectedDisplayContent = 2;
	private int mSelectedChildFragmentID;
	public boolean mPrefsCreated = false;
	private int mTimerPeriod;
	private boolean mMovementEnabled = false;
	private Timer mBlinkTimer;
//	private static final int BLINK_PERIOD = 1000;
//	private boolean mRunBlinkTask = false;
//	private boolean mBlinkOn = false;
//	private int mSavingDataSeconds = 0;


public boolean isMovementEnabled() {
		return mMovementEnabled;
	}

	public void setMovementEnabled(boolean movementEnabled) {
		mMovementEnabled = movementEnabled;
	}

	/// View's members
//	private RadioGroup mDelaySelector;

	private ToggleButton mToggleButtonMove;
	private TextView mBTConnectStatus;
	//private Button mBTConnectBtn;
	private TextView mMovingStatus;
	private TextView mSavingStatus;
	private TextView mLeftCmdView;
	private TextView mRightCmdView;


//	private RadioGroup mContentSelector;
	private OnNavigationListener mOnNavigationListener;

	/// child fragments
	private TiltViewFragment mTiltViewer = null;
	private AsciiViewFragment mAsciiViewer = null;
	private LeftRightViewFragment mLRViewer = null;


	public static SensorMovementFragment newInstance(int sectionNumber, Context context) {
		SensorMovementFragment fragment = new SensorMovementFragment();
		Bundle args = new Bundle();
		args.putInt(PlaceholderFragment.ARG_SECTION_NUMBER, sectionNumber);
		fragment.setContext(context);
		fragment.setArguments(args);
		return fragment;
	}

	public void setContext( Context context){
		mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState == null){
			if(D) Log.i(TAG, "onCreate");
			setRetainInstance(true);// onDestroy will not be called

			mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		    mController = new MoveCmdCalculator();
			mArobotSettings = ((MainActivity)getActivity()).getArobotSettings();
			mBlinkTimer = new Timer();

			mSensorData = ((MainActivity)getActivity()).getSensorData();
			mSensorData.connectParentFragment(this);
			mSensorData.init(mSensorManager);

			updateParams();

			//mSensorData.initListeners(mSelectedSensorDelay);

			mSensorReceivedData = new float[7];
			mSensorReceivedData[0] = (float)0.95;
			mSensorReceivedData[1] = (float)0.95;
			mSensorReceivedData[2] = (float)0.95;
			mPrefsCreated = true;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		if(D) Log.i(TAG, "onCreateView");
		View v = inflater.inflate(R.layout.fragment_movement, parent, false);
//		if(savedInstanceState == null){

		ActionBar actionbar = getActivity().getActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.display_list,
				android.R.layout.simple_spinner_dropdown_item);

		mBTConnectStatus  = (TextView)v.findViewById(R.id.tVConnected);
		if(((MainActivity)getActivity()).getBTConnected()){
			mBTConnectStatus.setBackgroundColor(COLOR_GREEN);
		}else{
			mBTConnectStatus.setBackgroundColor(COLOR_GREY);
		}

		//mBTConnectBtn = (Button)v.findViewById(R.id.btnBtConnect);
		//mBTConnectBtn.setEnabled(false);

		mMovingStatus = (TextView)v.findViewById(R.id.tVMoving);
		mSavingStatus = (TextView)v.findViewById(R.id.tVSavingStatus);
		mLeftCmdView = (TextView)v.findViewById(R.id.tvTiltLeft);
		mRightCmdView = (TextView)v.findViewById(R.id.tvTiltRight);

		mToggleButtonMove = (ToggleButton)v.findViewById(R.id.toggleButtonMove);
		mToggleButtonMove.setEnabled(false);
		mToggleButtonMove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mMovementEnabled == false){
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

		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mOnNavigationListener = new OnNavigationListener() {
			String[] strings = getResources().getStringArray(R.array.display_list);

			@Override
			public boolean onNavigationItemSelected(int position, long itemID){
				mSelectedChildFragmentID = position;
				FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
				// check if one child already exists, not yet
				if((mAsciiViewer == null) && (mTiltViewer == null) && (mLRViewer == null)){
//					if (position == SELECTED_LEFTRIGHT_CHILD){
//						mLRViewer = new LeftRightViewFragment();
//						transaction.add(R.id.child_fragment, mLRViewer).commit();
//					}
					if(position == SELECTED_ASCIIDATA_CHILD){
						mAsciiViewer = new AsciiViewFragment();
						transaction.add(R.id.child_fragment, mAsciiViewer).commit();
					}
					else if(position == SELECTED_TILTVIEW_CHILD){ // if child == tiltView
						mTiltViewer = new TiltViewFragment();
						transaction.add(R.id.child_fragment, mTiltViewer).commit();
					}
				} else { // one child alredy exists
//					if (position == SELECTED_LEFTRIGHT_CHILD){
//						if(mLRViewer == null){
//							mLRViewer = new LeftRightViewFragment();
//												}
//						transaction.replace(R.id.child_fragment, mLRViewer).commit();
//					}
					if(position == SELECTED_ASCIIDATA_CHILD){
						if(mAsciiViewer == null){
							mAsciiViewer = new AsciiViewFragment();
						}
						transaction.replace(R.id.child_fragment, mAsciiViewer).commit();
					}
					else if(position == SELECTED_TILTVIEW_CHILD){ // if
						if(mTiltViewer == null){
							mTiltViewer = new TiltViewFragment();
						}
						transaction.replace(R.id.child_fragment, mTiltViewer).commit();
					}
				}
				return true;
			}
		};
		actionbar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);


		return v;

	}

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.fragment_bt_option_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.start_stop_saving) {
			if(!mSavingSensorData){
				startSavingSensorsData();
			} else {
				stopSavingSensorsData();
				mBlinkTimer.cancel();
			}

		}
		return super.onOptionsItemSelected(item);
	}

//    class BlinkerTask extends TimerTask {
//		public void run() {
//			if(mSavingDataSeconds >= 5){
//				mSavingDataSeconds = 0;
//				cancel();
//				stopSavingSensorsData();
//			} else {
//				mSavingDataSeconds++;
//			}
//		}
//    }

	public void startSavingSensorsData() {

		      //mBlinkTimer.scheduleAtFixedRate(new BlinkerTask(),
                //     1000, BLINK_PERIOD);

		      mSavingStatus.setBackgroundColor(Color.RED);

			// Disable UI components so they cannot be changed while plotting
			// sensor data
//			for (int i = 0; i < mDelaySelector.getChildCount(); i++) {
//				mDelaySelector.getChildAt(i).setEnabled(false);
//			}
//			for (int i = 0; i < mContentSelector.getChildCount(); i++) {
//				mContentSelector.getChildAt(i).setEnabled(false);
//			}

			// Data files are stored on the external cache directory so they can
			// be pulled off of the device by the user
			File sensorsDataFile = filename();
			try {
				mPrintWriter = new PrintWriter(new BufferedWriter(
						new FileWriter(sensorsDataFile)));
				if(mSelectedSavingContent == mSensorData.SAVING_CONTENT_NATIVE){

					mPrintWriter.println(CSV_BASE_HEADER + " , native");
				}
				else if (mSelectedSavingContent == mSensorData.SAVING_CONTENT_ACCMAG){
					mPrintWriter.println(CSV_BASE_HEADER + " , acc/magn");
				}
				else if (mSelectedSavingContent == mSensorData.SAVING_CONTENT_GYRO){
					mPrintWriter.println(CSV_BASE_HEADER + " , gyro");
				}
				else if (mSelectedSavingContent == mSensorData.SAVING_CONTENT_FUSION){
					mPrintWriter.println(CSV_BASE_HEADER + " , fusion");
				}
			} catch (IOException e) {
				Log.e(TAG, "Could not open CSV file(s)", e);
			}

			mSensorData.startToSave(mPrintWriter);

			mSavingSensorData = true;

			Log.d(TAG, "Started reading sensors data");
			//TODO: add toast about saving
		}

	public void stopSavingSensorsData() {
		//if (mSavingSensorData) {
			mSensorData.stopSave();
			mSavingStatus.setBackgroundColor(Color.LTGRAY);
			// Re-enable sensor and options UI views
//			for (int i = 0; i < mDelaySelector.getChildCount(); i++) {
//				mDelaySelector.getChildAt(i).setEnabled(true);
//			}
//			for (int i = 0; i < mContentSelector.getChildCount(); i++) {
//				mContentSelector.getChildAt(i).setEnabled(true);
//			}

			mSavingSensorData = false;

			Log.d(TAG, "Stopped reading sensors data");
		}
	//}

	// ///////////////////////////////////////////////////
	// / File stuff
	// / from sensplore project, must be adapted
	private File mOutputFile = null;

	public static File filename() {
		File ext = Environment.getExternalStorageDirectory();
		ext = new File(ext, "saveFusion");
		if (!ext.exists()) {
			ext.mkdir();
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		return new File(ext, "fusionSave-" + df.format(new Date()) + ".csv");
	}

	public PrintStream getFile(boolean append) {
		PrintStream p = null;
		try {
			if (append) {
				p = new PrintStream(new FileOutputStream(mOutputFile, true));
			} else {
				mOutputFile = filename();
				// clean out all previous files
				for (File file : mOutputFile.getParentFile().listFiles()) {
					file.delete();
				}

				p = new PrintStream(new FileOutputStream(mOutputFile));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return p;
	}

	public Uri fileURI() {
		return Uri.fromFile(mOutputFile);
	}

	public void updateOrientationDisplay() {
		mLeftCmdView.setText(d.format(mSensorReceivedData[3]));
		mRightCmdView.setText(d.format(mSensorReceivedData[4]));
		// TODO: check which child fragment is active
//		if (mSelectedChildFragmentID == SELECTED_LEFTRIGHT_CHILD){
//			if(mLRViewer != null){
//				mLRViewer.updateDisplay(mSensorReceivedData);
//			}
//		}
		if (mSelectedChildFragmentID == SELECTED_ASCIIDATA_CHILD){
			if(mAsciiViewer != null){

				 mAsciiViewer.updateDisplay(mSensorReceivedData);
			}
		}
		else if (mSelectedChildFragmentID == SELECTED_TILTVIEW_CHILD){
			if(mTiltViewer != null){
				processTilt(mSensorReceivedData);
			}
		}
	}

	private Runnable updateOrientationDisplayTask = new Runnable() {
		public void run() {
			updateOrientationDisplay();
		}
	};
	private int mPWMMin;
	private float mAmplification;

    private void processTilt(float[] values) {
    	mTiltViewer.setTilt(values[6], values[1]);
    }

	@Override
	public void onStart() {
		super.onStart();
		if(D) Log.i(TAG, "onStart");
		restartSensors();
		if(mSensorData != null){
			mSensorData.setUpdateUi(true);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if(D) Log.i(TAG, "onResume");
		if(mSensorData != null){
			mSensorData.setUpdateUi(true);
		}
		updateUI();
	}

public void updateUI(){
	if(((MainActivity)getActivity()).getBTConnected()){
		mBTConnectStatus.setBackgroundColor(Color.GREEN);
		mBTConnectStatus.setText(R.string.connected_status_on);
		mToggleButtonMove.setEnabled(true);

	}else{
		mBTConnectStatus.setBackgroundColor(Color.LTGRAY);
		mBTConnectStatus.setText(R.string.connected_status_off);
		mToggleButtonMove.setEnabled(false);
	}
	if(mMovementEnabled == false){
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


	@Override
	public void onPause() {
		super.onPause();
		if(mSensorData != null){
			mSensorData.setUpdateUi(false);
		}
		setMovementEnabled(false);
		if(D) Log.i(TAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		if(mSensorData != null){
			mSensorData.setUpdateUi(false);
		}
		setMovementEnabled(false);
		stopSensors();
		if(D) Log.i(TAG, "onStop");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(D) Log.i(TAG, "onDestroy");
	}

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        mCallbacks = (Callbacks)activity;
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mCallbacks = null;
//    }
//
//    public interface Callbacks {
//        void onNewBTCommand(byte[] btMessage);
//    }

	public void stopSensors(){
		mSensorData.stopSensors();

	}

	public void restartSensors(){
		mSensorData.initListeners(mSelectedSensorDelay);

	}

	public void updateParams(){
		mSelectedSensorDelay = mArobotSettings.getPrefsSensorDelay();// = SensorManager.SENSOR_DELAY_FASTEST;
		mFilterFactor = mArobotSettings.getFilterFactor();
		mSelectedSavingContent = mArobotSettings.getPrefsSavingContent();
		mTimerPeriod = mArobotSettings.getPrefsTimerPeriod();
		mRollOffsset = mArobotSettings.getPrefsRollOffset();
		mPWMMin = mArobotSettings.getPrefsPwmMinimal();
		mAmplification = mArobotSettings.getPrefsAmplification();
		mSensorData.setFilterCoeff(mFilterFactor);
		mSensorData.setSelectedSavingContent(mSelectedSavingContent);
		mSensorData.setTimerPeriod(mTimerPeriod);
		mController.setRollOffset(mRollOffsset);
		mController.setPWMMin(mPWMMin);
		mController.setScaleCorrection(mAmplification);

	}

}
