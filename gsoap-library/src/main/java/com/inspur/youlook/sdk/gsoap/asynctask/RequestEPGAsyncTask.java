package com.inspur.youlook.sdk.gsoap.asynctask;

import com.google.gson.Gson;
import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallEventListener;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapConnectionListener;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;
import com.inspur.youlook.sdk.gsoap.utils.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by andyliu on 2017/5/18.
 */
public class RequestEPGAsyncTask extends GsoapAsyncTask implements GsoapCallEventListener {

    private static final String TAG = RequestEPGAsyncTask.class.getSimpleName();


    public RequestEPGAsyncTask(GsoapCallback callback) {
        super(callback);
    }

    @Override
    JniResponse requestAPI(String... params) {
        String userID = verifyUserID(params[0]);
        String stbToken = verifyStbToken(params[1]);
        String clientInfo = GsoapUtils.getClientInfo(userID, stbToken);
        Log.getInstance().writeLog(TAG, "requestAPI", "clientInfo=" + clientInfo);
        String requestInfo = params[2];
        Log.getInstance().writeLog(TAG, "requestAPI", "requestInfo=" + requestInfo);
        return GsoapProxySingleton.getInstance().hpstb_RequestEPG(clientInfo, requestInfo);
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        GsoapProxySingleton.getInstance().addGsoapCallEventListener(this);
    }

    @Override
    protected CallbackStatus doInBackground(String... params) {
        return super.doInBackground(params);

    }

    @Override
    protected void onPostExecute(CallbackStatus status) {
        super.onPostExecute(status);
//        GsoapProxySingleton.getInstance().removeGsoapCallEventListener(this);

    }

    @Override
    public void onEventSend(int eventType, String... data) {
        if (eventType == 0) { //GSOAP hpstb.event.type.h HPSTBEvent_ArriveEPG,//7days EPG
            Log.getInstance().writeLog(TAG,"data", String.valueOf(data));
//            if (data.length > 0) {
//                String jsonString = data[0];
//                SearchSTBBySSDPAsyncTask.GsoapSSDPDeviceBean gsoapSSDPDeviceBean = new Gson().fromJson(jsonString, SearchSTBBySSDPAsyncTask.GsoapSSDPDeviceBean.class);
////                String ip = gsoapSSDPDeviceBean.getDeviceIp();
////                if (isIpEnable(ip) && !mDevicesList.contains(ip)) {
////                    mDevicesList.add(ip);
////                    Log.getInstance().writeLog(TAG, "add", "ip = " + ip);
////                }
//            }
        }

    }
}
