package de.jandrotek.android.arobot.app.arobotbt;

import android.content.Context;
import android.content.SharedPreferences;

import de.jandrotek.android.arobot.core.TxBTMessage;

/**
 * This class is responsible for loading the last used settings for
 * the application.
 * Storing is managed in PrefsActivity
 * Every item has 3 defines: mPREFS_KEY, mDefaultValueString, mPrefsValue
 * Strings are defined in file: prefs_default.xml
 */
public class ArobotSettings {
	private static String mPREFS_KEY_MOTOR_TYPE;
	private static String mPREFS_KEY_SENSOR_DELAY;
	private static String mPREFS_KEY_FILTER_FACTOR;
	private static String mPREFS_KEY_SAVING_CONTENT;
	private static String mPREFS_KEY_FUSION_CONTENT;
	private static String mPREFS_KEY_FUSION_TIMER_PERIOD;
	private static String mPREFS_KEY_PWM_MINIMAL;
	private static String mPREFS_KEY_PWM_MAXIMAL;
	private static String mPREFS_KEY_ROLL_OFFSET;
	private static String mPREFS_KEY_AMPLIFICATION;

	private int mPrefsMotorType = TxBTMessage.MOTOR_TYPE_SIGN_AND_PWM;
	//	private int mPrefsMotorType = MotionTxMessage.MOTOR_TYPE_SIGN_AND_PWM;
	private int mPrefsSensorDelay;
	private int mPrefsTimerPeriod;
	private int mPrefsSavingContent;
	private int mPrefsFusionContent;
	private float mPrefsFilterFactor;
	private int mPrefsRollOffset;
	private int mPrefsPwmMinimal;
	private int mPrefsPwmMaximal;
	private float mPrefsAmplification;

	private String mDefaultFilterCoefficient;
	private String mDefaultPwmMinimal;
	private String mDefaultPwmMaximal;
	private String mDefaultMotorType;
	private String mDefaultSensorDelay;
	private String mDefaultTimerPeriod;
	private String mDefaultSavingContent;
	private String mDefaultFusionContent;
	private String mDefaultRollOffset;
	private String mDefaultAmplification;

	public ArobotSettings(){

	}
	public void loadSettings(Context context, SharedPreferences prefs) {

		mDefaultFilterCoefficient = context.getResources().getString(R.string.DEFAULT_FILTER_COEFFICIENT);
		mPREFS_KEY_FILTER_FACTOR = context.getResources().getString(R.string.PREFS_KEY_FILTER_FACTOR);
		this.mPrefsFilterFactor = Float.parseFloat(prefs.getString(mPREFS_KEY_FILTER_FACTOR, mDefaultFilterCoefficient));

		mDefaultPwmMinimal = context.getResources().getString(R.string.DEFAULT_PWM_MINIMAL);
		mPREFS_KEY_PWM_MINIMAL =  context.getResources().getString(R.string.PREFS_KEY_PWM_MINIMAL);
		this.mPrefsPwmMinimal = Integer.parseInt( prefs.getString(mPREFS_KEY_PWM_MINIMAL, mDefaultPwmMinimal));

		mDefaultPwmMaximal = context.getResources().getString(R.string.DEFAULT_PWM_MAXIMAL);
		mPREFS_KEY_PWM_MAXIMAL =  context.getResources().getString(R.string.PREFS_KEY_PWM_MAXIMAL);
		this.mPrefsPwmMaximal = Integer.parseInt( prefs.getString(mPREFS_KEY_PWM_MAXIMAL, mDefaultPwmMaximal));

		mDefaultMotorType = context.getResources().getString(R.string.DEFAULT_MOTOR_TYPE);
		mPREFS_KEY_MOTOR_TYPE =  context.getResources().getString(R.string.PREFS_KEY_MOTOR_TYPE);
		this.mPrefsMotorType = Integer.parseInt( prefs.getString(mPREFS_KEY_MOTOR_TYPE, mDefaultMotorType));

		mDefaultSavingContent = context.getResources().getString(R.string.DEFAULT_SAVING_CONTENT);
		mPREFS_KEY_SAVING_CONTENT =  context.getResources().getString(R.string.PREFS_KEY_SAVING_CONTENT);
		this.mPrefsSavingContent = Integer.parseInt(prefs.getString(mPREFS_KEY_SAVING_CONTENT, mDefaultSavingContent));

		mDefaultSensorDelay = context.getResources().getString(R.string.DEFAULT_SENSOR_DELAY);
		mPREFS_KEY_SENSOR_DELAY =  context.getResources().getString(R.string.PREFS_KEY_SENSOR_DELAY);
		this.mPrefsSensorDelay = Integer.parseInt(prefs.getString(mPREFS_KEY_SENSOR_DELAY, mDefaultSensorDelay));

		mDefaultTimerPeriod = context.getResources().getString(R.string.DEFAULT_FUSION_TIMER_PERIOD);
		mPREFS_KEY_FUSION_TIMER_PERIOD =  context.getResources().getString(R.string.PREFS_KEY_FUSION_TIMER_PERIOD);
		this.mPrefsTimerPeriod = Integer.parseInt(prefs.getString(mPREFS_KEY_FUSION_TIMER_PERIOD, mDefaultTimerPeriod));

		mDefaultFusionContent = context.getResources().getString(R.string.DEFAULT_FUSION_CONTENT);
		mPREFS_KEY_FUSION_CONTENT =  context.getResources().getString(R.string.PREFS_KEY_FUSION_CONTENT);
		this.mPrefsFusionContent = Integer.parseInt(prefs.getString(mPREFS_KEY_FUSION_CONTENT,mDefaultFusionContent));

		mDefaultRollOffset = context.getResources().getString(R.string.DEFAULT_ROLL_OFFSET);
		mPREFS_KEY_ROLL_OFFSET =  context.getResources().getString(R.string.PREFS_KEY_ROLL_OFFSET);
		this.mPrefsRollOffset = Integer.parseInt(prefs.getString(mPREFS_KEY_ROLL_OFFSET, mDefaultRollOffset));;

		mDefaultAmplification = context.getResources().getString(R.string.DEFAULT_AMPLIFICATION);
		mPREFS_KEY_AMPLIFICATION =  context.getResources().getString(R.string.PREFS_KEY_AMPLIFICATION);
		this.mPrefsAmplification = Float.parseFloat(prefs.getString(mPREFS_KEY_AMPLIFICATION, mDefaultRollOffset));;

	}


	public int getPrefsTimerPeriod() {
		return mPrefsTimerPeriod;
	}
	public int getPrefsFusionContent() {
		return mPrefsFusionContent;
	}

	public int getMotorType() {
		return mPrefsMotorType;
	}

	public int getPrefsSensorDelay() {
		return mPrefsSensorDelay;
	}
	public int getPrefsSavingContent() {
		return mPrefsSavingContent;
	}
	public float getFilterFactor() {
		return mPrefsFilterFactor;
	}

	public int getPrefsRollOffset() {
		return mPrefsRollOffset;
	}
	public int getPrefsPwmMinimal() {
		return mPrefsPwmMinimal;
	}
	public int getPrefsPwmMaximal() {
		return mPrefsPwmMaximal;
	}

	public float getPrefsAmplification() {
		return mPrefsAmplification;
	}
}
