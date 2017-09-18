package com.inspur.youlook.sdk.gsoap.utils;

import com.google.gson.JsonObject;


/**
 * Created by andyliu on 2017/3/28
 */
public class GsoapUtils {

    private static final String DEFAULT_STB_SERVER_PORT = "10000";
    private static final String DEFAULT_LOCAL_SERVER_PORT = "10001";

    /**
     * @return JSON object. EX: {"port":"10001"};
     */
    public static JsonObject getInitInfoJsonObject(String port) {
        JsonObject object = new JsonObject();
        object.addProperty("port", port);
        return object;
    }

    public static String getInitInfo() {
        return getInitInfoJsonObject(DEFAULT_LOCAL_SERVER_PORT).toString();
    }

    public static String getInitInfo(String port) {
        return getInitInfoJsonObject(port).toString();
    }

    /**
     * @param serverIp IP to connect.
     * @return JSON object. EX: {"server":"192.168.1.73:10000"}
     */
    public static JsonObject getServerInfoJsonObject(String serverIp) {
        JsonObject serverInfoObject = new JsonObject();
        serverInfoObject.addProperty("server", serverIp + ":" + DEFAULT_STB_SERVER_PORT);
        return serverInfoObject;
    }

    public static String getServerInfo(String serverIp) {
        return getServerInfoJsonObject(serverIp).toString();
    }

    /**
     * @param userID   User ID
     * @param stbToken STB token
     * @return JSON object. EX: {"userid":"userID", "token":"stbToken"}
     */
    public static JsonObject getClientInfoJsonObject(String userID, String stbToken) {
        JsonObject clientInfoObject = new JsonObject();
        clientInfoObject.addProperty("userid", userID);
        clientInfoObject.addProperty("token", stbToken);
        return clientInfoObject;
    }

    public static String getClientInfo(String userID, String stbToken) {
        return getClientInfoJsonObject(userID, stbToken).toString();
    }

    /**
     * @param anotherUserID Another user ID
     * @param anotherIP     Another IP
     * @return JSON object. EX: {"userid":"anotherUserID", "ip":"anotherIP"}
     */
    public static JsonObject getAnotherClientInfoJsonObject(String anotherUserID, String anotherIP) {
        JsonObject anotherClientInfo = new JsonObject();
        anotherClientInfo.addProperty("userid", anotherUserID);
        anotherClientInfo.addProperty("ip", anotherIP);
        return anotherClientInfo;
    }

    public static String getAnotherClientInfo(String anotherUserID, String anotherIP) {
        return getAnotherClientInfoJsonObject(anotherUserID, anotherIP).toString();
    }

    /**
     * @param channelFreq      Channel freq
     * @param channelTsid      Channel Ts ID
     * @param channelServiceId Channel Service ID
     * @return JSON object. EX: {"freq":648000, "tsid":1, "serviceid":1}
     */
    public static JsonObject getChannelInfoJsonObject(int channelFreq, int channelTsid, int channelServiceId) {
        JsonObject channelInfoObject = new JsonObject();
        channelInfoObject.addProperty("freq", channelFreq);
        channelInfoObject.addProperty("tsid", channelTsid);
        channelInfoObject.addProperty("serviceid", channelServiceId);
        return channelInfoObject;
    }

    public static String getChannelInfo(int channelFreq, int channelTsid, int channelServiceId) {
        return getChannelInfoJsonObject(channelFreq, channelTsid, channelServiceId).toString();
    }

    /**
     * @param role User role
     * @return JSON object. EX: {"role":"master"}
     */
    public static JsonObject getRoleJsonObject(String role) {
        JsonObject roleObject = new JsonObject();
        roleObject.addProperty("role", role);
        return roleObject;
    }

    public static String getRoleInfo(String role) {
        return getRoleJsonObject(role).toString();
    }

    /**
     * @param keyCode Send key code
     * @return JSON object. EX: {"key":"keyCode"}
     */
    public static JsonObject getKeyJsonObject(String keyCode) {
        JsonObject keyObject = new JsonObject();
        keyObject.addProperty("key", keyCode);
        return keyObject;
    }

    public static String getKeyInfo(String keyCode) {
        return getKeyJsonObject(keyCode).toString();
    }

    /**
     * @param password Parent password
     * @return JSON object. EX: {"parentRating":"1234"}
     */
    public static JsonObject getPasswordJsonObject(String password) {
        JsonObject passwordObject = new JsonObject();
        passwordObject.addProperty("parentRating", password);
        return passwordObject;
    }

    public static String getPasswordInfo(String password) {
        return getPasswordJsonObject(password).toString();
    }
}
