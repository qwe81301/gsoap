package com.example.inspur.testgsoup;

import android.content.Context;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.gson.JsonObject;
import com.inspur.youlook.sdk.gsoap.GsoapConnectionSingleton;
import com.inspur.youlook.sdk.gsoap.asynctask.GetChannelClassificationAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetChannelListAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetSTBCapacityAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetSTBInfoAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GsoapAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GsoapConnectAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GsoapDisconnectAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.RequestEPGAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.RequestShareChannelAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.SearchSTBBySSDPAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.StopSharingChannelAsyncTask;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.utils.Constants;
import com.inspur.youlook.sdk.gsoap.utils.GsoapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by bear on 2017/8/17.
 */

public class MainPresenter implements IMainPresenter {


    private static final String CHANNEL_INFO_DATA ="[{\"chno\":2,\"name\":\"bb kuaibao\",\"tsid\":1,\"serviceid\":2,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"true\",\"type\":\"tv\",\"bat\":\"\"},{\"chno\":96,\"name\":\"琛涜鍚堝姝\",\"tsid\":1,\"serviceid\":96,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"true\",\"type\":\"tv\",\"bat\":\"\"},{\"chno\":225,\"name\":\"鍦嬪鍦扮悊楂樼暙璩噹鐢熼牷閬揺\",\"tsid\":1,\"serviceid\":333,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"true\",\"type\":\"tv\",\"bat\":\"\"},{\"chno\":230,\"name\":\"HBO Family 婧Θ瀹跺涵 HD\",\"tsid\":1,\"serviceid\":302,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"true\",\"type\":\"tv\",\"bat\":\"\"},{\"chno\":235,\"name\":\"Syfy 瓒呰嚜鐒剁骞婚牷閬搯\",\"tsid\":1,\"serviceid\":303,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"true\",\"type\":\"tv\",\"bat\":\"\"},{\"chno\":236,\"name\":\"Universal Channel 鐠扮悆褰卞妵闋婚亾\",\"tsid\":1,\"serviceid\":304,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"true\",\"type\":\"tv\",\"bat\":\"\"},{\"chno\":801,\"name\":\"CNS-TICKER\",\"tsid\":1,\"serviceid\":801,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"false\",\"type\":\"tv\",\"bat\":\"\"},{\"chno\":884,\"name\":\"CNS-AD\",\"tsid\":1,\"serviceid\":884,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"false\",\"type\":\"tv\",\"bat\":\"\"},{\"chno\":901,\"name\":\"CNS-PORTALDATA\",\"tsid\":1,\"serviceid\":901,\"freq\":729000,\"tunerid\":0,\"isHide\":\"false\",\"isFavor\":\"false\",\"isLock\":\"false\",\"isHD\":\"false\",\"type\":\"tv\",\"bat\":\"\"}]";
    private static final String TAG = "Gsoap";
    private static ExecutorService mCachedThreadPool;
    private static SearchSTBBySSDPAsyncTask mSearchSTBAsyncTask;
    private static ExecutorService mSingleThreadExecutor;

    private IMainActivity mMainActivity = null; //null 可以拿掉？ 現在這狀況下 有或沒有 會影響？
    private IMainModel mMainModel;
    private InvokeFlag mInvokeFlag = null;
    private ChannelListBlankFragment mChannelListBlankFragment = null;
    private EPGBlankFragment mEPGBlankFragment = null;

    private boolean mIsConnecting = false;
    private String mSearchDevicesIP;
    private String mVideoUrl;
    private String mVideoUrlIPPortArray;
    private String mVideoUrlIP;
    private String mVideoUrlPort;
    private List<String> mSearchIpList = new ArrayList<String>();

    public MainPresenter(IMainActivity mainActivity, Context context) {
        mMainActivity = mainActivity;
        mMainModel = new MainModel();
        //初始化
        init(context);
    }

    public MainPresenter(ChannelListBlankFragment mChannelListBlankFragment, Context context) {
        this.mChannelListBlankFragment = mChannelListBlankFragment;
        init(context);
    }
    public MainPresenter(EPGBlankFragment mEPGBlankFragment, Context context) {
        this.mEPGBlankFragment = mEPGBlankFragment;
        init(context);
    }

    //初始化
    private void init(Context context) {
        NetworkUtil.getInstance().init(context);
        GsoapConnectionSingleton.getInstance().init(context);

    }

    //拿到搜尋到的STB的IP List
    final SearchSTBBySSDPAsyncTask.ReactEventListener searchEventListener = new SearchSTBBySSDPAsyncTask.ReactEventListener() {
        @Override
        public void sendEvent(String data) {
            Log.v("iPListData", data);
            try {
                mSearchIpList.clear();
//                    data = [{"devicesIP":"172.16.129.98","isConnected":false},{"devicesIP":"172.16.129.44","isConnected":false}]
                JSONArray searchDevicesIpJsonArray = new JSONArray(data);
                for (int i = 0; i < searchDevicesIpJsonArray.length(); i++) {
                    JSONObject searchDevicesIpJsonObject = searchDevicesIpJsonArray.getJSONObject(i);
                    Log.v("searchDevicesIPObject", String.valueOf(searchDevicesIpJsonObject));
                    mSearchDevicesIP = searchDevicesIpJsonObject.getString("devicesIP");
                    Log.v("searchDevicesIP", mSearchDevicesIP);
                    mSearchIpList.add(mSearchDevicesIP);
                }
                //mSearchIpListPresenter: [172.16.129.98, 172.16.129.44]
                Log.v("mSearchIpListPresenter", String.valueOf(mSearchIpList));
                //傳拿到的IpList給Model
                mMainModel.setIpArrayList(mSearchIpList);// 為了做MediaViewActivity 暫時註解 到時候要解回來
                updateViewIpList();// 為了做MediaViewActivity 暫時註解 到時候要解回來
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //Presenter傳IpList給View
    @Override
    public void updateViewIpList() {
        List<String> ipList = mMainModel.getIpArrayList();
        Log.v("<String>mSearchIpList", String.valueOf(ipList));
        mMainActivity.updateIpList(ipList);
    }

    //收尋STB
    //
    //
    //homeplus/reactnative/modules/api/NativeGsoapModule.java
    @Override
    public void searchSTBDevices() {
        String localHostIp = NetworkUtil.getInstance().getLocalHostIp();
        Log.v("searchSTBLocalHostIp", localHostIp);
        startSearch(localHostIp, gsoapCallback, searchEventListener);
    }

    //inspur/youlook/sdk/gsoap/GsoapFacade.java
    public void startSearch(String localHostIp, GsoapCallback callback, SearchSTBBySSDPAsyncTask.ReactEventListener eventListener) {
        if (mSearchSTBAsyncTask != null && mSearchSTBAsyncTask.getSearchingEnabled()) {
            mSearchSTBAsyncTask.resetSearchingTime();
            if (callback != null) {
                callback.invoke(-1, "搜尋中");
            }
        } else if (localHostIp != null) {
            Log.v("localHostIp", localHostIp);
            mSearchSTBAsyncTask = new SearchSTBBySSDPAsyncTask(callback);//拿到 ip and port
            mSearchSTBAsyncTask.setReactEventListener(eventListener);//拿到搜尋到的STB的IP List
            mSearchSTBAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, localHostIp);
        }
    }

    //停止收尋STB(暫時沒使用)
    //
    //
    @Override
    public void stopSearchSTBDevices() {
        com.inspur.youlook.sdk.gsoap.utils.Log.getInstance().writeLog(TAG, "stopSearchSTBDevices", "");
        stopSearching();
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


    //連線STB
    //
    //
    @Override
    public void connectToSTBDevice(String deviceIP, String userID) {
        mInvokeFlag = InvokeFlag.CONNECT_TO_STB_DEVICE_INVOKE_FLAG;
        String uuid = userID;
        com.inspur.youlook.sdk.gsoap.utils.Log.getInstance().writeLog(TAG, "connectToSTBDevice", "mDeviceIP=" + deviceIP + ", userID=" + userID);
        GsoapConnectionSingleton.getInstance().connect(deviceIP, userID, uuid, "", gsoapCallback);
    }

    //連線STB（驗證碼）
    //
    //
    @Override
    public void connectToSTBDeviceByJSON(String verifyCode) {
//        ReadableNativeMap dataNativeMap = (ReadableNativeMap) dataObject;
//        HashMap hashMap = dataNativeMap.toHashMap();
//        Log.getInstance().writeLog(TAG, "connectToSTBDeviceByJSON", "hashMap="+ hashMap);
//        String deviceIP = (String)hashMap.get("deviceIP");
//        String userID = (String)hashMap.get("userID");
//        String verifyCode = (String)hashMap.get("verifyCode");
//        mGsoapProxy.connect(deviceIP, userID, userID, verifyCode, gsoapCallback);
        mInvokeFlag = InvokeFlag.CONNECT_TO_STB_DEVICE_BY_JSON;
        GsoapConnectionSingleton.getInstance().connect("172.16.129.44", "userID_verify", "userID_verify", verifyCode, gsoapCallback);

    }





    //斷開STB
    //
    //
    @Override
    public void disconnectToSTBDevice(String userID, String stbToken) {
        com.inspur.youlook.sdk.gsoap.utils.Log.getInstance().writeLog(TAG, "disconnectToSTBDevice", "userID=" + userID + ", stbToken=" + stbToken);
        disconnect(gsoapCallback);
    }

    public void disconnect(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary
        executeAsyncTask(new GsoapDisconnectAsyncTask(callback), null, null);
    }

    private void executeAsyncTask(GsoapAsyncTask asyncTask, String... params) {
        if (mCachedThreadPool == null) {//沒有ThreadPool的話，新創一個
            mCachedThreadPool = Executors.newCachedThreadPool();
        }
        asyncTask.executeOnExecutor(mCachedThreadPool, params);
    }

    //取得機頂盒的資訊。
    //
    //ReactCallbackWrapper->invoke]: object={"hwver":"01.00","swver":"05.02 Build 2016-12-13 21:41:54 build 4665","sn":"11011603240000003","mac":"bc:20:ba:87:49:6f","devicename":"Inspur STB","operator":"SXGD"}
    @Override
    public void getSTBDeviceInfo(String userID, String stbToken) {
        mInvokeFlag = InvokeFlag.GET_STB_DEVICE_INFO_INVOKE_FLAG;
        Log.v(TAG, "getSTBDeviceInfo  userID=" + userID + ", stbToken=" + stbToken);
        getSTBInfo(gsoapCallback);
    }

    public void getSTBInfo(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetSTBInfoAsyncTask(callback), null, null);
    }

    //取得機頂盒的capacity資訊。
    //
    // object={"tunernum":4,"transcode":[{"type":"h264","resolutions":["640*480","352*288","176*144"]}],"audiodecode":[{"type":"aac"},{"type":"ac3"},{"type":"mpeg2"},{"type":"mpeg1"}],"videodecode":[{"type":"h265"},{"type":"h264"},{"type":"mpeg2"},{"type":"mpeg1"}],"gatewaymanage":"true"}
    @Override
    public void getCapacityOnSTBDevice(String userID, String stbToken) {
        mInvokeFlag = InvokeFlag.GET_CAPACITY_ON_STB_DEVICE_INVOKE_FLAG;
        Log.v(TAG, "getCapacityOnSTBDevice  userID=" + userID + ", stbToken=" + stbToken);
        getSTBCapacity(gsoapCallback);
    }

    public void getSTBCapacity(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetSTBCapacityAsyncTask(callback), null, null);
    }

    //取得機頂盒的頻道清單​。
    //
    //
    @Override
    public void getChannelInfoOnSTBDevice(String userID, String stbToken) {
        mInvokeFlag = InvokeFlag.GET_CHANNEL_INFO_ON_STB_DEVICE_INVOKE_FLAG;
        Log.v(TAG, "getChannelInfoOnSTBDevice  userID=" + userID + ", stbToken=" + stbToken);
        getChannelList(gsoapCallback);
    }

    public void getChannelList(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetChannelListAsyncTask(callback), null, null);
    }

    //取得機頂盒頻道的Classification 中文翻作 分類 是指頻道的分級嗎？ 只有編號 和 頻道名稱 感覺也不太像是分級的意思
    //[{"bat":1,"name":"央视频道"}, ... ,{"bat":2,"name":"北京频道"}]
    //
    @Override
    public void getChannelClassification(String userID, String stbToken) {
        mInvokeFlag = InvokeFlag.GET_CHANNEL_CLASSIFICATION_INVOKE_FLAG;
        Log.v(TAG, "getChannelClassification    userID=" + userID + ", stbToken=" + stbToken);
        getChannelClassification(gsoapCallback);
    }

    public void getChannelClassification(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetChannelClassificationAsyncTask(callback), null, null);
    }

    //Request EPG of STB.//電子節目指南（英語：Electronic program guide，縮寫：EPG）
    //
    //
    @Override
    public void requestEPG(String userID, String stbToken, int channelFreq, int channelTsid, int channelServiceId) {
        mInvokeFlag = InvokeFlag.REQUEST_EPG_INVOKE_FLAG;
        Log.v(TAG, "requestEPG   userID=" + userID + ", stbToken=" + stbToken);
        Log.v(TAG, "requestEPG   channelFreq=" + channelFreq + ", channelTsid=" + channelTsid + ", channelServiceId=" + channelServiceId);
        String channelInfo = GsoapUtils.getChannelInfo(channelFreq, channelTsid, channelServiceId);
        requestEPG(channelInfo, gsoapCallback);
    }
    public void requestEPG(String channelInfo, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new TestRequestEPGAsyncTask( this, callback), null, null, channelInfo);
    }
    
    //機頂盒開始對外分享直播。
    //
    //
    @Override
    public void startShareVideoOnSTBDevice(String userID, String stbToken, int channelFreq, int channelTsid, int channelServiceId, String protocolType, String codec, String res) {
        mInvokeFlag = InvokeFlag.START_SHARE_VIDEO_ON_STB_DEVICE_INVOKE_FLAG;
        Log.v(TAG, "userID=" + userID + ", stbToken=" + stbToken);
        Log.v(TAG, "channelFreq=" + channelFreq + ", channelTsid=" + channelTsid + ", channelServiceId=" + channelServiceId);
        Log.v(TAG, "protocolType=" + protocolType + ", codec=" + codec + ", res=" + res);
        requestShareChannel(channelFreq, channelTsid, channelServiceId, protocolType, codec, res, gsoapCallback);
    }

    public void requestShareChannel(int channelFreq, int channelTsid, int channelServiceId, String protocolType, String codec, String res, GsoapCallback callback) {
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
        this.requestShareChannel(requestInfo, callback);
    }

    public void requestShareChannel(String requestInfo, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeSingleAsyncTask(new RequestShareChannelAsyncTask(callback), null, null, requestInfo);
    }

    private void executeSingleAsyncTask(GsoapAsyncTask asyncTask, String... params) {
        if (mSingleThreadExecutor == null) {
            mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        }
        asyncTask.executeOnExecutor(mSingleThreadExecutor, params);
    }

    //機頂盒停止對外分享直播。
    //
    //
    @Override
    public void stopShareVideoOnSTBDevice(String userID, String stbToken, int channelFreq, int channelTsid, int channelServiceId) {
        Log.v(TAG, "userID=" + userID + ", stbToken=" + stbToken);
        Log.v(TAG, "channelFreq=" + channelFreq + ", channelTsid=" + channelTsid + ", channelServiceId=" + channelServiceId);
        stopSharingChannel(channelFreq, channelTsid, channelServiceId, gsoapCallback);
    }

    public void stopSharingChannel(int channelFreq, int channelTsid, int channelServiceId, GsoapCallback callback) {
        String channelInfo = GsoapUtils.getChannelInfo(channelFreq, channelTsid, channelServiceId);
        stopSharingChannel(channelInfo, callback);
    }

    public void stopSharingChannel(String channelInfo, GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeSingleAsyncTask(new StopSharingChannelAsyncTask(callback), null, null, channelInfo);
    }

    //開始錄製機頂盒分享直播影片的.ts碼流。
    //
    //

    public void startSaveVideo() {
        Thread saveVideoThread = new Thread(saveVideoRunnable);
        saveVideoThread.start();
    }

    public Runnable saveVideoRunnable = new Runnable() {
        public void run() {
            saveVideoStreaming(mVideoUrlIP, Integer.parseInt(mVideoUrlPort), mMainActivity.getSaveVideoPath());//getObbDir().getAbsolutePath()
        }
    };

    @Override
    public void saveVideoStreaming(String ip, int port, String filePath) {
        try {
            Socket socket = new Socket(ip, port);
            OutputStream out = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            String content = "GET / HTTP/1.1\r\nUser-Agent: ExoPlayerDemo/2.4.0 (Linux;Android 4.3) ExoPlayerLib/2.4.0\r\nAccept-Encoding: identity\r\nHost: 192.168.201.1:8095\r\nConnection: Keep-Alive\r\n\r\n";
            out.write(content.getBytes());
            String videoPath = filePath;
            Log.v("videoPath", videoPath);
            //  getFilesDir().getPath() 找不到也沒有成功寫入  /data/user/0/com.example.inspur.testgsoup/files
            //  getObbDir().getPath()能找到 雖然videoPath是寫 = /storage/emulated/0/Android/obb/com.example.inspur.testgsoup 不過實際位置 在/sdcard/Android/obb/com.example.inspur.testgsoup/2017-08-24-18:09:39.ts
            Calendar mCalendar = Calendar.getInstance();
            CharSequence timeFileName = DateFormat.format("yyyy-MM-dd kk:mm:ss", mCalendar.getTime());
            String fileName = String.valueOf(timeFileName);
            File file = new File(videoPath, fileName + ".ts");
            OutputStream output = new FileOutputStream(file);
            Log.v("output", String.valueOf(output));
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            output.close();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private enum InvokeFlag {
        CONNECT_TO_STB_DEVICE_INVOKE_FLAG,
        CONNECT_TO_STB_DEVICE_BY_JSON,
        GET_STB_DEVICE_INFO_INVOKE_FLAG,
        GET_CAPACITY_ON_STB_DEVICE_INVOKE_FLAG,
        GET_CHANNEL_INFO_ON_STB_DEVICE_INVOKE_FLAG,
        GET_CHANNEL_CLASSIFICATION_INVOKE_FLAG,
        REQUEST_EPG_INVOKE_FLAG,
        START_SHARE_VIDEO_ON_STB_DEVICE_INVOKE_FLAG,
    }


    GsoapCallback gsoapCallback = new GsoapCallback() {
        @Override
        public void invoke(Object... args) {
            if (gsoapCallback != null) {

                int statusCode = (int) args[0];
//                String statusMsg = (String) args[1];
                Log.v("gsoapCallback", "statusCode1=" + args[0] + ", statusMsg=" + args[1]);
                Object object = null;
                if(statusCode == 2016){//verifyCode
                    mMainActivity.showVerifyEditDialog();
                }
                if (args.length == 3) {
                    Log.v("gsoapCallback args[2]", "object =" + args[2]);
//                    setTextGsoapCallbackObject(String.valueOf(args[2]));
                    //data = [{"devicesIP":"172.16.129.44","isConnected":false}]
                    Log.v("mInvokeFlag", String.valueOf(mInvokeFlag));
                    switch (mInvokeFlag) {
                        case CONNECT_TO_STB_DEVICE_INVOKE_FLAG:
                            Log.v("connectToSTBDeviceFlag", "object =" + args[2]);
                            break;
                        case CONNECT_TO_STB_DEVICE_BY_JSON:
                            Log.v("connectToSTBDeviceJson", "object =" + args[2]);
                            break;
                        case GET_STB_DEVICE_INFO_INVOKE_FLAG:
                            Log.v("getSTBInfoInvokeFlag", "object =" + args[2]);
                            setTextGsoapCallbackObject(String.valueOf(args[2]));
                            break;
                        case GET_CAPACITY_ON_STB_DEVICE_INVOKE_FLAG:
                            Log.v("capacitySTBInvokeFlag", "object =" + args[2]);
                            setTextGsoapCallbackObject(String.valueOf(args[2]));
                            break;
                        case GET_CHANNEL_INFO_ON_STB_DEVICE_INVOKE_FLAG:
                            Log.v("channelSTBInvokeFlag", "object =" + args[2]);
//                            setTextGsoapCallbackObject(String.valueOf(args[2]));// 為了做MediaViewActivity 暫時註解 到時候要解回來
                            setJsonChannelList(String.valueOf(CHANNEL_INFO_DATA));//因為原始資料有編碼不同(有簡體字)導致缺少"t"的問題，所以先用寫死資料方法解決
                            break;
                        case GET_CHANNEL_CLASSIFICATION_INVOKE_FLAG:
                            Log.v("ClassificationFlag", "object =" + args[2]);
                            setTextGsoapCallbackObject(String.valueOf(args[2]));
                            break;
                        case REQUEST_EPG_INVOKE_FLAG:
                            Log.v("requestEPGFlag", "object =" + args[2]);
//                            setTextGsoapCallbackObject(String.valueOf(args[2]));// 為了做MediaViewActivity 暫時註解 到時候要解回來
//                            setJsonEPG(String.valueOf(args[2]));
                            break;
                        case START_SHARE_VIDEO_ON_STB_DEVICE_INVOKE_FLAG:
                            try {
                                mVideoUrl = new JSONObject((String) args[2]).getString("url");//{"url":"http://172.16.129.98:8095","channel":{"freq":729000,"tsid":1,"serviceid": 2,"tveid":0}}
                                Log.v("gsoapCallback", "videoUrl=" + mVideoUrl);
                                //mVideoUrl = http://172.16.129.44:8095
                                //做兩次字串切割 http://172.16.129.44:8095 變成 http://   172.16.129.44:8095
                                String[] videoUrlHttpAllArray = mVideoUrl.split("\\/\\/");
                                mVideoUrlIPPortArray = videoUrlHttpAllArray[1];
                                Log.v("gsoapCallback", "mVideoUrlIPPortArray=" + mVideoUrlIPPortArray);
                                String[] videoUrlIPPortArray = mVideoUrlIPPortArray.split(":");
                                // 取得分割字串 來取IP 和 Port號
                                mVideoUrlIP = videoUrlIPPortArray[0];
                                mVideoUrlPort = videoUrlIPPortArray[1];
                                Log.v("gsoapCallback", "mVideoUrlIP=" + mVideoUrlIP + "  mVideoUrlPort=" + mVideoUrlPort);
                                //當mVideoUrlIP 和 mVideoUrlPort都有時 觸發錄製碼流
                                if (mVideoUrlIP != null && mVideoUrlPort != null) {
                                    startSaveVideo();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            Log.v("gsoapCallback default:", "switch default");
                    }
                }
                //mCallback.invoke(statusCode, statusMsg, object);
            } else {
                Log.v("gsoapCallback", "Callback is null.");
            }
        }
    };

    //set get GsoapCallbackObject  arg[2] 傳回view顯示
    private void setTextGsoapCallbackObject(String arg) {
        mMainActivity.printGsoapCallbackObject(arg);
    }

    private void setJsonChannelList(String channelList) {
        mChannelListBlankFragment.dismantleJsonChannelList(channelList);
    }

    public void setJsonEPG(String epg) {
        mEPGBlankFragment.dismantleJsonEPG(epg);
    }



}

