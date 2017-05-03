package de.jandrotek.android.arobot.libbluetooth;

import org.junit.Before;
import org.junit.Test;

import static de.jandrotek.android.arobot.libbluetooth.TxBTMessage.BT_CMD_VELOCITY;
import static de.jandrotek.android.arobot.libbluetooth.TxBTMessage.BT_MAGIC_WORD;
import static de.jandrotek.android.arobot.libbluetooth.TxBTMessage.CMD_MAX_POSITIVE;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jan on 03.05.2017.
 */

public class TxBtMessageTest {
    private TxBTMessage mMessage;
    private float[] mLeftRightCmd;
    private byte[] mTxMessage;

    @Before
    public void crete(){
        mMessage = new TxBTMessage();
        mLeftRightCmd = new float[2];
    }

    @Test
    public void constructor_isCorrect() throws Exception {
        assertNotEquals(null, mMessage);
    }

    @Test
    public void velocity_isZero()throws Exception {
        mLeftRightCmd[0] = 0;
        mLeftRightCmd[1] = 0;
        short cmdLeft = 0;
        short cmdRight = 0;
        mTxMessage = mMessage.prepareTxMessage(mLeftRightCmd);
        boolean checkOk = checkBtVelocityMessage(cmdLeft, cmdRight);
        assertTrue(checkOk == true);
    }

    @Test
    public void velocity_isLeft100()throws Exception {
        mLeftRightCmd[0] = 100;
        mLeftRightCmd[1] = 0;
        short cmdLeft = 100;
        short cmdRight = 0;
        mTxMessage = mMessage.prepareTxMessage(mLeftRightCmd);
        boolean checkOk = checkBtVelocityMessage(cmdLeft, cmdRight);
        assertTrue(checkOk == true);
    }

    @Test
    public void velocity_isRight100()throws Exception {
        mLeftRightCmd[0] = 0;
        mLeftRightCmd[1] = 100;
        short cmdLeft = 0;
        short cmdRight = 100;
        mTxMessage = mMessage.prepareTxMessage(mLeftRightCmd);
        boolean checkOk = checkBtVelocityMessage(cmdLeft, cmdRight);
        assertTrue(checkOk == true);
    }

    @Test
    public void velocity_isLeft10000()throws Exception {
        mLeftRightCmd[0] = 10000;
        mLeftRightCmd[1] = 0;
        short cmdLeft = 10000;
        short cmdRight = 0;
        mTxMessage = mMessage.prepareTxMessage(mLeftRightCmd);
        boolean checkOk = checkBtVelocityMessage(cmdLeft, cmdRight);
        assertTrue(checkOk == true);
    }

    @Test
    public void velocity_isRight10000()throws Exception {
        mLeftRightCmd[0] = 0;
        mLeftRightCmd[1] = 10000;
        short cmdLeft = 0;
        short cmdRight = 10000;
        mTxMessage = mMessage.prepareTxMessage(mLeftRightCmd);
        boolean checkOk = checkBtVelocityMessage(cmdLeft, cmdRight);
        assertTrue(checkOk == true);
    }

    @Test
    public void velocity_isLeftLimited30000()throws Exception {
        mLeftRightCmd[0] = 30000;
        mLeftRightCmd[1] = 0;
        short cmdLeft = CMD_MAX_POSITIVE;
        short cmdRight = 0;
        mTxMessage = mMessage.prepareTxMessage(mLeftRightCmd);
        boolean checkOk = checkBtVelocityMessage(cmdLeft, cmdRight);
        assertTrue(checkOk == true);
    }

    @Test
    public void velocity_isRightLimited30000()throws Exception {
        mLeftRightCmd[0] = 0;
        mLeftRightCmd[1] = 30000;
        short cmdLeft = 0;
        short cmdRight = CMD_MAX_POSITIVE;
        mTxMessage = mMessage.prepareTxMessage(mLeftRightCmd);
        boolean checkOk = checkBtVelocityMessage(cmdLeft, cmdRight);
        assertTrue(checkOk == true);
    }

    private boolean checkBtVelocityMessage(short cmdLeft, short cmdRight){
         short txCmdLeft;
         short txCmdRight;

        if(mTxMessage[0] != BT_MAGIC_WORD)
            return false;
        if(mTxMessage[1] != BT_CMD_VELOCITY)
            return false;
        txCmdLeft = (short) ((char)mTxMessage[3] + (char)mTxMessage[2] * 256);
        txCmdRight = (short) ((char)mTxMessage[5] + (char)mTxMessage[4] * 256);
        if (txCmdLeft != cmdLeft)
            return false;
        if (txCmdRight != cmdRight)
            return false;
        return true;
    }
}
