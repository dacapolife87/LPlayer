LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libff
LOCAL_SRC_FILES := ffTest.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../ffmpeg/ \
                    $(LOCAL_PATH)/../ffmpeg/libavcodec \
                    $(LOCAL_PATH)/../ffmpeg/libavformat \
                    $(LOCAL_PATH)/../ffmpeg/libswscale

LOCAL_STATIC_LIBRARIES := libavformat libavcodec libswscale libavutil cpufeatures

LOCAL_LDLIBS := -lz -ljnigraphics

LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
