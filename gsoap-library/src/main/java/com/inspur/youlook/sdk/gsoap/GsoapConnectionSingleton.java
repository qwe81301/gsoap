package com.inspur.youlook.sdk.gsoap;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.inspur.youlook.sdk.gsoap.asynctask.GsoapConnectAsyncTask;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapConnectionListener;
import com.inspur.youlook.sdk.gsoap.utils.Constants;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;
import com.inspur.youlook.sdk.gsoap.utils.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GsoapConnectionSingleton {

    private static final String TAG = GsoapConnectionSingleton.class.getSimpleName();

    private static final long STB_KEEP_ALIVE_DURATION = 30000; // 30s

    private Context mContext;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private String mCurrentConnectionIP = "";
    private String mCurrentUserName = "";
    private String mCurrentStbToken = "";
    private String mCurrentUUID = "";

    private List<GsoapConnectionListener> mGsoapConnectionListenerList = new ArrayList<>();

    // private for Singleton
    private GsoapConnectionSingleton() {
    }

    public static GsoapConnectionSingleton getInstance() {
        return SingletonHolder.GSOAP_INSTANCE;
    }

    public String getCurrentStbToken() {
        return mCurrentStbToken;
    }

    public void setCurrentStbToken(String currentStbToken) {
        this.mCurrentStbToken = currentStbToken;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    public String getCurrentConnectionIP() {
        return mCurrentConnectionIP;
    }

    public String getCurrentUserName() {
        return mCurrentUserName;
    }

    public void setCurrentConnectionInfo(String connectionIP, String currentUserName, String currentUUID) {
        this.mCurrentConnectionIP = connectionIP;
        this.mCurrentUserName = currentUserName;
        this.mCurrentUUID = currentUUID;
        PreferenceUtil.saveConnectionInfo(mContext, mCurrentConnectionIP, mCurrentUserName, mCurrentUUID);
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setIsConnecting(boolean isConnecting) {
        this.isConnecting = isConnecting;
        for (GsoapConnectionListener gsoapConnectionListener : mGsoapConnectionListenerList) {
            gsoapConnectionListener.isConnecting(isConnecting);
        }
    }

    public void init(Context ctx) {
        this.mContext = ctx;
        if (!isConnected) {
            int result = GsoapProxySingleton.getInstance().hpstb_Init(GsoapUtils.getInitInfo("2000")).getReturnCode();
            Log.getInstance().writeLog(TAG, "init", "hpstb_Init " + (result == 0 ? "Success!" : "Failed!"));
            int setResult = GsoapProxySingleton.getInstance().hpstb_SetEventNotify().getReturnCode();
            Log.getInstance().writeLog(TAG, "init", "hpstb_SetEventNotify " + (setResult == 0 ? "Success!" : "Failed!"));
        }
//        startKeepAlive();
    }

    public void connect(String deviceIP, String userID, String uuid, String verifyCode, GsoapCallback callback) {
        Log.getInstance().writeLog(TAG, "connect", "deviceIP=" + deviceIP + ", userID=" + userID + ",verifyCode="+verifyCode);
        if (userID == null || userID.equals("") || deviceIP == null || deviceIP.equals("")) {
            int statusCode = Constants.RN_STATUS_PARAMETER_ERROR;
            String statusMsg = Constants.RN_STATUS_CODE_MAP.get(statusCode);
            if (callback != null)
                callback.invoke(statusCode, statusMsg);
            return;
        }
        if (!isConnecting) {//連接上進入此行
            String clientInfo = "{\"userid\":\""+userID+"\",\"uuid\":\"" + uuid +"\",\"verifyCode\":\"" + verifyCode +"\"}";
            new GsoapConnectAsyncTask(deviceIP, clientInfo, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.getInstance().writeLog(TAG, "connect", "Failed! Because: is connecting.");
        }
    }

    public void connect(String deviceIP, String userID, String uuid, String verifyCode) {
        connect(deviceIP, userID, uuid, verifyCode, null);
    }

    public void reconnect() {
        if (isWifiAvailable()) {
            String lastIP = PreferenceUtil.getLastIP(mContext);
            String lastUserID = PreferenceUtil.getLastUserName(mContext);
            String lastUUID = PreferenceUtil.getLastUUID(mContext);
            connect(lastIP, lastUserID, lastUUID, "");
        } else {
            Log.getInstance().writeLog(TAG, "reconnect()", "Failed! Because: Wifi unavailable.");
        }
    }

    // unused
    private void startKeepAlive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(STB_KEEP_ALIVE_DURATION);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isWifiAvailable() && isConnected) {
                        connect(mCurrentConnectionIP, mCurrentUserName, mCurrentUserName, "");//
                    }
                }
            }
        }).start();
    }

    public void wifiConnected(boolean connected) {
        if (connected) {
            if (mContext != null && PreferenceUtil.isAutoConnect(mContext))
                reconnect();
        } else {
            isConnected = false;
        }
    }

    public void addGsoapConnectionListener(GsoapConnectionListener connectionListener) {
        if (!mGsoapConnectionListenerList.contains(connectionListener))
            mGsoapConnectionListenerList.add(connectionListener);
    }

    public void removeGsoapConnectionListener(GsoapConnectionListener connectionListener) {
        if (mGsoapConnectionListenerList.contains(connectionListener))
            mGsoapConnectionListenerList.remove(connectionListener);
    }

    public void connectSuccess(String connectIp, String connectResult, String clientInfo) {
        isConnected = true;
        // connectResultInfo = {"userid":"15123456789","token":"15123456789-1025-413744719","expires":60}
        Log.getInstance().writeLog(TAG, "connectSuccess", "connectResult=" + connectResult);
        try {
            JSONObject resultJSONObject = new JSONObject(connectResult);
            String userId = resultJSONObject.getString("userid");
            mCurrentStbToken = resultJSONObject.getString("token");
            JSONObject clientJSONObject = new JSONObject(clientInfo);
            mCurrentUUID = clientJSONObject.getString("uuid");
            setCurrentConnectionInfo(connectIp, userId, mCurrentUUID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (GsoapConnectionListener gsoapConnectionListener : mGsoapConnectionListenerList) {
            gsoapConnectionListener.connectSuccess(connectIp, connectResult);
        }
    }

    public void connectFailed() {
        isConnected = false;
        for (GsoapConnectionListener gsoapConnectionListener : mGsoapConnectionListenerList) {
            gsoapConnectionListener.connectFailed();
        }
    }

    public void disconnectSuccess() {
        isConnected = false;
        setCurrentConnectionInfo("", "", "");
        for (GsoapConnectionListener gsoapConnectionListener : mGsoapConnectionListenerList) {
            gsoapConnectionListener.disconnectSuccess();
        }
    }

    public void disconnectFailed() {
        isConnected = false;
        for (GsoapConnectionListener gsoapConnectionListener : mGsoapConnectionListenerList) {
            gsoapConnectionListener.disconnectFailed();
        }
    }



    private static class SingletonHolder {
        private static final GsoapConnectionSingleton GSOAP_INSTANCE = new GsoapConnectionSingleton();
    }

    public boolean isWifiAvailable() {
        if (mContext == null)
            return false;
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {

        }
        return wifiManager.isWifiEnabled();
    }
}
