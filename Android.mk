LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_STATIC_JAVA_LIBRARIES := achartengine ksoap2

LOCAL_PACKAGE_NAME := DbstarLauncher
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)

########################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := achartengine:libs/achartengine-1.0.0.jar \
										ksoap2:libs/ksoap2-android-assembly-2.6.5-jar-with-dependencies.jar

include $(BUILD_MULTI_PREBUILT)

