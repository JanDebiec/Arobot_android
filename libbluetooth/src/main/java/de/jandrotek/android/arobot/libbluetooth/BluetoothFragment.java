package de.jandrotek.android.arobot.libbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;

/**
 * Created by jan on 08.07.16.
 */
public class BluetoothFragment extends Fragment {
    private static BluetoothFragment mInstance = null;

    public void setActivity(Activity activity) {
        mActivity = activity;
        mConversationArrayAdapter = new ArrayAdapter<String>(mActivity, R.layout.message);
    }

    private Activity mActivity = null;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    public BluetoothService mChatService = null;

    public static BluetoothFragment getInstance() {
        if (mInstance == null) {
            mInstance = new BluetoothFragment();
        }
        return mInstance;
    }



    public BluetoothFragment() {
    }

    public void clearChatAdapter() {
        if(mConversationArrayAdapter != null) {
            mConversationArrayAdapter.clear();
        }

    }

    public void readMsgFromHandler(Message msg) {
        byte[] readBuf = (byte[]) msg.obj;
        // construct a string from the valid bytes in the buffer
        String readMessage = new String(readBuf, 0, msg.arg1);
        mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);

    }

    public void writeMsgFromHandler(Message msg) {
        byte[] writeBuf = (byte[]) msg.obj;
        // construct a string from the buffer
        String writeMessage = new String(writeBuf);
        mConversationArrayAdapter.add("Me:  " + writeMessage);

    }
}
