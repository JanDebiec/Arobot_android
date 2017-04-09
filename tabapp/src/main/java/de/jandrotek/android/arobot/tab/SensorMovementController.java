package de.jandrotek.android.arobot.tab;

import android.hardware.SensorManager;

import de.jandrotek.android.arobot.core.MoveCmdCalculator;
import de.jandrotek.android.arobot.core.SensorCalc;

/**
 * Created by jan on 03.12.2016.
 */

class SensorMovementController {

    private boolean mMovementThreadRun = false;
    private SensorMovementFragment mSensorMovementFragment;
    private int mSelectedSensorDelay;

    public void setCalculator(SensorCalc calculator) {
        this.mCalculator = calculator;
        mSensorService.setCalculator(calculator);
    }

    private SensorCalc mCalculator;

    public void setSensorService(SensorService sensorService) {
        this.mSensorService = sensorService;
    }

    private SensorService mSensorService = null;

    public void setSensorMovementFragment(SensorMovementFragment sensorMovementFragment) {
        this.mSensorMovementFragment = sensorMovementFragment;
        mSensorService.setFragment(sensorMovementFragment);
    }

    public SensorMovementController(SensorManager sensorManager){
        mSensorService = new SensorService( sensorManager);//new SensorRx(this);

    }

    public void init(){
        mSensorService.initListeners(mSelectedSensorDelay);

    }

    public void stopSensors(){
        mSensorService.setLoopActive(false);
    }

    public void cleanSensors(){
//        mSensorService.stopThread();
    }

    public void startSensors(){
//        mSensorService.startThread();
    }

    public void activateMovementThread(boolean flag){
        if(flag){ //prepare run, start threads
//            mSensorService.startThread();
            mSensorService.setLoopActive(true);
        } else { // stop threads
            mSensorService.setLoopActive(false);
        }
        mMovementThreadRun = flag;

    }

}
