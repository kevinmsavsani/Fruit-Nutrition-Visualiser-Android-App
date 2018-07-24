LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#opencv
#OPENCVROOT:= D:\\development\\Android\\OpenCV-android-sdk
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=SHARED
#include ${OPENCVROOT}\\sdk\\native\\jni\\OpenCV.mk
include /home/kevin/Downloads/OpenCV-3.1.0-android-sdk/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk



LOCAL_SRC_FILES := arnutrition.cpp
LOCAL_LDLIBS += -llog
LOCAL_MODULE := arnutrition

include $(BUILD_SHARED_LIBRARY)