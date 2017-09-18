package com.inspur.youlook.sdk.gsoap.asynctask;

import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2017/5/18.
 */

public class SendKeyAsyncTask extends GsoapAsyncTask {

    private static final String TAG = SendKeyAsyncTask.class.getSimpleName();

    public SendKeyAsyncTask(GsoapCallback callback) {
        super(callback);
    }

    @Override
    JniResponse requestAPI(String... params) {
        String userID = verifyUserID(params[0]);
        String stbToken = verifyStbToken(params[1]);
        String clientInfo = GsoapUtils.getClientInfo(userID, stbToken);
        String keyCode = GsoapUtils.getKeyInfo(params[2]);
        Log.getInstance().writeLog(TAG, "requestAPI", "clientInfo=" + clientInfo);
        Log.getInstance().writeLog(TAG, "requestAPI", "keyCode=" + keyCode);
        return GsoapProxySingleton.getInstance().hpstb_SendKey(clientInfo, keyCode);
    }
}
