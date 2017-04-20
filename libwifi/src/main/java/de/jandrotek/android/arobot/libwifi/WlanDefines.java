package de.jandrotek.android.arobot.libwifi;

/**
 * Created by jan on 20.04.17.
 */

public class WlanDefines {
    // Message types sent from the WlanService Handler
    private static final int WLAN_MSG_OFFSET = 0x100;
    public static final int MESSAGE_STATE_CHANGE = 1 + WLAN_MSG_OFFSET;
    public static final int MESSAGE_READ = 2 + WLAN_MSG_OFFSET;
    public static final int MESSAGE_WRITE = 3 + WLAN_MSG_OFFSET;
    public static final int MESSAGE_DEVICE_NAME = 4 + WLAN_MSG_OFFSET;
    public static final int MESSAGE_TOAST = 5 + WLAN_MSG_OFFSET;
}
