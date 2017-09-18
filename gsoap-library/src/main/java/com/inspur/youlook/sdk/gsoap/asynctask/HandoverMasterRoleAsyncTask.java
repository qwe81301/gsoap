package com.inspur.youlook.sdk.gsoap.asynctask;

import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2017/5/18.
 */

public class HandoverMasterRoleAsyncTask extends GsoapAsyncTask {

    private static final String TAG = HandoverMasterRoleAsyncTask.class.getSimpleName();

    public HandoverMasterRoleAsyncTask(GsoapCallback callback) {
        super(callback);
    }

    @Override
    JniResponse requestAPI(String... params) {
        String userID = verifyUserID(params[0]);
        String stbToken = verifyStbToken(params[1]);
        String anotherUserID = params[2];
        String anotherIP = params[3];
        String clientInfo = GsoapUtils.getClientInfo(userID, stbToken);
        String anotherClientInfo = GsoapUtils.getAnotherClientInfo(anotherUserID, anotherIP);
        Log.getInstance().writeLog(TAG, "requestAPI", "clientInfo=" + clientInfo);
        Log.getInstance().writeLog(TAG, "requestAPI", "anotherClientInfo=" + anotherClientInfo);
        return GsoapProxySingleton.getInstance().hpstb_HandoverMasterRole(clientInfo, anotherClientInfo);
    }
}
