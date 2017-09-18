package com.example.inspur.testgsoup;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import com.inspur.youlook.sdk.gsoap.GsoapConnectionSingleton;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by inspur on 2017/8/14.
 */

public class NetworkUtil {
    private static final String TAG = NetworkUtil.class.getSimpleName();
    private static int mCurrentNetworkType;
    private static String mCurrentNetworkInfo;
    private ArrayList<WifiConnectionListener> mWifiConnectionListenerArray = new ArrayList<>();
    private Context mContext;

    private NetworkUtil() {
    }

    public static NetworkUtil getInstance() {
        return NetworkUtil.SingletonHolder.sSingleton;
    }

    public void init(Context context) {
        this.mContext = context;
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        Log.v("init networkInfo", String.valueOf(networkInfo));
        if (networkInfo != null) {
            mCurrentNetworkType = networkInfo.getType();
            Log.v("init CurrentNetworkType", String.valueOf(mCurrentNetworkType));
            mCurrentNetworkInfo = networkInfo.getExtraInfo();
            Log.v("init CurrentNetworkInfo", mCurrentNetworkInfo);
        }
    }

    /**
     * Only used by Connectivity_Change broadcast receiver
     *
     * @param connected
     */
    private void notifyWifiChanged(boolean connected) {
        for (WifiConnectionListener listener : mWifiConnectionListenerArray) {
            listener.wifiConnected(connected);
        }
        GsoapConnectionSingleton.getInstance().wifiConnected(connected);
    }

    public void addWifiListener(WifiConnectionListener listener) {
        if (!mWifiConnectionListenerArray.contains(listener))
            mWifiConnectionListenerArray.add(listener);
    }

    public void removeWifiListener(WifiConnectionListener listener) {
        if (mWifiConnectionListenerArray.contains(listener))
            mWifiConnectionListenerArray.remove(listener);
    }

    public boolean isWifiAvailable() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {

        }
        return wifiManager.isWifiEnabled();
    }

    public boolean isNetworkAvailable() {
        if (mContext == null)
            return false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public boolean isNetworkAvailable(String host) {
        Process p1;
        boolean reachable = false;
        try {
            String command = "ping -c 1 " + host;
            p1 = Runtime.getRuntime().exec(command);
            int returnVal = p1.waitFor();
            reachable = (returnVal == 0);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return reachable;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isNetworkAvailable(String addr, int openPort, int timeOutMillis) {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public String getLocalHostIp() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        //Log.v("isWifiEnabled", String.valueOf(wifiManager.isWifiEnabled()));
        if (!wifiManager.isWifiEnabled()) { //isWifiEnabled: true
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //Log.v("wifiInfo.getIpAddress", String.valueOf(wifiInfo.getIpAddress()));
        return intToIp(wifiInfo.getIpAddress()); //wifiInfo.getIpAddress: -645853012
    }

    private String intToIp(int i) {
        Log.v("intToIp", (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF));
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    public interface WifiConnectionListener {
        void wifiConnected(boolean connected);
    }

    private static class SingletonHolder {
        private static final NetworkUtil sSingleton = new NetworkUtil();
    }

    public static class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            int networkType = -1;
            String networkInfo = "";
            if (netInfo != null) {
                networkType = netInfo.getType();
                networkInfo = netInfo.getExtraInfo();
            }

            /* Receiving multiple broadcast is a device specific problem */
            if (networkType == mCurrentNetworkType && networkInfo.equals(mCurrentNetworkInfo))
                return;

            //Log.getInstance().writeLog(TAG, "onReceive()", "network type = " + networkType);
            //Log.getInstance().writeLog(TAG, "onReceive()", "network info=" + networkInfo);
            mCurrentNetworkType = networkType;
            mCurrentNetworkInfo = networkInfo;

            final boolean isWifiConnected = networkType == ConnectivityManager.TYPE_WIFI;
            //Log.getInstance().writeLog(TAG, "onReceive()", "Wifi Connected: " + isWifiConnected);
            NetworkUtil.getInstance().notifyWifiChanged(isWifiConnected);
        }
    }
}

