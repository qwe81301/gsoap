package com.inspur.youlook.sdk.gsoap.asynctask;

import android.os.AsyncTask;

import com.inspur.youlook.sdk.gsoap.GsoapConnectionSingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.Constants;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2017/5/18.
 */

public abstract class GsoapAsyncTask extends AsyncTask<String, Object, CallbackStatus> {

    private static final String TAG = GsoapAsyncTask.class.getSimpleName();

    private GsoapCallback mCallback;

    public GsoapAsyncTask(GsoapCallback callback) {
        mCallback = callback;
    }

    public GsoapCallback getCallback() {
        return mCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    abstract JniResponse requestAPI(String... params);

    @Override
    protected CallbackStatus doInBackground(String... params) {
        CallbackStatus status;
        String jsonString;
        if (!GsoapConnectionSingleton.getInstance().isConnected()) {
            status = new CallbackStatus(Constants.RN_STATUS_CODE_GSOAP_NOT_CONNECT);
        } else {
            try {
                JniResponse response = requestAPI(params);
                int responseCode = response != null ? response.getReturnCode() : 1;
                jsonString = responseCode == 0 ? response.getReturnValue() : "";
                status = new CallbackStatus(parseSTBStatusCode(responseCode));
                status.setData(jsonString);
            } catch (UnsatisfiedLinkError | NullPointerException e) {
                e.printStackTrace();
                status = new CallbackStatus("Gsoap Error:" + e.getCause().toString());
            }
        }
        return status;
    }

    @Override
    protected void onPostExecute(CallbackStatus status) {
        super.onPostExecute(status);
        if (mCallback != null) {
            mCallback.invoke(status.getCode(), status.getMessage(), status.getData());
        } else {
            Log.getInstance().writeLog(TAG, "onPostExecute", "call without callback", Log.LogLevel.ERROR);
        }
    }

    public static String verifyUserID(String userID) {
        if (userID == null || userID.length() == 0 || !GsoapConnectionSingleton.getInstance().getCurrentUserName().equalsIgnoreCase(userID))
            return GsoapConnectionSingleton.getInstance().getCurrentUserName();
        return userID;
    }

    public static String verifyStbToken(String stbToken) {
        if (stbToken == null || stbToken.length() == 0 || !GsoapConnectionSingleton.getInstance().getCurrentStbToken().equalsIgnoreCase(stbToken))
            return GsoapConnectionSingleton.getInstance().getCurrentStbToken();
        return stbToken;
    }

    public static int parseSTBStatusCode(int code) {
        return code + 2000;
    }
}
