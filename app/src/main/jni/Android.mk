LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE           := proc
LOCAL_SRC_FILES        := proc.c
LOCAL_LDLIBS           := -llog
include $(BUILD_SHARED_LIBRARY)
