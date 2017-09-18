package com.example.inspur.testgsoup;

import android.content.Context;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.gson.JsonObject;
import com.inspur.youlook.sdk.gsoap.GsoapConnectionSingleton;
import com.inspur.youlook.sdk.gsoap.asynctask.GetChannelListAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetSTBCapacityAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GetSTBInfoAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GsoapAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GsoapConnectAsyncTask;
import com.inspur.youlook.sdk.gsoap.asynctask.GsoapDisconnectAsyncTask;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by inspur on 8/17/17.
 */

public class MainPresenter implements IMainPresenter {

    private static final String TAG = "Gsoap";
    private IMainActivity mMainActivity = null; //null 可以拿掉？ 現在這狀況下 有或沒有 會影響？
    private IMainModel mMainModel;

    public MainPresenter(IMainActivity activity, Context context) { //? 改成MainActivity activity也可以run
        mMainActivity = activity;
        mMainModel = new MainModel();

        //初始化
        NetworkUtil.getInstance().init(context);
        GsoapConnectionSingleton.getInstance().init(context);

    }

    private String mSearchDevicesIP;
    private String mVideoUrl;
    private String mVideoUrlIPPortArray;
    private String mVideoUrlIP;
    private String mVideoUrlPort;

    List<String> mSearchIpList = new ArrayList<String>();

    private static ExecutorService mCachedThreadPool;
    private static SearchSTBBySSDPAsyncTask mSearchSTBAsyncTask;
    private static ExecutorService mSingleThreadExecutor;
    private boolean mIsConnecting = false;


    private String mInvokeFlag = null;



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
                mMainModel.setIpArrayList(mSearchIpList);
                updateViewIpList();
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

    //停止收尋STB(暫時沒用)
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
        mInvokeFlag = "connectToSTBDeviceInvokeFlag";
        com.inspur.youlook.sdk.gsoap.utils.Log.getInstance().writeLog(TAG, "connectToSTBDevice", "mDeviceIP=" + deviceIP + ", userID=" + userID);
        connect(deviceIP, userID, gsoapCallback);
    }

    public void connect(String deviceIP, String userID, GsoapCallback callback) {
        com.inspur.youlook.sdk.gsoap.utils.Log.getInstance().writeLog(TAG, "connect", "mDeviceIP=" + deviceIP + ", userID=" + userID);
        if (userID == null || userID.equals("") || deviceIP == null || deviceIP.equals("")) {
            int statusCode = Constants.RN_STATUS_PARAMETER_ERROR;
            String statusMsg = Constants.RN_STATUS_CODE_MAP.get(statusCode);
            if (callback != null)
                callback.invoke(statusCode, statusMsg);
            return;
        }
        if (!mIsConnecting) {//連接上進入此行
            String clientInfo = "{\"userid\":\"" + userID + "\",\"uuid\":\"" + userID + "\"}";
            new GsoapConnectAsyncTask(deviceIP, clientInfo, callback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            com.inspur.youlook.sdk.gsoap.utils.Log.getInstance().writeLog(TAG, "connect", "Failed! Because: is connecting.");
        }
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
        mInvokeFlag = "getSTBDeviceInfoInvokeFlag";
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
        mInvokeFlag = "getCapacityOnSTBDeviceInvokeFlag";
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
        mInvokeFlag = "getChannelInfoOnSTBDeviceInvokeFlag";
        Log.v(TAG, "getChannelInfoOnSTBDevice  userID=" + userID + ", stbToken=" + stbToken);
        getChannelList(gsoapCallback);
    }

    public void getChannelList(GsoapCallback callback) {
        // TODO userID & stbToken are not necessary.
        executeAsyncTask(new GetChannelListAsyncTask(callback), null, null);
    }

    //取得機頂盒頻道的Classification 中文翻作 分類 是指頻道的分級嗎？
    //
    //

    //機頂盒開始對外分享直播。
    //
    //
    @Override
    public void startShareVideoOnSTBDevice(String userID, String stbToken, int channelFreq, int channelTsid, int channelServiceId, String protocolType, String codec, String res) {
        mInvokeFlag = "startShareVideoOnSTBDeviceInvokeFlag";
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
//            saveVideoStreaming(mVideoUrlIP, Integer.parseInt(mVideoUrlPort), mSaveVideoRunnablePath);//getObbDir().getAbsolutePath()
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


    GsoapCallback gsoapCallback = new GsoapCallback() {
        @Override
        public void invoke(Object... args) {
            if (gsoapCallback != null) {
                int statusCode = (int) args[0];
                String statusMsg = (String) args[1];
                Log.v("gsoapCallback", "statusCode=" + args[0] + ", statusMsg=" + args[1]);
                Object object = null;
                if (args.length == 3) {
                    Log.v("gsoapCallback args[2]", "object =" + args[2]);
//                    setGsoapCallbackObject(String.valueOf(args[2]));
                    //data = [{"devicesIP":"172.16.129.44","isConnected":false}]
                    Log.v("mInvokeFlag", mInvokeFlag);
                    switch (mInvokeFlag) {
                        case "connectToSTBDeviceInvokeFlag":
                            Log.v("connectToSTBDeviceFlag", "object =" + args[2]);
                            break;
                        case "getSTBDeviceInfoInvokeFlag":
                            Log.v("getSTBInfoInvokeFlag", "object =" + args[2]);
                            setGsoapCallbackObject(String.valueOf(args[2]));
                            break;
                        case "getCapacityOnSTBDeviceInvokeFlag":
                            Log.v("capacitySTBInvokeFlag", "object =" + args[2]);
                            setGsoapCallbackObject(String.valueOf(args[2]));
                            break;
                        case "getChannelInfoOnSTBDeviceInvokeFlag":
                            Log.v("channelSTBInvokeFlag", "object =" + args[2]);
                            setGsoapCallbackObject(String.valueOf(args[2]));
                            break;
                        case "startShareVideoOnSTBDeviceInvokeFlag":
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
    public void setGsoapCallbackObject(String arg) {
        mMainActivity.getGsoapCallbackObject(arg);
    }




}
