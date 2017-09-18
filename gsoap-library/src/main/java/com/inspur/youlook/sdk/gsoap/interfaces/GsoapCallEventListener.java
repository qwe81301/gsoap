package com.inspur.youlook.sdk.gsoap.interfaces;

/**
 * Created by andyliu on 2016/11/18
 */
public interface GsoapCallEventListener {
    void onEventSend(int eventType, String... data);
}