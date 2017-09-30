package com.example.inspur.testgsoup;

import android.content.Context;

import com.inspur.youlook.sdk.gsoap.asynctask.SearchSTBBySSDPAsyncTask;
import com.inspur.youlook.sdk.gsoap.interfaces.*;

import java.util.List;

/**
 * Created by inspur on 8/17/17.
 */

public interface IMainPresenter {

    void searchSTBDevices();
    void stopSearchSTBDevices();
    void connectToSTBDevice(String deviceIP, String userID);
    void connectToSTBDeviceByJSON(String verifyCode);
    void disconnectToSTBDevice(String userID, String stbToken);
    void getSTBDeviceInfo(String userID, String stbToken);
    void getCapacityOnSTBDevice(String userID, String stbToken);
    void getChannelInfoOnSTBDevice(String userID, String stbToken);
    void getChannelClassification(String userID, String stbToken);
    void requestEPG(String userID, String stbToken, int channelFreq, int channelTsid, int channelServiceId);
    void startShareVideoOnSTBDevice(String userID, String stbToken, int channelFreq, int channelTsid, int channelServiceId,
                                           String protocolType, String codec, String res);
    void stopShareVideoOnSTBDevice(String userID, String stbToken, int channelFreq, int channelTsid, int channelServiceId);
    void saveVideoStreaming(String ip, int port,String filePath);
    void updateViewIpList();
    void setJsonEPG(String epg);


}
