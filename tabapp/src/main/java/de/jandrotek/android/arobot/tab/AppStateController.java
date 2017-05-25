package de.jandrotek.android.arobot.tab;

import android.graphics.Color;

/**
 * Created by jan on 10.05.17.
 */

public class AppStateController {
    // states and colors
    public final static int eStateIdle = 0;
    public final static int eColorIdle = Color.DKGRAY;

    public final static int eStateNotConnected = eStateIdle + 1;
    public final static int eColorNotConnected = Color.GRAY;
    public final static int eStateConnected = eStateNotConnected + 1;
    public final static int eColorConnected = Color.BLUE;
    public final static int eStateReadyToMove = eStateConnected + 1;
    public final static int eColorReadyToMove = Color.GREEN;
    public final static int eStateMoving = eStateReadyToMove + 1;
    public final static int eColorMoving = Color.RED;

    // external Interfaces
    public static final int EXT_CONN_UNKNOWN = -1;
    public static final int EXT_CONN_DEMO = 0;
    public static final int EXT_CONN_BT = 1;
    public static final int EXT_CONN_WLAN = 2;
    public static final int EXT_CONN_ROS = 3;


    private int mAppState;
    public int getAppState() {
        return mAppState;
    }

    public void setAppState(int appState) {
        mAppState = appState;
    }



//    public final static int eInterfaceNotDefined =  0;
//    public final static int eInterfaceBlueTooth = eInterfaceNotDefined + 1;
//    public final static int eInterfaceWlan = eInterfaceBlueTooth + 1;
//    public final static int eInterfaceWlanROS = eInterfaceWlan + 1;

    public int getInterface() {
        return mInterface;
    }

    public void setInterface(int anInterface) {
        mInterface = anInterface;
    }

    private int mInterface;


    public AppStateController(){
        mAppState = eStateIdle;
    }

    public boolean isInterfaceInPrefsDefined(){
        boolean flag = false;
        //check in prefs
        // set member
         return flag;
    }

    public void setInterfaceInPrefs(int anInterface){
        ;
    }

    public boolean connect(int timeout){
        boolean flag = false;
        return flag;
    }
}
