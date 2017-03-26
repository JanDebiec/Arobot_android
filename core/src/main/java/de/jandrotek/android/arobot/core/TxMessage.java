package de.jandrotek.android.arobot.core;

/**
 * Created by jan on 03.07.16.
 */

public class TxMessage {
    public static final int BTMessageLenght = 6;
    private static final int INDEX_MAGIC_WORD = 0;
    private static final int INDEX_CMD_ID = 1;
    private static final int INDEX_BYTE0 = 2;
    private static final int INDEX_BYTE1 = 3;
    private static final int INDEX_BYTE2 = 4;
    private static final int INDEX_BYTE3 = 5;

    private static final byte MAGIC_WORD = (byte) 0xA5;

    private byte[] mBTMessage;
    private byte mToggle = 0;

    public int mMotorType;

    public void setMotorType(int motorType) {
        mMotorType = motorType;
    }

    public static final int MOTOR_TYPE_2PWM = 0;
    public static final int MOTOR_TYPE_SIGN_AND_PWM = 1;



    public TxMessage() {
        mBTMessage = new byte[6];
        mBTMessage[INDEX_MAGIC_WORD] = MAGIC_WORD;
        mBTMessage[INDEX_CMD_ID] = 0;
    }

    private void changeToggleByte() {

        if (mToggle == 0) {
            mToggle = (byte) 0xFF;
        } else {
            mToggle = (byte) 0;
        }
        mBTMessage[INDEX_CMD_ID] = mToggle;
    }

    public byte[] prepareTxMessage(float[] LRCmd) {
        changeToggleByte();
        float leftP, leftN;
        float rightP, rightN;


        if (mMotorType == MOTOR_TYPE_2PWM) {
            leftP = LRCmd[0];
            rightP = LRCmd[1];
            //leftN = 128 -LRCmd[0];
            //rightN = 128 -LRCmd[1];
            if (leftP > 0.0) {
                if (leftP > 254) {
                    mBTMessage[INDEX_BYTE0] = (byte) 254;
                } else {
                    mBTMessage[INDEX_BYTE0] = (byte) leftP;
                }
                mBTMessage[INDEX_BYTE1] = 1;
            } else {
                mBTMessage[INDEX_BYTE0] = 1;
                if (leftP < -254) {
                    mBTMessage[INDEX_BYTE1] = (byte) 254;
                } else {
                    mBTMessage[INDEX_BYTE1] = (byte) (-(byte) leftP);
                }
            }

            if (rightP > 0.0) {
                if (rightP > 254) {
                    mBTMessage[INDEX_BYTE2] = (byte) 254;
                } else {
                    mBTMessage[INDEX_BYTE2] = (byte) rightP;
                }
                mBTMessage[INDEX_BYTE3] = 1;
            } else {
                mBTMessage[INDEX_BYTE2] = 1;
                if (rightP < -254) {
                    mBTMessage[INDEX_BYTE3] = (byte) 254;
                } else {
                    mBTMessage[INDEX_BYTE3] = (byte) (-(byte) rightP);
                }
            }
//            leftP = LRCmd[0] + 128;
//            rightP = LRCmd[1] + 128;
//            leftN = 128 -LRCmd[0];
//            rightN = 128 -LRCmd[1];
//            if(leftP < 0.0) {
//                mBTMessage[INDEX_BYTE0] = 0;
//            } else if (leftP > 255.0) {
//                mBTMessage[INDEX_BYTE0] = (byte)255;
//            } else {
//                mBTMessage[INDEX_BYTE0] = (byte)leftP;
//            }
//            if(leftN < 0.0) {
//                mBTMessage[INDEX_BYTE1] = 0;
//            } else if (leftP > 255.0) {
//                mBTMessage[INDEX_BYTE1] = (byte)255;
//            } else {
//                mBTMessage[INDEX_BYTE1] = (byte)leftN;
//            }
//
//            if(rightP < 0.0) {
//                mBTMessage[INDEX_BYTE2] = 0;
//            } else if (rightP > 255.0) {
//                mBTMessage[INDEX_BYTE2] = (byte)255;
//            } else {
//                mBTMessage[INDEX_BYTE2] = (byte)rightP;
//            }
//            if(rightN < 0.0) {
//                mBTMessage[INDEX_BYTE3] = 0;
//            } else if (rightN > 255.0) {
//                mBTMessage[INDEX_BYTE3] = (byte)255;
//            } else {
//                mBTMessage[INDEX_BYTE3] = (byte)rightN;
//            }
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
