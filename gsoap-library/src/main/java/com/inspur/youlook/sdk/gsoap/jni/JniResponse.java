package com.inspur.youlook.sdk.gsoap.jni;

/**
 * Created by andyliu on 2016/8/3.
 */
public class JniResponse {
    private int mReturnCode;
    private String mReturnValue;

    public JniResponse(int code, String value) {
        this.mReturnCode = code;
        this.mReturnValue = value;
    }

    public String getReturnValue() {
        return mReturnValue;
    }

    public void setReturnValue(String returnValue) {
        this.mReturnValue = returnValue;
    }

    public int getReturnCode() {
        return mReturnCode;
    }

    public void setReturnCode(int returnCode) {
        this.mReturnCode = returnCode;
    }
}
