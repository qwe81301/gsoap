package com.inspur.youlook.sdk.gsoap.asynctask;

import android.os.AsyncTask;

import com.inspur.youlook.sdk.gsoap.GsoapConnectionSingleton;
import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2016/10/14
 */
public class GsoapConnectAsyncTask extends AsyncTask<String, Integer, CallbackStatus> {

    private static final String TAG = "GsoapConnectAsyncTask";
    private String mServerIp;
    private String mClientInfo;
    private GsoapCallback mCallback;

    public GsoapConnectAsyncTask(String serverIp, String clientInfo, GsoapCallback stbCallBack) {
        this.mServerIp = serverIp;
        this.mClientInfo = clientInfo;
        this.mCallback = stbCallBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        GsoapConnectionSingleton.getInstance().setIsConnecting(true);
    }

    @Override
    protected CallbackStatus doInBackground(String... params) {
        try {
            if (GsoapConnectionSingleton.getInstance().isWifiAvailable()) {
                String resultInfo = "";
                String clientInfo = mClientInfo;
                Log.getInstance().writeLog(TAG, "doInBackground()", "Client Info = " + clientInfo);
                // connect to stb
                JniResponse response = GsoapProxySingleton.getInstance().hpstb_ConnectToSTB(clientInfo, GsoapUtils.getServerInfo(mServerIp));
                int resultCode = response != null ? response.getReturnCode() : -1;
                Log.getInstance().writeLog(TAG, "doInBackground", "hpstb_ConnectToSTB " + (resultCode == 0 ? "Success!" : "Failed!"));
                if (resultCode == 0) {
                    resultInfo = response.getReturnValue();
                    GsoapConnectionSingleton.getInstance().connectSuccess(mServerIp, resultInfo);
                } else {
                    GsoapConnectionSingleton.getInstance().connectFailed();
                }

                CallbackStatus callbackStatus = new CallbackStatus(GsoapAsyncTask.parseSTBStatusCode(resultCode));
                callbackStatus.setData(resultInfo);
                return callbackStatus;
            } else {
                return new CallbackStatus(GsoapAsyncTask.parseSTBStatusCode(1));
            }
        } catch (UnsatisfiedLinkError error) {
            Log.getInstance().writeLog(TAG, "doInBackground()", error.getMessage(), Log.LogLevel.ERROR);
            // "Sorry, we currently support connection on mips & x86-64 devices.";
            return new CallbackStatus("很抱歉, 暫不支援此裝置連接至機上盒");
        }
    }

    @Override
    protected void onPostExecute(CallbackStatus status) {
        super.onPostExecute(status);
        GsoapConnectionSingleton.getInstance().setIsConnecting(false);
        if (mCallback != null) {
            mCallback.invoke(status.getCode(), status.getMessage(), status.getData());
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        GsoapConnectionSingleton.getInstance().setIsConnecting(false);
    }
}
