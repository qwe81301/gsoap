LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := gsoapjni
LOCAL_SRC_FILES := com_inspur_youlook_sdk_gsoap_jni_GsoapJNI.cpp

LOCAL_SHARED_LIBRARIES := libhpstb_client
LOCAL_C_INCLUDES := $(LOCAL_PATH)

LOCAL_LDLIBS += -llog

include $(BUILD_SHARED_LIBRARY)
include $(LOCAL_PATH)/libs/Android.mk
