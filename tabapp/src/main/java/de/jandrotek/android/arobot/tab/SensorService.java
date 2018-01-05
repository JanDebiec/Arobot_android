package de.jandrotek.android.arobot.tab;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import de.jandrotek.android.arobot.core.ArobotDefines;
import de.jandrotek.android.arobot.core.SensorCalc;

/**
 * Created by PanJan on 30.11.2016.
 */

class SensorService
        implements SensorEventListener
{
    // Debugging
    private static final String TAG = "SensorService";
    private SensorManager mSensorManager = null;
    private MovementActivity mMotherActivity;
    private boolean mInitState = true;
    private boolean mSendZero = false;

    private int selectedSensorDelay;
    private Sensor mAccelSensor;
    private Sensor mGyroSensor;
    private Sensor mMagnetSensor;

    private int mCounterSensorAcc;
    private int mCounterSensorGyro;
    private int mCounterSensorMagnet;
    private int mCounterSensorGlobal;
    private int mCounterSensorFusion;
    private int mCounterUiUpdate;
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
//    private float[] mResult;
    private float[] xM;
    private float[] yM;
    private float[] zM;

    public float[] getRotationMatrix() {
        return mRotationMatrix;
    }

    private float sinX;
    private float cosX ;
    private float sinY;
    private float cosY;
    private float sinZ;
    private float cosZ;
    private float[] mLeftRightCmd;
    private byte[] mBTMessage;
    private float mOneMinusCoeff;
    private float mFilterCoeff = ArobotDefines.FILTER_COEFFICIENT;
    private float[] mMoveCmd;
    private float[] mMoveNormCmd = new float[2];
    private float[] mMoveHelpCmd = new float[2];

    private long mStartTime;
    public int mTimerPeriod = ArobotDefines.TIME_CONSTANT;
    private long mTimestamp;

//    public void setRunFuseTask(boolean runFuseTask) {
//        this.mRunFuseTask = runFuseTask;
//    }

    private boolean mRunFuseTask = false;

    public void setTransferAllowed(boolean transferAllowed) {
        mTransferAllowed = transferAllowed;
    }

    private boolean mTransferAllowed = false;

    private long lastUpdate = System.currentTimeMillis();
    private long actualTime = System.currentTimeMillis();
    private static final long eUpdateUITime = 200;
    private static final long eCalculateFuseTime = 100;
    float[] data2Tx = new float[7];
    private boolean mUpdateUi = true;

    private boolean mSensorsRegistered = false;
    // Constants that indicate the current service state
    private int mState;
    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_INITIALIZED = 1;     // now listening for incoming connections
    private static final int STATE_STARTED = 2;     // now listening for incoming connections
    private static final int STATE_REGISTERED = 3;     // now listening for incoming connections
    private static final int STATE_LOOP_RUNNING = 4;     // now listening for incoming connections

    private Handler mHandler;
    private Handler mFuseHandler;

    private int mSensorType;
    private long mTimestampGyro;
    private long mTimestampMagnet;
    private long mTimestampAccel;

    public void setFragment(SensorMovementFragment mFragment) {
        this.mFragment = mFragment;
    }

    private SensorMovementFragment mFragment;

    public void setCalculator(SensorCalc calculator) {
        this.mCalculator = calculator;
    }

    private SensorCalc mCalculator;

    SensorService(MovementActivity activity, SensorManager sm) {
        mMotherActivity = activity;
        mHandler = new Handler();
        mSensorManager = sm;
        mLeftRightCmd = new float[2];
//        mBTMessage = new byte[TxBTMessage.BTMessageLenght];

        mGyroOrientation[0] = 0.0f;
        mGyroOrientation[1] = 0.0f;
        mGyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        mGyroMatrix[0] = 1.0f;
        mGyroMatrix[1] = 0.0f;
        mGyroMatrix[2] = 0.0f;
        mGyroMatrix[3] = 0.0f;
        mGyroMatrix[4] = 1.0f;
        mGyroMatrix[5] = 0.0f;
        mGyroMatrix[6] = 0.0f;
        mGyroMatrix[7] = 0.0f;
        mGyroMatrix[8] = 1.0f;

//        d.setRoundingMode(RoundingMode.HALF_UP);
//        d.setMaximumFractionDigits(3);
//        d.setMinimumFractionDigits(3);

        mOneMinusCoeff = 1.0f - mFilterCoeff;
        xM = new float[9];
        yM = new float[9];
        zM = new float[9];

        mCounterSensorAcc = 0;
        mCounterSensorGyro = 0;
        mCounterSensorMagnet = 0;
        mCounterSensorGlobal = 0;
        mCounterSensorFusion = 0;
        mCounterUiUpdate = 0;

        mFuseHandler = new Handler();

        initListeners(SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void startFuseCalc() {
        mRunFuseTask = true;
        registerSensors();
        if(mRunFuseTask){
            mFuseHandler.postDelayed(calculateFusedOrientationTask, eCalculateFuseTime);
        }
    }

    public void stopFuseCalc(){
        mRunFuseTask = false;
        unregisterSensors();

    }

    private  void logCounters(){
//        if (BuildConfig.DEBUG) {
//            Log.d(TAG,
//                    " SAcc " + mCounterSensorAcc
//                            + " SGyro " + mCounterSensorGyro
//                            + " SMagnet " + mCounterSensorMagnet
//                            + " SGlobal " + mCounterSensorGlobal
//                            + " Fusion " + mCounterSensorFusion
//                            + " Ui " + mCounterUiUpdate);
//        }
        mCounterSensorAcc = 0;
        mCounterSensorGyro = 0;
        mCounterSensorMagnet = 0;
        mCounterSensorGlobal = 0;
        mCounterSensorFusion = 0;
        mCounterUiUpdate = 0;
    }

    void initListeners(int selectedSensorDelay) {
        if (BuildConfig.DEBUG) Log.d(TAG, "initListeners");
        this.selectedSensorDelay = selectedSensorDelay;
        if(!mSensorsRegistered){
            mAccelSensor = (Sensor) mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mGyroSensor = (Sensor) mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mMagnetSensor = (Sensor) mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mStartTime = SystemClock.uptimeMillis();
            mSensorsRegistered = true;
        }
        if(mState < STATE_INITIALIZED) {
            setState(STATE_INITIALIZED);
        }
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (BuildConfig.DEBUG) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
//        mHandler.obtainMessage(1, state, -1).sendToTarget();
    }



    public void setUpdateUi(boolean flag){

    }

    public void registerSensors(){
        if(mState < STATE_REGISTERED) {
            mSensorManager.registerListener(this, mAccelSensor,
                    selectedSensorDelay);
            mSensorManager.registerListener(this, mGyroSensor,
                    selectedSensorDelay);
            mSensorManager.registerListener(this, mMagnetSensor,
                    selectedSensorDelay);
            setState(STATE_REGISTERED);
        }
    }

    public void unregisterSensors(){
        mMotherActivity.handleVelCmd(0,0);
//        mRunFuseTask = false;
        mSensorManager.unregisterListener(this);
        setState(STATE_STARTED);
    }

    public void onSensorChanged(SensorEvent event) {
        mSensorType = event.sensor.getType();
        switch (mSensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array
                // then calculate new orientation
                mCounterSensorAcc ++;
                System.arraycopy(event.values, 0, mAccel, 0, 3);
                mTimestampAccel = event.timestamp;
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                mCounterSensorGyro++;
                // process gyro data
                mTimestampGyro = event.timestamp;
                System.arraycopy(event.values, 0, mGyro, 0, 3);
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                mCounterSensorMagnet++;
                // copy new magnetometer data into magnet array
                mTimestampMagnet = event.timestamp;
                System.arraycopy(event.values, 0, mMagnet, 0, 3);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    private void calculateAccMagOrientation() {
        if (SensorManager.getRotationMatrix(mRotationMatrix, null, mAccel, mMagnet)) {
            SensorManager.getOrientation(mRotationMatrix, mAccMagOrientation);
            //mAccMagOrientation is in radians
        }
    }

    // This function performs the integration of the gyroscope data.
    // It writes the gyroscope based orientation into gyroOrientation.
    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (mAccMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if (mInitState) {
            float[] initMatrix = new float[9];
            initMatrix = mCalculator.getRotationMatrixFromOrientation(mAccMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            mGyroMatrix = mCalculator.matrixMultiplication(mGyroMatrix, initMatrix);
            mInitState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (mTimestamp != 0) {
            final float dT = (event.timestamp - mTimestamp) * ArobotDefines.NS2S;
            System.arraycopy(event.values, 0, mGyro, 0, 3);
            mCalculator.getRotationVectorFromGyro(mGyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        mTimestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        mGyroMatrix = mCalculator.matrixMultiplication(mGyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(mGyroMatrix, mGyroOrientation);

    }

    private Runnable calculateFusedOrientationTask = new Runnable() {
        @Override
        public void run() {
            logCounters();
            if(mTransferAllowed) {
                if (mRunFuseTask) {
                    mCounterSensorFusion++;
//                float mOneMinusCoeff = 1.0f - mFilterCoeff;

				/*
                 * Fix for 179� <--> -179� transition problem: Check whether one of
				 * the two orientation angles (gyro or accMag) is negative while the
				 * other one is positive. If so, add 360� (2 * math.PI) to the
				 * negative value, perform the sensor fusion, and remove the 360�
				 * from the mResult if it is greater than 180�. This stabilizes the
				 * output in positive-to-negative-transition cases.
				 */

                    // azimuth
                    mFusedOrientation[0] = mCalculator.calcFusedOrientation(mGyroOrientation[0], mAccMagOrientation[0]);

                    // pitch
                    mFusedOrientation[1] = mCalculator.calcFusedOrientation(mGyroOrientation[1], mAccMagOrientation[1]);

                    // roll
                    mFusedOrientation[2] = mCalculator.calcFusedOrientation(mGyroOrientation[2], mAccMagOrientation[2]);

                    // overwrite gyro matrix and orientation with fused orientation
                    // to comensate gyro drift
                    mGyroMatrix = mCalculator.getRotationMatrixFromOrientation(mFusedOrientation);
                    System.arraycopy(mFusedOrientation, 0, mGyroOrientation, 0, 3);

                    mMoveCmd = mCalculator.calculateMovement(mFusedOrientation);

                    // update sensor output in GUI
                    // add timing results for UI-task
                    actualTime = System.currentTimeMillis();
//                mHandler.post(updateOreintationDisplayTask);
                } else {
                    mMoveCmd[3] = 0;
                    mMoveCmd[4] = 0;
                    mFuseHandler.postDelayed(calculateFusedOrientationTask, eCalculateFuseTime);
                }
                mHandler.post(updateOreintationDisplayTask);
                if (mRunFuseTask) {
                    mFuseHandler.postDelayed(calculateFusedOrientationTask, eCalculateFuseTime);
                }
            }
        }
    };

    private Runnable updateOreintationDisplayTask = new Runnable() {
        public void run() {
            mCounterUiUpdate++;
            //float[] data2Tx = new float[7];// 0,1,2 normal, 3,4 left right
            data2Tx[3] = mMoveCmd[3];
            data2Tx[4] = mMoveCmd[4];
            data2Tx[5] = mMoveCmd[5];
            data2Tx[6] = mMoveCmd[6];

            data2Tx[0] = mAccMagOrientation[0];
            data2Tx[1] = mAccMagOrientation[1];
            data2Tx[2] = mAccMagOrientation[2];

            //all data is scaled in radians

            //update UI, tx vel-cmd
            mMoveHelpCmd[0] = mMoveCmd[3]/2;
            mMoveHelpCmd[1] = mMoveCmd[4]/2;
            mMoveNormCmd[0] = (mMoveHelpCmd[0]) * (mMoveHelpCmd[0]);
            if(mMoveHelpCmd[0] < 0)
                mMoveNormCmd[0] = -mMoveNormCmd[0];
            mMoveNormCmd[1] = (mMoveHelpCmd[1]) * (mMoveHelpCmd[1]);
            if(mMoveHelpCmd[1] < 0)
                mMoveNormCmd[1] = -mMoveNormCmd[1];
            mFragment.mSensorReceivedData = data2Tx;
            mFragment.updateOrientationDisplay(); // val[5], [6] are used
            mMotherActivity.handleVelCmd(mMoveNormCmd[0], mMoveNormCmd[1]);
//            mMotherActivity.handleVelCmd(mMoveCmd[3], mMoveCmd[4]);
        }
    };
}
