package de.jandrotek.android.arobot.core;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by jan on 20.04.17.
 */
// not needed in tabapp, functions in the SensorCalcClass and tested in SensorCalcTest
public class MoveCmdCalcTest {
    @Test
    public void constructor_isCorrect() throws Exception {
        MoveCmdCalculator mObject = new MoveCmdCalculator();
        assertNotEquals(null, mObject);
    }
}
