package com.inspur.youlook.sdk.gsoap.asynctask;

import com.inspur.youlook.sdk.gsoap.utils.Constants;

/**
 * Created by andyliu on 2016/10/14
 */
public class CallbackStatus {
    private int mCode;
    private String mCustomMessage;
    private String mData = "";

    public CallbackStatus(int code) {
        this.mCode = code;
    }

    public CallbackStatus(String message) {
        this.mCode = -1;
        this.mCustomMessage = message;
    }

    public String getMessage() {
        if (Constants.RN_STATUS_CODE_MAP.containsKey(mCode)) {
            return Constants.RN_STATUS_CODE_MAP.get(mCode);
        } else {
            return mCustomMessage;
        }
    }

    public int getCode() {
        return mCode;
    }

    public String getData() {
        return mData;
    }

    public void setData(String data) {
        mData = data;
    }
}
