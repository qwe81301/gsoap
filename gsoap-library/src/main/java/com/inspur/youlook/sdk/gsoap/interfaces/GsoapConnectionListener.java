package com.inspur.youlook.sdk.gsoap.interfaces;

/**
 * Created by andyliu on 2017/6/1
 */
public interface GsoapConnectionListener {
    void isConnecting(boolean isConnecting);

    void connectSuccess(String connectIp, String connectResult);

    void connectFailed();

    void disconnectSuccess();

    void disconnectFailed();
}
