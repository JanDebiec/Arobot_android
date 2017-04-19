package de.jandrotek.android.arobot.core;

/**
 * Created by jan on 27.01.15.
 * part of sensor calculations without connection to Android framework.
 * can be tested with JUnit
 */
public class SensorCalc {

    private float mFilterCoeff = ArobotDefines.FILTER_COEFFICIENT;
    private float oneMinusCoeff = 1.0f - mFilterCoeff;
    // orientation angles from gyro matrix
    private float[] mGyroOrientation = new float[3];
    // orientation angles from accel and magnet
    private float[] mAccMagOrientation = new float[3];
    // final orientation angles from sensor fusion
    private float[] mFusedOrientation = new float[3];

    private float[] mMovementCmd = new float[8];// we need 7, but in PC 7???
    private float mPitch;
    private float mRoll;
    private float mRollOffset = 30;
    public void setRollOffset(float rollOffset) {
        mRollOffset = rollOffset;
    }

    private float mScaleCorrection = 1;
    public void setScaleCorrection(float scaleCorrection) {
        mScaleCorrection = scaleCorrection;
    }

    private float raw_left;
    private float raw_right;
    private float scaled_left;
    private float scaled_right;
    public void setPWMMin(float pWMMin) {
        mPWMMin = pWMMin;
    }

    public void setPWMMax(float pWMMax) {
        mPWMMax = pWMMax;
    }

    private float mPWMMin;
    private float mPWMMax;
    private float torsionCorrectedLeft;
    private float torsionCorrectedRight;
    private float[] mResult;

    private float[] xM ;
    private float[] yM ;
    private float[] zM ;
    private float[] normValues = new float[3];

    public SensorCalc() {
        xM = new float[9];
        yM = new float[9];
        zM = new float[9];
        normValues = new float[3];
        mResult = new float[9];

    }

    /**
     * left = pitch - roll
     * right = pitch + roll
     * pitch 90 deg left = 180, vertical = 0, right -180
     * roll 90 deg forward = 0, vertical = 180
     * @param sensorData input from sensors; azimuth, pitch, roll
     * @return left, right
     */
    public float[] calculateMovement(float [] sensorData){
        mPitch = (float)(sensorData[1] * 180 / Math.PI);
        mRoll = (float)((sensorData[2] * 180 / Math.PI) + 90 - mRollOffset);

        // subtract offset

        // calculate
        raw_left = (mRoll - mPitch );
        raw_right = (mRoll + mPitch );

        scaled_left = raw_left * mScaleCorrection;
        scaled_right = raw_right * mScaleCorrection;

        // BT scaling in BTMessage.java
        //scaled_left += 128; // zero in the middle (0x80)
        //scaled_right += 128;

        if(scaled_left > 0){
            torsionCorrectedLeft = scaled_left + mPWMMin;
        } else {
            torsionCorrectedLeft = scaled_left - mPWMMin;
        }

        if(scaled_right > 0){
            torsionCorrectedRight = scaled_right + mPWMMin;
        } else {
            torsionCorrectedRight = scaled_right - mPWMMin;
        }

        mMovementCmd[0] = sensorData[0];
        mMovementCmd[1] = sensorData[1];
        mMovementCmd[2] = sensorData[2];
        mMovementCmd[5] = mPitch;
        mMovementCmd[6] = mRoll;
        mMovementCmd[3] = torsionCorrectedLeft;
        mMovementCmd[4] = torsionCorrectedRight;
        return mMovementCmd;
    }


    public float[] getRotationMatrixFromOrientation(float[] o) {
//        float[] xM = new float[9];
//        float[] yM = new float[9];
//        float[] zM = new float[9];
//        mAccMagOrientation = o;

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

    public float[] matrixMultiplication(float[] A, float[] B) {
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


    public float calcFusedOrientation(float gyroOrientation, float accMagOrientation){
        float fusedOrientation = 0;
        // azimuth
        if (gyroOrientation < -0.5 * Math.PI
                && accMagOrientation > 0.0) {
            fusedOrientation = (float) (mFilterCoeff
                    * (gyroOrientation + 2.0 * Math.PI) + oneMinusCoeff
                    * accMagOrientation);
            fusedOrientation -= (fusedOrientation > Math.PI) ? 2.0 * Math.PI
                    : 0;
        } else if (accMagOrientation < -0.5 * Math.PI
                && gyroOrientation > 0.0) {
            fusedOrientation = (float) (mFilterCoeff
                    * gyroOrientation + oneMinusCoeff
                    * (accMagOrientation + 2.0 * Math.PI));
            fusedOrientation -= (fusedOrientation > Math.PI) ? 2.0 * Math.PI
                    : 0;
        } else {
            fusedOrientation = mFilterCoeff * gyroOrientation
                    + oneMinusCoeff * accMagOrientation;
        }
        return fusedOrientation;

    }


    // This function is borrowed from the Android reference
    // at http://developer.android.com/reference/android/hardware/SensorEvent.html#values
    // It calculates a rotation vector from the gyroscope angular speed values.
    public void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor)
    {
//        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > ArobotDefines.EPSILON) {
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

}
