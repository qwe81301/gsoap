package com.inspur.youlook.sdk.gsoap.asynctask;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.inspur.youlook.sdk.gsoap.GsoapConnectionSingleton;
import com.inspur.youlook.sdk.gsoap.GsoapProxySingleton;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallEventListener;
import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallback;
import com.inspur.youlook.sdk.gsoap.utils.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andyliu on 2016/10/14
 */
public class SearchSTBBySSDPAsyncTask extends AsyncTask<String, String, String> implements GsoapCallEventListener {

    public interface ReactEventListener {
        void sendEvent(String data);
    }

    private static final String TAG = SearchSTBBySSDPAsyncTask.class.getSimpleName();
    private GsoapCallback mCallback;
    private ReactEventListener mReactEventListener;
    private List<String> mDevicesList = new ArrayList<>();
    private boolean searchingEnabled = false;
    private int searchingTime = 0;
    private static final int SEARCHING_TIME_LIMIT = 2; // second （本來十秒會搜尋十次 我暫時改成兩秒）

    public SearchSTBBySSDPAsyncTask(GsoapCallback mCallback) {
        this.mCallback = mCallback;
    }

    public void setReactEventListener(ReactEventListener mReactEventListener) {
        this.mReactEventListener = mReactEventListener;
    }

    public boolean getSearchingEnabled() {
        return searchingEnabled;
    }

    public void stopSearching() {
        cancel(true);
        searchingEnabled = false;
    }
    
    public void resetSearchingTime() {
        searchingTime = 0;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        searchingEnabled = true;
        GsoapProxySingleton.getInstance().addGsoapCallEventListener(this);
    }

    @Override
    protected String doInBackground(String... params) {
        GsoapProxySingleton.getInstance().hpstb_StartSsdp();
        while (searchingEnabled && searchingTime < SEARCHING_TIME_LIMIT) {
            try {
                publishProgress();
                Thread.sleep(1000);
                searchingTime++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        GsoapProxySingleton.getInstance().hpstb_StopSsdp();
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        JsonArray jsonArray = new JsonArray();
        for (String devicesIP : mDevicesList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("devicesIP", devicesIP);
            jsonObject.addProperty("isConnected", (GsoapConnectionSingleton.getInstance().isConnected() && devicesIP.equalsIgnoreCase(GsoapConnectionSingleton.getInstance().getCurrentConnectionIP())));
            jsonArray.add(jsonObject);
        }
        if (mReactEventListener != null)
            mReactEventListener.sendEvent(jsonArray.toString());
    }

    @Override
    protected void onPostExecute(String result) {
        Log.getInstance().writeLog(TAG, "onPostExecute()", "Searching End.", Log.LogLevel.DEBUG);
        super.onPostExecute(result);
        GsoapProxySingleton.getInstance().removeGsoapCallEventListener(this);
        searchingEnabled = false;
        if (mCallback != null) {
            CallbackStatus callbackStatus = new CallbackStatus(0);
            mCallback.invoke(callbackStatus.getCode(), callbackStatus.getMessage());
        }
    }

    @Override
    protected void onCancelled() {
        Log.getInstance().writeLog(TAG, "onCancelled()", "Searching Cancelled.", Log.LogLevel.DEBUG);
        super.onCancelled();
        GsoapProxySingleton.getInstance().removeGsoapCallEventListener(this);
        searchingEnabled = false;
    }

    @Override
    public void onEventSend(int type, String... data) {
        if (type == 13) { //GSOAP SSDP
            if (data.length > 0) {
                String jsonString = data[0];
                GsoapSSDPDeviceBean gsoapSSDPDeviceBean = new Gson().fromJson(jsonString, GsoapSSDPDeviceBean.class);
                String ip = gsoapSSDPDeviceBean.getDeviceIp();
                if (isIpEnable(ip) && !mDevicesList.contains(ip)) {
                    mDevicesList.add(ip);
                    Log.getInstance().writeLog(TAG, "add", "ip = " + ip);
                }
            }
        }
    }

    private boolean isIpEnable(String ip) {
        return (ip != null && ip.length() != 0 && !ip.equalsIgnoreCase("0.0.0.0") && !ip.equalsIgnoreCase("000.000.000.000"));
    }

    class GsoapSSDPDeviceBean implements Parcelable {
        /**
         * deviceIp : 192.168.1.46
         * deviceName : inspurSTB
         * gsoapPort : 10000
         */

        private String deviceIp;
        private String deviceName;
        private int gsoapPort;

        public String getDeviceIp() {
            return deviceIp;
        }

        public void setDeviceIp(String deviceIp) {
            this.deviceIp = deviceIp;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public int getGsoapPort() {
            return gsoapPort;
        }

        public void setGsoapPort(int gsoapPort) {
            this.gsoapPort = gsoapPort;
        }

        protected GsoapSSDPDeviceBean(Parcel in) {
            deviceIp = in.readString();
            deviceName = in.readString();
            gsoapPort = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(deviceIp);
            dest.writeString(deviceName);
            dest.writeInt(gsoapPort);
        }

        @SuppressWarnings("unused")
        public final Parcelable.Creator<GsoapSSDPDeviceBean> CREATOR = new Parcelable.Creator<GsoapSSDPDeviceBean>() {
            @Override
            public GsoapSSDPDeviceBean createFromParcel(Parcel in) {
                return new GsoapSSDPDeviceBean(in);
            }

            @Override
            public GsoapSSDPDeviceBean[] newArray(int size) {
                return new GsoapSSDPDeviceBean[size];
            }
        };
    }
}
