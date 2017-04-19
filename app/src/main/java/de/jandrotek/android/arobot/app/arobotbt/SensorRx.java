
package de.jandrotek.android.arobot.app.arobotbt;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import de.jandrotek.android.arobot.core.MoveCmdCalculator;
import de.jandrotek.android.arobot.core.TxBTMessage;

//TODO: implement callback for results to send to BT
public class SensorRx implements SensorEventListener {
    private static final String TAG = "SensorRxClass";
    public static final int SAVING_CONTENT_NATIVE = 0;
    public static final int SAVING_CONTENT_ACCMAG = 1;
    public static final int SAVING_CONTENT_GYRO = 2;
    public static final int SAVING_CONTENT_FUSION = 3;
    private static final float NS2S = 1.0f / 1000000000.0f;
    public static final float EPSILON = 0.000000001f;
    private static final char CSV_DELIM = ',';
    private long lastUpdate = System.currentTimeMillis();
    private long actualTime = System.currentTimeMillis();
	private static final long eUpdateUITime = 200;
	float[] data2Tx = new float[7];
	private boolean mUpdateUi = true;

	private SensorManager mSensorManager = null;
    Callbacks mCallbacks;

    private long mTimestampGyro;
    private long mTimestampMagnet;
    private long mTimestampAccel;
    public Handler mHandler;
	private SensorMovementFragment mFragment;
	private MainActivity mActivity;
	private int mRadioSelection = 0;
    private int mSelectedSavingContent;
    private MoveCmdCalculator mController;
	private TxBTMessage mBTMessCreator;
	private float[] mLeftRightCmd;
    private byte[] mBTMessage;

	// angular speeds from gyro
    private float[] mGyro = new float[3];

    // rotation matrix from gyro data
    private float[] mGyroMatrix = new float[9];

    // orientation angles from gyro matrix
    private float[] mGyroOrientation = new float[3];

    // magnetic field vector
    private float[] mMagnet = new float[3];

    // accelerometer vector
    private float[] mAccel = new float[3];

    // orientation angles from accel and magnet
    private float[] mAccMagOrientation = new float[3];

    // final orientation angles from sensor fusion
    private float[] mFusedOrientation = new float[3];

    // accelerometer and magnetometer based rotation matrix
    private float[] mRotationMatrix = new float[9];
	private boolean mToSave;
	private Sensor mAccelSensor;
	private Sensor mGyroSensor;
	private Sensor mMagnetSensor;
	private PrintWriter mPrintWriter;
	private long mStartTime;
	public static DecimalFormat d = new DecimalFormat("#0.0");
	private boolean mSaveModeProcessed;
	private long mTimestamp;
	private boolean mInitState;
	public static final float FILTER_COEFFICIENT = 0.98f;
	private float mFilterCoeff = FILTER_COEFFICIENT;
	public static final int TIME_CONSTANT = 100;//30;
	public int mTimerPeriod = TIME_CONSTANT;

	private Timer mFuseTimer;
	public static int mFuseTimeConstant = TIME_CONSTANT;
	private 			float[] mMoveCmd;
	private boolean mRunTimerTask;

	private SensorRx(Activity activity){
		mActivity = (MainActivity) activity;
        mCallbacks = (Callbacks)mActivity;
		mBTMessCreator = new TxBTMessage();
		mLeftRightCmd = new float[2];
		mBTMessage = new byte[TxBTMessage.BTMessageLenght];
		mFuseTimer = new Timer();

        mHandler = new Handler();
        mGyroOrientation[0] = 0.0f;
        mGyroOrientation[1] = 0.0f;
        mGyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        mGyroMatrix[0] = 1.0f; mGyroMatrix[1] = 0.0f; mGyroMatrix[2] = 0.0f;
        mGyroMatrix[3] = 0.0f; mGyroMatrix[4] = 1.0f; mGyroMatrix[5] = 0.0f;
        mGyroMatrix[6] = 0.0f; mGyroMatrix[7] = 0.0f; mGyroMatrix[8] = 1.0f;

        d.setRoundingMode(RoundingMode.HALF_UP);
        d.setMaximumFractionDigits(3);
        d.setMinimumFractionDigits(3);
	}

	/**
	 * a methode to instance one and only instance of class as singleton
	 * @param activity
	 * @return
	 */
	public static final SensorRx INSTANCE(Activity activity){
		return new SensorRx(activity);
	}

	public void connectParentFragment(SensorMovementFragment fragment) {
		mFragment = fragment;
		mController = mFragment.mController;
	}

    public int getSelectedSavingContent() {
		return mSelectedSavingContent;
	}

	public void setSelectedSavingContent(int selectedSavingContent) {
		mSelectedSavingContent = selectedSavingContent;
	}

	public float getFilterCoeff() {
		return mFilterCoeff;
	}

	public void setFilterCoeff(float filterCoeff) {
		mFilterCoeff = filterCoeff;
	}

	public void setTimerPeriod(int timerPeriod) {
		mTimerPeriod = timerPeriod;
	}

	public boolean isToSave() {
		return mToSave;
	}

    public boolean isUpdateUi() {
		return mUpdateUi;
	}

	public void setUpdateUi(boolean updateUi) {
		mUpdateUi = updateUi;
	}

	public void startToSave(PrintWriter printWriter) {
		mToSave = true;
		mPrintWriter = printWriter;
        mStartTime = SystemClock.uptimeMillis();
	}

	public void stopSave(){
		mToSave = false;
	}


	public void init(SensorManager sm) {

		mSensorManager = sm;
	}


	public void initListeners( int selectedSensorDelay){

		mAccelSensor = (Sensor)mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    mSensorManager.registerListener(this, mAccelSensor,
	    		selectedSensorDelay);

		mGyroSensor = (Sensor)mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	    mSensorManager.registerListener(this,mGyroSensor,
	    		selectedSensorDelay);

		mMagnetSensor = (Sensor)mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    mSensorManager.registerListener(this,mMagnetSensor,
	    		selectedSensorDelay);
       mStartTime = SystemClock.uptimeMillis();

       mRunTimerTask = true;
		// wait for one second until gyroscope and magnetometer/accelerometer
       // data is initialised then scedule the complementary filter task
      mFuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                                    1000, mTimerPeriod);

	}

	public void stopSensors(){
		mSensorManager.unregisterListener(this);
		mRunTimerTask = false;
	}

	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		if(mSelectedSavingContent == SAVING_CONTENT_NATIVE){
	        if(mToSave){

	        // Write to data file
	        writeSensorEvent(
	        		sensorType,
	        		event.timestamp,
	        		event.values[0],
	        		event.values[1],
	        		event.values[2]
	                         );
	        }
		}
	    switch(sensorType) {
	    case Sensor.TYPE_ACCELEROMETER:
	        // copy new accelerometer data into accel array
	        // then calculate new orientation
	        System.arraycopy(event.values, 0, mAccel, 0, 3);
		    mTimestampAccel = event.timestamp;
	        calculateAccMagOrientation();
	        break;

	    case Sensor.TYPE_GYROSCOPE:
	        // process gyro data
		    mTimestampGyro = event.timestamp;
		    System.arraycopy(event.values, 0, mGyro, 0, 3);
	        gyroFunction(event);
	        break;

	    case Sensor.TYPE_MAGNETIC_FIELD:
	        // copy new magnetometer data into magnet array
		    mTimestampMagnet = event.timestamp;
	        System.arraycopy(event.values, 0, mMagnet, 0, 3);
	        break;
	    }
	}

    public boolean isSaveModeProcessed() {
		return mSaveModeProcessed;
	}

	public void setSaveModeProcessed(boolean saveModeProcessed) {
		mSaveModeProcessed = saveModeProcessed;
	}

	private void writeSensorEvent(
		int sensorType,
		long eventTime,
        float x,
        float y,
        float z
        )
    {
        if (mPrintWriter != null)
        {
            StringBuffer sb = new StringBuffer()
                .append(sensorType).append(CSV_DELIM)
                .append((eventTime / 1000000) - mStartTime).append(CSV_DELIM)
                .append(x).append(CSV_DELIM)
                .append(y).append(CSV_DELIM)
                .append(z).append(CSV_DELIM)
                ;

            mPrintWriter.println(sb.toString());
            if (mPrintWriter.checkError())
            {
                Log.w(TAG, "Error writing sensor event data");
            }
        }
    }

	public void calculateAccMagOrientation() {
	    if(SensorManager.getRotationMatrix(mRotationMatrix, null, mAccel, mMagnet)) {
	        SensorManager.getOrientation(mRotationMatrix, mAccMagOrientation);
	        //mAccMagOrientation is in radians
			if(mSelectedSavingContent == SAVING_CONTENT_ACCMAG){
		        if(mToSave){
		        	// Write to data file
		        	writeSensorEvent(
		        		SAVING_CONTENT_ACCMAG,
		        		mTimestampAccel,
		        		mAccMagOrientation[0],
		        		mAccMagOrientation[1],
		        		mAccMagOrientation[2]
		        	);
		        }
			}
	    }
	}

	// This function is borrowed from the Android reference
	// at http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	// It calculates a rotation vector from the gyroscope angular speed values.
    private void getRotationVectorFromGyro(float[] gyroValues,
            float[] deltaRotationVector,
            float timeFactor)
	{
		float[] normValues = new float[3];

		// Calculate the angular speed of the sample
		float omegaMagnitude =
		(float)Math.sqrt(gyroValues[0] * gyroValues[0] +
		gyroValues[1] * gyroValues[1] +
		gyroValues[2] * gyroValues[2]);

		// Normalize the rotation vector if it's big enough to get the axis
		if(omegaMagnitude > EPSILON) {
		normValues[0] = gyroValues[0] / omegaMagnitude;
		normValues[1] = gyroValues[1] / omegaMagnitude;
		normValues[2] = gyroValues[2] / omegaMagnitude;
		}

		// Integrate around this axis with the angular speed by the timestep
		// in order to get a delta rotation from this sample over the timestep
		// We will convert this axis-angle representation of the delta rotation
		// into a quaternion before turning it into the rotation matrix.
		float thetaOverTwo = omegaMagnitude * timeFactor;
		float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
		float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
		deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
		deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
		deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
		deltaRotationVector[3] = cosThetaOverTwo;
	}

    // This function performs the integration of the gyroscope data.
    // It writes the gyroscope based orientation into gyroOrientation.
    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (mAccMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(mInitState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(mAccMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            mGyroMatrix = matrixMultiplication(mGyroMatrix, initMatrix);
            mInitState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(mTimestamp != 0) {
            final float dT = (event.timestamp - mTimestamp) * NS2S;
        System.arraycopy(event.values, 0, mGyro, 0, 3);
        getRotationVectorFromGyro(mGyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        mTimestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        mGyroMatrix = matrixMultiplication(mGyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(mGyroMatrix, mGyroOrientation);

		if(mSelectedSavingContent == SAVING_CONTENT_GYRO){
	        if(mToSave){
	        	// Write to data file
	        	writeSensorEvent(
	        		SAVING_CONTENT_GYRO,
	        		mTimestamp,
	        		mGyroOrientation[0],
	        		mGyroOrientation[1],
	        		mGyroOrientation[2]
	        	);
	        }
		}

    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

	private float[] matrixMultiplication(float[] A, float[] B) {
	    float[] result = new float[9];

	    result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
	    result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
	    result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

	    result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
	    result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
	    result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

	    result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
	    result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
	    result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

	    return result;
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	class calculateFusedOrientationTask extends TimerTask {
		public void run() {
			if(mRunTimerTask){
				float oneMinusCoeff = 1.0f - mFilterCoeff;

				/*
				 * Fix for 179� <--> -179� transition problem: Check whether one of
				 * the two orientation angles (gyro or accMag) is negative while the
				 * other one is positive. If so, add 360� (2 * math.PI) to the
				 * negative value, perform the sensor fusion, and remove the 360�
				 * from the result if it is greater than 180�. This stabilizes the
				 * output in positive-to-negative-transition cases.
				 */

				// azimuth
				if (mGyroOrientation[0] < -0.5 * Math.PI
						&& mAccMagOrientation[0] > 0.0) {
					mFusedOrientation[0] = (float) (mFilterCoeff
							* (mGyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff
							* mAccMagOrientation[0]);
					mFusedOrientation[0] -= (mFusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
							: 0;
				} else if (mAccMagOrientation[0] < -0.5 * Math.PI
						&& mGyroOrientation[0] > 0.0) {
					mFusedOrientation[0] = (float) (mFilterCoeff
							* mGyroOrientation[0] + oneMinusCoeff
							* (mAccMagOrientation[0] + 2.0 * Math.PI));
					mFusedOrientation[0] -= (mFusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
							: 0;
				} else {
					mFusedOrientation[0] = mFilterCoeff * mGyroOrientation[0]
							+ oneMinusCoeff * mAccMagOrientation[0];
				}

				// pitch
				if (mGyroOrientation[1] < -0.5 * Math.PI
						&& mAccMagOrientation[1] > 0.0) {
					mFusedOrientation[1] = (float) (mFilterCoeff
							* (mGyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff
							* mAccMagOrientation[1]);
					mFusedOrientation[1] -= (mFusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
							: 0;
				} else if (mAccMagOrientation[1] < -0.5 * Math.PI
						&& mGyroOrientation[1] > 0.0) {
					mFusedOrientation[1] = (float) (mFilterCoeff
							* mGyroOrientation[1] + oneMinusCoeff
							* (mAccMagOrientation[1] + 2.0 * Math.PI));
					mFusedOrientation[1] -= (mFusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
							: 0;
				} else {
					mFusedOrientation[1] = mFilterCoeff * mGyroOrientation[1]
							+ oneMinusCoeff * mAccMagOrientation[1];
				}

				// roll
				if (mGyroOrientation[2] < -0.5 * Math.PI
						&& mAccMagOrientation[2] > 0.0) {
					mFusedOrientation[2] = (float) (mFilterCoeff
							* (mGyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff
							* mAccMagOrientation[2]);
					mFusedOrientation[2] -= (mFusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
							: 0;
				} else if (mAccMagOrientation[2] < -0.5 * Math.PI
						&& mGyroOrientation[2] > 0.0) {
					mFusedOrientation[2] = (float) (mFilterCoeff
							* mGyroOrientation[2] + oneMinusCoeff
							* (mAccMagOrientation[2] + 2.0 * Math.PI));
					mFusedOrientation[2] -= (mFusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
							: 0;
				} else {
					mFusedOrientation[2] = mFilterCoeff * mGyroOrientation[2]
							+ oneMinusCoeff * mAccMagOrientation[2];
				}

				// overwrite gyro matrix and orientation with fused orientation
				// to comensate gyro drift
				mGyroMatrix = getRotationMatrixFromOrientation(mFusedOrientation);
				System.arraycopy(mFusedOrientation, 0, mGyroOrientation, 0, 3);
				if(mSelectedSavingContent == SAVING_CONTENT_FUSION){
			        if(mToSave){
			        	// Write to data file
			        	writeSensorEvent(
			        		SAVING_CONTENT_FUSION,
			        		mTimestampAccel,
			        		mFusedOrientation[0],
			        		mFusedOrientation[1],
			        		mFusedOrientation[2]
			        	);
			        }
				}
				mMoveCmd = mController.calculateMovement(mFusedOrientation);

				mLeftRightCmd[0] = mMoveCmd[3];
				mLeftRightCmd[1] = mMoveCmd[4];
				mBTMessage = mBTMessCreator.prepareTxMessage(mLeftRightCmd);

				mCallbacks.onNewBTCommand(mBTMessage);

				// update sensor output in GUI
				// add timing results for UI-task
				actualTime = System.currentTimeMillis();
				if((actualTime - lastUpdate > eUpdateUITime) &&  (mUpdateUi)){
					lastUpdate = actualTime;
					mHandler.post(updateOreintationDisplayTask);
				}
			} else {
				cancel();
			}
		}
	}

    private Runnable updateOreintationDisplayTask = new Runnable() {
		public void run() {
			//float[] data2Tx = new float[7];// 0,1,2 normal, 3,4 left right
    		data2Tx[3] = mMoveCmd[3];
    		data2Tx[4] = mMoveCmd[4];
    		data2Tx[5] = mMoveCmd[5];
    		data2Tx[6] = mMoveCmd[6];
	    	switch(mFragment.mSelectedDisplayContent) {
	    	case SAVING_CONTENT_NATIVE:
	    	case SAVING_CONTENT_ACCMAG:
	    		data2Tx[0] = mAccMagOrientation[0];
	    		data2Tx[1] = mAccMagOrientation[1];
	    		data2Tx[2] = mAccMagOrientation[2];

	    		break;
	    	case SAVING_CONTENT_GYRO:
	    		data2Tx[0] = mGyroOrientation[0];
	    		data2Tx[1] = mGyroOrientation[1];
	    		data2Tx[2] = mGyroOrientation[2];
	    		break;
	    	case SAVING_CONTENT_FUSION:
	    		data2Tx[0] = mFusedOrientation[0];
	    		data2Tx[1] = mFusedOrientation[1];
	    		data2Tx[2] = mFusedOrientation[2];
	    		break;
	    	}
    		//all data is scaled in radians

	    	//update UI
	    	mFragment.mSensorReceivedData = data2Tx;
			mFragment.updateOrientationDisplay();

		}
	};

	public interface Callbacks {
		void onNewBTCommand(byte[] btMessage);
	}
}
