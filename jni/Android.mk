LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := nonfree_prebuilt
LOCAL_SRC_FILES := libnonfree.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := opencv_java_prebuilt
LOCAL_SRC_FILES := libopencv_java.so
include $(PREBUILT_SHARED_LIBRARY)

# OpenCV
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
#include D:\Android\OpenCV-2.4.8-android-sdk\sdk\native\jni\OpenCV.mk
include D:/Android/OpenCV-2.4.8-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := COMSYSAndroidCV
LOCAL_SHARED_LIBRARIES := nonfree_prebuilt opencv_java_prebuilt
LOCAL_SRC_FILES := COMSYSAndroidCV.cpp \
                   DescriptorController.cpp \
                   ObjectTracker.cpp

LOCAL_LDLIBS    += -landroid -llog -ldl

include $(BUILD_SHARED_LIBRARY)
