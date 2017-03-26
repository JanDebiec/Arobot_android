package de.jandrotek.android.arobot.tab;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import de.jandrotek.android.arobot.core.MoveCmdCalculator;
import de.jandrotek.android.arobot.core.SensorCalc;

import static android.os.SystemClock.sleep;

/**
 * Created by PanJan on 30.11.2016.
 */

class SensorService
//        implements SensorEventListener
{
    // Debugging
    private static final String TAG = "SensorService";
    public static DecimalFormat d = new DecimalFormat("#0.0");
//    private final Handler mHandler;
    private SensorThread mSensorThread = null;
    private SensorCalc mSensorCalc = null;
    private SensorManager mSensorManager = null;
    private boolean mSensorRegistered = false;
    private boolean mInitState = true;

    private int selectedSensorDelay;
    private Sensor mAccelSensor;
    private Sensor mGyroSensor;
    private Sensor mMagnetSensor;
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
    private float[] mResult;
    private float[] xM;
    private float[] yM;
    private float[] zM;

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

    private long mStartTime;
    public int mTimerPeriod = ArobotDefines.TIME_CONSTANT;
    private long mTimestamp;

    private Timer mFuseTimer = null;
    private boolean mRunTimerTask = false;
    private long lastUpdate = System.currentTimeMillis();
    private long actualTime = System.currentTimeMillis();
    private static final long eUpdateUITime = 200;
    float[] data2Tx = new float[7];
    private boolean mUpdateUi = true;


    private int mState;

    public void setLoopActive(boolean mLoopActive) {
        this.mLoopActive = mLoopActive;
    }

    private boolean mLoopActive = false;
    private boolean mSensorsRegistered = false;
    private boolean mLoopAllowed = false;
    // Constants that indicate the current service state
    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_INITIALIZED = 1;     // now listening for incoming connections
    private static final int STATE_STARTED = 2;     // now listening for incoming connections
    private static final int STATE_REGISTERED = 3;     // now listening for incoming connections
    private static final int STATE_LOOP_RUNNING = 4;     // now listening for incoming connections

    private Handler mHandler;

    public void setFragment(SensorMovementFragment mFragment) {
        this.mFragment = mFragment;
    }

    private SensorMovementFragment mFragment;

    public void setCalculator(SensorCalc calculator) {
        this.mCalculator = calculator;
    }

    private SensorCalc mCalculator;

    SensorService(SensorManager sm) {
//    public SensorService(Context context, Handler handler, SensorManager sm) {
//        mHandler = handler;
        mHandler = new Handler();
//        mCalculator = new SensorCalc();
        mSensorManager = sm;
//        mBTMessCreator = new TxMessage();
        mLeftRightCmd = new float[2];
//        mBTMessage = new byte[TxMessage.BTMessageLenght];

//        mHandler = new Handler();
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
        mResult = new float[9];
        xM = new float[9];
        yM = new float[9];
        zM = new float[9];
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
            if(mSensorThread == null) {
                mSensorThread = new SensorThread();
                mLoopAllowed = true;
            }
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


    public synchronized void stopThread() {
        if (BuildConfig.DEBUG) Log.d(TAG, "stopThread");

        if (mSensorThread != null) {
            mLoopAllowed = false;
            sleep(100);
            mSensorThread.cancel();
            mSensorThread = null;
            setState(STATE_NONE);
        }
    }

    public synchronized void startThread(){
        if (BuildConfig.DEBUG) Log.d(TAG, "startThread(), state=" + mState);
        if(mState < STATE_INITIALIZED){
            if(mSensorThread == null) {
                mSensorThread = new SensorThread();
                mLoopAllowed = true;
            }
            setState(STATE_INITIALIZED);
        }
        if(mState < STATE_STARTED) {
            mSensorThread.start();
            setState(STATE_STARTED);
        }
        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then scedule the complementary filter task
        if (mFuseTimer == null) {
            mFuseTimer = new Timer();
            mFuseTimer.scheduleAtFixedRate(
                    new SensorService.calculateFusedOrientationTask(),
                    1000, mTimerPeriod);
            mRunTimerTask = true;
        }
    }

    public void setUpdateUi(boolean flag){

    }

    private class SensorThread extends HandlerThread
//    private class SensorThread extends Thread
            implements SensorEventListener
    {
        private boolean bNewData = false;
        private int mSensorType;
        private SensorEvent mNewEvent;
        private long mTimestampGyro;
        private long mTimestampMagnet;
        private long mTimestampAccel;


        public SensorThread(String name) {
            super(name);
//        public SensorThread(String name, int priority) {
            Log.d(TAG, "create SensorThread");
        }
        public SensorThread() {
            super("SensorThread");
//        public SensorThread(String name, int priority) {
            Log.d(TAG, "create SensorThread");
        }

        public void onSensorChanged(SensorEvent event) {
            mSensorType = event.sensor.getType();
            bNewData = true;
            mNewEvent = event;
            // workout in run function of thread
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLooperPrepared(){
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

        public void run() {
            if (BuildConfig.DEBUG) Log.d(TAG, "BEGIN mSensorThread");
            onLooperPrepared();
            while (mLoopAllowed) {
//            while (mLoopActive) {
                if(bNewData && mLoopActive){
                    // workout the new data
                    switch (mSensorType) {
                        case Sensor.TYPE_ACCELEROMETER:
                            // copy new accelerometer data into accel array
                            // then calculate new orientation
                            System.arraycopy(mNewEvent.values, 0, mAccel, 0, 3);
                            mTimestampAccel = mNewEvent.timestamp;
                            calculateAccMagOrientation();
                            break;

                        case Sensor.TYPE_GYROSCOPE:
                            // process gyro data
                            mTimestampGyro = mNewEvent.timestamp;
                            System.arraycopy(mNewEvent.values, 0, mGyro, 0, 3);
                            gyroFunction(mNewEvent);
                            break;

                        case Sensor.TYPE_MAGNETIC_FIELD:
                            // copy new magnetometer data into magnet array
                            mTimestampMagnet = mNewEvent.timestamp;
                            System.arraycopy(mNewEvent.values, 0, mMagnet, 0, 3);
                            break;
                    }
                    bNewData = false;
                }
                try {
                    Thread.sleep(5);
                } catch(Exception e){
                    e.getLocalizedMessage();
                }
            }

        }

        public void cancel() {
//            try {
////                mmSocket.close();
//            } catch (IOException e) {
//                Log.e(TAG, "close() of SensorThread failed", e);
//            }
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
                mGyroMatrix = matrixMultiplication(mGyroMatrix, initMatrix);
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
            mGyroMatrix = matrixMultiplication(mGyroMatrix, deltaMatrix);

            // get the gyroscope based orientation from the rotation matrix
            SensorManager.getOrientation(mGyroMatrix, mGyroOrientation);

        }


    }

//    // This function is borrowed from the Android reference
//    // at http://developer.android.com/reference/android/hardware/SensorEvent.html#values
//    // It calculates a rotation vector from the gyroscope angular speed values.
//    private void getRotationVectorFromGyro(float[] gyroValues,
//                                           float[] deltaRotationVector,
//                                           float timeFactor) {
//        float[] normValues = new float[3];
//
//        // Calculate the angular speed of the sample
//        float omegaMagnitude =
//                (float) Math.sqrt(gyroValues[0] * gyroValues[0] +
//                        gyroValues[1] * gyroValues[1] +
//                        gyroValues[2] * gyroValues[2]);
//
//        // Normalize the rotation vector if it's big enough to get the axis
//        if (omegaMagnitude > ArobotDefines.EPSILON) {
//            normValues[0] = gyroValues[0] / omegaMagnitude;
//            normValues[1] = gyroValues[1] / omegaMagnitude;
//            normValues[2] = gyroValues[2] / omegaMagnitude;
//        }
//
//        // Integrate around this axis with the angular speed by the timestep
//        // in order to get a delta rotation from this sample over the timestep
//        // We will convert this axis-angle representation of the delta rotation
//        // into a quaternion before turning it into the rotation matrix.
//        float thetaOverTwo = omegaMagnitude * timeFactor;
//        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
//        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
//        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
//        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
//        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
//        deltaRotationVector[3] = cosThetaOverTwo;
//    }

//    private float[] getRotationMatrixFromOrientation(float[] o) {
////        float[] xM = new float[9];
////        float[] yM = new float[9];
////        float[] zM = new float[9];
//
//        sinX = (float) Math.sin(o[1]);
//        cosX = (float) Math.cos(o[1]);
//        sinY = (float) Math.sin(o[2]);
//        cosY = (float) Math.cos(o[2]);
//        sinZ = (float) Math.sin(o[0]);
//        cosZ = (float) Math.cos(o[0]);
//
//        // rotation about x-axis (pitch)
//        xM[0] = 1.0f;
//        xM[1] = 0.0f;
//        xM[2] = 0.0f;
//        xM[3] = 0.0f;
//        xM[4] = cosX;
//        xM[5] = sinX;
//        xM[6] = 0.0f;
//        xM[7] = -sinX;
//        xM[8] = cosX;
//
//        // rotation about y-axis (roll)
//        yM[0] = cosY;
//        yM[1] = 0.0f;
//        yM[2] = sinY;
//        yM[3] = 0.0f;
//        yM[4] = 1.0f;
//        yM[5] = 0.0f;
//        yM[6] = -sinY;
//        yM[7] = 0.0f;
//        yM[8] = cosY;
//
//        // rotation about z-axis (azimuth)
//        zM[0] = cosZ;
//        zM[1] = sinZ;
//        zM[2] = 0.0f;
//        zM[3] = -sinZ;
//        zM[4] = cosZ;
//        zM[5] = 0.0f;
//        zM[6] = 0.0f;
//        zM[7] = 0.0f;
//        zM[8] = 1.0f;
//
//        // rotation order is y, x, z (roll, pitch, azimuth)
//        float[] resultMatrix = matrixMultiplication(xM, yM);
//        resultMatrix = matrixMultiplication(zM, resultMatrix);
//        return resultMatrix;
//    }

    private float[] matrixMultiplication(float[] A, float[] B) {
//        float[] mResult = new float[9];

        mResult[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        mResult[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        mResult[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        mResult[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        mResult[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        mResult[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        mResult[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        mResult[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        mResult[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return mResult;
    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            if (mRunTimerTask) {
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
                if (mGyroOrientation[0] < -0.5 * Math.PI
                        && mAccMagOrientation[0] > 0.0) {
                    mFusedOrientation[0] = (float) (mFilterCoeff
                            * (mGyroOrientation[0] + 2.0 * Math.PI) + mOneMinusCoeff
                            * mAccMagOrientation[0]);
                    mFusedOrientation[0] -= (mFusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
                            : 0;
                } else if (mAccMagOrientation[0] < -0.5 * Math.PI
                        && mGyroOrientation[0] > 0.0) {
                    mFusedOrientation[0] = (float) (mFilterCoeff
                            * mGyroOrientation[0] + mOneMinusCoeff
                            * (mAccMagOrientation[0] + 2.0 * Math.PI));
                    mFusedOrientation[0] -= (mFusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
                            : 0;
                } else {
                    mFusedOrientation[0] = mFilterCoeff * mGyroOrientation[0]
                            + mOneMinusCoeff * mAccMagOrientation[0];
                }

                // pitch
                if (mGyroOrientation[1] < -0.5 * Math.PI
                        && mAccMagOrientation[1] > 0.0) {
                    mFusedOrientation[1] = (float) (mFilterCoeff
                            * (mGyroOrientation[1] + 2.0 * Math.PI) + mOneMinusCoeff
                            * mAccMagOrientation[1]);
                    mFusedOrientation[1] -= (mFusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
                            : 0;
                } else if (mAccMagOrientation[1] < -0.5 * Math.PI
                        && mGyroOrientation[1] > 0.0) {
                    mFusedOrientation[1] = (float) (mFilterCoeff
                            * mGyroOrientation[1] + mOneMinusCoeff
                            * (mAccMagOrientation[1] + 2.0 * Math.PI));
                    mFusedOrientation[1] -= (mFusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
                            : 0;
                } else {
                    mFusedOrientation[1] = mFilterCoeff * mGyroOrientation[1]
                            + mOneMinusCoeff * mAccMagOrientation[1];
                }

                // roll
                if (mGyroOrientation[2] < -0.5 * Math.PI
                        && mAccMagOrientation[2] > 0.0) {
                    mFusedOrientation[2] = (float) (mFilterCoeff
                            * (mGyroOrientation[2] + 2.0 * Math.PI) + mOneMinusCoeff
                            * mAccMagOrientation[2]);
                    mFusedOrientation[2] -= (mFusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
                            : 0;
                } else if (mAccMagOrientation[2] < -0.5 * Math.PI
                        && mGyroOrientation[2] > 0.0) {
                    mFusedOrientation[2] = (float) (mFilterCoeff
                            * mGyroOrientation[2] + mOneMinusCoeff
                            * (mAccMagOrientation[2] + 2.0 * Math.PI));
                    mFusedOrientation[2] -= (mFusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
                            : 0;
                } else {
                    mFusedOrientation[2] = mFilterCoeff * mGyroOrientation[2]
                            + mOneMinusCoeff * mAccMagOrientation[2];
                }

                // overwrite gyro matrix and orientation with fused orientation
                // to comensate gyro drift
                mGyroMatrix = mCalculator.getRotationMatrixFromOrientation(mFusedOrientation);
                System.arraycopy(mFusedOrientation, 0, mGyroOrientation, 0, 3);

                mMoveCmd = mCalculator.calculateMovement(mFusedOrientation);
//
//                mLeftRightCmd[0] = mMoveCmd[3];
//                mLeftRightCmd[1] = mMoveCmd[4];
//                mBTMessage = mBTMessCreator.prepareTxMessage(mLeftRightCmd);
//
//                mCallbacks.onNewBTCommand(mBTMessage);

                // update sensor output in GUI
                // add timing results for UI-task
                actualTime = System.currentTimeMillis();
                if ((actualTime - lastUpdate > eUpdateUITime) && (mUpdateUi)) {
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

            data2Tx[0] = mAccMagOrientation[0];
            data2Tx[1] = mAccMagOrientation[1];
            data2Tx[2] = mAccMagOrientation[2];

            //all data is scaled in radians

            //update UI
            mFragment.mSensorReceivedData = data2Tx;
            mFragment.updateOrientationDisplay();
        }
    };

//    public float[] calculateMovement(float [] sensorData){
//        mPitch = (float)(sensorData[1] * 180 / Math.PI);
//        mRoll = (float)((sensorData[2] * 180 / Math.PI) + 90 - mRollOffset);
//
//        // subtract offset
//
//        // calculate
//        raw_left = (mRoll - mPitch );
//        raw_right = (mRoll + mPitch );
//
//        scaled_left = raw_left * mScaleCorrection;
//        scaled_right = raw_right * mScaleCorrection;
//
//        // BT scaling in BTMessage.java
//        //scaled_left += 128; // zero in the middle (0x80)
//        //scaled_right += 128;
//
//        if(scaled_left > 0){
//            torsionCorrectedLeft = scaled_left + mPWMMin;
//        } else {
//            torsionCorrectedLeft = scaled_left - mPWMMin;
//        }
//
//        if(scaled_right > 0){
//            torsionCorrectedRight = scaled_right + mPWMMin;
//        } else {
//            torsionCorrectedRight = scaled_right - mPWMMin;
//        }
//
//        mMovementCmd[0] = sensorData[0];
//        mMovementCmd[1] = sensorData[1];
//        mMovementCmd[2] = sensorData[2];
//        mMovementCmd[5] = mPitch;
//        mMovementCmd[6] = mRoll;
//        mMovementCmd[3] = torsionCorrectedLeft;
//        mMovementCmd[4] = torsionCorrectedRight;
//        return mMovementCmd;
//    }

}
