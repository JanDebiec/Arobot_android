package de.jandrotek.android.arobot.tab;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jan on 13.07.16.
 */
public class SliderDatafilterOptTest {
    private SliderDatafilterOpt oFilter;
    private final int nSize = 4;

    @Before
    public void setUp() throws Exception {
        oFilter = new SliderDatafilterOpt(nSize);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testFirstInput(){
        float output;
        int input = 100;
        output = oFilter.calcFilter(input);
        assertEquals(output, (float)25.0f, 1.0f);
    }

    @Test
    public void testSecondInput(){
        float output;
        int input = 100;
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        assertEquals(output, (float)(50.0f), 1.0f);
    }

    @Test
    public void testThirdInput(){
        float output;
        int input = 100;
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        assertEquals(output, (float)(75.0f), 1.0f);
    }

    @Test
    public void testForthInput(){
        float output;
        int input = 100;
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        assertEquals(output, (float)(100.0f), 1.0f);
    }

    @Test
    public void testFifthInput(){
        float output;
        int input = 100;
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        output = oFilter.calcFilter(input);
        assertEquals( (float)(100.0f), output,1.0f);
    }

    @Test
    public void testNInput(){
        float output = -1.0f;
        int input = 100;
        for(int i = 0; i < 26; i++) {
            output = oFilter.calcFilter(input);
        }
        assertEquals((float)(100.0f), output,1.0f);
    }



}