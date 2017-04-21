package de.jandrotek.android.arobot.core;

// class not used in tabapp
public class MoveCmdCalculator {
//	private float[] mMovementDisplayValues = new float[8];
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


}
