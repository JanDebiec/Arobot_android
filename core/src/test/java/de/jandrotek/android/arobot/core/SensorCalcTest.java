package de.jandrotek.android.arobot.core;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by jan on 20.04.17.
 */

public class SensorCalcTest {

    private SensorCalc mObject;
    private float[] mAccMagOrientation;

    @Before
    public void create(){
        mAccMagOrientation = new float[3];
        mObject = new SensorCalc.Builder().build();
    }

    @Test
    public void constructor_isCorrect() throws Exception {
        assertNotEquals(null, mObject);
    }

    @Test
    public void calcRotMx_noExecpt() throws Exception {
        float[] result = null;
        result = mObject.getRotationMatrixFromOrientation(mAccMagOrientation);
        assertNotEquals(null, result);
    }

    @Test
    public void calcFused_noExcept() throws Exception {
        float gyroOrient = 0;
        float accOrient = 0;
        float result = -1;
        result = mObject.calcFusedOrientation(gyroOrient, accOrient);
        assertNotEquals(result, -1, 0.01);
    }

    @Test
    public void getRotFromGyro_noExcept() throws Exception {
        float[] gyroValues = {0, 0, 0};
        float[] deltaRot = {0, 0, 0, 0};
        float factor = 0;
        float result[];
        mObject.getRotationVectorFromGyro(gyroValues, deltaRot, factor);
        assertNotEquals(null, deltaRot);
    }

    @Test
    public void getRotFromOrient_getSize() throws Exception {
        float[] orient = {1, 1, 1};
        float result[];
        result = mObject.getRotationMatrixFromOrientation(orient);
        int size = result.length;
        assertEquals(9, size);
    }
}
