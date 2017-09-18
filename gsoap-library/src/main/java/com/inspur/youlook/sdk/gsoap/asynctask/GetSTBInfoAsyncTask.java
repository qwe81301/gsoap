package com.inspur.youlook.sdk.gsoap.asynctask;

import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2017/5/18.
 */
public class GetSTBInfoAsyncTask extends GsoapAsyncTask {

    private static final String TAG = GetSTBInfoAsyncTask.class.getSimpleName();

    public GetSTBInfoAsyncTask(GsoapCallback callback) {
        super(callback);
    }

    @Override
    JniResponse requestAPI(String... params) {
        String userID = verifyUserID(params[0]);
        String stbToken = verifyStbToken(params[1]);
        String clientInfo = GsoapUtils.getClientInfo(userID, stbToken);
        Log.getInstance().writeLog(TAG, "requestAPI", "clientInfo=" + clientInfo);
        return GsoapProxySingleton.getInstance().hpstb_GetSTBInfo(clientInfo);
    }
}
