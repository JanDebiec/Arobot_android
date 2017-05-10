package de.jandrotek.android.arobot.tab;

/**
 * Created by jan on 10.05.17.
 */

public class AppStateController {
    public final static int eIdle = 0;
    public final static int eNotConnected = eIdle + 1;
    public final static int eConnected = eNotConnected + 1;
    public final static int eReadyToMove = eConnected + 1;
    public final static int eMoving = eReadyToMove + 1;
    private int mAppState;
    public int getAppState() {
        return mAppState;
    }

    public void setAppState(int appState) {
        mAppState = appState;
    }



    public final static int eInterfaceNotDefined =  0;
    public final static int eInterfaceBlueTooth = eInterfaceNotDefined + 1;
    public final static int eInterfaceWlan = eInterfaceBlueTooth + 1;
    public final static int eInterfaceWlanROS = eInterfaceWlan + 1;

    public int getInterface() {
        return mInterface;
    }

    public void setInterface(int anInterface) {
        mInterface = anInterface;
    }

    private int mInterface;


    public AppStateController(){
        mAppState = eIdle;
    }

    public boolean isInterfaceInPrefsDefined(){
        //check in prefs
        // set member
        // return flag
    }

    public void setInterfaceInPrefs(int anInterface){
        ;
    }

    public boolean connect(int timeout){

    }
}
