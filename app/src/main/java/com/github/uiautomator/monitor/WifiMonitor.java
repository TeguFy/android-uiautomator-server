package com.github.uiautomator.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.github.uiautomator.R;
import com.github.uiautomator.Service;

/**
 * Created by hzsunshx on 2018/1/16.
 */

public class WifiMonitor extends AbstractMonitor {
    private static final String TAG = "WifiMonitor";
    private BroadcastReceiver receiver;

    public WifiMonitor(Context context, HttpPostNotifier notifier) {
        super(context, notifier);
    }

    @Override
    public void init() {
        Log.i(TAG, "Wifi monitor init");
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {// Monitor wifi on/off, unrelated to wifi connection
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (wifiState) {
                        case WifiManager.WIFI_STATE_DISABLED:
                        case WifiManager.WIFI_STATE_DISABLING:
                            report(notifier, "wifi");
                            report(notifier, new WifiInfos(false, "").toString());
                            break;
                    }
                }
                // Monitor network connection, including wifi and mobile data on/off, and all available connections will be monitored
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    //Get NetworkInfo object for network connection status
                    NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (info != null) {
                        //If current network connection is successful and available
                        if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                            WifiManager wifi = (WifiManager) ((Service) context).getSystemService(Context.WIFI_SERVICE);
                            WifiInfo wInfo = wifi.getConnectionInfo();
                            report(notifier, new WifiInfos(true, wInfo.getSSID()).toString());
                        }
                    }
                }
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                int ip = wifiManager.getConnectionInfo().getIpAddress();
                String ipStr = (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
                ((Service) context).setNotificationContentText(context.getString(R.string.monitor_service_text) + " " + ipStr);
            }
        };
    }

    @Override
    public void register() {
        Log.i(TAG, "Wifi monitor starting");

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);
    }

    @Override
    public void unregister() {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    private void report(HttpPostNotifier notifier, String content) {
        notifier.Notify("/info/wifi", content);
    }

    class WifiInfos {

        private boolean wifiStatus = false;
        private String ssid = "";

        public WifiInfos(boolean wifiStatus, String ssid) {
            this.wifiStatus = wifiStatus;
            this.ssid = ssid;
        }

        @Override
        public String toString() {
            return "{wifiStatus:" + wifiStatus +
                    ",ssid:" + ssid + "}";
        }
    }

}
