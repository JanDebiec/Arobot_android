package de.jandrotek.android.arobot.libbluetooth;

import java.util.UUID;

/**
 * Created by jan on 05.07.16.
 */

public class BluetoothDefines {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 2;
    public static final int REQUEST_ENABLE_BT = 3;

    public static final byte[] BT_STOP_MESSAGE = {(byte) 0xA5, 0, 0, 0, 0, 0};

    //     Unique UUID for this application
    public static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public static final UUID MY_UUID_SPP =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

}
