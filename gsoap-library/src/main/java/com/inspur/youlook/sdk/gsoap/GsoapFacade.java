package com.inspur.youlook.sdk.gsoap;

import android.os.AsyncTask;

import com.google.gson.JsonObject;
import com.inspur.youlook.sdk.gsoap.asynctask.GetChannelClassificationAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetChannelListAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetClientRoleAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetMasterClientAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetSTBCapacityAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetSTBInfoAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GsoapAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GsoapDisconnectAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.HandoverMasterRoleAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.ParentPasswordCheckAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.PlayChannelOnTVAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.RequestBindAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.RequestEPGAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.RequestPFAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.RequestShareChannelAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.SearchSTBBySSDPAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.SendKeyAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.SetClientRoleAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.StopSharingChannelAsyncTask;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by andyliu on 2017/6/19.
 */

public class GsoapFacade {

    private static SearchSTBBySSDPAsyncTask mSearchSTBAsyncTask;
    private static ExecutorService mCachedThreadPool;
    private static ExecutorService mSingleThreadExecutor;

    public void startSearch(String localHostIp, GsoapCallback callback, SearchSTBBySSDPAsyncTask.ReactEventListener eventListener) {
        if (mSearchSTBAsyncTask != null && mSearchSTBAsyncTask.getSearchingEnabled()) {
            mSearchSTBAsyncTask.resetSearchingTime();
            if (callback != null) {
                callback.invoke(-1, "搜尋中");
            }
        } else {
            if (localHostIp != null) {
                mSearchSTBAsyncTask = new SearchSTBBySSDPAsyncTask(callback);
                mSearchSTBAsyncTask.setReactEventListener(eventListener);//手機對機頂盒發出 callback，同時也發出一個廣播監聽，監聽有沒有機頂盒收到並回應
                mSearchSTBAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, localHostIp); //可能收尋到多個stb 多個stb 都回傳ip 所以用執行緒池方便管理
            }
        }
    }

    public void stopSearching() {
        if (mSearchSTBAsyncTask != null) {
            if (mSearchSTBAsyncTask.getSearchingEnabled()) {
                mSearchSTBAsyncTask.stopSearching();
            }
            if (!mSearchSTBAsyncTask.isCancelled()) {
                mSearchSTBAsyncTask.cancel(true);
            }
            mSearchSTBAsyncTask = null;
        }
    }

    public void connect(String deviceIP, String userID, GsoapCallback callback) {
        GsoapConnectionSingleton.getInstance().connect(deviceIP, userID, callback);
    }

    public void disconnect(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary
        executeAsyncTask(new GsoapDisconnectAsyncTask(callback), null, null);
    }

    public void requestBindJID(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new RequestBindAsyncTask(callback), null, null);
    }

    public void handoverMasterRole(String anotherUserID, String anotherIP, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new HandoverMasterRoleAsyncTask(callback), null, null, anotherUserID, anotherIP);
    }

    public void getMasterClient(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetMasterClientAsyncTask(callback), null, null);
    }

    public void setClientRole(String role, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new SetClientRoleAsyncTask(callback), null, null, role);
    }

    public void getClientRole(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetClientRoleAsyncTask(callback), null, null);
    }

    public void getSTBInfo(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetSTBInfoAsyncTask(callback), null, null);
    }

    public void getSTBCapacity(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetSTBCapacityAsyncTask(callback), null, null);
    }

    public void sendKey(String keyCode, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeSingleAsyncTask(new SendKeyAsyncTask(callback), null, null, keyCode);
    }

    public void getChannelList(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetChannelListAsyncTask(callback), null, null);
    }

    public void getChannelClassification(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetChannelClassificationAsyncTask(callback), null, null);
    }

    public void requestPF(int channelFreq, int channelTsid, int channelServiceId, GsoapCallback callback) {
        String channelInfo = GsoapUtils.getChannelInfo(channelFreq, channelTsid, channelServiceId);
        requestPF(channelInfo, callback);
    }

    public void requestPF(String channelInfo, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new RequestPFAsyncTask(callback), null, null, channelInfo);
    }

    public void requestEPG(int channelFreq, int channelTsid, int channelServiceId, GsoapCallback callback) {
        String channelInfo = GsoapUtils.getChannelInfo(channelFreq, channelTsid, channelServiceId);
        requestEPG(channelInfo, callback);
    }

    public void requestEPG(String channelInfo, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new RequestEPGAsyncTask(callback), null, null, channelInfo);
    }

    public void playChannelOnTV(int channelFreq, int channelTsid, int channelServiceId, GsoapCallback callback) {
        String channelInfo = GsoapUtils.getChannelInfo(channelFreq, channelTsid, channelServiceId);
        playChannelOnTV(channelInfo, callback);
    }

    public void playChannelOnTV(String channelInfo, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new PlayChannelOnTVAsyncTask(callback), null, null, channelInfo);
    }

    public void requestShareChannel(int channelFreq, int channelTsid, int channelServiceId,
                                    String protocolType, String codec, String res, GsoapCallback callback) {
        JsonObject channelInfoObject = GsoapUtils.getChannelInfoJsonObject(channelFreq, channelTsid, channelServiceId);

        JsonObject codecObject = new JsonObject();
        codecObject.addProperty("type", codec);
        codecObject.addProperty("res", res);

        JsonObject protocolJsonObject = new JsonObject();
        protocolJsonObject.addProperty("type", protocolType);
        protocolJsonObject.addProperty("port", "default");

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("channel", channelInfoObject);
        jsonObject.add("codec", codecObject);
        jsonObject.add("protocol", protocolJsonObject);
        jsonObject.addProperty("encrypt", "false");

        String requestInfo = jsonObject.toString();
        requestShareChannel(requestInfo, callback);
    }

    public void requestShareChannel(String requestInfo, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeSingleAsyncTask(new RequestShareChannelAsyncTask(callback), null, null, requestInfo);
    }

    public void stopSharingChannel(int channelFreq, int channelTsid, int channelServiceId, GsoapCallback callback) {
        String channelInfo = GsoapUtils.getChannelInfo(channelFreq, channelTsid, channelServiceId);
        stopSharingChannel(channelInfo, callback);
    }

    public void stopSharingChannel(String channelInfo, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeSingleAsyncTask(new StopSharingChannelAsyncTask(callback), null, null, channelInfo);
    }

    public void verifyParentPassword(String passwordInfo, GsoapCallback callback) {
        executeAsyncTask(new ParentPasswordCheckAsyncTask(callback), passwordInfo);
    }

    private void executeAsyncTask(GsoapAsyncTask asyncTask, String... params) {
        if (mCachedThreadPool == null) {
            mCachedThreadPool = Executors.newCachedThreadPool();
        }
        asyncTask.executeOnExecutor(mCachedThreadPool, params);
    }

    private void executeSingleAsyncTask(GsoapAsyncTask asyncTask, String... params) {
        if (mSingleThreadExecutor == null) {
            mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        }
        asyncTask.executeOnExecutor(mSingleThreadExecutor, params);
    }

    public void shutdownThread() {
        if (mSingleThreadExecutor != null && !mSingleThreadExecutor.isShutdown()) {
            mSingleThreadExecutor.shutdownNow();
            mSingleThreadExecutor = null;
        }
        if (mCachedThreadPool != null && !mCachedThreadPool.isShutdown()) {
            mCachedThreadPool.shutdownNow();
            mCachedThreadPool = null;
        }

    }
}
