package com.inspur.youlook.sdk.gsoap.asynctask;

import com.inspur.youlook.sdk.gsoap.GsoapConnectionSingleton;
import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2016/10/14
 */
public class GsoapDisconnectAsyncTask extends GsoapAsyncTask {

    private static final String TAG = GsoapDisconnectAsyncTask.class.getSimpleName();

    public GsoapDisconnectAsyncTask(GsoapCallback callback) {
        super(callback);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    JniResponse requestAPI(String... params) {
        String userID = verifyUserID(params[0]);
        String stbToken = verifyStbToken(params[1]);
        String clientInfo = GsoapUtils.getClientInfo(userID, stbToken);
        Log.getInstance().writeLog(TAG, "requestAPI", "clientInfo=" + clientInfo);
        JniResponse response = GsoapProxySingleton.getInstance().hpstb_Disconnect(clientInfo);
        int resultCode = response.getReturnCode();
        Log.getInstance().writeLog(TAG, "doInBackground()", "hpstb_Disconnect " + (resultCode == 0 ? "Success" : "Failed"));
        if (resultCode == 0) {
            GsoapConnectionSingleton.getInstance().disconnectSuccess();
        } else {
            GsoapConnectionSingleton.getInstance().disconnectFailed();
        }
        return response;
    }

    @Override
    protected void onPostExecute(CallbackStatus status) {
        super.onPostExecute(status);
        GsoapConnectionSingleton.getInstance().setIsConnecting(false);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        GsoapConnectionSingleton.getInstance().setIsConnecting(false);
    }
}
