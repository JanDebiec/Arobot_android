package de.jandrotek.android.arobot.libbluetooth;

/**
 * Created by jan on 03.07.16.
 */

public class TxBTMessage {
    public static final int BTMessageLenght = 6;
    private static final int INDEX_MAGIC_WORD = 0;
    private static final int INDEX_CMD_ID = 1;
    private static final int INDEX_BYTE0 = 2;
    private static final int INDEX_BYTE1 = 3;
    private static final int INDEX_BYTE2 = 4;
    private static final int INDEX_BYTE3 = 5;

    public static final byte BT_MAGIC_WORD = (byte) 0xA5;
    public static  final byte BT_CMD_VELOCITY = (byte)0;

    private byte[] mBTMessage;
    private byte mToggle = 0;

    public int mMotorType;

    public void setMotorType(int motorType) {
        mMotorType = motorType;
    }

    public static final int MOTOR_TYPE_SIGNED_PWM = 0;
    public static final int MOTOR_TYPE_SIGN_AND_ABS_PWM = 1;

    private short shortLeft;
    private short shortRight;
    private byte lowByte;
    private byte highByte;


    public TxBTMessage() {
        mMotorType = MOTOR_TYPE_SIGNED_PWM;
        mBTMessage = new byte[6];
        mBTMessage[INDEX_MAGIC_WORD] = BT_MAGIC_WORD;
        mBTMessage[INDEX_CMD_ID] = BT_CMD_VELOCITY;
    }

//    private void changeToggleByte() {
//
//        if (mToggle == 0) {
//            mToggle = (byte) 0xFF;
//        } else {
//            mToggle = (byte) 0;
//        }
//        mBTMessage[INDEX_CMD_ID] = mToggle;
//    }

    public byte[] prepareTxMessage(float[] LRCmd) {
//        changeToggleByte();
        float leftP, leftN;
        float rightP, rightN;

        if (mMotorType == MOTOR_TYPE_SIGNED_PWM) {
            shortLeft = (short) LRCmd[0];
            shortRight = (short) LRCmd[1];
            lowByte = (byte)(shortLeft & 0xFF);
            highByte = (byte)((shortLeft & 0xFF00) >> 8);
            mBTMessage[INDEX_BYTE0] = highByte;
            mBTMessage[INDEX_BYTE1] = lowByte;
            lowByte = (byte)(shortRight & 0xFF);
            highByte = (byte)((shortRight & 0xFF00) >> 8);
            mBTMessage[INDEX_BYTE2] = highByte;
            mBTMessage[INDEX_BYTE3] = lowByte;

        } else { // motor sign + PWM
            leftP = LRCmd[0];
            rightP = LRCmd[1];
            if (leftP < 0.0) {
                mBTMessage[INDEX_BYTE0] = 0;
                if (leftP < -255.0) {
                    mBTMessage[INDEX_BYTE1] = (byte) 255;
                } else {
                    mBTMessage[INDEX_BYTE1] = (byte) (-(byte) leftP);
                }
            } else {
                mBTMessage[INDEX_BYTE0] = (byte) 0xFF;
                if (leftP > 255.0) {
                    mBTMessage[INDEX_BYTE1] = (byte) 255;
                } else {
                    mBTMessage[INDEX_BYTE1] = ((byte) leftP);
                }
            }

            if (rightP < 0.0) {
                mBTMessage[INDEX_BYTE2] = 0;
                if (rightP < -255.0) {
                    mBTMessage[INDEX_BYTE3] = (byte) 255;
                } else {
                    mBTMessage[INDEX_BYTE3] = (byte) (-(byte) rightP);
                }
            } else {
                mBTMessage[INDEX_BYTE2] = (byte) 0xFF;
                if (leftP > 255.0) {
                    mBTMessage[INDEX_BYTE3] = (byte) 255;
                } else {
                    mBTMessage[INDEX_BYTE3] = ((byte) rightP);
                }
            }
        }
        return mBTMessage;
    }

}
