LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libhpstb_client
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libhpstb_client.so

include $(PREBUILT_SHARED_LIBRARY)



