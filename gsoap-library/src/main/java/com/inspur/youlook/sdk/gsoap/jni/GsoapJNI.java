package com.inspur.youlook.sdk.gsoap.jni;

import android.util.Log;

import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallEventListener;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GsoapJNI {
    final Lock mLock = new ReentrantLock();

    static {
        try {
            System.loadLibrary("hpstb_client");
            System.loadLibrary("gsoapjni");
        } catch (UnsatisfiedLinkError ule) {
            ule.printStackTrace();
        }
    }

    private ArrayList<GsoapCallEventListener> mGsoapCallEventListener = new ArrayList<>();

    public void addGsoapCallEventListener(GsoapCallEventListener listener) {
        if (!mGsoapCallEventListener.contains(listener))
            mGsoapCallEventListener.add(listener);
    }

    public void removeGsoapCallEventListener(GsoapCallEventListener listener) {
        if (mGsoapCallEventListener.contains(listener))
            mGsoapCallEventListener.remove(listener);
    }

    public void eventCallback(int event, String data) {
        mLock.lock();
        Log.i("GsoapJNI", "call event = " + event);
        Log.i("GsoapJNI", "data = " + data);
        for (GsoapCallEventListener listener : mGsoapCallEventListener) {
            listener.onEventSend(event, data);
        }
        Log.i("GsoapJNI", "send finish");
        mLock.unlock();
    }

    public native JniResponse hpstb_Init(String pInitInfo);

    public native JniResponse hpstb_SetNotifyEnable(int iEnable);

    public native JniResponse hpstb_SetEventNotify();

    public native JniResponse hpstb_ConnectToSTB(String pClientInfo, String pServerInfo);

    public native JniResponse hpstb_Disconnect(String pClientInfo);

    // Jerome: 只是返回盒子的jid回来, 並不是綁定 (可以重复调用，跟函数名有点不符合 ...)
    public native JniResponse hpstb_RequestBind(String pClientInfo);

    public native JniResponse hpstb_HandoverMasterRole(String pClientInfo, String pAnotherClient);

    public native JniResponse hpstb_GetMasterClient(String pClientInfo);

    public native JniResponse hpstb_SetClientRole(String pClientInfo, String pRoleInfo);

    public native JniResponse hpstb_GetClientRole(String pClientInfo);

    public native JniResponse hpstb_GetSTBInfo(String pClientInfo);

    public native JniResponse hpstb_GetSTBCapacity(String pClientInfo);

    // hpstb_SetBulletScreenEnable // TODO no implement
    // hpstb_GetBulletScreenEnable // TODO no implement
    // hpstb_SendText              // TODO no implement
    // hpstb_SendVoice             // TODO no implement

    public native JniResponse hpstb_SendKey(String pClientInfo, String pKeyInfo);

    // hpstb_GetSceenshot       // TODO no implement

    public native JniResponse hpstb_GetChannelList(String pClientInfo);

    public native JniResponse hpstb_GetChannelClassification(String pClientInfo);

    // hpstb_GetBookingList     // TODO no implement
    // hpstb_SetBookingList     // TODO no implement

    public native JniResponse hpstb_RequestPF(String pClientInfo, String pRequestInfo);

    public native JniResponse hpstb_RequestEPG(String pClientInfo, String pRequestInfo);

    public native JniResponse hpstb_PlayChannelOnTV(String pClientInfo, String pChannelInfo);

    // hpstb_GetPlayingOnTV     // TODO no implement
    // hpstb_GetSharingChannels // TODO no implement
    // hpstb_GetShareable       // TODO no implement

    public native JniResponse hpstb_RequestShareChannel(String pClientInfo, String pRequestInfo);

    public native JniResponse hpstb_StopSharingChannel(String pClientInfo, String pChannelInfo);

    public native JniResponse hpstb_StartSsdp();

    public native JniResponse hpstb_StopSsdp();

    public native JniResponse hpstb_Check_Password(String pClientInfo, String pPasswdInfo);

}
