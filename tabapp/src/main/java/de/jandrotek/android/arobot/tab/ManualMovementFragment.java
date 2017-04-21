package de.jandrotek.android.arobot.tab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.rajawali3d.view.ISurface;

import java.util.Timer;
import java.util.TimerTask;

import de.jandrotek.android.arobot.core.ArobotDefines;

/**
 * Created by jan on 08.07.16.
 */
public class ManualMovementFragment extends Fragment {
    private static final String TAG = "ManualMovementFragment";
    MovementActivity mActivity;
private VerticalSeekBar mVerticalSeekbarL;
private VerticalSeekBar mVerticalSeekbarR;
private TiltView mManualTilter;
    private SliderDatafilterOpt mFilterL;
    private SliderDatafilterOpt mFilterR;
    private static int mFilterSize = 4;
    private float mOutputFilteredL;
    private float mOutputFilteredR;

    private Timer mDataAcqTimer;
    private TiltRenderer mRenderer;
    private ISurface mRajawaliSurface;

    public void setDataAcqTimerPeriod(int dataAcqTimerPeriod) {
        mDataAcqTimerPeriod = dataAcqTimerPeriod;
    }

    private int mDataAcqTimerPeriod = 40; // in ms

    private static ManualMovementFragment ourInstance = null;

    public static ManualMovementFragment getInstance() {
        if (ourInstance == null) {
            ourInstance = new ManualMovementFragment();
        }
        return ourInstance;
    }

    private ManualMovementFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MovementActivity)getActivity();
        if (savedInstanceState == null) {
            if (BuildConfig.DEBUG) Log.i(TAG, "onCreate");
            setRetainInstance(true);// onDestroy will not be called
        }
        mFilterL = new SliderDatafilterOpt(mFilterSize);
        mFilterR = new SliderDatafilterOpt(mFilterSize);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) Log.i(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.fragment_manual_movement, parent, false);
//        mManualTilter = (TiltView)v.findViewById(R.id.manualTiltView);
        mVerticalSeekbarL = (VerticalSeekBar)v.findViewById(R.id.vertikalSeekBarLeft);
        mVerticalSeekbarR = (VerticalSeekBar)v.findViewById(R.id.vertikalSeekBarRight);
        // Find the TextureView
        mRajawaliSurface = (ISurface) v.findViewById(R.id.rajwali_surface);
        mRenderer = new TiltRenderer(getActivity());
        mRajawaliSurface.setSurfaceRenderer(mRenderer);
        return v;

    }

    @Override
    public void onResume(){
        super.onResume();
        if(mDataAcqTimer == null){
            mDataAcqTimer = new Timer();
        }
        mDataAcqTimer.scheduleAtFixedRate(new DataAcqTask(),
                1000, mDataAcqTimerPeriod);

    }
    @Override
    public void onPause(){
        super.onPause();
        if(mDataAcqTimer != null){
            mDataAcqTimer.cancel();
            mDataAcqTimer.purge();
            mDataAcqTimer = null;
        }
    }

    private void updateUIValues(){

        //tx data to Activity, to transfer to interface
        mActivity.handleVelCmd(mOutputFilteredL, mOutputFilteredR);

        // show on openGL
        float moveForwardCmd = (mOutputFilteredL + mOutputFilteredR) / 2;
        float turnToRightCmd = mOutputFilteredL - mOutputFilteredR;
        float moveForwardRnd = moveForwardCmd * 1.0f / 25;
        float turnToRightRnd = turnToRightCmd * 1.0f / 25;
        if(mRenderer != null){
            mRenderer.setRotateValues(
                    turnToRightRnd,//ok
                    0,
                    moveForwardRnd//
            );
        }
//        if (mManualTilter != null) {
//            float moveForward = (mOutputFilteredL + mOutputFilteredR) / 2;
//            float turnToRight = mOutputFilteredL - mOutputFilteredR;
//            // TODO scale to proper value ( radians ???)
//            moveForward = moveForward * 1.0f / 25;
//            turnToRight = turnToRight * 1.0f / 2500;
//            mManualTilter.setTilt(moveForward,
//                    turnToRight);
//        }
    }

    /**
     * task used for filtering data from sliders
     */
    private class DataAcqTask extends TimerTask {
		public void run() {
            int nInputL, nInputR;
            float fOutputL, fOutputR;
            nInputL = mVerticalSeekbarL.getProgress();
            nInputL -= ArobotDefines.SLIDER_OFFSET;
            nInputR = mVerticalSeekbarR.getProgress();
            nInputR -= ArobotDefines.SLIDER_OFFSET;
            fOutputL = mFilterL.calcFilter(nInputL);
            mOutputFilteredL = fOutputL * fOutputL;
            if(nInputL < 0)
                mOutputFilteredL = -mOutputFilteredL;
            fOutputR = mFilterR.calcFilter(nInputR);
            mOutputFilteredR = fOutputR * fOutputR;
            if(nInputR < 0)
                mOutputFilteredR = -mOutputFilteredR;
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUIValues();
                }
            });
		}
    }
}
