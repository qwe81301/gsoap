package com.inspur.youlook.sdk.gsoap.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by andyliu on 2016/11/30.
 */
public class PreferenceUtil {
    private static final String TAG = PreferenceUtil.class.getSimpleName();
    public static final String PREF_KEY_STB_AUTO_CONNECT = "stb_auto_connect";
    private static final String PREF_NAME_GSOAP = "gSOAP";
    private static final String PREF_KEY_STB_LAST_IP = "stb_last_ip";
    private static final String PREF_KEY_STB_LAST_USER_NAME = "stb_last_user";
    private static final String PREF_KEY_STB_LAST_UUID = "stb_last_uuid";
    /* gSOAP */

    public static void setAutoConnect(Context ctx, boolean enable) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME_GSOAP, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_KEY_STB_AUTO_CONNECT, enable);
        editor.apply();
    }

    public static boolean isAutoConnect(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME_GSOAP, 0);
        return settings.getBoolean(PREF_KEY_STB_AUTO_CONNECT, true);
    }

    public static void saveConnectionInfo(Context ctx, String ip, String userName, String uuid) {
        Log.getInstance().writeLog(TAG, "saveConnectionInfo", "ip = " + ip + ", userName = " + userName);
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME_GSOAP, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_KEY_STB_LAST_IP, ip);
        editor.putString(PREF_KEY_STB_LAST_USER_NAME, userName);
        editor.putString(PREF_KEY_STB_LAST_UUID, userName);
        editor.apply();
    }

    public static String getLastUserName(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME_GSOAP, 0);
        return settings.getString(PREF_KEY_STB_LAST_USER_NAME, "");
    }

    public static String getLastIP(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME_GSOAP, 0);
        return settings.getString(PREF_KEY_STB_LAST_IP, "");
    }
    public static String getLastUUID(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(PREF_NAME_GSOAP, 0);
        return settings.getString(PREF_KEY_STB_LAST_UUID, "");
    }
}
