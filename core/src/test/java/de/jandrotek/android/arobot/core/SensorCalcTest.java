package de.jandrotek.android.arobot.core;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by jan on 20.04.17.
 */

public class SensorCalcTest {

    @Test
    public void constructor_isCorrect() throws Exception {
        SensorCalc mObject = new SensorCalc();
        assertNotEquals(null, mObject);
    }

    @Test
    public void calcRotMx_noExecpt() throws Exception {
        float[] mAccMagOrientation = new float[3];
        float[] result = null;
        SensorCalc mObject = new SensorCalc();
        result = mObject.getRotationMatrixFromOrientation(mAccMagOrientation);
        assertNotEquals(null, result);

    }

    @Test
    public void calcFused_noExcept() throws Exception {
        float gyroOrient = 0;
        float accOrient = 0;
        SensorCalc mObject = new SensorCalc();
        float result = -1;
        result = mObject.calcFusedOrientation(gyroOrient, accOrient);
        assertNotEquals(result, -1, 0.01);

    }

    @Test
    public void getRotFromGyro_noExcept() throws Exception {
        float[] gyroValues = {0, 0, 0};
        float[] deltaRot = {0, 0, 0, 0};
        float factor = 0;
        SensorCalc mObject = new SensorCalc();
        float result[];
        mObject.getRotationVectorFromGyro(gyroValues, deltaRot, factor);
        assertNotEquals(null, deltaRot);

    }

    @Test
    public void getRotFromOrient_noExcept() throws Exception {
        float[] orient = {0, 0, 0};
        SensorCalc mObject = new SensorCalc();
        float result[];
        result = mObject.getRotationMatrixFromOrientation(orient);
        assertNotEquals(null, result);

    }
}
