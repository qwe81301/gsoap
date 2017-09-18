package com.inspur.youlook.sdk.gsoap;

import com.inspur.youlook.sdk.gsoap.interfaces.GsoapCallEventListener;
import com.inspur.youlook.sdk.gsoap.jni.GsoapJNI;
import com.inspur.youlook.sdk.gsoap.jni.JniResponse;

/**
 * Created by andyliu on 2016/11/7
 */
public class GsoapProxySingleton {

    private GsoapJNI mGsoapJNI;

    private GsoapProxySingleton() {
        mGsoapJNI = new GsoapJNI();
    }

    private static class SingletonHolder {
        private static final GsoapProxySingleton INSTANCE = new GsoapProxySingleton();
    }

    public static GsoapProxySingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public GsoapJNI getGoapJNI() {
        return mGsoapJNI;
    }

    public void addGsoapCallEventListener(GsoapCallEventListener listener) {
        if (mGsoapJNI != null)
            mGsoapJNI.addGsoapCallEventListener(listener);
    }

    public void removeGsoapCallEventListener(GsoapCallEventListener listener) {
        if (mGsoapJNI != null)
            mGsoapJNI.removeGsoapCallEventListener(listener);
    }

    public JniResponse hpstb_Init(String pInitInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_Init(pInitInfo);
    }

    public JniResponse hpstb_SetNotifyEnable(int enable) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_SetNotifyEnable(enable);
    }

    public JniResponse hpstb_SetEventNotify() {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_SetEventNotify();
    }

    public JniResponse hpstb_ConnectToSTB(String pClientInfo, String pServerInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_ConnectToSTB(pClientInfo, pServerInfo);
    }

    public JniResponse hpstb_Disconnect(String pClientInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_Disconnect(pClientInfo);
    }

    public JniResponse hpstb_RequestBind(String pClientInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_RequestBind(pClientInfo);
    }

    public JniResponse hpstb_HandoverMasterRole(String pClientInfo, String pAnotherClient) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_HandoverMasterRole(pClientInfo, pAnotherClient);
    }

    public JniResponse hpstb_GetMasterClient(String pClientInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_GetMasterClient(pClientInfo);
    }

    public JniResponse hpstb_SetClientRole(String pClientInfo, String pRoleInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_SetClientRole(pClientInfo, pRoleInfo);
    }

    public JniResponse hpstb_GetClientRole(String pClientInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_GetClientRole(pClientInfo);
    }

    public JniResponse hpstb_GetSTBInfo(String pClientInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_GetSTBInfo(pClientInfo);
    }

    public JniResponse hpstb_GetSTBCapacity(String pClientInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_GetSTBCapacity(pClientInfo);
    }

    public JniResponse hpstb_SendKey(String pClientInfo, String pKeyInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_SendKey(pClientInfo, pKeyInfo);
    }

    public JniResponse hpstb_GetChannelList(String pClientInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_GetChannelList(pClientInfo);
    }

    public JniResponse hpstb_GetChannelClassification(String pClientInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_GetChannelClassification(pClientInfo);
    }

    public JniResponse hpstb_RequestPF(String pClientInfo, String pRequestInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_RequestPF(pClientInfo, pRequestInfo);
    }

    public JniResponse hpstb_RequestEPG(String pClientInfo, String pRequestInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_RequestEPG(pClientInfo, pRequestInfo);
    }

    public JniResponse hpstb_PlayChannelOnTV(String pClientInfo, String pChannelInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_PlayChannelOnTV(pClientInfo, pChannelInfo);
    }

    public JniResponse hpstb_RequestShareChannel(String pClientInfo, String pRequestInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_RequestShareChannel(pClientInfo, pRequestInfo);
    }

    public JniResponse hpstb_StopSharingChannel(String pClientInfo, String pChannelInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_StopSharingChannel(pClientInfo, pChannelInfo);
    }

    public JniResponse hpstb_StartSsdp() {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_StartSsdp();
    }

    public JniResponse hpstb_StopSsdp() {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_StopSsdp();
    }

    public JniResponse hpstb_StopSsdp(String pClientInfo, String pPasswdInfo) {
        if (mGsoapJNI == null)
            return null;
        return mGsoapJNI.hpstb_Check_Password(pClientInfo, pPasswdInfo);
    }
}

