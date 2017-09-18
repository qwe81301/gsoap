package com.inspur.youlook.sdk.gsoap.asynctask;

import com.inspur.youlook.sdk.gsoap.GsoapConnectionSingleton;
import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2017/5/18.
 */
public class ParentPasswordCheckAsyncTask extends GsoapAsyncTask {

    private static final String TAG = ParentPasswordCheckAsyncTask.class.getSimpleName();

    public ParentPasswordCheckAsyncTask(GsoapCallback callback) {
        super(callback);
    }

    @Override
    JniResponse requestAPI(String... params) {
        String userID = GsoapConnectionSingleton.getInstance().getCurrentUserName();
        String stbToken = GsoapConnectionSingleton.getInstance().getCurrentStbToken();
        String clientInfo = GsoapUtils.getClientInfo(userID, stbToken);
        Log.getInstance().writeLog(TAG, "requestAPI", "clientInfo=" + clientInfo);
        String passwordInfo = GsoapUtils.getPasswordInfo(params[0]);
        Log.getInstance().writeLog(TAG, "requestAPI", "passwordInfo=" + passwordInfo);
        return GsoapProxySingleton.getInstance().hpstb_StopSsdp(clientInfo, passwordInfo);
    }
}
