package com.example.myfabapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import static android.R.drawable.ic_btn_speak_now;
import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;
import static android.R.drawable.ic_menu_help;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton mFab;
    private AppStateController mStateController;
    private TextView mTextOnScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mStateController = new AppStateController();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFABPress();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        mFab.setImageResource(ic_menu_help);
        mFab.setBackgroundTintList(ColorStateList.valueOf(Color.));
        mTextOnScreen = (TextView)findViewById(R.id.TextOnScreen);
        mTextOnScreen.setText("idle");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleFABPress() {
        // get state form StateCOntroller
        int state = mStateController.getAppState();
        // depending on state, call the proper commands
        if(state == AppStateController.eIdle) {
//            mFab.setImageResource(ic_media_pause);
            mStateController.setAppState(AppStateController.eNotConnected);
            mFab.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            mTextOnScreen.setText("eNotConnected");
        } else if(state == AppStateController.eNotConnected){
                // try to connect
            mFab.setImageResource(ic_btn_speak_now);
            mFab.setBackgroundTintList(ColorStateList.valueOf(Color.CYAN));
            mStateController.setAppState(AppStateController.eConnected);
            mTextOnScreen.setText("eConnected");
        } else if(state == AppStateController.eConnected){
            // try to connect
            mFab.setImageResource(ic_btn_speak_now);
            mFab.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
            mStateController.setAppState(AppStateController.eReadyToMove);
            mTextOnScreen.setText("eReadyToMove");
        } else if (state == AppStateController.eReadyToMove){
//            if(mFragmentIndexAct == ArobotDefines.FRAGMENT_SENSOR_MOVEMENT) {
//                if (mSensorService != null) {
//                    startMoveInSensFrag();
//                }
//            }
//            mAppBarExpanded = false;
//            mVisibility = View.INVISIBLE;
//                    mVisibility = View.GONE;
            mFab.setImageResource(ic_media_pause);
            mStateController.setAppState(AppStateController.eMoving);
            mFab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            mTextOnScreen.setText("eMoving");

        } else if (state == AppStateController.eMoving){
//            if(mFragmentIndexAct == ArobotDefines.FRAGMENT_SENSOR_MOVEMENT) {
//                stopMoveInSensFrag();
//            }
//            mAppBarExpanded = true;
//            mVisibility = View.VISIBLE;
            mFab.setImageResource(ic_media_play);
            mFab.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
//            handleBtMoveToggle(false);
            mStateController.setAppState(AppStateController.eReadyToMove);
            mTextOnScreen.setText("eReadyToMove");

        } else { // unknown
            mTextOnScreen.setText("unkown");

        }
    }

}
