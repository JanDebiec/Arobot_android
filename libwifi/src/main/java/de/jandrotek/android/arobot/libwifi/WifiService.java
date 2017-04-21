package de.jandrotek.android.arobot.libwifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by jan on 16.04.17.
 */

public class WifiService{
//public class WifiService(Activity activity){
private static final String TAG = "WifiService";

    private Activity mActivity;


    public WifiService(Activity activity){
        mActivity = activity;
    }


    public void init() {

        /**
         * Listing 16-14: Accessing the Wi-Fi Manager
         */
        String service = Context.WIFI_SERVICE;
        final WifiManager wifi = (WifiManager)mActivity.getSystemService(service);

        /**
         * Listing 16-15: Monitoring and changing Wi-Fi state
         */
        if (!wifi.isWifiEnabled())
            if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING)
                wifi.setWifiEnabled(true);

        /**
         * Listing 16-16: Querying the active network connection
         */
        WifiInfo info = wifi.getConnectionInfo();
        if (info.getBSSID() != null) {
            int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
            int speed = info.getLinkSpeed();
            String units = WifiInfo.LINK_SPEED_UNITS;
            String ssid = info.getSSID();

            String cSummary = String.format("Connected to %s at %s%s. " +
                            "Strength %s/5",
                    ssid, speed, units, strength);
            Log.d(TAG, cSummary);
        }

        /**
         * Listing 16-17: Conducting a scan for Wi-Fi access points
         */
        // Register a broadcast receiver that listens for scan results.
        mActivity.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = wifi.getScanResults();
                ScanResult bestSignal = null;
                for (ScanResult result : results) {
                    if (bestSignal == null ||
                            WifiManager.compareSignalLevel(
                                    bestSignal.level,result.level) < 0)
                        bestSignal = result;
                }

                String connSummary = String.format("%s networks found. %s is" +
                                "the strongest.",
                        results.size(),
                        bestSignal.SSID);

                Toast.makeText(mActivity,
                        connSummary, Toast.LENGTH_LONG).show();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Initiate a scan.
        wifi.startScan();
    }}
