//
// Created by andyliu on 2016/7/13.
//
#include <android/log.h>
#include <jni.h>
#include <dlfcn.h>
#include <stdio.h>
#include "hpstb_client_api.h"

#define TAG_GSOAP_JNI "gsoapJNI"

#define CLAZZ_PATH_JNI_RESPONSE "com/inspur/youlook/sdk/gsoap/jni/JniResponse"

#define NAME_LIB_HPSTB_SO "libhpstb_client.so"

#ifdef __cplusplus
extern "C" {
#endif

void loacl_hpstb_Free(char *ptr);

jobject only_request_by_clientInfo(const char *hpstb_api, JNIEnv *env, jstring clientInfo);

jobject one_info_no_return(const char *hpstb_api, JNIEnv *env, jstring info);

// Global variable
JavaVM *g_jvm = NULL;   // Get g_jvm from jni main thread use env->GetJavaVM(&g_jvm);
jobject g_obj = NULL;   // Where the java function exist (some Activity).
jmethodID g_mid = NULL;

void _UserEventCallBack(int event, void *arg, int arg_len, void *user_param) {

    JNIEnv *env;
    int getEnvStat = g_jvm->GetEnv((void **) &env, JNI_VERSION_1_6); // double check it's all ok
    if (getEnvStat == JNI_EDETACHED) {
        if (g_jvm->AttachCurrentThread(&env, NULL) != 0) {
            __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "Failed to attach");
        }
    } else if (getEnvStat == JNI_EVERSION) {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "GetEnv: version not supported");
    }

    jstring jstrBuf;
    __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "arg_len:%d", arg_len);
    if (arg_len <= 0)
        arg = (char *) "";

    jstrBuf = env->NewStringUTF((char *) arg);

    env->CallVoidMethod(g_obj, g_mid, event, jstrBuf);
    g_jvm->DetachCurrentThread();
}

/************************************************************
 @function: hpstb_Init
 Init hpstb module.

 @param pInitInfo:  JSON: e.g. {"port": 20000}
 @return: HPSTB_OK--success
          HPSTB_FAIL--fail
 *************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1Init
        (JNIEnv *env, jobject thiz, jstring initInfo) {

    env->GetJavaVM(&g_jvm);

    // convert local to global reference(local will die after this method call)
    g_obj = env->NewGlobalRef(thiz);

    // save refs for callback
    jclass g_clazz = env->GetObjectClass(g_obj);
    if (g_clazz == NULL) {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "hpstb_Init Failed to find class");
    }

    g_mid = env->GetMethodID(g_clazz, "eventCallback", "(ILjava/lang/String;)V");
    if (g_mid == NULL) {
        env->DeleteLocalRef(g_clazz);
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "hpstb_Init Unable to get method ref");
    }

    return one_info_no_return("hpstb_Init", env, initInfo);
}

/************************************************************
@function: hpstb_SetNotifyEnable
enable/disable notify task.
@param iEnable:  0/1
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1SetNotifyEnable
        (JNIEnv *env, jobject, jint javaInt) {
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        HPSTBErrCode (*sym)(int);
        sym = (HPSTBErrCode (*)(int)) dlsym(handle, "hpstb_SetNotifyEnable");
        HPSTBErrCode ret = sym(javaInt);

        dlclose(handle);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        jstring result = env->NewStringUTF("No return value"); // No return value

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_SetNotifyEnable dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/************************************************************
@function: hpstb_SetEventNotify
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1SetEventNotify
        (JNIEnv *env, jobject) {
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        HPSTBErrCode (*sym)(_EventCallBack, void *);
        sym = (HPSTBErrCode (*)(_EventCallBack, void *)) dlsym(handle, "hpstb_SetEventNotify");

        _EventCallBack eventCallBack = _UserEventCallBack;
        HPSTBErrCode ret = sym(eventCallBack, NULL);

        dlclose(handle);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        jstring result = env->NewStringUTF("No return value"); // No return value

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_SetEventNotify dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/************************************************************
 @function: hpstb_ConnectToSTB
 Client connect to STB. If success, client will get a token,
 A token is a session-binding string.

 @param pClientInfo:    input JSON: e.g. {"userid":"admin","uuid":"4028b88154d1460d0154d224060e000d"}
    userid is xmpp username,uuid is for http request
 @param pServerInfo: input JSON: e.g. {"server":"192.168.1.100:8100"}
 @param ppResultInfo: output JSON: e.g. {"userid":"admin","token":"8a234hsa432e","expires":60}, should call hpstb_Free after use.
 @return: HPSTB_OK--success
          HPSTB_FAIL--fail
          HPSTB_TOO_MANY_CLIENTS--refuse because of too many client
 *************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1ConnectToSTB
        (JNIEnv *env, jobject, jstring clientInfo, jstring serverInfo) {
    HPSTBErrCode (*sym)(const char *, const char *, char **);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *, char **)) dlsym(handle,
                                                                            "hpstb_ConnectToSTB");

        const char *nativeClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *nativeServerInfo = env->GetStringUTFChars(serverInfo, JNI_FALSE);

        char *retString = NULL;
        HPSTBErrCode ret = sym(nativeClientInfo, nativeServerInfo, &retString);

        env->ReleaseStringUTFChars(clientInfo, nativeClientInfo);
        env->ReleaseStringUTFChars(serverInfo, nativeServerInfo);

        jstring result = env->NewStringUTF(retString);

        dlclose(handle);
        loacl_hpstb_Free(retString);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_ConnectToSTB dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/************************************************************
@function: hpstb_Disconnect
Client disconnect to STB.

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@return: HPSTB_OK--success
         HPSTB_FAIL--fail
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1Disconnect
        (JNIEnv *env, jobject, jstring clientInfo) {
    return one_info_no_return("hpstb_Disconnect", env, clientInfo);
}

/***********************************************************
 * @function: hpstb_StartSsdp
 *
 *
 */
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1StartSsdp
        (JNIEnv *env, jobject instance) {
    HPSTBErrCode (*sym)(void);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "hpstb_StartSsdp 1!");
        sym = (HPSTBErrCode (*)(void)) dlsym(handle, "hpstb_StartSsdp");
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "hpstb_StartSsdp 2!");
        HPSTBErrCode ret = sym();
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "hpstb_StartSsdp 3!");
        dlclose(handle);
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "hpstb_StartSsdp 4!");
        jstring result = env->NewStringUTF("No return value"); // No return value

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_HandoverMasterRole dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}
/***********************************************************
 * @function: hpstb_StopSsdp
 *
 *
 */

JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1StopSsdp
        (JNIEnv *env, jobject instance) {
    HPSTBErrCode (*sym)(void);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(void)) dlsym(handle, "hpstb_StopSsdp");
        HPSTBErrCode ret = sym();
        dlclose(handle);
        jstring result = env->NewStringUTF("No return value"); // No return value

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_HandoverMasterRole dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}
/************************************************************
 *
 *
@function: hpstb_RequestBind
Client request bind to stb. If success, client will get STB xmpp JID

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param ppResultInfo:   output JSON: e.g. {"jid":"11011603240000001aabbccdde@218.57.146.181"}, should call hpstb_Free after use.
@return: HPSTB_OK--success
         HPSTB_FAIL--fail
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1RequestBind
        (JNIEnv *env, jobject, jstring clientInfo) {
    return only_request_by_clientInfo("hpstb_RequestBind", env, clientInfo);
}

/************************************************************
@function: hpstb_HandoverMasterRole
Handover current client's master role to another client,
and of cause then itself falls back to a slave.
Only a master client can call this API successfully.

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param pAnotherClient:    input JSON: e.g. {"userid":"USER2","ip":"10.0.0.5"}
@return: HPSTB_OK--success
         HPSTB_FAIL--fail
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1HandoverMasterRole
        (JNIEnv *env, jobject, jstring clientInfo, jstring anotherClient) {
    HPSTBErrCode (*sym)(const char *, const char *);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *)) dlsym(handle,
                                                                   "hpstb_HandoverMasterRole");

        const char *nativeClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *nativeAnotherClient = env->GetStringUTFChars(anotherClient, JNI_FALSE);

        HPSTBErrCode ret = sym(nativeClientInfo, nativeAnotherClient);

        env->ReleaseStringUTFChars(clientInfo, nativeClientInfo);
        env->ReleaseStringUTFChars(anotherClient, nativeAnotherClient);

        dlclose(handle);

        jstring result = env->NewStringUTF("No return value"); // No return value

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_HandoverMasterRole dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/************************************************************
@function: hpstb_GetMasterClient
Get current STB's master client.

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param ppMasterInfo:   output JSON: e.g. {"userid":"USER2", "ip":"10.0.0.5"}, should call hpstb_Free after use.

@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1GetMasterClient
        (JNIEnv *env, jobject, jstring clientInfo) {
    return only_request_by_clientInfo("hpstb_GetMasterClient", env, clientInfo);
}

/************************************************************
@function: hpstb_SetClientRole
Change current client's role. A slave-to-master change
may fail if current role is slave. Master-to-slave always
success because master has the right to give up it's role.

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param pRoleInfo:   input JSON: e.g. {"role":"slave"}
value of role: master/slave

@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1SetClientRole
        (JNIEnv *env, jobject, jstring clientInfo, jstring roleInfo) {
    HPSTBErrCode (*sym)(const char *, const char *);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *)) dlsym(handle, "hpstb_SetClientRole");

        const char *nativeClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *nativeRoleInfo = env->GetStringUTFChars(roleInfo, JNI_FALSE);

        HPSTBErrCode ret = sym(nativeClientInfo, nativeRoleInfo);

        env->ReleaseStringUTFChars(clientInfo, nativeClientInfo);
        env->ReleaseStringUTFChars(roleInfo, nativeRoleInfo);

        dlclose(handle);

        jstring result = env->NewStringUTF("No return value"); // No return value

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_SetClientRole dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/************************************************************
@function: hpstb_GetClientRole
Get current client role info from STB.

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param ppRoleInfo:   output JSON: e.g. {"role":"slave"}, should call hpstb_Free after use.
value of role: master/slave

@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1GetClientRole
        (JNIEnv *env, jobject, jstring clientInfo) {
    return only_request_by_clientInfo("hpstb_GetClientRole", env, clientInfo);
}

/************************************************************
@function: hpstb_GetSTBInfo
Get stb info.

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param ppStbInfo:        output JSON: e.g.
{"hwver":"1.00","swver":"1.00","sn":"LC000000001234567890","mac":"BC20BA123456",
 "devicename":"bcm7251s","smartcardno":"8531103988190404","operator":"beijinggehua"}
, should call hpstb_Free after use.

@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1GetSTBInfo
        (JNIEnv *env, jobject, jstring clientInfo) {
    return only_request_by_clientInfo("hpstb_GetSTBInfo", env, clientInfo);
}

/************************************************************
@function: hpstb_GetSTBCapacity
Get stb capacity.

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param ppCapacity:    output JSON: e.g.
{"tunernum":4,"transcode":[{"type":"h264","resolutions":["640*480","352*288","176*144"]}],
 "audiodecode":[{"type":"aac"},{"type":"ac3"},{"type":"mpeg2"},{"type":"mpeg1"}],
 "videodecode":[{"type":"h265"},{"type":"h264"},{"type":"mpeg2"},{"type":"mpeg1"}],
 "gatewaymanage":"flase"}
, should call hpstb_Free after use.

@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1GetSTBCapacity
        (JNIEnv *env, jobject, jstring clientInfo) {
    return only_request_by_clientInfo("hpstb_GetSTBCapacity", env, clientInfo);
}

/************************************************************
@function: hpstb_SendKey
Send keyevent to STB (only for controller).

@param pClientInfo:    input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param pKeyInfo:     input JSON: e.g. {"key":"UP"}  key:UP/DOWN/LEFT/RIGHT/BACK/HOME/1/2/3/....
@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1SendKey
        (JNIEnv *env, jobject, jstring clientInfo, jstring keyInfo) {
    HPSTBErrCode (*sym)(const char *, const char *);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *)) dlsym(handle, "hpstb_SendKey");

        const char *nativeClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *nativeKeyInfo = env->GetStringUTFChars(keyInfo, JNI_FALSE);

        HPSTBErrCode ret = sym(nativeClientInfo, nativeKeyInfo);

        env->ReleaseStringUTFChars(clientInfo, nativeClientInfo);
        env->ReleaseStringUTFChars(keyInfo, nativeKeyInfo);

        dlclose(handle);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        jstring result = env->NewStringUTF("No return value"); // No return value

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_SendKey dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/************************************************************
@function: hpstb_GetChannelList
Get channel list of STB.

@param pClientInfo:         input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param ppChannelList:       output JSON: e.g.
[
{"chno":1,"name":"ch1","tsid":1,"serviceid":111,"freq":195000,"tunerid":1,"isHide":"false","isLock":"false","isFavor":"false","isHD":"true","type":"tv"},
{"chno":2,"name":"ch2","tsid":1,"serviceid":112,"freq":195000,"tunerid":1,"isHide":"false","isLock":"false","isFavor":"false","isHD":"true","type":"tv"},
......
], should call hpstb_Free after use.

@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1GetChannelList
        (JNIEnv *env, jobject, jstring clientInfo) {
    return only_request_by_clientInfo("hpstb_GetChannelList", env, clientInfo);
}

/************************************************************
@function: hpstb_GetChannelClassification
Get channel classification of STB.

@param pClientInfo:         input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param ppChannelList:       output JSON: e.g.
[
{"bat":1,"name":"CCTV"},
{"bat":1,"name":"HDTV"},
......
]
hpstb_GetChannelList will get channel bat,according to bat you can get classification
by hpstb_GetChannelClassification
, should call hpstb_Free after use.

@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL
Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1GetChannelClassification
        (JNIEnv *env, jobject, jstring clientInfo) {
    return only_request_by_clientInfo("hpstb_GetChannelClassification", env, clientInfo);
}

/************************************************************
@function: hpstb_RequestPF
Request PF of STB.

@param pClientInfo:     input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param pRequestInfo:  input JSON: e.g. {"freq":195000,"tsid":1,"serviceid":111}
@param ppResult:    output JSON:
 e.g.
[
{"frequency":259000,"tsId":10,"serviceId":200,"duration":2700,"parentRating":0,"eventName":"test","startDateTime":"2016-06-03 13:58:00","endDateTime":"2016-06-03 14:43:00","content":"","languageLocal":"chi","eventNameLocal":"test","contentLocal":"","languageSecond":"","eventNameSecond":"","contentSecond":"","pfType":"present"}
]
, should call hpstb_Free after use.
usually,pf will arrive async,by notify,and maybe twice notify to get whole PF
[
{"frequency":259000,"tsId":10,"serviceId":200,"duration":2700,"parentRating":0,"eventName":"test","startDateTime":"2016-06-03 13:58:00","endDateTime":"2016-06-03 14:43:00","content":"","languageLocal":"chi","eventNameLocal":"test","contentLocal":"","languageSecond":"","eventNameSecond":"","contentSecond":"","pfType":"present"}
]
pfType:present/follow
@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1RequestPF
        (JNIEnv *env, jobject, jstring clientInfo, jstring requestInfo) {
    HPSTBErrCode (*sym)(const char *, const char *, char **);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *, char **)) dlsym(handle,
                                                                            "hpstb_RequestPF");
        const char *nativeClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *nativeRequestInfo = env->GetStringUTFChars(requestInfo, JNI_FALSE);

        char *resultInfo = NULL;

        HPSTBErrCode ret = sym(nativeClientInfo, nativeRequestInfo, &resultInfo);

        env->ReleaseStringUTFChars(clientInfo, nativeClientInfo);
        env->ReleaseStringUTFChars(requestInfo, nativeRequestInfo);

        jstring jStringResult = env->NewStringUTF(resultInfo);

        dlclose(handle);
        loacl_hpstb_Free(resultInfo);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        return env->NewObject(cls, constructor, ret, jStringResult);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_RequestPF dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/**
@brief hpstb_RequestEPG
Request EPG of STB.

@param[in] pClientInfo      e.g.
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pRequestInfo   e.g.
                   {"freq":195000,"tsid":1,"serviceid":111}
@param[out] ppResult
 e.g.
[
{"frequency":259000,"tsId":10,"serviceId":200,"duration":2700,"parentRating":0,"eventName":"test","startDateTime":"2016-06-03 13:58:00","endDateTime":"2016-06-03 14:43:00","content":"","languageLocal":"chi","eventNameLocal":"test","contentLocal":"","languageSecond":"","eventNameSecond":"","contentSecond":""}
]
, should call hpstb_Free after use.
usually,epg will arrive async,by notify
[
{"frequency":259000,"tsId":10,"serviceId":200,"duration":2700,"parentRating":0,"eventName":"test","startDateTime":"2016-06-03 13:58:00","endDateTime":"2016-06-03 14:43:00","content":"","languageLocal":"chi","eventNameLocal":"test","contentLocal":"","languageSecond":"","eventNameSecond":"","contentSecond":""}
]

@return refer to HPSTBErrCode
*/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1RequestEPG
        (JNIEnv *env, jobject, jstring clientInfo, jstring requestInfo) {
    HPSTBErrCode (*sym)(const char *, const char *, char **);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *, char **)) dlsym(handle,
                                                                            "hpstb_RequestEPG");

        const char *pClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *pRequestInfo = env->GetStringUTFChars(requestInfo, JNI_FALSE);

        char *resultInfo = NULL;
        HPSTBErrCode ret = sym(pClientInfo, pRequestInfo, &resultInfo);

        env->ReleaseStringUTFChars(clientInfo, pClientInfo);
        env->ReleaseStringUTFChars(requestInfo, pRequestInfo);

        jstring jStringResult = env->NewStringUTF(resultInfo);

        dlclose(handle);
        loacl_hpstb_Free(resultInfo);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");
        return env->NewObject(cls, constructor, ret, jStringResult);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_RequestEPG dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/**
@brief hpstb_PlayChannelOnTV
Play a specific channel on TV.

@param[in] pClientInfo  e.g.
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pChannelInfo e.g.
                   {"freq":195000,"tsid":1,"serviceid":111}
@return refer to HPSTBErrCode
*/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1PlayChannelOnTV
        (JNIEnv *env, jobject, jstring clientInfo, jstring channelInfo) {
    HPSTBErrCode (*sym)(const char *, const char *);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *)) dlsym(handle, "hpstb_PlayChannelOnTV");

        const char *pClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *pChannelInfo = env->GetStringUTFChars(channelInfo, JNI_FALSE);

        HPSTBErrCode ret = sym(pClientInfo, pChannelInfo);

        env->ReleaseStringUTFChars(clientInfo, pClientInfo);
        env->ReleaseStringUTFChars(channelInfo, pChannelInfo);

        dlclose(handle);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        jstring result = env->NewStringUTF("No return value"); // No return value

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_PlayChannelOnTV dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/************************************************************
@function: hpstb_RequestShareChannel
If STB is already sharing this channel, just return ok.
request maybe fails if stb resources are not enough.
Client can use this API to switch channel,STB will automatically
stop the last sharing channel for this client, and returns
a new url if necessary(maybe use the last url).

@param pClientInfo: input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param pRequestInfo: input JSON: e.g.
{
"channel":{"freq":195000,"tsid":1,"serviceid":111},
"codec":{"type":"default","res":"default"},
"protocol":{"type":"hls","port":"default"},
"encrypt":"false"
}
channel: required
codec: optional  type:default(no support now)  res:default(not support now)
protocol:type: hls/http_socket  port:default(no support now)
encrypt: true/false(no support now)

@param ppSharingInfo: output JSON: e.g.
{
"url":"http://109.163.0.5/streaming/112.ts",
"channel":{"freq":195000,"tsid":1,"serviceid": 111}
}
, should call hpstb_Free after use.
@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1RequestShareChannel
        (JNIEnv *env, jobject, jstring clientInfo, jstring requestInfo) {
    HPSTBErrCode (*sym)(const char *, const char *, char **);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *, char **)) dlsym(handle,
                                                                            "hpstb_RequestShareChannel");

        const char *nativeClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *nativeRequestInfo = env->GetStringUTFChars(requestInfo, JNI_FALSE);

        char *resultInfo = NULL;

        HPSTBErrCode ret = sym(nativeClientInfo, nativeRequestInfo, &resultInfo);

        env->ReleaseStringUTFChars(clientInfo, nativeClientInfo);
        env->ReleaseStringUTFChars(requestInfo, nativeRequestInfo);

        jstring jStringResult = env->NewStringUTF(resultInfo);

        dlclose(handle);
        loacl_hpstb_Free(resultInfo);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        return env->NewObject(cls, constructor, ret, jStringResult);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_RequestShareChannel dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/************************************************************
@function: hpstb_StopSharingChannel
Stop sharing a channel means only this client will stop
sharing this channel, if there are still some clients
attaching on this sharing, STB will not stop sharing,
or else STB will really stop sharing.

@param pClientInfo: input JSON: e.g. {"userid":"admin","token":"8a234hsa432e"}
@param pChannelInfo: input JSON: e.g.{"freq":195000,"tsid":1,"serviceid":111}
@return: refer to HPSTBErrCode
*************************************************************/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1StopSharingChannel
        (JNIEnv *env, jobject, jstring clientInfo, jstring channelInfo) {
    HPSTBErrCode (*sym)(const char *, const char *);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *)) dlsym(handle,
                                                                   "hpstb_StopSharingChannel");

        const char *nativeClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *nativeChannelInfo = env->GetStringUTFChars(channelInfo, JNI_FALSE);

        HPSTBErrCode ret = sym(nativeClientInfo, nativeChannelInfo);

        env->ReleaseStringUTFChars(clientInfo, nativeClientInfo);
        env->ReleaseStringUTFChars(channelInfo, nativeChannelInfo);

        dlclose(handle);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        jstring result = env->NewStringUTF("No return value"); // No return value

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_StopSharingChannel dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

/**
@brief hpstb_Check_Password
Client check password by STB.

@param[in] pClientInfo    e.g.{"userid":"admin","token":"8a234hsa432e"}
@param[in] pPasswdInfo    e.g.{"parentRating":"1234"}
@return refer to HPSTBErrCode:  HPSTB_OK--check success
                                HPSTB_FAIL--check fail
*/
JNIEXPORT jobject JNICALL Java_com_inspur_youlook_sdk_gsoap_jni_GsoapJNI_hpstb_1Check_1Password
        (JNIEnv *env, jobject, jstring clientInfo, jstring passwordInfo) {
    HPSTBErrCode (*sym)(const char *, const char *, char **);
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        sym = (HPSTBErrCode (*)(const char *, const char *, char **)) dlsym(handle,
                                                                            "hpstb_Check_Password");

        const char *pClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);
        const char *pPasswordInfo = env->GetStringUTFChars(passwordInfo, JNI_FALSE);

        char *resultInfo = NULL;
        HPSTBErrCode ret = sym(pClientInfo, pPasswordInfo, &resultInfo);

        env->ReleaseStringUTFChars(clientInfo, pClientInfo);
        env->ReleaseStringUTFChars(passwordInfo, pPasswordInfo);

        jstring jStringResult = env->NewStringUTF(resultInfo);

        dlclose(handle);
        loacl_hpstb_Free(resultInfo);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");
        return env->NewObject(cls, constructor, ret, jStringResult);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "hpstb_Check_Password dlopen libhpstb_client.so Failed!");
        return NULL;
    }
}

void loacl_hpstb_Free(char *ptr) {
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        void (*sym)(char *);
        sym = (void (*)(char *)) dlsym(handle, "hpstb_Free");
        sym(ptr);
        dlclose(handle);
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "hpstb_Free Success!");
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "hpstb_Free Failed!");
    }
}

jobject one_info_no_return(const char *hpstb_api, JNIEnv *env, jstring info) {
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        HPSTBErrCode (*sym)(const char *);
        sym = (HPSTBErrCode (*)(const char *)) dlsym(handle, hpstb_api);

        const char *nativeClientInfo = env->GetStringUTFChars(info, JNI_FALSE);
        HPSTBErrCode ret = sym(nativeClientInfo);

        env->ReleaseStringUTFChars(info, nativeClientInfo);
        dlclose(handle);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        jstring result = env->NewStringUTF("No return value"); // No return value

        return env->NewObject(cls, constructor, ret, result);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI, "%s dlopen libhpstb_client.so Failed!",
                            hpstb_api);
        return NULL;
    }
}

jobject only_request_by_clientInfo(const char *hpstb_api, JNIEnv *env, jstring clientInfo) {
    void *handle = dlopen(NAME_LIB_HPSTB_SO, RTLD_LAZY | RTLD_GLOBAL);
    if (handle) {
        HPSTBErrCode (*sym)(const char *, char **);
        sym = (HPSTBErrCode (*)(const char *, char **)) dlsym(handle, hpstb_api);

        const char *nativeClientInfo = env->GetStringUTFChars(clientInfo, JNI_FALSE);

        char *resultInfo = NULL;
        HPSTBErrCode ret = sym(nativeClientInfo, &resultInfo);
        env->ReleaseStringUTFChars(clientInfo, nativeClientInfo);
        jstring jStringResult = env->NewStringUTF(resultInfo);

        dlclose(handle);
        loacl_hpstb_Free(resultInfo);

        jclass cls = env->FindClass(CLAZZ_PATH_JNI_RESPONSE);
        jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");

        return env->NewObject(cls, constructor, ret, jStringResult);
    } else {
        __android_log_print(ANDROID_LOG_INFO, TAG_GSOAP_JNI,
                            "%s dlopen libhpstb_client.so Failed!", hpstb_api);
        return NULL;
    }
}

#ifdef __cplusplus
}
#endif