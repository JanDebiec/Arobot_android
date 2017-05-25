package de.jandrotek.android.arobot.core;

import java.text.DecimalFormat;

/**
 * Created by jan on 05.07.16.
 */

public class ArobotDefines {
    public static final float FILTER_COEFFICIENT = 0.98f;
    public static final float EPSILON = 0.000000001f;
    public static final int COLOR_GREEN = 0x008000;
    public static final int COLOR_GREY = 0x0f0f0f;

    public static final int FRAGMENT_SENSOR_MOVEMENT = 0;
    public static final int FRAGMENT_MANUAL_MOVEMENT = 1;
    public static final int FRAGMENT_BLUETOOTH_CHAT = 2;

    public static final String[] fragmentNames = {
            "Sensor movement",
            "Manual movement",
            "Bluetooth chat"
    };

    public static final int SLIDER_OFFSET = 50;

    public static final float NS2S = 1.0f / 1000000000.0f;
    public static final int TIME_CONSTANT = 100;//30;
    public static DecimalFormat cmdFormat = new DecimalFormat(" ####.0; -####.0");

//    public static DecimalFormat d = new DecimalFormat("#0.0");

//    public static final int EXT_CONN_UNKNOWN = -1;
//    public static final int EXT_CONN_BT = 0;
//    public static final int EXT_CONN_WLAN = 1;
//    public static final int EXT_CONN_ROS = 2;
    public static final float eBtVelCmdScaleFactor = 10;

}
