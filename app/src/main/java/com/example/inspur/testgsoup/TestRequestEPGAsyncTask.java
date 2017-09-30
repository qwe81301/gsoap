package com.example.inspur.testgsoup;

import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.asynctask.CallbackStatus;
import com.inspur.youlook.sdk.gsoap.asynctask.RequestEPGAsyncTask;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallEventListener;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.utils.Log;

/**
 * Created by bear on 2017/09/30.
 */

public class TestRequestEPGAsyncTask extends RequestEPGAsyncTask implements GsoapCallEventListener {

    private static final String TAG = TestRequestEPGAsyncTask.class.getSimpleName();


    private IMainPresenter mMainPresenter;
    public TestRequestEPGAsyncTask(IMainPresenter mMainPresenter, GsoapCallback callback) {
        super(callback);
        this.mMainPresenter = mMainPresenter;
    }

    public TestRequestEPGAsyncTask(GsoapCallback callback) {
        super(callback);
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
            Log.getInstance().writeLog(TAG, "data", String.valueOf(data));
            Log.getInstance().writeLog(TAG, "data", data[0]);
            mMainPresenter.setJsonEPG(data[0]);
        }

    }
}
