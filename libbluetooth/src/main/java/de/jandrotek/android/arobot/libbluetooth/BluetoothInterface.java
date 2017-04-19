package de.jandrotek.android.arobot.libbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by jan on 19.04.17.
 */

public class BluetoothInterface {

    // BT control vars
    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean mBTConnected = false;
    // we need service here, some other fragments can write to BT too
    private BluetoothService mBTService = null;
    private String mConnectedDeviceName = null;
    private TxBTMessage mBTMessCreator;
    private float[] mLeftRightCmd;
    private byte[] mBTMessage;




    void resumeBTConnection() {
        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mBTService.start();
            }
        }

    }

    private void connectBTDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBTService.connect(device);
    }

    public boolean getBTConnected() {
        return mBTConnected;
    }

    public void setBTConnected(boolean bTConnected) {
        mBTConnected = bTConnected;
    }

    public BluetoothService createBTService(Handler handler) {
        mBTService = new BluetoothService(this, handler);
        prepareBTInterface();
        return mBTService;
    }


    private boolean prepareBTInterface() {
        // prepare bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            // allow to run on emulator
            //finish();
            return false;
        }
        initBtMessages();
        return true;
    }

    private void initBtMessages() {
        mBTMessCreator = new TxBTMessage();
        mBTMessage = new byte[TxBTMessage.BTMessageLenght];
    }



    public BluetoothService getChatService() {

        return mBTService;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothDefines.MESSAGE_STATE_CHANGE:
                    if (BuildConfig.DEBUG) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(R.string.title_connected_to);// + mConnectedDeviceName);
                            mBTConnected = true;
                            updateUI();
                            if (mBluetoothFragment != null)
                                mBluetoothFragment.clearChatAdapter();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case BluetoothDefines.MESSAGE_WRITE:
                    if (mBluetoothFragment != null)
                        mBluetoothFragment.writeMsgFromHandler(msg);
                    break;
                case BluetoothDefines.MESSAGE_READ:
                    if (mBluetoothFragment != null)
                        mBluetoothFragment.readMsgFromHandler(msg);
                    break;
                case BluetoothDefines.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothDefines.DEVICE_NAME);
                    setBTConnected(true);

                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothDefines.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothDefines.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // implemented callback for SensorRx
    // receive the results from SensorRx
    // pack data into BT-Frame
    // write to BT-Service
    public void txNewBTCommand(byte[] btMessage) {
        //check if BT connecgted
        if (mBTService != null) {
            if (mBTService.getState() == BluetoothService.STATE_CONNECTED) {
                if (mSensorMovementFragment.isMovementEnabled()) {
                    // send message
                    mBTService.write(btMessage);
                } else {
                    mBTService.write(BluetoothDefines.BT_STOP_MESSAGE);
                }
            }
        }
    }

    public void startBluetooth() {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothDefines.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mBTService == null)
                mBTService = createBTService(mHandler);
        }

    }


}
