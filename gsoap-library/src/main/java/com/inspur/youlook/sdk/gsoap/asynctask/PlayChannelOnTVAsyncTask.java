package com.inspur.youlook.sdk.gsoap.asynctask;

import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by andyliu on 2017/5/18.
 */
public class PlayChannelOnTVAsyncTask extends GsoapAsyncTask {

    private static final String TAG = PlayChannelOnTVAsyncTask.class.getSimpleName();

    public PlayChannelOnTVAsyncTask(GsoapCallback callback) {
        super(callback);
    }

    @Override
    JniResponse requestAPI(String... params) {
        String userID = verifyUserID(params[0]);
        String stbToken = verifyStbToken(params[1]);
        String clientInfo = GsoapUtils.getClientInfo(userID, stbToken);
        Log.getInstance().writeLog(TAG, "requestAPI", "clientInfo=" + clientInfo);
        String channelInfo = params[2];
        Log.getInstance().writeLog(TAG, "requestAPI", "channelInfo=" + channelInfo);
        return GsoapProxySingleton.getInstance().hpstb_PlayChannelOnTV(clientInfo, channelInfo);
    }
}
