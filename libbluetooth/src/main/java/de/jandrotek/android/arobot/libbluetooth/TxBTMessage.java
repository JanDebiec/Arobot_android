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

    public static final int CMD_MAX_POSITIVE = 25000;
    public static final int CMD_MAX_NEGATIVE = -25000;

    public static final char BT_MAGIC_WORD = (char) 0xA5;
    public static  final char BT_CMD_VELOCITY = (char)0;

    private char[] mBTMessage;
    private char mToggle = 0;

    public int mMotorType;

    public void setMotorType(int motorType) {
        mMotorType = motorType;
    }

    public static final int MOTOR_TYPE_SIGNED_PWM = 0;
    public static final int MOTOR_TYPE_SIGN_AND_ABS_PWM = 1;

    private short shortLeft;
    private short shortRight;
    private char lowchar;
    private char highchar;


    public TxBTMessage() {
        mMotorType = MOTOR_TYPE_SIGNED_PWM;
        mBTMessage = new char[6];
        mBTMessage[INDEX_MAGIC_WORD] = BT_MAGIC_WORD;
        mBTMessage[INDEX_CMD_ID] = BT_CMD_VELOCITY;
    }

//    private void changeTogglechar() {
//
//        if (mToggle == 0) {
//            mToggle = (char) 0xFF;
//        } else {
//            mToggle = (char) 0;
//        }
//        mBTMessage[INDEX_CMD_ID] = mToggle;
//    }

    public char[] prepareTxMessage(float[] LRCmd) {
//        changeTogglechar();
        float cmdLeft = LRCmd[0];
        float cmdRight = LRCmd[1];
        if(LRCmd[0] > CMD_MAX_POSITIVE)
            cmdLeft = CMD_MAX_POSITIVE;
        if(LRCmd[0] < CMD_MAX_NEGATIVE)
            cmdLeft = CMD_MAX_NEGATIVE;
        if(LRCmd[1] > CMD_MAX_POSITIVE)
            cmdRight = CMD_MAX_POSITIVE;
        if(LRCmd[1] < CMD_MAX_NEGATIVE)
            cmdRight = CMD_MAX_NEGATIVE;

        if (mMotorType == MOTOR_TYPE_SIGNED_PWM) {
            shortLeft = (short) cmdLeft;
            shortRight = (short) cmdRight;
            lowchar = (char)(shortLeft & 0xFF);
            highchar = (char)((shortLeft & 0xFF00) >> 8);
            mBTMessage[INDEX_BYTE0] = highchar;
            mBTMessage[INDEX_BYTE1] = lowchar;
            lowchar = (char)(shortRight & 0xFF);
            highchar = (char)((shortRight & 0xFF00) >> 8);
            mBTMessage[INDEX_BYTE2] = highchar;
            mBTMessage[INDEX_BYTE3] = lowchar;

        } else { // motor sign + PWM
//            leftP = LRCmd[0];
//            rightP = LRCmd[1];
//            if (leftP < 0.0) {
//                mBTMessage[INDEX_BYTE0] = 0;
//                if (leftP < -255.0) {
//                    mBTMessage[INDEX_BYTE1] = (byte) 255;
//                } else {
//                    mBTMessage[INDEX_BYTE1] = (byte) (-(byte) leftP);
//                }
//            } else {
//                mBTMessage[INDEX_BYTE0] = (byte) 0xFF;
//                if (leftP > 255.0) {
//                    mBTMessage[INDEX_BYTE1] = (byte) 255;
//                } else {
//                    mBTMessage[INDEX_BYTE1] = ((byte) leftP);
//                }
//            }
//
//            if (rightP < 0.0) {
//                mBTMessage[INDEX_BYTE2] = 0;
//                if (rightP < -255.0) {
//                    mBTMessage[INDEX_BYTE3] = (byte) 255;
//                } else {
//                    mBTMessage[INDEX_BYTE3] = (byte) (-(byte) rightP);
//                }
//            } else {
//                mBTMessage[INDEX_BYTE2] = (byte) 0xFF;
//                if (leftP > 255.0) {
//                    mBTMessage[INDEX_BYTE3] = (byte) 255;
//                } else {
//                    mBTMessage[INDEX_BYTE3] = ((byte) rightP);
//                }
//            }
        }
        return mBTMessage;
    }

}
